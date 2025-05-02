package com.example.licentaagain.admin.users;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.licentaagain.R;
import com.example.licentaagain.admin.problems.AdminProblemCardAdapter;
import com.example.licentaagain.models.User;

import java.util.ArrayList;


public class AdminUserDetailsFragment extends Fragment {
    private User user;
    private AdminProblemsByUserViewModel viewModel;

    public AdminUserDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user=(User) getArguments().getSerializable("user");
        viewModel=new ViewModelProvider(requireActivity()).get(AdminProblemsByUserViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_user_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fillUiWithUserData(view);
        setUpRecyclerView(view);
    }

    private void setUpRecyclerView(View view) {
        RecyclerView rvProblems=view.findViewById(R.id.rvProblems);
        rvProblems.setNestedScrollingEnabled(false);
        AdminProblemCardAdapter adapter=new AdminProblemCardAdapter(getContext(), new ArrayList<>());
        rvProblems.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvProblems.setAdapter(adapter);

        viewModel.getProblems().observe(getViewLifecycleOwner(), adapter::updateData);

        if(user.getUid()!=null){
            viewModel.startListening(user.getUid());
        }
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

        viewModel.getProblemsCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                tvNrProblemeRaportate.setText(String.valueOf(count));
            } else {
                tvNrProblemeRaportate.setText("0");
            }
        });

    }
}