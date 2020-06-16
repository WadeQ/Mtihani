package com.wadektech.mtihanirevise.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.utils.Constants;

import java.util.HashMap;
import java.util.Objects;

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
                    Log.d(TAG, "error updating status " +e.getMessage());

                });
            }

        });
    }

}
