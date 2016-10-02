package cmg.demo.cmg_testapp.components;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import cmg.demo.cmg_testapp.R;
import cmg.demo.cmg_testapp.model.User;

/**
 * Created by Home on 02.10.2016.
 */

public class AdapterUsersList extends RecyclerView.Adapter<AdapterUsersList.CustomViewHolder> {

    private final String TAG = getClass().getSimpleName();
    private static ArrayList<User> usersList = new ArrayList<>();
    private Context context;

    public static class CustomViewHolder extends RecyclerView.ViewHolder {

        private TextView loginView;
        private ImageView photoView;

        public CustomViewHolder(View v) {
            super(v);
            loginView = (TextView) v.findViewById(R.id.user_login);
            photoView = (ImageView) v.findViewById(R.id.user_pic);
        }
    }

    public AdapterUsersList(Context context) {
        this.context = context;
    }

    @Override
    public AdapterUsersList.CustomViewHolder onCreateViewHolder(ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item, parent, false);
        CustomViewHolder vh = new CustomViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        User user = usersList.get(position);
        Log.d(TAG, "Updating: " + user.getId());
        holder.loginView.setText(user.getLogin());
        Glide.with(context).load(user.getAvatarUrl()).into(holder.photoView);
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public void addUsers(List<User> users) {
        Log.d(TAG, "Add users: " + users.size());
        usersList.addAll(users);
        notifyDataSetChanged();
    }

    public void removeUsers() {
        usersList.clear();
        notifyDataSetChanged();
    }
}
