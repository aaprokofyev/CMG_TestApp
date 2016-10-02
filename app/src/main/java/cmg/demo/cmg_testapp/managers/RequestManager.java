package cmg.demo.cmg_testapp.managers;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cmg.demo.cmg_testapp.model.RateLimitError;
import cmg.demo.cmg_testapp.model.User;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observer;

/**
 * Created by alprokof on 9/30/2016.
 */
public class RequestManager implements Observer<List<User>> {

    private final String TAG = getClass().getSimpleName();

    private ApiInteractor apiInteractor;

    private static List<GitHubResponseService> gitHubResponseServiceListeners = new ArrayList<>();

    private RequestManager() {
        apiInteractor = new ApiInteractor();
    }

    public static class RequestManagerHolder {
        public static final RequestManager HOLDER_INSTANCE = new RequestManager();
    }

    public static RequestManager getInstance() {
        return RequestManagerHolder.HOLDER_INSTANCE;
    }

    public void registerResponseServiceCallback(GitHubResponseService gitHubResponseServiceCallback) {
        gitHubResponseServiceListeners.add(gitHubResponseServiceCallback);
    }

    public void removeResponseServiceCallBack(GitHubResponseService gitHubResponseServiceCallback) {
        gitHubResponseServiceListeners.remove(gitHubResponseServiceCallback);
    }

    public void getUsers(String maxUserId) {
        Log.d(TAG, "Sending \"getUsers\" request");
        apiInteractor.getUsers(this, maxUserId);
    }

    @Override
    public void onCompleted() {
        //do nothing
    }

    @Override
    public void onError(Throwable e) {
        if (e instanceof HttpException) {
            //TODO: parse RateLimitError
            HttpException exception = (HttpException) e;
            Log.d(TAG, "HTTP exception: " + exception.getMessage());
            // We had non-2XX http error
        }
        if (e instanceof IOException) {
            IOException exception = (IOException) e;
            Log.d(TAG, "HTTP exception: " + exception.getMessage());
            // A network or conversion error happened
        }
        //do nothing
    }

    @Override
    public void onNext(List<User> userList) {
        Log.d(TAG, "RequestManager: onNext " + userList.toString());
        for (GitHubResponseService listener : gitHubResponseServiceListeners) {
            listener.onUsersReceived(userList);
        }
    }

    public interface GitHubResponseService {
        void onUsersReceived(List<User> users);
    }
}
