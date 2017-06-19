package com.bizcom.db.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import com.bizcom.db.ContentDescriptor;
import com.bizcom.util.CrashHandler;
import com.bizcom.util.V2Log;
import com.bizcom.vo.AudioVideoMessageBean;
import com.bizcom.vo.User;
import com.bizcom.vo.VideoBean;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;

import java.util.ArrayList;
import java.util.List;

public class MediaRecordProvider extends DatabaseProvider {

    /**
     * put the media(audio or video) record datas VMessage Object to DataBases
     */
    public static Uri saveMediaChatHistories(VideoBean bean) {

        if (bean == null)
            throw new NullPointerException("the given VideoBean object is null");

        Uri uri = null;
        ContentValues values = new ContentValues();
        values.put(ContentDescriptor.HistoriesMedia.Cols.OWNER_USER_ID,
                GlobalHolder.getInstance().getCurrentUserId());
        values.put(
                ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_SAVEDATE,
                GlobalConfig.getGlobalServerTime());
        values.put(ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_CHAT_ID,
                bean.mediaChatID);
        values.put(
                ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_FROM_USER_ID,
                bean.formUserID);
        values.put(
                ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_TO_USER_ID,
                bean.toUserID);
        values.put(
                ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_REMOTE_USER_ID,
                bean.remoteUserID);
        values.put(ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_TYPE,
                bean.mediaType);
        values.put(ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_STATE,
                bean.mediaState);
        values.put(
                ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_START_DATE,
                bean.startDate);
        values.put(
                ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_END_DATE,
                bean.endDate);
        values.put(
                ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_READ_STATE,
                bean.readSatate);

        values.put(
                ContentDescriptor.HistoriesMedia.Cols.HISTORY_ACOUNT,
                bean.account);
        values.put(
                ContentDescriptor.HistoriesMedia.Cols.HISTORY_AVATAR_URL,
                bean.avatarUrl);
        values.put(
                ContentDescriptor.HistoriesMedia.Cols.HISTORY_NICK_NAME,
                bean.nickName);
        //储存是否自己挂断
        values.put(ContentDescriptor.HistoriesMedia.Cols.CANCEL_BY_MINE,
                bean.isCancelByMine);
        uri = mContext.getContentResolver().insert(
                ContentDescriptor.HistoriesMedia.CONTENT_URI, values);
        return uri;
    }

    /**
     * According to remote user id, loading all audio or video communication
     * records.
     *
     * @param remoteUserID audio or video
     */
    public static List<AudioVideoMessageBean> loadMediaHistoriesMessage(
            long remoteUserID, int meidaType) {

        Cursor cursor = null;
        LongSparseArray<AudioVideoMessageBean> tempList = new LongSparseArray<AudioVideoMessageBean>();
        List<AudioVideoMessageBean> targetList = new ArrayList<AudioVideoMessageBean>();
        try {
            String selection;
            switch (meidaType) {
                case AudioVideoMessageBean.TYPE_AUDIO:
                    selection = (ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_FROM_USER_ID
                            + "= ? and "
                            + ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_TYPE + "= 0")
                            + "or"
                            + (ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_TO_USER_ID
                            + "= ? and "
                            + ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_TYPE + "= 0");
                    break;
                case AudioVideoMessageBean.TYPE_VIDEO:
                    selection = (ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_FROM_USER_ID
                            + "= ? and "
                            + ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_TYPE + "= 1")
                            + "or"
                            + (ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_TO_USER_ID
                            + "= ? and "
                            + ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_TYPE + "= 1");
                    break;
                default:
                    selection = ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_FROM_USER_ID
                            + "= ? or "
                            + ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_TO_USER_ID
                            + "= ?";
                    break;
            }

            String[] args = new String[]{String.valueOf(remoteUserID),
                    String.valueOf(remoteUserID)};
            String sortOrder = ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_SAVEDATE
                    + " desc";
            cursor = mContext.getContentResolver().query(
                    ContentDescriptor.HistoriesMedia.CONTENT_URI,
                    ContentDescriptor.HistoriesMedia.Cols.ALL_CLOS, selection,
                    args, sortOrder);

            if (cursor == null)
                return targetList;

            if (cursor.getCount() <= 0) {
                return targetList;
            }

            long currentID = GlobalHolder.getInstance().getCurrentUserId();
            AudioVideoMessageBean currentMedia;
            AudioVideoMessageBean.ChildMessageBean currentChildMedia;
            int isCallOut; // 主动呼叫还是被动 0 主动 1 被动
            while (cursor.moveToNext()) {
                currentChildMedia = new AudioVideoMessageBean.ChildMessageBean();
                int isCancelByMine = cursor
                        .getInt(cursor
                                .getColumnIndex(ContentDescriptor.HistoriesMedia.Cols.CANCEL_BY_MINE));
                int types = cursor
                        .getInt(cursor
                                .getColumnIndex(ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_TYPE));
                int readState = cursor
                        .getInt(cursor
                                .getColumnIndex(ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_READ_STATE));
                long startDate = cursor
                        .getLong(cursor
                                .getColumnIndex(ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_START_DATE));
                long endDate = cursor
                        .getLong(cursor
                                .getColumnIndex(ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_END_DATE));
                long formID = cursor
                        .getLong(cursor
                                .getColumnIndex(ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_FROM_USER_ID));
                long toID = cursor
                        .getLong(cursor
                                .getColumnIndex(ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_TO_USER_ID));
                long remoteID = cursor
                        .getLong(cursor
                                .getColumnIndex(ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_REMOTE_USER_ID));
                int mediaState = cursor
                        .getInt(cursor
                                .getColumnIndex(ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_STATE));
                int messageId = cursor
                        .getInt(cursor
                                .getColumnIndex(ContentDescriptor.HistoriesMedia.Cols.ID));
                if (currentID == formID)
                    isCallOut = AudioVideoMessageBean.STATE_CALL_OUT;
                else
                    isCallOut = AudioVideoMessageBean.STATE_CALL_IN;
//                String acount = cursor
//                        .getString(cursor
//                                .getColumnIndex(ContentDescriptor.HistoriesMedia.Cols.HISTORY_ACOUNT));
//                String avatarUrl = cursor
//                        .getString(cursor
//                                .getColumnIndex(ContentDescriptor.HistoriesMedia.Cols.HISTORY_AVATAR_URL));
//                String nikeName = cursor
//                        .getString(cursor
//                                .getColumnIndex(ContentDescriptor.HistoriesMedia.Cols.HISTORY_NICK_NAME));


//                Log.i("tvliao", "loadMediaHistoriesMessage" + acount + "-" + avatarUrl + "-" + nikeName);

                String remoteUserName = "";
                if (types == AudioVideoMessageBean.TYPE_SIP) {
                    remoteUserName = String.valueOf(remoteID);
                } else {
                    User remoteUser = GlobalHolder.getInstance().getUser(remoteID);
                    if (remoteUser == null) {
                        V2Log.e("get null when get remote user :" + remoteID);
                        continue;
                    }
                    remoteUserName = remoteUser.getDisplayName();
                }

                currentMedia = tempList.get(remoteID);
                if (currentMedia == null) {
                    AudioVideoMessageBean tempMedia = new AudioVideoMessageBean();
                    tempMedia.name = remoteUserName;
                    tempMedia.isCallOut = isCallOut;
                    tempMedia.fromUserID = formID;
                    tempMedia.toUserID = toID;
                    tempMedia.remoteUserID = remoteID;
                    tempMedia.readState = readState;
                    tempMedia.isCancelByMine = isCancelByMine;
                    tempList.put(remoteID, tempMedia);
                    currentMedia = tempMedia;
                }
                currentChildMedia.isCancelByMine = isCancelByMine;
                currentChildMedia.messageId = messageId;
                currentChildMedia.childMediaType = types;
                currentChildMedia.childISCallOut = isCallOut;
                currentChildMedia.childHoldingTime = endDate - startDate;
                currentChildMedia.childSaveDate = startDate;
                currentChildMedia.childReadState = readState;
                currentChildMedia.childMediaState = mediaState;
                currentMedia.mChildBeans.add(currentChildMedia);
                // 判断该条消息是否未读，更改未读消息数量
                if (readState == V2GlobalConstants.READ_STATE_UNREAD) {
                    currentMedia.callNumbers += 1;
                }
            }

            for (int i = 0; i < tempList.size(); i++) {
                AudioVideoMessageBean value = tempList.valueAt(i);
                // 获取最新的通话数据
                String selections = ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_REMOTE_USER_ID
                        + "= ? ";
                String[] selectionArgs = new String[]{String
                        .valueOf(value.remoteUserID)};
                VideoBean newestMediaMessage = getNewestMediaMessage(
                        selections, selectionArgs);
                // ChildMessageBean childMessageBean =
                // newestMediaMessage.mChildBeans.get(value.mChildBeans.size() -
                // 1);
                // 更新
                // value.holdingTime = childMessageBean.childHoldingTime;
                // value.mediaType = childMessageBean.childMediaType;
                // value.readState = childMessageBean.childReadState;
                value.holdingTime = newestMediaMessage.endDate
                        - newestMediaMessage.startDate;
                value.mediaType = newestMediaMessage.mediaType;
                value.meidaState = newestMediaMessage.mediaState;
                value.readState = newestMediaMessage.readSatate;
                targetList.add(value);
            }
            return targetList;
        } catch (Exception e) {
            e.printStackTrace();
            CrashHandler.getInstance().saveCrashInfo2File(e);
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * according to remote user id , delete all voice message..
     */
    public static int deleteMediaMessage(long remoteUserID) {
        int ret;
        if (remoteUserID == -1)
            ret = ChatMessageProvider.mContext.getContentResolver().delete(
                    ContentDescriptor.HistoriesMedia.CONTENT_URI, null, null);
        else
            ret = ChatMessageProvider.mContext
                    .getContentResolver()
                    .delete(ContentDescriptor.HistoriesMedia.CONTENT_URI,
                            ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_REMOTE_USER_ID
                                    + "= ?",
                            new String[]{String.valueOf(remoteUserID)});
        if (ret <= 0)
            V2Log.d(ChatMessageProvider.TAG,
                    "May delete voice Message failed...groupID : "
                            + remoteUserID);
        return ret;
    }

    public static int updateAllRecordToReaded() {
        ContentValues values = new ContentValues();
        values.put(
                ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_READ_STATE,
                V2GlobalConstants.READ_STATE_READ);
        String where = ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_READ_STATE
                + "= ?";
        return mContext.getContentResolver().update(
                ContentDescriptor.HistoriesMedia.CONTENT_URI,
                values,
                where,
                new String[]{String
                        .valueOf(V2GlobalConstants.READ_STATE_UNREAD)});
    }

    /**
     * 查询数据库中是否存在有未读的音视频消息记录
     *
     * @return
     */
    public static boolean queryIsHaveUnreadMessage() {
        String selection = ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_READ_STATE
                + " = ?";
        String[] args = new String[]{String
                .valueOf(V2GlobalConstants.READ_STATE_UNREAD)};
        return queryIsMediaVoiceMessages(selection, args);
    }

    /**
     * 查询数据库中是否存在音视频消息记录
     *
     * @return
     */
    public static boolean queryIsHaveMediaMessages() {
        return queryIsMediaVoiceMessages(null, null);
    }

    /**
     * 根据用户id , 查询该用户是否有音视频消息记录
     *
     * @param userID
     * @return true 代表有记录 , false 代表没有记录
     */
    public static boolean queryIsHaveMediaMessages(long userID) {
        String selection = ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_REMOTE_USER_ID
                + " = ? ";
        String[] args = new String[]{String.valueOf(userID)};
        return queryIsMediaVoiceMessages(selection, args);
    }

    /**
     * 根据用户id , 查询该用户是否有音视频消息记录
     *
     * @return true 代表有记录 , false 代表没有记录
     */
    public static boolean queryIsMediaVoiceMessages(String selection,
                                                    String[] args) {

        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(
                    ContentDescriptor.HistoriesMedia.CONTENT_URI, null,
                    selection, args, null);
            if (cursor == null || cursor.getCount() <= 0) {
                return false;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            CrashHandler.getInstance().saveCrashInfo2File(e);
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 获取最新的音视频记录消息
     *
     * @return
     */
    public static VideoBean getNewestMediaMessage() {
        return MediaRecordProvider.getNewestMediaMessage(null, null);
    }

    /**
     * 根据传入的查询条件，获取最新的通信消息对象(音频或视频通信)
     *
     * @param selection
     * @param selectionArgs
     * @return
     */
    public static VideoBean getNewestMediaMessage(String selection,
                                                  String[] selectionArgs) {

        Cursor cursor = null;
        try {

            String order = ContentDescriptor.HistoriesMedia.Cols.HISTORY_MEDIA_SAVEDATE
                    + " desc, "
                    + ContentDescriptor.HistoriesMedia.Cols.ID
                    + " desc limit 1 offset 0 ";

            cursor = mContext.getContentResolver().query(
                    ContentDescriptor.HistoriesMedia.CONTENT_URI,
                    ContentDescriptor.HistoriesMedia.Cols.ALL_CLOS, selection,
                    selectionArgs, order);

            if (cursor == null) {
                return null;
            }

            if (cursor.getCount() < 0) {
                return null;
            }

            if (cursor.moveToFirst()) {
                VideoBean bean = new VideoBean();
                bean.ownerID = cursor.getLong(cursor
                        .getColumnIndex("OwnerUserID"));
                bean.formUserID = cursor.getLong(cursor
                        .getColumnIndex("FromUserID"));
                bean.toUserID = cursor.getLong(cursor
                        .getColumnIndex("ToUserID"));
                bean.startDate = cursor.getLong(cursor
                        .getColumnIndex("StartDate"));
                bean.endDate = cursor.getLong(cursor.getColumnIndex("EndDate"));
                bean.readSatate = cursor.getInt(cursor
                        .getColumnIndex("ReadState"));
                bean.mediaState = cursor.getInt(cursor
                        .getColumnIndex("MediaState"));
                bean.mediaType = cursor.getInt(cursor
                        .getColumnIndex("MediaType"));
                bean.mediaChatID = cursor.getString(cursor
                        .getColumnIndex("MediaChatID"));
                bean.remoteUserID = cursor.getLong(cursor
                        .getColumnIndex("RemoteUserID"));
                return bean;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            CrashHandler.getInstance().saveCrashInfo2File(e);
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
