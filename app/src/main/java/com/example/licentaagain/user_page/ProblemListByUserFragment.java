package com.example.licentaagain.user_page;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.licentaagain.R;
import com.example.licentaagain.custom_adapters.ProblemCardAdapter;
import com.example.licentaagain.view_models.ProblemBySelectedUserViewModel;

import java.util.ArrayList;

public class ProblemListByUserFragment extends Fragment {
    private String selectedUserId;

    private ProblemBySelectedUserViewModel problemViewModel;
    private ProblemCardAdapter adapter;



    public ProblemListByUserFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        problemViewModel=new ViewModelProvider(requireActivity()).get(ProblemBySelectedUserViewModel.class);
        if (getArguments() != null) {
            selectedUserId = (String) getArguments().getSerializable("userId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_problem_list_by_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.rvProblems);
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new ProblemCardAdapter(getContext(), new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        problemViewModel.getProblems().observe(getViewLifecycleOwner(), problems -> adapter.updateData(problems));
        problemViewModel.fetchAllProblemsByUser(selectedUserId);
    }
}