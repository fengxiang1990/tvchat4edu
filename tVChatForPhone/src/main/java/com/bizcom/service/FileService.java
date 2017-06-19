package com.bizcom.service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.V2.jni.FileRequest;
import com.V2.jni.GroupRequest;
import com.V2.jni.callbacAdapter.FileRequestCallbackAdapter;
import com.bizcom.db.provider.ChatMessageProvider;
import com.bizcom.util.V2Log;
import com.bizcom.vo.FileInfoBean;
import com.bizcom.vo.meesage.VMessage;
import com.bizcom.vo.meesage.VMessageAbstractItem;
import com.bizcom.vo.meesage.VMessageFileItem;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

/**
 * Use to upload file and update uploading status to database
 * <ul>
 * intent key:  checkedFiles  : need to upload collection of files , FileInfoBean Object</br>
 * intent key:  gid  : if upload file to group, input group id</br>
 * </ul>
 * @author jiangzhen
 *
 */
public class FileService extends Service {

	private static final int UPDATE_FILE_STATE = 1;
	private static final int START_UPLOAD_FILE = 2;
	private Map<String, VMessageFileItem> mMoniterMap = new HashMap<String, VMessageFileItem>();

	private FileRequestCB frCB;
	private HandlerThread backThread;
	private Handler mLocalHandler;

	@Override
	public void onCreate() {
		super.onCreate();
		frCB = new FileRequestCB();
		FileRequest.getInstance().addCallback(frCB);
		backThread = new HandlerThread("FileUpdateService");
		backThread.start();
		mLocalHandler = new LocalHandler(backThread.getLooper());

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		FileRequest.getInstance().removeCallback(frCB);
		backThread.quit();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent != null){
			ArrayList<FileInfoBean> mCheckedList = intent.getParcelableArrayListExtra("uploads");
			long gid = intent.getLongExtra("gid", 0);
			if (mCheckedList != null && mCheckedList.size() > 0) {
				Message.obtain(mLocalHandler, START_UPLOAD_FILE, new LocalFileObject(mCheckedList , gid)).sendToTarget();
			} else {
				canQuit();
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * update file state to database
	 * @param uuid VMessageFileItem --> uuid
	 * @param st VMessageFileItem uploading state
	 */
	private void updateFile(String uuid, FileState st) {
		VMessageFileItem vMessageFileItem = mMoniterMap.get(uuid);
		switch (st) {
		case DONE:
			vMessageFileItem.setState(VMessageAbstractItem.STATE_FILE_SENT);
			break;
		case ERROR:
			vMessageFileItem.setState(VMessageAbstractItem.STATE_FILE_SENT_FALIED);
			break;
		case CANCEL:
			vMessageFileItem.setState(VMessageAbstractItem.STATE_FILE_SENDER_CANCEL);
			break;
		}
		//Update database
		ChatMessageProvider.updateFileItemState(this, vMessageFileItem);
		//remove cache
		mMoniterMap.remove(uuid);
		canQuit();
	}
	
	
	private boolean canQuit() {
		if (mMoniterMap.isEmpty()) {
			backThread.quit();
			this.stopSelf();
			return true;
		} else {
			return false;
		}
		
	}

	class FileRequestCB extends FileRequestCallbackAdapter {

		@Override
		public void OnFileTransEnd(String szFileID, String szFileName,
				long nFileSize, int nTransType) {
			if (mMoniterMap.containsKey(szFileID)) {
				Message.obtain(mLocalHandler, UPDATE_FILE_STATE,
						new TransObject(szFileID, FileState.DONE))
						.sendToTarget();
			}
		}
	}

	enum FileState {
		DONE, ERROR, CANCEL;
	}

	class TransObject {
		String uuid;
		FileState st;

		public TransObject(String uuid, FileState st) {
			super();
			this.uuid = uuid;
			this.st = st;
		}

	}

	class LocalFileObject {
		ArrayList<FileInfoBean> mCheckedList;
		long gid;

		public LocalFileObject(ArrayList<FileInfoBean> mCheckedList, long gid) {
			super();
			this.mCheckedList = mCheckedList;
			this.gid = gid;
		}

	}

	class LocalHandler extends Handler {

		public LocalHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATE_FILE_STATE:
				TransObject to = (TransObject) msg.obj;
				updateFile(to.uuid, to.st);
				break;
			case START_UPLOAD_FILE:
				LocalFileObject lfo = (LocalFileObject) msg.obj;
				for (FileInfoBean bean : lfo.mCheckedList) {
					if (bean == null || TextUtils.isEmpty(bean.filePath)){
						V2Log.e("FileService START_UPLOAD_FILE ---> send upload file failed , beacuse FileInfoBean is null or filePath is empty");
						continue;
					}
					//build VMessage Object and save in database.
					VMessage vm = new VMessage(V2GlobalConstants.GROUP_TYPE_CROWD, lfo.gid, GlobalHolder.getInstance().getCurrentUser(), null,
							new Date(GlobalConfig.getGlobalServerTime())); 
					VMessageFileItem item = new VMessageFileItem(vm , bean.filePath , 0);
					item.setUuid(bean.fileUUID);
					//put to map
					mMoniterMap.put(item.getUuid(), item);
					V2Log.d("FileService", "start upload a new file , group type : " + vm.getMsgCode()
					+ " | group id : " + vm.getGroupId() + " | xml : " + item.toXmlItem());
					//upload P2P or upload group file
					GroupRequest.getInstance().FileTransUploadGroupFile(vm.getMsgCode(),
							vm.getGroupId(), item.toXmlItem());
				}
				break;
			}
		}

	}

}
