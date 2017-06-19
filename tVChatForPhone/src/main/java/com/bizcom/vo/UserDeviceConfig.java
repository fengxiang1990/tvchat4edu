package com.bizcom.vo;

import com.bizcom.vc.activity.conference.ConferenceSurfaceView;
import com.config.V2GlobalConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import v2av.VideoPlayer;

/**
 * User video device configuration object.<br>
 * Local user video device is unnecessary VideoPlayer and device id information.
 * <br>
 * If we want to open remote user's video, we need VidePlayer and device id
 * information. To get remote user's device id, we can listen
 * . <br>
 * If object as local device, mVP is null and deviceID is "".
 *
 * @author jiangzhen
 * @see v2av.VideoPlayer com.V2.jni.VideoRequestCallback
 */
public class UserDeviceConfig {

    String tag = "UserDeviceConfig";
    private int groupType;
    private long groupID;
    private long mUerID;
    private String mDeviceID;
    private WeakReference<VideoPlayer> softReferenceVp;
    private int mBusinessType;
    private ConferenceSurfaceView mSVHolder;
    private boolean isShowing;
    private boolean isEnable;
    private boolean isDefault;
    private boolean isUserFirstDev;
    private int userDeviceConfigType;

    /**
     * Construct EVIDEODEVTYPE_VIDEO device
     *
     * @param groupType Conference or IM
     * @param groupID
     * @param mUerID
     * @param mDeviceID
     * @param mVP
     */
    public UserDeviceConfig(int groupType, long groupID, long mUerID, String mDeviceID, VideoPlayer mVP) {
        this(groupType, groupID, mUerID, mDeviceID, mVP, V2GlobalConstants.EVIDEODEVTYPE_VIDEO);
    }

    /**
     * According to parameter construct new user device donfiguration object
     *
     * @param groupType            Conference or IM
     * @param groupID
     * @param mUerID
     * @param mDeviceID
     * @param mVP
     * @param userDeviceConfigType
     */
    public UserDeviceConfig(int groupType, long groupID, long mUerID, String mDeviceID, VideoPlayer mVP,
                            int userDeviceConfigType) {
        this.groupType = groupType;
        this.groupID = groupID;
        this.mUerID = mUerID;
        this.mDeviceID = mDeviceID;
        this.softReferenceVp = new WeakReference<VideoPlayer>(mVP);
        this.userDeviceConfigType = userDeviceConfigType;
    }


    /**
     * Just clear all resources which this object holded. Notice: Before call
     * this function, you must call first
     */
    public void doClose() {
        this.isShowing = false;
        softReferenceVp.get().StopVideoPlay();
        softReferenceVp.get().Release();
        this.softReferenceVp.clear();
    }

    public long getUserID() {
        return mUerID;
    }

    public void setUserID(long userID) {
        this.mUerID = userID;
    }

    public String getDeviceID() {
        return mDeviceID;
    }

    public void setDeviceID(String deviceID) {
        this.mDeviceID = deviceID;
    }

    public VideoPlayer getVp() {
        return softReferenceVp.get();
    }

    public void setVp(VideoPlayer vp) {
        this.softReferenceVp = new WeakReference<VideoPlayer>(vp);
    }

    public int getBusinessType() {
        return mBusinessType;
    }

    public void setBusinessType(int businessType) {
        this.mBusinessType = businessType;
    }

    public ConferenceSurfaceView getSVHolder() {
        return mSVHolder;
    }

    public void setSVHolder(ConferenceSurfaceView sVHolder) {
        this.mSVHolder = sVHolder;
    }

    public boolean isShowing() {
        return isShowing;
    }

    public void setShowing(boolean isShowing) {
        this.isShowing = isShowing;
    }

    public int getType() {
        return this.userDeviceConfigType;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public int getGroupType() {
        return groupType;
    }

    public void setGroupType(int groupType) {
        this.groupType = groupType;
    }

    public long getGroupID() {
        return groupID;
    }

    public void setGroupID(long groupID) {
        this.groupID = groupID;
    }

    public boolean isUserFirstDev() {
        return isUserFirstDev;
    }

    public void setUserFirstDev(boolean isUserFirstDev) {
        this.isUserFirstDev = isUserFirstDev;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mDeviceID == null) ? 0 : mDeviceID.hashCode());
        result = prime * result + (int) (mUerID ^ (mUerID >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UserDeviceConfig other = (UserDeviceConfig) obj;
        if (mDeviceID == null) {
            if (other.mDeviceID != null)
                return false;
        } else if (!mDeviceID.equals(other.mDeviceID))
            return false;
        if (mUerID != other.mUerID)
            return false;
        return true;
    }

    /**
     * <xml defaultid='12:USB Camera____2080603797'><video bps='2048'
     * camtype='0' comm='0' desc='USB Camera____2080603797' fps='25' h='720'
     * id='12:USB Camera____2080603797' inuse='1' videotype='1' w='1280'/><video
     * bps='256' camtype='0' comm='0' desc='USB Camera____92543101' fps='15'
     * h='240' id='12:USB Camera____92543101' inuse='1' videotype='1'
     * w='320'/></xml>
     *
     * @param xmlData
     * @return
     */
    public static List<UserDeviceConfig> parseFromXml(long uid, String xmlData) {
        if (xmlData == null) {
            throw new RuntimeException(" user video data is null");
        }
        List<UserDeviceConfig> l = new ArrayList<UserDeviceConfig>();

        InputStream is = null;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            is = new ByteArrayInputStream(xmlData.getBytes("UTF-8"));
            Document doc = dBuilder.parse(is);

            doc.getDocumentElement().normalize();
            String defaultId = "";
            int start = xmlData.indexOf("defaultid='");
            int end = xmlData.indexOf("'", start + 11);
            defaultId = xmlData.substring(start + 11, end);

            NodeList videol = doc.getElementsByTagName("video");
            for (int t = 0; t < videol.getLength(); t++) {
                Element video = (Element) videol.item(t);
                String deviceId = video.getAttribute("id");
                UserDeviceConfig udc = new UserDeviceConfig(0, 0, uid, deviceId, null);
                udc.setEnable("1".equals(video.getAttribute("inuse")) ? true : false);
                if (defaultId.equals(deviceId)) {
                    udc.setDefault(true);
                }
                l.add(udc);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return l;
    }

}
