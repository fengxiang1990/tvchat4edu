package v2av;

import android.hardware.Camera;
import android.util.Log;

public class EncoderPreviewCallBack implements IPreviewCallBack {
    private VideoRecorder mRecorder = null;

    public EncoderPreviewCallBack(VideoRecorder recorder) {
        Log.i("DEBUG" , "EncoderPreviewCallBack");
        mRecorder = recorder;
    }

    public void onPreviewFrame(byte[] data, Camera camera) {
        camera.addCallbackBuffer(data);
        mRecorder.onGetVideoFrame(data, data.length);
    }

    public void SetFrameSize(int width, int height) {
    }
}


