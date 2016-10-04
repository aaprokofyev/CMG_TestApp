package cmg.demo.cmg_testapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cmg.demo.cmg_testapp.components.AdapterUsersList;
import cmg.demo.cmg_testapp.managers.DBRequestManager;
import cmg.demo.cmg_testapp.managers.GitHubApiRequestManager;
import cmg.demo.cmg_testapp.model.User;

public class MainActivity extends AppCompatActivity implements GitHubApiRequestManager.GitHubResponseService {
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

                Log.d(TAG, "visibleItemCount=" + visibleItemCount + ", totalItemCount=" + totalItemCount + ", firstVisibleItem=" + firstVisibleItem);

                // TODO: need to load earlier then we reach the bottom + check if some request is already in progress
                if (!isLoadingInProgress && firstVisibleItem >= totalItemCount - visibleItemCount - PRELOAD_ITEMS_DELTA) {
                    Log.d(TAG, "Scrolled to the bottom");
                    GitHubApiRequestManager.getInstance().getUsers(listAdapter.getLastLoadedId());
                    isLoadingInProgress = true;
                }
            }
        });

        // TODO: check if internet connection is ON, otherwise notify user and make load from DB
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        GitHubApiRequestManager.getInstance().registerResponseServiceCallback(this);
        if (isActivityJustCreated) {
            GitHubApiRequestManager.getInstance().getUsers(null);
            isActivityJustCreated = false;
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
        GitHubApiRequestManager.getInstance().removeResponseServiceCallBack(this);
        List<User> users = new ArrayList<>();

        users.addAll(DBRequestManager.getInstance(this).getPage(null));
        users.addAll(DBRequestManager.getInstance(this).getPage("46"));

        Log.d(TAG, "Loaded users: " + users.size());
    }

    @Override
    public void onUsersReceived(List<User> users) {
        Log.d(TAG, "onUsersReceived");

        isLoadingInProgress = false;

        if (users != null && !users.isEmpty()) {
            listAdapter.addUsers(users);

            List<Long> ids = DBRequestManager.getInstance(this).putAll(users);
            Log.d(TAG, "Users are added in database: internal ids:" + ids.toString());
        }
    }

}
