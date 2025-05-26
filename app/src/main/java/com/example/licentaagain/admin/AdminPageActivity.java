package com.example.licentaagain.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.licentaagain.R;
import com.example.licentaagain.admin.contacts.AdminContactsFragment;
import com.example.licentaagain.admin.problems.AdminProblemListFragment;
import com.example.licentaagain.admin.users.AdminUserListFragment;
import com.example.licentaagain.auth.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminPageActivity extends AppCompatActivity {
    FragmentManager fragmentManager;
    BottomNavigationView bottomNavigationView;
    private int lastSelectedItemId = R.id.bmProblems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        initializeVariables();
        setDefaultFragment();
        setupNavigationMenuEvents();
    }


    private void setupNavigationMenuEvents() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if(itemId == R.id.bmLogout){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Sunteți sigur că vreți să ieșiți din cont?")
                        .setPositiveButton("Da", (dialog, id) -> {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(this, LoginActivity.class);
                            startActivity(intent);
                            this.finish();
                        })
                        .setNegativeButton("Nu", (dialog, id) -> {
                            dialog.dismiss();
                            // revenire la ultimul tab selectat
                            bottomNavigationView.setSelectedItemId(lastSelectedItemId);
                        });
                builder.create().show();
                return false;
            }

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if(itemId == R.id.bmProblems){
                AdminProblemListFragment problemListFragment = new AdminProblemListFragment();
                fragmentTransaction.replace(R.id.fragment_container_view, problemListFragment);
            } else if(itemId == R.id.bmUsers){
                AdminUserListFragment userListFragment = new AdminUserListFragment();
                fragmentTransaction.replace(R.id.fragment_container_view, userListFragment);
            }
            else if(itemId==R.id.bmContacts){
                AdminContactsFragment adminContactsFragment=new AdminContactsFragment();
                fragmentTransaction.replace(R.id.fragment_container_view, adminContactsFragment);
            }

            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            lastSelectedItemId = itemId;
            return true;
        });
    }


    private void initializeVariables(){
        fragmentManager=getSupportFragmentManager();
        bottomNavigationView=findViewById(R.id.bottom_navigation);
    }

    private void setDefaultFragment(){
        AdminProblemListFragment adminProblemListFragment=new AdminProblemListFragment();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_view, adminProblemListFragment);
        fragmentTransaction.commit();
    }

}