package v2av;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class VideoPlayer {
    private static final String TAG = "DEBUG";
    public boolean mIsCropBitmap = false;

    private static boolean isUseMediaCodec = true;
    public static int DisplayRotation = 0;

    private int mixVideoType = -1;
    private SurfaceHolder mSurfaceH;

    private Bitmap mBitmap;
    private float mBaseScale;
    // private int mDisplayMode = 0; //0,1,2
    private int mClearCanvas = 2;

    private int mRotation = 0;
    private int mBmpRotation = 0;

    private Matrix mMatrix = null;
    private VideoDisplayMatrix mDisMatrix;
    private float mScaleRate;
    private ByteBuffer _playBuffer;
    private MediaCodec mediaCodec;

    private Lock mLock;
    private boolean mIsSuspended;
    public boolean mIsVideoDrawing;
    private long mLastTimeStamp;
    private SurfaceCavansProcessListener mSurfaceCavansProcessListener;
    private int mCount = 0;

    // 硬解码
    private RemoteVideoPlayCallBack mRemoteVideoPlayCallBack;
    public boolean isPausePlay = false;
    private long mLastTimeMillis;
    private File temps;
    FileOutputStream ips = null;
    private static VideoNative videoNative;

    public static void EnableMediaCodec(boolean bEnable) {
        isUseMediaCodec = bEnable;
        if (videoNative == null)
            videoNative = new VideoNative();
        videoNative.enablehardwarecodec(VideoEncoder.GetMediaCodecEnabled(), isUseMediaCodec);
    }

    public static boolean GetMediaCodecEnabled() {
        return isUseMediaCodec;
    }

    public VideoPlayer() {
        this(null);
    }

    public VideoPlayer(
            SurfaceCavansProcessListener mSurfaceCavansProcessListener) {
        mLock = new ReentrantLock();
        this.mSurfaceCavansProcessListener = mSurfaceCavansProcessListener;
        _playBuffer = ByteBuffer.allocateDirect(1920 * 1080 * 3 / 2);
    }

    public void setSurfaceHolder(SurfaceHolder holder) {
        mLock.lock();
        try {
            mSurfaceH = holder;
        } finally {
            mLock.unlock();
        }
    }

    public void setSurfaceViewSize(int w, int h) {
        mLock.lock();
        try {
            mClearCanvas = 0;
            if (mDisMatrix != null) {
                mDisMatrix.setViewSize(w, h, mScaleRate);
                UpdateMatrix();
            }
        } finally {
            mLock.unlock();
        }
    }

    public void setSurfaceViewSize(int w, int h, float scale) {
        mLock.lock();
        try {
            mClearCanvas = 0;
            if (mDisMatrix != null) {
                mScaleRate = scale;
                mDisMatrix.setViewSize(w, h, scale);
                UpdateMatrix();
            }
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Whether to stop the video drawing
     *
     * @param spended true is stop , false is not
     */
    public void setSuspended(boolean spended) {
        mLock.lock();
        try {
            mIsSuspended = spended;
            Log.d("VideoPlayer", "mIsSuspended : " + mIsSuspended);
            if (mIsSuspended) {
                mIsVideoDrawing = false;
            }
        } finally {
            mLock.unlock();
        }
    }

    public void setRotation(int rotation) {
        mLock.lock();
        try {
            if (mRotation == rotation) {
                return;
            }

            int temp = (rotation + 45) / 90 * 90;
            temp = (temp + DisplayRotation) % 360;
            temp = 360 - temp;
            if (mRotation == temp) {
                return;
            }

            mRotation = temp;
            mClearCanvas = 0;
            if (mDisMatrix != null) {
                mDisMatrix.setRotation((mRotation + mBmpRotation) % 360);
                UpdateMatrix();
            }
        } finally {
            mLock.unlock();
        }
    }

    public void setLayout(int lay) {
        mixVideoType = lay;
    }

    public void zoomIn() {
        if (mDisMatrix == null)
            return;

        mDisMatrix.zoomIn();
        mMatrix = mDisMatrix.getDisplayMatrix();
        mClearCanvas = 0;
    }

    public void zoomOut() {
        if (mDisMatrix == null)
            return;

        mDisMatrix.zoomOut();
        mMatrix = mDisMatrix.getDisplayMatrix();
        mClearCanvas = 0;
    }

    public void zoomTo(float scale, float cx, float cy, float durationMs) {
        mDisMatrix.zoomTo(scale, cx, cy, durationMs);
        mClearCanvas = -2;
    }

    public void translate(float dx, float dy) {
        if (mDisMatrix == null)
            return;

        mDisMatrix.translate(dx, dy);
        mMatrix = mDisMatrix.getDisplayMatrix();
        mClearCanvas = 0;
    }

    public float getBaseScale() {
        return mBaseScale;
    }

    public float getScale() {
        if (mDisMatrix == null) {
            return 0.0f;
        }
        return mDisMatrix.getScale();
    }

    public boolean isSuspended() {
        return mIsSuspended;
    }

    public void Release() {
        Log.d("VideoPlayer", "Release function call!");
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
        }

        mMatrix = null;
        mSurfaceH = null;
        mBitmap = null;
        mDisMatrix = null;
        _playBuffer = null;
    }

    @SuppressWarnings("unused")
    private void DestroyBitmap() {
        Log.w("V2TECH", "JNI destroy bitmap");
    }

    /*
     * Called by native
     */
    @SuppressWarnings("unused")
    private void SetBitmapRotation(int rotation) {
        mBmpRotation = rotation;
        mClearCanvas = 0;

        if (mDisMatrix != null) {
            mDisMatrix.setRotation((mRotation + mBmpRotation) % 360);
            UpdateMatrix();
        }
    }

    /*
     * Called by native
     * 杞В鐮佹墠浼氳蛋杩欎釜鍥炶皟
     */
    @SuppressWarnings("unused")
    private void CreateBitmap(int width, int height) {
        Log.i(TAG, "call create bitmap " + width + " " + height);
        mLock.lock();
        try {
            if (mIsSuspended || mSurfaceH == null
                    || !mSurfaceH.getSurface().isValid()) {
                return;
            }

            if (mBitmap != null && !mBitmap.isRecycled()) {
                mBitmap.recycle();
            }

            mBitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
            if (mDisMatrix == null)
                mDisMatrix = new VideoDisplayMatrix();

            mDisMatrix.setBitmap(mBitmap);
            mDisMatrix.setRotation((mRotation + mBmpRotation) % 360);

            mClearCanvas = 0;

            Canvas canvas = mSurfaceH.lockCanvas();
            int canvasWidth = canvas.getWidth();
            int canvasHeight = canvas.getHeight();
            float scaleRate = 0;
            if (canvas != null) {
                if (canvasWidth > canvasHeight) {
                    if (canvasHeight > height) {
                        scaleRate = (float) height / canvasHeight;
                    } else {
                        scaleRate = (float) canvasHeight / height;
                    }
                } else {
                    if (canvasWidth > width) {
                        scaleRate = (float) width / canvasWidth;
                    } else {
                        scaleRate = (float) canvasWidth / width;
                    }
                }
                Log.i(TAG, "get bitmap scale rate: " + scaleRate);
                setSurfaceViewSize(canvas.getWidth(), canvas.getHeight(),
                        scaleRate);
                mSurfaceH.unlockCanvasAndPost(canvas);
            }
            mScaleRate = (float) width / height;
            _playBuffer = ByteBuffer.allocateDirect(width * height * 4);
        } finally {
            mLock.unlock();
        }
    }

    private void UpdateMatrix() {
        mDisMatrix.resetMatrix();
        mMatrix = mDisMatrix.getDisplayMatrix();
        mBaseScale = mDisMatrix.getScale();
    }

    /*
     * Called by native
     */
    @SuppressWarnings("unused")
    private void OnPlayVideo() {
        mLock.lock();

        long mCurrentStamp = System.currentTimeMillis();
        if (mSurfaceCavansProcessListener != null
                && (mCurrentStamp - mLastTimeStamp) >= 4000) {
            mLastTimeStamp = mCurrentStamp;
            mSurfaceCavansProcessListener.callSurfaceCavansOnce(mIsSuspended,
                    mIsVideoDrawing);
        }

        try {
            if (mIsSuspended || mSurfaceH == null
                    || !mSurfaceH.getSurface().isValid()) {
                return;
            }

            Log.d(TAG,
                    "OnPlayVideo() SOFT --> Current Video Play's state is normal. datalen");

            mIsVideoDrawing = true;
            mBitmap.copyPixelsFromBuffer(_playBuffer);
            _playBuffer.rewind();
            Canvas canvas = mSurfaceH.lockCanvas();
            if (canvas == null) {
                return;
            }

            if (mClearCanvas < 2) {
                canvas.drawRGB(0, 0, 0);
                ++mClearCanvas;
            }

            Rect dest = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
            if (mMatrix == null) {
                canvas.drawBitmap(mBitmap, null, dest, null);
            } else {
                if (mIsCropBitmap) {
                    mDisMatrix.setBitmap(mBitmap);
                    mDisMatrix.setRotation((mRotation + mBmpRotation) % 360);
                    setSurfaceViewSize(canvas.getWidth(), canvas.getHeight());
                    canvas.drawBitmap(mBitmap, mMatrix, null);
                } else {
                    canvas.drawBitmap(mBitmap, null, dest, null);
                }
            }

            // draw border for combined video
            // if mixVideoType equals -1, means current video is not combined
            // video
            // FIXME this class should not cared combined video type,
            // see MixVideo.LayoutType
            if (mixVideoType > 0) {
                int width = canvas.getWidth();
                int height = canvas.getHeight();
                Paint p = new Paint();
                p.setColor(Color.WHITE);
                int boxHeight = 0;
                int boxWidth = 0;
                switch (mixVideoType) {
                    case 4:
                        boxHeight = height / 2;
                        boxWidth = width / 2;
                        canvas.drawLine(0, boxHeight, width, boxHeight, p);
                        canvas.drawLine(boxWidth, 0, boxWidth, height, p);
                        break;
                    case 6:
                        boxHeight = height / 3;
                        boxWidth = width / 3;
                        canvas.drawLine(boxWidth * 2, boxHeight, width, boxHeight,
                                p);
                        canvas.drawLine(0, boxHeight * 2, width, boxHeight * 2, p);

                        canvas.drawLine(boxWidth, boxHeight * 2, boxWidth, height,
                                p);
                        canvas.drawLine(boxWidth * 2, 0, boxWidth * 2, height, p);
                        break;
                    case 8:
                        boxHeight = height / 4;
                        boxWidth = width / 4;
                        canvas.drawLine(boxWidth * 3, boxHeight, width, boxHeight,
                                p);
                        canvas.drawLine(boxWidth * 3, boxHeight * 2, width,
                                boxHeight * 2, p);
                        canvas.drawLine(0, boxHeight * 3, width, boxHeight * 3, p);

                        canvas.drawLine(boxWidth, boxHeight * 3, boxWidth, height,
                                p);
                        canvas.drawLine(boxWidth * 2, boxHeight * 3, boxWidth * 2,
                                height, p);
                        canvas.drawLine(boxWidth * 3, 0, boxWidth * 3, height, p);
                        break;
                    case 9:
                        boxHeight = height / 3;
                        boxWidth = width / 3;

                        canvas.drawLine(0, boxHeight, width, boxHeight, p);
                        canvas.drawLine(0, boxHeight * 2, width, boxHeight * 2, p);

                        canvas.drawLine(boxWidth, 0, boxWidth, height, p);
                        canvas.drawLine(boxWidth * 2, 0, boxWidth * 2, height, p);
                        break;
                    case 101:
                        boxHeight = height / 4;
                        boxWidth = width / 4;

                        canvas.drawLine(0, boxHeight * 2, width, boxHeight * 2, p);
                        canvas.drawLine(0, boxHeight * 3, width, boxHeight * 3, p);

                        canvas.drawLine(boxWidth, boxHeight * 2, boxWidth, height,
                                p);
                        canvas.drawLine(boxWidth * 2, boxHeight * 2, boxWidth * 2,
                                height, p);
                        canvas.drawLine(boxWidth * 3, boxHeight * 2, boxWidth * 3,
                                height, p);
                        break;
                    case 11:
                        boxHeight = height / 4;
                        boxWidth = width / 4;
                        canvas.drawLine(0, boxHeight, boxWidth, boxHeight, p);
                        canvas.drawLine(boxWidth * 3, boxHeight, width, boxHeight,
                                p);
                        canvas.drawLine(0, boxHeight * 2, boxWidth, boxHeight * 2,
                                p);
                        canvas.drawLine(boxWidth * 3, boxHeight * 2, width,
                                boxHeight * 2, p);
                        canvas.drawLine(0, boxHeight * 3, width, boxHeight * 3, p);

                        canvas.drawLine(boxWidth, 0, boxWidth, height, p);
                        canvas.drawLine(boxWidth * 2, boxHeight * 3, boxWidth * 2,
                                height, p);
                        canvas.drawLine(boxWidth * 3, boxHeight * 3, boxWidth * 3,
                                height, p);
                        canvas.drawLine(boxWidth * 3, 0, boxWidth * 3, height, p);
                        break;
                    case 131:
                        boxHeight = height / 4;
                        boxWidth = width / 4;
                        canvas.drawLine(0, boxHeight, width, boxHeight, p);
                        canvas.drawLine(0, boxHeight * 2, boxWidth, boxHeight * 2,
                                p);
                        canvas.drawLine(boxWidth * 3, boxHeight * 2, width,
                                boxHeight * 2, p);
                        canvas.drawLine(0, boxHeight * 3, width, boxHeight * 3, p);

                        canvas.drawLine(boxWidth, 0, boxWidth, height, p);
                        canvas.drawLine(boxWidth * 2, 0, boxWidth * 2, boxHeight, p);
                        canvas.drawLine(boxWidth * 2, boxHeight * 3, boxWidth * 2,
                                height, p);
                        canvas.drawLine(boxWidth * 3, 0, boxWidth * 3, height, p);
                        break;
                    case 16:
                        boxHeight = height / 4;
                        boxWidth = width / 4;
                        canvas.drawLine(0, boxHeight, width, boxHeight, p);
                        canvas.drawLine(0, boxHeight * 2, width, boxHeight * 2, p);
                        canvas.drawLine(0, boxHeight * 3, width, boxHeight * 3, p);

                        canvas.drawLine(boxWidth, 0, boxWidth, height, p);
                        canvas.drawLine(boxWidth * 2, 0, boxWidth * 2, height, p);
                        canvas.drawLine(boxWidth * 3, 0, boxWidth * 3, height, p);
                        break;
                }
            }
            if (playerHandle != null) {
                playerHandle.playerHandle(true);
            }
            mSurfaceH.unlockCanvasAndPost(canvas);

        } finally {
            mLock.unlock();
        }
    }

    public interface SurfaceCavansProcessListener {

        void callSurfaceCavansOnce(boolean mIsSuspended, boolean mIsVideoDrawing);
    }

    // 绾剝袙閻焦绁︾粙锟�

    public void setRemoteVideoPlayCallBack(
            RemoteVideoPlayCallBack mRemoteVideoPlayCallBack) {
        this.mRemoteVideoPlayCallBack = mRemoteVideoPlayCallBack;
    }

    /**
     * Called by JNI
     */
    @SuppressLint("NewApi")
    @SuppressWarnings("unused")
    public void StartVideoPlay(int width, int height) {
        if (!isUseMediaCodec)
            return;
        mLock.lock();
        try {
            Log.i(TAG,
                    "StartVideoPlay() --> Start decoding the video data sent by the far end, and width :"
                            + " "
                            + width
                            + " | height : "
                            + height
                            + " | mSurfaceH : " + mSurfaceH);
            if (mRemoteVideoPlayCallBack != null) {
                mRemoteVideoPlayCallBack.receiveNewPlayParams(width, height,
                        true);
            }

            try {
                mediaCodec = MediaCodec.createDecoderByType("video/avc");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(
                    "video/avc", width, height);
            mediaCodec.configure(mediaFormat, mSurfaceH.getSurface(), null, 0);
            mediaCodec.start();
        } finally {
            mLock.unlock();
        }
    }

    /**
     * In the video call , when local SurfaceView and remote SurfaceVIew
     * transform the location and size of each other , UI need to call
     * startVideoPlay function to rebind the relationship between the viewer and
     * the decoder.
     *
     * @param width
     * @param height
     */
    public void startVideoPlay(int width, int height) {
        if (!isUseMediaCodec)
            return;
        mLock.lock();
        try {
            Log.d(TAG,
                    "startVideoPlay() --> Start decoding the video data sent by the far end, and width :"
                            + " "
                            + width
                            + " | height : "
                            + height
                            + " | mSurfaceH : " + mSurfaceH);

            if (mRemoteVideoPlayCallBack != null) {
                mRemoteVideoPlayCallBack.receiveNewPlayParams(width, height,
                        false);
            }

            try {
                // temps = new File("/sdcard/out.h264");
                // ips = new FileOutputStream(temps);
                mediaCodec = MediaCodec.createDecoderByType("video/avc");
                MediaFormat mediaFormat = MediaFormat.createVideoFormat(
                        "video/avc", width, height);
                mediaCodec.configure(mediaFormat, mSurfaceH.getSurface(), null,
                        0);
                mediaCodec.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Called by JNI
     */
    @SuppressLint("NewApi")
    @SuppressWarnings("unused")
    public void StopVideoPlay() {
        mLock.lock();
        try {
            Log.w(TAG, "StopVideoPlay() --> Stop the vieo.");
            if (mediaCodec != null) {
//                mediaCodec.stop();
//                mediaCodec.release();
                mediaCodec = null;
                _playBuffer = null;
                // temps.delete();
            }
        } finally {
            mLock.unlock();
        }
    }

    MediaCodec.BufferInfo _bufferInfo = new MediaCodec.BufferInfo();

    /**
     * Called by JNI
     */
    @SuppressLint("NewApi")
    @SuppressWarnings("unused")
    private void OnPlayVideo(int datalen) {
        mLock.lock();
        try {
            if (mediaCodec == null || isPausePlay) {
                return;
            }

            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis - mLastTimeMillis > 2000) {
                Log.d(TAG,
                        "OnPlayVideo() --> Current Video Play's state is normal. datalen : "
                                + datalen);
                mLastTimeMillis = currentTimeMillis;
            }

//			 ips.write(_playBuffer.array());
//			 ips.flush();

            ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
            int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
            Log.d(TAG, "inputBufferIndex " + inputBufferIndex);
            if (inputBufferIndex >= 0) {
//				ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
//				inputBuffer.clear();
//				inputBuffer.put(_playBuffer.array(), 0, datalen);
//				_playBuffer.rewind();
//				mediaCodec.queueInputBuffer(inputBufferIndex, 0, datalen,
//						mCount * 1000000 / 15, 0);
//				++mCount;

                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                byte[] arrPlayBuf = _playBuffer.array();
                int nBufOffset = _playBuffer.arrayOffset();
                inputBuffer.put(arrPlayBuf, nBufOffset, datalen);
                _playBuffer.rewind();
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, datalen,
                        mCount * 1000000 / 15, 0);
                ++mCount;
            }

            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(_bufferInfo, 50000);
            while (outputBufferIndex >= 0) {
                Log.d(TAG, "outputBufferIndex" + outputBufferIndex);
                mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
                outputBufferIndex = mediaCodec.dequeueOutputBuffer(_bufferInfo,
                        0);
            }
            if (playerHandle != null) {
                playerHandle.playerHandle(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mLock.unlock();
        }
    }

    private static PlayerHandle playerHandle;

    public static void setPlayerHandle(PlayerHandle playerHandle) {
        VideoPlayer.playerHandle = playerHandle;
    }

    public interface PlayerHandle {
        public void playerHandle(boolean startPlayer);
    }

    public interface RemoteVideoPlayCallBack {

        void receiveNewPlayParams(int width, int height, boolean isCallByJNI);
    }
}
