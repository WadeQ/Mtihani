package com.wadektech.mtihanirevise.auth;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.room.User;
import com.wadektech.mtihanirevise.ui.PastPapersActivity;
import com.wadektech.mtihanirevise.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import static android.widget.Toast.LENGTH_SHORT;


public class SignUpActivity extends AppCompatActivity {

    Button mLogin, btnSignUp;
    EditText etPassword, etUsername, etEmail;
    private static final int RC_SIGN_IN = 1;
    DatabaseReference reference;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private static final String TAG = "wadektech";
    // private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog mConnectionProgressDialog;
    private FirebaseUser firebaseUser;
    private AlertDialog alertDialogAndroid;
    String user;
    private String imageURL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etPassword = findViewById(R.id.et_password);
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        btnSignUp = findViewById(R.id.btnSignUp);
        mLogin = findViewById(R.id.btnLogin);

        mLogin.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            finish();
            startActivity(intent);

        });

        // mAuth = FirebaseAuth.getInstance() ;
        mConnectionProgressDialog = new ProgressDialog(this);

        btnSignUp.setOnClickListener(v -> {
            String userNmae = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Please Enter Your Email Address!");
            } else if (TextUtils.isEmpty(password)) {
                etPassword.setError("Please Enter Secure Password!");
            } else if (TextUtils.isEmpty(userNmae)) {
                etUsername.setError("Please Enter username!");
            } else {
                registerUser(password, userNmae, email);
            }
        });
        //initialize the firebase object
        mAuth = FirebaseAuth.getInstance();
       /* mAuthListener = firebaseAuth -> {
            if (firebaseAuth.getCurrentUser() != null) {
                checkAdminStatus ();
            }
        };*/
        mLogin = findViewById(R.id.loginButton);

        // Configure the ProgressDialog that will be shown if there is a
        // delay in presenting the user with the next sign in step.
        mConnectionProgressDialog = new ProgressDialog(this, R.style.DialogCustom);
        mConnectionProgressDialog.setMessage("Signing in...");
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, connectionResult -> Toast.makeText(getApplicationContext(),
                        "You got an error", LENGTH_SHORT).show())
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mLogin.setOnClickListener(view -> signIn());
    }

    @Override
    protected void onStart() {
        super.onStart();
        //mAuth.addAuthStateListener(mAuthListener);

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
            mConnectionProgressDialog = new ProgressDialog(SignUpActivity.this);
            mConnectionProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mConnectionProgressDialog.setTitle("Signing in...");
            mConnectionProgressDialog.setMessage("Please be patient");
            mConnectionProgressDialog.show();

            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }

            } else {
                // Google Sign In failed, update UI appropriately

                Toast.makeText(this, "Something went wrong.",
                        LENGTH_SHORT).show();
                mConnectionProgressDialog.dismiss();
                // ...
            }
        }
    }

    public void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                        Calendar calendar = Calendar.getInstance();

                        String delegate = "hh:mm aaa";
                        String time = (String) DateFormat.format(delegate, calendar.getTime());

                        String userId = "";
                        if (user != null) {
                            userId = user.getUid();
                        }
                        assert user != null;
                        reference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
                        reference.keepSynced(true);

                        String name = user.getDisplayName();
                        Uri image = user.getPhotoUrl();
                        String imageUrl = "";
                        imageURL = "";
                        if (image != null) {
                            imageUrl = image.toString();
                            imageURL = image.toString();
                        } else {
                            imageURL = "default";
                        }
                       HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("status", "offline");
                        reference.updateChildren (hashMap).addOnSuccessListener (aVoid -> {

                        }).addOnFailureListener (e -> Toast.makeText (getApplicationContext (),
                                "Error saving data " + e.toString () , LENGTH_SHORT).show ());


                        User mUser = new User(userId, name, imageURL, "offline", name.toLowerCase(),
                                "Hello there! I use Mtihani Revise.",
                                time, System.currentTimeMillis(), user.getEmail());
                        FirebaseFirestore
                                .getInstance()
                                .collection("Users")
                                .document(userId)
                                .set(mUser)
                                .addOnCompleteListener(this, task12 -> {
                                    if (task12.isSuccessful()) {
                                        //storing user details for quick access later
                                        SharedPreferences pfs = getSharedPreferences(Constants.myPreferences, MODE_PRIVATE);
                                        SharedPreferences.Editor editor = pfs.edit();
                                        editor.putString(Constants.userId, user.getUid());
                                        editor.putString(Constants.userName, name);
                                        editor.putString(Constants.imageURL, imageURL);
                                        editor.putString(Constants.email, user.getEmail());
                                        editor.apply();

                                        Intent intent = new Intent(SignUpActivity.this, PastPapersActivity.class);
                                        finish();
                                        startActivity(intent);
                                    } else {
                                        if (task12.getException() != null)
                                            Log.d(TAG, "error:" + task12.getException().toString());
                                        Toast.makeText(this, "Error saving data " +
                                                task12.getException().toString(), LENGTH_SHORT).show();
                                    }
                                });
                        mConnectionProgressDialog.dismiss();
                        //  updateUI(user);

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.e("SignupActivity", "Failed Registration" + task.getException());
                        Toast.makeText(getApplicationContext(), "Authentication failed." +
                                task.getException(), LENGTH_SHORT).show();
                        // updateUI(null);
                    }// ...
                });
    }

    private void registerUser(final String etUsername, String etPassword, String etEmail) {
        //showing progress dialog when registering user
        mConnectionProgressDialog = new ProgressDialog(SignUpActivity.this);
        mConnectionProgressDialog.setTitle("Signing In");
        mConnectionProgressDialog.setMessage("Signing user, please be patient.");
        mConnectionProgressDialog.show();

        mAuth.createUserWithEmailAndPassword(etEmail, etPassword).addOnCompleteListener(task -> {
            //We have received a response, we need to close/exit the Progress Dialog now
            mConnectionProgressDialog.dismiss();
            //checking if task is succesfully implemented
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                String userId = null;
                if (firebaseUser != null) {
                    userId = firebaseUser.getUid();
                }
                assert userId != null;
                reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("status", "offline");
                reference.updateChildren (hashMap).addOnSuccessListener (aVoid -> {

                }).addOnFailureListener (e -> Toast.makeText (getApplicationContext (),
                        "Error saving data " + e.toString () , LENGTH_SHORT).show ());

                Calendar calendar = Calendar.getInstance();

                String delegate = "hh:mm aaa";
                String time = (String) DateFormat.format(delegate, calendar.getTime());

                User user = new User(userId, etUsername, "default", "offline", etUsername.toLowerCase(),
                        "Hello there! I use Mtihani Revise.", time, System.currentTimeMillis(), etEmail);
                SharedPreferences pfs = getSharedPreferences(Constants.myPreferences, MODE_PRIVATE);
                SharedPreferences.Editor editor = pfs.edit();
                editor.putString(Constants.userId, userId);
                editor.putString(Constants.userName, etUsername);
                editor.putString(Constants.imageURL, "default");
                editor.putString(Constants.email, etEmail);
                editor.apply();
                FirebaseFirestore
                        .getInstance()
                        .collection("Users")
                        .document(userId)
                        .set(user)
                        .addOnCompleteListener(this, task12 -> {
                            if (task12.isSuccessful()) {
                                Intent intent = new Intent(SignUpActivity.this, PastPapersActivity.class);
                                finish();
                                startActivity(intent);

                            } else {
                                if (task12.getException() != null)
                                    Log.d(TAG, "error:" + task12.getException().toString());
                            }
                        });
            } else {
                Toast.makeText(this, "Oops! Something went wrong, try again." +
                        task.getException(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

