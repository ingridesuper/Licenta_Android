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
    TextInputEditText etEmail, etPassword, etNume,etPrenume;
    MaterialButton btnSignup;
    Spinner spnSector;
    FirebaseAuth mAuth;

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
        mAuth=FirebaseAuth.getInstance();

        etEmail=findViewById(R.id.etEmail);
        etPassword=findViewById(R.id.etPassword);
        etNume=findViewById(R.id.etNume);
        etPrenume=findViewById(R.id.etPrenume);
        btnSignup=findViewById(R.id.btnSignup);
        spnSector=findViewById(R.id.spnSector);

        ArrayAdapter<Sector> arrayAdapterSpinner=new ArrayAdapter<Sector>(this, android.R.layout.simple_spinner_item, Sector.values());
        spnSector.setAdapter(arrayAdapterSpinner);

        btnSignup.setOnClickListener(v->{
            String email=String.valueOf(etEmail.getText());
            String password=String.valueOf(etPassword.getText());
            String nume=String.valueOf(etNume.getText());
            String prenume=String.valueOf(etPrenume.getText());
            Sector selectedSector = (Sector) spnSector.getSelectedItem();


            if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(nume) || TextUtils.isEmpty(prenume)){
                Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                if (firebaseUser != null) {
                                    String uid = firebaseUser.getUid();
                                    User user = new User(uid, email, nume, prenume, selectedSector.getNumar());
                                    // Save user to Firestore
                                    db.collection("users").document(uid)
                                            .set(user)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(SignupActivity.this, "Account created and data saved.", Toast.LENGTH_SHORT).show();
//                                                Intent intent = new Intent(getApplicationContext(), HomePageActivity.class);
//                                                startActivity(intent);
//                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("FirestoreError", "Error adding document", e);
                                                Toast.makeText(SignupActivity.this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                                            });
                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                Exception e = task.getException();
                                Log.e("SignupError", "Failed to create user", e);
                                Toast.makeText(SignupActivity.this, "Signing up failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });
    }
}