package com.example.licentaagain.admin;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.licentaagain.R;
import com.example.licentaagain.admin.problems.AdminProblemListFragment;
import com.example.licentaagain.admin.users.AdminUserListFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminPageActivity extends AppCompatActivity {
    FragmentManager fragmentManager;
    BottomNavigationView bottomNavigationView;

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
            FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
            if(item.getItemId()==R.id.bmProblems){
                AdminProblemListFragment problemListFragment=new AdminProblemListFragment();
                fragmentTransaction.replace(R.id.fragment_container_view, problemListFragment);
            }
            else if(item.getItemId()==R.id.bmUsers){
                AdminUserListFragment userListFragment=new AdminUserListFragment();
                fragmentTransaction.replace(R.id.fragment_container_view, userListFragment);
            }
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
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