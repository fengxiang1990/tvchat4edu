package com.bizcom.vc.activity.contacts;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bizcom.bo.ConversationNotificationObject;
import com.bizcom.bo.GroupUserObject;
import com.bizcom.request.V2ContactsRequest;
import com.bizcom.request.V2ImRequest;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.service.JNIService;
import com.bizcom.util.BitmapUtil;
import com.bizcom.util.DialogManager;
import com.bizcom.util.MessageUtil;
import com.bizcom.util.V2Log;
import com.bizcom.util.V2Toast;
import com.bizcom.util.WaitDialogBuilder;
import com.bizcom.vc.activity.BaseActivity;
import com.bizcom.vc.activity.contacts.clipavatar.ClipAvatarActivity;
import com.bizcom.vc.activity.conversation.ConversationSelectImageActivity;
import com.bizcom.vc.activity.message.MessageAuthenticationActivity;
import com.bizcom.vc.widget.MarqueeTextView;
import com.bizcom.vc.widget.cus.edittext.ClearEditText;
import com.bizcom.vo.Conversation;
import com.bizcom.vo.Group;
import com.bizcom.vo.User;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.config.PublicIntent;
import com.config.V2GlobalConstants;
import com.shdx.tvchat.phone.R;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ContactDetail2 extends BaseActivity {

    public static final String EXTRA_KEY_IMAGE_PATH = "extra_key_image_path";

    private static final int UPDATE_USER_INFO = 2;
    private static final int UPDATE_USER_INFO_DONE = 3;
    private static final int DELETE_CONTACT_USER = 4;

    public static final int ACTIVITY_PICK_CAMERA = 0x002;
    public static final int ACTIVITY_PICK_PHOTO = 0x003;
    public static final int ACTIVITY_PICK_AVATAR_SIZE = 0x004;
    public static final int ACTIVITY_PICK_AVATAR_SIZE_CANCEL = 0x005;

    private static final int ORG_SAME_CONTACT = 6;
    private static final int AUTH_MESSAGE_TYPE = 7;

    private static final int ACTIVITY_PICK_BELONG_GROUP = 0x001;
    public static final int START_AUTHENTICATION_ACTIVITY = 0;

    private long mUid;
    private long mLoginUserID;
    private User mShowInfoUser;
    private V2ImRequest mImRequestService = new V2ImRequest();
    private V2ContactsRequest mContactRequestService = new V2ContactsRequest();

    private TextView mNameTitleIV;
    private ImageView mHeadIconIV;
    private TextView mTitleContent;

    private LinearLayout mSignTVLayout;
    private View mSignTVLine;
    private RadioGroup mGenderRG;
    private ClearEditText mSignatureTV;
    private ClearEditText mAccountTV;
    private ClearEditText mGendarTV;
    private TextView mBirthdayTV;
    private ClearEditText mCellphoneTV;
    private ClearEditText mFaxphoneTV;
    private ClearEditText mTelephoneTV;
    private ClearEditText mPostJobTV;
    private ClearEditText mAddressTV;
    private MarqueeTextView mSignTV;
    private ClearEditText mDeptTV;
    private ClearEditText mCompanyTV;
    private EditText[] mPersonInfoEdits;
    private Calendar mBirthdayDefDate;
    private Date mOwnerBirthday;

    private View mCompanyLayout;
    private View mDeptLayout;
    private View mPostJobLayout;
    private View mCompanyLayoutDevider;
    private View mDeptLayoutDevider;
    private View mPostJobLayoutDevider;

    private ClearEditText mNickNameET;
    private TextView mGroupNameTV;
    private TextView mAddContactButton;
    private View mUpdateContactGroupButton;
    private View mStartConfMeetingButton;
    private View mDeleteContactButton;

    private OnTouchListener contactDetailMainLayoutTouchListener = new ContactDetailMainLayoutTouchListener();
    private OnClickListener returnButtonListener = new ReturnButtonListener();
    private OnClickListener updateContactGroupButtonListener = new UpdateContactGroupButtonListener();
    private OnClickListener mStartConfMeeingClickListener = new StartConfMeeingClickListener();
    private OnClickListener addContactButtonClickListener = new AddContactButtonClickListener();
    private OnClickListener deleteContactButtonClickListener = new DeleteContactButtonClickListener();
    private OnClickListener mChangeOwnerAvatarClickListener = new ChangeOwnerAvatarClickListener();

    private Dialog deleteContactDialog;

    private boolean isRelation;
    private Group belongs;

    private int currentContactType = ORG_SAME_CONTACT;
    private String fromActivity;

    private boolean isOutOrgUser;
    private boolean isOwnerNeedUpdate;
    private boolean isOwnerSexDefault;
    private boolean isNeedUpdate;
    private boolean isShowDate;
    private String currentEditNickName = null;
    private Dialog avatarDialog;

    // 好友验证相关
    private LinearLayout llAuthenticationMessageLayout;
    private TextView tvAuthenticationState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(android.R.style.Theme_Holo_Light_NoActionBar);
        // 当前所展示信息的用户
        mUid = this.getIntent().getLongExtra("uid", 0);
        mShowInfoUser = GlobalHolder.getInstance().getUser(mUid);
        mLoginUserID = GlobalHolder.getInstance().getCurrentUserId();
        this.setContentView(R.layout.activity_contact_detail_2);
        super.setNeedBroadcast(true);
        super.setNeedHandler(true);
        super.setNeedAvatar(true);
        super.onCreate(savedInstanceState);

        // 不同页面跳转过来
        fromActivity = this.getIntent().getStringExtra("fromActivity");
        if (fromActivity != null) {
            if ("MessageAuthenticationActivity".equals(fromActivity)) {
                currentContactType = AUTH_MESSAGE_TYPE;
            } else if ("MessageAuthenticationActivity-ContactDetail".equals(fromActivity)) {
                currentContactType = ORG_SAME_CONTACT;
            } else if (fromActivity.equals("SearchResultActivity")) {
                isOutOrgUser = getIntent().getBooleanExtra("isOutOrg", false);
            }
        }
        // 判断好友关系
        List<Group> friendGroup = GlobalHolder.getInstance().getGroup(V2GlobalConstants.GROUP_TYPE_CONTACT);
        for (Group group : friendGroup) {
            if ((belongs = group.findUser(mShowInfoUser)) != null) {
                isRelation = true;
                break;
            }
        }
        updateContactGroup();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mImRequestService.clearCalledBack();
        mContactRequestService.clearCalledBack();
        if (mNickNameET != null) {
            mNickNameET.removeTextChangedListener(tw);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mShowInfoUser != null) {
            showUserInfo();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String receiveNickName = intent.getStringExtra("nickName");
        if (mShowInfoUser != null)
            mShowInfoUser.setCommentName(receiveNickName);
        mNickNameET.setText(receiveNickName);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        GlobalHolder.getInstance().checkServerConnected(mContext);
        if (mShowInfoUser.getmUserId() == mLoginUserID) {
            if (isOwnerNeedUpdate) {
                Message m = Message.obtain(mHandler, UPDATE_USER_INFO);
                mHandler.sendMessage(m);
            }
        } else {
            if (isNeedUpdate && !mNickNameET.getText().toString().equals(mShowInfoUser.getCommentName())) {
                isNeedUpdate = false;
                Message m = Message.obtain(mHandler, UPDATE_USER_INFO);
                mHandler.sendMessage(m);
            }
        }
        super.onBackPressed();
    }

    @Override
    public void addBroadcast(IntentFilter filter) {
        filter.addAction(JNIService.JNI_BROADCAST_CONTACTS_AUTHENTICATION);
        filter.addAction(JNIService.JNI_BROADCAST_USER_UPDATE_BASE_INFO);
        filter.addAction(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED);
    }

    @Override
    public void receiveBroadcast(Intent intent) {
        String action = intent.getAction();
        if (action.equals(JNIService.JNI_BROADCAST_CONTACTS_AUTHENTICATION)) {
            long uid = intent.getLongExtra("uid", -1);
            if (uid == -1) {
                return;
            }

            if (uid == mShowInfoUser.getmUserId()) {
                long gid = intent.getLongExtra("gid", -1);
                if (gid == -1) {
                    // OnRefuseInviteJoinGroup 我加别人被拒绝
                    String authType = intent.getStringExtra("authType");
                    if (authType != null && authType.equals("refuseInvite") && "MessageAuthenticationActivity".equals(fromActivity)) {
                        mSignTVLayout.setVisibility(View.GONE);
                        mAddContactButton.setVisibility(View.GONE);

                        llAuthenticationMessageLayout.setVisibility(View.VISIBLE);
                        tvAuthenticationState.setVisibility(View.VISIBLE);
                        tvAuthenticationState.setText(R.string.contactDetail_AuthenticationState_refused_your_apply);
                        mTitleContent.setText(R.string.contactDetail_titlebar_title_text2);
                    }
                } else {
                    if ((belongs = GlobalHolder.getInstance().getGroupById(gid)) == null) {
                        return;
                    }

                    // update group
                    isRelation = true;
                    if (currentContactType == AUTH_MESSAGE_TYPE) {
                        llAuthenticationMessageLayout.setVisibility(View.GONE);
                        mTitleContent.setText(R.string.contactDetail_titlebar_title_text);
                        mAddContactButton.setVisibility(View.GONE);
                        mDeleteContactButton.setVisibility(View.VISIBLE);
                        mStartConfMeetingButton.setVisibility(View.VISIBLE);
                    } else {
                        currentEditNickName = mShowInfoUser.getCommentName();
                        mGroupNameTV = (TextView) findViewById(R.id.detail_detail_2_group_name);
                        mGroupNameTV.setText(belongs.getName());
                        mAddContactButton.setVisibility(View.GONE);
                        mDeleteContactButton.setVisibility(View.VISIBLE);
                        mSignTVLayout.setVisibility(View.VISIBLE);
                        mSignTVLine.setVisibility(View.VISIBLE);
                    }
                    // update user info
                    mShowInfoUser = GlobalHolder.getInstance().getUser(uid);
                    showUserInfo();
                    Toast.makeText(
                            mContext,
                            R.string.authenticationActivity_send_success_hint,
                            Toast.LENGTH_SHORT).show();
                }
            }
        } else if (action.equals(JNIService.JNI_BROADCAST_GROUP_USER_REMOVED)) {
            GroupUserObject obj = intent.getParcelableExtra("obj");
            if (obj.getmUserId() != mShowInfoUser.getmUserId()) {
                return;
            }

            if (mShowInfoUser.isOutOrg) {
                finish();
            }

            if (obj.getmType() == V2GlobalConstants.GROUP_TYPE_CONTACT) {
                // 现在不是解除好友关系，而是销毁界面
//				belongs = null;
//				isRelation = false;
//				updateContactGroup();
                finish();
            } else if (obj.getmType() == V2GlobalConstants.GROUP_TYPE_DEPARTMENT) {
                if (obj.getmUserId() == mShowInfoUser.getmUserId()) {
                    finish();
                }
            }
        } else if (action.equals(JNIService.JNI_BROADCAST_USER_UPDATE_BASE_INFO)) {
            long uid = intent.getLongExtra("uid", -1);
            if (uid == -1) {
                return;
            }

            if (uid == mShowInfoUser.getmUserId()) {
                mShowInfoUser = GlobalHolder.getInstance().getUser(uid);
                showUserInfo();
            }
        }
    }

    @Override
    public void receiveMessage(Message msg) {
        switch (msg.what) {
            case UPDATE_USER_INFO:
                String nothing = getResources().getString(R.string.contacts_user_detail_item_nothing);
                if (mShowInfoUser.getmUserId() == mLoginUserID) {
                    if (!nothing.equals(mSignatureTV.getText().toString())) {
                        mShowInfoUser.setSignature(mSignatureTV.getText().toString());
                    } else {
                        mShowInfoUser.setSignature("");
                    }

                    if (!nothing.equals(getValueOfSexByRadioValue())) {
                        mShowInfoUser.setSex(getValueOfSexByRadioValue());
                    } else {
                        mShowInfoUser.setSex("");
                    }

                    mShowInfoUser.setBirthday(mOwnerBirthday);
                    if (!nothing.equals(mCellphoneTV.getText().toString())) {
                        mShowInfoUser.setMobile(mCellphoneTV.getText().toString());
                    } else {
                        mShowInfoUser.setMobile("");
                    }

                    if (!nothing.equals(mFaxphoneTV.getText().toString())) {
                        mShowInfoUser.setFax(mFaxphoneTV.getText().toString());
                    } else {
                        mShowInfoUser.setFax("");
                    }

                    if (!nothing.equals(mTelephoneTV.getText().toString())) {
                        mShowInfoUser.setTelephone(mTelephoneTV.getText().toString());
                    } else {
                        mShowInfoUser.setTelephone("");
                    }

                    if (!nothing.equals(mAddressTV.getText().toString())) {
                        mShowInfoUser.setAddress(mAddressTV.getText().toString());
                    } else {
                        mShowInfoUser.setAddress("");
                    }
                } else {
                    if (!nothing.equals(mNickNameET.getText().toString())) {
                        mShowInfoUser.setCommentName(mNickNameET.getText().toString());
                    } else {
                        mShowInfoUser.setCommentName("");
                    }
                }
                mImRequestService.updateUserInfo(mShowInfoUser, new HandlerWrap(mHandler, UPDATE_USER_INFO_DONE, null));
                break;
            case UPDATE_USER_INFO_DONE:
                JNIResponse userRes = (JNIResponse) msg.obj;
                if (userRes.getResult() == JNIResponse.Result.SUCCESS) {
                    V2Log.d("ContactDetail2 --> update user info SUCCESS! user name is : " + mShowInfoUser.getDisplayName());
                    Intent intent = new Intent();
                    intent.setAction(PublicIntent.BROADCAST_USER_COMMENT_NAME_NOTIFICATION);
                    intent.addCategory(PublicIntent.DEFAULT_CATEGORY);
                    intent.putExtra("modifiedUser", mShowInfoUser.getmUserId());
                    sendBroadcast(intent);
                } else {
                    V2Log.d("ContactDetail2 --> update user info TIME_OUT! user name is : " + mShowInfoUser.getDisplayName());
                }
                break;
            case DELETE_CONTACT_USER:
                WaitDialogBuilder.dismissDialog();
                JNIResponse response = (JNIResponse) msg.obj;
                if (response.getResult() == JNIResponse.Result.SUCCESS) {
                    // Send a broadcast to all interface to update user nickname
                    if (!TextUtils.isEmpty(mShowInfoUser.getNickName())) {
                        Intent i = new Intent();
                        i.setAction(PublicIntent.BROADCAST_USER_COMMENT_NAME_NOTIFICATION);
                        i.addCategory(PublicIntent.DEFAULT_CATEGORY);
                        i.putExtra("modifiedUser", mShowInfoUser.getmUserId());
                        sendBroadcast(i);
                    }

                    if ((fromActivity != null) && (fromActivity.equals("MessageAuthenticationActivity"))) {
                        belongs = null;
                        isRelation = false;
                        updateContactGroup();
                    } else if ((fromActivity != null)
                            && (fromActivity.equals("MessageAuthenticationActivity-ContactDetail"))) {
//					belongs = null;
//					isRelation = false;
//					updateContactGroup();
                        finish();
                    } else if ((fromActivity != null) && (fromActivity.equals("SearchedResultActivity"))) {
                        belongs = null;
                        isRelation = false;
                        updateContactGroup();
                    } else {
                        if ((fromActivity != null) && fromActivity.equals("SearchedResultActivity-ContactDetail")) {
                            finish();
                        } else {
                            // NOTICE 到底删除好友时要不要保留上一层界面
                            // Intent i = new Intent(mContext, MainActivity.class);
                            // i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            // mContext.startActivity(i);
                            finish();
                        }

                        Intent intent = new Intent(PublicIntent.REQUEST_UPDATE_CONVERSATION);
                        intent.addCategory(PublicIntent.DEFAULT_CATEGORY);
                        ConversationNotificationObject obj = new ConversationNotificationObject(
                                Conversation.TYPE_VERIFICATION_MESSAGE, Conversation.SPECIFIC_VERIFICATION_ID, false);
                        intent.putExtra("obj", obj);
                        mContext.sendBroadcast(intent);
                    }

                } else if (response.getResult() == JNIResponse.Result.TIME_OUT) {
                    Toast.makeText(mContext, mContext.getString(R.string.contacts_delete_net_failed), Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.contacts_delete_failed), Toast.LENGTH_SHORT)
                            .show();
                }
                break;
        }
    }

    @Override
    public void initViewAndListener() {
        View contactDetailMainLayout = findViewById(R.id.contact_detail_main_layout);
        contactDetailMainLayout.setOnTouchListener(contactDetailMainLayoutTouchListener);

        mTitleContent = (TextView) findViewById(R.id.ws_common_activity_title_content);
        mTitleContent.setText(R.string.contacts_detail_title);

        TextView mTitleLeftTV = (TextView) findViewById(R.id.ws_common_activity_title_left_button);
        mTitleLeftTV.setText(R.string.contacts_user_detail_return_button);
        mTitleLeftTV.setOnClickListener(returnButtonListener);

        TextView mTitleRightTV = (TextView) findViewById(R.id.ws_common_activity_title_right_button);
        mTitleRightTV.setVisibility(View.INVISIBLE);

        View commonLy = findViewById(R.id.contact_user_detail_button_layout);
        commonLy.setVisibility(View.VISIBLE);

        llAuthenticationMessageLayout = (LinearLayout) findViewById(R.id.authentication_message_layout);
        mAddContactButton = (TextView) findViewById(R.id.contact_user_detail_add_friend);
        mAddContactButton.setOnClickListener(addContactButtonClickListener);
        mDeleteContactButton = findViewById(R.id.contact_user_detail_delete_friend);
        mDeleteContactButton.setOnClickListener(deleteContactButtonClickListener);
        //好友分组暂时不用
        mUpdateContactGroupButton = findViewById(R.id.contact_detail_contact_group_item_ly);
        mUpdateContactGroupButton.setOnClickListener(updateContactGroupButtonListener);
        mStartConfMeetingButton = findViewById(R.id.ws_contact_detail2_conf);
        mStartConfMeetingButton.setOnClickListener(mStartConfMeeingClickListener);

        if (mShowInfoUser.getmUserId() == mLoginUserID) {
            //自己的头部信息
            findViewById(R.id.contact_detail_ownerly).setVisibility(View.VISIBLE);
            findViewById(R.id.contact_detail_ohterly).setVisibility(View.INVISIBLE);
            mNameTitleIV = (TextView) findViewById(R.id.contact_detail_owner_name);
            mHeadIconIV = (ImageView) findViewById(R.id.contact_detail_owner_avatar);
            mHeadIconIV.setOnClickListener(mChangeOwnerAvatarClickListener);
            //性别
            mGenderRG = (RadioGroup) findViewById(R.id.contact_user_detail_gender_rg);
            mGenderRG.setVisibility(View.VISIBLE);
            mGenderRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup rg, int id) {
                    isOwnerNeedUpdate = true;
                }

            });
            findViewById(R.id.contact_user_detail_gender_tv).setVisibility(View.GONE);
            //生日
            mBirthdayTV = (TextView) findViewById(R.id.contact_user_detail_birthday_tv);
            mBirthdayTV.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    mBirthdayDefDate = Calendar.getInstance();
                    mBirthdayDefDate.setTime(mOwnerBirthday);
                    new MyDatePickerDialog(mContext, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker dp, int year, int monthOfYear, int dayOfMonth) {
                            ((TextView) v).setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                            Calendar cl = Calendar.getInstance();
                            cl.set(Calendar.YEAR, year);
                            cl.set(Calendar.MONTH, monthOfYear);
                            cl.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            mOwnerBirthday = cl.getTime();
                            isOwnerNeedUpdate = true;
                        }


                    }, mBirthdayDefDate.get(Calendar.YEAR), mBirthdayDefDate.get(Calendar.MONTH), mBirthdayDefDate.get(Calendar.DAY_OF_MONTH)).show();
                }
            });

            mSignatureTV = (ClearEditText) findViewById(R.id.contact_user_detail_signature_et);
            mSignatureTV.addTextChangedListener(tw);
            findViewById(R.id.contact_user_detail_nick_name_et_linearlayout).setVisibility(View.GONE);
            findViewById(R.id.contact_user_detail_nick_name_et_belowline).setVisibility(View.GONE);
        } else {
            //其它人的头部信息
            mHeadIconIV = (ImageView) findViewById(R.id.ws_common_avatar);
            mNameTitleIV = (TextView) findViewById(R.id.ws_common_contact_conversation_topContent);
            mSignTV = (MarqueeTextView) findViewById(R.id.ws_common_contact_conversation_belowContent);
            //备注
            mSignTVLayout = (LinearLayout) findViewById(R.id.contact_user_detail_nick_name_et_linearlayout);
            mSignTVLine = findViewById(R.id.contact_user_detail_nick_name_et_belowline);
            mGendarTV = (ClearEditText) findViewById(R.id.contact_user_detail_gender_tv);
            mGendarTV.setEnabled(false);
            mBirthdayTV = (TextView) findViewById(R.id.contact_user_detail_birthday_tv);
            mBirthdayTV.setEnabled(false);
            mNickNameET = (ClearEditText) findViewById(R.id.contact_user_detail_nick_name_et);
            mNickNameET.addTextChangedListener(tw);
            findViewById(R.id.contact_user_detail_signature_et_linearlayout).setVisibility(View.GONE);
            findViewById(R.id.contact_user_detail_signature_et_belowline).setVisibility(View.GONE);
        }

        //对于自己不可更改的项
        mAccountTV = (ClearEditText) findViewById(R.id.contact_user_detail_account_tv);
        mAccountTV.setEnabled(false);
        mAccountTV.setTag(ClearEditText.TAG_IS_PATTERN);
        mCompanyTV = (ClearEditText) findViewById(R.id.contact_user_detail_company_tv);
        mCompanyTV.setEnabled(false);
        mDeptTV = (ClearEditText) findViewById(R.id.contact_user_detail_department_tv);
        mDeptTV.setEnabled(false);
        mPostJobTV = (ClearEditText) findViewById(R.id.contact_user_detail_title_tv);
        mPostJobTV.setEnabled(false);

        mCellphoneTV = (ClearEditText) findViewById(R.id.contact_user_detail_cell_phone_tv);
        mCellphoneTV.setInputType(InputType.TYPE_CLASS_NUMBER);
        mFaxphoneTV = (ClearEditText) findViewById(R.id.contact_user_detail_fax_tv);
        mFaxphoneTV.setInputType(InputType.TYPE_CLASS_NUMBER);
        mTelephoneTV = (ClearEditText) findViewById(R.id.contact_user_detail_telephone_tv);
        mTelephoneTV.setInputType(InputType.TYPE_CLASS_NUMBER);
        mAddressTV = (ClearEditText) findViewById(R.id.contact_user_detail_address_tv);
        mPersonInfoEdits = new EditText[]{mCellphoneTV, mFaxphoneTV, mTelephoneTV, mAddressTV};

        mCompanyLayout = findViewById(R.id.contact_user_detail_company_tv_layout);
        mDeptLayout = findViewById(R.id.contact_user_detail_department_tv_layout);
        mPostJobLayout = findViewById(R.id.contact_user_detail_title_tv_layout);
        mCompanyLayoutDevider = findViewById(R.id.contact_user_detail_company_tv_layout_devider);
        mDeptLayoutDevider = findViewById(R.id.contact_user_detail_department_tv_layout_devider);
        mPostJobLayoutDevider = findViewById(R.id.contact_user_detail_title_tv_layout_devider);
    }

    @Override
    public void receiveNewAvatar(User targetUser, Bitmap bnewAvatarm) {
        if (targetUser.getmUserId() == mShowInfoUser.getmUserId()) {
            mHeadIconIV.setImageBitmap(bnewAvatarm);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case START_AUTHENTICATION_ACTIVITY:
                if (data != null) {
                    boolean isReturnAuth = data.getBooleanExtra("isReturnAuth", false);
                    if (isReturnAuth) {
                        Intent i = new Intent(mContext, MessageAuthenticationActivity.class);
                        i.putExtra("remoteUserID", mUid);
                        setResult(5, i);
                        finish();
                    }
                }
                break;
            case ACTIVITY_PICK_BELONG_GROUP:
                if (resultCode == SelectJionGroupActivity.SELECT_GROUP_RESPONSE_CODE_DONE) {
                    if (data != null) {
                        String selectGroupName = data.getStringExtra("groupName");
                        long selectGroupID = data.getLongExtra("groupID", 0);
                        belongs = GlobalHolder.getInstance().getGroupById(selectGroupID);
                        mGroupNameTV.setText(selectGroupName);

                        // 请求刷新联系人界面
                        Intent intent = new Intent(PublicIntent.BROADCAST_REQUEST_UPDATE_CONTACTS_GROUP);
                        mContext.sendBroadcast(intent);
                    }
                }
                break;
            case ACTIVITY_PICK_CAMERA:
                File file = new File(GlobalConfig.getGlobalPicsPath() + "/avatar.png");
                if (file.exists()) {
                    startPhotoZoom(file.getAbsolutePath(), true);
                }
                break;
            case ACTIVITY_PICK_PHOTO:
                if (data != null) {
                    String filePath = data.getStringExtra("checkedImage");
                    if (filePath == null) {
                        V2Toast.makeText(mContext, R.string.error_contact_messag_invalid_image_path, V2Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    startPhotoZoom(filePath, false);
                }
                break;
            case ACTIVITY_PICK_AVATAR_SIZE:
                if (data != null) {
                    boolean isCamera = data.getBooleanExtra("fromPlace", false);
                    if (!isCamera) {
                        Bitmap photo = data.getParcelableExtra("data");
                        if (photo != null) {
                            if (!GlobalHolder.getInstance().checkServerConnected(mContext)) {
                                byte[] bitmap2Bytes = BitmapUtil.Bitmap2Bytes(photo);
                                V2ImRequest.invokeNative(V2ImRequest.NATIVE_CHANGE_OWNER_AVATAR, bitmap2Bytes,
                                        bitmap2Bytes.length, ".png");
                            }
                        } else {
                            Intent intent = new Intent(mContext, ConversationSelectImageActivity.class);
                            startActivityForResult(intent, ACTIVITY_PICK_PHOTO);
                        }
                    } else {
                        Bitmap photo = data.getParcelableExtra("data");
                        if (photo != null) {
                            byte[] bitmap2Bytes = BitmapUtil.Bitmap2Bytes(photo);
                            V2ImRequest.invokeNative(V2ImRequest.NATIVE_CHANGE_OWNER_AVATAR, bitmap2Bytes,
                                    bitmap2Bytes.length, ".png");
                        }

                        File temp = new File(GlobalConfig.getGlobalPicsPath() + "/avatar.png");
                        if (temp.exists()) {
                            temp.delete();
                        }
                    }
                }
                break;
        }
    }

    private void showUserInfo() {
        if (mShowInfoUser.getAvatarBitmap() != null) {
            mHeadIconIV.setImageBitmap(mShowInfoUser.getAvatarBitmap());
        }

        mNameTitleIV.setText(mShowInfoUser.getNickName());
        if (mShowInfoUser.getmUserId() == mLoginUserID) {
            //性别选择
            String genderVal = mShowInfoUser.getSex();
            for (int i = 0; i < mGenderRG.getChildCount(); i++) {
                RadioButton rg = (RadioButton) mGenderRG.getChildAt(i);
                if (rg.getTag().equals(genderVal)) {
                    rg.setChecked(true);
                    isOwnerSexDefault = true;
                }
            }

            if (!isOwnerSexDefault) {
                RadioButton radioButton = (RadioButton) findViewById(R.id.radio2);
                radioButton.setChecked(true);
            }

            mOwnerBirthday = mShowInfoUser.getBirthday();
            if (!TextUtils.isEmpty(mShowInfoUser.getSignature())) {
                mSignatureTV.setText(mShowInfoUser.getSignature());
            }
        } else {
            if (!TextUtils.isEmpty(mShowInfoUser.getSignature())) {
                mSignTV.setText(mShowInfoUser.getSignature());
            }

            if (!TextUtils.isEmpty(currentEditNickName))
                mNickNameET.setText(currentEditNickName);

            if (mShowInfoUser.getSex() != null) {
                if (mShowInfoUser.getSex().equals("0")) {
                    mGendarTV.setText(mContext.getText(R.string.contacts_user_detail_gender_priacy));
                } else if (mShowInfoUser.getSex().equals("1")) {
                    mGendarTV.setText(mContext.getText(R.string.contacts_user_detail_gender_male));
                } else if (mShowInfoUser.getSex().equals("2")) {
                    mGendarTV.setText(mContext.getText(R.string.contacts_user_detail_gender_female));
                }

            } else {
                mGendarTV.setText(mContext.getText(R.string.contacts_user_detail_gender_priacy));
            }
        }

        // 如果帐号是手机号,则隐藏中间四位
        String account = mShowInfoUser.getAccount();
        if (MessageUtil.isMobileNO(account)) {
            mAccountTV.setText(account.substring(0, 3) + "****" + account.substring(7, account.length()));
        } else {
            mAccountTV.setText(account);
        }

        if (!TextUtils.isEmpty(mShowInfoUser.getBirthdayStr())) {
            mBirthdayTV.setText(mShowInfoUser.getBirthdayStr());
        }

        if (!TextUtils.isEmpty(mShowInfoUser.getMobile())) {
            mCellphoneTV.setText(mShowInfoUser.getMobile());
        }

        if (!TextUtils.isEmpty(mShowInfoUser.getFax())) {
            mFaxphoneTV.setText(mShowInfoUser.getFax());
        }

        if (!TextUtils.isEmpty(mShowInfoUser.getTelephone())) {
            mTelephoneTV.setText(mShowInfoUser.getTelephone());
        }

        if (!TextUtils.isEmpty(mShowInfoUser.getJob())) {
            mPostJobTV.setText(mShowInfoUser.getJob());
        }

        if (!TextUtils.isEmpty(mShowInfoUser.getAddress())) {
            mAddressTV.setText(mShowInfoUser.getAddress());
        }

        if (!TextUtils.isEmpty(mShowInfoUser.getDepartment())) {
            mDeptTV.setText(mShowInfoUser.getDepartment());
        }

        if (!TextUtils.isEmpty(mShowInfoUser.getCompany())) {
            mCompanyTV.setText(mShowInfoUser.getCompany());
        }
    }

    private void updateContactGroup() {
        mUpdateContactGroupButton.setVisibility(View.GONE);
        mCompanyLayout.setVisibility(View.VISIBLE);
        mDeptLayout.setVisibility(View.VISIBLE);
        mPostJobLayout.setVisibility(View.VISIBLE);
        mCompanyLayoutDevider.setVisibility(View.VISIBLE);
        mDeptLayoutDevider.setVisibility(View.VISIBLE);
        mPostJobLayoutDevider.setVisibility(View.VISIBLE);
        changeEditTextEnable(mPersonInfoEdits, false);

        //是否是从好友验证界面跳转过来的
        if (currentContactType == AUTH_MESSAGE_TYPE) {
            handlerFriendAuth();
            boolean showContact = this.getIntent().getBooleanExtra("contactButtonShow", true);
            if (showContact)
                mStartConfMeetingButton.setVisibility(View.VISIBLE);
            else
                mStartConfMeetingButton.setVisibility(View.GONE);
            if (isRelation)
                currentEditNickName = mShowInfoUser.getCommentName();
        } else {
            if (isOutOrgUser || mUid == mLoginUserID) {
                mStartConfMeetingButton.setVisibility(View.GONE);
            } else {
                mStartConfMeetingButton.setVisibility(View.VISIBLE);
            }

            if (mShowInfoUser.getmUserId() == mLoginUserID) {
                changeEditTextEnable(mPersonInfoEdits, true);
            } else {
                if (isRelation) {
                    currentEditNickName = mShowInfoUser.getCommentName();
                    mGroupNameTV = (TextView) findViewById(R.id.detail_detail_2_group_name);
                    mGroupNameTV.setText(belongs.getName());
                    mAddContactButton.setVisibility(View.GONE);
                    mDeleteContactButton.setVisibility(View.VISIBLE);
                    mSignTVLayout.setVisibility(View.VISIBLE);
                    mSignTVLine.setVisibility(View.VISIBLE);
                } else {
                    mAddContactButton.setVisibility(View.VISIBLE);
                    mDeleteContactButton.setVisibility(View.GONE);
                    mSignTVLayout.setVisibility(View.GONE);
                    mSignTVLine.setVisibility(View.GONE);
                }
            }
        }
    }

    private void changeEditTextEnable(EditText[] arrs, boolean isEnable) {
        for (int i = 0; i < arrs.length; i++) {
            arrs[i].setEnabled(isEnable);
            if (isEnable) {
                arrs[i].addTextChangedListener(tw);
            }
        }
    }

    private String getValueOfSexByRadioValue() {
        for (int i = 0; i < mGenderRG.getChildCount(); i++) {
            RadioButton rg = (RadioButton) mGenderRG.getChildAt(i);
            if (rg.isChecked()) {
                return rg.getTag().toString();
            }
        }
        return "1";
    }

    private void handlerFriendAuth() {
        // 附加消息布局
        llAuthenticationMessageLayout.setVisibility(View.VISIBLE);
        TextView tvAuthenticationMessage = (TextView) findViewById(R.id.authentication_message);

        Button bAccess = (Button) findViewById(R.id.access);
        Button bRefuse = (Button) findViewById(R.id.refuse);
        tvAuthenticationState = (TextView) findViewById(R.id.authentication_state);

        String authenticationMessage = getIntent().getStringExtra("authenticationMessage");
        if (TextUtils.isEmpty(authenticationMessage))
            tvAuthenticationMessage.setText(R.string.common_no_string);
        else
            tvAuthenticationMessage.setText(authenticationMessage);

        if (TextUtils.isEmpty(authenticationMessage))
            tvAuthenticationMessage.setText(R.string.common_no_string);
        else
            tvAuthenticationMessage.setText(authenticationMessage);

        int state = this.getIntent().getIntExtra("state", -1);
        switch (state) {
            // 别人加我：允许任何人：0 已添加您为好友，需要验证：1未处理，2已同意，3已拒绝
            // 我加别人：允许认识人：4 你们已成为了好友，需要验证：5等待对方验证，4被同意（你们已成为了好友），6拒绝了你为好友
            case 0:// 0成为好友
            case 4:
                llAuthenticationMessageLayout.setVisibility(View.GONE);
                mTitleContent.setText(R.string.contactDetail_titlebar_title_text);
                mAddContactButton.setVisibility(View.GONE);
                mDeleteContactButton.setVisibility(View.VISIBLE);
                break;
            case 1:// 未处理
                bAccess.setVisibility(View.VISIBLE);
                bRefuse.setVisibility(View.VISIBLE);
                tvAuthenticationState.setVisibility(View.GONE);
                mTitleContent.setText(R.string.contactDetail_titlebar_title_text1);
                mSignTVLayout.setVisibility(View.GONE);
                break;
            case 2:// 已同意
                tvAuthenticationState.setVisibility(View.VISIBLE);
                tvAuthenticationState.setText(R.string.contactDetail_AuthenticationState_allowed_apply);
                mTitleContent.setText(R.string.contactDetail_titlebar_title_text1);
                llAuthenticationMessageLayout.setVisibility(View.VISIBLE);
                break;
            case 3:// 3已拒绝
                tvAuthenticationState.setVisibility(View.VISIBLE);
                tvAuthenticationState.setText(R.string.contactDetail_AuthenticationState_refused_apply);
                mTitleContent.setText(R.string.contactDetail_titlebar_title_text1);
                llAuthenticationMessageLayout.setVisibility(View.VISIBLE);
                mSignTVLayout.setVisibility(View.GONE);
                break;
            case 5:
                mAddContactButton.setVisibility(View.VISIBLE);
                mDeleteContactButton.setVisibility(View.GONE);
                llAuthenticationMessageLayout.setVisibility(View.GONE);
                break;
            case 6:// 6被拒绝
                mSignTVLayout.setVisibility(View.GONE);
                tvAuthenticationState.setVisibility(View.VISIBLE);
                tvAuthenticationState.setText(R.string.contactDetail_AuthenticationState_refused_your_apply);
                mTitleContent.setText(R.string.contactDetail_titlebar_title_text2);
                break;
        }

        bAccess.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(mContext, InputRemarkActivity.class);
                intent.putExtra("remoteUserID", mUid);
                intent.putExtra("cause", "access_friend_authentication");
                startActivity(intent);
                finish();
            }
        });

        bRefuse.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (GlobalHolder.getInstance().checkServerConnected(mContext)) {
                    return;
                }

                Intent intent = new Intent(mContext, InputAuthenticationActivity.class);
                intent.putExtra("remoteUserID", mUid);
                intent.putExtra("cause", "refuse_friend_authentication");
                startActivityForResult(intent, START_AUTHENTICATION_ACTIVITY);
            }
        });
    }

    /**
     * 裁剪图片方法实现
     *
     * @param filePath
     * @param isFromCamera
     */
    public void startPhotoZoom(String filePath, boolean isFromCamera) {
        // 系统的截取界面
        // int avatarSize = BitmapUtil.getAvatarSize();
        // Intent intent = new Intent("com.android.camera.action.CROP");
        // intent.setDataAndType(uri, "image/*");
        // // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        // intent.putExtra("crop", "true");
        // // aspectX aspectY 是宽高的比例
        // intent.putExtra("aspectX", 1);
        // intent.putExtra("aspectY", 1);
        // // outputX outputY 是裁剪图片宽高
        // intent.putExtra("outputX", avatarSize);
        // intent.putExtra("outputY", avatarSize);
        // intent.putExtra("scale", true);// 黑边
        // intent.putExtra("scaleUpIfNeeded", true);// 黑边
        // intent.putExtra("noFaceDetection", true);
        // intent.putExtra("outputFormat",
        // Bitmap.CompressFormat.PNG.toString());
        // intent.putExtra("return-data", true);
        // startActivityForResult(intent, ACTIVITY_PICK_AVATAR_SIZE);
        Intent intent = new Intent(mContext, ClipAvatarActivity.class);
        if (TextUtils.isEmpty(filePath))
            return;
        intent.putExtra(EXTRA_KEY_IMAGE_PATH, filePath);
        intent.putExtra("fromPlace", isFromCamera);
        startActivityForResult(intent, ACTIVITY_PICK_AVATAR_SIZE);
    }

    private void showDeleteContactDialog() {
        deleteContactDialog = DialogManager.getInstance()
                .showNoTitleDialog(DialogManager.getInstance().new DialogInterface(mContext, null,
                        mContext.getText(R.string.contacts_delete_confirm),
                        mContext.getText(R.string.conversation_quit_dialog_confirm_text),
                        mContext.getText(R.string.conversation_quit_dialog_cancel_text)) {

                    @Override
                    public void confirmCallBack() {
                        // 删除好友
                        deleteContactDialog.dismiss();
                        mContactRequestService.delContact(mShowInfoUser, new HandlerWrap(mHandler, DELETE_CONTACT_USER, null));
                        WaitDialogBuilder.showNormalWithHintProgress(mContext);
                    }

                    @Override
                    public void cannelCallBack() {
                        deleteContactDialog.dismiss();
                    }
                });

        deleteContactDialog.show();
    }

    private TextWatcher tw = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable ed) {
            if (mShowInfoUser.getmUserId() == mLoginUserID) {
                isOwnerNeedUpdate = true;
            } else {
                if (!ed.toString().equals(mShowInfoUser.getDisplayName())) {
                    currentEditNickName = ed.toString();
                    isNeedUpdate = true;
                }
            }
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

        }

    };

    private class ContactDetailMainLayoutTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return false;
        }
    }

    ;

    private class ReturnButtonListener implements OnClickListener {

        @Override
        public void onClick(View view) {
            onBackPressed();
        }
    }

    ;

    private class UpdateContactGroupButtonListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            Intent i = new Intent();
            i.setClass(mContext, SelectJionGroupActivity.class);
            i.putExtra("uid", mShowInfoUser.getmUserId());
            i.putExtra("gid", belongs.getGroupID());
            startActivityForResult(i, ACTIVITY_PICK_BELONG_GROUP);
        }
    }

    ;

    private class StartConfMeeingClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            Intent i = new Intent(PublicIntent.START_CONFERENCE_CREATE_ACTIVITY);
            i.addCategory(PublicIntent.DEFAULT_CATEGORY);
            i.putExtra("uid", mShowInfoUser.getmUserId());
            startActivityForResult(i, 0);
        }
    }

    ;

    private class AddContactButtonClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (GlobalHolder.getInstance().checkServerConnected(mContext)) {
                return;
            }

            // 加为好友
            Intent i = new Intent();
            switch (mShowInfoUser.getAuthtype()) {
                case 0:
                    i.setClass(mContext, InputRemarkActivity.class);
                    i.putExtra("uid", mUid);
                    i.putExtra("cause", "ContactDetail2");
                    mContext.startActivity(i);
                    break;
                case 1:
                    i.setClass(mContext, InputAuthenticationActivity.class);
                    i.putExtra("uid", mUid);
                    mContext.startActivity(i);
                    break;
                case 2:
                    Toast.makeText(mContext, R.string.contacts_detail2_refused_add, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(mContext, R.string.contacts_detail2_refused_add, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    ;

    private class DeleteContactButtonClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (GlobalHolder.getInstance().checkServerConnected(mContext)) {
                return;
            }

            showDeleteContactDialog();
        }
    }

    ;

    private class ChangeOwnerAvatarClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (mShowInfoUser.getmUserId() != GlobalHolder.getInstance().getCurrentUserId()) {
                return;
            }

            if (avatarDialog != null) {
                avatarDialog.show();
                return;
            }
            avatarDialog = new Dialog(mContext, R.style.ContactUserDetailVoiceCallDialog);
            avatarDialog.setCancelable(true);
            avatarDialog.setCanceledOnTouchOutside(true);
            avatarDialog.setContentView(R.layout.contacts_user_detail_avatar);
            TextView cameraTV = (TextView) avatarDialog.findViewById(R.id.contacts_user_detail_avatar_dialog_1);
            cameraTV.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    avatarDialog.dismiss();
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(new File(GlobalConfig.getGlobalPicsPath(), "avatar.png")));
                    startActivityForResult(intent, ACTIVITY_PICK_CAMERA);
                }
            });
            TextView photosTV = (TextView) avatarDialog.findViewById(R.id.contacts_user_detail_avatar_dialog_2);
            photosTV.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    avatarDialog.dismiss();
                    Intent intent = new Intent(mContext, ConversationSelectImageActivity.class);
                    startActivityForResult(intent, ACTIVITY_PICK_PHOTO);
                }
            });
            avatarDialog.show();
        }
    }

    ;

    class MyDatePickerDialog extends DatePickerDialog {

        private Calendar mNowDate;

        public MyDatePickerDialog(Context context, OnDateSetListener callBack,
                                  int year, int monthOfYear, int dayOfMonth) {
            super(context, callBack, year, monthOfYear, dayOfMonth);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            isShowDate = true;
            mNowDate = Calendar.getInstance();
            super.onCreate(savedInstanceState);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        @Override
        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            if (year > mNowDate.get(Calendar.YEAR)) {
                view.updateDate(mBirthdayDefDate
                        .get(Calendar.YEAR), mBirthdayDefDate
                        .get(Calendar.MONTH), mBirthdayDefDate.get(Calendar.DAY_OF_MONTH));
            }

            if (monthOfYear > mNowDate.get(Calendar.MONTH) && year == mNowDate.get(Calendar.YEAR))
                view.updateDate(mBirthdayDefDate
                        .get(Calendar.YEAR), mBirthdayDefDate
                        .get(Calendar.MONTH), mBirthdayDefDate.get(Calendar.DAY_OF_MONTH));
            if (dayOfMonth > mNowDate.get(Calendar.DAY_OF_MONTH) && year == mNowDate.get(Calendar.YEAR) &&
                    monthOfYear == mNowDate.get(Calendar.MONTH))
                view.updateDate(mBirthdayDefDate
                        .get(Calendar.YEAR), mBirthdayDefDate
                        .get(Calendar.MONTH), mBirthdayDefDate.get(Calendar.DAY_OF_MONTH));
        }


        @Override
        protected void onStop() {
            isShowDate = false;
            super.onStop();
        }
    }
}
