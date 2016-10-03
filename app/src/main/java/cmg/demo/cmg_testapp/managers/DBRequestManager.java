package cmg.demo.cmg_testapp.managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cmg.demo.cmg_testapp.model.User;

/**
 * Created by Home on 03.10.2016.
 */
public class DBRequestManager {

    private final String TAG = getClass().getSimpleName();

    private DBHelper dbHelper;

    private DBRequestManager(Context context) {
        dbHelper = new DBHelper(context);
    }

    private static volatile DBRequestManager instance;

    public static DBRequestManager getInstance(Context context) {
        DBRequestManager localInstance = instance;
        if (localInstance == null) {
            synchronized (DBRequestManager.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new DBRequestManager(context);
                }
            }
        }
        return localInstance;
    }

    /**
     * @param user that need to be inserted into the database
     * @return primary key of the new created row
     */
    public long put(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBHelper.DBUsersContract.Users.COLUMN_GITHUB_ID, user.getId());
        values.put(DBHelper.DBUsersContract.Users.COLUMN_LOGIN, user.getLogin());
        values.put(DBHelper.DBUsersContract.Users.COLUMN_PHOTO_URL, user.getAvatarUrl());

        return db.insert(DBHelper.DBUsersContract.Users.TABLE_NAME, null, values);
    }

    /**
     * @param users that need to be inserted into the database
     * @return list of primary keys for the inserted users
     */
    public List<Long> putAll(List<User> users) {
        List<Long> ids = new ArrayList<>(users.size());
        for (User user : users) {
            ids.add(put(user));
        }
        return ids;
    }

    /**
     *
     * @param githubId of the user
     * @return User with details or null if not found in database
     */
    public User get(String githubId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DBHelper.DBUsersContract.Users.COLUMN_GITHUB_ID,
                DBHelper.DBUsersContract.Users.COLUMN_LOGIN,
                DBHelper.DBUsersContract.Users.COLUMN_PHOTO_URL
        };

        String selection = DBHelper.DBUsersContract.Users.COLUMN_GITHUB_ID + " = ?";
        String[] selectionArgs = {githubId};

        String sortOrder = DBHelper.DBUsersContract.Users.COLUMN_GITHUB_ID + " DESC";

        Cursor cursor = db.query(
                DBHelper.DBUsersContract.Users.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );


        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            User user = new User();
            user.setId(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.DBUsersContract.Users.COLUMN_GITHUB_ID)));
            user.setLogin(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.DBUsersContract.Users.COLUMN_LOGIN)));
            user.setAvatarUrl(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.DBUsersContract.Users.COLUMN_PHOTO_URL)));
            return user;
        }

        return null;
    }

    public List<User> getPage(String sinceGithubId) {
        return null;
    }


}
