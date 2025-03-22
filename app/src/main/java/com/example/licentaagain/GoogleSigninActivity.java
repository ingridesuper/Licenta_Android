package com.example.licentaagain;

import static android.content.ContentValues.TAG;
import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;
import static java.nio.charset.StandardCharsets.UTF_8;

import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Base64;
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

import com.google.android.gms.common.SignInButton;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONException;
import org.json.JSONObject;

public class GoogleSigninActivity extends AppCompatActivity {
    Button btn;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_google_signin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        btn=findViewById(R.id.bt_sign_in);

       btn.setOnClickListener(v-> {
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
                           handleSignIn(result);
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
//
//    public void handleSignIn(GetCredentialResponse result) throws JSONException {
//
//        Credential credential = result.getCredential();
//        if (credential instanceof CustomCredential) {
//
//            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
//
//                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom((credential).getData());
//
//                /*This code extracts the google user ID from token*/
//                String idToken = googleIdTokenCredential.getIdToken();
//                String[] segments = idToken.split("\\.");
//                byte[] payloadAsByteArray = Base64.decode(segments[1], Base64.NO_PADDING);
//                JSONObject payloadInJson = new JSONObject(new String(payloadAsByteArray, UTF_8));
//                String googleId = (String) payloadInJson.get("sub");
//                /*end of code*/
//
//
//                String email = googleIdTokenCredential.getId();
//                String firstName = googleIdTokenCredential.getGivenName();
//                String lastName = googleIdTokenCredential.getFamilyName();
//                String displayName = googleIdTokenCredential.getDisplayName();
//                Uri picture = googleIdTokenCredential.getProfilePictureUri();
//
//                firebaseAuthWithGoogle(idToken);
//
//            }
//            else {
//            Log.w(TAG, "Credential is not of type Google ID!");
//        }
//        }
//    }

    private void handleSignIn(GetCredentialResponse result) {
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
                        Log.i("user", user.getEmail()+" "+user.getDisplayName());
                        Toast.makeText(this, "Succes", Toast.LENGTH_SHORT).show();
                    } else {
                        // If sign in fails, display a message to the user
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(this, "Fail", Toast.LENGTH_SHORT).show();

                        //updateUI(null);
                    }
                });
    }
}