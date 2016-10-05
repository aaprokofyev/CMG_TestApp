package cmg.demo.cmg_testapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.List;

import cmg.demo.cmg_testapp.components.AdapterUsersList;
import cmg.demo.cmg_testapp.managers.DBInteractor;
import cmg.demo.cmg_testapp.managers.GitHubApiRequestManager;
import cmg.demo.cmg_testapp.managers.NetworkManager;
import cmg.demo.cmg_testapp.model.User;

public class MainActivity extends AppCompatActivity implements GitHubApiRequestManager.GitHubResponseService, NetworkManager.NetworkStateChanged, DBInteractor.DBPageLoadedService {
    private final String TAG = getClass().getSimpleName();
    private final int PRELOAD_ITEMS_DELTA = 10;

    private boolean isLoadingInProgress = false;
    private boolean isActivityJustCreated = true;

    private RecyclerView recyclerView;
    private AdapterUsersList listAdapter;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "MainActivity:onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        listAdapter = new AdapterUsersList(this);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        recyclerView.setAdapter(listAdapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = recyclerView.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

                if (!isLoadingInProgress && firstVisibleItem >= totalItemCount - visibleItemCount - PRELOAD_ITEMS_DELTA) {
                    String lastLoadedUserId = listAdapter.getLastLoadedId();
                    Log.d(TAG, "Scrolled to the bottom. Requesting data since " + lastLoadedUserId);
                    requestUsersPage(lastLoadedUserId);
                }
            }
        });

        // TODO: notify user if the internet connection is OFF
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        GitHubApiRequestManager.getInstance().registerResponseServiceCallback(this);
        DBInteractor.getInstance(this).registerPageLoadedCallback(this);
        NetworkManager.getInstance(this).registerNetworkStateCallback(this);

        // Request first page when activity just created
        if (isActivityJustCreated) {
            requestUsersPage(null);
            isActivityJustCreated = false;
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
        GitHubApiRequestManager.getInstance().removeResponseServiceCallBack(this);
        DBInteractor.getInstance(this).removePageLoadedCallBack(this);
        NetworkManager.getInstance(this).removeNetworkStateCallback(this);
    }

    private void requestUsersPage(String sinceUser) {
        Log.d(TAG, "requestUsersPage(" + sinceUser + ");");

        isLoadingInProgress = true;

        // Select where from the page should be requested, i.e. network or DB
        if (NetworkManager.getInstance(getBaseContext()).isNetworkAvailable()) {
            GitHubApiRequestManager.getInstance().getUsers(sinceUser);
        } else {
            DBInteractor.getInstance(this).requestUsersPage(sinceUser);
        }
    }

    @Override
    public void onUsersReceived(List<User> users) {
        Log.d(TAG, "onUsersReceived");
        isLoadingInProgress = false;

        if (!users.isEmpty()) {
            listAdapter.addUsers(users);
            // When users received from the network they immediately updated in DB
            DBInteractor.getInstance(this).insertOrUpdate(users);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        Log.d(TAG, "onError");
        isLoadingInProgress = false;
    }

    @Override
    public void onNetworkAvailable() {
        if (!isLoadingInProgress && listAdapter.getItemCount() == 0) {
            // We enabled network on empty screen, let's load some data
            requestUsersPage(null);
        }
    }

    @Override
    public void onNetworkUnavailable() {
        //do nothing
    }

    @Override
    public void onPageLoaded(List<User> users) {
        if (!users.isEmpty()) {
            Log.d(TAG, "Loaded " + users.size() + " users from database");
            listAdapter.addUsers(users);
        } else {
            Log.d(TAG, "No more users in database");
        }

        isLoadingInProgress = false;
    }
}
