package com.bizcom.vc.hg.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bizcom.request.V2ImRequest;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.util.BitmapManager;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.BitmapUtil;
import com.bizcom.util.V2Log;
import com.bizcom.vc.hg.util.BitmapUtils;
import com.bizcom.vc.hg.web.interf.BaseResponse;
import com.bizcom.vc.hg.web.interf.BussinessManger;
import com.bizcom.vc.hg.web.interf.ErrorResponse;
import com.bizcom.vc.hg.web.interf.IBussinessManager;
import com.bizcom.vc.hg.web.interf.SimpleResponseListener;
import com.bizcom.vc.hg.web.models.NickName;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.shdx.tvchat.phone.R;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by admin on 2016/12/6.
 */

/**
 * 母亲：
 * http://tvl.hongguaninfo.com/headPic/mum_select.png
 * http://tvl.hongguaninfo.com/headPic/mum_unselect.png
 * baby：
 * http://tvl.hongguaninfo.com/headPic/baby_select.png
 * http://tvl.hongguaninfo.com/headPic/baby_unselect.png
 * 父亲：
 * http://tvl.hongguaninfo.com/headPic/dad_select.png
 * http://tvl.hongguaninfo.com/headPic/dad_unselect.png
 * 祖父：
 * http://tvl.hongguaninfo.com/headPic/grandFather_select.png
 * http://tvl.hongguaninfo.com/headPic/grandFather_unselect.png
 * 祖母：
 * http://tvl.hongguaninfo.com/headPic/grandMother_select.png
 * http://tvl.hongguaninfo.com/headPic/grandMother_unselect.png
 */
public class NickNamePhotoSettingActivity extends CameraWithNoUploadActivity implements BitmapManager.BitmapChangedListener {

    String tag = "NickNamePhoto";

    Unbinder unBinder;

    @BindView(R.id.img_back)
    ImageView img_back;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.img_camera_bg)
    SimpleDraweeView img_camera_bg;

    @BindView(R.id.edit_nickname)
    EditText edit_nickname;

    @BindView(R.id.img_camera)
    ImageView img_camera;

    @BindView(R.id.img_shaizi_gif)
    SimpleDraweeView img_shaizi_gif;

    LinearLayoutManager layoutManager;

    List<Node> data = new ArrayList<>();

    User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.setNeedHandler(true);
        super.onCreate(savedInstanceState);
        user = (User) getIntent().getSerializableExtra("user");
        BitmapManager.getInstance().registerBitmapChangedListener(this);

    }

    Bitmap localBitmap;

    @Override
    public void onCamera(Bitmap bitmap) {
        localBitmap = bitmap;
        String str = BitmapUtils.bitmapToBase64(bitmap);
        String uriStr = "data:image/png;base64," + str;
        img_camera_bg.setImageURI(Uri.parse(uriStr));
        img_camera.setVisibility(View.GONE);
        for (Node node : data) {
            node.selected = false;
        }
        recyclerView.getAdapter().notifyDataSetChanged();
    }


    @Override
    public void addBroadcast(IntentFilter filter) {

    }

    @Override
    public void receiveBroadcast(Intent intent) {

    }


    @Override
    public void notifyAvatarChanged(User user, Bitmap bm) {
    }

    @Override
    public void receiveMessage(Message msg) {
        switch (msg.what) {
            case 0:
                Bitmap photo = (Bitmap) msg.obj;
                byte[] bitmap2Bytes = BitmapUtil.Bitmap2Bytes(photo);
                Log.e("receiveMessage", "bitmap2Bytes-->" + bitmap2Bytes.length);
                if (!GlobalHolder.getInstance().checkServerConnected(mContext)) {
                    V2ImRequest.invokeNative(V2ImRequest.NATIVE_CHANGE_OWNER_AVATAR, bitmap2Bytes,
                            bitmap2Bytes.length, ".png");
                }
                imService.updateUserInfo(user, new HandlerWrap(mHandler, UPDATE_NICKNAME, null));
                break;
            case UPDATE_NICKNAME:
                JNIResponse rlr = (JNIResponse) msg.obj;
                V2Log.d("JNIResponse -->" + rlr.getResult().name());
                if (rlr.getResult() != JNIResponse.Result.SUCCESS) {
                    Toast.makeText(mContext, "操作失败", Toast.LENGTH_SHORT).show();
                    pg.dismiss();
                    return;
                }
                if (rlr.getResult() == JNIResponse.Result.SUCCESS) {
                    String userId = user.getmUserId() + "";
                    userId = userId.substring(2, userId.length());
                    Log.e(tag, "user-->" + user.getId() + " " + user.getmUserId());
                    BussinessManger.getInstance(mContext).updateNickNameByUid(new IBussinessManager.OnResponseListener() {
                        @Override
                        public void onResponse(boolean isSuccess, int what, Object obj) {
                            Log.e(tag, "updateNickNameByUid isSuccess-->" + isSuccess);
                            Log.e(tag, "updateNickNameByUid obj-->" + String.valueOf(obj));
                            if (isSuccess) {
                                Toast.makeText(mContext, "设置成功", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(NickNamePhotoSettingActivity.this, HomeActivity.class));
                                finish();
                            }
                            pg.dismiss();
                        }
                    }, nickName, userId);
                }

                break;
        }
    }

    @Override
    public void initViewAndListener() {
        setContentView(R.layout.activity_firststartup_nickname);
        unBinder = ButterKnife.bind(this);
        img_back.setImageResource(R.mipmap.x);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(OrientationHelper.HORIZONTAL);
        initRecyleViewData();
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new PhotoAdapter());
        user = GlobalHolder.getInstance().getCurrentUser();
        if (user != null) {
            //edit_nickname.setText("tv_" + (TextUtils.isEmpty(user.getAccount()) ? "" : user.getAccount()));
            createName();
        }
    }

    @Override
    public void receiveNewAvatar(User targetUser, Bitmap bnewAvatarm) {

    }


    V2ImRequest imService = new V2ImRequest();
    public static final int UPDATE_NICKNAME = 1;
    String nickName;

    @OnClick(R.id.btn_getname)
    void getName() {
        createName();
    }


    void createName() {
        edit_nickname.setEnabled(false);
        img_shaizi_gif.setVisibility(View.VISIBLE);
        Uri uri2 = Uri.parse("asset:///" + "shaizi.gif");
        DraweeController controller2 = Fresco.newDraweeControllerBuilder()
                .setUri(uri2)
                .setAutoPlayAnimations(true)
                .build();
        img_shaizi_gif.setController(controller2);
        String uid = String.valueOf(user.getmUserId()).substring(0, 2);
        BussinessManger.getInstance(this).getNickNameByRandom(uid, new SimpleResponseListener<BaseResponse<NickName>>() {
            @Override
            protected void onSuccess(final BaseResponse<NickName> t) {

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        NickName nickNameCla = t.data;
                        String nickName = nickNameCla.nickName;
                        edit_nickname.setEnabled(true);
                        img_shaizi_gif.setVisibility(View.INVISIBLE);
                        edit_nickname.setText(nickName);
                    }
                }, 1000);


            }

            @Override
            protected void onError(ErrorResponse response) {
                Log.e(tag, "getNickNameByRandom onError:" + response.message);
            }
        });
    }

    ProgressDialog pg;

    int seconds = 0;

    class CountTimeRunnable implements Runnable {

        @Override
        public void run() {
            if (seconds++ > 30) {
                if (pg != null) {
                    pg.dismiss();
                }
            } else {
                mHandler.postDelayed(this, 1000);
            }
        }
    }

    @OnClick(R.id.btn_ok)
    void okClick() {
        nickName = String.valueOf(edit_nickname.getText());
        if (TextUtils.isEmpty(nickName)) {
            Toast.makeText(mContext, "昵称不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        user.setNickName(nickName);
        //匹配中文
        String regexStr = "[\u4E00-\u9FA5]";
        Pattern p = Pattern.compile(regexStr);
        Matcher m = p.matcher(nickName);
        if (m.find()) {
            if (nickName.length() > 12) {
                Toast.makeText(mContext, "只能输入12个汉字或24个字母", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            if (nickName.length() > 24) {
                Toast.makeText(mContext, "只能输入12个汉字或24个字母", Toast.LENGTH_SHORT).show();
                return;
            }
        }
//        if (!TextUtils.isEmpty(nickName) && nickName.length() > 12) {
//            Toast.makeText(NickNamePhotoSettingActivity.this, "昵称不能超过12个字符", Toast.LENGTH_SHORT).show();
//            return;
//        }
        if (pg == null) {
            pg = ProgressDialog.show(NickNamePhotoSettingActivity.this, "", "请求中...");
        }
        pg.show();
        mHandler.post(new CountTimeRunnable());
        new Thread() {
            public void run() {
                Node selectedNode = null;
                for (Node node : data) {
                    if (node.selected) {
                        selectedNode = node;
                        break;
                    }
                }
                if (localBitmap != null) {
                    Message message = mHandler.obtainMessage();
                    message.what = 0;
                    message.obj = localBitmap;
                    mHandler.sendMessage(message);
                }
                if (selectedNode != null) {
                    HttpURLConnection urlConnection = null;
                    try {
                        URL url = new URL(selectedNode.url_unselected);
                        urlConnection = (HttpURLConnection) url.openConnection();
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        Bitmap photo = BitmapFactory.decodeStream(in);
                        Message message = mHandler.obtainMessage();
                        message.what = 0;
                        message.obj = photo;
                        mHandler.sendMessage(message);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        urlConnection.disconnect();
                    }

                }
            }
        }.start();
    }

    @OnClick(R.id.img_camera)
    void imgCameraClick() {
        popSelectPicture.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
    }

    @OnClick(R.id.img_camera_bg)
    void imgCameraBgClick() {
        popSelectPicture.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
    }

    @OnClick(R.id.img_back)
    void back() {
        startActivity(new Intent(NickNamePhotoSettingActivity.this, HomeActivity.class));
        finish();
    }


    class PhotoAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.draweeview, parent, false));
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final Node node = data.get(position);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.leftMargin = 12;
            if (node.selected) {
                layoutParams.width = getWindowManager().getDefaultDisplay().getWidth() / 5;
                layoutParams.height = getWindowManager().getDefaultDisplay().getWidth() / 5;
                holder.draweeView.setImageURI(Uri.parse(node.url_selected));
            } else {
                layoutParams.width = getWindowManager().getDefaultDisplay().getWidth() / 6;
                layoutParams.height = getWindowManager().getDefaultDisplay().getWidth() / 6;
                holder.draweeView.setImageURI(Uri.parse(node.url_unselected));
            }
            holder.draweeView.setLayoutParams(layoutParams);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (Node node1 : data) {
                        node1.selected = false;
                    }
                    node.selected = true;
                    img_camera.setVisibility(View.VISIBLE);
                    img_camera_bg.setImageURI(Uri.EMPTY);
                    if (localBitmap != null && !localBitmap.isRecycled()) {
                        localBitmap.recycle();
                        localBitmap = null;
                    }
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        public SimpleDraweeView draweeView;

        public MyViewHolder(View itemView) {
            super(itemView);
            draweeView = (SimpleDraweeView) itemView.findViewById(R.id.draweeView);
        }


    }

    void initRecyleViewData() {
        Node node1 = new Node("mother");
        Node node2 = new Node("father");
        Node node3 = new Node("grandma");
        Node node4 = new Node("grandpa");
        Node node5 = new Node("baby");

        node1.url_selected = "http://tvl.hongguaninfo.com/headPic/mum_select.png";
        node1.url_unselected = "http://tvl.hongguaninfo.com/headPic/mum_unselect.png";

        node2.url_selected = "http://tvl.hongguaninfo.com/headPic/dad_select.png";
        node2.url_unselected = "http://tvl.hongguaninfo.com/headPic/dad_unselect.png";

        node3.url_selected = "http://tvl.hongguaninfo.com/headPic/grandMother_select.png";
        node3.url_unselected = "http://tvl.hongguaninfo.com/headPic/grandMother_unselect.png";

        node4.url_selected = "http://tvl.hongguaninfo.com/headPic/grandFather_select.png";
        node4.url_unselected = "http://tvl.hongguaninfo.com/headPic/grandFather_unselect.png";

        node5.url_selected = "http://tvl.hongguaninfo.com/headPic/baby_select.png";
        node5.url_unselected = "http://tvl.hongguaninfo.com/headPic/baby_unselect.png";
        node5.selected = true;

        data.add(node4);
        data.add(node3);
        data.add(node5);
        data.add(node2);
        data.add(node1);

    }

    class Node {

        public Node(String tag) {
            this.tag = tag;
        }

        public boolean selected = false;
        public String tag;
        public String url_selected;
        public String url_unselected;
    }

}
