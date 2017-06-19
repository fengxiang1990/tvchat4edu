package com.bizcom.vc.activity.conversation;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bizcom.bo.GroupUserObject;
import com.bizcom.request.V2ConferenceRequest;
import com.bizcom.service.JNIService;
import com.bizcom.util.BitmapUtil;
import com.bizcom.util.DensityUtils;
import com.bizcom.util.V2Log;
import com.bizcom.util.nanotasks.BackgroundWork;
import com.bizcom.util.nanotasks.Completion;
import com.bizcom.util.nanotasks.Tasks;
import com.bizcom.vc.activity.BaseActivity;
import com.bizcom.vc.adapter.SimpleBaseAdapter;
import com.bizcom.vc.widget.cus.V2ImageView;
import com.bizcom.vc.widget.cus.V2ImageView.BitmapResultCallBack;
import com.bizcom.vo.FileInfoBean;
import com.bizcom.vo.User;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.shdx.tvchat.phone.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConversationSelectImageActivity extends BaseActivity {

	private static final String TAG = ConversationSelectImageActivity.class.getSimpleName();
	protected static final int CANCEL_SELECT_PICTURE = 0;
	private static final int SCROLL_STATE_TOUCH_SCROLL = 1;
	private static final int UPDATE_BITMAP = 3;
	protected static final int SCAN_SDCARD = 4;

	private static final int DEFAULT_PATH = 0x0001;
	private static final int NOTIFY_HOST_PERMISSION_REQUESTED = 0x002;
	private int DEFAULT_PATH_INDEX;
	private int SDCARD_PATH_INDEX;
	private String SDCARD_ROOT_NAME;
	private String SDCARD_ROOT;

	private V2ConferenceRequest v2ConferenceRequest;

	private RelativeLayout buttomTitle;
	private LinearLayout buttomDivider;
	private LinearLayout loading;
	private TextView backButton;
	private TextView finishButton;
	private TextView title;
	private GridView gridViews;
	private ListView listViews;
	private ImageListAdapter imageAdapter;
	private ImageClassifyAdapter classifyAdapter;
	private ArrayList<String> pictresClassify;
	private ArrayList<FileInfoBean> pictures;
	private Context mContext;
	private long remoteID;
	private boolean isClassify = true;
	private boolean isBack;
	private String isFromConference;

	private HashMap<String, ArrayList<FileInfoBean>> listMap;
	private String[][] selectArgs = { { String.valueOf(MediaStore.Images.Media.INTERNAL_CONTENT_URI), "image/png" },
			{ String.valueOf(MediaStore.Images.Media.INTERNAL_CONTENT_URI), "image/jpeg" },
			{ String.valueOf(MediaStore.Images.Media.EXTERNAL_CONTENT_URI), "image/png" },
			{ String.valueOf(MediaStore.Images.Media.EXTERNAL_CONTENT_URI), "image/jpeg" } };
	private final int LRU_MAX_MEMORY = (int) ((Runtime.getRuntime().maxMemory()) / 8);
	private LruCache<String, Bitmap> lruCache = new LruCache<String, Bitmap>(LRU_MAX_MEMORY) {

		@Override
		protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
			if (key != null) {
				if (lruCache != null) {
					Bitmap bm = lruCache.remove(key);
					if (bm != null && !bm.isRecycled()) {
						bm.recycle();
						bm = null;
					}
				}
			}
		}
	};
	protected int isLoading;
	public int gridImageHeight = 0;
	public int gridImageWidth = 0;
	private ImageLoader imageLoader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_selectfile);
		super.setNeedAvatar(false);
		super.setNeedBroadcast(true);
		super.setNeedHandler(true);
		super.onCreate(savedInstanceState);
		if (GlobalConfig.PROGRAM_IS_PAD) {
			if (GlobalConfig.SCREEN_HEIGHT > GlobalConfig.SCREEN_WIDTH) {
				gridImageHeight = GlobalConfig.SCREEN_WIDTH / 3;//
				gridImageWidth = (GlobalConfig.SCREEN_HEIGHT - 20) / 3;
			} else {
				gridImageHeight = GlobalConfig.SCREEN_HEIGHT / 3;//
				gridImageWidth = (GlobalConfig.SCREEN_WIDTH - 20) / 3;
			}
		} else {
			if(GlobalHolder.getInstance().isInMeeting()){
				gridImageHeight = GlobalConfig.SCREEN_WIDTH / 3;//
				gridImageWidth = (GlobalConfig.SCREEN_HEIGHT - 20) / 3;
			} else {
				gridImageHeight = GlobalConfig.SCREEN_HEIGHT / 4;//
				gridImageWidth = (GlobalConfig.SCREEN_WIDTH - 20) / 3; // 一屏显示3列
			}
		}
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().showImageOnFail(R.drawable.ic_launcher)
				.showImageForEmptyUri(R.drawable.ic_launcher).considerExifParams(true)
				.bitmapConfig(Bitmap.Config.RGB_565).showImageOnLoading(R.drawable.ic_launcher).cacheInMemory(true)
				.cacheOnDisk(true).build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
				.defaultDisplayImageOptions(defaultOptions).build();
		imageLoader = ImageLoader.getInstance();
		imageLoader.init(config);
		init();
		setListener();
		initPath();
		mContext = this;
		isFromConference = getIntent().getStringExtra("fromPlace");
	}

	@Override
	public void onBackPressed() {
		if (isBack) {
			listViews.setVisibility(View.VISIBLE);
			gridViews.setVisibility(View.GONE);
			classifyAdapter = new ImageClassifyAdapter();
			listViews.setAdapter(classifyAdapter);
			isBack = false;
			isClassify = true;
		} else {
			setResult(Activity.RESULT_CANCELED);
			super.onBackPressed();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		lruCache.evictAll();
		imageLoader.clearMemoryCache();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		pictresClassify.clear();
		pictures.clear();
		listMap.clear();
		lruCache.evictAll();
		v2ConferenceRequest.unRegisterLectureRequestListener(mHandler, NOTIFY_HOST_PERMISSION_REQUESTED, null);
		v2ConferenceRequest.unRegisterPermissionUpdateListener(mHandler, NOTIFY_HOST_PERMISSION_REQUESTED, null);
	}

	@Override
	public void addBroadcast(IntentFilter filter) {
		filter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED);
	}

	@Override
	public void receiveBroadcast(Intent intent) {
		String action = intent.getAction();
		if (JNIService.JNI_BROADCAST_GROUP_USER_REMOVED.equals(action)) {
			GroupUserObject obj = intent.getParcelableExtra("obj");
			if (obj.getmType() == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
				if (obj.getmUserId() == remoteID) {
					finish();
				}
			}
		}
	}

	@Override
	public void receiveMessage(Message msg) {
		switch (msg.what) {
		case UPDATE_BITMAP:
			final ViewHolder holder = (ViewHolder) ((Object[]) msg.obj)[0];
			Bitmap bt = (Bitmap) ((Object[]) msg.obj)[1];
			final FileInfoBean fb = (FileInfoBean) ((Object[]) msg.obj)[2];
			if (bt != null && !bt.isRecycled()) {
				fb.isLoadOver = true;
				holder.fileIcon.setImageBitmap(bt, new BitmapResultCallBack() {

					@Override
					public void onFailed() {
						fb.isLoadOver = false;
						V2Log.d(TAG, "加载图片失败，得到一个回收的图片，重新加载");
						// 开始加载图片
						startLoadBitmap(holder, fb);
					}

					@Override
					public void onSuccess() {

					}
				});
			} else {
				fb.isLoadOver = false;
				pictures.remove(fb);
				// holder.fileIcon.setImageResource(R.drawable.ic_launcher);
				// startLoadBitmap(holder, fb);
			}
			break;
		case SCAN_SDCARD:
			loading.setVisibility(View.GONE);
			classifyAdapter = new ImageClassifyAdapter();
			listViews.setAdapter(classifyAdapter);
			break;
		case NOTIFY_HOST_PERMISSION_REQUESTED:
			setResult(100);
			finish();
			break;
		}
	}

	@Override
	public void initViewAndListener() {
		title = (TextView) findViewById(R.id.ws_common_activity_title_content);
		title.setText(R.string.conversation_select_image_file_title);
		backButton = (TextView) findViewById(R.id.ws_common_activity_title_left_button);
		setComBackImageTV(backButton);
		finishButton = (TextView) findViewById(R.id.ws_common_activity_title_right_button);
		finishButton.setText(R.string.conversation_select_file_cannel);

		buttomTitle = (RelativeLayout) findViewById(R.id.activity_selectfile_buttom);
		buttomDivider = (LinearLayout) findViewById(R.id.ws_selectFile_buttom_divider);
		gridViews = (GridView) findViewById(R.id.selectfile_gridview);
		listViews = (ListView) findViewById(R.id.selectfile_lsitview);
		loading = (LinearLayout) findViewById(R.id.selectfile_loading);
	}

	@Override
	public void receiveNewAvatar(User targetUser, Bitmap bnewAvatarm) {

	}

	private void initPath() {
		boolean sdExist = android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState());
		if (sdExist) {
			String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
			String[] split = sdPath.split("/");
			SDCARD_PATH_INDEX = split.length;
			SDCARD_ROOT = split[1];
			SDCARD_ROOT_NAME = split[2];
			// V2Log.d(TAG, "sdcard path : " + sdPath);
			// V2Log.d(TAG, "sdcard SDCARD_ROOT_NAME : " + SDCARD_ROOT_NAME);
		}

		// String defPath = getApplicationContext().getFilesDir().getParent();
		// String[] split = defPath.split("/");
		// V2Log.d(TAG, "internal path : " + defPath);
		// V2Log.d(TAG, "internal root name : " + split[split.length - 1]);
		DEFAULT_PATH_INDEX = 2;
	}

	private void init() {
		title.setText(R.string.conversation_select_file_entry_picture);
		listViews.setVisibility(View.VISIBLE);
		gridViews.setVisibility(View.GONE);
		finishButton.setVisibility(View.INVISIBLE);
		buttomTitle.setVisibility(View.GONE);
		buttomDivider.setVisibility(View.GONE);
		pictresClassify = new ArrayList<String>();
		pictures = new ArrayList<FileInfoBean>();
		listMap = new HashMap<String, ArrayList<FileInfoBean>>();

		v2ConferenceRequest = new V2ConferenceRequest();
		v2ConferenceRequest.registerLectureRequestListener(mHandler, NOTIFY_HOST_PERMISSION_REQUESTED, null);
		v2ConferenceRequest.registerPermissionUpdateListener(mHandler, NOTIFY_HOST_PERMISSION_REQUESTED, null);

		remoteID = getIntent().getLongExtra("uid", -1);
		new Thread(new Runnable() {

			@Override
			public void run() {
				loading.setVisibility(View.VISIBLE);
				for (int i = 0; i < selectArgs.length; i++) {

					initPictures(Uri.parse(selectArgs[i][0]), selectArgs[i][1]);
				}

				mHandler.sendEmptyMessage(SCAN_SDCARD);
			}
		}).start();
	}

	private void initPictures(Uri uri, String select) {
		ContentResolver resolver = getContentResolver();
		String[] projection = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME,
				MediaStore.Images.Media.DATA, MediaStore.Images.Media.SIZE };
		String selection = MediaStore.Images.Media.MIME_TYPE + "=?";
		String[] selectionArgs = { select };
		String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " desc";
		Cursor cursor = null;
		try {
			cursor = resolver.query(uri, projection, selection, selectionArgs, sortOrder);
			if (cursor != null) {
				FileInfoBean bean = null;
				ArrayList<FileInfoBean> currentList = null;
				while (cursor.moveToNext()) {
					bean = new FileInfoBean();
					String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
					File temp = new File(filePath);
					if (!temp.exists())
						continue;
					bean.filePath = filePath;
					bean.fileSize = Long.valueOf(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.SIZE)));
					bean.fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
					bean.fileType = 1;
					if (TextUtils.isEmpty(filePath))
						continue;

					String parentName;
					String[] s = filePath.split("/");
					// V2Log.i(TAG, "file path : " + filePath);
					// V2Log.i(TAG, "arr size : " + s.length);

					int type = 0;
					if (!SDCARD_ROOT.equals(s[1]))
						type = DEFAULT_PATH;

					if (type == DEFAULT_PATH) {
						if (DEFAULT_PATH_INDEX < s.length)
							parentName = s[DEFAULT_PATH_INDEX];
						else
							continue;
						// V2Log.i(TAG, "index name : " +
						// s[DEFAULT_PATH_INDEX]);
					} else {
						if (SDCARD_PATH_INDEX <= s.length && SDCARD_ROOT_NAME.equals(s[2])) {
							if (SDCARD_PATH_INDEX == s.length - 1)
								parentName = "root";
							else
								parentName = s[SDCARD_PATH_INDEX];
						} else
							continue;
						V2Log.i(TAG, "index name : " + s[SDCARD_PATH_INDEX]);
					}
					// V2Log.i(TAG, "parentName name : " + parentName);
					// V2Log.i("------------------------");
					if (listMap.containsKey(parentName)) {
						currentList = listMap.get(parentName);
						if (currentList == null)
							currentList = new ArrayList<FileInfoBean>();
					} else {
						pictresClassify.add(parentName);
						currentList = new ArrayList<FileInfoBean>();
					}
					currentList.add(bean);
					listMap.put(parentName, currentList);
					bean = null;
				}
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
	}

	private void setListener() {

		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		boolean pauseOnScroll = false; // or true
		boolean pauseOnFling = true; // or false
		PauseOnScrollListener listener = new PauseOnScrollListener(imageLoader, pauseOnScroll, pauseOnFling);
		gridViews.setOnScrollListener(listener);

		gridViews.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(isFromConference == null || !isFromConference.equals("Conference")){
					int size = (int) (pictures.get(position).fileSize / (double) 1048576);
//					if (size > 3) {
//						Toast.makeText(getApplicationContext(), R.string.conversation_select_file_entry_pictures_limited,
//								Toast.LENGTH_SHORT).show();
//					} else {
						Intent intent = new Intent();
						intent.putExtra("checkedImage", pictures.get(position).filePath);
						setResult(100, intent);
						ConversationSelectImageActivity.super.onBackPressed();
//					}
				} else {
					Intent intent = new Intent();
					intent.putExtra("checkedImage", pictures.get(position).filePath);
					setResult(100, intent);
					ConversationSelectImageActivity.super.onBackPressed();
				}
			}
		});

		listViews.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				isClassify = false;
				String classifyName = pictresClassify.get(position);
				pictures = listMap.get(classifyName);
				listViews.setVisibility(View.GONE);
				gridViews.setVisibility(View.VISIBLE);
				imageAdapter = new ImageListAdapter(mContext, pictures);
				gridViews.setAdapter(imageAdapter);
				isBack = true;
			}
		});
	}

	class ImageClassifyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return pictresClassify.size();
		}

		@Override
		public Object getItem(int position) {
			return pictresClassify.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(ConversationSelectImageActivity.this,
						R.layout.activity_selectfile_image_adapter, null);
				holder.fileIcon = (V2ImageView) convertView.findViewById(R.id.ws_selectFile_iamge_icon);
				holder.fileName = (TextView) convertView.findViewById(R.id.ws_selectFile_iamge_name);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			LayoutParams para = holder.fileIcon.getLayoutParams();
			para.width = DensityUtils.dip2px(mContext, 100);
			para.height = DensityUtils.dip2px(mContext, 100);
			holder.fileIcon.setLayoutParams(para);

			if (position >= pictresClassify.size()) {
				classifyAdapter.notifyDataSetChanged();
				return convertView;
			}

			String classifyName = pictresClassify.get(position);
			ArrayList<FileInfoBean> arrayList = listMap.get(classifyName);
			if (arrayList == null || arrayList.size() == 0) {
				listMap.remove(classifyName);
				pictresClassify.remove(position);
				classifyAdapter.notifyDataSetChanged();
				return convertView;
			}
			final FileInfoBean fb = arrayList.get(0);
			holder.fileName.setText(classifyName + "( " + arrayList.size() + " )");
			fb.fileName = fb.filePath.substring(fb.filePath.lastIndexOf("/") + 1);
			if (TextUtils.isEmpty(fb.filePath) || TextUtils.isEmpty(fb.fileName)) {
				holder.fileIcon.setImageResource(R.drawable.ic_launcher);
			} else {
				File temp = new File(fb.filePath);
				if (temp.exists()) {
					Bitmap bit = lruCache.get(fb.fileName);
					if (bit == null || bit.isRecycled()) {
						if (isLoading != SCROLL_STATE_TOUCH_SCROLL && isLoading != 1)
							// 开始加载图片
							startLoadBitmap(holder, fb);
						else
							// 加载中显示的图片
							holder.fileIcon.setImageResource(R.drawable.ic_launcher);
					} else {
						fb.isLoadOver = true;
						holder.fileIcon.setImageBitmap(bit, new BitmapResultCallBack() {

							@Override
							public void onFailed() {
								fb.isLoadOver = false;
								V2Log.d(TAG, "加载图片失败，得到一个回收的图片，重新加载");
								// 开始加载图片
								startLoadBitmap(holder, fb);
							}

							@Override
							public void onSuccess() {

							}
						});
					}
				} else {
					holder.fileIcon.setImageResource(R.drawable.ic_launcher);
				}
			}
			return convertView;
		}
	}

	class ImageListAdapter extends SimpleBaseAdapter<FileInfoBean> {

		public ImageListAdapter(Context mContext, List<FileInfoBean> list) {
			super(mContext, list);
		}

        @Override
        protected int compareToItem(ListItem currentItem, ListItem another) {
            return 0;
        }

        @Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			if (pictures.size() <= 0) {
				V2Log.e(TAG, "error mFileLists size zero");
				Toast.makeText(getApplicationContext(), R.string.conversation_select_file_entry_picture_anomaly,
						Toast.LENGTH_SHORT).show();
				finish();
			} else if (position >= pictures.size()) {
				return convertView;
			}

			final ViewHolder holder;
			if (convertView == null) {

				holder = new ViewHolder();
				convertView = View.inflate(ConversationSelectImageActivity.this, R.layout.activity_imagefile_adapter,
						null);
				holder.fileIcon = (V2ImageView) convertView.findViewById(R.id.selectfile_adapter_image);
				holder.fileCheck = (CheckBox) convertView.findViewById(R.id.selectfile_adapter_check);
				LayoutParams para = holder.fileIcon.getLayoutParams();
				if (para == null) {
					para = new ViewGroup.LayoutParams(gridImageWidth, gridImageHeight);
				}
				para.height = gridImageHeight;//
				para.width = gridImageWidth;// 一屏显示3列
				holder.fileIcon.setLayoutParams(para);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.fileCheck.setVisibility(View.INVISIBLE);

			FileInfoBean fb = checkIteratorFilePath(position);
			if (fb == null) {
				pictures.remove(position);
				return convertView;
			}
			imageLoader.displayImage("file://" + fb.filePath, holder.fileIcon);
			return convertView;
		}

	}

	class ViewHolder {

		public V2ImageView fileIcon;
		public CheckBox fileCheck;
		public TextView fileName;
	}

	private FileInfoBean checkIteratorFilePath(int position) {
		if (position >= pictures.size()) {
			return null;
		}

		FileInfoBean fb = pictures.get(position);
		if (TextUtils.isEmpty(fb.fileName)) {
			if (TextUtils.isEmpty(fb.filePath)) {
				V2Log.e(TAG, "error that this file name is vaild!");
				pictures.remove(position);
				checkIteratorFilePath(position + 1);
			} else {
				fb.fileName = fb.filePath.substring(fb.filePath.lastIndexOf("/") + 1);
			}
		}

		if (TextUtils.isEmpty(fb.filePath)) {
			V2Log.e(TAG, "error that this file name is vaild!");
			pictures.remove(position);
			checkIteratorFilePath(position + 1);
		}

		File target = new File(fb.filePath);
		if (!target.exists()) {
			pictures.remove(position);
			checkIteratorFilePath(position + 1);
		}
		return fb;
	}

	public void startLoadBitmap(final ViewHolder holder, final FileInfoBean fb) {

		Tasks.executeInBackground(mContext, new BackgroundWork<Bitmap>() {
			@Override
			public Bitmap doInBackground() throws Exception {
				return startThreadLoadBitmap(holder, fb);
			}
		}, new Completion<Bitmap>() {
			@Override
			public void onSuccess(Context context, Bitmap result) {
				Message.obtain(mHandler, UPDATE_BITMAP, new Object[] { holder, result, fb }).sendToTarget();
			}

			@Override
			public void onError(Context context, Exception e) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						Bitmap startThreadLoadBitmap = startThreadLoadBitmap(holder, fb);
						Message.obtain(mHandler, UPDATE_BITMAP, new Object[] { holder, startThreadLoadBitmap, fb })
								.sendToTarget();
					}
				}).start();
			}
		});
	}

	private Bitmap startThreadLoadBitmap(ViewHolder holder, FileInfoBean fb) {
		try {
			Bitmap bitmap = null;
			if (isClassify)
				bitmap = BitmapUtil.getImageThumbnail(fb.filePath, 100, 100);
			else
				bitmap = BitmapUtil.getImageThumbnail(fb.filePath, gridImageWidth, gridImageHeight);

			if (fb.fileName == null && bitmap != null) {
				V2Log.d(TAG, "加载图片错误，图片的文件名是null！");
				if (!bitmap.isRecycled()) {
					bitmap.recycle();
					bitmap = null;
				}
				return null;
			}

			if (bitmap == null) {
				V2Log.e(TAG, "get null when loading " + fb.fileName + " picture.");
				return null;
			} else {
				lruCache.put(fb.fileName, bitmap);
				return bitmap;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
