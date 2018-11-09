package com.wadektech.mtihanirevise;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class StatusUpdate extends Activity {

    private TextInputLayout statusUpdate ;
    private Button statusBtnUpdate ;

    DatabaseReference reference ;
    FirebaseUser firebaseUser ;

    private ProgressDialog pDialog ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_status_update);

        statusBtnUpdate = findViewById (R.id.btnUpdate);
        statusUpdate = findViewById (R.id.etStatus);

        statusBtnUpdate.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                //progress dialog
                pDialog = new ProgressDialog(StatusUpdate.this);
                pDialog.setTitle("Updating Status...");
                pDialog.setMessage("Updating status, please be patient.");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(false);
                pDialog.show();
                String status = statusUpdate.getEditText ().getText ().toString () ;
                reference = FirebaseDatabase.getInstance ().getReference ().child ("Users").child (firebaseUser.getUid ());

                reference.child ("User").child ("update").setValue (status).addOnCompleteListener (new OnCompleteListener<Void> () {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful ()){
                            pDialog.dismiss ();
                        }else {
                            Toast.makeText (getApplicationContext (), "Error saving status, please try again!", Toast.LENGTH_SHORT).show ();
                        }
                    }
                });
            }
        });
    }

}
