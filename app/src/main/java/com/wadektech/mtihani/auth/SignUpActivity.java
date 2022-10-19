package com.wadektech.mtihani.auth;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
import com.wadektech.mtihani.R;
import com.wadektech.mtihani.room.User;
import com.wadektech.mtihani.ui.PastPapersActivity;
import com.wadektech.mtihani.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import timber.log.Timber;

import static android.widget.Toast.LENGTH_SHORT;


public class SignUpActivity extends AppCompatActivity {
    Button btnSignUp;
    EditText etPassword, etUsername, etEmail;
    TextView mLogin;
    private static final int RC_SIGN_IN = 1;
    DatabaseReference reference;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private static final String TAG = "wadektech";
    private ProgressDialog mConnectionProgressDialog;
    private FirebaseUser firebaseUser;
    private AlertDialog alertDialogAndroid;
    String user;
    private String imageURL;
    CheckBox mCheckPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etPassword = findViewById(R.id.et_password);
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        btnSignUp = findViewById(R.id.btnSignUp);
        mLogin = findViewById(R.id.btnLogin);
        mCheckPassword = findViewById(R.id.password_check);

        mLogin.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            finish();
            startActivity(intent);

        });

        mCheckPassword.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                // Show Password
                etPassword.setTransformationMethod(HideReturnsTransformationMethod
                        .getInstance());
            } else {
                // Hide Password
                etPassword.setTransformationMethod(PasswordTransformationMethod
                        .getInstance());
            }
        });

        mConnectionProgressDialog = new ProgressDialog(this);

        btnSignUp.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Please Enter Your Email Address!");
            } else if (TextUtils.isEmpty(password)) {
                etPassword.setError("Please Enter Secure Password!");
            } else if (TextUtils.isEmpty(username)) {
                etUsername.setError("Please Enter username!");
            } else {
                registerUser(username, email, password);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mLogin = findViewById(R.id.loginButton);

        mConnectionProgressDialog = new ProgressDialog(this, R.style.DialogCustom);
        mConnectionProgressDialog.setMessage("Signing in...");
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions
                .DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this,
                        connectionResult -> Toast.makeText(getApplicationContext(),
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
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            mConnectionProgressDialog = new ProgressDialog(SignUpActivity.this);
            mConnectionProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mConnectionProgressDialog.setTitle("Signing in...");
            mConnectionProgressDialog.setMessage("Please be patient");
            mConnectionProgressDialog.show();

            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } else {
                Toast.makeText(this, "Something went wrong.",
                        LENGTH_SHORT).show();
                mConnectionProgressDialog.dismiss();
            }
        }
    }

    public void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider
                .getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Timber.d("signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                        Calendar calendar = Calendar.getInstance();

                        String delegate = "hh:mm aaa";
                        String time = (String) DateFormat
                                .format(delegate, calendar.getTime());

                        String userId = "";
                        if (user != null) {
                            userId = user.getUid();
                        }
                        assert user != null;
                        reference = FirebaseDatabase
                                .getInstance()
                                .getReference("Users")
                                .child(user.getUid())
                                .child("status");
                        reference.keepSynced(true);

                        String name = user.getDisplayName();
                        Uri image = user.getPhotoUrl();
                        imageURL = "";
                        if (image != null) {
                            imageURL = image.toString();
                        } else {
                            imageURL = "default";
                        }

                        String saveCurrentTime;
                        String saveCurrentDate;
                        Calendar cal = Calendar.getInstance();


                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd");
                        saveCurrentDate = currentDate.format(cal.getTime());
                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
                        saveCurrentTime = currentTime.format(cal.getTime());

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("status", "offline");
                        hashMap.put("date", saveCurrentDate);
                        hashMap.put("time", saveCurrentTime);
                        reference.updateChildren(hashMap).addOnSuccessListener(aVoid -> {

                        }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(),
                                "Error saving data " + e.toString(), LENGTH_SHORT).show());

                        assert name != null;
                        User mUser = new User(userId, name, imageURL, "offline",
                                name.toLowerCase(),
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
                                        SharedPreferences pfs = getSharedPreferences(Constants
                                                .myPreferences, MODE_PRIVATE);
                                        SharedPreferences.Editor editor = pfs.edit();
                                        editor.putString(Constants.userId, user.getUid());
                                        editor.putString(Constants.userName, name);
                                        editor.putString(Constants.imageURL, imageURL);
                                        editor.putString(Constants.email, user.getEmail());
                                        editor.apply();

                                        Intent intent = new Intent(SignUpActivity.this,
                                                PastPapersActivity.class);
                                        finish();
                                        startActivity(intent);
                                    } else {
                                        if (task12.getException() != null)
                                            Timber.d("error:%s", task12.getException()
                                                    .toString());
                                        Toast.makeText(this, "Error saving data " +
                                                        Objects.requireNonNull(task12.getException())
                                                                .toString(),
                                                LENGTH_SHORT).show();
                                    }
                                });
                        mConnectionProgressDialog.dismiss();
                        //  updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Timber.e("Failed Registration%s", task.getException());
                        Toast.makeText(getApplicationContext(), "Authentication failed." +
                                task.getException(), LENGTH_SHORT).show();
                        // updateUI(null);
                    }// ...
                });
    }

    private void registerUser(String username, String email, String password) {
        //showing progress dialog when registering user
        mConnectionProgressDialog = new ProgressDialog(SignUpActivity.this);
        mConnectionProgressDialog.setTitle("Signing In");
        mConnectionProgressDialog.setMessage("Signing user, please be patient.");
        mConnectionProgressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            //We have received a response, we need to close/exit the Progress Dialog now
            mConnectionProgressDialog.dismiss();
            //checking if task is successfully implemented
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                String userId = null;
                if (firebaseUser != null) {
                    userId = firebaseUser.getUid();
                }

                assert userId != null;
                reference = FirebaseDatabase
                        .getInstance()
                        .getReference("Users")
                        .child(userId)
                        .child("status");
                reference.keepSynced(true);

                String saveCurrentTime;
                String saveCurrentDate;
                Calendar cal = Calendar.getInstance();

                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd");
                saveCurrentDate = currentDate.format(cal.getTime());
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
                saveCurrentTime = currentTime.format(cal.getTime());

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("status", "offline");
                hashMap.put("date", saveCurrentDate);
                hashMap.put("time", saveCurrentTime);
                reference.updateChildren(hashMap).addOnSuccessListener(aVoid -> {

                }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(),
                        "Error saving data " + e.toString(), LENGTH_SHORT).show());

                Calendar calendar = Calendar.getInstance();

                String delegate = "hh:mm aaa";
                String time = (String) DateFormat.format(delegate, calendar.getTime());

                User user = new User(userId,
                        username,
                        "default",
                        "offline",
                        username.toLowerCase(),
                        "Hello there! I use Mtihani Revise.",
                        time,
                        System.currentTimeMillis(), email);
                SharedPreferences pfs = getSharedPreferences(Constants
                        .myPreferences, MODE_PRIVATE);
                SharedPreferences.Editor editor = pfs.edit();
                editor.putString(Constants.userId, userId);
                editor.putString(Constants.userName, username);
                editor.putString(Constants.imageURL, "default");
                editor.putString(Constants.email, email);
                editor.apply();
                FirebaseFirestore
                        .getInstance()
                        .collection("Users")
                        .document(userId)
                        .set(user)
                        .addOnCompleteListener(this, task12 -> {
                            if (task12.isSuccessful()) {
                                Intent intent = new Intent(SignUpActivity.this,
                                        PastPapersActivity.class);
                                finish();
                                startActivity(intent);
                            } else {
                                if (task12.getException() != null)
                                    Timber.d("error:%s",
                                            task12.getException().toString());
                            }
                        });
            } else {
                Toast.makeText(this, "Oops! Something went wrong, try again." +
                        task.getException(), Toast.LENGTH_LONG).show();
            }
        });
    }

}

