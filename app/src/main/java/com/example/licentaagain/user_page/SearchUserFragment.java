package com.example.licentaagain.user_page;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.licentaagain.R;
import com.example.licentaagain.custom_adapters.SearchUserAdapter;
import com.example.licentaagain.view_models.SearchedUserViewModel;

import java.util.ArrayList;

public class SearchUserFragment extends Fragment {
    private SearchedUserViewModel viewModel;
    private SearchUserAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel=new ViewModelProvider(requireActivity()).get(SearchedUserViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_person_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpUserRecyclerView(view);
        setUpSearchEvents(view);
    }

    private void setUpSearchEvents(View view) {
        SearchView searchView=view.findViewById(R.id.searchBar);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.isEmpty()){
                    viewModel.setUsers(new ArrayList<>());
                }
                else {
                    viewModel.searchUserByEmailOrNameSurname(newText);
                }
                return false;
            }
        });
    }

    private void setUpUserRecyclerView(View view) {
        RecyclerView recyclerView=view.findViewById(R.id.rvUsers);
        recyclerView.setNestedScrollingEnabled(false);
        adapter=new SearchUserAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        viewModel.getUsers().observe(getViewLifecycleOwner(), users -> {
            adapter.updateData(users);
            Log.i("fetchedUsers", String.valueOf(users.size())+": "+users.toString());

        });
    }
}