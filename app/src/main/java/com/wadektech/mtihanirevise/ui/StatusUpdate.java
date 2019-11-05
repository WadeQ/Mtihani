package com.wadektech.mtihanirevise.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
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
            pDialog = new ProgressDialog (StatusUpdate.this);
            pDialog.setMessage ("Please be patient as we update your status");
            pDialog.show ();

            String updatestatus = statusUpdate.getEditText ().getText ().toString ();

            FirebaseFirestore
                    .getInstance()
                    .collection("Users")
                    .document(Constants.getUserId())
                    .update("update" , updatestatus)
                    .addOnCompleteListener(task -> {
                        Snackbar snackbar = Snackbar.make(mCoordinate, "Successfully updated your status..." , Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        //clear edit text
                        statusUpdate.getEditText().setText("");
                    }).addOnFailureListener(e -> {

                Log.d(TAG, "error updating status " +e.getMessage());

                    });
        });

    }

}
