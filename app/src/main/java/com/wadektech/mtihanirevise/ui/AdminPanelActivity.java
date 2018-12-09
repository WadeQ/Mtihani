package com.wadektech.mtihanirevise.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.wadektech.mtihanirevise.R;

import java.util.ArrayList;

public class AdminPanelActivity extends AppCompatActivity {
     private Button btnSelect, btnUpload ;
     private BarChart barChart ;
     FirebaseStorage storage ;
     FirebaseDatabase database;
     Uri pdfUri ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Admin Dashboard");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

       btnSelect = findViewById(R.id.btn_select);
       btnUpload = findViewById(R.id.btn_upload);

        barChart = findViewById(R.id.user_bar_graph);
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(44f,0));
        barEntries.add(new BarEntry(88f,1));
        barEntries.add(new BarEntry(66f,2));
        barEntries.add(new BarEntry(42f,3));
        barEntries.add(new BarEntry(58f,4));
        barEntries.add(new BarEntry(91f,5));

        BarDataSet barDataSet = new BarDataSet(barEntries, "Dates");

        ArrayList<String> dates= new ArrayList<>();
        dates.add("April");
        dates.add("May");
        dates.add("June");
        dates.add("July");
        dates.add("August");
        dates.add("September");

        BarData data = new BarData(dates, barDataSet);
        barChart.setData(data);
        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);

        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(AdminPanelActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                    selectPdf();
                }else {
                    ActivityCompat.requestPermissions(AdminPanelActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 9);
                }
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pdfUri != null){
                    uploadPDF(pdfUri);
                } else {
                    Toast.makeText(getApplicationContext(),"Please select a file!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_admin, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menuNavigate) {
            startActivity(new Intent(getApplicationContext(), PastPapersActivity.class));
            return true;
        }
        if (id == R.id.menu_broadcast_message) {
                //call the broadcast message method here
                return true;

            }
            return super.onOptionsItemSelected(item);
        }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 9 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            selectPdf();
        }else {
            Toast.makeText(AdminPanelActivity.this, "Please provide permission!" , Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("InlinedApi")
    private void selectPdf() {
        //allow user to select a file using file manager and intent
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 86);
    }
    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //check if file has been selected by user
        if (requestCode == 86 && resultCode == RESULT_OK && data.getClipData()!=null){
            int totalPdfSelected = data.getClipData().getItemCount();
            for (int i = 0 ; i < totalPdfSelected ; i++) {
                pdfUri = data.getClipData().getItemAt(i).getUri();
            }
        }else if (data.getData()!= null){
            pdfUri = data.getData();
        }else {
            Toast.makeText(AdminPanelActivity.this, "Please select a file!", Toast.LENGTH_SHORT).show();
        }
    }
    private void uploadPDF(Uri pdfUri) {
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setTitle("Uploading File...");
        pDialog.setProgress(0);
        pDialog.show();

        final String fileName = System.currentTimeMillis()+"" ;
        final String fileName1 = System.currentTimeMillis ()+"";

        final StorageReference storageReference = storage.getReference() ;
        storageReference.child("pdf").child(fileName).putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //return url of uploaded file
                        String url = storageReference.getDownloadUrl().toString();
                        //store url to realtime database
                        DatabaseReference databaseReference = database.getReference().child("pdf").child("2011");
                        //return path to root
                        databaseReference.child(fileName1).setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(getApplicationContext(),"File succesfully uploaded.",Toast.LENGTH_SHORT).show();
                                    pDialog.dismiss();
                                } else {
                                    Toast.makeText(getApplicationContext(),"File not uploaded!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"File not uploaded!", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                //track uploading of file
                int currentProgress = (int) (100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                pDialog.setProgress(currentProgress);
            }
        });
    }
}

