package com.example.licentaagain.account.account_info;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.licentaagain.R;
import com.example.licentaagain.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountFragment extends Fragment {

    FirebaseAuth auth;
    Button btnLogout;
    Button btnEditAccount;
    TextView tvName, tvSector, tvEmail;
    FirebaseUser user;
    FirebaseFirestore db;
    String name, surname;
    int sector;

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

        btnEditAccount.setOnClickListener(v->{
            EditAccountFragment editAccountFragment=new EditAccountFragment();
            Bundle bundle = new Bundle();
            bundle.putString("nume", name);
            bundle.putString("prenume", surname);
            bundle.putInt("sector", sector);
            bundle.putString("email", user.getEmail());

            editAccountFragment.setArguments(bundle);

            FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container_view, editAccountFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
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
        user=auth.getCurrentUser();

        btnLogout=view.findViewById(R.id.btnLogout);
        btnEditAccount=view.findViewById(R.id.btnEditAccount);
        tvName=view.findViewById(R.id.tvName);
        tvSector=view.findViewById(R.id.tvSector);
        tvEmail=view.findViewById(R.id.tvEmail);
    }


    private void getUserInfo(){
        DocumentReference documentReference=db.collection("users").document(user.getUid());
        documentReference.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            name = document.getString("name");
                            surname = document.getString("surname");
                            sector=document.getLong("sector").intValue();
                            populateWithUserData();
                        } else {
                            Log.d("Firestore", "Documentul nu există!");
                        }
                    } else {
                        Log.d("Firestore", "Eroare la obținerea documentului: " + task.getException());
                    }
                });
    }
    private void populateWithUserData(){
        if(name!=null && surname!=null){
            tvName.setText(name+" "+surname);
        }
        else {
            tvName.setText("Va rugam sa va completati numele si prenumele. Astfel veti putea semna.");
        }

        if (sector!=0){
            tvSector.setText("Sectorul "+sector);
        }
        else {
            tvSector.setText("Va rugam sa va completati sectorul.");
        }

        tvEmail.setText(user.getEmail());

    }



}
