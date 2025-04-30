package com.example.licentaagain.user_page;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.licentaagain.R;
import com.example.licentaagain.models.User;

public class OtherUserFragment extends Fragment {
    private User user;
    private ProblemBySelectedUserViewModel problemViewModel;
    private FragmentManager fragmentManager;

    public OtherUserFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user=(User) getArguments().getSerializable("user");
        fragmentManager=getChildFragmentManager();
        problemViewModel=new ViewModelProvider(requireActivity()).get(ProblemBySelectedUserViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_other_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fillUiWithUserData(view);
        setUpProblemListFragment();
    }

    private void setUpProblemListFragment() {
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        ProblemListByUserFragment problemListFragment=new ProblemListByUserFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("userId", user.getUid());
        problemListFragment.setArguments(bundle);
        fragmentTransaction.add(R.id.userProblemListFragment, problemListFragment);
        fragmentTransaction.commit();
    }

    private void fillUiWithUserData(View view) {
        TextView tvName=view.findViewById(R.id.tvName);
        TextView tvEmail=view.findViewById(R.id.tvEmail);
        TextView tvSector=view.findViewById(R.id.tvSector);
        TextView headingProblemsReported=view.findViewById(R.id.headingProblemsReported);
        TextView tvNrProblemeRaportate=view.findViewById(R.id.tvNrProblemeRaportate);

        tvName.setText(user.getName()+" "+user.getSurname());
        tvEmail.setText(user.getEmail());
        tvSector.setText("Sectorul "+user.getSector());
        headingProblemsReported.setText("Problems reported by "+user.getName()+" "+user.getSurname()+":");

        problemViewModel.getReportedProblemsCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                tvNrProblemeRaportate.setText(String.valueOf(count));
            } else {
                tvNrProblemeRaportate.setText("0");
            }
        });

    }
}