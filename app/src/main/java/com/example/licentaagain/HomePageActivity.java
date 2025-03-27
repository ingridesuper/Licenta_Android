package com.example.licentaagain;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.licentaagain.account.AccountFragment;
import com.example.licentaagain.auth.LoginActivity;
import com.example.licentaagain.mainpage.MainPageFragment;
import com.example.licentaagain.problem.AddProblemFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;


public class HomePageActivity extends AppCompatActivity {
    FragmentManager fragmentManager;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            //changed the bottom to 0, i don't think it's a good idea but it looks better now
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        initializeVariables();
        setDefaultFragment();
        setupNavigationMenuEvents();
    }

    private void goToLoginPage(){
        Intent intent=new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void initializeVariables(){
        fragmentManager=getSupportFragmentManager();
        bottomNavigationView=findViewById(R.id.bottom_navigation);
    }

    private void setDefaultFragment(){
        MainPageFragment mainPageFragment=new MainPageFragment();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_view, mainPageFragment);
        fragmentTransaction.commit();
    }

    private void setupNavigationMenuEvents(){
        bottomNavigationView.setOnItemSelectedListener(item->{
            FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
            if(item.getItemId()==R.id.bmAccount){
                AccountFragment accountFragment=new AccountFragment();
                fragmentTransaction.replace(R.id.fragment_container_view, accountFragment);
            }
            else if (item.getItemId()==R.id.bmHome){
                MainPageFragment mainPageFragment=new MainPageFragment();
                fragmentTransaction.replace(R.id.fragment_container_view, mainPageFragment);
            }
            else if (item.getItemId()==R.id.bmAdd){
                AddProblemFragment addProblemFragment=new AddProblemFragment();
                fragmentTransaction.replace(R.id.fragment_container_view, addProblemFragment);
            }
            fragmentTransaction.commit();
            return true;
        });
    }

}