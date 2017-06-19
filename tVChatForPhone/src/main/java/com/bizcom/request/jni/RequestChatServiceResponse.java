package com.bizcom.request.jni;

/**
 * Used to wrap response data from JNI when receive call from JNI
 *
 * @author 28851274
 */
public class RequestChatServiceResponse extends JNIResponse {

    public static final int UNKNOWN = 0;
    public static final int ACCEPTED = 1;
    public static final int REJCTED = 2;
    public static final int CANCELED = 3;
    public static final int HANGUP = 4;

    public static final int P2P_TYPE_AUDIO = 5;
    public static final int P2P_TYPE_VIDEO = 6;

    private int code;
    private int mP2PChatType;
    private long uid;
    private String szDeviceID;
    private String szSessionID;
    private long fromUserID;
    private String data;

    public RequestChatServiceResponse(int p2PChatType , int state, long remoteUserID,
                                      String szSessionID, String szDeviceID , Result result) {
        super(result);
        this.mP2PChatType = p2PChatType;
        this.uid = remoteUserID;
        this.szSessionID = szSessionID;
        this.code = state;
        this.szDeviceID = szDeviceID;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public long getFromUserID() {
        return fromUserID;
    }

    public void setFromUserID(long fromUserID) {
        this.fromUserID = fromUserID;
    }

    public int getCode() {
        return code;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getSzDeviceID() {
        return szDeviceID;
    }

    public String getSzSessionID() {
        return szSessionID;
    }

    public int getP2PChatType(){
        return mP2PChatType;
    }
}
