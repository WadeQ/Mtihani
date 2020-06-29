package com.wadektech.mtihanirevise.repository;

import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.wadektech.mtihanirevise.database.MessagesBoundaryCallback;
import com.wadektech.mtihanirevise.database.MtihaniDatabase;
import com.wadektech.mtihanirevise.database.UsersBoundaryCallback;
import com.wadektech.mtihanirevise.persistence.MtihaniRevise;
import com.wadektech.mtihanirevise.pojo.PDFObject;
import com.wadektech.mtihanirevise.pojo.SinglePDF;
import com.wadektech.mtihanirevise.room.Chat;
import com.wadektech.mtihanirevise.room.ChatItem;
import com.wadektech.mtihanirevise.room.User;
import com.wadektech.mtihanirevise.utils.Constants;
import com.wadektech.mtihanirevise.utils.InjectorUtils;
import com.wadektech.mtihanirevise.utils.MtihaniAppExecutors;
import com.wadektech.mtihanirevise.utils.SingleLiveEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import timber.log.Timber;

public class MtihaniRepository {
    private static final Object LOCK = new Object();
    private static MtihaniRepository sInstance;
    private static final String TAG = "MtihaniRepository";

    public synchronized static MtihaniRepository getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new MtihaniRepository ();
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

    public static LiveData<PagedList<Chat>> loadMessagesFromRoom(String myId, String userId) {
        downloadMessages(myId,userId);
        MessagesBoundaryCallback boundaryCallback = new MessagesBoundaryCallback(myId, userId);
        PagedList.Config pagedListConfig = (new PagedList.Config.Builder()
                .setPageSize(30)
                .setPrefetchDistance(5)
                .build());
        return new LivePagedListBuilder<> (MtihaniDatabase
                .getInstance(MtihaniRevise
                        .Companion
                        .getApp()
                        .getApplicationContext())
                .singleMessageDao()
                .getChatMessages(myId, userId), pagedListConfig)
                .setBoundaryCallback(boundaryCallback)
                .build();
    }

    private static void downloadMessages(String myId, String userId) {
        MtihaniDatabase db = MtihaniDatabase
                .getInstance(MtihaniRevise
                        .Companion
                .getApp()
                .getApplicationContext());
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference messages = firestore.collection("messages");

        List<Task<QuerySnapshot>> queryArrayList = new ArrayList<>();

        queryArrayList.add(messages.
                whereEqualTo("sender", myId)
                .whereEqualTo("receiver", userId)
                .whereLessThan("date", System.currentTimeMillis())
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(100)
                .get());

        queryArrayList.add(messages.
                whereEqualTo("sender", userId)
                .whereEqualTo("receiver", myId)
                .whereLessThan("date", System.currentTimeMillis())
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(100)
                .get());
        Task<List<Task<?>>> combinedTask = Tasks.whenAllComplete(queryArrayList
                .toArray(new Task[2]));
        combinedTask.addOnCompleteListener(MtihaniAppExecutors.getInstance().getDiskIO(),
                tasks -> {
                    if (tasks.getResult() != null) {
                        for (Task task : tasks.getResult()) {
                            if (task.isSuccessful()) {
                                QuerySnapshot snapshot = (QuerySnapshot) task.getResult();
                                assert snapshot != null;
                                if (!snapshot.isEmpty()) {
                                    List<Chat> chatList = new ArrayList<>();
                                    for(DocumentSnapshot document:snapshot.getDocuments()){
                                        Chat chat = document.toObject(Chat.class);
                                        if(chat != null){
                                            chat.setDocumentId(document.getId());
                                            chatList.add(chat);
                                        }
                                    }
                                    Log.d(TAG, "message chats received are: %s" + chatList.size());
                                    db.singleMessageDao()
                                            .saveChatsList(chatList);
                                } else {
                                    Log.d(TAG, "messages snapshot is empty");
                                }
                            } else {
                                if (task.getException() != null)
                                    Log.d(TAG, task.getException().toString());
                            }

                        }
                    }
                });
    }

    public static LiveData<PagedList<User>> loadUsersFromRoom() {
        UsersBoundaryCallback boundaryCallback = new UsersBoundaryCallback();
        PagedList.Config pagedListConfig = (new PagedList.Config.Builder()
                .setPrefetchDistance(5)
                .setEnablePlaceholders(true)
                .setPageSize(50)
                .build());
        return new LivePagedListBuilder<> (MtihaniDatabase
                .getInstance(MtihaniRevise
                        .Companion
                        .getApp()
                        .getApplicationContext())
                .usersDao()
                .getAllUsers(Constants.getUserId()), pagedListConfig)
                .setBoundaryCallback(boundaryCallback)
                .build();
    }


    public static LiveData<PagedList<User>> searchUsersFromRoom(String filter) {
        UsersBoundaryCallback boundaryCallback = new UsersBoundaryCallback();
        PagedList.Config pagedListConfig = (new PagedList.Config.Builder()
                .setPrefetchDistance(5)
                .setEnablePlaceholders(true)
                .setPageSize(50)
                .build());
        return new LivePagedListBuilder<> (MtihaniDatabase
                .getInstance(MtihaniRevise
                        .Companion
                        .getApp()
                        .getApplicationContext())
                .usersDao()
                .searchUsers(filter), pagedListConfig)
                .setBoundaryCallback(boundaryCallback)
                .build();
    }

    public static void saveMessage(Chat chat) {
        MtihaniAppExecutors
                .getInstance()
                .getDiskIO()
                .execute(() ->
                        MtihaniDatabase
                                .getInstance(MtihaniRevise
                                        .Companion
                                        .getApp()
                                        .getApplicationContext())
                                .singleMessageDao()
                                .add(chat));
    }

    public static void saveNewMessages(List<Chat> chats) {
        //Remember Room is accessed from a background thread at all times!
        MtihaniAppExecutors
                .getInstance()
                .getDiskIO()
                .execute(() ->
                        MtihaniDatabase
                                .getInstance(MtihaniRevise
                                        .Companion
                                        .getApp()
                                        .getApplicationContext())
                                .singleMessageDao()
                                .saveChatsList(chats));
    }

    public static void sendMessageToFirebase(Chat chat) {
        FirebaseFirestore.getInstance()
                .collection("messages")
                .document()
                .set(chat);

    }

    public static void onZeroUsersLoaded() {
        MtihaniDatabase db = MtihaniDatabase.getInstance(MtihaniRevise
                .Companion
                .getApp()
                .getApplicationContext());
        FirebaseFirestore
                .getInstance()
                .collection("Users")
                .whereLessThan("date", System.currentTimeMillis())
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnCompleteListener(MtihaniAppExecutors
                                .getInstance()
                                .getDiskIO()
                        , task -> {
                            if (task.isSuccessful()) {
                                if (task.getResult() != null) {
                                    if (!task.getResult().isEmpty()) {
                                        db
                                                .usersDao()
                                                .saveUsersList(task.getResult().toObjects(User.class));
                                    } else {
                                        Log.d(TAG, "onZeroUsersLoaded list is empty");
                                    }
                                }
                            } else {
                                if (task.getException() != null) {
                                    Log.d(TAG, "error onZeroUsersLoaded" + task.getException().toString());
                                }
                            }
                        });
    }

    public static void onUserAtFrontLoaded(User itemAtFront) {
        MtihaniDatabase db = MtihaniDatabase
                .getInstance(MtihaniRevise
                        .Companion
                        .getApp()
                        .getApplicationContext());
        FirebaseFirestore
                .getInstance()
                .collection("Users")
                .whereGreaterThan("date", itemAtFront.getDate())
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnCompleteListener(MtihaniAppExecutors
                                .getInstance()
                                .getDiskIO()
                        , task -> {
                            if (task.isSuccessful()) {
                                if (task.getResult() != null) {
                                    if (!task.getResult().isEmpty()) {
                                        db
                                                .usersDao()
                                                .saveUsersList(task.getResult().toObjects(User.class));
                                    } else {
                                        Log.d(TAG, "onUserAtFrontLoaded list is empty");
                                    }
                                }
                            } else {
                                if (task.getException() != null) {
                                    Log.d(TAG, "error onUserAtFrontLoaded" + task.getException().toString());
                                }
                            }
                        });
    }

    public static void onUserAtEndLoaded(User itemAtEnd) {
        MtihaniDatabase db = MtihaniDatabase
                .getInstance(MtihaniRevise
                        .Companion
                        .getApp()
                        .getApplicationContext());
        FirebaseFirestore
                .getInstance()
                .collection("Users")
                .whereLessThan("date", itemAtEnd.getDate())
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnCompleteListener(MtihaniAppExecutors
                                .getInstance()
                                .getDiskIO()
                        , task -> {
                            if (task.isSuccessful()) {
                                if (task.getResult() != null) {
                                    if (!task.getResult().isEmpty()) {
                                        db
                                                .usersDao()
                                                .saveUsersList(task.getResult().toObjects(User.class));
                                    } else {
                                        Log.d(TAG, "onUserAtEndLoaded list is empty");
                                    }
                                }
                            } else {
                                if (task.getException() != null) {
                                    Log.d(TAG, "error onUserAtEndLoaded" + task.getException().toString());
                                }
                            }
                        });
    }

    public static LiveData<PagedList<ChatItem>> loadChatList() {
        PagedList.Config pagedListConfig = (new PagedList.Config.Builder()
                .setPrefetchDistance(5)
                .setEnablePlaceholders(true)
                .setPageSize(50)
                .build());
        return new LivePagedListBuilder<> (MtihaniDatabase
                .getInstance(MtihaniRevise
                        .Companion
                        .getApp()
                        .getApplicationContext())
                .chatDao()
                .getAllChatUsers(), pagedListConfig)
                .build();
    }

    public static void saveChatListUser(ChatItem user) {
        MtihaniAppExecutors
                .getInstance()
                .getDiskIO()
                .execute(()-> MtihaniDatabase
                        .getInstance(MtihaniRevise
                                .Companion
                                .getApp()
                                .getApplicationContext())
                        .chatDao()
                        .addUser(user));
    }

    public static void updateMessage(Chat chat) {
        MtihaniAppExecutors
                .getInstance()
                .getDiskIO()
                .execute(()-> MtihaniDatabase
                        .getInstance(MtihaniRevise
                                .Companion
                                .getApp()
                                .getApplicationContext())
                        .singleMessageDao()
                        .update(chat));
        if(!Constants.getUserId().equals(chat.getSender())) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("seen", true);
            hashMap.put("documentId",chat.getDocumentId());
            FirebaseFirestore
                    .getInstance()
                    .collection("messages")
                    .document(chat.getDocumentId())
                    .update(hashMap);
        }
    }

    public static void downloadUsers() {
        MtihaniDatabase db = MtihaniDatabase.getInstance(MtihaniRevise
                .Companion.getApp().getApplicationContext());
        FirebaseFirestore
                .getInstance()
                .collection("Users")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnCompleteListener(MtihaniAppExecutors
                                .getInstance()
                                .getDiskIO()
                        , task -> {
                            if (task.isSuccessful()) {
                                if (task.getResult() != null) {
                                    if (!task.getResult().isEmpty()) {
                                        db
                                                .usersDao()
                                                .saveUsersList(task.getResult().toObjects(User.class));
                                    } else {
                                        Log.d(TAG, "downloadUsers list is empty");
                                    }
                                }
                            } else {
                                if (task.getException() != null) {
                                    Log.d(TAG, "error downloadUsers" + task.getException().toString());
                                }
                            }
                        });
    }

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

    /**
     * This method gets fired when the database has zero messages
     * when it is the case then the first 200 messages will be downloaded from the server
     *
     * @param myId
     * @param userId
     */
    public static void onZeroItemsLoaded(String myId, String userId) {
        MtihaniDatabase db = MtihaniDatabase.getInstance(MtihaniRevise
                .Companion
                .getApp().getApplicationContext());

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference messages = firestore.collection("messages");

        List<Task<QuerySnapshot>> queryArrayList = new ArrayList<>();

        queryArrayList.add(messages.
                whereEqualTo("sender", myId)
                .whereEqualTo("receiver", userId)
                .whereLessThan("date", System.currentTimeMillis())
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(50)
                .get());

        queryArrayList.add(messages.
                whereEqualTo("sender", userId)
                .whereEqualTo("receiver", myId)
                .whereLessThan("date", System.currentTimeMillis())
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(50)
                .get());
        Task<List<Task<?>>> combinedTask = Tasks.whenAllComplete(queryArrayList
                .toArray(new Task[2]));
        combinedTask.addOnCompleteListener(MtihaniAppExecutors.getInstance().getDiskIO(),
                tasks -> {
                    if (tasks.getResult() != null) {
                        for (Task task : tasks.getResult()) {
                            if (task.isSuccessful()) {
                                QuerySnapshot snapshot = (QuerySnapshot) task.getResult();
                                assert snapshot != null;
                                if (!snapshot.isEmpty()) {
                                    List<Chat> chatList = new ArrayList<>();
                                    for(DocumentSnapshot document:snapshot.getDocuments()){
                                        Chat chat = document.toObject(Chat.class);
                                        if(chat != null){
                                            chat.setDocumentId(document.getId());
                                            chatList.add(chat);
                                        }
                                    }
                                    Log.d(TAG, "onZeroItemLoaded chats received are: %s" + chatList.size());
                                    db.singleMessageDao()
                                            .saveChatsList(chatList);
                                } else {
                                    Log.d(TAG, "onZeroItemLoaded snapshot is empty");
                                }
                            } else {
                                if (task.getException() != null)
                                    Log.d(TAG, task.getException().toString());
                            }

                        }
                    }
                });
    }

    /**
     * This method will be fired after the first message has been loaded from the database
     * This method will seek to download any newer messages from the servers
     *
     * @param itemAtFront
     */
    public static void onItemAtFrontLoaded(Chat itemAtFront, String myId, String userId) {
        MtihaniDatabase db = MtihaniDatabase.getInstance(MtihaniRevise
                .Companion
                .getApp().getApplicationContext());
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference messages = firestore.collection("messages");

        List<Task<QuerySnapshot>> queryArrayList = new ArrayList<>();

        queryArrayList.add(messages.
                whereEqualTo("sender", myId)
                .whereEqualTo("receiver", userId)
                .whereLessThan("date", itemAtFront.getDate())
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(50)
                .get());

        queryArrayList.add(messages.
                whereEqualTo("sender", userId)
                .whereEqualTo("receiver", myId)
                .whereLessThan("date", itemAtFront.getDate())
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(50)
                .get());
        Task<List<Task<?>>> combinedTask = Tasks.whenAllComplete(queryArrayList
                .toArray(new Task[2]));
        combinedTask.addOnCompleteListener(MtihaniAppExecutors.getInstance().getDiskIO(),
                tasks -> {
                    if (tasks.getResult() != null) {
                        for (Task task : tasks.getResult()) {
                            if (task.isSuccessful()) {
                                QuerySnapshot snapshot = (QuerySnapshot) task.getResult();
                                assert snapshot != null;
                                if (!snapshot.isEmpty()) {
                                    List<Chat> chatList = new ArrayList<>();
                                    for(DocumentSnapshot document:snapshot.getDocuments()){
                                        Chat chat = document.toObject(Chat.class);
                                        if(chat != null){
                                            chat.setDocumentId(document.getId());
                                            chatList.add(chat);
                                        }
                                    }
                                    Log.d(TAG, "onItemAtFrontLoaded chats received are: %s" + chatList.size());
                                    db.singleMessageDao()
                                            .saveChatsList(chatList);
                                } else {
                                    Log.d(TAG, "onItemAtFrontLoaded snapshot is empty");
                                }
                            } else {
                                if (task.getException() != null)
                                    Log.d(TAG, task.getException().toString());
                            }

                        }
                    }
                });

    }

    /**
     * This method will be fired when the database loads the last remaining item
     * This is a clear indication that the local database has run out of items
     * hence we fetch more from firebase
     *
     * @param itemAtEnd
     */

    @SuppressLint("TimberArgCount")
    public static void onItemAtEndLoaded(Chat itemAtEnd, String myId, String userId) {
        MtihaniDatabase db = MtihaniDatabase.getInstance(Objects.requireNonNull(MtihaniRevise
                .Companion
                .getApp()).getApplicationContext());
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference messages = firestore.collection("messages");

        List<Task<QuerySnapshot>> queryArrayList = new ArrayList<>();

        queryArrayList.add(messages.
                whereEqualTo("sender", myId)
                .whereEqualTo("receiver", userId)
                .whereGreaterThan("date", itemAtEnd.getDate())
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(50)
                .get());

        queryArrayList.add(messages.
                whereEqualTo("sender", userId)
                .whereEqualTo("receiver", myId)
                .whereLessThan("date", itemAtEnd.getDate())
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(50)
                .get());
        Task<List<Task<?>>> combinedTask = Tasks.whenAllComplete(queryArrayList
                .toArray(new Task[2]));
        combinedTask.addOnCompleteListener(MtihaniAppExecutors.getInstance().getDiskIO(),
                tasks -> {
                    if (tasks.getResult() != null) {
                        for (Task task : tasks.getResult()) {
                            if (task.isSuccessful()) {
                                QuerySnapshot snapshot = (QuerySnapshot) task.getResult();
                                assert snapshot != null;
                                if (!snapshot.isEmpty()) {

                                    List<Chat> chatList = new ArrayList<>();
                                    for(DocumentSnapshot document:snapshot.getDocuments()){
                                       Chat chat = document.toObject(Chat.class);
                                       if(chat != null){
                                           chat.setDocumentId(document.getId());
                                           chatList.add(chat);
                                       }
                                    }
                                    Timber.d("onItemAtEndLoaded chats received are: ", +chatList.size());
                                    db.singleMessageDao()
                                            .saveChatsList(chatList);
                                } else {
                                    Timber.d("onItemAtEndLoaded snapshot is empty");
                                }
                            } else {
                                if (task.getException() != null)
                                    Timber.d(task.getException().toString());
                            }

                        }
                    }
                });
    }
}
