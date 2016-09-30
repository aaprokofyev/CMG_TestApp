package cmg.demo.cmg_testapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import cmg.demo.cmg_testapp.managers.RequestManager;

public class MainActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Starting...");
        setContentView(R.layout.activity_main);
        RequestManager.getInstance().getUsers(null);
    }


}
