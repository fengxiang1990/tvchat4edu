package v2av;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;

import v2av.VideoCaptureDevInfo.CapParams;
import v2av.VideoCaptureDevInfo.VideoCaptureDevice;

public class VideoRecorder {
    public static final String TAG = "VideoRecorder";
    public static SurfaceHolder VideoPreviewSurfaceHolder = null;
    //	public static boolean mIsScreenOriatationPortrait;

    public static Display mDisplay = null;
    //	public static int mActivityDisplayOriatation;
    public static boolean isOpenCamera;
    public static int CodecType = 0;

    private int mVideoWidth, mWOld;
    private int mVideoHeight, mHOld;
    private int mBitrate, mBRateOld;
    private int mPreviewFormat;
    private int mFrameRate;
    private int mSelectedFrameRate;
    // private int mCameraRotation;
    // private boolean mbMirror;
    private int framecount;

    //只在软编的时候起作用  控制远端视频方向
    private int cameraRotation;

    private static Camera mCamera = null;
    private VideoCaptureDevInfo mCapDevInfo = null;

    private VideoEncoder mEncoder = null;
    private boolean mIsNeedSendDatas;
    private EncoderPreviewCallBack mVRCallback = null;

    VideoRecorder() {
        mCapDevInfo = VideoCaptureDevInfo.CreateVideoCaptureDevInfo();
        mVRCallback = new EncoderPreviewCallBack(this);
    }

    public static void UnInitCameraPreview() {
        try {
            if (mCamera != null) {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This function will be called fisrt by JNI. And it will guide the
     * subsequent operation of all open camera.
     *
     * @return
     */
    @SuppressWarnings("unused")
    private int StartRecordVideo() {
        Log.i("DEBUG", "===============================");
        if (mCapDevInfo == null) {
            Log.e("DEBUG",
                    "StartRecordVideo()--> VideoCaptureDevInfo Object is null , There's not available camera... -1");
            return -1;
        }

        Log.i("DEBUG", "DefaultDevName " + mCapDevInfo.GetDefaultDevName());
        VideoCaptureDevice device = mCapDevInfo.GetDevice(mCapDevInfo.GetDefaultDevName());
        if (device == null) {
            Log.e("DEBUG",
                    "StartRecordVideo()--> VideoCaptureDevInfo Object is null , Get default camera is null... -1");
            return -1;
        }

		CapParams capParams = mCapDevInfo.GetCapParams();
		mVideoWidth = capParams.width;
		mVideoHeight = capParams.height;
		mBitrate = capParams.bitrate;
		mFrameRate = capParams.fps;
		mPreviewFormat = capParams.format;
		mSelectedFrameRate = selectFramerate(device, mFrameRate);
		switch (InitCamera(device)) {
		case Err_CameraOpenError:
			return -1;
		default:
			break;
		}

		mEncoder = VideoEncoder.getInstance(mVideoWidth, mVideoHeight, mFrameRate, mBitrate);
		StartPreview();
		isOpenCamera = true;
		return 0;
	}

    /**
     * It's called by JNI.
     *
     * @return
     */
    @SuppressWarnings("unused")
    private int StopRecordVideo() {
        isOpenCamera = false;
        UninitCamera();
        return 0;
    }

    /**
     * init carame and start it, call by jni
     *
     * @param device
     * @return
     */
    private AVCode InitCamera(VideoCaptureDevice device) {
        Log.i(TAG, "InitCamera() --> start init Camera !");
        if (mCamera != null) {
            Log.e(TAG, "InitCamera() one--> Camera already opend .... failed!!");
            return AVCode.Err_CameraAlreadyOpen;
        }

        if (Build.VERSION.SDK_INT <= VERSION_CODES.GINGERBREAD) {
            Log.e(TAG, "InitCamera() two--> Mobile System Version Error , Less than 2.3");
            return AVCode.Err_ErrorState;
        } else {
            if (!OpenCamera(device)) {
                Log.e(TAG, "InitCamera() three--> 调用OpenCamera()函数返回false");
            }
        }

        if (mCamera == null) {
            Log.e(TAG, "InitCamera() four--> 打开camera流程都没问题, 但最后回去camera对象是null");
            return AVCode.Err_CameraOpenError;
        }
        return AVCode.Err_None;
    }

    /**
     * Release the carema object
     */
    @SuppressWarnings("deprecation")
    private void UninitCamera() {
        Log.d(TAG, "UninitCamera call");
        if (mCamera != null) {
            try {
                mIsNeedSendDatas = false;
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * It's called by JNI.
     *
     * @return
     */
    @SuppressWarnings("unused")
    private int GetCodecType() {
        return CodecType;
    }

    /**
     * It's called by JNI.
     *
     * @return
     */
    @SuppressWarnings("unused")
    private int GetRecordWidth() {
        if (cameraRotation == 90 || cameraRotation == 270) {
            Log.d(TAG, "GetRecordWidth cameraRotation : " + cameraRotation + " Width : " + mVideoHeight);
            return mVideoHeight;
        } else {
            Log.d(TAG, "GetRecordWidth cameraRotation : " + cameraRotation + " Width : " + mVideoWidth);
            return mVideoWidth;
        }
    }

    /**
     * It's called by JNI.
     *
     * @return
     */
    @SuppressWarnings("unused")
    private int GetRecordHeight() {
        if (cameraRotation == 90 || cameraRotation == 270) {
            Log.d(TAG, "GetRecordHeight cameraRotation : " + cameraRotation + " Height : " + mVideoWidth);
            return mVideoWidth;
        } else {
            Log.d(TAG, "GetRecordHeight cameraRotation : " + cameraRotation + " Height : " + mVideoHeight);
            return mVideoHeight;
        }
    }

    /**
     * It's called by JNI.
     *
     * @return
     */
    @SuppressWarnings("unused")
    private int GetRecordBitrate() {
        Log.d(TAG, "GetRecordBitrate mBitrate :" + mBitrate);
        return mBitrate;
    }

    /**
     * It's called by JNI.
     *
     * @return
     */
    @SuppressWarnings("unused")
    private int GetRecordFPS() {
        Log.d(TAG, "GetRecordFPS mFrameRate :" + mFrameRate);
        return mFrameRate;
    }

    /**
     * It's called by JNI.
     *
     * @return
     */
    @SuppressWarnings("unused")
    private int GetRecordFormat() {
        return mPreviewFormat;
    }

    /**
     * It's called by JNI.useless.
     *
     * @return
     */
    @SuppressWarnings("unused")
    private int GetPreviewSize() {
        Log.i(TAG, "GetPreviewSize");
        return 0;
    }

    /**
     * It's called by JNI.
     *
     * @return
     */
    @SuppressWarnings("unused")
    private int GetPreviewWidth() {
        Log.i(TAG, "GetPreviewWidth " + mVideoWidth);
        return mVideoWidth;
    }

    /**
     * It's called by JNI.
     *
     * @return
     */
    @SuppressWarnings("unused")
    private int GetPreviewHeight() {
        Log.i(TAG, "GetPreviewHeight " + mVideoHeight);
        return mVideoHeight;
    }

    /**
     * It's called by JNI.
     *
     * @return
     */
    @SuppressWarnings("unused")
    private int GetRotation() {
        Log.i(TAG, "GetRotation" + cameraRotation);
        return cameraRotation;
    }

    int mFrameNum = 0;
    long mLastTimeMs;

	public void onGetVideoFrame(byte[] databuf, int len) {
		if (DropFrame()) {
			mCamera.addCallbackBuffer(databuf);
			return;
		}

		if (mIsNeedSendDatas) {
			mEncoder.OnFrame(databuf, 0, len, 0); // MediaCodec encode
		}
		mCamera.addCallbackBuffer(databuf);

		mFrameNum++;
		long currentTimeMs = System.currentTimeMillis();
		if (currentTimeMs - mLastTimeMs > 2000) {
//			Log.e("hzg", "Preview fps is about " + (mFrameNum / 2));
			mLastTimeMs = currentTimeMs;
			mFrameNum = 0;
		}
	}

	byte[] mCamBuf1;

    @SuppressWarnings("unused")
    private int StartSend() {
        Log.i(TAG, "StartSend() --> Start Send Binary Datas");
        if (mCamera == null) {
            return -1;
        }

		// mCamera.setPreviewCallback(mVRCallback);
		int yStride = (int) Math.ceil(mVideoWidth / 16.0) * 16;
		int uStride = (int) Math.ceil((yStride / 2) / 16.0) * 16;
		int bufsize = yStride * mVideoHeight + uStride * mVideoHeight;
		mCamBuf1 = new byte[bufsize];
		mCamera.addCallbackBuffer(mCamBuf1);
		mCamera.setPreviewCallbackWithBuffer(mVRCallback);
		mIsNeedSendDatas = true;
		return 0;
	}

    @SuppressWarnings("unused")
    private int StopSend() {
        Log.i(TAG, "StopSend() --> Stop Send Binary Datas");
        if (mCamera == null) {
            return -1;
        }
        mIsNeedSendDatas = false;
        mCamera.setPreviewCallback(null);
        return 0;
    }

    /**
     * It's used to open the camera preview.
     *
     * @return
     */
    @SuppressWarnings("deprecation")
    private void StartPreview() {
        if (mCamera == null) {
            return;
        }

        try {
            if (VideoPreviewSurfaceHolder != null) {
                mCamera.stopPreview();
                mCamera.setPreviewDisplay(VideoPreviewSurfaceHolder);
                mCamera.startPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
            UninitCamera();
            Log.i(TAG, "StartPreview " + e.getLocalizedMessage());
        }
    }

    private int selectFramerate(VideoCaptureDevice device, int fps) {
        int selectedFps = 0;
        for (Integer framerate : device.framerates) {
            if (framerate >= fps) {
                selectedFps = framerate;
                break;
            }
        }
        return selectedFps;
    }

    private CapParams selectAndCheckCapParams(CapParams capParams) {
        LinkedList<CaptureCapability> capabilites = mCapDevInfo.GetCurrDevice().capabilites;
        boolean isExist = false;
        if (capabilites.size() > 0) {
            for (int i = 0; i < capabilites.size(); i++) {
                CaptureCapability captureCapability = capabilites.get(i);
                if (captureCapability.width == capParams.width && captureCapability.height == capParams.height) {
                    isExist = true;
                }
            }
        }

        if (!isExist && capabilites.size() > 0) {
            TreeMap<Integer, CaptureCapability> temp = new TreeMap<>();
            for (int i = 0; i < capabilites.size(); i++) {
                int sum = capabilites.get(i).width * capabilites.get(i).height;
                int result = Math.abs(sum - (capParams.width * capParams.height));
                temp.put(result, capabilites.get(i));
            }

            for (Map.Entry<Integer, CaptureCapability> en : temp.entrySet()) {
                Log.d(TAG, " checkCapParams map key : " + en.getKey());
                Log.d(TAG, " checkCapParams map value : " + en.getValue().width);
            }

            capParams.width = temp.firstEntry().getValue().width;
            capParams.height = temp.firstEntry().getValue().height;
            mVideoWidth = capParams.width;
            mVideoHeight = capParams.height;
        }
        return capParams;
    }

    private boolean DropFrame() {
        if (mSelectedFrameRate <= mFrameRate) {
            return false;
        }

        framecount++;

        switch (mSelectedFrameRate) {
            case 10:
                if (framecount % 2 != 0) {
                    return true;
                }
                break;
            case 15:
                if (mFrameRate == 5) {
                    if (framecount % 3 != 0) {
                        return true;
                    }
                } else // mVideoRecordInfo.mFrameRate == 10
                {
                    if (framecount % 3 == 0) {
                        return true;
                    }
                }
                break;
            case 30:
                if (mFrameRate == 5) {
                    if (framecount % 6 != 0) {
                        return true;
                    }
                } else if (mFrameRate == 10) {
                    if (framecount % 3 != 0) {
                        return true;
                    }
                } else // mVideoRecordInfo.mFrameRate == 15
                {
                    if (framecount % 2 != 0) {
                        return true;
                    }
                }
                break;
            default:
                break;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private boolean OpenCamera(VideoCaptureDevice device) {
        try {
            mCamera = Camera.open(device.index);
        } catch (RuntimeException e) {
            Log.e(TAG, "OpenCamera() one--> 打开camera失败, 异常.. message : " + e.getLocalizedMessage());
            return false;
        }

        if (mCamera == null) {
            Log.e(TAG, "OpenCamera() two--> 打开camera失败, 没有获取到camera对象..");
            return false;
        }

        Camera.Parameters para = mCamera.getParameters();
        CapParams capParams = mCapDevInfo.GetCapParams();
        List<String> supportedFocusModes = mCamera.getParameters().getSupportedFocusModes();
        boolean hasAutoFocus = supportedFocusModes != null
                && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        if (hasAutoFocus) {
            para.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        try {
            Parameters beforeParam = mCamera.getParameters();
            Size before = beforeParam.getPreviewSize();
            selectAndCheckCapParams(capParams);
            para.setPreviewSize(capParams.width, capParams.height);
            para.setPreviewFormat(mPreviewFormat);
            mCamera.setParameters(para);
        } catch (Exception e) {
            e.printStackTrace();
            para.setPreviewSize(mVideoWidth, mVideoHeight);
            para.setPreviewFormat(mPreviewFormat);
            mCamera.setParameters(para);
        }


        android.hardware.Camera.CameraInfo cameraInfo = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(device.index, cameraInfo);
        Log.e("mDisplay:",String.valueOf(mDisplay));
        Log.e("mCamera:",String.valueOf(mCamera));
        if (mDisplay != null) {
            switch (mDisplay.getRotation()) {
                case Surface.ROTATION_0://手机处于正常状态
                    mCamera.setDisplayOrientation(90);

                    cameraRotation = 270;
                    break;
                case Surface.ROTATION_90://手机旋转90度
                    mCamera.setDisplayOrientation(0);
                    cameraRotation = 0;
                    break;
                case Surface.ROTATION_180:
                    mCamera.setDisplayOrientation(270);
                    cameraRotation = 270;
                    break;
                case Surface.ROTATION_270:
                    mCamera.setDisplayOrientation(180);
                    cameraRotation = 180;
                    break;
            }
            }else{

            mCamera.setDisplayOrientation(0);
            cameraRotation = 0;
        }
//		Log.i(TAG, "camera orientation : " + cameraInfo.orientation + " | mIsScreenOriatationPortrait : "
//				+ mIsScreenOriatationPortrait);
//		Log.e(TAG, "cameraInfo orientation -->" + cameraInfo.orientation);

//		if (mIsScreenOriatationPortrait) {
//			if (cameraInfo.orientation != 270 && cameraInfo.orientation != 0) {
//				if (cameraInfo.orientation == 90 || cameraInfo.orientation == 180) {
//					mCamera.setDisplayOrientation(90);
//					Log.e(TAG, "set camera orientation : " + 270);
//					cameraRotation = 90;
//				}
//			} else {
//				if (mActivityDisplayOriatation == 0) {
//					mCamera.setDisplayOrientation(90);
//					Log.e(TAG, "set camera orientation : " + 90);
//					cameraRotation = 270;
//				} else {
//					mCamera.setDisplayOrientation(270);
//					Log.e(TAG, "set camera orientation : " + 270);
//					cameraRotation = 90;
//				}
//			}
//		} else {
//			if (cameraInfo.orientation == 90 ) {
//				mCamera.setDisplayOrientation(0);
//				Log.e(TAG, "set camera orientation : " + 180);
//				cameraRotation = 0;
//			}else if(cameraInfo.orientation == 180){
//				mCamera.setDisplayOrientation(0);
//				Log.e(TAG, "set camera orientation : " + 180);
//				cameraRotation = 180;
//			}
//			else if (cameraInfo.orientation == 270) {
//				if (mActivityDisplayOriatation == 90) {
//					mCamera.setDisplayOrientation(0);
//					Log.e(TAG, "orientation90 set camera orientation : " + 0);
//					cameraRotation = 0;
//				} else if(mActivityDisplayOriatation == 0){
//					mCamera.setDisplayOrientation(0);
//					cameraRotation = 0;
//				}else{
//					mCamera.setDisplayOrientation(180);
//					Log.e(TAG, "orientation270 set camera orientation : " + 180);
//					cameraRotation = 180;
//				}
//			}
//		}
            return true;
        }
    }
