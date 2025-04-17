package com.example.licentaagain.account;

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
import com.example.licentaagain.custom_adapters.ProblemsByUserCardAdapter;
import com.example.licentaagain.view_models.ProblemByUserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MyReportedProblemsFragment extends Fragment {
    FirebaseFirestore db;
    FirebaseAuth auth;
    ProblemsByUserCardAdapter adapter;
    ProblemByUserViewModel viewModel;

    public MyReportedProblemsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db=FirebaseFirestore.getInstance();
        auth=FirebaseAuth.getInstance();
        viewModel = new ViewModelProvider(requireActivity()).get(ProblemByUserViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_reported_problems, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.rvProblems);
        recyclerView.setNestedScrollingEnabled(false);
        adapter=new ProblemsByUserCardAdapter(getContext(), new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        viewModel.getProblems().observe(getViewLifecycleOwner(), problems -> adapter.updateData(problems));
        viewModel.fetchAllProblemsByUser(auth.getCurrentUser().getUid());
    }
}