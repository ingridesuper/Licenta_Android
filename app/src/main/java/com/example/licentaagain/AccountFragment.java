package com.example.licentaagain;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.licentaagain.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountFragment extends Fragment {

    FirebaseAuth auth;
    Button btnLogout;
    TextView tvName;
    FirebaseUser user;
    FirebaseFirestore db;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_account, container, false);

        initializeVariables(view);

        if(user==null){
            goToLoginPage();
        }
        else {
            getUserInfo();
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

    private void initializeVariables(View view){
        db=FirebaseFirestore.getInstance();
        auth=FirebaseAuth.getInstance();
        btnLogout=view.findViewById(R.id.btnLogout);
        tvName=view.findViewById(R.id.tvName);
        user=auth.getCurrentUser();
    }


    private void getUserInfo(){
        DocumentReference documentReference=db.collection("users").document(user.getUid());
        documentReference.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String name = document.getString("name");
                            String surname = document.getString("surname");
                            //int sector=Integer.parseInt(String.valueOf(document.getLong("sector")));
                            populateWithUserData(name, surname);

                        } else {
                            Log.d("Firestore", "Documentul nu există!");
                        }
                    } else {
                        Log.d("Firestore", "Eroare la obținerea documentului: " + task.getException());
                    }
                });
    }
    private void populateWithUserData(String name, String surname){
        if(name!=null && surname!=null){
            tvName.setText(name+" "+surname);
        }
        else {
            tvName.setText("Va rugam sa va completati numele si prenumele. Astfel veti putea semna.");
        }
//        if (sector){
//
//        }

    }
}
