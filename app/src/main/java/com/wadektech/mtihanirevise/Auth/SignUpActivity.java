package com.wadektech.mtihanirevise.Auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wadektech.mtihanirevise.AdminPanelActivity;
import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.UI.PastPapersActivity;

import java.util.HashMap;

import static android.widget.Toast.LENGTH_SHORT;


public class SignUpActivity extends AppCompatActivity {

    Button mLogin , btnSignUp;
    EditText etPassword, etUsername , etEmail ;
    private static final int RC_SIGN_IN = 1;
    DatabaseReference reference ;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private static final String TAG = "wadektech";
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog mConnectionProgressDialog;
    FirebaseUser firebaseUser ;
    SharedPreferences sharedPrefManager ;
    private ProgressDialog pDialog ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etPassword = findViewById(R.id.et_password);
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        btnSignUp = findViewById(R.id.btn_SignUp);

        mAuth = FirebaseAuth.getInstance() ;
        pDialog = new ProgressDialog(this);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userNmae = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String email = etEmail.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    etEmail.setError("Please Enter Your Email Address!");

                } else if(TextUtils.isEmpty(password) || password.length() < 6) {
                    etPassword.setError("Please Enter Secure Password or make sure your password is six characters long!");
                }
                    else if (TextUtils.isEmpty(userNmae)) {
                    etUsername.setError("Please Enter username!");
                }
                else {
                    registerUser(password , userNmae , email);
                }
            }
        });

        //initialize the firebase object
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    checkAdminStatus();
                }
            }
        };
        mLogin = findViewById(R.id.loginButton);

        // Configure the ProgressDialog that will be shown if there is a
        // delay in presenting the user with the next sign in step.
        mConnectionProgressDialog = new ProgressDialog(this, R.style.DialogCustom);
        mConnectionProgressDialog.setMessage("Signing in...");
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(getApplicationContext(), "You got an error", LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            mConnectionProgressDialog = new ProgressDialog(this,R.style.DialogCustom);
            mConnectionProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            ProgressDialog.show(new ContextThemeWrapper(this, R.style.DialogCustom), "Signing In", "Please be patient...");
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }

            } else {
                // Google Sign In failed, update UI appropriately

                Toast.makeText(getApplicationContext(), "Something went wrong.",
                        LENGTH_SHORT).show();
                mConnectionProgressDialog.dismiss();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            mConnectionProgressDialog.dismiss();
                            //  updateUI(user);

                      } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    LENGTH_SHORT).show();
                            // updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void registerUser(final String etUsername, String etPassword , String etEmail){
        //showing progress dialog when registering user
        pDialog = new ProgressDialog(SignUpActivity.this);
        pDialog.setTitle("Signing In");
        pDialog.setMessage("Signing user, please be patient.");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

       mAuth.createUserWithEmailAndPassword(etEmail,etPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

           @Override
           public void onComplete(@NonNull Task<AuthResult> task) {
               //We have received a response, we need to close/exit the Progress Dialog now
               pDialog.dismiss();
               //checking if task is succesfully implemented
               if (task.isSuccessful()) {
                   FirebaseUser firebaseUser = mAuth.getCurrentUser();
                   String userId = null;
                   if (firebaseUser != null) {
                       userId = firebaseUser.getUid();
                   }
                   assert userId != null;
                   reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                       HashMap<String , String> hashMap = new HashMap<>();
                       hashMap.put("id", userId);
                       hashMap.put("username", etUsername);
                       hashMap.put("imageURL", "default");

                       reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull Task<Void> task) {
                               if (task.isSuccessful()){
                                  checkAdminStatus();
                               }
                           }
                       });

                   } else {
                       Toast.makeText(getApplicationContext(), "Oops! Something went wrong, try again.", LENGTH_SHORT).show();
                   }
               }
           });
       }
       private void checkAdminStatus(){
        if (mAuth.getCurrentUser().getEmail().equals("derrickwadek@gmail.com")){
            Intent intent = new Intent(getApplicationContext(), AdminPanelActivity.class);
            startActivity(intent);
            finish();
        }else {
            Intent intent = new Intent(getApplicationContext(), PastPapersActivity.class);
            startActivity(intent);
            finish();
        }
       }
    }
