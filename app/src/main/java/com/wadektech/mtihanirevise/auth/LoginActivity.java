package com.wadektech.mtihanirevise.auth;

import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.ui.PastPapersActivity;
import com.wadektech.mtihanirevise.viewmodels.ChatActivityViewModel;

import static android.widget.Toast.LENGTH_SHORT;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;
    Button btnlogin, btnSignUp, googleLogin;
    EditText loginEmail, loginPassword;
    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient ;
    private static final String TAG = "wadektech";
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog mConnectionProgressDialog;
    ChatActivityViewModel viewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnSignUp = findViewById (R.id.btn_sign_up);
        btnlogin = findViewById (R.id.btn_login);
        loginEmail = findViewById (R.id.et_email);
        loginPassword = findViewById (R.id.et_password);
        googleLogin = findViewById (R.id.tv_google_login);

        mAuth = FirebaseAuth.getInstance ();
         viewModel = ViewModelProviders.of(this).get(ChatActivityViewModel.class);
        viewModel.getReturningUser().observe(this,response->{
            if(response != null){
                if(response.equals("success")){
                   // Toast.makeText(this, "response is successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent (LoginActivity.this, PastPapersActivity.class);
                    finish ();
                    startActivity (intent);
                }else if(response.equals("fail")){
                    Toast.makeText(this, "record not found, please sign up again", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //initialize the firebase object
        mAuth = FirebaseAuth.getInstance ();

        btnlogin.setOnClickListener (v -> {
            String email = loginEmail.getText ().toString ();
            String password = loginPassword.getText ().toString ();
            if (TextUtils.isEmpty (email)) {
                loginEmail.setError ("Enter a valid email!");
            } else if (TextUtils.isEmpty (password)) {
                loginPassword.setError ("Please enter a strong email of more that 6 characters!");
            } else {
                mAuth.signInWithEmailAndPassword (email, password).addOnCompleteListener (task -> {
                    if (task.isSuccessful ()) {
                       viewModel.signInReturningUser(email);
                    } else {
                        Toast.makeText (getApplicationContext (),
                                "Authentication Failed! Please check your network and try again" + task.getException (), Toast.LENGTH_SHORT).show ();
                    }
                });
            }
        });
        btnSignUp.setOnClickListener (v -> {
            Intent intent = new Intent (this, SignUpActivity.class);
            finish();
            startActivity (intent);

        });

        googleLogin.setOnClickListener (v -> signIn ());
        mAuth = FirebaseAuth.getInstance();
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }
    @Override
    protected void onStart() {
        super.onStart ();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
       // mAuth.addAuthStateListener(mAuthListener);

    }
    private void signIn() {
       // Toast.makeText(this, "signIn()", Toast.LENGTH_SHORT).show();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult (requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent (data);
            mConnectionProgressDialog = new ProgressDialog (LoginActivity.this);
            mConnectionProgressDialog.setProgressStyle (ProgressDialog.STYLE_SPINNER);
            mConnectionProgressDialog.setTitle ("Welcome back...");
            mConnectionProgressDialog.setMessage ("Please be patient as we log you in");
            mConnectionProgressDialog.show ();

            if (result.isSuccess ()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount ();
                if (account != null) {
                    //Toast.makeText(this, "onActivityResult account is NOT NULL", Toast.LENGTH_SHORT).show();
                    firebaseAuthWithGoogle (account);
                }
            }else {
                // Google Sign In failed, update UI appropriately

                Toast.makeText(getApplicationContext(), "Something went wrong.",
                        LENGTH_SHORT).show();
                mConnectionProgressDialog.dismiss();
                Log.e(TAG, "Error type is" );
                // ...
            }
        }
    }
    public void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential (account.getIdToken (), null);
        mAuth.signInWithCredential (credential)
                .addOnCompleteListener (this, task -> {
                    if (task.isSuccessful ()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d (TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser ();
                        String userId = "";
                        if (user != null) {
                            userId = user.getUid ();
                            if(user.getEmail() != null){
                                //Toast.makeText(this, "off to firestore, email is not null", Toast.LENGTH_SHORT).show();
                                viewModel.signInReturningUser(user.getEmail());
                            }
                        }
                        mConnectionProgressDialog.dismiss ();
                        //  updateUI(user);


                    } else {
                        // If sign in fails, display a message to the user.
                        Log.e ("SignupActivity", "Failed Registration" + task.getException ());
                        Toast.makeText (LoginActivity.this, "Authentication failed." + task.getException (), LENGTH_SHORT).show ();
                        // updateUI(null);
                    }// ...
                });
    }
}