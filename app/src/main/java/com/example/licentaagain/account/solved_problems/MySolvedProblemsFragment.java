package com.example.licentaagain.account.solved_problems;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MySolvedProblemsFragment extends Fragment {
    FirebaseAuth auth;
    FirebaseFirestore db;
    SolvedProblemsByCurrentUserCardAdapter adapter;
    SolvedProblemsByCurrentUserViewModel viewModel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db=FirebaseFirestore.getInstance();
        auth=FirebaseAuth.getInstance();
        viewModel = new ViewModelProvider(requireActivity()).get(SolvedProblemsByCurrentUserViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_solved_problems, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rvSolvedProblems=view.findViewById(R.id.rvSolvedProblems);
        rvSolvedProblems.setNestedScrollingEnabled(false);
        rvSolvedProblems.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter=new SolvedProblemsByCurrentUserCardAdapter(getContext(), new ArrayList<>(), viewModel);
        rvSolvedProblems.setAdapter(adapter);
        viewModel.getProblems().observe(getViewLifecycleOwner(), problems -> adapter.updateData(problems));
        viewModel.fetchProblems(auth.getCurrentUser().getUid());
    }
}