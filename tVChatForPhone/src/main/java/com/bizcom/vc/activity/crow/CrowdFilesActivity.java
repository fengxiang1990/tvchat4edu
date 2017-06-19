package com.bizcom.vc.activity.crow;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.bizcom.bo.GroupUserObject;
import com.bizcom.db.provider.ChatMessageProvider;
import com.bizcom.request.V2CrowdGroupRequest;
import com.bizcom.request.jni.FileTransStatusIndication;
import com.bizcom.request.jni.FileTransStatusIndication.FileTransProgressStatusIndication;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.jni.JNIResponse.Result;
import com.bizcom.request.jni.RequestFetchGroupFilesResponse;
import com.bizcom.request.util.AsyncResult;
import com.bizcom.request.util.FileOperationEnum;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.service.JNIService;
import com.bizcom.util.AlgorithmUtil;
import com.bizcom.util.FileUtils;
import com.bizcom.util.MessageUtil;
import com.bizcom.util.V2Log;
import com.bizcom.util.V2Toast;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.activity.conversation.ConversationSelectFileActivity;
import com.bizcom.vc.listener.CommonCallBack;
import com.bizcom.vc.listener.CommonCallBack.CrowdFileExeType;
import com.bizcom.vc.widget.CustomAvatarImageView;
import com.bizcom.vo.CrowdGroup;
import com.bizcom.vo.FileDownLoadBean;
import com.bizcom.vo.FileInfoBean;
import com.bizcom.vo.VCrowdFile;
import com.bizcom.vo.VFile;
import com.bizcom.vo.VFile.State;
import com.bizcom.vo.enums.NetworkStateCode;
import com.bizcom.vo.meesage.VMessage;
import com.bizcom.vo.meesage.VMessageAbstractItem;
import com.bizcom.vo.meesage.VMessageFileItem;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CrowdFilesActivity extends Activity {

	private static final String TAG = CrowdFilesActivity.class.getSimpleName();

	private static final int FETCH_FILES_DONE = 0;

	private static final int OPERATE_FILE = 1;

	private static final int FILE_TRANS_NOTIFICATION = 2;

	private static final int FILE_REMOVE_NOTIFICATION = 3;

	private static final int NEW_FILE_NOTIFICATION = 5;

	private static final int SHOW_DELETE_BUTTON_FLAG = 1;

	private static final int HIDE_DELETE_BUTTON_FLAG = 0;

	private static final int RECEIVE_SELECTED_FILE = 200;

	// private static final int TYPE_FILE = 10;
	// private static final int TYPE_IMAGE = 11;
	// private static final int TYPE_AUDIO = 12;

	private Map<String, VCrowdFile> mShowProgressFileMap;
	private HashMap<String, VMessageFileItem> mLocalSaveFile;
	private List<VCrowdFile> mServerExistFiles;
	private List<VCrowdFile> mUploadedFiles;
	private Map<String, VMessage> mUploadingVMFiles;
	private ArrayList<FileInfoBean> mCheckedList;
	private List<String> mDownLoadFiles = new ArrayList<String>();

	private Context mContext;
	private LinearLayout mUploadFinish;
	private ListView mListView;
	private FileListAdapter adapter;
	private View mReturnButton;
	private View mCannelButton;
	private TextView mShowUploadedFileButton;
	private TextView mTitle;
	private ImageView mUploadingFileNotificationIcon;
	private boolean showUploaded;
	private boolean isInDeleteMode;
	private boolean isFromChatActivity; // 如果从聊天界面点击正在上传的文件，进入正在上传界面，就直接返回聊天界面
	private boolean isFromStartState = true;

	private V2CrowdGroupRequest service;
	private LocalHandler mLocalHandler = new LocalHandler();
	private CrowdGroup crowd;
	private LocalReceiver localReceiver;
	private long currentLoginUserID;
	private VCrowdFile waittingDelete;
	private boolean isScrollButtom;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.crowd_files_activity);
		currentLoginUserID = GlobalHolder.getInstance().getCurrentUserId();

		mUploadFinish = (LinearLayout) findViewById(R.id.crowd_files_uploaded_hint);
		mListView = (ListView) findViewById(R.id.crowd_files_list);
		mListView.setTextFilterEnabled(true);
		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				// 当不滚动时
				case OnScrollListener.SCROLL_STATE_IDLE:
					// 判断滚动到底部
					if (mListView.getLastVisiblePosition() == (mListView.getCount() - 1)) {
						isScrollButtom = true;
					} else {
						isScrollButtom = false;
					}
					break;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

			}
		});
		mTitle = (TextView) findViewById(R.id.crowd_files_title);
		mUploadingFileNotificationIcon = (ImageView) findViewById(R.id.crowd_file_upload_icon);
		mUploadingFileNotificationIcon.setOnClickListener(mShowUPloadingFileListener);

		mReturnButton = findViewById(R.id.crowd_members_return_button);
		mReturnButton.setOnClickListener(mBackButtonListener);

		mCannelButton = findViewById(R.id.crowd_files_uploaded_cancel_button);
		mCannelButton.setOnClickListener(mCannelButtonListener);

		mShowUploadedFileButton = (TextView) findViewById(R.id.crowd_files_uploaded_file_button);
		mShowUploadedFileButton.setOnClickListener(mShowUploadedFileButtonListener);

		mServerExistFiles = new ArrayList<VCrowdFile>();
		mUploadedFiles = new ArrayList<VCrowdFile>();
		mShowProgressFileMap = new HashMap<String, VCrowdFile>();
		mUploadingVMFiles = new HashMap<String, VMessage>();

		initReceiver();
		service = new V2CrowdGroupRequest();
		// register file transport listener
		service.registerFileTransStatusListener(mLocalHandler, FILE_TRANS_NOTIFICATION, null);
		// register file removed listener
		service.registerFileRemovedNotification(mLocalHandler, FILE_REMOVE_NOTIFICATION, null);

		service.registerNewFileNotification(mLocalHandler, NEW_FILE_NOTIFICATION, null);

		adapter = new FileListAdapter();
		mListView.setAdapter(adapter);
		mListView.setOnItemLongClickListener(mDeleteModeListener);
		overridePendingTransition(R.anim.left_in, R.anim.left_out);
		mLocalSaveFile = new HashMap<String, VMessageFileItem>();
		crowd = (CrowdGroup) GlobalHolder.getInstance().getGroupById(V2GlobalConstants.GROUP_TYPE_CROWD,
				getIntent().getLongExtra("cid", 0));
		// Reset crowd new file count to 0
		if (crowd == null) {
			finish();
			return;
		}
		crowd.resetNewFileCount();
		loadFiles();
		CrowdFileActivityType type = (CrowdFileActivityType) getIntent().getSerializableExtra("crowdFileActivityType");
		if (type != null) {
			if (CrowdFileActivityType.CROWD_FILE_UPLOING_ACTIVITY == type) {
				mUploadingFileNotificationIcon.performClick();
			}
			isFromChatActivity = true;
		}
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.right_in, R.anim.right_out);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RECEIVE_SELECTED_FILE) {
			if (data != null) {
				mCheckedList = data.getParcelableArrayListExtra("checkedFiles");
				if (mCheckedList.size() > 0) {
					for (int i = 0; i < mCheckedList.size(); i++) {
						FileInfoBean fb = mCheckedList.get(i);
						// 对象转换
						VCrowdFile vf = convertToVCrowdFile(fb);
						// 添加到集合
						mUploadedFiles.add(vf);
						mShowProgressFileMap.put(vf.getId(), vf);
						// 保存到数据库
						VMessage vm = MessageUtil.buildFileMessage(V2GlobalConstants.GROUP_TYPE_CROWD,
								crowd.getGroupID(), GlobalHolder.getInstance().getCurrentUser(), null, fb);
						VMessageFileItem fileItem = vm.getFileItems().get(0);
						fileItem.setUuid(vf.getId());
						vm.setmXmlDatas(vm.toXml());
						vm.setDate(new Date(GlobalConfig.getGlobalServerTime()));
						vm = ChatMessageProvider.saveChatMessage(vm);
						ChatMessageProvider.saveFileVMessage(vm);

						CommonCallBack.getInstance().executeUpdateCrowdFileState(vf.getId(), vm, null);
						mUploadingVMFiles.put(vf.getId(), vm);
						mLocalSaveFile.put(vf.getId(), fileItem);
						GlobalHolder.getInstance().changeGlobleTransFileMember(V2GlobalConstants.FILE_TRANS_SENDING,
								mContext, true, crowd.getGroupID(), "CrowdFilesActivity onActivityResult");
						File file = new File(fb.filePath);
						if (file.exists() && file.isFile()) {
							GlobalHolder.getInstance().mTransingLockFiles.put(vf.getId(), fb.filePath);
						}
						// 发送文件
						service.handleCrowdFile(vf, FileOperationEnum.OPERATION_START_SEND, null);
					}
					// Update screen to uploading UI
					adapterUploadShow();
					adapter.notifyDataSetChanged();
				}

				// 通知消息界面应该创建一个该群的消息记录
				Intent i = new Intent();
				i.setAction(PublicIntent.CHAT_SYNC_MESSAGE_INTERFACE);
				i.addCategory(PublicIntent.DEFAULT_CATEGORY);
				i.putExtra("groupType", V2GlobalConstants.GROUP_TYPE_CROWD);
				i.putExtra("groupID", crowd.getGroupID());
				i.putExtra("isDelete", false);
				sendBroadcast(i);
			}
		}
	}

	private VCrowdFile convertToVCrowdFile(FileInfoBean fb) {
		VCrowdFile vf = new VCrowdFile();
		vf.setCrowd(crowd);
		vf.setUploader(GlobalHolder.getInstance().getCurrentUser());
		vf.setId(UUID.randomUUID().toString());
		vf.setPath(fb.filePath);
		vf.setName(fb.fileName);
		vf.setSize(fb.fileSize);
		vf.setState(VFile.State.UPLOADING);
		return vf;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mShowProgressFileMap.clear();
		mShowProgressFileMap = null;
		mLocalSaveFile.clear();
		mLocalSaveFile = null;
		mServerExistFiles.clear();
		mServerExistFiles = null;
		mUploadedFiles.clear();
		mUploadedFiles = null;
		mUploadingVMFiles.clear();
		mUploadingVMFiles = null;
		if (mCheckedList != null) {
			mCheckedList.clear();
			mCheckedList = null;
		}
		mDownLoadFiles.clear();
		mDownLoadFiles = null;

		service.unRegisterFileTransStatusListener(mLocalHandler, FILE_TRANS_NOTIFICATION, null);
		service.unRegisterFileRemovedNotification(mLocalHandler, FILE_REMOVE_NOTIFICATION, null);

		service.unRegisterNewFileNotification(mLocalHandler, NEW_FILE_NOTIFICATION, null);
		service.clearCalledBack();
		this.unregisterReceiver(localReceiver);
	}

	@Override
	public void onBackPressed() {
		if (isInDeleteMode) {
			mCannelButton.setVisibility(View.INVISIBLE);
			mUploadingFileNotificationIcon.setClickable(true);
			isInDeleteMode = false;
			// resume all uploading files
			if (showUploaded)
				suspendOrResumeUploadingFiles(false);
			else
				suspendOrResumeDownloadingFiles(false);
			adapter.notifyDataSetChanged();
			// set cancel button text to upload text
			mShowUploadedFileButton.setText(R.string.crowd_files_title_upload);
			return;
		} else if (showUploaded) {
//			if (isFromChatActivity) {
//				super.onBackPressed();
//				return;
//			}
			showUploaded = false;
			mTitle.setText(R.string.crowd_files_title);
			mShowUploadedFileButton.setText(R.string.crowd_files_title_upload);
			mShowUploadedFileButton.setVisibility(View.VISIBLE);
			adapterUploadShow();
			adapter.notifyDataSetChanged();
			return;
		}

		if (isFromChatActivity) {
			super.onBackPressed();
			return;
		}
		Intent i = new Intent(PublicIntent.SHOW_CROWD_DETAIL_ACTIVITY);
		i.addCategory(PublicIntent.DEFAULT_CATEGORY);
		i.putExtra("cid", crowd.getGroupID());
		startActivity(i);
		crowd.resetNewFileCount();
		finish();
	}

	private void initReceiver() {
		localReceiver = new LocalReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(PublicIntent.BROADCAST_GROUP_DELETED_NOTIFICATION);
		filter.addAction(JNIService.JNI_BROADCAST_KICED_CROWD);
		filter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED);
		filter.addAction(JNIService.JNI_BROADCAST_CONNECT_STATE_NOTIFICATION);
		filter.addAction(JNIService.JNI_BROADCAST_FILE_STATUS_ERROR_NOTIFICATION);
		filter.addAction(JNIService.BROADCAST_CROWD_NEW_UPLOAD_FILE_NOTIFICATION);
		filter.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
		filter.addCategory(PublicIntent.DEFAULT_CATEGORY);
		this.registerReceiver(localReceiver, filter);
	}

	private void loadFiles() {
		if (crowd == null) {
			V2Log.e("Unknow crowd");
			return;
		}
		// loading local files and judging whether show uploading icon
		loadLocalSaveFile();
		// fetch files from server
		service.fetchGroupFiles(crowd, new HandlerWrap(mLocalHandler, FETCH_FILES_DONE, null));
	}

	private void loadLocalSaveFile() {
		List<VMessageFileItem> fileItems = ChatMessageProvider.loadGroupFileItemConvertToVCrowdFile(crowd.getGroupID(),
				crowd);
		if (fileItems != null && fileItems.size() > 0) {
			for (int i = 0; i < fileItems.size(); i++) {
				VMessageFileItem temp = fileItems.get(i);
				VCrowdFile crowdFile = ChatMessageProvider.convertToVCrowdFile(temp, crowd);
				mLocalSaveFile.put(temp.getUuid(), temp);

				if (temp.getState() == VMessageAbstractItem.STATE_FILE_SENDING
						|| temp.getState() == VMessageAbstractItem.STATE_FILE_SENT_FALIED
						|| temp.getState() == VMessageAbstractItem.STATE_FILE_PAUSED_SENDING
						|| temp.getState() == VMessageAbstractItem.STATE_FILE_PAUSED_DOWNLOADING
						|| temp.getState() == VMessageAbstractItem.STATE_FILE_DOWNLOADED_FALIED
						|| temp.getState() == VMessageAbstractItem.STATE_FILE_DOWNLOADING) {
					mUploadingVMFiles.put(fileItems.get(i).getUuid(), fileItems.get(i).getVm());

					if (temp.getState() == VMessageAbstractItem.STATE_FILE_SENDING
							|| temp.getState() == VMessageAbstractItem.STATE_FILE_SENT_FALIED
							|| temp.getState() == VMessageAbstractItem.STATE_FILE_PAUSED_SENDING) {
						mUploadedFiles.add(crowdFile);
					}

					if (temp.getState() == VMessageAbstractItem.STATE_FILE_SENDING
							|| temp.getState() == VMessageAbstractItem.STATE_FILE_PAUSED_SENDING
							|| temp.getState() == VMessageAbstractItem.STATE_FILE_PAUSED_DOWNLOADING
							|| temp.getState() == VMessageAbstractItem.STATE_FILE_DOWNLOADING) {
						mShowProgressFileMap.put(crowdFile.getId(), crowdFile);
					}
				}
			}

			if (mUploadedFiles.size() > 0)
				mUploadingFileNotificationIcon.setVisibility(View.VISIBLE);
			else
				mUploadingFileNotificationIcon.setVisibility(View.INVISIBLE);
		} else
			mUploadingFileNotificationIcon.setVisibility(View.INVISIBLE);
	}

	/**
	 * Save file list and update file state
	 * 
	 * @param files
	 */
	private void handleFetchFilesDone(List<VCrowdFile> fetchFiles) {
		if (fetchFiles == null) {
			V2Log.e("Fetch files from server side failed ! Because get collection is null !");
			return;
		}

		mServerExistFiles.clear();
		mServerExistFiles.addAll(fetchFiles);
		for (VCrowdFile serverFile : mServerExistFiles) {
			// 从数据库读取该文件存的状态
			VMessageFileItem dataBaseSaveFile = mLocalSaveFile.get(serverFile.getId());
			if (dataBaseSaveFile != null && serverFile.getId().equals(dataBaseSaveFile.getUuid())) {
				String localName = dataBaseSaveFile.getFileName();
				serverFile.setName(localName);

				String serverPath = serverFile.getPath();
				String prefix = serverPath.substring(0, serverPath.lastIndexOf("/"));
				String newPath = prefix + "/" + localName;
				serverFile.setPath(newPath);
			}

			// 自己上传的文件，从数据库获取文件路径，判断文件是否存在
			if (serverFile.getUploader().getmUserId() == GlobalHolder.getInstance().getCurrentUserId()) {
				if (dataBaseSaveFile != null) {
					File ownerFile = new File(dataBaseSaveFile.getFilePath());
					if (!ownerFile.exists()) {
						if (State.fromInt(dataBaseSaveFile.getState()) == VCrowdFile.State.DOWNLOADED)
							serverFile.setState(VCrowdFile.State.UNKNOWN);
						else
							serverFile.setState(State.fromInt(dataBaseSaveFile.getState()));
					} else {
						if (ownerFile.length() == serverFile.getSize()) {
							serverFile.setState(VCrowdFile.State.DOWNLOADED);
							serverFile.setPath(dataBaseSaveFile.getFilePath());
						} else {
							ownerFile.delete();
							if (State.fromInt(dataBaseSaveFile.getState()) == VCrowdFile.State.DOWNLOADED)
								serverFile.setState(VCrowdFile.State.UNKNOWN);
							else
								serverFile.setState(State.fromInt(dataBaseSaveFile.getState()));
						}
					}
				} else {
					serverFile.setState(VCrowdFile.State.UNKNOWN);
				}
			} else {
				// 其他人的文件路径，则根据文件类型，通过默认路径去判断
				File localFile = buildDefaultFilePath(serverFile);
				if (!localFile.exists()) {
					if (dataBaseSaveFile != null) {
						changeServerFileState(serverFile, dataBaseSaveFile, localFile);
					} else
						serverFile.setState(VCrowdFile.State.UNKNOWN);
				} else {
					if (dataBaseSaveFile == null) {
						if (localFile.length() == serverFile.getSize()) {
							serverFile.setState(VCrowdFile.State.DOWNLOADED);
						} else {
							localFile.delete();
							serverFile.setState(VCrowdFile.State.UNKNOWN);
						}
					} else {
						changeServerFileState(serverFile, dataBaseSaveFile, localFile);
					}
				}
			}
		}
		adapter.notifyDataSetChanged();
	}

	private File buildDefaultFilePath(VCrowdFile serverFile) {
		// int fileDirType = -1;
		// int index = serverFile.getName().indexOf(".");
		// if(index == -1)
		// fileDirType = TYPE_FILE;
		// else{
		// String postfixName = serverFile.getName().substring(index);
		// if(postfixName.equals(".aac"))
		// fileDirType = TYPE_AUDIO;
		//
		// FileType type = FileUitls.getFileType(postfixName);
		// if(type == FileType.IMAGE)
		// fileDirType = TYPE_IMAGE;
		// else
		// fileDirType = TYPE_FILE;
		// }
		//
		// File localFile = null;
		// if(fileDirType == TYPE_IMAGE)
		// localFile = new File(GlobalConfig.getGlobalPicsPath() + "/" +
		// serverFile.getName());
		// else if(fileDirType == TYPE_AUDIO)
		// localFile = new File(GlobalConfig.getGlobalAudioPath() + "/" +
		// serverFile.getName());
		// else
		// localFile = new File(GlobalConfig.getGlobalFilePath() + "/" +
		// serverFile.getName());
		// return localFile;
		return new File(GlobalConfig.getGlobalFilePath() + "/" + serverFile.getName());
	}

	private void changeServerFileState(VCrowdFile serverFile, VMessageFileItem dataBaseVm, File localFile) {

		VCrowdFile dataBaseSaveFile = ChatMessageProvider.convertToVCrowdFile(dataBaseVm, crowd);

		State state = dataBaseSaveFile.getState();
		if (state == State.DOWNLOADING || state == State.DOWNLOAD_PAUSE) {
			mShowProgressFileMap.put(serverFile.getId(), serverFile);
		}

		if (state == State.DOWNLOADED) {
			if (localFile.exists() && localFile.length() == serverFile.getSize()) {
				serverFile.setState(VCrowdFile.State.DOWNLOADED);
			} else {
				localFile.delete();
				serverFile.setState(VCrowdFile.State.UNKNOWN);
			}
		} else {
			if (serverFile.getId().equals(dataBaseSaveFile.getId())) {
				if (state == State.UPLOADED && dataBaseSaveFile.getUploader().getmUserId() != GlobalHolder.getInstance()
						.getCurrentUserId()) {
					serverFile.setPath(dataBaseSaveFile.getPath());
					serverFile.setState(State.UNKNOWN);
				} else
					serverFile.setState(state);
			}
		}
	}

	private void handleFileTransNotification(FileTransStatusIndication ind) {
		if (mShowProgressFileMap == null) {
			return;
		}

		VCrowdFile file = mShowProgressFileMap.get(ind.uuid);
		if (file == null) {
			V2Log.e(TAG, "File id doesn't exist: " + ind.uuid);
			return;
		}

		if (mDownLoadFiles.contains(ind.uuid)) {
			V2Log.d(TAG, "the file : " + ind.uuid + " has already finish!");
			return;
		}

		if (ind.indType == FileTransStatusIndication.IND_TYPE_PROGRESS) {
			FileTransProgressStatusIndication progress = (FileTransProgressStatusIndication) ind;
			V2Log.e("CrowdFilesActivity handleFileTransNotification --> "
					+ "receive progress upload file state.... normal , file id is : " + file.getId()
					+ " file name is : " + file.getName());
			if (progress.progressType == FileTransStatusIndication.IND_TYPE_PROGRESS_END) {
				file.setProceedSize(file.getSize());
				mDownLoadFiles.add(file.getId());
				mShowProgressFileMap.remove(ind.uuid);
			} else {
				isFromStartState = false;
				file.setProceedSize(progress.nTranedSize);
			}
			if (file.getProceedSize() == file.getSize()
					&& file.getUploader().getmUserId() == GlobalHolder.getInstance().getCurrentUserId()) {
				this.mUploadedFiles.remove(file);
				adapterUploadShow();
			}

		} else if (ind.indType == FileTransStatusIndication.IND_TYPE_DOWNLOAD_ERR) {
			if (file.getState() == VFile.State.DOWNLOADING)
				file.setState(VFile.State.DOWNLOAD_FAILED);
			else
				file.setState(VFile.State.UPLOAD_FAILED);
			V2Log.e("CrowdFilesActivity handleFileTransNotification --> DWONLOAD_ERROR ...file id is : " + file.getId()
					+ " file name is : " + file.getName() + " error code is : " + ind.errorCode);
			mShowProgressFileMap.remove(ind.uuid);
			mUploadingVMFiles.remove(ind.uuid);
		} else if (ind.indType == FileTransStatusIndication.IND_TYPE_TRANS_ERR) {
			if (file.getState() == VFile.State.DOWNLOADING) {
				file.setState(VFile.State.DOWNLOAD_FAILED);
			} else {
				file.setState(VFile.State.UPLOAD_FAILED);
			}
			V2Log.e("CrowdFilesActivity handleFileTransNotification --> TRANS_ERROR ...file id is : " + file.getId()
					+ " file name is : " + file.getName() + " error code is : " + ind.errorCode);

			Intent i = new Intent();
			i.addCategory(PublicIntent.DEFAULT_CATEGORY);
			i.setAction(PublicIntent.BROADCAST_CROWD_FILE_ACTIVITY_SEND_NOTIFICATION);
			i.putExtra("fileID", file.getId());
			sendBroadcast(i);
			mShowProgressFileMap.remove(ind.uuid);
			mUploadingVMFiles.remove(ind.uuid);
		}
		adapter.notifyDataSetChanged();
	}

	/**
	 * Handle file removed notification
	 * 
	 * @param files
	 */
	private void handleFileRemovedEvent(List<VCrowdFile> files) {
		for (VCrowdFile removedFile : files) {
			for (int i = 0; i < mServerExistFiles.size(); i++) {
				if (mServerExistFiles.get(i).getId().equals(removedFile.getId())) {
					VCrowdFile file = mServerExistFiles.get(i);
					V2Log.d(TAG, "handleFileRemovedEvent -->The group file was remove! cancel downloading was called!");
					if (file.getState() == State.DOWNLOADING) {
						GlobalHolder.getInstance().changeGlobleTransFileMember(V2GlobalConstants.FILE_TRANS_DOWNLOADING,
								mContext, false, crowd.getGroupID(), "CrowdFilesActivity handleFileRemovedEvent");
						service.handleCrowdFile(file, FileOperationEnum.OPERATION_CANCEL_DOWNLOADING, null);
						file.setState(VFile.State.REMOVED);
					} else {
						mServerExistFiles.remove(i);
					}
					break;
				}
			}
		}
		adapter.notifyDataSetChanged();
	}

	private void suspendOrResumeUploadingFiles(boolean flag) {
		for (int i = 0; i < this.mUploadedFiles.size(); i++) {
			VCrowdFile vf = mUploadedFiles.get(i);
			if (flag) {
				if (vf.getState() != VFile.State.UPLOAD_FAILED) {
					vf.setState(VFile.State.UPLOAD_PAUSE);
					service.handleCrowdFile(vf, FileOperationEnum.OPERATION_PAUSE_SENDING, null);
				}
			} else {
				if (vf.getState() != VFile.State.UPLOAD_FAILED) {
					vf.setState(VFile.State.UPLOADING);
					service.handleCrowdFile(vf, FileOperationEnum.OPERATION_RESUME_SEND, null);
				}
			}
		}
	}

	private void suspendOrResumeDownloadingFiles(boolean flag) {
		for (int i = 0; i < this.mServerExistFiles.size(); i++) {
			VCrowdFile vf = mServerExistFiles.get(i);
			if (flag) {
				if (vf.getState() == VFile.State.DOWNLOADING) {
					vf.setState(VFile.State.DOWNLOAD_PAUSE);
					service.handleCrowdFile(vf, FileOperationEnum.OPERATION_PAUSE_DOWNLOADING, null);
				}
			} else {
				if (vf.getState() == VFile.State.DOWNLOAD_PAUSE) {
					vf.setState(VFile.State.DOWNLOADING);
					service.handleCrowdFile(vf, FileOperationEnum.OPERATION_RESUME_DOWNLOAD, null);
				}
			}
		}
	}

	/**
	 * Handle new file notification
	 * 
	 * @param files
	 */
	private void handleNewFileEvent(List<VCrowdFile> files) {
		for (VCrowdFile vCrowdFile : files) {
			VMessage vm = mUploadingVMFiles.get(vCrowdFile.getId());
			if (vm != null) {
				vCrowdFile.setState(State.DOWNLOADED);
				vCrowdFile.setPath(vm.getFileItems().get(0).getFilePath());
			}
			mServerExistFiles.add(0, vCrowdFile);
		}
		adapter.notifyDataSetChanged();
	}

	private void handleFileRemovedEvent(VCrowdFile file) {
		if (file == null) {
			return;
		}

		List<VCrowdFile> list;
		if (showUploaded) {
			list = mUploadedFiles;
		} else {
			list = mServerExistFiles;
		}
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getId().equals(file.getId())) {
				list.remove(i);
				if (file.getState() == VCrowdFile.State.DOWNLOADING) {
					V2Log.d(TAG, "handleFileRemovedEvent --> cancel downloading was called!");
					service.handleCrowdFile(file, FileOperationEnum.OPERATION_CANCEL_DOWNLOADING, null);
				}
				break;
			}
		}
		mLocalSaveFile.remove(file.getId());
		mShowProgressFileMap.remove(file.getId());
		mUploadingVMFiles.remove(file.getId());
		adapter.notifyDataSetChanged();
	}

	/**
	 * adapter upload show style
	 */
	private void adapterUploadShow() {
		if (showUploaded) {
			mUploadingFileNotificationIcon.setVisibility(View.INVISIBLE);
			if (mUploadedFiles.size() <= 0){
//				mUploadFinish.setVisibility(View.VISIBLE);
				onBackPressed();
			}
			else
				mUploadFinish.setVisibility(View.INVISIBLE);
		} else {
			mUploadFinish.setVisibility(View.INVISIBLE);
			if (mUploadedFiles.size() > 0)
				mUploadingFileNotificationIcon.setVisibility(View.VISIBLE);
			else
				mUploadingFileNotificationIcon.setVisibility(View.INVISIBLE);
		}
	}

	private OnClickListener mBackButtonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			onBackPressed();
		}
	};

	private OnClickListener mCannelButtonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			onBackPressed();
		}
	};

	private OnClickListener mShowUPloadingFileListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// Update screen to uploading UI
			showUploaded = true;
			mTitle.setText(R.string.crowd_files_title_uploading);
			mShowUploadedFileButton.setVisibility(View.INVISIBLE);
			adapterUploadShow();
			adapter.notifyDataSetChanged();
		}
	};

	private OnClickListener mShowUploadedFileButtonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (isInDeleteMode) {
				// // set cancel button text to upload text
				mShowUploadedFileButton.setText(R.string.crowd_files_title_upload);
				onBackPressed();
				// isInDeleteMode = false;
				// suspendOrResumeDownloadingFiles(false);
				// adapter.notifyDataSetChanged();
			} else {
				Intent intent = new Intent(mContext, ConversationSelectFileActivity.class);
				intent.putExtra("type", "crowdFile");
				intent.putExtra("uid", crowd.getGroupID());
				startActivityForResult(intent, RECEIVE_SELECTED_FILE);
			}
		}

	};

	private OnItemLongClickListener mDeleteModeListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			if (isInDeleteMode) {
				if (showUploaded) {
					mCannelButton.setVisibility(View.INVISIBLE);
				}
				return false;
			} else {
				if (showUploaded) {
					mCannelButton.setVisibility(View.VISIBLE);
					isInDeleteMode = true;
					// Pause all uploading files
					suspendOrResumeUploadingFiles(true);
					adapter.notifyDataSetChanged();
				} else {
					boolean showDeleteMode = false;
					if (crowd.getOwnerUser().getmUserId() == currentLoginUserID) {
						showDeleteMode = true;
						suspendOrResumeDownloadingFiles(true);
					} else {
						for (VCrowdFile file : mServerExistFiles) {
							if (file.getUploader().getmUserId() == currentLoginUserID) {
								showDeleteMode = true;
								break;
							}
						}
					}

					if (showDeleteMode) {
						isInDeleteMode = true;
						// Pause all uploading files
						adapter.notifyDataSetChanged();
						// update upload button text to cancel
						mShowUploadedFileButton.setText(R.string.crowd_files_title_cancel_button);
						mUploadingFileNotificationIcon.setClickable(false);
					}
				}
				return true;
			}
		}

	};

	class LocalHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case FETCH_FILES_DONE:
				JNIResponse res = (JNIResponse) msg.obj;
				if (res.getResult() == JNIResponse.Result.SUCCESS) {
					RequestFetchGroupFilesResponse rf = (RequestFetchGroupFilesResponse) res;
					handleFetchFilesDone(rf.getList());
				} else if (res.getResult() != JNIResponse.Result.SUCCESS
						& res.getResult() != JNIResponse.Result.TIME_OUT) {
					V2Log.e(TAG, "Get upload files failed ... ERROR CODE IS : " + res.getResult().name());
					Toast.makeText(mContext, getResources().getString(R.string.crowd_files_fill_adapter_failed),
							Toast.LENGTH_SHORT).show();
				}
				break;
			case OPERATE_FILE:
				break;
			case FILE_TRANS_NOTIFICATION:
				FileTransStatusIndication ind = (FileTransStatusIndication) (((AsyncResult) msg.obj).getResult());
				handleFileTransNotification(ind);
				break;
			case NEW_FILE_NOTIFICATION:
				AsyncResult result = (AsyncResult) msg.obj;
				RequestFetchGroupFilesResponse newFileni = (RequestFetchGroupFilesResponse) result.getResult();
				if (newFileni.getResult() == JNIResponse.Result.SUCCESS) {
					if (newFileni.getGroupID() == crowd.getGroupID()) {
						handleNewFileEvent(newFileni.getList());
						adapterUploadShow();
					}
				}
				break;
			case FILE_REMOVE_NOTIFICATION:
				WaitDialogBuilder.dismissDialog();
				AsyncResult asyResult = (AsyncResult) msg.obj;
				JNIResponse remove = (JNIResponse) asyResult.getResult();
				if (remove.getResult() == Result.SUCCESS) {
					if (waittingDelete != null) {
						int transType = -1;
						if (waittingDelete.getState() == State.DOWNLOADING
								|| waittingDelete.getState() == State.DOWNLOAD_PAUSE) {
							transType = V2GlobalConstants.FILE_TRANS_DOWNLOADING;
						} else if (waittingDelete.getState() == State.UPLOADING
								|| waittingDelete.getState() == State.UPLOAD_PAUSE) {
							transType = V2GlobalConstants.FILE_TRANS_SENDING;
						}

						GlobalHolder.getInstance().changeGlobleTransFileMember(transType, mContext, false,
								crowd.getGroupID(), "CrowdFilesActivity mDeleteButtonListener");
						VMessage vm = mUploadingVMFiles.get(waittingDelete.getId());
						if (vm != null) {
							ChatMessageProvider.deleteMessage(mContext, vm, true);
							CommonCallBack.getInstance().executeUpdateCrowdFileState(waittingDelete.getId(), vm,
									CrowdFileExeType.DELETE_FILE);
						}
						handleFileRemovedEvent(waittingDelete);
						waittingDelete = null;
						onBackPressed();
						// FIXME 正在上传界面，当删除完文件，应该显示空的图片
					} else {
						handleFileRemovedEvent(((RequestFetchGroupFilesResponse) remove).getList());
					}
				} else if (remove.getResult() == Result.TIME_OUT) {
					Toast.makeText(mContext, getResources().getString(R.string.error_time_out), Toast.LENGTH_SHORT)
							.show();
				}
				waittingDelete = null;
				break;
			}
		}

	};

	class LocalReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(JNIService.JNI_BROADCAST_KICED_CROWD)) {
				GroupUserObject obj = intent.getParcelableExtra("group");
				if (obj == null) {
					V2Log.e("CrowdFilesActivity",
							"Received the broadcast to quit the crowd group , but crowd id is wroing... ");
					return;
				}
				if (obj.getmGroupId() == crowd.getGroupID()) {
					for (VCrowdFile f : mServerExistFiles) {
						if (f.getState() == VFile.State.DOWNLOADING) {
							V2Log.d(TAG, "JNI_BROADCAST_KICED_CROWD --> cancel downloading was called!");
							service.handleCrowdFile(f, FileOperationEnum.OPERATION_CANCEL_DOWNLOADING, null);
						} else if (f.getState() == VFile.State.UPLOADING) {
							V2Log.d(TAG, "JNI_BROADCAST_KICED_CROWD --> cancel sending was called!");
							service.handleCrowdFile(f, FileOperationEnum.OPERATION_CANCEL_SENDING, null);
						}
					}
					finish();
				}
			} else if (intent.getAction().equals(JNIService.JNI_BROADCAST_CONNECT_STATE_NOTIFICATION)) {
				NetworkStateCode code = (NetworkStateCode) intent.getExtras().get("state");
				if (code != NetworkStateCode.CONNECTED) {
					for (int i = 0; i < mUploadedFiles.size(); i++) {
						VCrowdFile temp = mUploadedFiles.get(i);
						if (temp.getState() == State.UPLOAD_PAUSE || temp.getState() == State.UPLOADING) {
							temp.setState(State.UPLOAD_FAILED);
						}
					}

					for (int i = 0; i < mServerExistFiles.size(); i++) {
						VCrowdFile temp = mServerExistFiles.get(i);
						if (temp.getState() == State.DOWNLOAD_PAUSE || temp.getState() == State.DOWNLOADING) {
							temp.setState(State.DOWNLOAD_FAILED);
						}
					}
					adapter.notifyDataSetChanged();
				}
			} else if (intent.getAction().equals(JNIService.JNI_BROADCAST_FILE_STATUS_ERROR_NOTIFICATION)) {
				String fileID = intent.getStringExtra("fileID");
				int transType = intent.getIntExtra("transType", -1);
				if (fileID == null || transType == -1)
					return;

				if (transType == V2GlobalConstants.FILE_TRANS_SENDING) {
					for (int i = 0; i < mUploadedFiles.size(); i++) {
						VCrowdFile temp = mUploadedFiles.get(i);
						if (temp.getId().equals(fileID)) {
							temp.setState(State.UPLOAD_FAILED);
							break;
						}
					}
				} else {
					for (int i = 0; i < mServerExistFiles.size(); i++) {
						VCrowdFile temp = mServerExistFiles.get(i);
						if (temp.getId().equals(fileID)) {
							temp.setState(State.DOWNLOAD_FAILED);
							break;
						}
					}
				}
				adapter.notifyDataSetChanged();
			} else if (intent.getAction().equals(JNIService.BROADCAST_CROWD_NEW_UPLOAD_FILE_NOTIFICATION)) {
				long crowdId = intent.getLongExtra("groupID", 0);
				if (crowdId == crowd.getGroupID()) {
					crowd.resetNewFileCount();
				}
			} else if (JNIService.JNI_BROADCAST_GROUP_USER_REMOVED.equals(intent.getAction())) {
				GroupUserObject guo = (GroupUserObject) intent.getExtras().get("obj");
				long ownerUserID = crowd.getOwnerUser().getmUserId();
				if (guo.getmUserId() == ownerUserID) {
					finish();
				}
			} else if (PublicIntent.BROADCAST_GROUP_DELETED_NOTIFICATION.equals(intent.getAction())) {
				GroupUserObject obj = intent.getParcelableExtra("group");
				if (obj == null) {
					V2Log.e("CrowdDetailActivity",
							"Received the broadcast to quit the crowd group , but crowd id is wroing... ");
					return;
				}
				if (obj.getmGroupId() == crowd.getGroupID()) {
					finish();
				}
			}
		}
	}

	class FileListAdapter extends BaseAdapter implements Filterable {
		private int progressLayoutWidth;
		private Map<ViewItem, VCrowdFile> items = new HashMap<CrowdFilesActivity.FileListAdapter.ViewItem, VCrowdFile>();

		class ViewItem {
			ImageView mFileDeleteModeButton;
			CustomAvatarImageView mFileIcon;
			TextView mFileName;
			TextView mFileSize;
			TextView mFileButton;
			TextView mFileText;
			TextView mVelocity;
			ImageView mProgress;
			TextView mFileProgress;
			View mProgressParent;
			ImageView mFailedIcon;
			TextView mFileDeleteButton;
			View mProgressLayout;
		}

		class Tag {
			VCrowdFile vf;
			ViewItem item;

			public Tag(VCrowdFile vf, ViewItem item) {
				super();
				this.vf = vf;
				this.item = item;
			}

		}

		private LayoutInflater layoutInflater;

		public FileListAdapter() {
			layoutInflater = LayoutInflater.from(mContext);
		}

		@Override
		public int getCount() {
			if (showUploaded) {
				return mUploadedFiles.size();
			} else {
				return mServerExistFiles.size();
			}
		}

		@Override
		public Object getItem(int position) {
			if (showUploaded) {
				return mUploadedFiles.get(position);
			} else {
				return mServerExistFiles.get(position);
			}
		}

		@Override
		public long getItemId(int position) {
			if (showUploaded) {
				return mUploadedFiles.get(position).hashCode();
			} else {
				return mServerExistFiles.get(position).hashCode();
			}
		}

		@Override
		public Filter getFilter() {
			return null;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			VCrowdFile file = null;
			if (showUploaded) {
				file = mUploadedFiles.get(position);
			} else {
				file = mServerExistFiles.get(position);
			}

			ViewItem item = null;
			Tag tag = null;
			if (convertView == null) {
				convertView = layoutInflater.inflate(R.layout.crowd_file_adapter_item, null);
				item = new ViewItem();
				tag = new Tag(file, item);
				item.mFileDeleteModeButton = (ImageView) convertView.findViewById(R.id.ic_delete);
				item.mFileDeleteModeButton.setTag(tag);
				item.mFileDeleteModeButton.setOnClickListener(mDeleteModeButtonListener);

				item.mFileIcon = (CustomAvatarImageView) convertView.findViewById(R.id.ws_common_avatar);
				item.mFileName = (TextView) convertView.findViewById(R.id.ws_common_conversation_layout_topContent);
				item.mFileSize = (TextView) convertView.findViewById(R.id.ws_common_conversation_layout_belowContent);
				item.mFileButton = (TextView) convertView.findViewById(R.id.crowd_file_button);

				item.mFileText = (TextView) convertView.findViewById(R.id.crowd_file_text);
				item.mVelocity = (TextView) convertView.findViewById(R.id.file_velocity);
				item.mFileProgress = (TextView) convertView.findViewById(R.id.file_process_percent);
				item.mProgressParent = convertView.findViewById(R.id.file_download_progress_state_ly);
				item.mProgress = (ImageView) convertView.findViewById(R.id.ile_download_progress_state);
				item.mFailedIcon = (ImageView) convertView.findViewById(R.id.crowd_file_failed_icon);
				item.mFileDeleteButton = (TextView) convertView.findViewById(R.id.crowd_file_delete_button);
				item.mFileDeleteButton.setTag(tag);
				item.mFileDeleteButton.setOnClickListener(mDeleteButtonListener);
				item.mFailedIcon.setOnClickListener(mFailIconListener);
				item.mFileButton.setOnClickListener(mButtonListener);

				item.mProgressLayout = convertView.findViewById(R.id.crowd_file_item_progrss_ly);
				convertView.setTag(tag);
			} else {
				tag = (Tag) convertView.getTag();
				item = tag.item;
				tag.vf = file;
			}

			updateViewItem(tag);
			return convertView;
		}

		private void updateViewItem(Tag tag) {
			final VCrowdFile file = tag.vf;
			final ViewItem item = tag.item;
			item.mFileButton.setTag(file);
			item.mFailedIcon.setTag(file);
			item.mFileName.setText(file.getName());
			item.mFileSize.setText(file.getFileSizeStr());
			// item.mFileButton.setTag(file);
			// item.mFailedIcon.setTag(file);
			VFile.State fs = file.getState();

			if (isInDeleteMode && (file.getUploader().getmUserId() == currentLoginUserID
					|| GlobalHolder.getInstance().getCurrentUserId() == crowd.getOwnerUser().getmUserId())) {
				item.mFileDeleteModeButton.setVisibility(View.VISIBLE);
			} else {
				item.mFileDeleteModeButton.setVisibility(View.GONE);
				// Record flag for show delete button
				file.setFlag(HIDE_DELETE_BUTTON_FLAG);
			}

			item.mFileIcon.setImageResource(FileUtils.adapterFileIcon(file.getName()));

			switch (fs) {
			case UNKNOWN:
				item.mFileButton.setText(R.string.crowd_files_button_name_download);
				item.mFailedIcon.setVisibility(View.INVISIBLE);
				item.mFileText.setVisibility(View.INVISIBLE);
				item.mFileButton.setVisibility(View.VISIBLE);
				item.mProgressLayout.setVisibility(View.GONE);
				break;
			case UPLOADING:
			case DOWNLOADING:
				item.mFileButton.setText(R.string.crowd_files_button_name_pause);
				item.mFileButton.setVisibility(View.VISIBLE);
				item.mFailedIcon.setVisibility(View.INVISIBLE);
				item.mFileText.setVisibility(View.INVISIBLE);
				item.mProgressLayout.setVisibility(View.VISIBLE);
				break;
			case UPLOAD_PAUSE:
			case DOWNLOAD_PAUSE:
				item.mFileButton.setText(R.string.crowd_files_button_name_resume);
				item.mFailedIcon.setVisibility(View.INVISIBLE);
				item.mFileText.setVisibility(View.INVISIBLE);
				item.mFileButton.setVisibility(View.VISIBLE);
				item.mProgressLayout.setVisibility(View.VISIBLE);
				break;
			case DOWNLOADED:
				item.mFileButton.setText(R.string.crowd_files_name_open_file);
				item.mFileButton.setVisibility(View.VISIBLE);
				item.mFileText.setVisibility(View.INVISIBLE);
				item.mFileProgress.setVisibility(View.INVISIBLE);
				item.mFailedIcon.setVisibility(View.INVISIBLE);
				item.mProgressLayout.setVisibility(View.GONE);
				break;
			case UPLOADED:
				if (file.getUploader().getmUserId() == GlobalHolder.getInstance().getCurrentUserId()) {
					item.mFileButton.setText(R.string.crowd_files_name_open_file);
					item.mFileButton.setVisibility(View.VISIBLE);
					item.mFileText.setVisibility(View.INVISIBLE);
					item.mFileProgress.setVisibility(View.INVISIBLE);
					item.mFailedIcon.setVisibility(View.INVISIBLE);
					item.mProgressLayout.setVisibility(View.GONE);
				} else {
					item.mFileButton.setText(R.string.crowd_files_button_name_download);
					item.mFailedIcon.setVisibility(View.INVISIBLE);
					item.mFileText.setVisibility(View.INVISIBLE);
					item.mFileButton.setVisibility(View.VISIBLE);
					item.mProgressLayout.setVisibility(View.GONE);
				}
				break;
			case DOWNLOAD_FAILED:
			case UPLOAD_FAILED:
			case REMOVED:
				item.mFailedIcon.setVisibility(View.VISIBLE);
				item.mFileButton.setVisibility(View.INVISIBLE);
				item.mFileText.setVisibility(View.INVISIBLE);
				item.mProgressLayout.setVisibility(View.GONE);
				break;
			default:
				break;
			}

			if (file.getFlag() == SHOW_DELETE_BUTTON_FLAG) {
				item.mFileDeleteButton.setVisibility(View.VISIBLE);
				item.mFailedIcon.setVisibility(View.INVISIBLE);
				item.mFileText.setVisibility(View.INVISIBLE);
				item.mFileButton.setVisibility(View.INVISIBLE);
			} else {
				item.mFileDeleteButton.setVisibility(View.INVISIBLE);
			}

			ViewTreeObserver viewTreeObserver = item.mProgressLayout.getViewTreeObserver();
			viewTreeObserver.addOnPreDrawListener(new OnPreDrawListener() {

				@Override
				public boolean onPreDraw() {
					if (progressLayoutWidth == 0) {
						progressLayoutWidth = item.mProgressLayout.getMeasuredWidth();
						runOnUiThread(new Runnable() {

							@Override
							public void run() {

								if (items.size() > 0) {
									Set<ViewItem> keySet = items.keySet();
									for (ViewItem viewItem : keySet) {
										VCrowdFile file = items.get(viewItem);
										updateProgress(viewItem, file);
									}
									items.clear();
								}
							}
						});
					}
					return true;
				}
			});

			if (item.mProgressLayout.getMeasuredWidth() == 0) {
				items.put(item, file);
			}

			updateProgress(item, file);
		}

		private void updateProgress(ViewItem item, VCrowdFile file) {

			item.mFileProgress.setVisibility(View.VISIBLE);
			FileDownLoadBean bean = GlobalHolder.getInstance().mGlobleFileProgress.get(file.getId());
			if (!isFromStartState
					&& (file.getState() == VFile.State.UPLOAD_PAUSE || file.getState() == VFile.State.DOWNLOAD_PAUSE)) {
				if (bean != null)
					file.setProceedSize(bean.currentLoadSize);
				item.mFileProgress.setText(file.getProceedSizeStr() + "/" + file.getFileSizeStr());
				item.mVelocity.setText("0KB/S");

				double percent = ((double) file.getProceedSize() / (double) file.getSize());
				// int width = item.mProgressLayout.getMeasuredWidth();
				ViewGroup.LayoutParams vl = item.mProgress.getLayoutParams();
				vl.width = (int) (progressLayoutWidth * percent);
				item.mProgress.setLayoutParams(vl);
			} else {
				double percent = 0;
				float speed = 0;
				if (bean != null) {
					file.setProceedSize(bean.currentLoadSize);
					if (file.getState() == VFile.State.UPLOAD_PAUSE || file.getState() == VFile.State.DOWNLOAD_PAUSE) {
						item.mVelocity.setText("0KB/S");
						percent = ((double) file.getProceedSize() / (double) file.getSize());
					} else {
						long sec = (System.currentTimeMillis() - bean.lastLoadTime);
						long size = file.getProceedSize() - bean.lastLoadSize;
						percent = ((double) file.getProceedSize() / (double) file.getSize());
						speed = (size / sec) * 1000;
						item.mVelocity.setText(file.getFileSize(speed) + "/S");
					}
				} else {
					item.mVelocity.setText("0KB/S");
				}

				item.mFileProgress.setText(file.getProceedSizeStr() + "/" + file.getFileSizeStr());

				// int width = item.mProgressLayout.getMeasuredWidth();
				ViewGroup.LayoutParams vl = item.mProgress.getLayoutParams();
				vl.width = (int) (progressLayoutWidth * percent);
				item.mProgress.setLayoutParams(vl);
			}
		}

		private OnClickListener mFailIconListener = new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!GlobalHolder.getInstance().isServerConnected()) {
					Toast.makeText(mContext, R.string.common_networkIsDisconnection_failed_no_network, Toast.LENGTH_SHORT).show();
					return;
				}

				boolean isDownloading = false;
				VCrowdFile file = (VCrowdFile) v.getTag();
				if (file.getState() == VFile.State.REMOVED) {
					mServerExistFiles.remove(file);
					V2Toast.makeText(mContext, R.string.crowd_files_deleted_notification, Toast.LENGTH_SHORT).show();
				} else {
					boolean isNotifiy = true;
					int transType;
					if (file.getState() == VFile.State.UPLOAD_FAILED) {
						transType = V2GlobalConstants.FILE_TRANS_SENDING;
					} else {
						isNotifiy = false;
						transType = V2GlobalConstants.FILE_TRANS_DOWNLOADING;
					}

					boolean flag = GlobalHolder.getInstance().changeGlobleTransFileMember(transType, mContext, true,
							crowd.getGroupID(), "CrowdFilesActivity mFailIconListener");
					if (!flag)
						return;

					if (file.getState() == VFile.State.DOWNLOAD_FAILED) {
						file.setState(VFile.State.DOWNLOADING);
						isDownloading = true;
						file.setProceedSize(0);
						mShowProgressFileMap.put(file.getId(), file);
						file.setStartTime(new Date(GlobalConfig.getGlobalServerTime()));
						service.handleCrowdFile(file, FileOperationEnum.OPERATION_START_DOWNLOAD, null);
					} else if (file.getState() == VFile.State.UPLOAD_FAILED) {
						file.setState(VFile.State.UPLOADING);
						mShowProgressFileMap.put(file.getId(), file);
						file.setProceedSize(0);
						file.setStartTime(new Date(GlobalConfig.getGlobalServerTime()));
						service.handleCrowdFile(file, FileOperationEnum.OPERATION_START_SEND, null);
					}
					VMessage vMessage = mUploadingVMFiles.get(file.getId());
					if (vMessage != null) {
						// 更新数据库
						VMessageFileItem fileItem = vMessage.getFileItems().get(0);
						fileItem.setState(file.getState().intValue());
						fileItem.setUuid(file.getId());
						ChatMessageProvider.updateVMessageItem(mContext, fileItem);
						// 通知聊天界面更新
						if (isNotifiy) {
							Intent i = new Intent();
							i.addCategory(PublicIntent.DEFAULT_CATEGORY);
							i.setAction(PublicIntent.BROADCAST_CROWD_FILE_ACTIVITY_SEND_NOTIFICATION);
							i.putExtra("exeType", VMessageAbstractItem.STATE_FILE_SENDING);
							i.putExtra("fileID", file.getId());
							sendBroadcast(i);
						}
					} else {
						VMessage vm = new VMessage(V2GlobalConstants.GROUP_TYPE_CROWD, crowd.getGroupID(),
								file.getUploader(), GlobalHolder.getInstance().getCurrentUser(),
								new Date(GlobalConfig.getGlobalServerTime()));
						VMessageFileItem fileItem = new VMessageFileItem(vm, file.getName(), file.getState().intValue(),
								file.getId());
						fileItem.setFilePath(file.getPath());
						vm.setDate(new Date(GlobalConfig.getGlobalServerTime()));
						ChatMessageProvider.saveFileVMessage(vm);
						mUploadingVMFiles.put(file.getId(), vm);
					}
				}
				adapter.notifyDataSetChanged();
				if (isScrollButtom && isDownloading) {
					V2Log.d(TAG, "开始下载群文件，当前在列表底部，需要刷新！");
					mListView.setSelection(mListView.getCount() - 1);
				}
			}

		};

		private OnClickListener mDeleteModeButtonListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				Tag tag = (Tag) v.getTag();
				ViewItem item = tag.item;

				if (tag.vf.getUploader() != null
						&& tag.vf.getUploader().getmUserId() != GlobalHolder.getInstance().getCurrentUserId()
						&& GlobalHolder.getInstance().getCurrentUserId() != crowd.getOwnerUser().getmUserId()) {
					Toast.makeText(mContext, R.string.crowd_files_delete_file_no_rights_notification,
							Toast.LENGTH_SHORT).show();
					return;
				}

				if (tag.vf.getState() == VFile.State.UPLOADING) {
					Toast.makeText(mContext, R.string.crowd_files_delete_file_no_rights_for_uploading_notification,
							Toast.LENGTH_SHORT).show();
					return;
				}

				if (item.mFileDeleteButton.getVisibility() == View.VISIBLE) {
					item.mFileDeleteButton.setVisibility(View.INVISIBLE);
					switch (tag.vf.getState()) {
					case DOWNLOAD_FAILED:
					case UPLOAD_FAILED:
					case REMOVED:
						item.mFailedIcon.setVisibility(View.VISIBLE);
						break;
					case UPLOADED:
						item.mFileText.setVisibility(View.VISIBLE);
						break;
					default:
						item.mFileButton.setVisibility(View.VISIBLE);
						break;
					}
					tag.vf.setFlag(HIDE_DELETE_BUTTON_FLAG);
				} else {
					item.mFileDeleteButton.setVisibility(View.VISIBLE);
					item.mFailedIcon.setVisibility(View.INVISIBLE);
					item.mFileButton.setVisibility(View.INVISIBLE);
					item.mFileText.setVisibility(View.INVISIBLE);
					tag.vf.setFlag(SHOW_DELETE_BUTTON_FLAG);
				}
			}

		};

		private OnClickListener mDeleteButtonListener = new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (GlobalHolder.getInstance().checkServerConnected(mContext)) {
					return;
				}

				Tag tag = (Tag) v.getTag();
				VCrowdFile vf = tag.vf;
				waittingDelete = vf;
				List<VCrowdFile> list = new ArrayList<VCrowdFile>();
				list.add(vf);
				service.removeGroupFiles(crowd, list, null);
				WaitDialogBuilder.showNormalWithHintProgress(mContext);
			}

		};

		private OnClickListener mButtonListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isInDeleteMode) {
					Toast.makeText(mContext, R.string.crowd_files_in_deletion_failed, Toast.LENGTH_SHORT).show();
					return;

				}

				if (!GlobalHolder.getInstance().isServerConnected()) {
					Toast.makeText(mContext, R.string.common_networkIsDisconnection_failed_no_network, Toast.LENGTH_SHORT).show();
					return;
				}

				if (AlgorithmUtil.isFastClick()) {
					return;
				}

				VCrowdFile file = (VCrowdFile) v.getTag();
				if (file.getState() == VFile.State.DOWNLOADED || (file.getState() == VFile.State.UPLOADED
						&& file.getUploader().getmUserId() == GlobalHolder.getInstance().getCurrentUserId())) {
					String filePath = file.getPath();
					String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
					int indexOf = fileName.indexOf(".");
					if (indexOf == -1) {
						FileUtils.openFile(file.getPath());
					} else {
						String postfixName = fileName.substring(fileName.indexOf("."));
						if (postfixName != null && postfixName.equals(".gif")) {
							Intent i = new Intent();
							i.addCategory(PublicIntent.DEFAULT_CATEGORY);
							i.setAction(PublicIntent.START_VIDEO_IMAGE_GALLERY);
							i.putExtra("onlyOpenGif", true);
							i.putExtra("filePath", filePath);
							mContext.startActivity(i);
						} else {
							FileUtils.openFile(file.getPath());
						}
					}
					return;
				}

				boolean isUpdatePath = false;
				boolean isUpload = false;
				if (file.getState() == VFile.State.UNKNOWN) {
					boolean isAdd = GlobalHolder.getInstance().changeGlobleTransFileMember(
							V2GlobalConstants.FILE_TRANS_DOWNLOADING, mContext, true, crowd.getGroupID(),
							"CrowdFilesActivity mButtonListener");
					if (!isAdd) {
						return;
					}

					int count = 0;
					for (int i = 0; i < mServerExistFiles.size(); i++) {
						VCrowdFile temp = mServerExistFiles.get(i);
						if (!temp.getId().equals(file.getId())) {
							if (temp.getName().equals(file.getName())) {
								File tempFile = buildDefaultFilePath(temp);
								if (tempFile.exists()) {
									count++;
								}
							}
						}
					}
					// if count > 0 mean have repeat file
					if (count != 0) {
						String name = file.getName();
						String prefix = name.substring(0, name.indexOf("."));
						String postfixName = name.substring(name.indexOf("."));
						String newName = prefix + "(" + count + ")" + postfixName;
						file.setName(newName);

						String prefixPath = file.getPath().substring(0, file.getPath().lastIndexOf("/"));
						String newPath = prefixPath + "/" + newName;
						file.setPath(newPath);
						isUpdatePath = true;
					}

					file.setState(VFile.State.DOWNLOADING);
					file.setStartTime(new Date(GlobalConfig.getGlobalServerTime()));
					((TextView) v).setText(R.string.crowd_files_button_name_pause);
					mShowProgressFileMap.put(file.getId(), file);
					service.handleCrowdFile(file, FileOperationEnum.OPERATION_START_DOWNLOAD, null);
					if (isScrollButtom) {
						V2Log.d(TAG, "开始下载群文件，当前在列表底部，需要刷新！");
						mListView.setSelection(mListView.getCount() - 1);
					}
				} else if (file.getState() == VFile.State.DOWNLOADING) {
					file.setState(VFile.State.DOWNLOAD_PAUSE);
					((TextView) v).setText(R.string.crowd_files_button_name_resume);
					service.handleCrowdFile(file, FileOperationEnum.OPERATION_PAUSE_DOWNLOADING, null);
				} else if (file.getState() == VFile.State.DOWNLOAD_PAUSE) {
					file.setState(VFile.State.DOWNLOADING);
					((TextView) v).setText(R.string.crowd_files_button_name_pause);
					service.handleCrowdFile(file, FileOperationEnum.OPERATION_RESUME_DOWNLOAD, null);
				} else if (file.getState() == VFile.State.UPLOADING) {
					file.setState(VFile.State.UPLOAD_PAUSE);
					((TextView) v).setText(R.string.crowd_files_button_name_pause);
					service.handleCrowdFile(file, FileOperationEnum.OPERATION_PAUSE_SENDING, null);
					isUpload = true;
				} else if (file.getState() == VFile.State.UPLOAD_PAUSE) {
					if (isInDeleteMode) {
						Toast.makeText(mContext, R.string.crowd_files_resume_uploading_failed, Toast.LENGTH_SHORT)
								.show();
						return;
					}

					file.setState(VFile.State.UPLOADING);
					((TextView) v).setText(R.string.crowd_files_button_name_resume);
					v.invalidate();
					service.handleCrowdFile(file, FileOperationEnum.OPERATION_RESUME_SEND, null);
					isUpload = true;
				}
				adapter.notifyDataSetChanged();

				if (file.getState() == State.DOWNLOADING && isScrollButtom) {
					V2Log.d(TAG, "开始下载群文件，当前在列表底部，需要刷新！");
					mListView.setSelection(mListView.getCount() - 1);
				}
				VMessageFileItem fileItem = ChatMessageProvider.queryFileItemByID(file.getId());
				// save state to database
				if (fileItem != null) {
					if (isUpdatePath) {
						fileItem.setFilePath(file.getPath());
						fileItem.setFileName(file.getName());
					}
					fileItem.setState(file.getState().intValue());
					fileItem.setUuid(file.getId());
					ChatMessageProvider.updateVMessageItem(mContext, fileItem);
				} else {
					V2Log.e(TAG, "没有从数据库获取到文件对象！");
					// 处于下载状态，如果数据库中不存在该记录，需要创建该记录
					VMessage vm = new VMessage(crowd.getGroupType(), crowd.getGroupID(), file.getUploader(), null,
							new Date(GlobalConfig.getGlobalServerTime()));
					VMessageFileItem newFileItem = new VMessageFileItem(vm, file.getPath(), file.getState().intValue());
					if (isUpdatePath) {
						newFileItem.setFilePath(file.getPath());
						newFileItem.setFileName(file.getName());
					}
					vm.getFileItems().get(0).setUuid(file.getId());
					vm.setmXmlDatas(vm.toXml());
					ChatMessageProvider.saveFileVMessage(vm);
					vm = null;
				}

				if (isUpload) {
					CommonCallBack.getInstance().executeUpdateCrowdFileState(file.getId(), fileItem.getVm(),
							CrowdFileExeType.UPDATE_FILE);
				}
			}
		};
	}

	public enum CrowdFileActivityType {
		CROWD_FILE_ACTIVITY(0), CROWD_FILE_UPLOING_ACTIVITY(1), UNKNOWN(2);

		private int type;

		private CrowdFileActivityType(int type) {
			this.type = type;
		}

		public static CrowdFileActivityType fromInt(int code) {
			switch (code) {
			case 0:
				return CROWD_FILE_ACTIVITY;
			case 1:
				return CROWD_FILE_UPLOING_ACTIVITY;
			default:
				return UNKNOWN;

			}
		}

		public int intValue() {
			return type;
		}
	}
}
