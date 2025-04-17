package com.example.licentaagain.custom_adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licentaagain.R;
import com.example.licentaagain.models.User;
import com.example.licentaagain.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.UserViewHolder> {

    private Context context;

    private List<User> userList;
    private UserRepository userRepository;

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
        holder.name.setText(user.getName());
        holder.email.setText(user.getEmail());
    }

    @Override
    public int getItemCount() {
        return userList.size();
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
