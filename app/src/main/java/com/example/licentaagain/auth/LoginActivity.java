package com.example.licentaagain.auth;

import static android.content.ContentValues.TAG;
import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.example.licentaagain.HomePageActivity;
import com.example.licentaagain.R;
import com.example.licentaagain.admin.AdminPageActivity;
import com.example.licentaagain.disabled_user.DisabledUserActivity;
import com.example.licentaagain.models.User;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText etEmail, etPassword;
    Button btnSignup, btnGoogleSignin;
    MaterialButton btnLogin, btnForgotPassword;
    FirebaseAuth mAuth;
    FirebaseFirestore db;


    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        boolean isAdmin = Boolean.TRUE.equals(documentSnapshot.getBoolean("isAdmin"));
                        boolean isDisabled = Boolean.TRUE.equals(documentSnapshot.getBoolean("isDisabled"));

                        if (isDisabled) {
                            mAuth.signOut();
                            Intent intent=new Intent(getApplicationContext(), DisabledUserActivity.class);
                            startActivity(intent);
                            finish();
                        } else if (isAdmin) {
                            goToAdminPage();
                        } else if (currentUser.isEmailVerified()) {
                            goToHomePage();
                        } else {
                            Toast.makeText(this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Could not check user role.", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                    });
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
        db= FirebaseFirestore.getInstance();

        btnSignup=findViewById(R.id.btnSignup);
        btnLogin=findViewById(R.id.btnLogin);
        btnForgotPassword=findViewById(R.id.btnForgotPassowrd);
        btnGoogleSignin=findViewById(R.id.btnSigninGoogle);
        etEmail=findViewById(R.id.etEmail);
        etPassword=findViewById(R.id.etPassword);
    }

    private void subscribeToEvents(){
        btnSignup.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
            startActivity(intent);
            finish();
        });

        btnLogin.setOnClickListener(v->{
            String email=String.valueOf(etEmail.getText());
            String password=String.valueOf(etPassword.getText());

            if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                Toast.makeText(this, "Password and email required.", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user == null) return;

                            String uid = user.getUid();
                            FirebaseFirestore.getInstance().collection("users")
                                    .document(uid)
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        boolean isAdmin = Boolean.TRUE.equals(documentSnapshot.getBoolean("isAdmin"));
                                        boolean isDisabled = Boolean.TRUE.equals(documentSnapshot.getBoolean("isDisabled"));

                                        if (isDisabled) {
                                            mAuth.signOut();
                                            Intent intent=new Intent(getApplicationContext(), DisabledUserActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else if (isAdmin) {
                                            goToAdminPage();
                                        } else if (user.isEmailVerified()) {
                                            goToHomePage();
                                        } else {
                                            Toast.makeText(this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                                            mAuth.signOut();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to fetch user role.", Toast.LENGTH_SHORT).show();
                                        mAuth.signOut();
                                    });

                        } else {
                            Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnGoogleSignin.setOnClickListener(v->{
            CredentialManager credentialManager = CredentialManager.create(getApplicationContext());

            GetSignInWithGoogleOption googleOption = new GetSignInWithGoogleOption.Builder(getString(R.string.default_web_client_id))
                    .setNonce("optional random string to increase encyption security")
                    .build();

            GetCredentialRequest credentialsRequest = new GetCredentialRequest.Builder()
                    .addCredentialOption(googleOption)
                    .build();

            CancellationSignal cancellationSignal = new CancellationSignal();
            cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
                @Override
                public void onCancel() {
                    //handle cancellation if needed
                }
            });

            credentialManager.getCredentialAsync(
                    getApplicationContext(),
                    credentialsRequest,
                    cancellationSignal,
                    Runnable::run,
                    new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>(){
                        @Override
                        public void onResult(GetCredentialResponse result) {
                            handleGoogleSignIn(result);
                        }

                        @Override
                        public void onError(GetCredentialException e) {
                            //handle failure exception
                            Log.e(TAG, "CredentialManager Error: " + e.getMessage(), e);
                        }
                    }
            );

        });

        //daca e un user pe google si completeaza asta -> il muta la email
        btnForgotPassword.setOnClickListener(v->{
            String email = String.valueOf(etEmail.getText()).trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(LoginActivity.this, "Please enter your email.", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Reset link sent to your email.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void handleGoogleSignIn(GetCredentialResponse result) {
        Credential credential = result.getCredential();

        // Check if credential is of type Google ID
        if (credential instanceof CustomCredential &&
                credential.getType().equals(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {

            // Perform explicit casting to CustomCredential
            CustomCredential customCredential = (CustomCredential) credential;

            // Create Google ID Token
            Bundle credentialData = customCredential.getData();
            GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credentialData);
            Log.d(TAG, "ID Token: " + googleIdTokenCredential);


            // Sign in to Firebase using the token
            firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
        } else {
            Log.w(TAG, "Credential is not of type Google ID!");
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        //updateUI(user);
                        boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();
                        if (isNewUser) { //verific pt ca altfel it is overwritten si asta nuuu ne dorim
                            Log.i("count logare", "prima logare" );
                            String uid=user.getUid();
                            User newUser=new User(uid, user.getEmail());
                            addUserToFirestore(uid, newUser);

                        } else {
                            Log.i("count logare", "nu e prima logare");
                        }

                        FirebaseFirestore.getInstance().collection("users")
                                .document(user.getUid())
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    boolean isDisabled = Boolean.TRUE.equals(documentSnapshot.getBoolean("isDisabled"));

                                    if (isDisabled) {
                                        mAuth.signOut();
                                        Intent intent=new Intent(getApplicationContext(), DisabledUserActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else  {
                                        goToHomePage();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to fetch user role.", Toast.LENGTH_SHORT).show();
                                    mAuth.signOut();
                                });

                    } else {
                        // If sign in fails, display a message to the user
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(this, "Fail", Toast.LENGTH_SHORT).show();
                        //updateUI(null);
                    }
                });
    }


    private void goToHomePage(){
        Intent intent=new Intent(getApplicationContext(), HomePageActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToAdminPage(){
        Intent intent=new Intent(getApplicationContext(), AdminPageActivity.class);
        startActivity(intent);
        finish();
    }

    private void addUserToFirestore(String uid, User user) {
        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    goToHomePage();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error adding document", e);
                    Toast.makeText(this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                });
    }
}