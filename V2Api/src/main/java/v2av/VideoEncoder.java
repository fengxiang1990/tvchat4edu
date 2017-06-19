package v2av;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;

import java.io.File;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class VideoEncoder {
    private VideoEncoder() {
    }

    private static final String TAG = "DEBUG";

    private static boolean isUseMediaCodec = true;
    private static VideoEncoder mInstance;
    private MediaCodec mediaCodec;
    private OutputStream os;
    private MediaCodecInfo videoCodecInfo;
    private static int colorformat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
    protected static int[] recognizedFormats;
    private MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
    private int srcW, srcH;
    private int mBitrate;
    private byte[] mSPS_PPS = null;
    private File fOut = null; // new File("/sdcard/outEnc.h264");

    private File fOutYuv = null; //new File("/sdcard/out.yuv");
    private OutputStream sYuv;

    static {
        recognizedFormats = new int[]{
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar

        };
    }

    byte[] mYuvConverted = null;
    private static int yuvqueuesize = 10;
    private ArrayBlockingQueue<byte[]> YUVQueue = new ArrayBlockingQueue<byte[]>(yuvqueuesize);
    private boolean isRun = true;
    private Thread mThreadEnc;
    private int[] nColorMatched = new int[64];
    private int nColorMatchedNum = 0;
    private long presentationTimeUs;
    private static VideoNative videoNative;

    /**
     * 控制硬编还是软编的函数
     *
     * @param bEnable true为启用硬编，false为启用软编
     */
    public static void EnableMediaCodec(boolean bEnable) {
        isUseMediaCodec = bEnable;
        if (videoNative == null)
            videoNative = new VideoNative();
        videoNative.enablehardwarecodec(isUseMediaCodec, VideoPlayer.GetMediaCodecEnabled());
    }

    public static boolean GetMediaCodecEnabled() {
        return isUseMediaCodec;
    }

    public static VideoEncoder getInstance(int width, int height, int fps,
                                           int bitrate) {
        if (mInstance != null) {
            synchronized (VideoEncoder.class) {
                if (mInstance != null) {
                    if (mInstance.srcW == width && mInstance.srcH == height
                            && mInstance.mBitrate == bitrate) {
                        Log.i(TAG, "use the VideoEncoder same as last time");
                        return mInstance;
                    } else {
                        mInstance.Close();
                        mInstance = null;
                    }
                }
            }
        }

        if (mInstance == null) {
            mInstance = new VideoEncoder();
            mInstance.srcW = width;
            mInstance.srcH = height;
            mInstance.createEncoder(width, height, fps, bitrate);
        }
        return mInstance;
    }

    private void createEncoder(int width, int height, int fps, int bitrate) {
        Log.i(TAG, "VideoEncoder() --> Start initializing VideoEncoder...");
        srcW = width;
        srcH = height;
        mBitrate = bitrate;
        presentationTimeUs = new Date().getTime() * 1000;
        if (isUseMediaCodec) {
            videoCodecInfo = selectVideoCodec("video/avc");
            if (videoCodecInfo == null) {
                Log.e(TAG,
                        "VideoEncoder() --> Unable to find an appropriate codec for video/avc");
            }

            // Log.i(TAG,
            // "VideoEncoder() --> The end use of the MediaCodec is " +
            // videoCodecInfo.getName()
            // + " and ColorFormat : " + colorformat);
            Log.i(TAG, "VideoEncoder() --> Width is : " + width
                    + " | Height is : " + height + " | Fps is : " + fps
                    + " | Bitrate is : " + bitrate);
            try {
                mediaCodec = MediaCodec.createEncoderByType("video/avc");
                MediaFormat mediaFormat = MediaFormat.createVideoFormat(
                        "video/avc", width, height);
                mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate * 1000);
                mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, fps);
                // ZTE 固定设置 COLOR_FormatYUV420SemiPlanar
                //mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                //		MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
                // 其他设置为 colorformat
                mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                        nColorMatched[0]);
                mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
                mediaCodec.configure(mediaFormat, null, null,
                        MediaCodec.CONFIGURE_FLAG_ENCODE);

                mediaCodec.start();


            } catch (Exception e) {
                Log.e(TAG, "MediaCodec error creation or configure");
            }

            // try { os = new FileOutputStream(fOut); } catch (Exception e) { }
            // try { sYuv = new FileOutputStream(fOutYuv); } catch (Exception e) { }
        }
        startEncoderThread();
    }

    public native void encodeframe(byte[] databuf, int len);

    public void OnFrame(byte[] data, int offset, int length, int flag) {
        putYUVData(data, data.length);
        //Log.i(TAG, "OnFrame " + length);
    }

    private void putYUVData(byte[] buffer, int length) {
        if (YUVQueue.size() >= yuvqueuesize) {
            YUVQueue.poll();
        }
        YUVQueue.add(buffer);
    }

    // ZTE: bToNV12 = false, 其他 bToNV12 = true
    boolean bToNV12 = true;

    private void startEncoderThread() {
        mThreadEnc = new Thread(new Runnable() {

            @SuppressLint("NewApi")
            @Override
            public void run() {

                Log.i(TAG, "startEncoderThread");
                byte[] data = null;
                byte[] outData = new byte[1500];
                while (isRun) {
                    try {
                        data = YUVQueue.poll(30, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        data = null;
                    }

                    if (data == null)
                        continue;
                    // try { sYuv.write(data); sYuv.flush(); } catch
                    // (IOException e) {}
                    // try { Thread.sleep(140, 0); } catch (Exception e) {}
                    if (!isUseMediaCodec) {
                        encodeframe(data, data.length);
                    } else {
                        if (bToNV12) {
                            if (nColorMatched[0] == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)
                                NV21ToNV12(data, srcW, srcH);
                            else if (nColorMatched[0] == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar)
                                NV21ToI420(data, srcW, srcH);
                        }
                        try {
                            ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
                            ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
                            int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
                            if (inputBufferIndex >= 0) {
                                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                                inputBuffer.clear();
                                long pts = new Date().getTime() * 1000 - presentationTimeUs;
                                if (bToNV12) {
                                    inputBuffer.put(mYuvConverted);
                                    mediaCodec.queueInputBuffer(inputBufferIndex, 0,
                                            mYuvConverted.length, pts, 0);
                                } else {
                                    inputBuffer.put(data);
                                    mediaCodec.queueInputBuffer(inputBufferIndex, 0,
                                            data.length, pts, 0);
                                }
                            }
                            //Log.i("OnFrame", "dequeueInputBuffer " + inputBufferIndex);

                            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(
                                    bufferInfo, 12000);
                            while (outputBufferIndex >= 0) {

                                Log.i("OnFrame", "bufferInfo size is "
                                        + bufferInfo.size + " dequeueOutputBuffer "
                                        + outputBufferIndex);

                                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];

                                if (bufferInfo.size > outData.length)
                                    outData = new byte[bufferInfo.size];
                                outputBuffer.get(outData, 0, bufferInfo.size);

                                if (bufferInfo.size > 4) {
                                    int nalut = outData[4] & 0x1f;
                                    if (nalut == 5) {
                                        encodeframe(mSPS_PPS, mSPS_PPS.length);
                                    }
                                    if (nalut == 7) {
                                        saveSPS(outData, bufferInfo.size);
                                    }
                                }

                                //try { os.write(outData); os.flush(); } catch (IOException e) { }

                                encodeframe(outData, bufferInfo.size);

                                mediaCodec.releaseOutputBuffer(outputBufferIndex,
                                        false);
                                outputBufferIndex = mediaCodec.dequeueOutputBuffer(
                                        bufferInfo, 12000);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, "exception-->" + e.getMessage());
                        }
                    }
                }
                Log.i(TAG, "stop EncoderThread");
            }
        });
        mThreadEnc.start();
    }

    private void saveSPS(byte[] data, int nLength) {
        for (int i = 0; i < nLength - 4; i++) {
            if (data[i] == 0 && data[i + 1] == 0 && data[i + 2] == 0 && data[i + 3] == 1) {
                int nalut = data[i + 4] & 0x1f;
                if (nalut == 5) {
                    mSPS_PPS = new byte[i];
                    System.arraycopy(data, 0, mSPS_PPS, 0, i);
                    Log.i(TAG, "VideoEncoder SPS copied " + i);
                    break;
                }
            }
        }

        if (mSPS_PPS == null) {
            mSPS_PPS = new byte[nLength];
            System.arraycopy(data, 0, mSPS_PPS, 0, nLength);
            Log.i(TAG, "VideoEncoder SPS copied %i" + nLength);
        }
    }

    private void NV21ToI420(byte[] nv21buf, int width, int height) {
        int i, j;
        if (mYuvConverted == null) {
            mYuvConverted = new byte[nv21buf.length];
        }
        System.arraycopy(nv21buf, 0, mYuvConverted, 0, nv21buf.length);

        //copy Y
        //for(i=0; i<width*height; i++)
        //{
        //	mYuvConverted[i] = nv21buf[i];
        //}

        i = width * height;
        //copy U
        for (j = width * height + 1; j < width * height + width * height / 2; j += 2, i++) {
            mYuvConverted[i] = nv21buf[j];
        }

        //copy V
        for (j = width * height; j < width * height + width * height / 2; j += 2, i++) {
            mYuvConverted[i] = nv21buf[j];
        }
    }

    public void NV21ToNV12(byte[] nv21buf, int width, int height) {
        int i, j, framesize;
        if (mYuvConverted == null) {
            mYuvConverted = new byte[nv21buf.length];
        }
        System.arraycopy(nv21buf, 0, mYuvConverted, 0, nv21buf.length);
        framesize = width * height;
        //copy Y
        //for(i=0; i<framesize; i++)
        //{
        //	mYuvConverted[i] = nv21buf[i];
        //}
        for (j = 0; j < framesize / 2; j += 2) {
            mYuvConverted[framesize + j] = nv21buf[j + framesize + 1];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            mYuvConverted[framesize + j + 1] = nv21buf[j + framesize];
        }
    }

    /**
     * Called by JNI
     */
    public void Close() {
        try {
            if (mediaCodec != null) {
                while (mThreadEnc.isAlive()) {
                    isRun = false;
                    Thread.sleep(10);
                }

                Log.i(TAG, "VideoEncoder Close");
                mediaCodec.stop();
                mediaCodec.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected final MediaCodecInfo selectVideoCodec(final String mimeType) {
        // Get the list of available codec.
        final int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            final MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) { // skipp decoder
                continue;
            }
            // select first codec that match a specific MIME type and color
            // format
            boolean bCodecMatched = false;
            final String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    Log.i(TAG,
                            "selectVideoCodec() --> Select codec's name is : "
                                    + codecInfo.getName() + ", and MIME = "
                                    + types[j]);
                    bCodecMatched = true;
                    break;
                }
            }

            if (bCodecMatched) {
                MediaCodecInfo.CodecCapabilities caps = codecInfo
                        .getCapabilitiesForType(mimeType);
                for (int k = 0; k < recognizedFormats.length; k++) {
                    int colorFormat;
                    for (int j = 0; j < caps.colorFormats.length; j++) {
                        colorFormat = caps.colorFormats[j];
                        Log.i(TAG, "colorFormat: " + colorFormat);
                        if (recognizedFormats[k] == colorFormat) {
                            nColorMatched[nColorMatchedNum] = colorFormat;
                            nColorMatchedNum++;
                            Log.i(TAG, "matched colorformat: " + colorFormat);
                        }
                    }
                }
                return codecInfo;
            }
        }
        return null;
    }

    @SuppressLint("NewApi")
    protected static final int selectColorFormat(
            final MediaCodecInfo codecInfo, final String mimeType) {
        int result = 0;
        MediaCodecInfo.CodecCapabilities caps = codecInfo
                .getCapabilitiesForType(mimeType);
        int colorFormat;
        for (int i = 0; i < caps.colorFormats.length; i++) {
            colorFormat = caps.colorFormats[i];
            if (isRecognizedViewoFormat(colorFormat)) {
                if (result == 0)
                    result = colorFormat;
                break;
            }
        }
        if (result == 0)
            Log.e(TAG,
                    "selectColorFormat() --> Can't find a good color format for "
                            + codecInfo.getName() + " / " + mimeType);
        return result;
    }

    protected static boolean isRecognizedViewoFormat(final int colorFormat) {
        final int n = recognizedFormats != null ? recognizedFormats.length : 0;
        for (int i = 0; i < n; i++) {
            if (recognizedFormats[i] == colorFormat) {
                Log.i(TAG,
                        "isRecognizedViewoFormat() --> the color format has:"
                                + recognizedFormats[i]);
                return true;
            }
        }
        return false;
    }
}
