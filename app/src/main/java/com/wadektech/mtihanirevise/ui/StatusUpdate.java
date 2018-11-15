package com.wadektech.mtihanirevise.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wadektech.mtihanirevise.R;

import java.util.HashMap;

public class StatusUpdate extends AppCompatActivity {

    private TextInputLayout statusUpdate;

    DatabaseReference reference;
    FirebaseUser firebaseUser;

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_status_update);

        firebaseUser = FirebaseAuth.getInstance ().getCurrentUser ();
        Button statusBtnUpdate = findViewById (R.id.btnUpdate);
        statusUpdate = findViewById (R.id.etStatus);

        statusBtnUpdate.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                pDialog = new ProgressDialog (StatusUpdate.this);
                pDialog.setTitle ("Updating Status...");
                pDialog.setMessage ("Please be patient as we update your status.");
                pDialog.show ();

                String updatestatus = statusUpdate.getEditText ().getText ().toString ();
                reference = FirebaseDatabase.getInstance ().getReference ("Users").child (firebaseUser.getUid ());
                HashMap<String, Object> hashMap = new HashMap<> ();
                hashMap.put ("update", updatestatus);
                reference.updateChildren(hashMap).addOnCompleteListener (new OnCompleteListener<Void> () {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful ()){
                            pDialog.dismiss ();
                            finish ();
                        }else {
                            Toast.makeText (getApplicationContext (), "There was an error updating your status!", Toast.LENGTH_SHORT).show ();
                        }
                    }
                });

            }
        });

    }

}
