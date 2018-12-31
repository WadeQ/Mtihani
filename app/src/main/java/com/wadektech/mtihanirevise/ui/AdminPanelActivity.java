package com.wadektech.mtihanirevise.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.pojo.PDFObject;
import com.wadektech.mtihanirevise.utils.InjectorUtils;
import com.wadektech.mtihanirevise.utils.ReselectableSpinner;
import com.wadektech.mtihanirevise.viewmodels.AdminPanelViewModel;

import java.util.ArrayList;
import java.util.List;

public class AdminPanelActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Button btnSelect;
    private BarChart barChart;
    private static final int GALLERY_REQUEST_CODE = 23;
    private static final int PDF_REQUEST_CODE = 34;
    private TextView notify;
    private ReselectableSpinner mSpinner;
    private String fileName;
    private List<PDFObject> pdfObjects;
    private String category = "";
    private ProgressBar progressBar;
    private String[] input = {"NOT SET", "KCSE 2000", "KCSE 2001", "KCSE 2002", "KCSE 2003", "KCSE 2004","KCSE 2005",
            "KCSE 2006","KCSE 2007","KCSE 2008","KCSE 2009","KCSE 2010","KCSE 2011","KCSE 2012",
            "KCSE 2013", "KCSE 2014", "KCSE 2015", "KCSE 2016", "KCSE 2017", "KCSE 2018","KCSE ANSWERS"};

    FirebaseStorage storage;
    FirebaseDatabase database;

    ProgressDialog pDialog;
    Uri pdfUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Admin Dashboard");
        setSupportActionBar(toolbar);
        progressBar = findViewById(R.id.progressBar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        btnSelect = findViewById(R.id.btn_select);
        Button btnUpload = findViewById(R.id.btn_upload);
        mSpinner = findViewById(R.id.categorySpinner);
        ArrayAdapter<String> categoryAdapter = InjectorUtils.provideCategoryAdapter(this, input);
        mSpinner.setAdapter(categoryAdapter);
        mSpinner.setOnItemSelectedListener(this);

        barChart = findViewById(R.id.user_bar_graph);
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(44f, 0));
        barEntries.add(new BarEntry(88f, 1));
        barEntries.add(new BarEntry(66f, 2));
        barEntries.add(new BarEntry(42f, 3));
        barEntries.add(new BarEntry(58f, 4));
        barEntries.add(new BarEntry(91f, 5));

        BarDataSet barDataSet = new BarDataSet(barEntries, "Dates");

        ArrayList<String> dates = new ArrayList<>();
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

        btnSelect.setOnClickListener(v -> {
            preCheck();
            /*if (ContextCompat.checkSelfPermission(AdminPanelActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                selectPdf();
            }else {
                ActivityCompat.requestPermissions(AdminPanelActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 9);
            }*/
        });

        btnUpload.setOnClickListener(v -> {
            if (pdfObjects != null && pdfObjects.size()>0) {
                /*uploadPDF(pdfUri);*/
                if (!category.isEmpty() && !category.equals("NOT SET")) {
                    AdminPanelViewModel viewModel = ViewModelProviders.of(AdminPanelActivity.this)
                            .get(AdminPanelViewModel.class);
                    progressBar.setVisibility(View.VISIBLE);
                    viewModel.uploadMultiplePDFs(pdfObjects,category);
                    Toast.makeText(this, "Uploading pdf file, please wait...", Toast.LENGTH_SHORT).show();
                    viewModel.getUploadProgress().observe(this, this::onResponseReceived);
                } else {
                    Toast.makeText(this, "Please choose category", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(getApplicationContext(), "Please select a file!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onResponseReceived(String response) {
        if (response != null) {
            progressBar.setVisibility(View.INVISIBLE);
            if (response.equals("success")) {
                Snackbar.make(progressBar, "PDF file has been uploaded",
                        Snackbar.LENGTH_LONG)
                        .show();
            } else {
                Toast.makeText(this, "An error has occurred while uploading PDF", Toast.LENGTH_SHORT).show();
            }
        }
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
   /* @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 9 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            selectPdf();
        }else {
            Toast.makeText(AdminPanelActivity.this, "Please provide permission!" , Toast.LENGTH_SHORT).show();
        }
    }*/

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
        if (requestCode == PDF_REQUEST_CODE && resultCode == RESULT_OK && data.getClipData() != null) {
            int totalPdfSelected = data.getClipData().getItemCount();
            pdfObjects = new ArrayList<>();
            for (int i = 0; i < totalPdfSelected; i++) {
                pdfUri = data.getClipData().getItemAt(i).getUri();
                pdfObjects.add(new PDFObject(getFileName(pdfUri),pdfUri));

                /*final ProgressDialog pDialog = new ProgressDialog(this);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setTitle("Uploading File...");
                pDialog.setProgress(0);
                pDialog.show();

                final String fileNameTextView = System.currentTimeMillis() + "";
                final StorageReference storageReference = storage.getReference();
                storageReference.child("pdf").child(fileNameTextView).putFile(pdfUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //return url of uploaded file
                                String url = storageReference.getDownloadUrl().toString();
                                //store url to realtime database
                                DatabaseReference databaseReference = database.getReference().child("pdf").child("2012");
                                //return path to root
                                databaseReference.child(fileNameTextView).setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "File succesfully uploaded.", Toast.LENGTH_SHORT).show();
                                            pDialog.dismiss();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "File not uploaded!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "File not uploaded!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        //track uploading of file
                        int currentProgress = (int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        pDialog.setProgress(currentProgress);
                    }
                });*/
            }

        } else if (data.getData() != null) {
            pdfUri = data.getData();
        } else {
            Toast.makeText(AdminPanelActivity.this, "Please select a file!", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadPDF(Uri pdfUri) {
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setTitle("Uploading File...");
        pDialog.setProgress(0);
        pDialog.show();

        final String fileName = System.currentTimeMillis() + ".pdf";
        final StorageReference storageReference = storage.getReference();
        storageReference.child("pdf").child(fileName).putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //return url of uploaded file
                        String url = storageReference.getDownloadUrl().toString();
                        //store url to realtime database
                        DatabaseReference databaseReference = database.getReference().child("2012");
                        //return path to root
                        databaseReference.child(fileName).setValue(url).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "File succesfully uploaded.", Toast.LENGTH_SHORT).show();
                                pDialog.dismiss();
                            } else {
                                Toast.makeText(getApplicationContext(), "File not uploaded!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "File not uploaded!", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                //track uploading of file
                int currentProgress = (int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                pDialog.setProgress(currentProgress);
            }
        });
    }

    private void preCheck() {
        //check permissions first!
        int gallery_permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (gallery_permission != PackageManager.PERMISSION_GRANTED) {
                //check if permission has ever been denied
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Permission to access files is required for Mtihani Revise to choose PDF files")
                            .setTitle("Permission Required")
                            .setPositiveButton("OK", (dialog, which) -> ask_Gallery_Permission()).setNegativeButton("CANCEL", (dialog, which) -> {
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    //first time this permission is being asked
                    ask_Gallery_Permission();
                }
            } else {
                //permission is granted.proceed to file directory
                choosePDF();
            }

        } else {
            //choose PDF. device is pre mashmallow
            choosePDF();
        }
    }

    /*@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PDF_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data.getData() != null) {
                    pdfUri = data.getData();
                    fileName=getFileName(pdfUri);
                    fileNameTextView.setText(fileName);

                }
            } else {
                Toast.makeText(this, "cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }*/


    private void choosePDF() {
      /*  Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");*/
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // startActivityForResult(intent, 86);

        startActivityForResult(Intent.createChooser(intent, "Choose PDF"), PDF_REQUEST_CODE);
    }

    public void ask_Gallery_Permission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, GALLERY_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == GALLERY_REQUEST_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pdfChooser();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    private void pdfChooser() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(Intent.createChooser(intent, "Choose PDF"), PDF_REQUEST_CODE);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        category = mSpinner.getSelectedItem().toString().trim();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                }
            } finally {
                cursor.close();
            }
            if (result == null) {
                result = uri.getPath();
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }

        }
        return result;
    }
}
