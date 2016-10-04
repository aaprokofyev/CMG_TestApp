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
    private final long PAGE_SIZE = 30;

    private DBHelper dbHelper;

    private static volatile DBRequestManager instance;

    private DBRequestManager(Context context) {
        dbHelper = new DBHelper(context);
    }

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
        values.put(DBHelper.DBUsersContract.Users.COLUMN_GITHUB_ID, user.getGitHubId());
        values.put(DBHelper.DBUsersContract.Users.COLUMN_LOGIN, user.getLogin());
        values.put(DBHelper.DBUsersContract.Users.COLUMN_PHOTO_URL, user.getAvatarUrl());
        long id = db.insert(DBHelper.DBUsersContract.Users.TABLE_NAME, null, values);

        db.close();

        return id;
    }

    /**
     * @param users that need to be inserted into the database
     * @return list of primary keys for the inserted users
     */
    public List<Long> putAll(List<User> users) {
        List<Long> ids = new ArrayList<>(users.size());
        for (User user : users) {
            ids.add(put(user));
            //TODO: optimize in order not to open and close db on each user
        }
        return ids;
    }

    /**
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
        User user = null;

        if (cursor != null) {

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                user = new User();
                user.setGitHubId(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.DBUsersContract.Users.COLUMN_GITHUB_ID)));
                user.setLogin(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.DBUsersContract.Users.COLUMN_LOGIN)));
                user.setAvatarUrl(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.DBUsersContract.Users.COLUMN_PHOTO_URL)));
            }

            cursor.close();
        }

        db.close();

        return user;
    }

    /**
     * Get page of users since provided github ID
     *
     * @param sinceGithubId
     * @return list of users or empty list if no users found
     */
    public List<User> getPage(String sinceGithubId) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        List<User> users = new ArrayList<>();
        Long lastLoadedUserId = null;

        if (sinceGithubId != null) {
            // Get internal ID of last user
            String[] projectionUser = {
                    DBHelper.DBUsersContract.Users._ID,
            };

            String selectionUser = DBHelper.DBUsersContract.Users.COLUMN_GITHUB_ID + " = ?";
            String[] selectionUserArgs = {sinceGithubId};

            String sortOrderUser = DBHelper.DBUsersContract.Users.COLUMN_GITHUB_ID + " DESC";

            Cursor cursorUser = db.query(
                    DBHelper.DBUsersContract.Users.TABLE_NAME,
                    projectionUser,
                    selectionUser,
                    selectionUserArgs,
                    null,
                    null,
                    sortOrderUser
            );

            if (cursorUser != null) {
                if (cursorUser.getCount() > 0) {
                    cursorUser.moveToFirst();
                    lastLoadedUserId = cursorUser.getLong(cursorUser.getColumnIndexOrThrow(DBHelper.DBUsersContract.Users._ID));
                }
                cursorUser.close();
            }
        } else {
            //will be loaded from ID 0 till ID 30 first time
            lastLoadedUserId = -1L;
        }
        //Getting page since found ID
        if (lastLoadedUserId != null) {

            String[] projectionPage = {
                    DBHelper.DBUsersContract.Users.COLUMN_GITHUB_ID,
                    DBHelper.DBUsersContract.Users.COLUMN_LOGIN,
                    DBHelper.DBUsersContract.Users.COLUMN_PHOTO_URL
            };

            String sortOrderPage = DBHelper.DBUsersContract.Users.COLUMN_GITHUB_ID + " DESC";

            long sinceID = lastLoadedUserId + 1L;
            long tillID = sinceID + PAGE_SIZE;
            String limitPage = sinceID + ", " + tillID;

            Cursor cursorPage = db.query(
                    DBHelper.DBUsersContract.Users.TABLE_NAME,
                    projectionPage,
                    null,
                    null,
                    null,
                    null,
                    sortOrderPage,
                    limitPage
            );


            if (cursorPage != null && cursorPage.getCount() > 0) {
                cursorPage.moveToFirst();
                do {
                    User user = new User();
                    user.setGitHubId(cursorPage.getString(cursorPage.getColumnIndexOrThrow(DBHelper.DBUsersContract.Users.COLUMN_GITHUB_ID)));
                    user.setLogin(cursorPage.getString(cursorPage.getColumnIndexOrThrow(DBHelper.DBUsersContract.Users.COLUMN_LOGIN)));
                    user.setAvatarUrl(cursorPage.getString(cursorPage.getColumnIndexOrThrow(DBHelper.DBUsersContract.Users.COLUMN_PHOTO_URL)));
                    users.add(user);
                } while (cursorPage.moveToNext());

                cursorPage.close();
            }

        }

        db.close();

        return users;
    }

    /**
     * Updating profile info in DB based on received data
     *
     * @param user User to be updated
     * @return count of updated entries. Should be 1 or probably 0 if everything is fine with write functionality :)
     */
    public int update(User user) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBHelper.DBUsersContract.Users.COLUMN_LOGIN, user.getLogin());
        values.put(DBHelper.DBUsersContract.Users.COLUMN_PHOTO_URL, user.getAvatarUrl());

        String selection = DBHelper.DBUsersContract.Users.COLUMN_GITHUB_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(user.getGitHubId())};

        int count = db.update(
                DBHelper.DBUsersContract.Users.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        db.close();
        return count;
    }

    /**
     * This function is used to insert or update users in database
     *
     * @param users to put/update in/into the database
     */
    public void insertOrUpdate(List<User> users) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        for (User user : users) {
            if (get(user.getGitHubId()) != null) {
                update(user);
                Log.d(TAG, "Updating user " + user.getGitHubId());
            } else {
                //TODO: use putAll after optimization
                put(user);
                Log.d(TAG, "Inserting user " + user.getGitHubId());
            }
        }
        db.close();
    }
}
