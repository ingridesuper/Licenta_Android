package com.example.licentaagain.account;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.licentaagain.R;
import com.google.android.material.button.MaterialButton;

public class TopProfileFragment extends Fragment {
    FragmentManager fragmentManager;
    MaterialButton btnAccount;
    MaterialButton btnMyProblems;
    MaterialButton btnMyIdeas;
    View indicatorMyProblems;
    View indicatorMyIdeas;
    View indicatorAccount;


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
        //initial fragment set to account
        loadSubFragment(new AccountFragment(), btnAccount, indicatorAccount, btnMyProblems, btnMyIdeas, indicatorMyProblems, indicatorMyIdeas);

        setUpMenuButtonEvents(view);
    }

    private void initializeVariables(View view) {
        btnAccount=view.findViewById(R.id.btnMyAccountSettings);
        btnMyProblems=view.findViewById(R.id.btnMyProblems);
        btnMyIdeas=view.findViewById(R.id.btnMyIdeas);
        indicatorMyProblems=view.findViewById(R.id.indicatorMyProblems);
        indicatorMyIdeas=view.findViewById(R.id.indicatorMyIdeas);
        indicatorAccount=view.findViewById(R.id.indicatorAccount);
    }

    private void setUpMenuButtonEvents(View view) {
        btnAccount.setOnClickListener(v -> {
            loadSubFragment(new AccountFragment(), btnAccount, indicatorAccount,
                    btnMyProblems, btnMyIdeas, indicatorMyProblems, indicatorMyIdeas);
        });

        btnMyProblems.setOnClickListener(v -> {
            loadSubFragment(new MyReportedProblemsFragment(), btnMyProblems, indicatorMyProblems,
                    btnAccount, btnMyIdeas, indicatorAccount, indicatorMyIdeas);
        });

        btnMyIdeas.setOnClickListener(v -> {
            loadSubFragment(new MyIdeasFragment(), btnMyIdeas, indicatorMyIdeas,
                    btnAccount, btnMyProblems, indicatorAccount, indicatorMyProblems);
        });
    }

    private void loadSubFragment(Fragment fragment,  MaterialButton selectedButton, View selectedIndicator, MaterialButton btn2, MaterialButton btn3, View ind2, View ind3) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();

        updateTabStyle(selectedButton, btn2, btn3);

        selectedIndicator.setBackgroundColor(getResources().getColor(R.color.darkGray));
        ind2.setBackgroundColor(Color.TRANSPARENT);
        ind3.setBackgroundColor(Color.TRANSPARENT);
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