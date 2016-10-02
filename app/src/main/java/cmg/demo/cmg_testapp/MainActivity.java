package cmg.demo.cmg_testapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.List;

import cmg.demo.cmg_testapp.components.AdapterUsersList;
import cmg.demo.cmg_testapp.managers.RequestManager;
import cmg.demo.cmg_testapp.model.User;

public class MainActivity extends AppCompatActivity implements RequestManager.GitHubResponseService {
    private final String TAG = getClass().getSimpleName();

    private String lastLoadedId;

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
                if (firstVisibleItem == totalItemCount - visibleItemCount) {
                    Log.d(TAG, "Scrolled to the bottom");
                    RequestManager.getInstance().getUsers(lastLoadedId);
                }
            }
        });

        // TODO: check if internet connection is ON, otherwise notify user
        RequestManager.getInstance().getUsers(null);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        RequestManager.getInstance().registerResponseServiceCallback(this);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        RequestManager.getInstance().removeResponseServiceCallBack(this);
    }

    @Override
    public void onUsersReceived(List<User> users) {
        //TODO: onUsersReceived will not be called in case server responded earlier then onResume called first time
        // Need to move first request out from onCreate to onResume and check if this first activity launch or not
        // This code should probably be connected with database
        Log.d(TAG, "onUsersReceived");
        if (users != null && !users.isEmpty()) {
            listAdapter.addUsers(users);
            lastLoadedId = users.get(users.size() - 1).getId();
            Log.d(TAG, "lastLoadedId=" + lastLoadedId);
        }
    }

}
