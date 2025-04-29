package com.example.licentaagain.account.reported_problems;

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
import com.example.licentaagain.custom_adapters.ProblemsByCurrentUserCardAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MyReportedProblemsFragment extends Fragment {
    private FirebaseAuth auth;
    private ProblemsByCurrentUserCardAdapter adapterGatheringSignatures;
    private ProblemsByCurrentUserCardAdapter adapterSentProblems;
    private ProblemsByCurrentUserViewModel viewModel;

    public MyReportedProblemsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        viewModel = new ViewModelProvider(requireActivity()).get(ProblemsByCurrentUserViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_reported_problems, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvProblemsGatheringSignatures = view.findViewById(R.id.rvProblemsGatheringSignatures);
        adapterGatheringSignatures = new ProblemsByCurrentUserCardAdapter(getContext(), new ArrayList<>());
        rvProblemsGatheringSignatures.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvProblemsGatheringSignatures.setAdapter(adapterGatheringSignatures);

        RecyclerView rvProblemsSent = view.findViewById(R.id.rvProblemsSent);
        adapterSentProblems = new ProblemsByCurrentUserCardAdapter(getContext(), new ArrayList<>());
        rvProblemsSent.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvProblemsSent.setAdapter(adapterSentProblems);

        viewModel.getProblemsGatheringSignatures().observe(getViewLifecycleOwner(), adapterGatheringSignatures::updateData);
        viewModel.getProblemsSent().observe(getViewLifecycleOwner(), adapterSentProblems::updateData);

        // Start real-time listeners!!
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid != null) {
            viewModel.startListening(uid);
        }
    }
}
