package com.example.licentaagain.admin.users;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licentaagain.R;
import com.example.licentaagain.admin.AdminPageActivity;
import com.example.licentaagain.models.User;

import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {


    private List<User> userList;

    public AdminUserAdapter(List<User> users){
        this.userList=users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.name.setText(user.getName()+" "+user.getSurname());
        holder.email.setText(user.getEmail());
        setOnUserClickListener(holder, user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    private void setOnUserClickListener(UserViewHolder holder, User user) {
        holder.itemView.setOnClickListener(v->{
            Context context=v.getContext();
            if(context instanceof AdminPageActivity){
                AdminPageActivity activity=(AdminPageActivity) context;

                Bundle bundle=new Bundle();
                bundle.putSerializable("user", user);

                AdminUserDetailsFragment adminUserDetailsFragment=new AdminUserDetailsFragment();
                adminUserDetailsFragment.setArguments(bundle);

                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container_view, adminUserDetailsFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    public void updateData(List<User> newUsers) {
        this.userList = newUsers;
        notifyDataSetChanged(); // You could use DiffUtil for better performance
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{
        TextView name, email;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvName);
            email = itemView.findViewById(R.id.tvEmail);
        }
    }
}
