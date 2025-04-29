package com.example.licentaagain.account.signed_problems;

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

public class MySignaturesFragment extends Fragment {
    FirebaseFirestore db;
    FirebaseAuth auth;
    SignedByUserAdapter adapter;
    SignaturesOfUserViewModel viewModel;

    public MySignaturesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        viewModel = new ViewModelProvider(requireActivity()).get(SignaturesOfUserViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_signatures, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.rvProblems);
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new SignedByUserAdapter(getContext(), new ArrayList<>(), viewModel);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        viewModel.getProblems().observe(getViewLifecycleOwner(), problems -> adapter.updateData(problems));

        if (auth.getCurrentUser() != null) {
            viewModel.startListening(auth.getCurrentUser().getUid());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.stopListening();
    }
}
