package com.example.licentaagain.auth;

import static android.content.ContentValues.TAG;

import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
import com.example.licentaagain.enums.Sector;
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

public class SignupActivity extends AppCompatActivity {
    TextInputEditText etEmail, etPassword, etName, etSurname;
    MaterialButton btnSignup;
    Button btnSignupGoogle;
    Spinner spnSector;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String email, password, surname, name;
    Sector selectedSector;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser(); //? se repeta, care e ok?
        if(currentUser != null){
            goToHomePage();
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
        mAuth=FirebaseAuth.getInstance(); //aici se repeta
        db= FirebaseFirestore.getInstance();

        etEmail=findViewById(R.id.etEmail);
        etPassword=findViewById(R.id.etPassword);
        etSurname=findViewById(R.id.etSurname);
        etName=findViewById(R.id.etName);
        btnSignup=findViewById(R.id.btnSignup);
        btnSignupGoogle=findViewById(R.id.btnSignupGoogle);
        spnSector=findViewById(R.id.spnSector);
    }
    private void populateSpinner(){
        ArrayAdapter<Sector> arrayAdapterSpinner=new ArrayAdapter<Sector>(this, android.R.layout.simple_spinner_item, Sector.values());
        spnSector.setAdapter(arrayAdapterSpinner);
    }
    private void subscribeToEvents(){
        btnSignup.setOnClickListener(v->{
            getUserInputValues();
            if(!isUserInputOk()){
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            addUserToFirestore(firebaseUser, false);
                        } else {
                            Exception e = task.getException();
                            handleSignupException(e);
                        }
                    });

        });

        btnSignupGoogle.setOnClickListener(v->{
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

    private void getUserInputValues(){
        email=String.valueOf(etEmail.getText());
        password=String.valueOf(etPassword.getText());
        surname=String.valueOf(etSurname.getText());
        name=String.valueOf(etName.getText());
        selectedSector = (Sector) spnSector.getSelectedItem();
    }

    private void goToHomePage(){
        Intent intent=new Intent(getApplicationContext(), HomePageActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isUserInputOk(){
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(name) || TextUtils.isEmpty(surname)){
            Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        //updateUI(user);
                        Toast.makeText(this, "Succes", Toast.LENGTH_SHORT).show();
                        boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();
                        if (isNewUser) { //verific pt ca altfel it is overwritten si asta nuuu ne dorim
                            Log.i("count logare", "prima logare" );
                            addUserToFirestore(firebaseUser, true);
                        } else {
                            Log.i("count logare", "nu e prima logare");
                        }
                        goToHomePage();

                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(this, "Fail", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addUserToFirestore(FirebaseUser firebaseUser, boolean usesGoogleSignup) {
        if (firebaseUser == null) {
            return;
        }
        String uid = firebaseUser.getUid();
        User user;
        if(usesGoogleSignup){
            user=new User(uid, firebaseUser.getEmail());
        }
        else {
            user = new User(uid, email, name, surname, selectedSector.getNumar());
            Log.i("new user see sector", user.toString());
        }

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