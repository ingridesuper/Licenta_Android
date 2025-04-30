package com.example.licentaagain.admin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.licentaagain.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class AdminProblemListFragment extends Fragment {
    private AdminProblemsViewModel viewModel;

    public AdminProblemListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel=new ViewModelProvider(requireActivity()).get(AdminProblemsViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_problem_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rvProblems=view.findViewById(R.id.rvProblems);
        AdminProblemCardAdapter adapter = new AdminProblemCardAdapter(getContext(), new ArrayList<>());
        rvProblems.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvProblems.setAdapter(adapter);
        viewModel.getProblems().observe(getViewLifecycleOwner(), adapter::updateData);

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid != null) {
            viewModel.startListening();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.stopListening();
    }
}