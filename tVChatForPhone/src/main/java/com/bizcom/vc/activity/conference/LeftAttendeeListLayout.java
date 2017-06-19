package com.bizcom.vc.activity.conference;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bizcom.util.AlgorithmUtil;
import com.bizcom.util.MessageUtil;
import com.bizcom.util.SearchUtils;
import com.bizcom.util.V2Log;
import com.bizcom.vc.widget.cus.edittext.ClearEditText;
import com.bizcom.vo.Attendee;
import com.bizcom.vo.AttendeeMixedDevice;
import com.bizcom.vo.Conference;
import com.bizcom.vo.User;
import com.bizcom.vo.User.DeviceType;
import com.bizcom.vo.UserDeviceConfig;
import com.config.GlobalHolder;
import com.shdx.tvchat.phone.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class LeftAttendeeListLayout extends LinearLayout {

    private static final String TAG = LeftAttendeeListLayout.class.getSimpleName();

    private static final int FIRST_DEVICE_FLAG = -1;

    private Conference conf;

    private View rootView;

    private VideoAttendeeActionListener listener;

    private List<Wrapper> mList;
    private List<Wrapper> mSearchList;

    private boolean mIsStartedSearch;
    private boolean isFrist = true;
    public boolean mIsOwnerVideoOpen = true;

    private ClearEditText mSearchET;
    private Drawable mIconSearchClear;
    private View mPinButton;
    private TextView attendPersons;
    private OnClickListener mPinButtonOnClickListener = new PinButtonOnClickListener();
    private AttendeeContainerOnItemClickListener mAttendeeContainerOnItemClickListener = new AttendeeContainerOnItemClickListener();
    private TextWatcher mSearchETTextChangedListener = new SearchETTextChangedListener();

    private AttendeeContainerAdapter adapter = new AttendeeContainerAdapter();

    /**
     * Online attendee count without mixed device
     */
    private int onLinePersons = 0;

    /**
     * All attendee count without mixed device
     */
    private int mAttendeeCount = 0;
    private SearchUtils mSearchUtils;

    public interface VideoAttendeeActionListener {

        void OnAttendeeDragged(Attendee at, UserDeviceConfig udc, int x, int y);

        void OnAttendeeClicked(Attendee at, UserDeviceConfig udc);

        void requestAttendeeViewFixedLayout(View v);

        void requestAttendeeViewFloatLayout(View v);

        boolean checkSurfaceViewMax();
    }

    public LeftAttendeeListLayout(Conference conf, Context context) {
        super(context);
        this.conf = conf;
        mList = new ArrayList<>();
        initLayout();
    }

    private void initLayout() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.video_attendee_list_layout, null, false);

        attendPersons = (TextView) view.findViewById(R.id.video_attendee_pin_persons);
        ListView mAttendeeContainer = (ListView) view.findViewById(R.id.video_attendee_container);
        mSearchET = (ClearEditText) view.findViewById(R.id.ws_common_create_search);

        mPinButton = view.findViewById(R.id.video_attendee_pin_button);
        mPinButton.setOnClickListener(mPinButtonOnClickListener);

        mAttendeeContainer.setAdapter(adapter);
        mAttendeeContainer.setTextFilterEnabled(true);
        mAttendeeContainer.setOnItemClickListener(mAttendeeContainerOnItemClickListener);

        mAttendeeContainer.setFocusable(true);
        mAttendeeContainer.setFocusableInTouchMode(true);
        final Resources res = getResources();
        mIconSearchClear = res.getDrawable(R.drawable.txt_search_clear);
        mSearchET.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        mSearchET.addTextChangedListener(tbxSearch_TextChanged);
        mSearchET.setOnTouchListener(txtSearch_OnTouch);
        this.addView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        mSearchET.addTextChangedListener(mSearchETTextChangedListener);
        mSearchUtils = new SearchUtils();
        rootView = this;
    }

    private OnTouchListener txtSearch_OnTouch = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    int curX = (int) event.getX();
                    if (curX > v.getWidth() - 38 && !TextUtils.isEmpty(mSearchET.getText())) {
                        mSearchET.setText("");
                        int cacheInputType = mSearchET.getInputType();// backup
                        mSearchET.setInputType(InputType.TYPE_NULL);// disable
                        mSearchET.onTouchEvent(event);// call native handler
                        mSearchET.setInputType(cacheInputType);// restore input
                        return true;// consume touch even
                    }
                    break;
            }
            return false;
        }
    };
    private TextWatcher tbxSearch_TextChanged = new TextWatcher() {
        private boolean isnull = true;

        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s)) {
                if (!isnull) {
                    mSearchET.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    isnull = true;
                }
            } else {
                if (isnull) {
                    mSearchET.setCompoundDrawablesWithIntrinsicBounds(null, null, mIconSearchClear, null);
                    isnull = false;
                }
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    };

    public void setListener(VideoAttendeeActionListener listener) {
        this.listener = listener;
    }

    /**
     * Add new attendee to list
     *
     * @param at
     */
    public void addNewAttendee(Attendee at) {
        if (at == null) {
            return;
        }
        List<Attendee> list = new ArrayList<>(1);
        list.add(at);
        addAttendeeWithoutNotification(list);
        Collections.sort(mList);
        adapter.notifyDataSetChanged();
    }

    /**
     * Add new attendee list to current list
     *
     * @param atList
     */
    public void addNewAttendee(List<Attendee> atList) {
        if (atList == null) {
            return;
        }
        addAttendeeWithoutNotification(atList);
        Collections.sort(mList);
        adapter.notifyDataSetChanged();
    }

    public void hideKeyBoard() {
        MessageUtil.hideKeyBoard(getContext(), mSearchET.getWindowToken());
    }

    private void addAttendeeWithoutNotification(List<Attendee> atList) {
        for (int index = 0; index < atList.size(); index++) {
            Attendee at = atList.get(index);
            if (at.getType() != Attendee.TYPE_MIXED_VIDEO) {
                configAttendee(at);
            }

            List<UserDeviceConfig> dList = GlobalHolder.getInstance().getAttendeeDevice(at.getAttId());
            int i = 0;
            int deviceIndex = 1;
            do {
                if (dList == null || dList.size() == 0) {
                    mList.add(new Wrapper(at, null, FIRST_DEVICE_FLAG));
                } else {
                    mList.add(new Wrapper(at, dList.get(i), i == 0 ? FIRST_DEVICE_FLAG : deviceIndex++));
                }
                i++;
            } while (dList != null && i < dList.size());
        }
    }

    private View buildAttendeeView(Wrapper wr) {

        Context ctx = this.getContext();
        View view = LayoutInflater.from(ctx).inflate(R.layout.video_attendee_device_layout, null, false);
        updateView(wr, view);
        return view;

    }

    private void updateView(Wrapper wr, View view) {
        if (wr == null || view == null) {
            return;
        }

        view.setTag(wr);
        TextView nameTV = (TextView) view.findViewById(R.id.video_attendee_device_name);
        ImageView lectureStateIV = (ImageView) view.findViewById(R.id.video_attendee_device_lectrue_state_icon);
        ImageView spIV = (ImageView) view.findViewById(R.id.video_attendee_device_speaker_icon);
        ImageView cameraIV = (ImageView) view.findViewById(R.id.video_attendee_device_camera_icon);

        if (wr.sortFlag == FIRST_DEVICE_FLAG) {
            if (wr.a != null) {
                if (wr.a.getType() == Attendee.TYPE_MIXED_VIDEO) {
                    AttendeeMixedDevice mix = (AttendeeMixedDevice) wr.a;
                    V2Log.i(TAG, "混合视频  id = " + wr.a.getAttId());
                    nameTV.setText(
                            getResources().getString(R.string.vo_attendee_mixed_device_mix_video) + mix.getMixSize());
                } else if (wr.a.getType() == Attendee.TYPE_ATTENDEE) {
                    User aUser = wr.a.getUser();
                    if (aUser != null) {
                        if (aUser.isRapidInitiation()) {
                            nameTV.setText("<" + aUser.getDisplayName() + ">");
                            V2Log.i(TAG, "快速入会 name = " + aUser.getDisplayName());
                        } else {
                            nameTV.setText(aUser.getDisplayName());
                            if (wr.udc != null) {
                                wr.udc.setUserFirstDev(true);
                            }
                            V2Log.i(TAG, "普通用户 name = " + aUser.getDisplayName());
                        }
                    } else {
                        V2Log.i(TAG, "不是参与者 , id : " + wr.a.getAttId());
                        nameTV.setText("");
                    }
                }

            }
        } else {
            UserDeviceConfig udc = wr.getUserDeviceConfig();
            if (udc != null) {
                String deviceID = udc.getDeviceID();
                String deciceName = null;
                if (deviceID != null) {
                    int start = deviceID.indexOf(':');
                    int end = deviceID.indexOf('_');
                    deciceName = deviceID.substring(start + 1, end);
                }
                nameTV.setText("     " + deciceName);
            } else {
                nameTV.setText("     ");
            }
        }

        if (wr.udc != null) {
            if (!wr.udc.isShowing()) {
                view.setBackgroundColor(Color.WHITE);
            } else {
                view.setBackgroundColor(getContext().getResources().getColor(R.color.attendee_select_bg));
            }
        } else {
            if(wr.a.isSelf()){
                if (mIsOwnerVideoOpen){
                    view.setBackgroundColor(getContext().getResources().getColor(R.color.attendee_select_bg));
                } else {
                    view.setBackgroundColor(Color.WHITE);
                }
            } else{
                view.setBackgroundColor(Color.WHITE);
            }
        }

        // Set text color and camera icon
        setStyle(wr, nameTV, lectureStateIV, spIV, cameraIV);
    }

    /**
     * @param wr
     * @param name
     * @param cameraIV   camera imave view
     * @param speakingIV speaker image view
     */
    private void setStyle(Wrapper wr, TextView name, ImageView lectureStateIV, ImageView speakingIV,
                          ImageView cameraIV) {
        Attendee at = wr.a;

        // 有可能是混合视频
        DeviceType atDeviceType = null;
        if (at.getType() == Attendee.TYPE_ATTENDEE) {
            User user = GlobalHolder.getInstance().getUser(at.getAttId());
            if (user != null) {
                atDeviceType = user.getDeviceType();
            }
        }

        UserDeviceConfig udc = wr.udc;
        // 如果是自己
        if (at.isSelf()) {
            // 字体粗
            name.setTypeface(null, Typeface.BOLD);
            // 如果是主席
            if (at.isChairMan() || conf.getChairman() == at.getAttId()) {
                name.setTextColor(getContext().getResources().getColor(R.color.video_attendee_chair_man_name_color));
            } else {
                name.setTextColor(getContext().getResources().getColor(R.color.video_attendee_name_color));
            }
        } else {
            // 字体正常
            name.setTypeface(null, Typeface.NORMAL);
            // 是否在线
            if (at.isJoined()) {
                // 是否是主席，更改名字颜色
                if (at.isChairMan() || conf.getChairman() == at.getAttId()) {
                    name.setTextColor(
                            getContext().getResources().getColor(R.color.video_attendee_chair_man_name_color));
                } else {
                    name.setTextColor(getContext().getResources().getColor(R.color.video_attendee_name_color));
                }
            } else {
                name.setTextColor(getContext().getResources().getColor(R.color.video_attendee_name_color_offline));
            }
        }

        // 名字后面携带的图标
        if (at.getType() != Attendee.TYPE_MIXED_VIDEO) {
            if (at.isSelf()) {
                cameraIV.setVisibility(View.VISIBLE);
                cameraIV.setImageResource(R.drawable.phone_camera);
            } else {
                if (udc != null) {
                    // set camera icon
                    if (at.isJoined()) {
                        cameraIV.setVisibility(View.VISIBLE);
                        if (udc.isEnable()) {
                            if (atDeviceType != null && atDeviceType == DeviceType.CELL_PHONE) {
                                cameraIV.setImageResource(R.drawable.phone_camera);
                            } else {
                                cameraIV.setImageResource(R.drawable.camera);
                            }
                        } else {
                            if (atDeviceType != null && atDeviceType == DeviceType.CELL_PHONE) {
                                cameraIV.setImageResource(R.drawable.phone_camera_pressed);
                            } else {
                                cameraIV.setImageResource(R.drawable.camera_pressed);
                            }
                        }
                    } else {
                        cameraIV.setVisibility(View.GONE);
                        // 离线用户设备图标隐藏
                        // if (atDeviceType != null
                        // && atDeviceType == DeviceType.CELL_PHONE) {
                        // cameraIV.setImageResource(R.drawable.phone_camera_pressed);
                        // } else {
                        // cameraIV.setImageResource(R.drawable.camera_pressed);
                        // }
                    }
                } else {
                    if (at.isJoined()) {
                        cameraIV.setVisibility(View.VISIBLE);
                        if (atDeviceType != null && atDeviceType == DeviceType.CELL_PHONE) {
                            cameraIV.setImageResource(R.drawable.phone_camera_pressed);
                        } else {
                            cameraIV.setImageResource(R.drawable.camera_pressed);
                        }
                    } else {
                        cameraIV.setVisibility(View.GONE);
                    }
                }
            }
        } else {
            cameraIV.setImageResource(R.drawable.mixed_video_camera);
        }

        // If attaendee is mixed video or is not default flag, then hide speaker
        if (at.getType() == Attendee.TYPE_MIXED_VIDEO || wr.sortFlag != FIRST_DEVICE_FLAG) {
            lectureStateIV.setVisibility(View.INVISIBLE);
            speakingIV.setVisibility(View.INVISIBLE);
        }

        if (wr.sortFlag == FIRST_DEVICE_FLAG) {
            // Update lecture state display
            switch (at.getLectureState()) {
                case Attendee.LECTURE_STATE_NOT:
                    lectureStateIV.setVisibility(View.INVISIBLE);
                    break;
                case Attendee.LECTURE_STATE_APPLYING:
                    lectureStateIV.setVisibility(View.VISIBLE);
                    lectureStateIV.setImageResource(R.drawable.lecture_state_applaying);
                    break;
                case Attendee.LECTURE_STATE_GRANTED:
                    lectureStateIV.setVisibility(View.VISIBLE);
                    lectureStateIV.setImageResource(R.drawable.lecture_state_granted);
                    break;
            }

            // Update speaking display
            if (!at.isSpeaking()) {
                speakingIV.setVisibility(View.INVISIBLE);
            } else {
                speakingIV.setVisibility(View.VISIBLE);
                speakingIV.setImageResource(R.drawable.conf_speaking);
                ((AnimationDrawable) speakingIV.getDrawable()).start();
            }
        } else {
            lectureStateIV.setVisibility(View.INVISIBLE);
            speakingIV.setVisibility(View.INVISIBLE);
        }

    }

    public void setAttendsList(List<Attendee> l) {
        // Copy list for void concurrency exception
        List<Attendee> list = new ArrayList<>(l);
        for (Attendee at : list) {
            List<UserDeviceConfig> dList = GlobalHolder.getInstance().getAttendeeDevice(at.getAttId());
            if (at.getType() == Attendee.TYPE_MIXED_VIDEO) {
                if (dList != null && dList.size() > 0) {
                    mList.add(new Wrapper(at, dList.get(0), FIRST_DEVICE_FLAG));
                }
            } else {
                int i = 0;
                int deviceIndex = 1;
                do {
                    if (dList == null || dList.size() <= 0) {
                        mList.add(new Wrapper(at, null, FIRST_DEVICE_FLAG));
                    } else {
                        UserDeviceConfig tempUdc = dList.get(i);
                        if (i == 0) {
                            mList.add(new Wrapper(at, tempUdc, FIRST_DEVICE_FLAG));
                        } else {
                            if (at.isJoined()) {
                                mList.add(new Wrapper(at, tempUdc, deviceIndex++));
                            }
                        }
                    }
                    i++;
                } while (dList != null && i < dList.size());

                mAttendeeCount++;
                if ((at.isJoined() || at.isSelf())) {
                    onLinePersons++;
                }
            }
        }


        Collections.sort(mList);
        updateStatist();
    }

    public void updateEnteredAttendee(Attendee at) {
        if (at == null) {
            return;
        }

        at.setJoined(true);
        List<UserDeviceConfig> dList = GlobalHolder.getInstance().getAttendeeDevice(at.getAttId());
        if (at.getType() == Attendee.TYPE_MIXED_VIDEO) {
            V2Log.e(TAG, "Successfully added a mix video");
            mList.add(new Wrapper(at, dList.get(0), FIRST_DEVICE_FLAG));
        } else {
            // for fast enter conference user
            int index = 0;
            boolean isFound = false;
            boolean isAdd = true;

            for (int i = 0; i < mList.size(); i++) {
                Attendee attendee = mList.get(i).a;
                if (attendee.getAttId() == at.getAttId()) {
                    isFound = true;
                    if (dList != null && dList.size() > 0) {
                        mList.get(i).udc = dList.get(0);
                        mList.get(i).sortFlag = FIRST_DEVICE_FLAG;
                        index = i;
                    }
                    break;
                }
            }

            if (dList != null) {
                for (int i = 1; i < dList.size(); i++) {
                    if (index + 1 == mList.size() - 1) {
                        mList.add(new Wrapper(at, dList.get(i), i));
                        isAdd = false;
                    } else {
                        mList.add(index + 1, new Wrapper(at, dList.get(i), i));
                        isAdd = false;
                    }
                    index++;
                }
            }

            if (!isFound) {
                mAttendeeCount++;
                if (isAdd) {
                    mList.add(new Wrapper(at, null, FIRST_DEVICE_FLAG));
                }
            }

            if ((at.isJoined() || at.isSelf())) {
                onLinePersons++;
            }
            updateStatist();

            // // boolean isNew = false;
            // int index = 0;
            // if (mList.size() > 0 && dList != null && dList.size() > 0) {
            //
            // for (int i = 0; i < mList.size(); i++) {
            // Wrapper wr = mList.get(i);
            // if (wr.a.getAttId() == at.getAttId()) {
            // index = i;
            // break;
            // }
            // }
            //
            // }
            //
            // int deviceIndex = 1;
            // for (int i = 0; i < dList.size(); i++) {
            // UserDeviceConfig udc = dList.get(i);
            // if (i == 0) {
            //
            //
            // mList.sendFriendToTv(index, new Wrapper(at, udc, FIRST_DEVICE_FLAG));
            // } else {
            // if (index + 1 == mList.size() - 1) {
            // mList.sendFriendToTv(new Wrapper(at, udc, deviceIndex++));
            // } else {
            // mList.sendFriendToTv(index + 1,
            // new Wrapper(at, udc, deviceIndex++));
            // }
            // index++;
            // // isNew = true;
            // }
            //
            // }

            // 设备显示不出问题时删除这段注释
            // if (dList != null) {
            // UserDeviceConfig defaultDevice = null;
            // for (int i = 0; i < dList.size(); i++) {
            // UserDeviceConfig udc = dList.get(i);
            // if (udc.isDefault()) {
            // defaultDevice = udc;
            // }
            // }
            //
            // int deviceIndex = 1;
            // if (defaultDevice != null) {
            // mList.sendFriendToTv(index, new Wrapper(at, defaultDevice,
            // FIRST_DEVICE_FLAG));
            // for (int i = 0; i < dList.size(); i++) {
            // UserDeviceConfig udc = dList.get(i);
            // if (!udc.isDefault()) {
            // if (index + 1 == mList.size() - 1) {
            // mList.sendFriendToTv(new Wrapper(at, udc, deviceIndex++));
            // } else {
            // mList.sendFriendToTv(index + 1, new Wrapper(at, udc,
            // deviceIndex++));
            // }
            // index++;
            // // isNew = true;
            // }
            // }
            // } else {
            // for (int i = 0; i < dList.size(); i++) {
            // UserDeviceConfig udc = dList.get(i);
            // if (i == 0) {
            // mList.sendFriendToTv(index, new Wrapper(at, udc,
            // FIRST_DEVICE_FLAG));
            // } else {
            // if (index + 1 == mList.size() - 1) {
            // mList.sendFriendToTv(new Wrapper(at, udc, deviceIndex++));
            // } else {
            // mList.sendFriendToTv(index + 1, new Wrapper(at, udc,
            // deviceIndex++));
            // }
            // index++;
            // // isNew = true;
            // }
            //
            // }
            //
            // }
            //
            // }

            // if (isNew)
            // mAttendeeCount++;
            // if ((at.isJoined() || at.isSelf())) {
            // onLinePersons++;
            // }
            // updateStatist();

        }

        Collections.sort(mList);
        adapter.notifyDataSetChanged();
    }

    public void updateExitedAttendee(Attendee at) {
        V2Log.d(TAG, "updateExitedAttendee 执行了");
        if (at == null) {
            return;
        }

        boolean found = false;
        for (int i = 0; i < mList.size(); i++) {
            Wrapper wr = mList.get(i);
            // Remove attendee devices, leave one device item
            if (wr.a.getAttId() == at.getAttId()) {
                if (found || wr.a.getType() == Attendee.TYPE_MIXED_VIDEO || wr.a.isRapidInitiation()) {
                    mList.remove(i--);
                } else {
                    found = true;
                }
            }
        }

        if (at.isRapidInitiation()) {
            mAttendeeCount--;
        }

        // Update on line count
        if (at.getType() != Attendee.TYPE_MIXED_VIDEO) {
            onLinePersons--;
            updateStatist();
        }

        Collections.sort(mList);
        adapter.notifyDataSetChanged();
    }

    /**
     * Update attendee device status
     *
     * @param att
     * @param udc
     */
    // public void updateAttendeeDevice(Attendee att, UserDeviceConfig udc) {
    //
    // for (int i = 0; i < mList.size(); i++) {
    // Wrapper wr = mList.get(i);
    // // Remove attendee devices, leave one device item
    // if (wr.a.getAttId() == att.getAttId()) {
    // // If user doesn't exist device before, then set
    // if (wr.udc == null) {
    // wr.udc = udc;
    // } else {
    // wr.udc.setShowing(false);
    // wr.udc.setEnable(udc.isEnable());
    // }
    // }
    // }
    // adapter.notifyDataSetChanged();
    // }
    public void updateAttendeeDevice(Attendee att, UserDeviceConfig udc) {

        for (int i = 0; i < mList.size(); i++) {
            Wrapper wr = mList.get(i);
            // Remove attendee devices, leave one device item
            if (wr.a.getAttId() == att.getAttId()) {
                // If user doesn't exist device before, then set
                if (wr.udc == null) {
                    wr.udc = udc;
                    break;
                } else {
                    if (wr.udc.getDeviceID().equals(udc.getDeviceID())) {
                        wr.udc.setShowing(false);
                        wr.udc.setEnable(udc.isEnable());
                    }
                }
            }
        }
        adapter.notifyDataSetChanged();

    }

    /**
     * reset attendee devices
     *
     * @param att
     * @param list
     */
    public void resetAttendeeDevices(Attendee att, List<UserDeviceConfig> list) {
        Wrapper attendeeFirstWrapper = null;
        int index = -1;
        // Remove exists devices
        for (int i = 0; i < mList.size(); i++) {
            Wrapper wr = mList.get(i);
            // Remove attendee devices, leave one device item
            if (wr.a.getAttId() == att.getAttId()) {
                // 记录设备的显示状态
                if(wr.udc != null){
                    for (int j = 0; j < list.size(); j++) {
                        UserDeviceConfig udc = list.get(j);
                        if (udc.getDeviceID().equals(wr.udc.getDeviceID())) {
                            udc.setShowing(wr.udc.isShowing());
                            break;
                        }
                    }
                }

                if (wr.sortFlag == FIRST_DEVICE_FLAG) {
                    wr.udc = null;
                    attendeeFirstWrapper = wr;
                    index = i;
                } else {
                    mList.remove(i--);
                }
            }
        }

        if (attendeeFirstWrapper == null) {
            V2Log.e("Error no first device ");
            return;
        }

        int deviceIndex = 1;
        for (int i = 0; i < list.size(); i++) {
            UserDeviceConfig udc = list.get(i);
            if (i == 0) {
                attendeeFirstWrapper.udc = udc;
            } else {
                mList.add(++index, new Wrapper(attendeeFirstWrapper.a, udc, deviceIndex++));
            }
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * Remove attend and device from layout.
     *
     * @param at
     */
    public void removeAttendee(Attendee at) {
        for (int i = 0; i < mList.size(); i++) {
            Wrapper wr = mList.get(i);
            // Remove attendee devices, leave one device item
            if (wr.a.getAttId() == at.getAttId()) {
                mList.remove(i--);
            }
        }
        // update attendee members
        mAttendeeCount--;
        updateStatist();
    }

    /**
     * Used to manually request FloatLayout, Because when this layout will hide,
     * call this function to inform interface
     */
    public void requestFloatLayout() {

        if ("float".equals(mPinButton.getTag())) {
            return;
        }

        if (this.listener != null) {
            this.listener.requestAttendeeViewFloatLayout(rootView);
        }

        mPinButton.setTag("float");
        ((ImageView) mPinButton).setImageResource(R.drawable.pin_button_selector);
    }

    public boolean getWindowSizeState() {
        String str = (String) mPinButton.getTag();
        return !(str == null || str.equals("float"));
    }

    private void configAttendee(Attendee at) {
        if (at == null) {
            return;
        }
        if (at.getType() != Attendee.TYPE_MIXED_VIDEO) {
            mAttendeeCount++;
            if ((at.isJoined() || at.isSelf())) {
                onLinePersons++;
            }
        }
        updateStatist();
    }

    public void updateStatist() {
        attendPersons.setText(TextUtils.concat(String.valueOf(onLinePersons), "/", String.valueOf(mAttendeeCount)));
        adapter.notifyDataSetChanged();
    }

    public void updateDisplay() {
        adapter.notifyDataSetChanged();
    }

    public void updateDeviceDisplay(UserDeviceConfig udc) {
        for (int i = 0; i < mList.size(); i++) {
            Wrapper wrapper = mList.get(i);
            if (wrapper.udc != null && wrapper.udc.getDeviceID().equals(udc.getDeviceID())) {
                wrapper.udc.setShowing(udc.isShowing());
                adapter.notifyDataSetChanged();
                break;
            }
        }
    }

    private class AttendeeContainerOnItemClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> ad, View view, int pos, long id) {
            if (AlgorithmUtil.isFastClick() || listener == null) {
                return;
            }

            Wrapper wr = (Wrapper) view.getTag();
            if(!wr.a.isSelf()){
                if (!wr.a.isJoined()) {
                    V2Log.d(TAG, " response item click failed! Current attendee not join or is self");
                    V2Log.d(TAG, " is join : " + wr.a.isJoined());
                    V2Log.d(TAG, " is selft : " + wr.a.isSelf());
                    return;
                }

                if (wr.udc == null || !wr.udc.isEnable()) {
                    Toast.makeText(getContext(), R.string.error_open_device_disable, Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean isMax = listener.checkSurfaceViewMax();
                if (isMax && !wr.udc.isShowing()) {
                    Toast.makeText(getContext(), R.string.error_exceed_support_video_count, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            listener.OnAttendeeClicked(wr.a, wr.udc);
        }

    }

    private class PinButtonOnClickListener implements OnClickListener {

        @Override
        public void onClick(View view) {
            if(AlgorithmUtil.isFastClick()){
                return ;
            }

            if (view.getTag().equals("float")) {
                if (listener != null) {
                    listener.requestAttendeeViewFixedLayout(rootView);
                }
            } else {
                if (listener != null) {
                    listener.requestAttendeeViewFloatLayout(rootView);
                }
            }

            if (view.getTag().equals("float")) {
                view.setTag("fix");
                ((ImageView) view).setImageResource(R.drawable.pin_fixed_button_selector);
            } else {
                view.setTag("float");
                ((ImageView) view).setImageResource(R.drawable.pin_button_selector);
            }
        }

    }

    private class SearchETTextChangedListener implements TextWatcher {

        @Override
        public void afterTextChanged(Editable et) {
            if (TextUtils.isEmpty(et)) {
                mSearchUtils.clearAll();
                mIsStartedSearch = mSearchUtils.mIsStartedSearch;
                isFrist = true;
                if(mSearchList != null)
                    mSearchList.clear();
                adapter.notifyDataSetChanged();
            } else {
                if (isFrist) {
                    mSearchUtils.clearAll();
                    mIsStartedSearch = false;
                    List<Object> wrappers = new ArrayList<>();
                    wrappers.addAll(mList);
                    mSearchUtils.receiveList = wrappers;
                    isFrist = false;
                }

                mSearchList = mSearchUtils.startVideoAttendeeSearch(et.toString());
                mIsStartedSearch = mSearchUtils.mIsStartedSearch;
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int cout) {

        }

    }

    private class AttendeeContainerAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mIsStartedSearch)
                return mSearchList.size();
            else
                return mList.size();
        }

        @Override
        public Object getItem(int position) {
            if (mIsStartedSearch)
                return mSearchList.get(position);
            else
                return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Wrapper wrapper;
            if (mIsStartedSearch)
                wrapper = mSearchList.get(position);
            else
                wrapper = mList.get(position);

            if (convertView == null) {
                convertView = buildAttendeeView(wrapper);
            } else {
                updateView(wrapper, convertView);
            }
            return convertView;
        }
    }

    public class Wrapper implements Comparable<Wrapper> {
        public Attendee a;
        public UserDeviceConfig udc;
        // Use to sort.
        // we can remove view if sortFlag is DEFAULT_DEVICE_FLAG
        int sortFlag;

        public Wrapper(Attendee a, UserDeviceConfig udc, int sortFlag) {
            super();
            this.a = a;
            this.udc = udc;
            this.sortFlag = sortFlag;
        }

        public UserDeviceConfig getUserDeviceConfig() {
            return this.udc;
        }

        @Override
        public int compareTo(Wrapper wr) {

            if (this.a == null) {
                if (wr.a == null) {
                    return 0;
                } else {
                    return 1;
                }
            } else if (!this.a.isJoined()) {
                if (wr.a == null) {
                    return -1;
                } else if (!wr.a.isJoined()) {
                    return 0;
                } else {
                    return 1;
                }

            } else if (this.a.getUser() != null && this.a.getUser().isRapidInitiation()) {
                if (wr.a == null) {
                    return -1;
                } else if (!wr.a.isJoined()) {
                    return -1;
                } else if (wr.a.getUser() != null && wr.a.getUser().isRapidInitiation()) {
                    // return 0;
                    return calledByCompareTo(wr);
                } else {
                    return 1;
                }
            } else if (this.a.getType() == Attendee.TYPE_MIXED_VIDEO) {
                if (wr.a == null) {
                    return -1;
                } else if (!wr.a.isJoined()) {
                    return -1;
                } else if (wr.a.getUser() != null && wr.a.getUser().isRapidInitiation()) {
                    return -1;
                } else if (wr.a.getType() == Attendee.TYPE_MIXED_VIDEO) {
                    return calledByCompareTo(wr);
                } else {
                    return -1;
                }

            } else if (this.a.isChairMan()) {
                if (wr.a == null) {
                    return -1;
                } else if (!wr.a.isJoined()) {
                    return -1;
                } else if (wr.a.getUser() != null && wr.a.getUser().isRapidInitiation()) {
                    return -1;
                } else if (wr.a.getType() == Attendee.TYPE_MIXED_VIDEO) {
                    return 1;
                } else if (wr.a.isChairMan()) {
                    // return 0;
                    return calledByCompareTo(wr);
                } else {
                    return -1;
                }
            } else if (this.a.getLectureState() == Attendee.LECTURE_STATE_GRANTED) {
                if (wr.a == null) {
                    return -1;
                } else if (!wr.a.isJoined()) {
                    return -1;
                } else if (wr.a.getUser() != null && wr.a.getUser().isRapidInitiation()) {
                    return -1;
                } else if (wr.a.getType() == Attendee.TYPE_MIXED_VIDEO) {
                    return 1;
                } else if (wr.a.isChairMan()) {
                    return 1;
                } else if (wr.a.getLectureState() == Attendee.LECTURE_STATE_GRANTED) {
                    // return 0;
                    return calledByCompareTo(wr);
                } else {
                    return -1;
                }
            } else if (this.a.getLectureState() == Attendee.LECTURE_STATE_APPLYING) {
                if (wr.a == null) {
                    return -1;
                } else if (!wr.a.isJoined()) {
                    return -1;
                } else if (wr.a.getUser() != null && wr.a.getUser().isRapidInitiation()) {
                    return -1;
                } else if (wr.a.getType() == Attendee.TYPE_MIXED_VIDEO) {
                    return 1;
                } else if (wr.a.isChairMan()) {
                    return 1;
                } else if (wr.a.getLectureState() == Attendee.LECTURE_STATE_GRANTED) {
                    return 1;
                } else if (wr.a.getLectureState() == Attendee.LECTURE_STATE_APPLYING) {
                    // return 0;
                    return calledByCompareTo(wr);
                } else {
                    return -1;
                }
            } else if (this.a.isSpeaking()) {
                if (wr.a == null) {
                    return -1;
                } else if (!wr.a.isJoined()) {
                    return -1;
                } else if (wr.a.getUser() != null && wr.a.getUser().isRapidInitiation()) {
                    return -1;
                } else if (wr.a.getType() == Attendee.TYPE_MIXED_VIDEO) {
                    return 1;
                } else if (wr.a.isChairMan()) {
                    return 1;
                } else if (wr.a.getLectureState() == Attendee.LECTURE_STATE_GRANTED) {
                    return 1;
                } else if (wr.a.getLectureState() == Attendee.LECTURE_STATE_APPLYING) {
                    return 1;
                } else if (this.a.isSpeaking()) {
                    return calledByCompareTo(wr);
                } else {
                    return -1;
                }
            } else {
                if (wr.a == null) {
                    return -1;
                } else if (!wr.a.isJoined()) {
                    return -1;
                } else if (wr.a.getUser() != null && wr.a.getUser().isRapidInitiation()) {
                    return -1;
                } else if (wr.a.getType() == Attendee.TYPE_MIXED_VIDEO) {
                    return 1;
                } else if (wr.a.isChairMan()) {
                    return 1;
                } else if (wr.a.getLectureState() == Attendee.LECTURE_STATE_GRANTED) {
                    return 1;
                } else if (wr.a.getLectureState() == Attendee.LECTURE_STATE_APPLYING) {
                    return 1;
                } else if (this.a.isSpeaking()) {
                    return 1;
                } else {
                    // return 0;
                    return calledByCompareTo(wr);
                }
            }
        }

        private int calledByCompareTo(Wrapper wr) {
            int ret = this.a.compareTo(wr.a);
            if (ret == 0) {
                if (this.sortFlag < wr.sortFlag) {
                    return -1;
                }
                if (this.sortFlag > wr.sortFlag) {
                    return 0;
                } else {
                    return 1;
                }

            } else {
                if (this.a.isJoined()) {
                    return -1;
                } else if (wr.a.isJoined()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    }

}
