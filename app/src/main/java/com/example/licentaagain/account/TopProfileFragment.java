package com.example.licentaagain.account;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.licentaagain.R;
import com.example.licentaagain.account.account_info.AccountFragment;
import com.example.licentaagain.account.reported_problems.MyReportedProblemsFragment;
import com.example.licentaagain.account.signed_problems.MySignaturesFragment;
import com.example.licentaagain.account.solved_problems.MySolvedProblemsFragment;
import com.google.android.material.button.MaterialButton;

public class TopProfileFragment extends Fragment {
    FragmentManager fragmentManager;
    MaterialButton btnAccount, btnMyProblems, btnMySolvedProblems, btnMySignatures;
    View indicatorMyProblems, indicatorMySolvedProblems, indicatorAccount, indicatorMySignatures;


    public TopProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager=getChildFragmentManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_top_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeVariables(view);

        //initial fragment set to my problems
        loadSubFragment(new MyReportedProblemsFragment(), btnMyProblems, indicatorMyProblems, btnAccount, btnMySolvedProblems, btnMySignatures, indicatorAccount, indicatorMySolvedProblems, indicatorMySignatures);
        setUpMenuButtonEvents();
    }

    private void initializeVariables(View view) {
        btnAccount=view.findViewById(R.id.btnMyAccountSettings);
        btnMyProblems=view.findViewById(R.id.btnMyProblems);
        btnMySolvedProblems=view.findViewById(R.id.btnMySolvedProblems);
        btnMySignatures=view.findViewById(R.id.btnMySignatures);
        indicatorMyProblems=view.findViewById(R.id.indicatorMyProblems);
        indicatorMySolvedProblems=view.findViewById(R.id.indicatorMySolvedProblems);
        indicatorAccount=view.findViewById(R.id.indicatorAccount);
        indicatorMySignatures=view.findViewById(R.id.indicatorMySignatures);
    }

    private void setUpMenuButtonEvents() {
        btnAccount.setOnClickListener(v -> loadSubFragment(new AccountFragment(), btnAccount, indicatorAccount,
                btnMyProblems, btnMySolvedProblems, btnMySignatures, indicatorMyProblems, indicatorMySolvedProblems, indicatorMySignatures));
        btnMyProblems.setOnClickListener(v -> loadSubFragment(new MyReportedProblemsFragment(), btnMyProblems, indicatorMyProblems,
                btnAccount, btnMySolvedProblems, btnMySignatures, indicatorAccount, indicatorMySolvedProblems, indicatorMySignatures));
        btnMySolvedProblems.setOnClickListener(v -> loadSubFragment(new MySolvedProblemsFragment(), btnMySolvedProblems, indicatorMySolvedProblems,
                btnMyProblems, btnAccount, btnMySignatures, indicatorMyProblems, indicatorAccount, indicatorMySignatures));
        btnMySignatures.setOnClickListener(v->loadSubFragment(new MySignaturesFragment(), btnMySignatures, indicatorMySignatures,
                btnAccount, btnMySolvedProblems, btnMyProblems ,indicatorAccount, indicatorMySolvedProblems, indicatorMyProblems));
    }

    private void loadSubFragment(Fragment fragment,  MaterialButton selectedButton, View selectedIndicator, MaterialButton btn2, MaterialButton btn3, MaterialButton btn4 , View ind2, View ind3, View ind4) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();

        updateTabStyle(selectedButton, btn2, btn3, btn4);

        selectedIndicator.setBackgroundColor(getResources().getColor(R.color.darkGray));
        ind2.setBackgroundColor(Color.TRANSPARENT);
        ind3.setBackgroundColor(Color.TRANSPARENT);
        ind4.setBackgroundColor(Color.TRANSPARENT);
    }


    private void updateTabStyle(MaterialButton selectedButton, MaterialButton... otherButtons) {
        selectedButton.setBackgroundColor(getResources().getColor(R.color.dustyPink));
        selectedButton.setTextColor(getResources().getColor(R.color.black));
        selectedButton.setIconTintResource(R.color.black);

        for (MaterialButton btn : otherButtons) {
            btn.setBackgroundColor(Color.TRANSPARENT);
            btn.setTextColor(getResources().getColor(R.color.darkGray));
            btn.setIconTintResource(R.color.darkGray);
        }
    }

}