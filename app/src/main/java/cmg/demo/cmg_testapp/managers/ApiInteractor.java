package cmg.demo.cmg_testapp.managers;

import android.support.annotation.Nullable;
import android.util.Log;

import org.xml.sax.ErrorHandler;

import java.lang.annotation.Annotation;
import java.util.List;

import cmg.demo.cmg_testapp.model.RateLimitError;
import cmg.demo.cmg_testapp.model.User;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.plugins.RxJavaErrorHandler;
import rx.schedulers.Schedulers;

/**
 * Created by alprokof on 9/30/2016.
 * This class is used to interact with GitHub service via REST client.
 */
public class ApiInteractor {

    private final String TAG = getClass().getSimpleName();

    private Retrofit retrofit;
    private static final String API_URL = "https://api.github.com/";
    private GitHubAPIService mGitHubAPIService;


    public ApiInteractor() {

         retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        mGitHubAPIService = retrofit.create(GitHubAPIService.class);
        Log.d(TAG, "Retrofit instance created");
    }

    /**
     * This function is used to get users list using Retrofit+RxJava async request.
     *
     * @param view View that is observing users list response.
     * @param maxUserId Id of the last user in the previous list. Can be used to get next page. If parameter is null, then the first page will be requested.
     */
    public void getUsers(final Observer<List<User>> view, @Nullable String maxUserId) {
        Log.d(TAG, "getUsers");
        Observable<List<User>> users= mGitHubAPIService.getUsers(maxUserId);
        users.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .doOnNext(new Action1<List<User>>() {
                    @Override
                    public void call(List<User> usersList) {
                        Log.d(TAG, "OnNext");
                        for (User user : usersList) {
                            Log.d(TAG, "User fetched: " + user.getId() + ":" + user.getLogin());
                        }
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(TAG, "Error message: " + throwable.getMessage());
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        Log.d(TAG, "Completed");
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view);
    }

    private interface GitHubAPIService {
        @GET("users")
        Observable<List<User>> getUsers(@Query("since") String maxUserId);
    }
}
