package com.example.licentaagain;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.licentaagain.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountFragment extends Fragment {

    FirebaseAuth auth;
    Button btnLogout;
    TextView tvWelcome;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_account, container, false);

        auth=FirebaseAuth.getInstance();
        btnLogout=view.findViewById(R.id.btnLogout);
        tvWelcome=view.findViewById(R.id.tvWelcome);
        FirebaseUser user=auth.getCurrentUser();

        if(user==null){
            goToLoginPage();
        }
        else {
            tvWelcome.setText("Welcome, "+user.getEmail());
        }

        btnLogout.setOnClickListener(v->{
            FirebaseAuth.getInstance().signOut();
            goToLoginPage();
        });

        return view;
    }

    private void goToLoginPage(){
        Intent intent=new Intent(getActivity(), LoginActivity.class); //getActivity - access to the activity the fragment is attached to
        startActivity(intent);
        getActivity().finish();
    }


}
