package com.example.licentaagain.custom_adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licentaagain.HomePageActivity;
import com.example.licentaagain.R;
import com.example.licentaagain.suspened_user.SuspendedSearchedUserFragment;
import com.example.licentaagain.models.User;
import com.example.licentaagain.user_page.OtherUserFragment;

import java.util.List;

public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.UserViewHolder> {

    private Context context;

    private List<User> userList;

    public SearchUserAdapter(List<User> userList) {
        this.userList = userList;
    }


    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.name.setText(user.getName()+" "+user.getSurname());
        holder.email.setText(user.getEmail());
        setOnUserClickListener(holder, user);
    }

    private void setOnUserClickListener(UserViewHolder holder, User user) {
        holder.itemView.setOnClickListener(v->{
            Context context=v.getContext();
            if (context instanceof HomePageActivity) {
                HomePageActivity activity = (HomePageActivity) context;
                if(user.getIsDisabled()){
                    SuspendedSearchedUserFragment disabledSearchedUserFragment=new SuspendedSearchedUserFragment();
                    activity.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container_view, disabledSearchedUserFragment)
                            .addToBackStack(null)
                            .commit();
                }
                else {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("user", user);

                    OtherUserFragment otherUserFragment = new OtherUserFragment();
                    otherUserFragment.setArguments(bundle);

                    activity.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container_view, otherUserFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateData(List<User> newUsers) {
        this.userList = newUsers;
        notifyDataSetChanged(); // You could use DiffUtil for better performance
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView name, email;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvName);
            email = itemView.findViewById(R.id.tvEmail);
        }
    }
}
