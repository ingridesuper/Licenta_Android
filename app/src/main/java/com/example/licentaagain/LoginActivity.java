package com.example.licentaagain;

import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.Credential;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText etEmail, etPassword;
    Button btnSignup, btnGoogleSignin;
    MaterialButton btnLogin;
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
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeVariables();
        subscribeToEvents();
    }

    private void initializeVariables(){
        mAuth=FirebaseAuth.getInstance();

        btnSignup=findViewById(R.id.btnSignup);
        btnLogin=findViewById(R.id.btnLogin);
        btnGoogleSignin=findViewById(R.id.btnGoogleLogin);
        etEmail=findViewById(R.id.etEmail);
        etPassword=findViewById(R.id.etPassword);
    }

    private void subscribeToEvents(){
        btnSignup.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v->{
            String email=String.valueOf(etEmail.getText());
            String password=String.valueOf(etPassword.getText());

            if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                Toast.makeText(this, "Password and email required.", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Intent intent=new Intent(getApplicationContext(), HomePageActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        btnGoogleSignin.setOnClickListener(v->{
            Intent intent=new Intent(this, GoogleSigninActivity.class);
            startActivity(intent);
            finish();
//            GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
//                    .setFilterByAuthorizedAccounts(false)
//                    .setServerClientId(getBaseContext().getString(R.string.default_web_client_id))
//                    .build();
//
//            GetCredentialRequest request = new GetCredentialRequest.Builder()
//                    .addCredentialOption(googleIdOption)
//                    .build();

        });
    }

//    private void handleSignIn(Credential credential) {
//        // Check if credential is of type Google ID
//        if (credential instanceof CustomCredential &&
//                credential.getType().equals(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
//
//            CustomCredential customCredential = (CustomCredential) credential;
//            // Create Google ID Token
//            Bundle credentialData = customCredential.getData();
//            GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credentialData);
//
//            // Sign in to Firebase using the token
//            firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
//        } else {
//            Log.w("credential warning", "Credential is not of type Google ID!");
//        }
//    }


//    private void firebaseAuthWithGoogle(String idToken) {
//        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, task -> {
//                    if (task.isSuccessful()) {
//                        // Sign in success, update UI with the signed-in user's information
//                        Log.d("credential success", "signInWithCredential:success");
//                        FirebaseUser user = mAuth.getCurrentUser();
//                        //updateUI(user);
//                    } else {
//                        // If sign in fails, display a message to the user
//                        Log.w("credential success", "signInWithCredential:failure", task.getException());
//                        //updateUI(null);
//                    }
//                });
//    }
}