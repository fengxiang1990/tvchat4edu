package com.bizcom.db;

import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.config.GlobalHolder;

/**
 * ChatFiles： TransState --传输状态 -3:等待接收，-2:正在传输, -1: 传输暂停, 0:传输成功, 其它值为传输失败
 * ReadState: 0 未读 ，1 已读
 *
 * @author
 */
public final class ContentDescriptor {

    public static String AUTHORITY;

    public static Uri BASE_URI;

    public static UriMatcher URI_MATCHER;
    public static final String BASE_OWNER_USER_ID = "OwnerUserID";
    public static final String BASE_SAVEDATE = "SaveDate";

    private ContentDescriptor() {
    }

    public static void initInstance() {
        BASE_URI = Uri.parse("content://" + AUTHORITY);
        URI_MATCHER = buildUriMatcher();
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        String augura = AUTHORITY;

        matcher.addURI(augura, HistoriesMessage.PATH, HistoriesMessage.TOKEN);
        matcher.addURI(augura, HistoriesMessage.PATH + "/#", HistoriesMessage.TOKEN_WITH_ID);
        matcher.addURI(augura, HistoriesMessage.PATH + "/page", HistoriesMessage.TOKEN_BY_PAGE);

        matcher.addURI(augura, HistoriesGraphic.PATH, HistoriesGraphic.TOKEN);
        matcher.addURI(augura, HistoriesGraphic.PATH + "/#", HistoriesGraphic.TOKEN_WITH_ID);
        matcher.addURI(augura, HistoriesGraphic.PATH + "/page", HistoriesGraphic.TOKEN_BY_PAGE);

        matcher.addURI(augura, HistoriesAudios.PATH, HistoriesAudios.TOKEN);
        matcher.addURI(augura, HistoriesAudios.PATH + "/#", HistoriesAudios.TOKEN_WITH_ID);

        matcher.addURI(augura, HistoriesFiles.PATH, HistoriesFiles.TOKEN);
        matcher.addURI(augura, HistoriesFiles.PATH + "/#", HistoriesFiles.TOKEN_WITH_ID);

        matcher.addURI(augura, RecentHistoriesMessage.PATH, RecentHistoriesMessage.TOKEN);
        matcher.addURI(augura, RecentHistoriesMessage.PATH + "/#", RecentHistoriesMessage.TOKEN_WITH_ID);

        matcher.addURI(augura, HistoriesMedia.PATH, HistoriesMedia.TOKEN);
        matcher.addURI(augura, HistoriesMedia.PATH + "/#", HistoriesMedia.TOKEN_WITH_ID);

        matcher.addURI(augura, HistoriesAddFriends.PATH, HistoriesAddFriends.TOKEN);
        matcher.addURI(augura, HistoriesAddFriends.PATH + "/#", HistoriesAddFriends.TOKEN_WITH_ID);

        matcher.addURI(augura, HistoriesCrowd.PATH, HistoriesCrowd.TOKEN);
        matcher.addURI(augura, HistoriesCrowd.PATH + "/#", HistoriesCrowd.TOKEN_WITH_ID);

        matcher.addURI(augura, HistoriesConfInvites.PATH, HistoriesConfInvites.TOKEN);
        matcher.addURI(augura, HistoriesConfInvites.PATH + "/#", HistoriesConfInvites.TOKEN_WITH_ID);
        return matcher;
    }

    /**
     * 创建好友数据库表
     *
     * @param tableName 表名
     * @return
     */
    public static boolean execSQLCreate(String tableName) {
        if (TextUtils.isEmpty(tableName)) {
            return false;
        }

        try {
            String sql = " create table " + tableName + " ( " + HistoriesMessage.Cols.ID
                    + " integer primary key AUTOINCREMENT," + HistoriesMessage.Cols.OWNER_USER_ID
                    + " bigint," + HistoriesMessage.Cols.HISTORY_MESSAGE_GROUP_TYPE + " bigint,"
                    + HistoriesMessage.Cols.HISTORY_MESSAGE_GROUP_ID + " bigint,"
                    + HistoriesMessage.Cols.HISTORY_MESSAGE_REMOTE_USER_ID + " bigint,"
                    + HistoriesMessage.Cols.HISTORY_MESSAGE_FROM_USER_ID + " bigint,"
                    + HistoriesMessage.Cols.HISTORY_MESSAGE_TO_USER_ID + " bigint,"
                    + HistoriesMessage.Cols.HISTORY_MESSAGE_ID + " nvarchar(4000),"
                    + HistoriesMessage.Cols.HISTORY_MESSAGE_CHAT_CONTENT + " nvarchar(4000),"
                    + HistoriesMessage.Cols.HISTORY_MESSAGE_CONTENT + " binary,"
                    + HistoriesMessage.Cols.HISTORY_MESSAGE_SAVEDATE + " bignit,"
                    + HistoriesMessage.Cols.HISTORY_MESSAGE_TRANSTATE + " bignit,"
                    + HistoriesMessage.Cols.HISTORY_MESSAGE_SHOW_TIME + " bignit)";
            V2techBaseProvider.mSQLitDatabaseHolder.execSQL(sql);
            GlobalHolder.getInstance().getDataBaseTableCacheName().add(tableName);
        } catch (Exception e) {
            e.getStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 聊天历史消息记录表
     *
     * @author
     */
    public static class HistoriesMessage {

        public static String PATH = "";

        public static String NAME = PATH;

        public static final int TOKEN = 1;

        public static final int TOKEN_WITH_ID = 2;

        public static final int TOKEN_BY_PAGE = 3;

        public static Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();

        public static class Cols {

            public static final String ID = BaseColumns._ID;

            public static String OWNER_USER_ID = BASE_OWNER_USER_ID;
            public static String HISTORY_MESSAGE_SAVEDATE = BASE_SAVEDATE;
            public static final String HISTORY_MESSAGE_GROUP_TYPE = "GroupType";
            public static final String HISTORY_MESSAGE_GROUP_ID = "GroupID";
            public static final String HISTORY_MESSAGE_REMOTE_USER_ID = "RemoteUserID";
            public static final String HISTORY_MESSAGE_FROM_USER_ID = "FromUserID";
            public static final String HISTORY_MESSAGE_TO_USER_ID = "ToUserID";
            public static final String HISTORY_MESSAGE_ID = "MsgID";
            public static final String HISTORY_MESSAGE_CONTENT = "MsgContent";
            public static final String HISTORY_MESSAGE_TRANSTATE = "TransState";
            public static final String HISTORY_MESSAGE_SHOW_TIME = "ShowTime";
            public static final String HISTORY_MESSAGE_CHAT_CONTENT = "MsgPlainText";

            public static final String[] ALL_CLOS = {ID, HISTORY_MESSAGE_GROUP_TYPE, HISTORY_MESSAGE_GROUP_ID,
                    HISTORY_MESSAGE_REMOTE_USER_ID, HISTORY_MESSAGE_FROM_USER_ID, HISTORY_MESSAGE_TO_USER_ID,
                    HISTORY_MESSAGE_ID, HISTORY_MESSAGE_CONTENT, HISTORY_MESSAGE_SAVEDATE, HISTORY_MESSAGE_TRANSTATE,
                    HISTORY_MESSAGE_SHOW_TIME, HISTORY_MESSAGE_CHAT_CONTENT};
        }
    }

    /**
     * 聊天收发图片记录表
     *
     * @author
     */
    public static class HistoriesGraphic {

        public static final String PATH = "ChatGraphics";

        public static final String NAME = PATH;

        public static final int TOKEN = 4;

        public static final int TOKEN_WITH_ID = 5;

        public static final int TOKEN_BY_PAGE = 6;

        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();

        public static class Cols {

            public static final String ID = BaseColumns._ID;

            public static String OWNER_USER_ID = BASE_OWNER_USER_ID;
            public static String HISTORY_GRAPHIC_SAVEDATE = BASE_SAVEDATE;
            public static final String HISTORY_GRAPHIC_GROUP_TYPE = "GroupType";
            public static final String HISTORY_GRAPHIC_GROUP_ID = "GroupID";
            public static final String HISTORY_GRAPHIC_FROM_USER_ID = "FromUserID";
            public static final String HISTORY_GRAPHIC_TO_USER_ID = "ToUserID";
            public static final String HISTORY_GRAPHIC_REMOTE_USER_ID = "RemoteUserID";
            public static final String HISTORY_GRAPHIC_ID = "GraphicID";
            public static final String HISTORY_MESSAGE_ID = "MsgID";
            public static final String HISTORY_GRAPHIC_PATH = "FileExt";
            public static final String HISTORY_GRAPHIC_TRANSTATE = "TransState";

            public static final String[] ALL_CLOS = {ID, OWNER_USER_ID, HISTORY_GRAPHIC_SAVEDATE,
                    HISTORY_GRAPHIC_GROUP_TYPE, HISTORY_GRAPHIC_GROUP_ID, HISTORY_GRAPHIC_FROM_USER_ID,
                    HISTORY_GRAPHIC_TO_USER_ID, HISTORY_GRAPHIC_REMOTE_USER_ID, HISTORY_GRAPHIC_ID, HISTORY_MESSAGE_ID,
                    HISTORY_GRAPHIC_PATH, HISTORY_GRAPHIC_TRANSTATE};
        }
    }

    /**
     * 聊天收发的语音留言记录表
     *
     * @author
     */
    public static class HistoriesAudios {

        public static final String PATH = "ChatAudios";

        public static final String NAME = PATH;

        public static final int TOKEN = 7;

        public static final int TOKEN_WITH_ID = 8;

        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();

        public static class Cols {

            public static final String ID = BaseColumns._ID;

            public static String OWNER_USER_ID = BASE_OWNER_USER_ID;
            public static String HISTORY_AUDIO_SAVEDATE = BASE_SAVEDATE;
            public static final String HISTORY_AUDIO_GROUP_TYPE = "GroupType";
            public static final String HISTORY_AUDIO_GROUP_ID = "GroupID";
            public static final String HISTORY_AUDIO_FROM_USER_ID = "FromUserID";
            public static final String HISTORY_AUDIO_TO_USER_ID = "ToUserID";
            public static final String HISTORY_AUDIO_REMOTE_USER_ID = "RemoteUserID";
            public static final String HISTORY_AUDIO_ID = "AudioID";
            public static final String HISTORY_MESSAGE_ID = "MsgID";
            public static final String HISTORY_AUDIO_PATH = "FileExt";
            public static final String HISTORY_AUDIO_SEND_STATE = "TransState";
            public static final String HISTORY_AUDIO_READ_STATE = "ReadState";
            public static final String HISTORY_AUDIO_SECOND = "AudioSeconds";

            public static final String[] ALL_CLOS = {ID, OWNER_USER_ID, HISTORY_AUDIO_SAVEDATE,
                    HISTORY_AUDIO_GROUP_TYPE, HISTORY_AUDIO_READ_STATE, HISTORY_AUDIO_GROUP_ID,
                    HISTORY_AUDIO_FROM_USER_ID, HISTORY_AUDIO_TO_USER_ID, HISTORY_AUDIO_ID, HISTORY_MESSAGE_ID,
                    HISTORY_AUDIO_PATH, HISTORY_AUDIO_SEND_STATE, HISTORY_AUDIO_SECOND, HISTORY_AUDIO_REMOTE_USER_ID};
        }
    }

    /**
     * 聊天收发的文件记录表
     *
     * @author
     */
    public static class HistoriesFiles {

        public static final String PATH = "ChatFiles";

        public static final String NAME = PATH;

        public static final int TOKEN = 9;

        public static final int TOKEN_WITH_ID = 10;

        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();

        public static class Cols {

            public static final String ID = BaseColumns._ID;

            public static String OWNER_USER_ID = BASE_OWNER_USER_ID;
            public static String HISTORY_FILE_SAVEDATE = BASE_SAVEDATE;
            public static final String HISTORY_FILE_FROM_USER_ID = "FromUserID";
            public static final String HISTORY_FILE_TO_USER_ID = "ToUserID";
            public static final String HISTORY_FILE_REMOTE_USER_ID = "RemoteUserID";
            public static final String HISTORY_FILE_ID = "FileID";
            public static final String HISTORY_MESSAGE_ID = "MsgID";
            public static final String HISTORY_FILE_PATH = "FileName";
            public static final String HISTORY_FILE_SEND_STATE = "TransState";
            // public static final String HISTORY_FILE_READ_STATE = "ReadState";
            public static final String HISTORY_FILE_SIZE = "FileSize";

            public static final String[] ALL_CLOS = {ID, OWNER_USER_ID, HISTORY_FILE_SAVEDATE,
                    HISTORY_FILE_REMOTE_USER_ID, HISTORY_FILE_FROM_USER_ID, HISTORY_FILE_TO_USER_ID, HISTORY_FILE_ID,
                    HISTORY_MESSAGE_ID, HISTORY_FILE_PATH, HISTORY_FILE_SEND_STATE, HISTORY_FILE_SIZE};
        }
    }

    /**
     * 最近联系人表
     *
     * @author
     */
    public static class RecentHistoriesMessage {

        public static final String PATH = "Recents";

        public static final String NAME = PATH;

        public static final int TOKEN = 11;

        public static final int TOKEN_WITH_ID = 12;

        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();

        public static class Cols {

            public static final String ID = BaseColumns._ID;

            public static String OWNER_USER_ID = BASE_OWNER_USER_ID;
            public static String HISTORY_RECENT_MESSAGE_SAVEDATE = BASE_SAVEDATE;
            public static final String HISTORY_RECENT_MESSAGE_GROUP_TYPE = "GroupType";
            public static final String HISTORY_RECENT_MESSAGE_USER_TYPE_ID = "GroupID";
            public static final String HISTORY_RECENT_MESSAGE_FROM_USER_ID = "FromUserID";
            public static final String HISTORY_RECENT_MESSAGE_TO_USER_ID = "ToUserID";
            public static final String HISTORY_RECENT_MESSAGE_REMOTE_USER_ID = "RemoteUserID";
            public static final String HISTORY_RECENT_MESSAGE_ID = "MsgID";
            public static final String HISTORY_RECENT_MESSAGE_CONTENT = "MsgContent";
            public static final String HISTORY_RECENT_MESSAGE_READ_STATE = "ReadState";

            public static final String[] ALL_CLOS = {ID, OWNER_USER_ID, HISTORY_RECENT_MESSAGE_SAVEDATE,
                    HISTORY_RECENT_MESSAGE_TO_USER_ID, HISTORY_RECENT_MESSAGE_GROUP_TYPE,
                    HISTORY_RECENT_MESSAGE_USER_TYPE_ID, HISTORY_RECENT_MESSAGE_FROM_USER_ID, HISTORY_RECENT_MESSAGE_ID,
                    HISTORY_RECENT_MESSAGE_CONTENT, HISTORY_RECENT_MESSAGE_READ_STATE,
                    HISTORY_RECENT_MESSAGE_REMOTE_USER_ID};
        }
    }

    /**
     * 音视频聊天记录
     *
     * @author
     */
    public static class HistoriesMedia {

        public static final String PATH = "ChatMedias";

        public static final String NAME = PATH;

        public static final int TOKEN = 13;

        public static final int TOKEN_WITH_ID = 14;

        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();

        public static class Cols {

            public static final String ID = BaseColumns._ID;

            public static String OWNER_USER_ID = BASE_OWNER_USER_ID;
            public static String HISTORY_MEDIA_SAVEDATE = BASE_SAVEDATE;
            public static final String HISTORY_MEDIA_CHAT_ID = "MediaChatID";
            public static final String HISTORY_MEDIA_FROM_USER_ID = "FromUserID";
            public static final String HISTORY_MEDIA_TO_USER_ID = "ToUserID";
            public static final String HISTORY_MEDIA_REMOTE_USER_ID = "RemoteUserID";
            public static final String HISTORY_MEDIA_TYPE = "MediaType";
            public static final String HISTORY_MEDIA_STATE = "MediaState";
            public static final String HISTORY_MEDIA_START_DATE = "StartDate";
            public static final String HISTORY_MEDIA_END_DATE = "EndDate";
            public static final String HISTORY_MEDIA_READ_STATE = "ReadState";
            public static final String CANCEL_BY_MINE = "CancelByMine";

            public static final String HISTORY_ACOUNT = "account";
            public static final String HISTORY_AVATAR_URL = "avatarUrl";
            public static final String HISTORY_NICK_NAME = "nickName";

            public static final String[] ALL_CLOS = {ID, OWNER_USER_ID, HISTORY_MEDIA_SAVEDATE, HISTORY_MEDIA_CHAT_ID,
                    HISTORY_MEDIA_FROM_USER_ID, HISTORY_MEDIA_TO_USER_ID, HISTORY_MEDIA_TYPE, HISTORY_MEDIA_STATE,
                    HISTORY_MEDIA_REMOTE_USER_ID, HISTORY_MEDIA_START_DATE, HISTORY_MEDIA_END_DATE,
                    HISTORY_MEDIA_READ_STATE, CANCEL_BY_MINE, HISTORY_ACOUNT, HISTORY_AVATAR_URL, HISTORY_NICK_NAME};
        }
    }

    /**
     * 添加好友历史记录表 ======= /** ��Ӻ�����ʷ��¼�� >>>>>>> 2670ca0... 1.增加好友
     *
     * @author
     */
    public static class HistoriesAddFriends {

        public static final String PATH = "AddFriendHistories";

        public static final String NAME = PATH;

        public static final int TOKEN = 15;

        public static final int TOKEN_WITH_ID = 16;

        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();

        public static class Cols {

            public static final String ID = BaseColumns._ID;

            public static String OWNER_USER_ID = BASE_OWNER_USER_ID;
            public static String HISTORY_FRIEND_SAVEDATE = BASE_SAVEDATE;
            public static String HISTORY_FRIEND_AUTHTYPE = "OwnerAuthType";
            public static final String HISTORY_FRIEND_FROM_USER_ID = "FromUserID";
            public static final String HISTORY_FRIEND_TO_USER_ID = "ToUserID";
            public static final String HISTORY_FRIEND_REMOTE_USER_ID = "RemoteUserID";
            public static final String HISTORY_CROWD_REMOTE_USER_NICK_NAME = "RemoteUserNickname";
            public static final String HISTORY_FRIEND_APPLY_REASON = "ApplyReason";
            public static final String HISTORY_FRIEND_REFUSE_REASON = "RefuseReason";
            public static final String HISTORY_FRIEND_STATE = "AddState";
            public static final String HISTORY_MEDIA_READ_STATE = "ReadState";

            public static final String[] ALL_CLOS = {ID, OWNER_USER_ID, HISTORY_FRIEND_SAVEDATE,
                    HISTORY_FRIEND_AUTHTYPE, HISTORY_FRIEND_FROM_USER_ID, HISTORY_FRIEND_TO_USER_ID,
                    HISTORY_FRIEND_REMOTE_USER_ID, HISTORY_FRIEND_APPLY_REASON, HISTORY_FRIEND_REFUSE_REASON,
                    HISTORY_FRIEND_STATE, HISTORY_MEDIA_READ_STATE, HISTORY_CROWD_REMOTE_USER_NICK_NAME};
        }
    }

    /**
     * 加群历史记录表
     *
     * @author
     */
    public static class HistoriesCrowd {

        public static final String PATH = "JoinCrowdHistories";

        public static final String NAME = PATH;

        public static final int TOKEN = 17;

        public static final int TOKEN_WITH_ID = 18;

        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();

        public static class Cols {

            public static final String ID = BaseColumns._ID;

            public static String OWNER_USER_ID = BASE_OWNER_USER_ID;
            public static String HISTORY_CROWD_SAVEDATE = BASE_SAVEDATE;
            public static final String HISTORY_CROWD_ID = "CrowdID";
            public static final String HISTORY_CROWD_AUTHTYPE = "CrowdAuthType";
            public static final String HISTORY_CROWD_FROM_USER_ID = "FromUserID";
            public static final String HISTORY_CROWD_REMOTE_USER_ID = "RemoteUserID";
            public static final String HISTORY_CROWD_TO_USER_ID = "ToUserID";
            public static final String HISTORY_CROWD_REMOTE_USER_NICK_NAME = "RemoteUserNickname ";
            public static final String HISTORY_CROWD_APPLY_REASON = "ApplyReason";
            public static final String HISTORY_CROWD_REFUSE_REASON = "RefuseReason";
            public static final String HISTORY_CROWD_STATE = "JoinState";
            public static final String HISTORY_CROWD_READ_STATE = "ReadState";
            public static final String HISTORY_CROWD_RECEIVER_STATE = "ReceiveState";
            public static final String HISTORY_CROWD_BASE_INFO = "CrowdXml";

            public static final String[] ALL_CLOS = {ID, OWNER_USER_ID, HISTORY_CROWD_SAVEDATE, HISTORY_CROWD_AUTHTYPE,
                    HISTORY_CROWD_REMOTE_USER_NICK_NAME, HISTORY_CROWD_ID, HISTORY_CROWD_FROM_USER_ID,
                    HISTORY_CROWD_REMOTE_USER_ID, HISTORY_CROWD_APPLY_REASON, HISTORY_CROWD_REFUSE_REASON,
                    HISTORY_CROWD_STATE, HISTORY_CROWD_READ_STATE, HISTORY_CROWD_TO_USER_ID,
                    HISTORY_CROWD_RECEIVER_STATE, HISTORY_CROWD_BASE_INFO};
        }
    }

    /**
     * 会议邀请历史记录表
     *
     * @author
     */
    public static class HistoriesConfInvites {

        public static final String PATH = "ConfInvites";

        public static final String NAME = PATH;

        public static final int TOKEN = 19;

        public static final int TOKEN_WITH_ID = 20;

        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();

        public static class Cols {

            public static final String ID = BaseColumns._ID;

            public static String OWNER_USER_ID = BASE_OWNER_USER_ID;
            public static String HISTORY_CONFINVITE_SAVEDATE = BASE_SAVEDATE;
            public static final String HISTORY_CONFINVITE_ID = "ConfID";
            public static final String HISTORY_CONFINVITE_REMOTE_USER_ID = "RemoteUserID";
            public static final String HISTORY_CONFINVITE_USER_BASE_INFO = "RemoteUserXml";
            public static final String HISTORY_CONFINVITE_CONF_BASE_INFO = "ConfXml";
            public static final String HISTORY_CONFINVITE_READ_STATE = "ReadState";

            public static final String[] ALL_CLOS = {ID, OWNER_USER_ID, HISTORY_CONFINVITE_SAVEDATE,
                    HISTORY_CONFINVITE_REMOTE_USER_ID, HISTORY_CONFINVITE_USER_BASE_INFO,
                    HISTORY_CONFINVITE_CONF_BASE_INFO, HISTORY_CONFINVITE_READ_STATE};
        }
    }
}
