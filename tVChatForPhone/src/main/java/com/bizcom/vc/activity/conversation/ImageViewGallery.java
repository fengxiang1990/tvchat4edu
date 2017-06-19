package com.bizcom.vc.activity.conversation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.bizcom.db.provider.ChatMessageProvider;
import com.bizcom.vc.widget.LazyViewPager;
import com.bizcom.vc.widget.LazyViewPager.OnPageChangeListener;
import com.bizcom.vo.meesage.VMessage;
import com.bizcom.vo.meesage.VMessageImageItem;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

public class ImageViewGallery extends FragmentActivity {

	private View mReturnButton;

	private LazyViewPager mImageViewPager;

	private List<ListItem> vimList;

	private String currentImageID;

	private int initPos;

	private TextView mTitle;

	private ImageAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_view_gallery);

		mImageViewPager = (LazyViewPager) findViewById(R.id.image_view_view_pager);
		Intent intent = getIntent();

		mTitle = (TextView) findViewById(R.id.image_galley_title);
		mReturnButton = findViewById(R.id.image_galley_detail_return_button);
		mReturnButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}

		});

		currentImageID = intent.getStringExtra("imageID");
		vimList = new ArrayList<ListItem>();
		boolean onlyOpenGif = intent.getBooleanExtra("onlyOpenGif", false);
		if (onlyOpenGif) {
			String filePath = intent.getStringExtra("filePath");
			mTitle.setVisibility(View.INVISIBLE);
			vimList.add(new ListItem(new VMessageImageItem(new VMessage(0, 0,
					null, null), UUID.randomUUID().toString(), filePath, 0)));
		} else {
			int type = intent.getIntExtra("type", 0);
			switch (type) {
			case V2GlobalConstants.GROUP_TYPE_USER:
				loadUserImages(intent.getLongExtra("uid1", 0),
						intent.getLongExtra("uid2", 0));
				break;
			case V2GlobalConstants.GROUP_TYPE_CONFERENCE:
				long conferenceID = intent.getLongExtra("gid", 0);
				loadConferenceImages(conferenceID);
				break;
			case V2GlobalConstants.GROUP_TYPE_CROWD:
				long gid = intent.getLongExtra("gid", 0);
				loadCrowdImages(gid);
				break;
			case V2GlobalConstants.GROUP_TYPE_DEPARTMENT:
				long departmentID = intent.getLongExtra("gid", 0);
				loadDepartmentImages(departmentID);
				break;
			case V2GlobalConstants.GROUP_TYPE_DISCUSSION:
				long discussionID = intent.getLongExtra("gid", 0);
				loadDiscussionImages(discussionID);
				break;
			default:
				throw new RuntimeException(
						"The given group type is error , please check it :"
								+ type);
			}
		}
		
		adapter = new ImageAdapter(this.getSupportFragmentManager());
		mImageViewPager.setAdapter(adapter);

		mImageViewPager.setOffscreenPageLimit(1);
		mImageViewPager.setOnPageChangeListener(pageChangeListener);
		mTitle.setText((initPos + 1) + "/" + vimList.size());
		mImageViewPager.setCurrentItem(initPos);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		vimList.clear();
	}

	/**
	 * Load group image message list
	 * 
	 * @param groupId
	 */
	private void loadCrowdImages(long groupId) {
		List<VMessage> list = ChatMessageProvider.loadGroupImageMessage(this,
				Integer.valueOf(V2GlobalConstants.GROUP_TYPE_CROWD), groupId);
		populateImageMessage(list);
	}

	private void loadUserImages(long user1Id, long user2Id) {
		List<VMessage> list = ChatMessageProvider.loadImageMessage(
				user1Id, user2Id);
		populateImageMessage(list);
	}

	private void loadConferenceImages(long groupId) {
		List<VMessage> list = ChatMessageProvider.loadGroupImageMessage(this,
				Integer.valueOf(V2GlobalConstants.GROUP_TYPE_CONFERENCE),
				groupId);
		populateImageMessage(list);
	}

	private void loadDepartmentImages(long groupId) {
		List<VMessage> list = ChatMessageProvider.loadGroupImageMessage(this,
				Integer.valueOf(V2GlobalConstants.GROUP_TYPE_DEPARTMENT),
				groupId);
		populateImageMessage(list);
	}

	private void loadDiscussionImages(long groupId) {
		List<VMessage> list = ChatMessageProvider.loadGroupImageMessage(this,
				Integer.valueOf(V2GlobalConstants.GROUP_TYPE_DISCUSSION),
				groupId);
		populateImageMessage(list);
	}

	private void populateImageMessage(List<VMessage> list) {
		boolean flag = false;
		for (int i = list.size() - 1; i >= 0; i--) {
			VMessage vm = list.get(i);
			List<VMessageImageItem> items = vm.getImageItems();
			for (VMessageImageItem item : items) {
				vimList.add(new ListItem(item));
				if (item.getUuid().equals(currentImageID)) {
					flag = true;
				}
				if (!flag) {
					initPos++;
				}
			}
		}
	}

	private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {

		};

		@Override
		public void onPageSelected(int pos) {
			mTitle.setText((pos + 1) + "/" + vimList.size());
		}

	};

	class ImageAdapter extends FragmentPagerAdapter {

		public ImageAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int pos) {
			return vimList.get(pos).frg;
		}

		@Override
		public int getCount() {
			return vimList == null ? 0 : vimList.size();
		}

	}

	class ListItem {
		VMessageImageItem imageItem;
		PlaceSlideFragment frg;

		public ListItem(VMessageImageItem imageItem) {
			super();
			this.imageItem = imageItem;
			frg = new PlaceSlideFragment();
			Bundle bundle = new Bundle();
			bundle.putString("filePath", imageItem.getFilePath());
			frg.setMessage(imageItem);
			frg.setArguments(bundle);
		}

	}

}
