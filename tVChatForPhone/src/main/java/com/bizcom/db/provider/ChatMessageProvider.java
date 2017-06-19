package com.bizcom.db.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.SparseArray;

import com.V2.jni.FileRequest;
import com.bizcom.db.ContentDescriptor;
import com.bizcom.db.ContentDescriptor.HistoriesMessage;
import com.bizcom.db.DataBaseContext;
import com.bizcom.db.V2techBaseProvider;
import com.bizcom.util.CrashHandler;
import com.bizcom.util.MessageUtil;
import com.bizcom.util.V2Log;
import com.bizcom.util.XmlParser;
import com.bizcom.vo.CrowdGroup;
import com.bizcom.vo.Group;
import com.bizcom.vo.User;
import com.bizcom.vo.VCrowdFile;
import com.bizcom.vo.meesage.VMessage;
import com.bizcom.vo.meesage.VMessageAbstractItem;
import com.bizcom.vo.meesage.VMessageAudioItem;
import com.bizcom.vo.meesage.VMessageFileItem;
import com.bizcom.vo.meesage.VMessageFileItem.FileType;
import com.bizcom.vo.meesage.VMessageImageItem;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatMessageProvider extends DatabaseProvider {

    public static final String TAG = ChatMessageProvider.class.getSimpleName();

    /**
     * put the chat datas VMessage Object to DataBases
     */
    public static VMessage saveChatMessage(VMessage vm) {
        if (vm == null) {
            V2Log.e(TAG, "saveChatMessage : Save failed! Given VMessage is null!");
            return null;
        }

        int groupType = vm.getMsgCode();
        long groupID = vm.getGroupId();
        long remote = confirmRmoteID(vm);

        // 判断数据库是否存在
        if (!isTableExist(vm)) {
            return null;
        }

        if (vm.getMsgCode() == V2GlobalConstants.GROUP_TYPE_USER) {
            if (vm.isLocal()) {
                GlobalHolder.getInstance().setMessageShowTime(mContext, vm.getMsgCode(), -1,
                        vm.getToUser().getmUserId(), vm);
            } else {
                GlobalHolder.getInstance().setMessageShowTime(mContext, vm.getMsgCode(), -1,
                        vm.getFromUser().getmUserId(), vm);
            }
        } else {
            GlobalHolder.getInstance().setMessageShowTime(mContext, vm.getMsgCode(), vm.getGroupId(), -1, vm);
        }

        // 直接将xml存入数据库中，方便以后扩展。
        ContentValues values = new ContentValues();
        values.put(HistoriesMessage.Cols.HISTORY_MESSAGE_GROUP_TYPE, groupType);
        values.put(HistoriesMessage.Cols.HISTORY_MESSAGE_GROUP_ID, groupID);
        values.put(HistoriesMessage.Cols.HISTORY_MESSAGE_FROM_USER_ID, vm.getFromUser().getmUserId());
        if (vm.getToUser() != null) {
            values.put(HistoriesMessage.Cols.HISTORY_MESSAGE_TO_USER_ID, vm.getToUser().getmUserId());
        }
        values.put(HistoriesMessage.Cols.HISTORY_MESSAGE_REMOTE_USER_ID, remote);
        values.put(HistoriesMessage.Cols.HISTORY_MESSAGE_ID, vm.getUUID());
        values.put(HistoriesMessage.Cols.HISTORY_MESSAGE_TRANSTATE, vm.getState());
        if (vm.isShowTime()) {
            values.put(HistoriesMessage.Cols.HISTORY_MESSAGE_SHOW_TIME,
                    V2GlobalConstants.MESSAGE_SHOW_TIME);
        } else {
            values.put(HistoriesMessage.Cols.HISTORY_MESSAGE_SHOW_TIME,
                    V2GlobalConstants.MESSAGE_NOT_SHOW_TIME);
        }
        values.put(HistoriesMessage.Cols.HISTORY_MESSAGE_TRANSTATE, vm.getState());
        values.put(HistoriesMessage.Cols.HISTORY_MESSAGE_CONTENT, vm.getmXmlDatas());
        //此处解析是为了存储MsgPlainText字段，查询使用
        if (!vm.isLocal()) {
            vm = XmlParser.extraTextMetaFrom(vm, vm.getmXmlDatas());
        }
        String chatContent = MessageUtil.getMixedConversationCopyedContent(vm).toString();
        values.put(HistoriesMessage.Cols.HISTORY_MESSAGE_CHAT_CONTENT, chatContent);
        values.put(HistoriesMessage.Cols.HISTORY_MESSAGE_SAVEDATE, vm.getmDateLong());
        values.put(HistoriesMessage.Cols.OWNER_USER_ID,
                GlobalHolder.getInstance().getCurrentUserId());
        Uri uri = mContext.getContentResolver().insert(HistoriesMessage.CONTENT_URI, values);
        vm.setId(ContentUris.parseId(uri));
        return vm;
    }

    /**
     * put the binary(image or record audio) datas VMessage Object to DataBases
     */
    public static Uri saveBinaryVMessage(VMessage vm) {
        if (vm == null) {
            V2Log.e(TAG, "saveBinaryVMessage : Save failed! Given VMessage is null!");
            return null;
        }

        Uri uri = null;
        // 确定远程用户
        long remote = confirmRmoteID(vm);

        int groupType = vm.getMsgCode();
        long groupID = vm.getGroupId();
        if (!isTableExist(vm))
            return null;
        ContentValues values = new ContentValues();

        List<VMessageImageItem> imageItems = vm.getImageItems();
        for (VMessageImageItem vMessageImageItem : imageItems) {
            values.put(ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_GROUP_TYPE, groupType);
            values.put(ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_GROUP_ID, groupID);
            values.put(ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_FROM_USER_ID,
                    vm.getFromUser().getmUserId());
            if (vm.getToUser() != null) {
                values.put(ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_TO_USER_ID,
                        vm.getToUser().getmUserId());
            }
            values.put(HistoriesMessage.Cols.HISTORY_MESSAGE_REMOTE_USER_ID, remote);
            values.put(ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_ID, vMessageImageItem.getUuid());
            values.put(ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_TRANSTATE, vm.getState());
            values.put(ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_SAVEDATE, vm.getmDateLong());
            values.put(ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_PATH, vMessageImageItem.getFilePath());
            values.put(ContentDescriptor.HistoriesGraphic.Cols.OWNER_USER_ID,
                    GlobalHolder.getInstance().getCurrentUserId());
            uri = mContext.getContentResolver().insert(ContentDescriptor.HistoriesGraphic.CONTENT_URI, values);
        }

        List<VMessageAudioItem> audioItems = vm.getAudioItems();
        for (VMessageAudioItem vMessageAudioItem : audioItems) {
            values.put(ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_GROUP_TYPE, groupType);
            values.put(ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_GROUP_ID, groupID);
            values.put(ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_FROM_USER_ID,
                    vm.getFromUser().getmUserId());
            if (vm.getToUser() != null) {
                values.put(ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_TO_USER_ID,
                        vm.getToUser().getmUserId());
            }
            values.put(ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_REMOTE_USER_ID, remote);
            values.put(ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_ID, vMessageAudioItem.getUuid());
            values.put(ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_SEND_STATE, vm.getState());
            values.put(ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_SAVEDATE, vm.getmDateLong());
            values.put(ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_PATH, vMessageAudioItem.getAudioFilePath());
            values.put(ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_SECOND, vMessageAudioItem.getSeconds());
            values.put(ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_READ_STATE,
                    vMessageAudioItem.getReadState());
            values.put(ContentDescriptor.HistoriesAudios.Cols.OWNER_USER_ID,
                    GlobalHolder.getInstance().getCurrentUserId());
            uri = mContext.getContentResolver().insert(ContentDescriptor.HistoriesAudios.CONTENT_URI, values);
        }
        return uri;
    }

    /**
     * put the file datas VMessage Object to DataBases
     */
    public static Uri saveFileVMessage(VMessage vm) {

        if (vm == null) {
            V2Log.e(TAG, "saveFileVMessage : Save failed! Given VMessage is null!");
            return null;
        }

        if (vm.getFileItems().size() <= 0)
            return null;

        // 确定远程用户
        long remote = confirmRmoteID(vm);
        Uri uri = null;
        VMessageFileItem file;
        for (int i = 0; i < vm.getFileItems().size(); i++) {
            file = vm.getFileItems().get(i);
            ContentValues values = new ContentValues();
            values.put(ContentDescriptor.HistoriesFiles.Cols.OWNER_USER_ID,
                    GlobalHolder.getInstance().getCurrentUserId());
            values.put(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SAVEDATE, GlobalConfig.getGlobalServerTime());
            values.put(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_FROM_USER_ID, vm.getFromUser().getmUserId());
            if (vm.getToUser() != null) {
                values.put(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_TO_USER_ID, vm.getToUser().getmUserId());
            }
            values.put(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_REMOTE_USER_ID, remote);
            values.put(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_ID, file.getUuid());
            values.put(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_PATH, file.getFilePath());
            values.put(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SIZE, file.getFileSize());
            values.put(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SEND_STATE, file.getState());
            values.put(ContentDescriptor.HistoriesFiles.Cols.HISTORY_MESSAGE_ID, vm.getUUID());
            uri = mContext.getContentResolver().insert(ContentDescriptor.HistoriesFiles.CONTENT_URI, values);
        }
        return uri;
    }

    /**
     * 分页加载聊天消息数据
     */
    public static List<VMessage> loadMessageByPage(int groupType, long fromUserID, long remoteUserID, int limit,
                                                   int offset) {
        if (!isTableExist(0, 0, remoteUserID))
            return null;

        String selection = "((" + HistoriesMessage.Cols.HISTORY_MESSAGE_FROM_USER_ID + "=? and "
                + HistoriesMessage.Cols.HISTORY_MESSAGE_TO_USER_ID + "=? ) or " + "("
                + HistoriesMessage.Cols.HISTORY_MESSAGE_FROM_USER_ID + "=? and "
                + HistoriesMessage.Cols.HISTORY_MESSAGE_TO_USER_ID + "=? ))  and "
                + HistoriesMessage.Cols.HISTORY_MESSAGE_GROUP_TYPE + "= ?";

        String[] args = new String[]{String.valueOf(fromUserID), String.valueOf(remoteUserID),
                String.valueOf(remoteUserID), String.valueOf(fromUserID), String.valueOf(groupType)};

        String order = HistoriesMessage.Cols.ID + " desc , "
                + HistoriesMessage.Cols.ID + " desc limit " + limit + " offset  " + offset;
        return queryMessage(selection, args, order);
    }

    /**
     * according given VMessage ID , get VMessage Object
     *
     * @param groupType
     * @param groupID
     * @param remoteID
     * @param msgId
     * @return
     */
    public static VMessage loadMessageById(int groupType, long groupID, long remoteID, long msgId) {
        if (!isTableExist(groupType, groupID, remoteID))
            return null;

        String selection = HistoriesMessage.Cols.ID + "=? ";
        String[] args = new String[]{String.valueOf(msgId)};
        String order = HistoriesMessage.Cols.ID + " desc limit 1 offset 0 ";
        List<VMessage> list = queryMessage(selection, args, order);
        if (list != null && list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    /**
     * loading user to user chating messages
     *
     * @param remoteID
     * @param msgId
     * @return
     */
    public static VMessage loadUserMessageById(long remoteID, long msgId) {
        return loadMessageById(0, 0, remoteID, msgId);
    }

    /**
     * loading crowd chating messages
     *
     * @param groupType
     * @param groupID
     * @param msgId
     * @return
     */
    public static VMessage loadGroupMessageById(int groupType, long groupID, long msgId) {
        return loadMessageById(groupType, groupID, 0, msgId);
    }

    /**
     * 根据指定用户id，加载出所有图片
     *
     * @param uid1
     * @param uid2
     * @return
     */
    public static List<VMessage> loadImageMessage(long uid1, long uid2) {
        Cursor cursor = null;
        List<VMessage> imageItems = new ArrayList<>();
        try {
            Uri uri = ContentDescriptor.HistoriesGraphic.CONTENT_URI;
            String sortOrder = ContentDescriptor.HistoriesGraphic.Cols.ID + " desc";
            String[] projection = ContentDescriptor.HistoriesGraphic.Cols.ALL_CLOS;
            String selection = "((" + ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_FROM_USER_ID + "=? and "
                    + ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_TO_USER_ID + "=? ) or " + "("
                    + ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_FROM_USER_ID + "=? and "
                    + ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_TO_USER_ID + "=? ))  and "
                    + ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_GROUP_TYPE + "= 0";
            String[] args = new String[]{String.valueOf(uid1), String.valueOf(uid2), String.valueOf(uid2),
                    String.valueOf(uid1)};
            cursor = mContext.getContentResolver().query(uri, projection, selection, args, sortOrder);

            if (cursor == null) {
                return imageItems;
            }

            if (cursor.getCount() < 0) {
                return imageItems;
            }

            VMessage current;
            while (cursor.moveToNext()) {
                int groupType = cursor.getInt(
                        cursor.getColumnIndex(ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_GROUP_TYPE));
                long groupID = cursor.getLong(
                        cursor.getColumnIndex(ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_GROUP_ID));
                long fromUserID = cursor.getLong(
                        cursor.getColumnIndex(ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_FROM_USER_ID));
                long date = cursor.getLong(
                        cursor.getColumnIndex(ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_SAVEDATE));
                String imageID = cursor
                        .getString(cursor.getColumnIndex(ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_ID));
                String imagePath = cursor
                        .getString(cursor.getColumnIndex(ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_PATH));
                User fromUser = GlobalHolder.getInstance().getUser(fromUserID);
                if (fromUser == null) {
                    V2Log.e("get null when loadImageMessage get fromUser :" + fromUserID);
                    continue;
                }
                current = new VMessage(groupType, groupID, fromUser, new Date(date));
                current.setUUID(imageID);
                new VMessageImageItem(current, imageID, imagePath, 0);
                imageItems.add(current);
            }
            return imageItems;
        } catch (Exception e) {
            e.printStackTrace();
            CrashHandler.getInstance().saveCrashInfo2File(e);
            return imageItems;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // ****************************群组操作**************************************************

    /**
     * 查询指定群组的所有聊天消息记录
     *
     * @param context
     * @param groupType
     * @param gid
     * @return
     */
    public static List<VMessage> loadGroupMessage(Context context, int groupType, long gid) {
        if (!isTableExist(groupType, gid, 0))
            return null;
        String selection = HistoriesMessage.Cols.HISTORY_MESSAGE_GROUP_ID + "=? ";
        String order = HistoriesMessage.Cols.HISTORY_MESSAGE_SAVEDATE + " desc ";
        String[] args = new String[]{gid + ""};
        return queryMessage(selection, args, order);
    }

    /**
     * 查询指定群组中聊天收发的所有图片
     *
     * @param context
     * @param type
     * @param gid
     * @return
     */
    public static List<VMessage> loadGroupImageMessage(Context context, int type, long gid) {
        if (!isTableExist(type, gid, 0))
            return null;

        List<VMessage> imageItems = new ArrayList<>();
        DataBaseContext mContext = new DataBaseContext(context);
        Cursor cursor = null;
        try {

            String sortOrder = ContentDescriptor.HistoriesGraphic.Cols.ID + " desc";
            String where = ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_GROUP_TYPE + "=? and "
                    + ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_GROUP_ID + "= ?";
            String[] args = new String[]{String.valueOf(type), String.valueOf(gid)};
            Uri uri = ContentDescriptor.HistoriesGraphic.CONTENT_URI;
            String[] projection = ContentDescriptor.HistoriesGraphic.Cols.ALL_CLOS;
            cursor = mContext.getContentResolver().query(uri, projection, where, args, sortOrder);

            if (cursor == null) {
                return imageItems;
            }

            if (cursor.getCount() < 0) {
                return imageItems;
            }

            VMessage current;
            while (cursor.moveToNext()) {
                int groupType = cursor.getInt(
                        cursor.getColumnIndex(ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_GROUP_TYPE));
                long groupID = cursor.getLong(
                        cursor.getColumnIndex(ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_GROUP_ID));
                long fromUserID = cursor.getLong(
                        cursor.getColumnIndex(ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_FROM_USER_ID));
                long date = cursor.getLong(
                        cursor.getColumnIndex(ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_SAVEDATE));
                String imageID = cursor
                        .getString(cursor.getColumnIndex(ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_ID));
                String imagePath = cursor
                        .getString(cursor.getColumnIndex(ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_PATH));
                User fromUser = GlobalHolder.getInstance().getUser(fromUserID);
                if (fromUser == null) {
                    V2Log.e("get null when loadImageMessage get fromUser :" + fromUserID);
                    continue;
                }
                current = new VMessage(groupType, groupID, fromUser, new Date(date));
                new VMessageImageItem(current, imageID, imagePath, 0);
                imageItems.add(current);
            }
            return imageItems;
        } catch (Exception e) {
            e.printStackTrace();
            CrashHandler.getInstance().saveCrashInfo2File(e);
            return imageItems;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 查询指定群组中聊天收发的所有文件
     *
     * @param type
     * @param remoteID
     * @return
     */
    public static List<VMessageFileItem> loadFileMessages(int type, long remoteID) {
        // 传两个-1是在MainActivity中需要查出文件表中所有文件而传递的
        if (type != -1 && remoteID != -1) {
            if (!isTableExist(type, remoteID, 0))
                return null;
        }

        List<VMessageFileItem> fileItems = new ArrayList<>();
        Uri uri = ContentDescriptor.HistoriesFiles.CONTENT_URI;
        String[] args;
        String where;
        String sortOrder;
        Cursor cursor = null;
        try {
            if (type == -1 && remoteID == -1) {
                sortOrder = ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SAVEDATE + " desc";
                where = ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SEND_STATE + " = ? or "
                        + ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SEND_STATE + " = ? or "
                        + ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SEND_STATE + " = ? or "
                        + ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SEND_STATE + " = ? or "
                        + ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SEND_STATE + " = ?";
                args = new String[]{String.valueOf(VMessageAbstractItem.STATE_FILE_DOWNLOADING),
                        String.valueOf(VMessageAbstractItem.STATE_FILE_UNDOWNLOAD),
                        String.valueOf(VMessageAbstractItem.STATE_FILE_SENDING),
                        String.valueOf(VMessageAbstractItem.STATE_FILE_PAUSED_SENDING),
                        String.valueOf(VMessageAbstractItem.STATE_FILE_PAUSED_DOWNLOADING)};
                cursor = mContext.getContentResolver().query(uri, null, where, args, sortOrder);
            } else {
                sortOrder = ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SAVEDATE + " desc";
                where = ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_REMOTE_USER_ID + " = ? and "
                        + ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_FROM_USER_ID + " = ? ";
                args = new String[]{String.valueOf(remoteID),
                        String.valueOf(GlobalHolder.getInstance().getCurrentUserId())};
                cursor = mContext.getContentResolver().query(uri, null, where, args, sortOrder);
            }
            if (cursor == null) {
                return fileItems;
            }

            if (cursor.getCount() < 0) {
                return fileItems;
            }

            while (cursor.moveToNext()) {
                VMessageFileItem fileItem = extractFileItem(cursor, type, remoteID);
                if (fileItem != null)
                    fileItems.add(fileItem);
            }
            return fileItems;
        } catch (Exception e) {
            e.printStackTrace();
            CrashHandler.getInstance().saveCrashInfo2File(e);
            return fileItems;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 查询指定群组中聊天中收发的文件，并转换为VCrowdFile对象集合
     *
     * @param gid
     * @param crowd
     * @return
     */
    public static List<VMessageFileItem> loadGroupFileItemConvertToVCrowdFile(long gid, CrowdGroup crowd) {
        if (crowd == null) {
            V2Log.e(TAG, "loadGroupFileItemConvertToVCrowdFile --> Given CrowdGroup is null!");
            return null;
        }

        Cursor cursor = null;
        try {

            List<VMessageFileItem> fileItems = new ArrayList<>();
            Uri uri = ContentDescriptor.HistoriesFiles.CONTENT_URI;
            String where = ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_REMOTE_USER_ID + " = ?";
            String[] args = new String[]{String.valueOf(gid)};
            String sortOrder = ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SAVEDATE + " desc";
            cursor = mContext.getContentResolver().query(uri, null, where, args, sortOrder);

            if (cursor == null || cursor.getCount() < 0) {
                return null;
            }

            while (cursor.moveToNext()) {
                VMessageFileItem fileItem = extractFileItem(cursor, V2GlobalConstants.GROUP_TYPE_CROWD, gid);
                if (fileItem != null) {
                    fileItems.add(fileItem);
                }
            }
            return fileItems;
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
     * 分页查询指定群组的聊天消息
     *
     * @param groupType
     * @param groupId
     * @param limit
     * @param offset
     * @return
     */
    public static List<VMessage> loadGroupMessageByPage(int groupType, long groupId, int limit,
                                                        int offset) {
        if (!isTableExist(groupType, groupId, 0))
            return null;

        String selection = HistoriesMessage.Cols.HISTORY_MESSAGE_GROUP_TYPE + "=? and "
                + HistoriesMessage.Cols.HISTORY_MESSAGE_GROUP_ID + "= ?";
        String[] args = new String[]{String.valueOf(groupType), String.valueOf(groupId)};
        String order = HistoriesMessage.Cols.ID + " desc limit " + limit + " offset  " + offset;
        return queryMessage(selection, args, order);
    }

    /**
     * query VMessageFileItme Object by uuid and groupType..
     *
     * @param uuid
     * @return
     */
    public static VMessageFileItem queryFileItemByID(String uuid) {

        if (TextUtils.isEmpty(uuid))
            throw new RuntimeException(
                    "MessageLoader queryFileItemByID ---> the given VMessageFileItem fileID is null");

        String selection = ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_ID + "=?";
        Cursor cursor = null;
        try {
            String[] args = new String[]{uuid};
            cursor = mContext.getContentResolver().query(ContentDescriptor.HistoriesFiles.CONTENT_URI, null, selection,
                    args, null);

            if (cursor == null)
                return null;

            if (cursor.getCount() <= 0)
                return null;

            if (cursor.moveToFirst()) {
                long fromUserID = cursor.getLong(
                        cursor.getColumnIndex(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_FROM_USER_ID));
                long remoteUserID = cursor.getLong(
                        cursor.getColumnIndex(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_REMOTE_USER_ID));
                long saveDate = cursor
                        .getLong(cursor.getColumnIndex(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SAVEDATE));
                String filePath = cursor
                        .getString(cursor.getColumnIndex(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_PATH));
                int fileState = cursor
                        .getInt(cursor.getColumnIndex(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SEND_STATE));

                int groupType;
                VMessage vm;
                if (remoteUserID != -1) {
                    Group tempGroup = GlobalHolder.getInstance().getGroupById(remoteUserID);
                    if (tempGroup == null)
                        groupType = V2GlobalConstants.GROUP_TYPE_USER;
                    else
                        groupType = V2GlobalConstants.GROUP_TYPE_CROWD;
                } else {
                    V2Log.e(TAG, "queryFileItemByID -- > Get remoteID is -1 , uuid is : " + uuid);
                    return null;
                }

                if (groupType == V2GlobalConstants.GROUP_TYPE_USER)
                    vm = new VMessage(groupType, -1, GlobalHolder.getInstance().getUser(fromUserID),
                            GlobalHolder.getInstance().getUser(remoteUserID), new Date(saveDate));
                else
                    vm = new VMessage(groupType, remoteUserID, GlobalHolder.getInstance().getUser(fromUserID), null,
                            new Date(saveDate));
                VMessageFileItem fileItem = new VMessageFileItem(vm, filePath, fileState);
                fileItem.setUuid(uuid);
                return fileItem;
            }
            return null;
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    public static SparseArray<List<VMessage>> querySearchChatMessages(String searchKey) {
        List<String> dataBaseTableCacheName = GlobalHolder.getInstance().getDataBaseTableCacheName();
        SparseArray<List<VMessage>> results = new SparseArray<>();
        String[] selectArgs = new String[]{"%" + searchKey + "%"};
        for (int i = 0; i < dataBaseTableCacheName.size(); i++) {
            String tableName = dataBaseTableCacheName.get(i);
            if (tableName.startsWith("Histories")) {
                long key;
                String[] tableInfos = tableName.split("_");
                int groupType = Integer.valueOf(tableInfos[1]);
                try {
                    if (groupType == V2GlobalConstants.GROUP_TYPE_USER) {
                        key = Integer.valueOf(tableInfos[3]);
                    } else {
                        key = Integer.valueOf(tableInfos[2]);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    return results;
                }

                Cursor cursor = null;
                try {
                    cursor = V2techBaseProvider.
                            mSQLitDatabaseHolder.rawQuery("select * from " + tableName + " where MsgPlainText like ?", selectArgs);
                    List<VMessage> searchContent = new ArrayList<>();
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            VMessage extract = extractMsg(cursor);
                            if (extract == null) {
                                V2Log.d("The extract VMessage from Cursor failed...get null , id is : " + cursor.getInt(0));
                                continue;
                            }

                            VMessage vm = XmlParser.parseForMessage(extract);
                            if (vm == null) {
                                V2Log.d("The parse VMessage from failed...get null , id is : " + cursor.getInt(0));
                                continue;
                            }
                            searchContent.add(vm);
                        }
                    }
                    results.append((int) key, searchContent);
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
        return results;
    }


    /**
     * 根据传入条件，查询聊天消息记录
     *
     * @param selection
     * @param args
     * @param sortOrder
     * @return
     */
    public static synchronized List<VMessage> queryMessage(String selection, String[] args, String sortOrder) {
        List<VMessage> vimList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(HistoriesMessage.CONTENT_URI,
                    HistoriesMessage.Cols.ALL_CLOS, selection, args, sortOrder);

            if (cursor == null) {
                return vimList;
            }

            if (cursor.getCount() < 0) {
                return vimList;
            }

            while (cursor.moveToNext()) {
                VMessage extract = extractMsg(cursor);
                if (extract == null) {
                    V2Log.d("The extract VMessage from Cursor failed...get null , id is : " + cursor.getInt(0));
                    continue;
                }

                VMessage vm = XmlParser.parseForMessage(extract);
                if (vm == null) {
                    V2Log.d("The parse VMessage from failed...get null , id is : " + cursor.getInt(0));
                    continue;
                }

                loadAudioMessageById(vm);
                loadImageMessageById(vm);
                loadFileMessageById(vm);
                vimList.add(vm);
            }
            return vimList;
        } catch (Exception e) {
            e.printStackTrace();
            CrashHandler.getInstance().saveCrashInfo2File(e);
            return vimList;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * According to given VMessage Object , delete it and ohter message (file ,
     * audio , image)
     *
     * @param context
     * @param vm
     * @param isDeleteOhter true mean delete other messages(file . audio . image)
     * @return
     */
    public static int deleteMessage(Context context, VMessage vm, boolean isDeleteOhter) {
        if (vm == null || !isTableExist(vm))
            return -1;

        DataBaseContext mContext = new DataBaseContext(context);
        int ret = mContext.getContentResolver().delete(HistoriesMessage.CONTENT_URI,
                HistoriesMessage.Cols.HISTORY_MESSAGE_ID + "=?",
                new String[]{String.valueOf(vm.getUUID())});

        if (isDeleteOhter) {
            List<VMessageAudioItem> audioItems = vm.getAudioItems();
            for (int i = 0; i < audioItems.size(); i++) {
                mContext.getContentResolver().delete(ContentDescriptor.HistoriesAudios.CONTENT_URI,
                        ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_ID + "=?",
                        new String[]{String.valueOf(audioItems.get(i).getUuid())});
            }

            List<VMessageFileItem> fileItems = vm.getFileItems();
            for (int i = 0; i < fileItems.size(); i++) {
                deleteFileItem(fileItems.get(i).getUuid());
            }

            List<VMessageImageItem> imageItems = vm.getImageItems();
            for (int i = 0; i < imageItems.size(); i++) {
                mContext.getContentResolver().delete(ContentDescriptor.HistoriesGraphic.CONTENT_URI,
                        ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_ID + "=?",
                        new String[]{String.valueOf(imageItems.get(i).getUuid())});
            }
        }
        return ret;
    }

    /**
     * delete all messages , according given args..
     *
     * @param groupType
     * @param groupID
     * @param userID
     * @param isDeleteFile For crowd group , Don't delete file record
     * @return
     */
    public static boolean deleteMessageByID(int groupType, long groupID, long userID, boolean isDeleteFile) {
//        List<String> tableNames = GlobalHolder.getInstance().getDataBaseTableCacheName();
        String sql;
        String tableName;
        if (groupType != V2GlobalConstants.GROUP_TYPE_USER)
            tableName = "Histories_" + groupType + "_" + groupID + "_0";
        else
            tableName = "Histories_0_0_" + userID;

//        if (tableNames.contains(tableName)) {
//            tableNames.remove(tableName);
        sql = "drop table " + tableName;
//        } else {
//            V2Log.e(TAG, "drop table failed...table no exists , name is : " + tableName);
//            return false;
//        }

        try {
            V2techBaseProvider.mSQLitDatabaseHolder.execSQL(sql);
        } catch (Exception e) {
            e.printStackTrace();
            V2Log.d(TAG, "May delete HistoriesMessage failed...have exception...groupType : " + groupType
                    + "  groupID : " + groupID + "  userID : " + userID);
            return false;
        }

        // 删除其他信息
        String audioCondition;
        String imageCondition;
        String fileCondition;
        String[] args;
        if (groupType == V2GlobalConstants.GROUP_TYPE_USER) {
            audioCondition = ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_GROUP_TYPE + "= ? and "
                    + ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_GROUP_ID + "= ? and "
                    + ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_REMOTE_USER_ID + "= ?";
            imageCondition = ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_GROUP_TYPE + "= ? and "
                    + ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_GROUP_ID + "= ? and "
                    + ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_REMOTE_USER_ID + "= ?";
            fileCondition = ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_REMOTE_USER_ID + "= ?";
            args = new String[]{String.valueOf(groupType), String.valueOf(groupID), String.valueOf(userID)};
        } else {
            audioCondition = ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_GROUP_TYPE + "= ? and "
                    + ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_GROUP_ID + "= ?";
            imageCondition = ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_GROUP_TYPE + "= ? and "
                    + ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_GROUP_ID + "= ? ";
            fileCondition = ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_REMOTE_USER_ID + "= ?";
            args = new String[]{String.valueOf(groupType), String.valueOf(groupID)};

            if (groupType == V2GlobalConstants.GROUP_TYPE_CROWD) {
                List<VMessageFileItem> loadFileMessages = loadFileMessages(V2GlobalConstants.GROUP_TYPE_CROWD, groupID);
                if (loadFileMessages != null) {
                    for (int i = 0; i < loadFileMessages.size(); i++) {
                        VMessageFileItem vMessageFileItem = loadFileMessages.get(i);
                        if (vMessageFileItem.getState() == VMessageAbstractItem.STATE_FILE_SENDING) {
                            FileRequest.getInstance().FileTransCloseSendFile(vMessageFileItem.getUuid());
                        }
                    }
                }

                // 删除正在上传的文件记录
                String where = "( " + ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SEND_STATE + "= ? or "
                        + ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SEND_STATE + "= ? or "
                        + ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SEND_STATE + "= ? ) and "
                        + ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_REMOTE_USER_ID + "= ? ";
                String[] arg = new String[]{String.valueOf(VMessageAbstractItem.STATE_FILE_SENDING),
                        String.valueOf(VMessageAbstractItem.STATE_FILE_PAUSED_SENDING),
                        String.valueOf(VMessageAbstractItem.STATE_FILE_SENT_FALIED), String.valueOf(groupID)};
                mContext.getContentResolver().delete(ContentDescriptor.HistoriesFiles.CONTENT_URI, where, arg);
            }
        }

        String[] tables = new String[]{ContentDescriptor.HistoriesAudios.CONTENT_URI.toString(),
                ContentDescriptor.HistoriesGraphic.CONTENT_URI.toString()};
        String[] conditions = new String[]{audioCondition, imageCondition};
        String[] names = new String[]{"audioCondition", "imageCondition"};

        for (int i = 0; i < conditions.length; i++) {
            int ret = mContext.getContentResolver().delete(Uri.parse(tables[i]), conditions[i], args);
            if (ret <= 0)
                V2Log.d(TAG, "May delete " + names[i] + " failed...groupType : " + groupType + "  groupID : " + groupID
                        + "  userID : " + userID);
        }

        if (isDeleteFile) {
            // 删除文件
            String[] fileArgs;
            if (groupType == V2GlobalConstants.GROUP_TYPE_USER)
                fileArgs = new String[]{String.valueOf(userID)};
            else
                fileArgs = new String[]{String.valueOf(groupID)};

            int ret = mContext.getContentResolver().delete(ContentDescriptor.HistoriesFiles.CONTENT_URI, fileCondition,
                    fileArgs);
            if (ret <= 0)
                V2Log.d(TAG, "May delete fileConditions failed...groupType : " + groupType + "  groupID : " + groupID
                        + "  userID : " + userID);
        }
        return true;
    }

    public static int deleteFileItem(String fileID) {
        return mContext.getContentResolver().delete(ContentDescriptor.HistoriesFiles.CONTENT_URI,
                ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_ID + "=?", new String[]{fileID});
    }

    /**
     * 修改聊天对象数据到数据库
     *
     * @param context
     * @param vm
     * @return
     */
    public static int updateChatMessageState(Context context, VMessage vm) {

        if (vm == null || !isTableExist(vm)) {
            V2Log.e(TAG, "updateChatMessageState --> get VMessage Object is null...please check it");
            return -1;
        }

        DataBaseContext mContext = new DataBaseContext(context);
        ContentValues values = new ContentValues();
        ContentResolver contentResolver = mContext.getContentResolver();
        values.put(HistoriesMessage.Cols.HISTORY_MESSAGE_TRANSTATE, vm.getState());
        String where = HistoriesMessage.Cols.HISTORY_MESSAGE_ID + "= ?";

        String[] selectionArgs;
        if (vm.getmOldUUID() != null) {
            selectionArgs = new String[]{vm.getmOldUUID()};
            values.put(HistoriesMessage.Cols.HISTORY_MESSAGE_ID, vm.getUUID());
            values.put(HistoriesMessage.Cols.HISTORY_MESSAGE_CONTENT, vm.toXml());
            vm.setmOldUUID(null);
        } else
            selectionArgs = new String[]{vm.getUUID()};

        int update = contentResolver.update(HistoriesMessage.CONTENT_URI, values, where,
                selectionArgs);
        if (update <= 0) {
            V2Log.e(TAG, "updateChatMessageState --> update chat message failed...message id is :" + "" + vm.getUUID()
                    + " and table name is : " + HistoriesMessage.CONTENT_URI);
            return -1;
        }

        List<VMessageAbstractItem> items = vm.getItems();
        if (items == null || items.size() <= 0) {
            V2Log.e(TAG, "updateChatMessageState --> get VMessageAbstractItem collection failed...is null");
            return -1;
        }

        for (int j = 0; j < items.size(); j++) {
            VMessageAbstractItem item = items.get(j);
            values.clear();
            selectionArgs = new String[]{item.getUuid()};
            switch (item.getType()) {
                case VMessageAbstractItem.ITEM_TYPE_AUDIO:
                    values.put(ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_SEND_STATE, item.getState());
                    where = ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_ID + "= ?";
                    int audioUpdate = contentResolver.update(ContentDescriptor.HistoriesAudios.CONTENT_URI, values, where,
                            selectionArgs);
                    if (audioUpdate <= 0) {
                        V2Log.e(TAG, "updateChatMessageState --> update audio chat message failed...message id is :" + ""
                                + vm.getUUID() + " and audio message id is : " + item.getUuid());
                    }
                    break;
                case VMessageAbstractItem.ITEM_TYPE_FILE:
                    values.put(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SEND_STATE, item.getState());
                    where = ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_ID + "= ?";
                    int fileUpdate = contentResolver.update(ContentDescriptor.HistoriesFiles.CONTENT_URI, values, where,
                            selectionArgs);
                    if (fileUpdate <= 0) {
                        V2Log.e(TAG, "updateChatMessageState --> update file chat message failed...message id is :" + ""
                                + vm.getUUID() + " and file message id is : " + item.getUuid());
                    }
                    break;
                case VMessageAbstractItem.ITEM_TYPE_IMAGE:
                    VMessageImageItem image = (VMessageImageItem) item;
                    if (image.getmOldUUID() != null) {
                        selectionArgs = new String[]{image.getmOldUUID()};
                        values.put(ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_ID, image.getUuid());
                        image.setmOldUUID(null);
                    }

                    values.put(ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_TRANSTATE, item.getState());
                    where = ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_ID + "= ?";

                    int imageUpdate = contentResolver.update(ContentDescriptor.HistoriesGraphic.CONTENT_URI, values, where,
                            selectionArgs);
                    if (imageUpdate <= 0) {
                        V2Log.e(TAG, "updateChatMessageState --> update image chat message failed...message id is :" + ""
                                + vm.getUUID() + " and image message id is : " + item.getUuid());
                    }
                    break;
                default:
            }
        }
        return 1;
    }

    public static int updateChatMessageState(VMessage vm) {
        if (vm == null) {
            V2Log.e(TAG, "updateChatMessageState --> get VMessage Object is null...please check it");
            return -1;
        }

        ContentValues values = new ContentValues();
        ContentResolver contentResolver = mContext.getContentResolver();
        values.put(HistoriesMessage.Cols.HISTORY_MESSAGE_TRANSTATE, vm.getState());
        String where = HistoriesMessage.Cols.HISTORY_MESSAGE_ID + "= ?";
        String[] selectionArgs = new String[]{vm.getUUID()};
        int update = contentResolver.update(HistoriesMessage.CONTENT_URI, values, where,
                selectionArgs);
        if (update <= 0) {
            V2Log.e(TAG, "updateChatMessageState --> update chat message failed...message id is :" + "" + vm.getUUID()
                    + " and table name is : " + HistoriesMessage.CONTENT_URI);
            return -1;
        }
        return 1;
    }

    /**
     * update the given audio message read state...
     *
     * @param audioItem
     * @return
     */
    public static int updateBinaryAudioItem(VMessageAudioItem audioItem) {
        if (audioItem == null)
            return -1;

        ContentValues values = new ContentValues();
        values.put(ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_SEND_STATE, audioItem.getState());
        values.put(ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_READ_STATE, audioItem.getReadState());
        String where = ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_ID + "= ?";
        String[] selectionArgs = new String[]{audioItem.getUuid()};
        return mContext.getContentResolver().update(ContentDescriptor.HistoriesAudios.CONTENT_URI, values, where,
                selectionArgs);
    }

    /**
     * update the given image message filePath...
     *
     * @param imageItem
     * @return
     */
    public static int updateBinaryImageItem(VMessageImageItem imageItem) {
        if (imageItem == null)
            return -1;

        ContentValues values = new ContentValues();
        values.put(ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_TRANSTATE,
                VMessageAbstractItem.TRANS_SENT_FALIED);
        values.put(ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_PATH, imageItem.getFilePath());
        String where = ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_ID + "= ?";
        String[] selectionArgs = new String[]{imageItem.getUuid()};
        return mContext.getContentResolver().update(ContentDescriptor.HistoriesGraphic.CONTENT_URI, values, where,
                selectionArgs);
    }

    /**
     * update VMessageFileItem Object state to failed by fileID..
     *
     * @param fileID If fileID is null , mean change all fileItem to failed...
     * @return
     */
    public static int updateFileItemStateToFailed(String fileID) {

        ContentValues values = new ContentValues();
        if (TextUtils.isEmpty(fileID))
            return -1;

        VMessageFileItem fileItem = ChatMessageProvider.queryFileItemByID(fileID);
        if (fileItem == null) {
            V2Log.e(TAG, "updateFileItemStateToFailed --> get VMessageFileItem Object failed...fileID is " + fileID);
            return -1;
        }

        if (fileItem.getState() == VMessageAbstractItem.STATE_FILE_DOWNLOADING)
            values.put(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SEND_STATE,
                    VMessageAbstractItem.STATE_FILE_DOWNLOADED_FALIED);
        else if (fileItem.getState() == VMessageAbstractItem.STATE_FILE_SENDING)
            values.put(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SEND_STATE,
                    VMessageAbstractItem.STATE_FILE_SENT_FALIED);
        else
            return -1;
        String where = ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_ID + "= ?";
        String[] selectionArgs = new String[]{fileItem.getUuid()};
        return mContext.getContentResolver().update(ContentDescriptor.HistoriesFiles.CONTENT_URI, values, where,
                selectionArgs);
    }

    /**
     * According to Given the VMessageFileItem Object , update Transing State!
     *
     * @param context
     * @param fileItem
     * @return
     */
    public static int updateFileItemState(Context context, VMessageFileItem fileItem) {

        if (fileItem == null)
            return -1;

        DataBaseContext mContext = new DataBaseContext(context);
        ContentValues values = new ContentValues();
        values.put(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SEND_STATE, fileItem.getState());
        values.put(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_ID, fileItem.getUuid());
        String where = ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_ID + "= ?";
        String[] selectionArgs = new String[]{fileItem.getUuid()};
        return mContext.getContentResolver().update(ContentDescriptor.HistoriesFiles.CONTENT_URI, values, where,
                selectionArgs);
    }

    public static int updateVMessageItemToSentFalied(Context context, VMessage vm) {
        DataBaseContext mContext = new DataBaseContext(context);
        ContentValues itemVal = new ContentValues();
        int updates = 0;
        List<VMessageAbstractItem> items = vm.getItems();
        for (VMessageAbstractItem item : items) {
            switch (item.getType()) {
                case VMessageAbstractItem.ITEM_TYPE_FILE:
                    if (vm.isLocal()) {
                        itemVal.put(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SEND_STATE,
                                VMessageAbstractItem.STATE_FILE_SENT_FALIED);
                    } else {
                        itemVal.put(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SEND_STATE,
                                VMessageAbstractItem.STATE_FILE_DOWNLOADED_FALIED);
                    }

                    updates = mContext.getContentResolver().update(ContentDescriptor.HistoriesFiles.CONTENT_URI, itemVal,
                            ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_ID + "=?",
                            new String[]{String.valueOf(item.getUuid())});
                    break;
                default:
                    break;
            }
        }
        return updates;
    }

    public static int updateVMessageItem(Context context, VMessageAbstractItem item) {
        DataBaseContext mContext = new DataBaseContext(context);
        ContentValues itemVal = new ContentValues();
        int updates = 0;
        switch (item.getType()) {
            case VMessageAbstractItem.ITEM_TYPE_FILE:
                VMessageFileItem fileItem = (VMessageFileItem) item;
                itemVal.put(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SEND_STATE, fileItem.getState());
                itemVal.put(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_PATH, fileItem.getFilePath());
                updates = mContext.getContentResolver().update(ContentDescriptor.HistoriesFiles.CONTENT_URI, itemVal,
                        ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_ID + "=?",
                        new String[]{String.valueOf(fileItem.getUuid())});
                break;
            default:
                break;
        }
        return updates;
    }

    // private static void loadVMessageItem(Context context, VMessage vm,
    // int msgType) {
    // String selection = ContentDescriptor.MessageItems.Cols.MSG_ID + "=? ";
    // String[] args = null;
    // if (msgType != 0) {
    // selection += "and " + ContentDescriptor.MessageItems.Cols.TYPE
    // + "=? ";
    // args = new String[] { vm.getId() + "", msgType + "" };
    // } else {
    // args = new String[] { vm.getId() + "" };
    // }
    //
    // Cursor cur = context.getContentResolver().query(
    // ContentDescriptor.MessageItems.CONTENT_URI,
    // ContentDescriptor.MessageItems.Cols.ALL_CLOS, selection, args,
    // ContentDescriptor.MessageItems.Cols.ID);
    //
    // while (cur.moveToNext()) {
    // int id = cur.getInt(0);
    // // content
    // String content = cur.getString(2);
    // // Item type
    // int type = cur.getInt(3);
    // // new line flag
    // int newLineFlag = cur.getInt(4);
    //
    // String uuid = cur.getString(5);
    //
    // int state = cur.getInt(6);
    //
    // VMessageAbstractItem vai = null;
    // switch (type) {
    // case VMessageAbstractItem.ITEM_TYPE_TEXT:
    // vai = new VMessageTextItem(vm, content);
    // break;
    // case VMessageAbstractItem.ITEM_TYPE_FACE:
    // vai = new VMessageFaceItem(vm, Integer.parseInt(content));
    // break;
    // case VMessageAbstractItem.ITEM_TYPE_IMAGE:
    // vai = new VMessageImageItem(vm, content);
    // case VMessageAbstractItem.ITEM_TYPE_AUDIO:
    // if (content != null && !content.isEmpty()) {
    // String[] str = content.split("\\|");
    // if (str.length > 1) {
    // vai = new VMessageAudioItem(vm, str[0],
    // Integer.parseInt(str[1]));
    // }
    // }
    // break;
    // case VMessageAbstractItem.ITEM_TYPE_FILE: {
    // String fileName = null;
    // String filePath = null;
    // long fileSize = 0;
    // if (content != null && !content.isEmpty()) {
    // String[] str = content.split("\\|");
    // if (str.length > 2) {
    // fileName = str[0];
    // filePath = str[1];
    // fileSize = Long.parseLong(str[2]);
    // }
    // }
    //
    // vai = new VMessageFileItem(vm, filePath);
    // ((VMessageFileItem) vai).setFileSize(fileSize);
    // ((VMessageFileItem) vai).setFileName(fileName);
    //
    // }
    // break;
    // case VMessageAbstractItem.ITEM_TYPE_LINK_TEXT:
    // String[] str = content.split("\\|");
    // vai = new VMessageLinkTextItem(vm, str[0], str[1]);
    // break;
    //
    // }
    // if (vai != null
    // && newLineFlag == VMessageAbstractItem.NEW_LINE_FLAG_VALUE) {
    // vai.setNewLine(true);
    // }
    //
    // vai.setId(id);
    // vai.setUuid(uuid);
    // vai.setState(state);
    // }
    //
    // cur.close();
    // }

    /**
     * 根据传入的group的type和id，查询数据库，获取最新的VMessage对象，群组
     *
     * @param groupType
     * @param groupId
     * @return
     */
    public static synchronized VMessage getNewestGroupMessage(int groupType, long groupId) {

        if (!isTableExist(groupType, groupId, 0)) {
            V2Log.e(TAG, "getNewestGroupMessage --> Get newest group message failed! table no exist!"
                    + " group id is : " + groupId);
            return null;
        }

        String selection = HistoriesMessage.Cols.HISTORY_MESSAGE_GROUP_ID + "=? ";
        String[] args = new String[]{String.valueOf(groupId)};
        String order = HistoriesMessage.Cols.HISTORY_MESSAGE_SAVEDATE + " desc limit 1 offset 0 ";
        List<VMessage> list = queryMessage(selection, args, order);
        if (list != null && list.size() > 0) {
            return list.get(0);
        } else {
            V2Log.e(TAG, "getNewestGroupMessage --> Get newest group message failed! build message is null"
                    + " group id is : " + groupId);
            return null;
        }
    }

    /**
     * 根据传入的id，查询数据库，获取最新的VMessage对象
     *
     * @param localUserID
     * @param remoteUserID
     * @return
     */
    public static synchronized VMessage getNewestMessage(long localUserID, long remoteUserID) {
        if (!isTableExist(0, 0, remoteUserID)) {
            V2Log.e(TAG, "getNewestMessage --> Get newest p2p message failed! table no exist!" + " remote user id is : "
                    + remoteUserID);
            return null;
        }

        String selection = "((" + HistoriesMessage.Cols.HISTORY_MESSAGE_FROM_USER_ID + "=? and "
                + HistoriesMessage.Cols.HISTORY_MESSAGE_TO_USER_ID + "=? ) or " + "("
                + HistoriesMessage.Cols.HISTORY_MESSAGE_FROM_USER_ID + "=? and "
                + HistoriesMessage.Cols.HISTORY_MESSAGE_TO_USER_ID + "=? ))  and "
                + HistoriesMessage.Cols.HISTORY_MESSAGE_GROUP_TYPE + "= 0 ";

        String[] args = new String[]{String.valueOf(localUserID), String.valueOf(remoteUserID),
                String.valueOf(remoteUserID), String.valueOf(localUserID)};

        String order = HistoriesMessage.Cols.HISTORY_MESSAGE_SAVEDATE + " desc, "
                + HistoriesMessage.Cols.HISTORY_MESSAGE_ID + " desc limit 1 offset 0 ";

        List<VMessage> list = queryMessage(selection, args, order);
        if (list != null && list.size() > 0) {
            return list.get(0);
        } else {
            V2Log.e(TAG, "getNewestMessage --> Get newest p2p message failed! build message is null!"
                    + " remote user id is : " + remoteUserID);
            return null;
        }
    }

    /**
     * 根据传入的id，查询数据库，获取最新的VMessage对象
     *
     * @param groupType
     * @param groupID
     * @param remoteUserID
     * @return
     */
    public static synchronized VMessage getNewestShowTimeMessage(int groupType, long groupID, long remoteUserID) {
        if (groupType == V2GlobalConstants.GROUP_TYPE_USER) {
            if (!isTableExist(0, 0, remoteUserID)) {
                V2Log.e(TAG, "getNewestShowTimeMessage --> Get newest p2p message failed! table no exist!"
                        + " remote user id is : " + remoteUserID);
                return null;
            }
        } else {
            if (!isTableExist(groupType, groupID, 0)) {
                V2Log.e(TAG, "getNewestShowTimeMessage --> Get newest p2p message failed! table no exist!"
                        + " remote user id is : " + remoteUserID);
                return null;
            }
        }

        String selection = HistoriesMessage.Cols.HISTORY_MESSAGE_SHOW_TIME + " = ? ";

        String[] args = new String[]{String.valueOf(V2GlobalConstants.MESSAGE_SHOW_TIME)};
        String order = HistoriesMessage.Cols.HISTORY_MESSAGE_SAVEDATE + " desc, "
                + HistoriesMessage.Cols.HISTORY_MESSAGE_ID + " desc limit 1 offset 0 ";

        List<VMessage> list = queryMessage(selection, args, order);
        if (list != null && list.size() > 0) {
            return list.get(0);
        } else {
            V2Log.e(TAG, "getNewestMessage --> Get newest p2p message failed! build message is null!"
                    + " remote user id is : " + remoteUserID);
            return null;
        }
    }

    /**
     * 根据传入的Cursor对象，构造一个VMessage对象
     *
     * @param cur
     * @return
     */
    private static VMessage extractMsg(Cursor cur) {
        long user1Id = cur
                .getLong(cur.getColumnIndex(HistoriesMessage.Cols.HISTORY_MESSAGE_FROM_USER_ID));
        long user2Id = cur
                .getLong(cur.getColumnIndex(HistoriesMessage.Cols.HISTORY_MESSAGE_TO_USER_ID));
        User fromUser = GlobalHolder.getInstance().getUser(user1Id);
        if (fromUser == null) {
            fromUser = new User(user1Id);
        }
        User toUser = GlobalHolder.getInstance().getUser(user2Id);
        if (toUser == null) {
            toUser = new User(user2Id);
        }

        int id = cur.getInt(0);
        // message type
        int type = cur.getInt(cur.getColumnIndex(HistoriesMessage.Cols.HISTORY_MESSAGE_GROUP_TYPE));
        // date time
        long dateString = cur
                .getLong(cur.getColumnIndex(HistoriesMessage.Cols.HISTORY_MESSAGE_SAVEDATE));
        // group id
        long groupId = cur
                .getLong(cur.getColumnIndex(HistoriesMessage.Cols.HISTORY_MESSAGE_GROUP_ID));
        // message state
        int state = cur.getInt(cur.getColumnIndex(HistoriesMessage.Cols.HISTORY_MESSAGE_TRANSTATE));
        // message id
        String uuid = cur.getString(cur.getColumnIndex(HistoriesMessage.Cols.HISTORY_MESSAGE_ID));
        // message show time
        int showTime = cur
                .getInt(cur.getColumnIndex(HistoriesMessage.Cols.HISTORY_MESSAGE_SHOW_TIME));
        String searchContent = cur.getString(cur.getColumnIndex(HistoriesMessage.Cols.HISTORY_MESSAGE_CHAT_CONTENT));

        String xml = cur.getString(cur.getColumnIndex(HistoriesMessage.Cols.HISTORY_MESSAGE_CONTENT));
        VMessage vm = new VMessage(type, groupId, fromUser, toUser, uuid, new Date(dateString));
        if (showTime == V2GlobalConstants.MESSAGE_SHOW_TIME) {
            vm.setShowTime(true);
        } else {
            vm.setShowTime(false);
        }
        vm.setId(id);
        vm.setState(state);
        vm.setmXmlDatas(xml);
        vm.setPlainText(searchContent);
        return vm;
    }

    private static VMessageFileItem extractFileItem(Cursor cursor, int groupType, long groupID) {
        int fromUserID = cursor
                .getInt(cursor.getColumnIndex(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_FROM_USER_ID));
        long date = cursor.getLong(cursor.getColumnIndex(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SAVEDATE));
        int fileState = cursor
                .getInt(cursor.getColumnIndex(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SEND_STATE));
        String uuid = cursor.getString(cursor.getColumnIndex(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_ID));
        String msgID = cursor
                .getString(cursor.getColumnIndex(ContentDescriptor.HistoriesFiles.Cols.HISTORY_MESSAGE_ID));
        String filePath = cursor
                .getString(cursor.getColumnIndex(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_PATH));
        long fileSize = cursor.getLong(cursor.getColumnIndex(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SIZE));
        User fromUser = GlobalHolder.getInstance().getUser(fromUserID);
        if (fromUser == null) {
            V2Log.e("get null when loadImageMessage get fromUser :" + fromUserID);
            return null;
        }

        VMessage current;
        if (groupType == V2GlobalConstants.GROUP_TYPE_CROWD) {
            current = new VMessage(groupType, groupID, fromUser, new Date(date));
        } else if (groupType == V2GlobalConstants.GROUP_TYPE_USER) {
            current = new VMessage(groupType, 0, fromUser, new Date(date));
        } else {
            current = new VMessage(-1, -1, fromUser, new Date(date));
        }
        current.setUUID(msgID);
        return new VMessageFileItem(current, uuid, filePath, null, fileSize, fileState, 0f, 0l, 0f, FileType.UNKNOW, 2,
                null);
    }

    public static List<VCrowdFile> convertToVCrowdFile(List<VMessageFileItem> fileItems, CrowdGroup crowd) {
        List<VCrowdFile> crowdFiles = new ArrayList<>();
        for (int i = 0; i < fileItems.size(); i++) {
            VMessageFileItem vMessageFileItem = fileItems.get(i);
            VCrowdFile file = convertToVCrowdFile(vMessageFileItem, crowd);
            crowdFiles.add(file);
        }
        return crowdFiles;
    }

    public static VCrowdFile convertToVCrowdFile(VMessageFileItem vMessageFileItem, CrowdGroup crowd) {
        VCrowdFile crowdFile = new VCrowdFile();
        crowdFile.setId(vMessageFileItem.getUuid());
        crowdFile.setPath(vMessageFileItem.getFilePath());
        crowdFile.setSize(vMessageFileItem.getFileSize());
        crowdFile.setName(vMessageFileItem.getFileName());
        crowdFile.setState(com.bizcom.vo.VFile.State.fromInt(vMessageFileItem.getState()));
        crowdFile.setProceedSize((long) vMessageFileItem.getProgress());
        crowdFile.setUploader(vMessageFileItem.getVm().getFromUser());
        crowdFile.setStartTime(vMessageFileItem.getVm().getDate());
        crowdFile.setCrowd(crowd);
        return crowdFile;
    }

    /**
     * 根据传入的VMessage对象，查询数据库，向其填充VMessageImageItem对象。
     *
     * @param vm
     * @return
     */
    private static VMessage loadImageMessageById(VMessage vm) {
        List<VMessageImageItem> imageItems = vm.getImageItems();
        Cursor mCur = null;
        try {
            String selection = ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_ID + "=? ";
            String sortOrder = ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_SAVEDATE
                    + " desc limit 1 offset 0 ";
            Uri uri = ContentDescriptor.HistoriesGraphic.CONTENT_URI;
            String[] projection = ContentDescriptor.HistoriesGraphic.Cols.ALL_CLOS;
            for (VMessageImageItem item : imageItems) {
                try {
                    String[] selectionArgs = new String[]{item.getUuid()};
                    mCur = mContext.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
                    if (mCur == null || mCur.getCount() <= 0) {
                        V2Log.e("the loading VMessageImageItem --" + item.getUuid() + "-- get null........");
                        return vm;
                    }

                    while (mCur.moveToNext()) {
                        int transState = mCur.getInt(
                                mCur.getColumnIndex(ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_TRANSTATE));
                        String filePath = mCur.getString(
                                mCur.getColumnIndex(ContentDescriptor.HistoriesGraphic.Cols.HISTORY_GRAPHIC_PATH));
                        item.setState(transState);
                        item.setFilePath(filePath);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    CrashHandler.getInstance().saveCrashInfo2File(e);
                } finally {
                    if (mCur != null)
                        mCur.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            CrashHandler.getInstance().saveCrashInfo2File(e);
        } finally {
            if (mCur != null)
                mCur.close();
        }
        return vm;
    }

    /**
     * 根据传入的VMessage对象，查询数据库，向其填充VMessageAudioItem对象。
     *
     * @param vm
     * @return
     */
    private static VMessage loadAudioMessageById(VMessage vm) {
        String selection = ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_ID + "=? ";
        String sortOrder = ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_SAVEDATE + " desc limit 1 offset 0 ";
        Uri uri = ContentDescriptor.HistoriesAudios.CONTENT_URI;
        String[] projection = ContentDescriptor.HistoriesAudios.Cols.ALL_CLOS;
        for (VMessageAudioItem item : vm.getAudioItems()) {
            Cursor mCur = null;
            try {
                String[] selectionArgs = new String[]{item.getUuid()};
                mCur = mContext.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
                if (mCur == null || mCur.getCount() <= 0) {
                    V2Log.e("the loading VMessageAudioItem --" + item.getUuid() + "-- get null........");
                    return vm;
                }

                while (mCur.moveToNext()) {
                    int readState = mCur.getInt(
                            mCur.getColumnIndex(ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_READ_STATE));
                    String filePath = mCur
                            .getString(mCur.getColumnIndex(ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_PATH));
                    int transState = mCur.getInt(
                            mCur.getColumnIndex(ContentDescriptor.HistoriesAudios.Cols.HISTORY_AUDIO_SEND_STATE));
                    item.setReadState(readState);
                    item.setState(transState);
                    item.setAudioFilePath(filePath);
                }
            } catch (Exception e) {
                e.printStackTrace();
                CrashHandler.getInstance().saveCrashInfo2File(e);
            } finally {
                if (mCur != null)
                    mCur.close();
            }
        }
        return vm;
    }

    /**
     * 根据传入的VMessage对象，查询数据库，向其填充VMessageFileItem对象。
     *
     * @param vm
     * @return
     */
    private static boolean loadFileMessageById(VMessage vm) {

        List<VMessageFileItem> fileItems = vm.getFileItems();
        String selection = ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_ID + "=? ";
        String sortOrder = ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SAVEDATE + " desc limit 1 offset 0 ";
        Uri uri = ContentDescriptor.HistoriesFiles.CONTENT_URI;
        String[] projection = ContentDescriptor.HistoriesFiles.Cols.ALL_CLOS;
        for (VMessageFileItem item : fileItems) {
            Cursor mCur = null;
            try {
                String[] selectionArgs = new String[]{item.getUuid()};
                mCur = mContext.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
                if (mCur == null || mCur.getCount() <= 0) {
                    fileItems.remove(item);
                    V2Log.e("the loading VMessageFileItem --" + item.getUuid() + "-- get null........");
                    return false;
                }

                while (mCur.moveToNext()) {
                    int fileTransState = mCur
                            .getInt(mCur.getColumnIndex(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SEND_STATE));
                    int fileSize = mCur
                            .getInt(mCur.getColumnIndex(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_SIZE));
                    String filePath = mCur
                            .getString(mCur.getColumnIndex(ContentDescriptor.HistoriesFiles.Cols.HISTORY_FILE_PATH));
                    item.setState(fileTransState);
                    item.setFileSize(fileSize);
                    // 为了兼容群文件中重名文件更改
                    item.setFilePath(filePath);
                    String name = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
                    item.setFileName(name);
                }
            } catch (Exception e) {
                e.printStackTrace();
                CrashHandler.getInstance().saveCrashInfo2File(e);
                return false;
            } finally {
                if (mCur != null)
                    mCur.close();
            }
        }
        return true;
    }

    private static boolean isTableExist(VMessage vm) {
        int groupType = vm.getMsgCode();
        long remoteID;
        if (groupType == V2GlobalConstants.GROUP_TYPE_USER) {
            if (vm.getFromUser().getmUserId() == GlobalHolder.getInstance().mCurrentUserId)
                remoteID = vm.getToUser().getmUserId();
            else
                remoteID = vm.getFromUser().getmUserId();
            return isTableExist(groupType, 0, remoteID);
        } else
            return isTableExist(groupType, vm.getGroupId(), 0);
    }

    /**
     * 查询前要判断该用户的数据库是否存在，不存在则创建
     *
     * @param groupType
     * @param groupID
     * @param remoteUserID
     * @return
     */
    public static boolean isTableExist(int groupType, long groupID, long remoteUserID) {

        String name;
        switch (groupType) {
            case V2GlobalConstants.GROUP_TYPE_USER:
                name = "Histories_0_0_" + remoteUserID;
                break;
            default:
                name = "Histories_" + groupType + "_" + groupID + "_0";
        }

        //这里先不将数据库中表寸到内存中，可能会引起获取不到的情况。
//		List<String> cacheNames = GlobalHolder.getInstance().getDataBaseTableCacheName();
//		boolean flag = true;
//		if (!cacheNames.contains(name)) {
//			// 创建表
//			boolean isCreate = ContentDescriptor.execSQLCreate(mContext, name);
//			if (isCreate) {
//				GlobalHolder.getInstance().getDataBaseTableCacheName().sendFriendToTv(name);
//				flag = true;
//			} else {
//				// 如果创建失败，则在查看是否该表已经存在，但没取到
//				boolean existTable = isExistTable(name);
//				if (existTable) {
//					GlobalHolder.getInstance().getDataBaseTableCacheName().sendFriendToTv(name);
//					flag = true;
//					V2Log.e(TAG, "this table alreadly exist ! name is : " + name);
//				} else {
//					V2Log.d(TAG, "create database fialed... name is : " + name);
//					flag = false;
//				}
//			}
//		}

        boolean existTable = isExistTable(name);
        boolean flag;
        if (!existTable) {
            // 创建表
            boolean isCreate = ContentDescriptor.execSQLCreate(name);
            flag = isCreate;
        } else {
            flag = true;
        }

        // init dataBase path
        if (flag) {
            HistoriesMessage.PATH = name;
            HistoriesMessage.NAME = name;
            ContentDescriptor.URI_MATCHER.addURI(ContentDescriptor.AUTHORITY, HistoriesMessage.PATH,
                    HistoriesMessage.TOKEN);
            ContentDescriptor.URI_MATCHER.addURI(ContentDescriptor.AUTHORITY, HistoriesMessage.PATH + "/#",
                    HistoriesMessage.TOKEN_WITH_ID);
            ContentDescriptor.URI_MATCHER.addURI(ContentDescriptor.AUTHORITY, HistoriesMessage.PATH + "/page",
                    HistoriesMessage.TOKEN_BY_PAGE);
            HistoriesMessage.CONTENT_URI = ContentDescriptor.BASE_URI.buildUpon()
                    .appendPath(HistoriesMessage.PATH).build();
        }
        return flag;
    }

    /**
     * 根据传入的表名，判断当前数据库中是否存在该表
     *
     * @param tabName 表名
     * @return
     */
    private static boolean isExistTable(String tabName) {
        Cursor cursor = null;
        try {
            String sql = "select count(*) as c from sqlite_master where type ='table' " + "and name ='" + tabName.trim()
                    + "' ";
            cursor = V2techBaseProvider.mSQLitDatabaseHolder.rawQuery(sql, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            V2Log.e("detection table " + tabName + " is failed..."); // 检测失败
            e.getStackTrace();
            return false;
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    /**
     * 确定远程用户
     *
     * @param vm
     * @return
     */
    private static long confirmRmoteID(VMessage vm) {
        // 确定远程用户
        long remote;
        if (vm.getFromUser().getmUserId() == GlobalHolder.getInstance().getCurrentUserId()) {
            if (vm.getMsgCode() == V2GlobalConstants.GROUP_TYPE_USER)
                remote = vm.getToUser().getmUserId();
            else
                remote = vm.getGroupId();
        } else {
            if (vm.getMsgCode() == V2GlobalConstants.GROUP_TYPE_USER)
                remote = vm.getFromUser().getmUserId();
            else
                remote = vm.getGroupId();
        }
        return remote;
    }
}
