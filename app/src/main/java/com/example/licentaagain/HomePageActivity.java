package com.example.licentaagain;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.licentaagain.about.AboutFragment;
import com.example.licentaagain.account.TopProfileFragment;
import com.example.licentaagain.auth.LoginActivity;
import com.example.licentaagain.mainpage.MainPageFragment;
import com.example.licentaagain.problem.AddProblemFragment;
import com.example.licentaagain.user_page.SearchUserFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import android.Manifest;
import android.util.Log;

import org.checkerframework.checker.units.qual.A;


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

        //used to manually fetch for tetsing; delete after testing
//        FirebaseMessaging.getInstance().getToken()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        String token = task.getResult();
//                        Log.d("FCM", token);
//                    } else {
//                        Log.e("FCM", "Token fetch failed", task.getException());
//                    }
//                });

        initializeVariables();
        setDefaultFragment();
        setupNavigationMenuEvents();
        createNotificationChannel();
        requestNotificationPermission();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    getString(R.string.notification_channel_id),
                    "Canal notificări",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
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
                TopProfileFragment topProfileFragment=new TopProfileFragment();
                fragmentTransaction.replace(R.id.fragment_container_view, topProfileFragment);

            }
            else if (item.getItemId()==R.id.bmHome){
                MainPageFragment mainPageFragment=new MainPageFragment();
                fragmentTransaction.replace(R.id.fragment_container_view, mainPageFragment);
            }
            else if (item.getItemId()==R.id.bmAdd){
                AddProblemFragment addProblemFragment=new AddProblemFragment();
                fragmentTransaction.replace(R.id.fragment_container_view, addProblemFragment);
            }
            else if(item.getItemId()==R.id.bmSearchAccount){
                SearchUserFragment otherUserTopPageFragment=new SearchUserFragment();
                fragmentTransaction.replace(R.id.fragment_container_view, otherUserTopPageFragment);
            }
            else if (item.getItemId()==R.id.bmTutorial){
                AboutFragment aboutFragment=new AboutFragment();
                fragmentTransaction.replace(R.id.fragment_container_view, aboutFragment);
            }
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            return true;
        });
    }

}