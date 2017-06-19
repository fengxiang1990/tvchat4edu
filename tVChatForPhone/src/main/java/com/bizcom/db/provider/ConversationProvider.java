package com.bizcom.db.provider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.bizcom.db.ContentDescriptor;
import com.bizcom.db.ContentDescriptor.HistoriesConfInvites;
import com.bizcom.util.CrashHandler;
import com.bizcom.util.MessageUtil;
import com.bizcom.util.V2Log;
import com.bizcom.util.XmlAttributeExtractor;
import com.bizcom.vo.ConferenceConversation;
import com.bizcom.vo.ConferenceGroup;
import com.bizcom.vo.ContactConversation;
import com.bizcom.vo.Conversation;
import com.bizcom.vo.CrowdConversation;
import com.bizcom.vo.CrowdGroup;
import com.bizcom.vo.DepartmentConversation;
import com.bizcom.vo.DiscussionConversation;
import com.bizcom.vo.DiscussionGroup;
import com.bizcom.vo.Group;
import com.bizcom.vo.OrgGroup;
import com.bizcom.vo.User;
import com.bizcom.vo.meesage.VMessage;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class ConversationProvider extends DatabaseProvider {

    /**
     * 向数据库插入新的消息对象
     *
     * @param vm
     */
    public static void saveConversation(VMessage vm) {

        if (vm == null) {
            V2Log.e("ConversationsTabFragment", "Save Conversation Failed... Because given VMessage Object is null!");
            return;
        }

        long remoteID = 0;
        int readState = 0;
        switch (vm.getMsgCode()) {
            case V2GlobalConstants.GROUP_TYPE_USER:
                if (vm.getFromUser() == null || vm.getToUser() == null) {
                    V2Log.e("ConversationsTabFragment",
                            "Save Conversation Failed... Because getFromeUser or getToUser is null"
                                    + "for given VMessage Object ... id is : " + vm.getId());
                    return;
                }

                if (vm.getFromUser().getmUserId() == GlobalHolder.getInstance().getCurrentUserId()) {
                    remoteID = vm.getToUser().getmUserId();
                    readState = V2GlobalConstants.READ_STATE_READ;
                } else {
                    remoteID = vm.getFromUser().getmUserId();
                    readState = V2GlobalConstants.READ_STATE_READ;
                }

                boolean userConversation = queryUserConversation(remoteID);
                if (userConversation) {
                    V2Log.e("ConversationsTabFragment",
                            "Save Conversation Failed... Because the Conversation is already exist!" + "remoteUserID is : "
                                    + remoteID);
                    return;
                }
                break;
            default:
                boolean groupConversation = queryGroupConversation(vm.getMsgCode(), vm.getGroupId());
                if (groupConversation) {
                    V2Log.e("ConversationsTabFragment",
                            "Save Conversation Failed... Because the Conversation is already exist!" + "groupType is : "
                                    + vm.getMsgCode() + " groupID is : " + vm.getGroupId());
                    return;
                }
                break;
        }

        ContentValues conCv = new ContentValues();
        conCv.put(ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_FROM_USER_ID,
                vm.getFromUser().getmUserId());
        conCv.put(ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_REMOTE_USER_ID, remoteID);
        conCv.put(ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_GROUP_TYPE, vm.getMsgCode());
        conCv.put(ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_READ_STATE, readState);
        conCv.put(ContentDescriptor.RecentHistoriesMessage.Cols.OWNER_USER_ID,
                GlobalHolder.getInstance().getCurrentUserId());
        conCv.put(ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_TO_USER_ID,
                vm.getToUser().getmUserId());
        conCv.put(ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_USER_TYPE_ID, vm.getGroupId());
        conCv.put(ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_CONTENT, vm.getmXmlDatas());
        conCv.put(ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_ID, vm.getUUID());
        conCv.put(ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_SAVEDATE, vm.getmDateLong());
        mContext.getContentResolver().insert(ContentDescriptor.RecentHistoriesMessage.CONTENT_URI, conCv);
    }

    /**
     * groupInfo =
     * <conf createuserid='1117' id='1514482563582' starttime='1448256369'
     * subject='11'/> userInfo = <user id='1117' uetype='1'/> additInfo =
     *
     * @param conference
     */
    public static void saveConfInviteConversation(ConferenceGroup conference, User inviateUser, Date mInviteDate,
                                                  int readState) {
        User ownerUser = conference.getOwnerUser();
        String remoteUserXml = "<user id='" + inviateUser.getmUserId() + "' uetype='1'/>";
        String confXml = "<conf createuserid='" + ownerUser.getmUserId() + "' id='" + conference.getGroupID()
                + "' starttime='" + conference.getCreateDate().getTime() + "' subject='" + conference.getName() + "'/>";
        ContentValues conCv = new ContentValues();
        conCv.put(ContentDescriptor.HistoriesConfInvites.Cols.OWNER_USER_ID, conference.getOwnerUser().getmUserId());
        conCv.put(ContentDescriptor.HistoriesConfInvites.Cols.HISTORY_CONFINVITE_ID, conference.getGroupID());
        conCv.put(ContentDescriptor.HistoriesConfInvites.Cols.HISTORY_CONFINVITE_REMOTE_USER_ID,
                inviateUser.getmUserId());
        conCv.put(ContentDescriptor.HistoriesConfInvites.Cols.HISTORY_CONFINVITE_USER_BASE_INFO, remoteUserXml);
        conCv.put(ContentDescriptor.HistoriesConfInvites.Cols.HISTORY_CONFINVITE_CONF_BASE_INFO, confXml);
        conCv.put(ContentDescriptor.HistoriesConfInvites.Cols.HISTORY_CONFINVITE_READ_STATE, readState);
        conCv.put(ContentDescriptor.HistoriesConfInvites.Cols.HISTORY_CONFINVITE_SAVEDATE, mInviteDate.getTime());
        mContext.getContentResolver().insert(ContentDescriptor.HistoriesConfInvites.CONTENT_URI, conCv);
    }

    /**
     * loading the department conversation
     *
     * @return
     */
    public static List<DepartmentConversation> loadDepartConversation() {

        List<DepartmentConversation> lists = new ArrayList<>();
        Cursor cursor = null;
        try {

            String where = ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_GROUP_TYPE + "= ?";
            String[] args = new String[]{String.valueOf(V2GlobalConstants.GROUP_TYPE_DEPARTMENT)};
            cursor = mContext.getContentResolver().query(ContentDescriptor.RecentHistoriesMessage.CONTENT_URI,
                    ContentDescriptor.RecentHistoriesMessage.Cols.ALL_CLOS, where, args,
                    ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_SAVEDATE + " desc");

            if (cursor == null || cursor.getCount() < 0) {
                V2Log.e("ConversationsTabFragment", "loading department conversation get zero");
                return lists;
            }

            DepartmentConversation depart;
            while (cursor.moveToNext()) {
                long groupID = cursor.getLong(cursor.getColumnIndex(
                        ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_USER_TYPE_ID));
                Group department = GlobalHolder.getInstance().findGroupById(groupID);
                Group group;
                if (department != null) {
                    group = department;
                } else {
                    group = new OrgGroup(groupID, "");
                }
                depart = new DepartmentConversation(group);
                depart.setReadFlag(V2GlobalConstants.READ_STATE_READ);
                lists.add(depart);
            }
            return lists;
        } catch (Exception e) {
            e.printStackTrace();
            CrashHandler.getInstance().saveCrashInfo2File(e);
            return lists;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Depending on the type of organization, query the database fill the
     * specified collection
     *
     * @param mConvList
     * @return
     */
    public static List<Conversation> loadUserConversation(List<Conversation> mConvList) {
        Cursor mCur = null;
        try {
            mCur = mContext.getContentResolver().query(ContentDescriptor.RecentHistoriesMessage.CONTENT_URI,
                    ContentDescriptor.RecentHistoriesMessage.Cols.ALL_CLOS, null, null,
                    ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_SAVEDATE + " desc");

            if (mCur == null || mCur.getCount() <= 0) {
                V2Log.e("TabFragmentMessage", "loadUserConversation --> loading department conversation get zero");
                return mConvList;
            }

            while (mCur.moveToNext()) {
                Conversation cov = extractConversation(mContext, mCur);
                mConvList.add(cov);
            }
        } catch (Exception e) {
            V2Log.e("TabFragmentMessage", "loadUserConversation --> loading... message exception : " + e.getLocalizedMessage());
        } finally {
            if (mCur != null)
                mCur.close();
        }
        return mConvList;
    }

    public static List<Conversation> loadConfInviteConversation(List<Conversation> mConvList) {
        Cursor mCur = null;
        try {
            mCur = mContext.getContentResolver().query(ContentDescriptor.HistoriesConfInvites.CONTENT_URI,
                    ContentDescriptor.HistoriesConfInvites.Cols.ALL_CLOS, null, null,
                    ContentDescriptor.HistoriesConfInvites.Cols.HISTORY_CONFINVITE_SAVEDATE + " desc");

            if (mCur == null || mCur.getCount() <= 0) {
                V2Log.e("TabFragmentMessage", "loadConfInviteConversation --> loading department conversation get zero");
                return mConvList;
            }

            while (mCur.moveToNext()) {
                Conversation cov = extractConfInviteConversation(mCur);
                mConvList.add(cov);
            }
        } catch (Exception e) {
            V2Log.e("TabFragmentMessage", "loadConfInviteConversation --> loading... conf invite exception : " + e.getLocalizedMessage());
        } finally {
            if (mCur != null)
                mCur.close();
        }
        return mConvList;
    }

    /**
     * remove user or department conversation from databases
     *
     * @param mContext
     * @param cov
     */
    public static void deleteConversation(Context mContext, Conversation cov) {
        if (cov == null)
            return;

        String where = "";
        switch (cov.getType()) {
            case Conversation.TYPE_CONTACT:
                where = ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_REMOTE_USER_ID + "=? and "
                        + ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_GROUP_TYPE + "=?";
                break;
            case Conversation.TYPE_DEPARTMENT:
            case Conversation.TYPE_GROUP:
            case V2GlobalConstants.GROUP_TYPE_DISCUSSION:
                where = ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_USER_TYPE_ID + "=? and "
                        + ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_GROUP_TYPE + "=?";
                break;
            case Conversation.TYPE_CONFERNECE:
                where = ContentDescriptor.HistoriesConfInvites.Cols.HISTORY_CONFINVITE_ID + "=?";
                mContext.getContentResolver().delete(ContentDescriptor.HistoriesConfInvites.CONTENT_URI, where,
                        new String[]{String.valueOf(cov.getExtId())});
                return;
        }
        mContext.getContentResolver().delete(ContentDescriptor.RecentHistoriesMessage.CONTENT_URI, where,
                new String[]{String.valueOf(cov.getExtId()), String.valueOf(cov.getType())});
    }

    public static boolean queryGroupConversation(int groupType, long groupID) {
        Cursor mCur = null;
        try {
            mCur = mContext.getContentResolver().query(ContentDescriptor.RecentHistoriesMessage.CONTENT_URI,
                    ContentDescriptor.RecentHistoriesMessage.Cols.ALL_CLOS,
                    ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_GROUP_TYPE + "= ? and "
                            + ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_USER_TYPE_ID
                            + " = ?",
                    new String[]{String.valueOf(groupType), String.valueOf(groupID)},
                    ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_SAVEDATE + " desc");
            return !(mCur == null || mCur.getCount() <= 0);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (mCur != null)
                mCur.close();
        }
    }

    public static boolean queryUserConversation(long remoteUserID) {
        Cursor mCur = null;
        try {
            mCur = mContext.getContentResolver().query(ContentDescriptor.RecentHistoriesMessage.CONTENT_URI,
                    ContentDescriptor.RecentHistoriesMessage.Cols.ALL_CLOS,
                    ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_GROUP_TYPE + "= ? and "
                            + ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_REMOTE_USER_ID
                            + " = ? ",
                    new String[]{String.valueOf(V2GlobalConstants.GROUP_TYPE_USER), String.valueOf(remoteUserID)},
                    ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_SAVEDATE + " desc");
            return !(mCur == null || mCur.getCount() <= 0);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (mCur != null)
                mCur.close();
        }
    }

    /**
     * update conversation read state
     *
     * @param cov
     * @param readState
     * @return
     */
    public static int updateConversationToDatabase(Conversation cov, int readState) {
        if (cov == null)
            return -1;

        String where;
        String[] selectionArgs;
        switch (cov.getType()) {
            case Conversation.TYPE_CONFERNECE:
                ConferenceConversation conf = (ConferenceConversation) cov;
                where = ContentDescriptor.HistoriesConfInvites.Cols.HISTORY_CONFINVITE_REMOTE_USER_ID + "= ?";
                selectionArgs = new String[]{String.valueOf(conf.getInviteUser().getmUserId())};
                break;
            case Conversation.TYPE_CONTACT:
                where = ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_REMOTE_USER_ID + "= ?";
                selectionArgs = new String[]{String.valueOf(cov.getExtId())};
                break;
            case Conversation.TYPE_VOICE_MESSAGE:
                return -1;
            default:
                where = ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_GROUP_TYPE + "= ? and "
                        + ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_USER_TYPE_ID + "= ?";
                selectionArgs = new String[]{String.valueOf(cov.getType()), String.valueOf(cov.getExtId())};
                break;
        }

        ContentValues values = new ContentValues();
        if (cov.getType() == Conversation.TYPE_CONFERNECE) {
//			if (cov.getDateLong() != 0) {
//				values.put(ContentDescriptor.HistoriesConfInvites.Cols.HISTORY_CONFINVITE_SAVEDATE, cov.getDateLong());
//			}
            values.put(ContentDescriptor.HistoriesConfInvites.Cols.HISTORY_CONFINVITE_READ_STATE, readState);
            return mContext.getContentResolver().update(ContentDescriptor.HistoriesConfInvites.CONTENT_URI, values,
                    where, selectionArgs);

        } else {
            if (cov.getDateLong() != 0) {
                values.put(ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_SAVEDATE,
                        cov.getDateLong());
            }
            values.put(ContentDescriptor.RecentHistoriesMessage.Cols.HISTORY_RECENT_MESSAGE_READ_STATE, readState);
            return mContext.getContentResolver().update(ContentDescriptor.RecentHistoriesMessage.CONTENT_URI, values,
                    where, selectionArgs);
        }
    }

    /**
     * Depending on the Cursor Object to extract the Conversation Object.
     *
     * @param mContext
     * @param cur
     * @return
     */
    private static Conversation extractConversation(Context mContext, Cursor cur) {

        Conversation cov;
        VMessage vm;
        int groupType = cur.getInt(cur.getColumnIndex("GroupType"));
        switch (groupType) {
            case V2GlobalConstants.GROUP_TYPE_CROWD:
            case V2GlobalConstants.GROUP_TYPE_DISCUSSION:
            case V2GlobalConstants.GROUP_TYPE_DEPARTMENT:
                long groupID = cur.getLong(cur.getColumnIndex("GroupID"));
                Group group = GlobalHolder.getInstance().getGroupById(groupID);
                if (group == null) {
                    V2Log.e("TabFragmentMessage", "ConversationProvider:extractConversation ---> get Group is null , id is :" + groupID);
                }

                switch (groupType) {
                    case V2GlobalConstants.GROUP_TYPE_CROWD:
                        group = new CrowdGroup(groupID, null, null);
                        cov = new CrowdConversation(group);
                        break;
                    case V2GlobalConstants.GROUP_TYPE_DEPARTMENT:
                        group = new OrgGroup(groupID, null);
                        cov = new DepartmentConversation(group);
                        break;
                    case V2GlobalConstants.GROUP_TYPE_DISCUSSION:
                        group = new DiscussionGroup(groupID, null, null);
                        cov = new DiscussionConversation(group);
                        break;
                    default:
                        throw new RuntimeException(
                                "ConversationProvider:extractConversation ---> invalid groupType : " + groupType);
                }
                vm = ChatMessageProvider.getNewestGroupMessage(groupType, groupID);
                if (vm == null)
                    V2Log.e("TabFragmentMessage", "ConversationProvider:extractConversation ---> get Newest VMessage is null , update failed , id is :"
                            + groupID);
                break;
            case V2GlobalConstants.GROUP_TYPE_USER:
                long extId = cur.getLong(cur.getColumnIndex("RemoteUserID"));
                cov = new ContactConversation(extId);
                vm = ChatMessageProvider.getNewestMessage(GlobalHolder.getInstance().getCurrentUserId(), extId);
                if (vm == null)
                    V2Log.e("TabFragmentMessage", "ConversationProvider:extractConversation ---> get Newest VMessage is null , update failed , id is :"
                            + extId);
                break;
            default:
                throw new RuntimeException(
                        "ConversationProvider:extractConversation ---> invalid groupType : " + groupType);
        }
        int readState = cur.getInt(cur.getColumnIndex("ReadState"));
        if (vm != null) {
            cov.setDate(vm.getDate());
            CharSequence newMessage = MessageUtil.getMixedConversationContent(mContext, vm);
            cov.setMsg(newMessage);
            cov.setReadFlag(readState);
        }
        return cov;
    }

    /**
     * Depending on the Cursor Object to extract the Conversation Object.
     *
     * @param cur
     * @return
     */
    private static Conversation extractConfInviteConversation(Cursor cur) {
        long mGId = cur.getLong(cur.getColumnIndex(HistoriesConfInvites.Cols.HISTORY_CONFINVITE_ID));
        long remoteUserID = cur
                .getLong(cur.getColumnIndex(HistoriesConfInvites.Cols.HISTORY_CONFINVITE_REMOTE_USER_ID));
        String remoteUserXml = cur
                .getString(cur.getColumnIndex(HistoriesConfInvites.Cols.HISTORY_CONFINVITE_USER_BASE_INFO));
        String remoteConfXml = cur
                .getString(cur.getColumnIndex(HistoriesConfInvites.Cols.HISTORY_CONFINVITE_CONF_BASE_INFO));
        int readState = cur.getInt(cur.getColumnIndex(HistoriesConfInvites.Cols.HISTORY_CONFINVITE_READ_STATE));
        long saveDate = cur.getLong(cur.getColumnIndex(HistoriesConfInvites.Cols.HISTORY_CONFINVITE_SAVEDATE));

        String mName = XmlAttributeExtractor.extractAttribute(remoteConfXml, "subject");
        long mInviteID = Long.valueOf(XmlAttributeExtractor.extractAttribute(remoteUserXml, "id"));
        User user = new User(remoteUserID);
        User inviteUser = new User(mInviteID);
        Date createDate = new Date(
                Long.valueOf(XmlAttributeExtractor.extractAttribute(remoteConfXml, "starttime")) * 1000);
        ConferenceGroup conference = new ConferenceGroup(mGId, mName, user, createDate, user);
        ConferenceConversation con = new ConferenceConversation(conference, true);
        con.setInviteUser(inviteUser);
        con.setInviteDate(new Date(saveDate));
        con.setReadFlag(readState);
        return con;
    }
}
