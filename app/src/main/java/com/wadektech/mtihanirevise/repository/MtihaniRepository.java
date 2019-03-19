package com.wadektech.mtihanirevise.repository;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.wadektech.mtihanirevise.pojo.PDFObject;
import com.wadektech.mtihanirevise.pojo.SinglePDF;
import com.wadektech.mtihanirevise.utils.InjectorUtils;
import com.wadektech.mtihanirevise.utils.SingleLiveEvent;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MtihaniRepository {
    private static final Object LOCK = new Object();
    private static MtihaniRepository sInstance;
    private final String TAG = "MtihaniRepository";

    public synchronized static MtihaniRepository getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new MtihaniRepository();
            }
        }
        return sInstance;
    }

    private SingleLiveEvent<String> adminPassword;
    private SingleLiveEvent<String> uploadResponse;
    private SingleLiveEvent<List<SinglePDF>> pdfPerCategoryResponse;
    private SingleLiveEvent<String> pdfsDownloadResponse;
    private SingleLiveEvent<String> singlePDFDownloadResponse;
    private SingleLiveEvent<Integer> progressUpdate;

    public SingleLiveEvent<String> getAdminPasswordResponse() {
        if (adminPassword != null) {
            return adminPassword;
        } else {
            adminPassword = InjectorUtils.provideSingleLiveEvent();
            return adminPassword;
        }
    }

    public SingleLiveEvent<String> getSinglePDFDownloadResponse() {
        if (singlePDFDownloadResponse != null) {
            return singlePDFDownloadResponse;
        } else {
            singlePDFDownloadResponse = InjectorUtils.provideSingleLiveEvent();
            return singlePDFDownloadResponse;
        }
    }

    public SingleLiveEvent<Integer> getProgressUpdate() {
        if (progressUpdate != null) {
            return progressUpdate;
        } else {
            progressUpdate = InjectorUtils.provideIntSingleLiveEvent();
            return progressUpdate;
        }
    }

    public SingleLiveEvent<List<SinglePDF>> getPdfPerCategoryResponse() {
        if (pdfPerCategoryResponse != null) {
            return pdfPerCategoryResponse;
        } else {
            pdfPerCategoryResponse = InjectorUtils.provideListSingleLiveEvent();
            return pdfPerCategoryResponse;
        }
    }

    public void getAdminPassword() {
        adminPassword = InjectorUtils.provideSingleLiveEvent();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference password = db.collection("admin_password");
        password.document("password_id")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        if (snapshot.get("password") != null) {
                            adminPassword.setValue(snapshot.get("password").toString());
                            adminPassword = null;
                        } else {
                            adminPassword.setValue("password is empty");
                            adminPassword = null;

                        }

                    }
                })
                .addOnFailureListener(e -> adminPassword.setValue("Unable to authenticate, please try again"));
    }

    public SingleLiveEvent<String> getUploadResponse() {
        if (uploadResponse != null) {
            return uploadResponse;
        } else {
            uploadResponse = InjectorUtils.provideSingleLiveEvent();
            return uploadResponse;
        }
    }

    public SingleLiveEvent<String> getPdfsDownloadResponse() {
        if (pdfsDownloadResponse != null) {
            return pdfsDownloadResponse;
        } else {
            pdfsDownloadResponse = InjectorUtils.provideSingleLiveEvent();
            return pdfsDownloadResponse;
        }
    }

    public void uploadPDFFile(Uri uri, String category, String fileName) {
        if (uploadResponse == null)
            uploadResponse = InjectorUtils.provideSingleLiveEvent();
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference ref = firebaseStorage.getReference("PDF_Files");
        StorageReference timetable = ref.child(fileName);
        timetable.putFile(uri)
                .continueWithTask(task -> {
                    // Forward any exceptions
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }

                    return timetable.getDownloadUrl();
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String pdfUrl = downloadUri.toString();
                        uploadResponse.setValue("success");
                        uploadResponse = null;
                        savePDFDownloadUrlInDb(pdfUrl, category, fileName);

                    } else {
                        uploadResponse.setValue("fail");
                        uploadResponse = null;
                    }

                });
    }

    private void savePDFDownloadUrlInDb(String pdfUrl, String category, String fileName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> map = InjectorUtils.provideStringHashMap();
        map.put("pdfUrl", pdfUrl);
        map.put("category", category);
        map.put("fileName", fileName);
        CollectionReference ref = db.collection("PDFs");
        ref.document()
                .set(map)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "token sent to server!"))
                .addOnFailureListener(e -> Log.d(TAG, "failed to send token to server: %s" + e.toString()));
    }

    public void downloadPDFPerCategory(String category) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference ref = db.collection("PDFs");
        ref.whereEqualTo("category", category)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        pdfsDownloadResponse.setValue("loaded");
                        pdfPerCategoryResponse.setValue(snapshot.toObjects(SinglePDF.class));

                    } else {
                        pdfsDownloadResponse.setValue("empty");
                    }
                })
                .addOnFailureListener(e -> pdfsDownloadResponse.setValue(e.toString()));
    }

    public void downloadPDF(String fileName) {
        if (progressUpdate == null)
            progressUpdate = InjectorUtils.provideIntSingleLiveEvent();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference("PDF_Files");
        StorageReference islandRef = storageRef.child(fileName);

        File rootPath = new File(Environment.getExternalStorageDirectory(), "Mtihani");
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }

        final File localFile = new File(rootPath, fileName);

        islandRef.getFile(localFile)
                .addOnProgressListener(taskSnapshot -> {
                    int count = (int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressUpdate.setValue(count);
                })
                .addOnSuccessListener(taskSnapshot -> {
                    // Log.e("firebase ", ";local tem file created  created " + localFile.toString());
                    //  updateDb(timestamp,localFile.toString(),position);
                    singlePDFDownloadResponse.setValue("success");
                }).addOnFailureListener(exception -> {
            // Log.e("firebase ", ";local tem file not created  created " + exception.toString());
            singlePDFDownloadResponse.setValue("An error occurred");
        });

    }

    public void uploadPDFs(List<PDFObject> pdfObjects, String category) {
        if (uploadResponse == null)
            uploadResponse = InjectorUtils.provideSingleLiveEvent();
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference ref = firebaseStorage.getReference("PDF_Files");

        for (int i = 0; i < pdfObjects.size(); i++) {
            int count = i;
            PDFObject pdf = pdfObjects.get(i);
            StorageReference pdfUpload = ref.child(pdf.getFileName());
            pdfUpload.putFile(pdf.getPdfUri())
                    .continueWithTask(task -> {
                        // Forward any exceptions
                        if (!task.isSuccessful()) {
                            throw Objects.requireNonNull(task.getException());
                        }

                        return pdfUpload.getDownloadUrl();
                    })
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            String pdfUrl = downloadUri.toString();
                            if (count == pdfObjects.size() - 1) {
                                uploadResponse.setValue("success");
                            }

                            savePDFDownloadUrlInDb(pdfUrl, category, pdf.getFileName());

                        } else {
                            uploadResponse.setValue("fail");
                            uploadResponse = null;
                        }

                    });


        }


    }
}
