package com.example.licentaagain;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.licentaagain.enums.Sector;
import com.example.licentaagain.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignupActivity extends AppCompatActivity {
    TextInputEditText etEmail, etPassword, etName, etSurname;
    MaterialButton btnSignup;
    Spinner spnSector;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent=new Intent(getApplicationContext(), HomePageActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeVariables();
        populateSpinner();
        subscribeToEvents();
    }

    private void initializeVariables(){
        mAuth=FirebaseAuth.getInstance();
        db= FirebaseFirestore.getInstance();

        etEmail=findViewById(R.id.etEmail);
        etPassword=findViewById(R.id.etPassword);
        etSurname=findViewById(R.id.etSurname);
        etName=findViewById(R.id.etName);
        btnSignup=findViewById(R.id.btnSignup);
        spnSector=findViewById(R.id.spnSector);
    }
    private void populateSpinner(){
        ArrayAdapter<Sector> arrayAdapterSpinner=new ArrayAdapter<Sector>(this, android.R.layout.simple_spinner_item, Sector.values());
        spnSector.setAdapter(arrayAdapterSpinner);
    }
    private void subscribeToEvents(){
        btnSignup.setOnClickListener(v->{
            String email=String.valueOf(etEmail.getText());
            String password=String.valueOf(etPassword.getText());
            String surname=String.valueOf(etSurname.getText());
            String name=String.valueOf(etName.getText());
            Sector selectedSector = (Sector) spnSector.getSelectedItem();

            if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(name) || TextUtils.isEmpty(surname)){
                Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                if (firebaseUser != null) {
                                    String uid = firebaseUser.getUid();
                                    User user = new User(uid, email, name, surname, selectedSector.getNumar());
                                    db.collection("users").document(uid)
                                            .set(user)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(SignupActivity.this, "Account created and data saved.", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(getApplicationContext(), HomePageActivity.class);
                                                startActivity(intent);
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("FirestoreError", "Error adding document", e);
                                                Toast.makeText(SignupActivity.this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                                            });
                                }
                            } else {
                                Exception e = task.getException();
                                handleSignupException(e);

                            }
                        }
                    });

        });
    }
    private void handleSignupException(Exception e){
        if (e instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException) {
            Toast.makeText(SignupActivity.this, "This email is already in use.", Toast.LENGTH_SHORT).show();
        } else if (e instanceof com.google.firebase.auth.FirebaseAuthWeakPasswordException) {
            Toast.makeText(SignupActivity.this, "Weak password. Please use at least 6 characters.", Toast.LENGTH_SHORT).show();
        } else {
            Log.e("SignupError", "Failed to create user", e);
            Toast.makeText(SignupActivity.this, "Signing up failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}