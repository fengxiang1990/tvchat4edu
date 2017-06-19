package com.bizcom.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.bizcom.util.V2Log;
import com.config.GlobalHolder;

import java.util.List;

public class V2techBaseProvider extends ContentProvider {

    public static String AUTHORITY;
    /*
     * Defines a handle to the database helper object. The MainDatabaseHelper
     * class is defined in a following snippet.
     */
    public static SQLiteDatabase mSQLitDatabaseHolder;
    private static DataBaseContext mContext;

    @Override
    public boolean onCreate() {
        return true;
    }

    public static void init(Context context) {
        mContext = new DataBaseContext(context);
        V2TechDBHelper mOpenHelper = V2TechDBHelper.getInstance(mContext);
        mSQLitDatabaseHolder = mOpenHelper.getWritableDatabase();
    }

    public static void initDataBaseCache() {
        String path = mSQLitDatabaseHolder.getPath();
        V2Log.d("current query database path : " + path);
        Cursor cursor = null;
        try {
            cursor = mSQLitDatabaseHolder.rawQuery("select name from sqlite_master where type ='table'", null);
            if (cursor != null) {
                List<String> dataBaseTableCacheName = GlobalHolder.getInstance().getDataBaseTableCacheName();
                while (cursor.moveToNext()) {
                    // 遍历出表名
                    String name = cursor.getString(0);
                    V2Log.d("iteration DataBase table name : " + name);
                    dataBaseTableCacheName.add(name);
                }
            } else
                throw new RuntimeException("init DataBase table names failed.. get null , please check");
        } catch (Exception e) {
            throw new RuntimeException(
                    "init DataBase table names failed.. get null , please check" + e.getLocalizedMessage());
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionsArgs) {
        int ret = 0;
        int token = ContentDescriptor.URI_MATCHER.match(uri);
        String table;
        switch (token) {
            case ContentDescriptor.HistoriesMessage.TOKEN:
                table = ContentDescriptor.HistoriesMessage.NAME;
                break;
            case ContentDescriptor.HistoriesGraphic.TOKEN:
                table = ContentDescriptor.HistoriesGraphic.NAME;
                break;
            case ContentDescriptor.RecentHistoriesMessage.TOKEN:
                table = ContentDescriptor.RecentHistoriesMessage.NAME;
                break;
            case ContentDescriptor.HistoriesAudios.TOKEN:
                table = ContentDescriptor.HistoriesAudios.NAME;
                break;
            case ContentDescriptor.HistoriesMedia.TOKEN:
                table = ContentDescriptor.HistoriesMedia.NAME;
                break;
            case ContentDescriptor.HistoriesFiles.TOKEN:
                table = ContentDescriptor.HistoriesFiles.NAME;
                break;
            case ContentDescriptor.HistoriesAddFriends.TOKEN:
                table = ContentDescriptor.HistoriesAddFriends.NAME;
                break;
            case ContentDescriptor.HistoriesCrowd.TOKEN:
                table = ContentDescriptor.HistoriesCrowd.NAME;
                break;
            case ContentDescriptor.HistoriesConfInvites.TOKEN:
                table = ContentDescriptor.HistoriesConfInvites.NAME;
                break;
            default:
                throw new RuntimeException("Does not support operation ：" + token + " uri is : " + uri);
        }
        if (table != null) {
            ret = mSQLitDatabaseHolder.delete(table, selection, selectionsArgs);
            mContext.getContentResolver().notifyChange(uri, null);
        }
        return ret;
    }

    @Override
    public String getType(Uri arg0) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri newUri;
        long id;
        int token = ContentDescriptor.URI_MATCHER.match(uri);
        switch (token) {
            case ContentDescriptor.HistoriesMessage.TOKEN:
                id = mSQLitDatabaseHolder.insert(ContentDescriptor.HistoriesMessage.NAME, null, values);
                newUri = ContentDescriptor.HistoriesMessage.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
                break;
            case ContentDescriptor.HistoriesGraphic.TOKEN:
                id = mSQLitDatabaseHolder.insert(ContentDescriptor.HistoriesGraphic.NAME, null, values);
                newUri = ContentDescriptor.HistoriesGraphic.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
                break;
            case ContentDescriptor.RecentHistoriesMessage.TOKEN:
                id = mSQLitDatabaseHolder.insert(ContentDescriptor.RecentHistoriesMessage.NAME, null, values);
                newUri = ContentDescriptor.RecentHistoriesMessage.CONTENT_URI.buildUpon().appendPath(String.valueOf(id))
                        .build();
                break;
            case ContentDescriptor.HistoriesAudios.TOKEN:
                id = mSQLitDatabaseHolder.insert(ContentDescriptor.HistoriesAudios.NAME, null, values);
                newUri = ContentDescriptor.HistoriesAudios.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
                break;
            case ContentDescriptor.HistoriesMedia.TOKEN:
                id = mSQLitDatabaseHolder.insert(ContentDescriptor.HistoriesMedia.NAME, null, values);
                newUri = ContentDescriptor.HistoriesMedia.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
                break;
            case ContentDescriptor.HistoriesFiles.TOKEN:
                id = mSQLitDatabaseHolder.insert(ContentDescriptor.HistoriesFiles.NAME, null, values);
                newUri = ContentDescriptor.HistoriesFiles.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
                break;
            case ContentDescriptor.HistoriesAddFriends.TOKEN:
                id = mSQLitDatabaseHolder.insert(ContentDescriptor.HistoriesAddFriends.NAME, null, values);
                newUri = ContentDescriptor.HistoriesAddFriends.CONTENT_URI.buildUpon().appendPath(String.valueOf(id))
                        .build();
                break;
            case ContentDescriptor.HistoriesCrowd.TOKEN:
                id = mSQLitDatabaseHolder.insert(ContentDescriptor.HistoriesCrowd.NAME, null, values);
                newUri = ContentDescriptor.HistoriesCrowd.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
                break;
            case ContentDescriptor.HistoriesConfInvites.TOKEN:
                id = mSQLitDatabaseHolder.insert(ContentDescriptor.HistoriesConfInvites.NAME, null, values);
                newUri = ContentDescriptor.HistoriesConfInvites.CONTENT_URI.buildUpon().appendPath(String.valueOf(id))
                        .build();
                break;
            default:
                throw new RuntimeException("Does not support operation ：" + token + " uri is : " + uri);
        }
        mContext.getContentResolver().notifyChange(newUri, null);
        return newUri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        int token = ContentDescriptor.URI_MATCHER.match(uri);
        String tableName = null;
        switch (token) {
            case ContentDescriptor.HistoriesMessage.TOKEN:
                // qb.setTables(ContentDescriptor.HistoriesMessage.NAME);
                tableName = ContentDescriptor.HistoriesMessage.NAME;
                break;
            case ContentDescriptor.HistoriesMessage.TOKEN_WITH_ID:
                // qb.setTables(ContentDescriptor.HistoriesMessage.NAME);
                tableName = ContentDescriptor.HistoriesMessage.NAME;
                selection = ContentDescriptor.HistoriesMessage.Cols.ID + "=? ";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                break;
            case ContentDescriptor.HistoriesMessage.TOKEN_BY_PAGE:
                break;
            case ContentDescriptor.HistoriesGraphic.TOKEN:
                tableName = ContentDescriptor.HistoriesGraphic.NAME;
                break;
            case ContentDescriptor.HistoriesGraphic.TOKEN_WITH_ID:
                tableName = ContentDescriptor.HistoriesGraphic.NAME;
                selection = ContentDescriptor.HistoriesGraphic.Cols.ID + "=? ";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                break;
            case ContentDescriptor.HistoriesGraphic.TOKEN_BY_PAGE:
                break;
            case ContentDescriptor.HistoriesAudios.TOKEN:
                tableName = ContentDescriptor.HistoriesAudios.NAME;
                break;
            case ContentDescriptor.HistoriesFiles.TOKEN:
                tableName = ContentDescriptor.HistoriesFiles.NAME;
                break;
            case ContentDescriptor.RecentHistoriesMessage.TOKEN:
                tableName = ContentDescriptor.RecentHistoriesMessage.NAME;
                break;
            case ContentDescriptor.HistoriesMedia.TOKEN:
                tableName = ContentDescriptor.HistoriesMedia.NAME;
                break;
            case ContentDescriptor.HistoriesAddFriends.TOKEN:
                tableName = ContentDescriptor.HistoriesAddFriends.NAME;
                break;
            case ContentDescriptor.HistoriesCrowd.TOKEN:
                tableName = ContentDescriptor.HistoriesCrowd.NAME;
                break;
            case ContentDescriptor.HistoriesConfInvites.TOKEN:
                tableName = ContentDescriptor.HistoriesConfInvites.NAME;
                break;
            default:
                throw new RuntimeException("Does not support operation ：" + token + " uri is : " + uri);
        }
        // Cursor c = qb.query(db, projection, selection, selectionArgs, null,
        // null, sortOrder);
        if (mSQLitDatabaseHolder != null && mSQLitDatabaseHolder.isOpen()) {
            Cursor c = mSQLitDatabaseHolder.query(tableName, null, selection, selectionArgs, null, null, sortOrder);
            c.setNotificationUri(mContext.getContentResolver(), uri);
            return c;
        } else {
            return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] args) {
        int ret = 0;
        int token = ContentDescriptor.URI_MATCHER.match(uri);
        String table;
        switch (token) {
            case ContentDescriptor.HistoriesMessage.TOKEN:
                table = ContentDescriptor.HistoriesMessage.NAME;
                break;
            case ContentDescriptor.HistoriesGraphic.TOKEN:
                table = ContentDescriptor.HistoriesGraphic.NAME;
                break;
            case ContentDescriptor.HistoriesAudios.TOKEN:
                table = ContentDescriptor.HistoriesAudios.NAME;
                break;
            case ContentDescriptor.HistoriesFiles.TOKEN:
                table = ContentDescriptor.HistoriesFiles.NAME;
                break;
            case ContentDescriptor.RecentHistoriesMessage.TOKEN:
                table = ContentDescriptor.RecentHistoriesMessage.NAME;
                break;
            case ContentDescriptor.HistoriesMedia.TOKEN:
                table = ContentDescriptor.HistoriesMedia.NAME;
                break;
            case ContentDescriptor.HistoriesAddFriends.TOKEN:
                table = ContentDescriptor.HistoriesAddFriends.NAME;
                break;
            case ContentDescriptor.HistoriesCrowd.TOKEN:
                table = ContentDescriptor.HistoriesCrowd.NAME;
                break;
            case ContentDescriptor.HistoriesConfInvites.TOKEN:
                table = ContentDescriptor.HistoriesConfInvites.NAME;
                break;
            default:
                throw new RuntimeException("Does not support operation ：" + token + " uri is : " + uri);
        }
        if (table != null) {
            ret = mSQLitDatabaseHolder.update(table, values, selection, args);
            mContext.getContentResolver().notifyChange(uri, null);
        }
        return ret;
    }

}
