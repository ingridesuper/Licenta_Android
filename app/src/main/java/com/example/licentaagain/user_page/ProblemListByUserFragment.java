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
import android.widget.TextView;

import com.example.licentaagain.R;
import com.example.licentaagain.custom_adapters.ProblemCardAdapter;

import java.util.ArrayList;

public class ProblemListByUserFragment extends Fragment {
    private String selectedUserId;

    private ProblemBySelectedUserViewModel problemViewModel;
    private ProblemCardAdapter gsCardAdapter;
    private ProblemsSentSolvedCardAdapter sentCardAdapter;
    private ProblemsSentSolvedCardAdapter solvedCardAdapter;


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

        TextView tvNoItemsGS = view.findViewById(R.id.tvNoItemsGS);
        TextView tvNoProblemsSent = view.findViewById(R.id.tvNoProblemsSent);
        TextView tvNoProblemsSolved = view.findViewById(R.id.tvNoProblemsSolved);

        RecyclerView rvProblemsGS = view.findViewById(R.id.rvProblemsGS);
        rvProblemsGS.setNestedScrollingEnabled(false);
        gsCardAdapter = new ProblemCardAdapter(getContext(), new ArrayList<>());
        rvProblemsGS.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvProblemsGS.setAdapter(gsCardAdapter);

        RecyclerView rvProblemsSent = view.findViewById(R.id.rvProblemsSent);
        rvProblemsSent.setNestedScrollingEnabled(false);
        sentCardAdapter = new ProblemsSentSolvedCardAdapter(getContext(), new ArrayList<>());
        rvProblemsSent.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvProblemsSent.setAdapter(sentCardAdapter);

        RecyclerView rvProblemsSolved = view.findViewById(R.id.rvProblemsSolved);
        rvProblemsSolved.setNestedScrollingEnabled(false);
        solvedCardAdapter = new ProblemsSentSolvedCardAdapter(getContext(), new ArrayList<>());
        rvProblemsSolved.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvProblemsSolved.setAdapter(solvedCardAdapter);

        problemViewModel.getProblemsGatheringSignatures().observe(getViewLifecycleOwner(), list -> {
            gsCardAdapter.updateData(list);
            if (list.isEmpty()) {
                tvNoItemsGS.setVisibility(View.VISIBLE);
                rvProblemsGS.setVisibility(View.GONE);
            } else {
                tvNoItemsGS.setVisibility(View.GONE);
                rvProblemsGS.setVisibility(View.VISIBLE);
            }
        });

        problemViewModel.getProblemsSent().observe(getViewLifecycleOwner(), list -> {
            sentCardAdapter.updateData(list);
            if (list.isEmpty()) {
                tvNoProblemsSent.setVisibility(View.VISIBLE);
                rvProblemsSent.setVisibility(View.GONE);
            } else {
                tvNoProblemsSent.setVisibility(View.GONE);
                rvProblemsSent.setVisibility(View.VISIBLE);
            }
        });

        problemViewModel.getProblemsSolved().observe(getViewLifecycleOwner(), list -> {
            solvedCardAdapter.updateData(list);
            if (list.isEmpty()) {
                tvNoProblemsSolved.setVisibility(View.VISIBLE);
                rvProblemsSolved.setVisibility(View.GONE);
            } else {
                tvNoProblemsSolved.setVisibility(View.GONE);
                rvProblemsSolved.setVisibility(View.VISIBLE);
            }
        });

        if (selectedUserId != null) {
            problemViewModel.startListening(selectedUserId);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        problemViewModel.stopListening();
    }
}