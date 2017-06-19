package com.bizcom.vc.hg.ui.edu.view;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bizcom.vc.activity.conference.ConferenceSurfaceView;

/**
 * Created by admin on 2017/2/12.
 */

public class StudentSurfaceLayout extends FrameLayout {

    String tag = "StudentSurfaceLayout";

    private ConferenceSurfaceView surfaceView;
    private String studentName;
    private TextView studentNameView;

    public StudentSurfaceLayout(Context context) {
        super(context);
        init(context);
    }

    public StudentSurfaceLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }


    void init(Context context) {
        Log.e(tag, "init student surfacelayout");
        setBackgroundColor(Color.WHITE);
        setPadding(5, 5, 5, 5);
        surfaceView = new ConferenceSurfaceView(context);
        surfaceView.setZOrderOnTop(true);
        surfaceView.setZOrderMediaOverlay(true);
        studentNameView = new TextView(context);
        studentNameView.setTextColor(Color.WHITE);
        studentNameView.setSingleLine();
        studentNameView.setMaxEms(5);
        studentNameView.setEllipsize(TextUtils.TruncateAt.END);
        studentNameView.setTextSize(10);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.START;
        params.leftMargin = 8;
        params.bottomMargin = 8;
        Log.e(tag, "add student surfaceview");
        addView(surfaceView);
        addView(studentNameView, params);
    }


    public ConferenceSurfaceView getSurfaceView() {
        return surfaceView;
    }

    public void setSurfaceView(ConferenceSurfaceView surfaceView) {
        this.surfaceView = surfaceView;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
        studentNameView.setText(this.getStudentName().isEmpty() ? "" : this.getStudentName());
    }
}
