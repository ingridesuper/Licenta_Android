package com.example.licentaagain.problem;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.licentaagain.R;
import com.example.licentaagain.models.Problem;


public class ProblemDetailsFragment extends Fragment {
    private Problem problem;


    public ProblemDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_problem_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            problem = (Problem) getArguments().getSerializable("problem");
        }

        TextView tvHelloProblem=view.findViewById(R.id.tvHelloProblem);
        tvHelloProblem.setText(problem.getTitle());
    }
}