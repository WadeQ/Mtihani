package com.wadektech.mtihani.auth;

import android.app.ProgressDialog;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.wadektech.mtihani.R;
import com.wadektech.mtihani.ui.PastPapersActivity;
import com.wadektech.mtihani.viewmodels.ChatActivityViewModel;

import timber.log.Timber;

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
    CheckBox mCheckPassword ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnSignUp = findViewById (R.id.btn_sign_up);
        btnlogin = findViewById (R.id.btn_login);
        loginEmail = findViewById (R.id.et_email);
        loginPassword = findViewById (R.id.et_password);
        googleLogin = findViewById (R.id.tv_google_login);
        mCheckPassword = findViewById(R.id.password_check);

        mAuth = FirebaseAuth.getInstance ();
         viewModel = ViewModelProviders.of(this).get(ChatActivityViewModel.class);
        viewModel.getReturningUser().observe(this,response->{
            if(response != null){
                if(response.equals("success")){
                    Intent intent = new Intent (LoginActivity.this,
                            PastPapersActivity.class);
                    finish ();
                    startActivity (intent);
                }else if(response.equals("fail")){
                    Toast.makeText(this,
                            "record not found, please sign up again",
                            Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this,
                            "An error occurred",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

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
                                "Authentication Failed! Please check your network and try again"
                                        + task.getException (), Toast.LENGTH_SHORT).show ();
                    }
                });
            }
        });

        btnSignUp.setOnClickListener (v -> {
            Intent intent = new Intent (this, SignUpActivity.class);
            finish();
            startActivity (intent);

        });

        mCheckPassword.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                // Show Password
                loginPassword.setTransformationMethod(HideReturnsTransformationMethod
                        .getInstance());
            } else {
                // Hide Password
                loginPassword.setTransformationMethod(PasswordTransformationMethod
                        .getInstance());
            }
        });

        googleLogin.setOnClickListener (v -> signIn ());
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions
                .DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onStart() {
        super.onStart ();

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult (requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent (data);
            mConnectionProgressDialog = new ProgressDialog (LoginActivity.this);
            mConnectionProgressDialog.setProgressStyle (ProgressDialog.STYLE_SPINNER);
            mConnectionProgressDialog.setTitle ("Welcome back...");
            mConnectionProgressDialog.setMessage ("Please be patient as we log you in");
            mConnectionProgressDialog.show ();

            if (result.isSuccess ()) {
                GoogleSignInAccount account = result.getSignInAccount ();
                if (account != null) {
                    firebaseAuthWithGoogle (account);
                }
            }else {
                Toast.makeText(getApplicationContext(), "Something went wrong.",
                        LENGTH_SHORT).show();
                mConnectionProgressDialog.dismiss();
                Timber.e("Login failed with error type is");
            }
        }
    }

    public void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential (account.getIdToken (),
                null);
        mAuth.signInWithCredential (credential)
                .addOnCompleteListener (this, task -> {
                    if (task.isSuccessful ()) {
                        Timber.d("signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser ();
                        String userId = "";
                        if (user != null) {
                            userId = user.getUid ();
                            if(user.getEmail() != null){
                                viewModel.signInReturningUser(user.getEmail());
                            }
                        }
                        mConnectionProgressDialog.dismiss ();

                    } else {
                        Timber.d("Failed Registration %s", task.getException());
                        Toast.makeText (LoginActivity.this, "Authentication failed."
                                + task.getException (), LENGTH_SHORT).show ();
                    }
                });
    }
}