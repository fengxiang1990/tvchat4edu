/*
 * Copyright (C) 2014 zzl09
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bizcom.vc.activity.contacts.clipavatar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

import com.bizcom.vc.activity.contacts.ContactDetail2;
import com.config.GlobalHolder;
import com.shdx.tvchat.phone.R;

import java.io.File;

public class ClipAvatarActivity extends Activity implements OnClickListener {
    private ClipAvatarLayout mClipLayout;
    private boolean isCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clip_picture);
        String imgPath = getIntent().getStringExtra(ContactDetail2.EXTRA_KEY_IMAGE_PATH);
        isCamera = getIntent().getBooleanExtra("fromPlace", false);
        findViewById(R.id.ok).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
        initView();
        initBitmap(imgPath);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        i.putExtra("fromPlace", isCamera);
        setResult(ContactDetail2.ACTIVITY_PICK_AVATAR_SIZE_CANCEL, i);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mClipLayout.onDestory();
    }

    private void initView() {
        mClipLayout = (ClipAvatarLayout) findViewById(R.id.clip_layout);
    }

    private void initBitmap(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            Window window = getWindow();
            Bitmap bitmap = BitmapUtils.createImageThumbnailScale(filePath, 800);
            if (bitmap != null) {
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                if (width > height) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                }
            }
            if (bitmap != null && !bitmap.isRecycled()) {
                mClipLayout.setSourceImage(bitmap, window);
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

    private void clipBitmap() {
        if (GlobalHolder.getInstance().checkServerConnected(this)) {
            return;
        }
        Bitmap bitmap = mClipLayout.getBitmap();
        Intent i = new Intent();
        i.putExtra("data", bitmap);
        i.putExtra("fromPlace", isCamera);
        setResult(ContactDetail2.ACTIVITY_PICK_AVATAR_SIZE, i);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ok:
                clipBitmap();
                break;
            case R.id.cancel:
                onBackPressed();
                break;
            default:
                break;
        }
    }
}
