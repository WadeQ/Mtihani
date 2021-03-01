package com.wadektech.mtihani.ui;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.wadektech.mtihani.R;
import com.wadektech.mtihani.utils.Constants;

import java.util.Objects;

import timber.log.Timber;

public class StatusUpdate extends AppCompatActivity {
    private TextInputLayout statusUpdate;
    DatabaseReference reference;
    FirebaseUser firebaseUser;
    private ProgressDialog pDialog;
    private CoordinatorLayout mCoordinate ;
    private static final String TAG = "StatusUpdate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_status_update);

        firebaseUser = FirebaseAuth.getInstance ().getCurrentUser ();
        Button statusBtnUpdate = findViewById (R.id.btnUpdate);
        statusUpdate = findViewById (R.id.etStatus);
        mCoordinate = findViewById(R.id.coordinator);

        statusBtnUpdate.setOnClickListener (view -> {
            String updatestatus = Objects.requireNonNull(statusUpdate.getEditText()).getText ().toString ();
            if (TextUtils.isEmpty(updatestatus)){
                statusUpdate.setError("Cannot be blank!");
                statusUpdate.requestFocus();
            } else {
                pDialog = new ProgressDialog (StatusUpdate.this);
                pDialog.setMessage ("Please be patient as we update your status");
                pDialog.show ();

                FirebaseFirestore
                        .getInstance()
                        .collection("Users")
                        .document(Constants.getUserId())
                        .update("update" , updatestatus)
                        .addOnCompleteListener(task -> {
                            pDialog.dismiss();
                            Snackbar snackbar = Snackbar.make(mCoordinate, "Successfully updated your status..." , Snackbar.LENGTH_SHORT);
                            snackbar.show();
                            //clear edit text
                            statusUpdate.getEditText().setText("");
                        }).addOnFailureListener(e -> {
                    pDialog.dismiss();
                    Timber.d("error updating status %s", e.getMessage());

                });
            }

        });
    }

}
