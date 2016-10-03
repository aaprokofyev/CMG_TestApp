package cmg.demo.cmg_testapp.managers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Home on 03.10.2016.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "UsersList.db";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DBUsersContract.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(DBUsersContract.SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);
    }

    public static class DBUsersContract {
        private DBUsersContract() {
        }

        // Table Users content
        public static class Users implements BaseColumns {
            public static final String TABLE_NAME = "users";
            public static final String COLUMN_GITHUB_ID = "githubId";
            public static final String COLUMN_LOGIN = "login";
            public static final String COLUMN_PHOTO_URL = "photoUrl";
        }

        // Helpful statements
        private static final String TEXT_TYPE = " TEXT";
        private static final String COMMA_SEP = ",";
        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + Users.TABLE_NAME + " (" +
                        Users._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                        Users.COLUMN_GITHUB_ID + TEXT_TYPE + COMMA_SEP +
                        Users.COLUMN_LOGIN + TEXT_TYPE + COMMA_SEP +
                        Users.COLUMN_PHOTO_URL + TEXT_TYPE + " )";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + Users.TABLE_NAME;


    }

}
