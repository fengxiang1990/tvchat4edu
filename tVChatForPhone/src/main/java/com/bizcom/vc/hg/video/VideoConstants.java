package com.bizcom.vc.hg.video;

import android.graphics.Canvas;

public class VideoConstants {

	public static String web_video_path="http://192.168.3.41:8989/iptv/tv/";
    /**
     * 记录播放位置
     */
    public static int playPosition=-1;
    
    private static  Canvas canvas;

    public static Canvas getCanvas() {
        return canvas;
    }

    public static void setCanvas(Canvas canvas) {
        VideoConstants.canvas = canvas;
    }
    
    
}
