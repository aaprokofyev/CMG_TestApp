package cmg.demo.cmg_testapp.managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import cmg.demo.cmg_testapp.model.User;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Home on 03.10.2016.
 */
public class DBInteractor {

    private final static String TAG = DBInteractor.class.getSimpleName();
    private final long PAGE_SIZE = 30;

    private DBHelper dbHelper;

    private static volatile DBInteractor instance;

    private DBInteractor(Context context) {
        dbHelper = new DBHelper(context);
    }

    public static DBInteractor getInstance(Context context) {
        DBInteractor localInstance = instance;
        if (localInstance == null) {
            synchronized (DBInteractor.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new DBInteractor(context);
                }
            }
        }
        return localInstance;
    }

    private static List<DBPageLoadedService> dbPageLoadedServiceListeners = new ArrayList<>();

    public void registerPageLoadedCallback(DBPageLoadedService dbPageLoadedServiceCallback) {
        dbPageLoadedServiceListeners.add(dbPageLoadedServiceCallback);
    }

    public void removePageLoadedCallBack(DBPageLoadedService dbPageLoadedServiceCallback) {
        dbPageLoadedServiceListeners.remove(dbPageLoadedServiceCallback);
    }

    private static <T> Observable<T> makeObservable(final Callable<T> func) {
        return Observable.create(
                new Observable.OnSubscribe<T>() {
                    @Override
                    public void call(Subscriber<? super T> subscriber) {
                        try {
                            subscriber.onNext(func.call());
                        } catch (Exception ex) {
                            Log.e(TAG, "Error reading from the database", ex);
                        }
                    }
                });
    }

    private Callable<Long> callablePut(final SQLiteDatabase db, final User user) {
        return new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                ContentValues values = new ContentValues();
                values.put(DBHelper.DBUsersContract.Users.COLUMN_GITHUB_ID, user.getGitHubId());
                values.put(DBHelper.DBUsersContract.Users.COLUMN_LOGIN, user.getLogin());
                values.put(DBHelper.DBUsersContract.Users.COLUMN_PHOTO_URL, user.getAvatarUrl());
                long id = db.insert(DBHelper.DBUsersContract.Users.TABLE_NAME, null, values);

                return id;
            }
        };
    }

    public Observable<Long> put(SQLiteDatabase db, User user) {
        return makeObservable(callablePut(db, user))
                .subscribeOn(Schedulers.computation());
    }

    private Callable<Void> callablePutAll(final SQLiteDatabase db, final List<User> users) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                for (User user : users) {
                    ContentValues values = new ContentValues();
                    values.put(DBHelper.DBUsersContract.Users.COLUMN_GITHUB_ID, user.getGitHubId());
                    values.put(DBHelper.DBUsersContract.Users.COLUMN_LOGIN, user.getLogin());
                    values.put(DBHelper.DBUsersContract.Users.COLUMN_PHOTO_URL, user.getAvatarUrl());
                    long id = db.insert(DBHelper.DBUsersContract.Users.TABLE_NAME, null, values);

                    Log.d(TAG, "Inserting user " + user.getGitHubId());
                }

                return null;
            }
        };
    }

    public Observable<Void> putAll(SQLiteDatabase db, List<User> users) {
        return makeObservable(callablePutAll(db, users))
                .subscribeOn(Schedulers.computation());
    }

    private Callable<User> callableGet(final SQLiteDatabase db, final String githubId) {
        return new Callable<User>() {
            @Override
            public User call() throws Exception {
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

                return user;
            }
        };
    }

    public Observable<User> get(SQLiteDatabase db, String githubId) {
        return makeObservable(callableGet(db, githubId))
                .subscribeOn(Schedulers.computation());
    }

    private Callable<List<User>> callableGetPage(final SQLiteDatabase db, final String sinceGithubId) {
        return new Callable<List<User>>() {
            @Override
            public List<User> call() throws Exception {
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
                    // Page will be loaded from ID 0 till ID 30 first time
                    lastLoadedUserId = 0L;
                }

                Log.d(TAG, "Internal ID of last loaded GitHub user is " + lastLoadedUserId);

                // Getting page since found ID
                if (lastLoadedUserId != null) {

                    String[] projectionPage = {
                            DBHelper.DBUsersContract.Users.COLUMN_GITHUB_ID,
                            DBHelper.DBUsersContract.Users.COLUMN_LOGIN,
                            DBHelper.DBUsersContract.Users.COLUMN_PHOTO_URL
                    };

                    long OFFSET = lastLoadedUserId;
                    long LIMIT = PAGE_SIZE;
                    String limitPage = OFFSET + ", " + LIMIT;
                    Log.d(TAG, "Page limit clause: " + limitPage);

                    Cursor cursorPage = db.query(
                            DBHelper.DBUsersContract.Users.TABLE_NAME,
                            projectionPage,
                            null,
                            null,
                            null,
                            null,
                            null,
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

                for (User user : users) {
                    Log.d(TAG, "Loaded from DB: " + user.getGitHubId() + ":" + user.getLogin());
                }

                return users;
            }
        };
    }

    public Observable<List<User>> getPage(SQLiteDatabase db, String sinceId) {
        return makeObservable(callableGetPage(db, sinceId))
                .subscribeOn(Schedulers.computation());
    }

    private Callable<Integer> callableUpdate(final SQLiteDatabase db, final User user) {
        return new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
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

                return count;
            }
        };
    }

    public Observable<Integer> update(SQLiteDatabase db, User user) {
        return makeObservable(callableUpdate(db, user))
                .subscribeOn(Schedulers.computation());
    }

    private Callable<Void> callableUpdateAll(final SQLiteDatabase db, final List<User> users) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                for (User user : users) {

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

                    Log.d(TAG, "Updating user " + user.getGitHubId());
                }

                return null;
            }
        };
    }

    public Observable<Void> updateAll(SQLiteDatabase db, List<User> users) {
        return makeObservable(callableUpdateAll(db, users))
                .subscribeOn(Schedulers.computation());
    }

    public void requestUsersPage(String sinceUserId) {
        final SQLiteDatabase readableDb = dbHelper.getReadableDatabase();
        getPage(readableDb, sinceUserId)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<List<User>>() {
                    @Override
                    public void call(List<User> users) {
                        for (DBPageLoadedService listener : dbPageLoadedServiceListeners) {
                            listener.onPageLoaded(users);
                        }
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        for (DBPageLoadedService listener : dbPageLoadedServiceListeners) {
                            listener.onPageLoaded(null);
                        }
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        readableDb.close();
                    }
                })
                .subscribe();
    }

    public void insertOrUpdate(List<User> users) {

        final SQLiteDatabase readableDb = dbHelper.getReadableDatabase();

        for (final User user : users) {
            get(readableDb, user.getGitHubId())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(new Action1<User>() {
                        @Override
                        public void call(final User userFromDB) {
                            if (userFromDB != null) {
                                final SQLiteDatabase readableDb2 = dbHelper.getReadableDatabase();
                                update(readableDb2, userFromDB)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .doOnNext(new Action1<Integer>() {
                                            @Override
                                            public void call(Integer integer) {
                                                Log.d(TAG, "Updating user " + userFromDB.getGitHubId());
                                            }
                                        })
                                        .doOnCompleted(new Action0() {
                                            @Override
                                            public void call() {
                                                readableDb2.close();
                                            }
                                        })
                                        .subscribe();
                            } else {
                                final SQLiteDatabase writableDb = dbHelper.getWritableDatabase();
                                put(writableDb, user)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .doOnNext(new Action1<Long>() {
                                            @Override
                                            public void call(Long aLong) {
                                                Log.d(TAG, "Inserting user " + user.getGitHubId());
                                            }
                                        })
                                        .doOnCompleted(new Action0() {
                                            @Override
                                            public void call() {
                                                writableDb.close();
                                            }
                                        })
                                        .subscribe();
                            }
                        }
                    })
                    .doOnCompleted(new Action0() {
                        @Override
                        public void call() {
                            readableDb.close();
                        }
                    })
                    .subscribe();
        }
    }

    public interface DBPageLoadedService {
        void onPageLoaded(List<User> users);
    }
}
