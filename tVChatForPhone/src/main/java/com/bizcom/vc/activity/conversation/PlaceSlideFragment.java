package com.bizcom.vc.activity.conversation;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bizcom.util.V2Log;
import com.bizcom.vc.widget.TouchImageView;
import com.bizcom.vc.widget.cus.gif.GifPlayer;
import com.bizcom.vc.widget.cus.gif.GifPlayer.GifPlayListener;
import com.bizcom.vc.widget.cus.subsamplingscaleImage.SubsamplingScaleImageView;
import com.bizcom.vo.meesage.VMessage;
import com.bizcom.vo.meesage.VMessageImageItem;
import com.bizcom.vo.meesage.VMessageImageItem.Size;
import com.config.GlobalConfig;
import com.shdx.tvchat.phone.R;

import java.util.UUID;

public class PlaceSlideFragment extends Fragment implements GifPlayListener {

	private Bitmap mHoldPlaceBitmap;

	private VMessageImageItem vim;
	private String filePath;

	private AsyncTask<Void, Void, Bitmap> loadImageTask;

	private View root;
	private RelativeLayout rootLayout;
	private View cusView;

	private boolean isGifView;
	private GifPlayer mGifPlayer;
	
	private boolean isVisibleToUser;

	public PlaceSlideFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle arguments = getArguments();
		filePath = arguments.getString("filePath");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.image_view, container, false);
		rootLayout = (RelativeLayout) root.findViewById(R.id.image_view_root);
		rootLayout.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

		if (vim == null)
			vim = new VMessageImageItem(new VMessage(0, 0, null, null), UUID
					.randomUUID().toString(), filePath, 0);
		if (".gif".equals(vim.getExtension())) {
			cusView = new TouchImageView(getActivity());
			mGifPlayer = new GifPlayer();
			mGifPlayer.setGifPlayListener(this);
			isGifView = true;
		} else {
			Size fullBitmapSize = vim.getFullBitmapSize();
			if (fullBitmapSize.width > GlobalConfig.BITMAP_MAX_SIZE
					|| fullBitmapSize.height > GlobalConfig.BITMAP_MAX_SIZE) {
				cusView = new SubsamplingScaleImageView(this.getActivity());
				SubsamplingScaleImageView subImage = (SubsamplingScaleImageView) cusView;
				subImage.setFitScreen(true);
				subImage.setImageFile(filePath);
			} else {
				cusView = new TouchImageView(this.getActivity());
				final TouchImageView iv = (TouchImageView) cusView;
				mHoldPlaceBitmap = Bitmap.createBitmap(50, 50,
						Bitmap.Config.RGB_565);
				iv.setImageBitmap(mHoldPlaceBitmap);
				loadImageTask = new AsyncTask<Void, Void, Bitmap>() {

					@Override
					protected Bitmap doInBackground(Void... params) {
						synchronized (PlaceSlideFragment.class) {
							if (vim != null) {
								return vim.getFullQuantityBitmap();
							}
							return null;
						}
					}

					@Override
					protected void onPostExecute(Bitmap result) {
						if (result == null) {
							V2Log.e("ConversationView",
									"getFullQuantityBitmap is null");
							return;
						}
						iv.setImageBitmap(result);
					}

					@Override
					protected void onCancelled() {
						super.onCancelled();
						synchronized (vim) {
							if (vim != null) {
								vim.recycleFull();
							}
						}
						iv.setImageBitmap(null);
					}

					@Override
					protected void onCancelled(Bitmap result) {
						super.onCancelled(result);
						synchronized (vim) {
							if (vim != null) {
								vim.recycle();
							}
						}
						iv.setImageBitmap(null);
					}
				}.execute();
			}
		}

		RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		rl.addRule(RelativeLayout.CENTER_IN_PARENT);
		rootLayout.addView(cusView, rl);
		return root;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		this.isVisibleToUser = isVisibleToUser;
		if (!isVisibleToUser && rootLayout != null
				&& rootLayout.getChildCount() > 0) {
			View view = rootLayout.getChildAt(rootLayout.getChildCount() - 1);
			if (view instanceof TouchImageView) {
				((TouchImageView) view).resetZoom();
			} else if(view instanceof SubsamplingScaleImageView){
				((SubsamplingScaleImageView) view).resetScaleAndCenter();
			} 
		}
		
		if (isGifView) {
			if(isVisibleToUser){
				if(!mGifPlayer.isPlaying()){
					mGifPlayer.play(filePath, true);
				}
			} else {
				if(mGifPlayer.isPlaying())
					mGifPlayer.stop();
			}
		}
	}
	
	@Override
	public void onStart() {
		if (isGifView && isVisibleToUser && !mGifPlayer.isPlaying()) {
			mGifPlayer.play(filePath, true);
		}
		super.onStart();
	}
	
	@Override
	public void onStop() {
		if (mGifPlayer != null && mGifPlayer.isPlaying()){
			mGifPlayer.stop();
		}
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		if (isGifView && mGifPlayer.isPlaying()){
			mGifPlayer.stop();
			mGifPlayer = null;
		}
		
		if (loadImageTask != null)
			loadImageTask.cancel(true);

		if (vim != null) {
			vim.recycleFull();
		}
			
		if (mHoldPlaceBitmap != null) {
			mHoldPlaceBitmap.recycle();
		}

		if (rootLayout != null) {
			rootLayout.removeAllViews();
		}
		super.onDestroyView();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void setMessage(VMessageImageItem vim) {
		this.vim = vim;
	}

	@Override
	public void onNextBitmapReady(Bitmap bmp) {
		if(GlobalConfig.GLOBAL_DENSITY_LEVEL == GlobalConfig.DENSITY_XXXHIGH
				&& (bmp.getWidth() < 200 && bmp.getHeight() < 200)){
			bmp = Bitmap.createScaledBitmap(bmp, bmp.getWidth() * 3, bmp.getHeight() * 3, false);
		}
		((ImageView) cusView).setImageBitmap(bmp);
	}

	@Override
	public void onPlayFinish() {

	}

	@Override
	public void onLoop() {

	}
}
