package com.wadektech.mtihani.core.repository

import android.annotation.SuppressLint
import android.util.Log
import com.wadektech.mtihani.app.MtihaniRevise.Companion.app
import com.wadektech.mtihani.core.SingleLiveEvent
import com.wadektech.mtihani.pdf.domain.pojo.SinglePDF
import com.wadektech.mtihani.core.InjectorUtils
import timber.log.Timber
import com.google.firebase.storage.FirebaseStorage
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.wadektech.mtihani.chat.data.localDatasource.room.Chat
import com.wadektech.mtihani.chat.data.firebaseDataSource.MessagesBoundaryCallback
import androidx.paging.LivePagedListBuilder
import com.google.android.gms.tasks.*
import com.google.firebase.firestore.*
import com.wadektech.mtihani.core.MtihaniAppExecutors
import com.google.firebase.storage.FileDownloadTask
import com.wadektech.mtihani.chat.data.localDatasource.MtihaniDatabase
import com.wadektech.mtihani.core.Constants
import com.wadektech.mtihani.chat.data.firebaseDataSource.UsersBoundaryCallback
import com.wadektech.mtihani.chat.data.localDatasource.room.ChatItem
import com.wadektech.mtihani.chat.data.localDatasource.room.User
import java.io.File
import java.lang.Exception
import java.util.*

class MtihaniRepository {
    private var adminPassword: SingleLiveEvent<String>? = null
    var uploadResponse: SingleLiveEvent<String>? = null
        get() = if (field != null) {
            field
        } else {
            field = InjectorUtils.provideSingleLiveEvent()
            field
        }
        private set
    private var pdfPerCategoryResponse: SingleLiveEvent<List<SinglePDF>>? = null
    private var pdfsDownloadResponse: SingleLiveEvent<String>? = null
    private var singlePDFDownloadResponse: SingleLiveEvent<String>? = null
    private var progressUpdate: SingleLiveEvent<Int>? = null
    val adminPasswordResponse: SingleLiveEvent<String>?
        get() = if (adminPassword != null) {
            adminPassword
        } else {
            adminPassword = InjectorUtils.provideSingleLiveEvent()
            adminPassword
        }

    fun getSinglePDFDownloadResponse(): SingleLiveEvent<String>? {
        return if (singlePDFDownloadResponse != null) {
            singlePDFDownloadResponse
        } else {
            singlePDFDownloadResponse = InjectorUtils.provideSingleLiveEvent()
            singlePDFDownloadResponse
        }
    }

    fun getProgressUpdate(): SingleLiveEvent<Int>? {
        return if (progressUpdate != null) {
            progressUpdate
        } else {
            progressUpdate = InjectorUtils.provideIntSingleLiveEvent()
            progressUpdate
        }
    }

    fun getPdfPerCategoryResponse(): SingleLiveEvent<List<SinglePDF>>? {
        return if (pdfPerCategoryResponse != null) {
            pdfPerCategoryResponse
        } else {
            pdfPerCategoryResponse = InjectorUtils.provideListSingleLiveEvent()
            pdfPerCategoryResponse
        }
    }

    fun getAdminPassword() {
        adminPassword = InjectorUtils.provideSingleLiveEvent()
        val db = FirebaseFirestore.getInstance()
        val password = db.collection("admin_password")
        password.document("password_id")
            .get()
            .addOnSuccessListener { snapshot: DocumentSnapshot ->
                if (snapshot.exists()) {
                    adminPassword = if (snapshot["password"] != null) {
                        adminPassword?.value = snapshot["password"].toString()
                        null
                    } else {
                        adminPassword?.value = "password is empty"
                        null
                    }
                }
            }
            .addOnFailureListener { e: Exception? -> adminPassword?.setValue("Unable to authenticate, please try again") }
    }

    fun getPdfsDownloadResponse(): SingleLiveEvent<String>? {
        return if (pdfsDownloadResponse != null) {
            pdfsDownloadResponse
        } else {
            pdfsDownloadResponse = InjectorUtils.provideSingleLiveEvent()
            pdfsDownloadResponse
        }
    }

    @SuppressLint("TimberArgCount")
    private fun savePDFDownloadUrlInDb(pdfUrl: String, category: String, fileName: String) {
        val db = FirebaseFirestore.getInstance()
        val map = InjectorUtils.provideStringHashMap()
        map["pdfUrl"] = pdfUrl
        map["category"] = category
        map["fileName"] = fileName
        val ref = db.collection("PDFs")
        ref.document()
            .set(map)
            .addOnSuccessListener { aVoid: Void? -> Timber.tag(TAG).d("token sent to server!") }
            .addOnFailureListener { e: Exception ->
                Timber.tag(TAG).d("failed to send token to server: %s%s", e.toString())
            }
    }

    fun downloadPDFPerCategory(category: String?) {
        val db = FirebaseFirestore.getInstance()
        val ref = db.collection("PDFs")
        ref.whereEqualTo("category", category)
            .get()
            .addOnSuccessListener { snapshot: QuerySnapshot ->
                if (!snapshot.isEmpty) {
                    pdfsDownloadResponse!!.value = "loaded"
                    pdfPerCategoryResponse!!.setValue(snapshot.toObjects(SinglePDF::class.java))
                } else {
                    pdfsDownloadResponse!!.setValue("empty")
                }
            }
            .addOnFailureListener { e: Exception -> pdfsDownloadResponse!!.setValue(e.toString()) }
    }

    fun downloadPDF(fileName: String?) {
        if (progressUpdate == null) progressUpdate = InjectorUtils.provideIntSingleLiveEvent()
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReference("PDF_Files")
        val islandRef = storageRef.child(fileName!!)
        val rootPath = File(
            Objects.requireNonNull(app)
                ?.applicationContext
                ?.getExternalFilesDir(null)
                ?.absolutePath , "Mtihani"
        )
        if (!rootPath.exists()) {
            rootPath.mkdirs()
        }
        val localFile = File(rootPath, fileName)
        islandRef.getFile(localFile)
            .addOnProgressListener { taskSnapshot: FileDownloadTask.TaskSnapshot ->
                val count =
                    (100 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                progressUpdate!!.setValue(count)
            }
            .addOnSuccessListener {
                //  updateDb(timestamp,localFile.toString(),position);
                singlePDFDownloadResponse!!.setValue("success")
            }.addOnFailureListener { exception: Exception ->
                // Log.e("firebase ", ";local tem file not created  created " + exception.toString());
                singlePDFDownloadResponse!!.value = "An error occurred."
                Timber.d("Error downloading pdf: %s", exception.cause.toString())
            }
    }

    companion object {
        private val LOCK = Any()
        private var sInstance: MtihaniRepository? = null
        private const val TAG = "MtihaniRepository"

        @JvmStatic
        @get:Synchronized
        val instance: MtihaniRepository?
            get() {
                if (sInstance == null) {
                    synchronized(LOCK) { sInstance = MtihaniRepository() }
                }
                return sInstance
            }

        @JvmStatic
        fun loadMessagesFromRoom(myId: String, userId: String): LiveData<PagedList<Chat>> {
            downloadMessages(myId, userId)
            val boundaryCallback =
                MessagesBoundaryCallback(
                    myId,
                    userId
                )
            val pagedListConfig = PagedList.Config.Builder()
                .setPageSize(30)
                .setPrefetchDistance(5)
                .build()
            return LivePagedListBuilder(
                MtihaniDatabase
                    .getInstance(
                        Objects.requireNonNull(app)
                            ?.applicationContext
                    )
                    .singleMessageDao()
                    .getChatMessages(myId, userId), pagedListConfig
            )
                .setBoundaryCallback(boundaryCallback)
                .build()
        }

        @SuppressLint("TimberArgCount")
        private fun downloadMessages(myId: String, userId: String) {
            val db = MtihaniDatabase
                .getInstance(
                    Objects.requireNonNull(app)?.applicationContext
                )
            val firestore = FirebaseFirestore.getInstance()
            val messages = firestore.collection("messages")
            val queryArrayList: MutableList<Task<QuerySnapshot>> = ArrayList()
            queryArrayList.add(
                messages.whereEqualTo("sender", myId)
                    .whereEqualTo("receiver", userId)
                    .whereLessThan("date", System.currentTimeMillis())
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(100)
                    .get()
            )
            queryArrayList.add(
                messages.whereEqualTo("sender", userId)
                    .whereEqualTo("receiver", myId)
                    .whereLessThan("date", System.currentTimeMillis())
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(100)
                    .get()
            )
            val combinedTask = Tasks.whenAllComplete(
                *queryArrayList
                    .toTypedArray<Task<*>>()
            )
            combinedTask.addOnCompleteListener(
                MtihaniAppExecutors.getInstance().diskIO
            ) { tasks: Task<List<Task<*>>?> ->
                if (tasks.result != null) {
                    for (task in tasks.result!!) {
                        if (task.isSuccessful) {
                            val snapshot = (task.result as QuerySnapshot)
                            if (!snapshot.isEmpty) {
                                val chatList: MutableList<Chat> = ArrayList()
                                for (document in snapshot.documents) {
                                    val chat = document.toObject(Chat::class.java)
                                    if (chat != null) {
                                        chat.documentId = document.id
                                        chatList.add(chat)
                                    }
                                }
                                Timber.tag(TAG).d("message chats received are: %s%s", chatList.size)
                                db.singleMessageDao()
                                    .saveChatsList(chatList)
                            } else {
                                Log.d(TAG, "messages snapshot is empty")
                            }
                        } else {
                            if (task.exception != null) Log.d(TAG, task.exception.toString())
                        }
                    }
                }
            }
        }

        @JvmStatic
        fun loadUsersFromRoom(): LiveData<PagedList<User>> {
            val boundaryCallback =
                UsersBoundaryCallback()
            val pagedListConfig = PagedList.Config.Builder()
                .setPrefetchDistance(5)
                .setEnablePlaceholders(true)
                .setPageSize(50)
                .build()
            return LivePagedListBuilder(
                MtihaniDatabase
                    .getInstance(
                        Objects.requireNonNull(app)
                            ?.applicationContext
                    )
                    .usersDao()
                    .getAllUsers(Constants.getUserId()), pagedListConfig
            )
                .setBoundaryCallback(boundaryCallback)
                .build()
        }

        @JvmStatic
        fun searchUsersFromRoom(filter: String?): LiveData<PagedList<User>> {
            val boundaryCallback =
                UsersBoundaryCallback()
            val pagedListConfig = PagedList.Config.Builder()
                .setPrefetchDistance(5)
                .setEnablePlaceholders(true)
                .setPageSize(50)
                .build()
            return LivePagedListBuilder(
                MtihaniDatabase
                    .getInstance(
                        Objects.requireNonNull(app)?.applicationContext
                    )
                    .usersDao()
                    .searchUsers(filter), pagedListConfig
            )
                .setBoundaryCallback(boundaryCallback)
                .build()
        }

        @JvmStatic
        fun saveMessage(chat: Chat?) {
            MtihaniAppExecutors
                .getInstance()
                .diskIO
                .execute {
                    MtihaniDatabase
                        .getInstance(
                            Objects.requireNonNull(app)?.applicationContext
                        )
                        .singleMessageDao()
                        .add(chat)
                }
        }

        @JvmStatic
        fun saveNewMessages(chats: List<Chat?>?) {
            MtihaniAppExecutors
                .getInstance()
                .diskIO
                .execute {
                    MtihaniDatabase
                        .getInstance(
                            Objects.requireNonNull(app)?.applicationContext
                        )
                        .singleMessageDao()
                        .saveChatsList(chats)
                }
        }

        @JvmStatic
        fun sendMessageToFirebase(chat: Chat?) {
            FirebaseFirestore.getInstance()
                .collection("messages")
                .document()
                .set(chat!!)
        }

        @JvmStatic
        fun onZeroUsersLoaded() {
            val db = MtihaniDatabase
                .getInstance(
                    Objects.requireNonNull(app)?.applicationContext
                )
            FirebaseFirestore
                .getInstance()
                .collection("Users")
                .whereLessThan("date", System.currentTimeMillis())
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnCompleteListener(
                    MtihaniAppExecutors
                        .getInstance()
                        .diskIO
                ) { task: Task<QuerySnapshot?> ->
                    if (task.isSuccessful) {
                        if (task.result != null) {
                            if (!task.result!!.isEmpty) {
                                db
                                    .usersDao()
                                    .saveUsersList(
                                        task.result!!
                                            .toObjects(User::class.java)
                                    )
                            } else {
                                Timber.tag(TAG).d("onZeroUsersLoaded list is empty")
                            }
                        }
                    } else {
                        if (task.exception != null) {
                            Timber.tag(TAG)
                                .d("error onZeroUsersLoaded%s", task.exception.toString())
                        }
                    }
                }
        }

        @JvmStatic
        fun onUserAtFrontLoaded(itemAtFront: User) {
            val db = MtihaniDatabase
                .getInstance(
                    Objects.requireNonNull(app)
                        ?.applicationContext
                )
            FirebaseFirestore
                .getInstance()
                .collection("Users")
                .whereGreaterThan("date", itemAtFront.date)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnCompleteListener(
                    MtihaniAppExecutors
                        .getInstance()
                        .diskIO
                ) { task: Task<QuerySnapshot?> ->
                    if (task.isSuccessful) {
                        if (task.result != null) {
                            if (!task.result!!.isEmpty) {
                                db
                                    .usersDao()
                                    .saveUsersList(
                                        task.result!!
                                            .toObjects(User::class.java)
                                    )
                            } else {
                                Timber.tag(TAG).d("onUserAtFrontLoaded list is empty")
                            }
                        }
                    } else {
                        if (task.exception != null) {
                            Timber.tag(TAG)
                                .d("error onUserAtFrontLoaded%s", task.exception.toString())
                        }
                    }
                }
        }

        @JvmStatic
        fun onUserAtEndLoaded(itemAtEnd: User) {
            val db = MtihaniDatabase
                .getInstance(
                    Objects.requireNonNull(app)?.applicationContext
                )
            FirebaseFirestore
                .getInstance()
                .collection("Users")
                .whereLessThan("date", itemAtEnd.date)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnCompleteListener(
                    MtihaniAppExecutors
                        .getInstance()
                        .diskIO
                ) { task: Task<QuerySnapshot?> ->
                    if (task.isSuccessful) {
                        if (task.result != null) {
                            if (!task.result!!.isEmpty) {
                                db
                                    .usersDao()
                                    .saveUsersList(
                                        task.result!!
                                            .toObjects(User::class.java)
                                    )
                            } else {
                                Timber.tag(TAG).d("onUserAtEndLoaded list is empty")
                            }
                        }
                    } else {
                        if (task.exception != null) {
                            Timber.tag(TAG).d(
                                "error onUserAtEndLoaded%s",
                                task.exception.toString()
                            )
                        }
                    }
                }
        }

        @JvmStatic
        fun loadChatList(): LiveData<PagedList<ChatItem>> {
            val pagedListConfig = PagedList.Config.Builder()
                .setPrefetchDistance(5)
                .setEnablePlaceholders(true)
                .setPageSize(50)
                .build()
            return LivePagedListBuilder(
                MtihaniDatabase
                    .getInstance(
                        Objects.requireNonNull(app)?.applicationContext
                    )
                    .chatDao()
                    .allChatUsers, pagedListConfig
            )
                .build()
        }

        @JvmStatic
        fun saveChatListUser(user: ChatItem?) {
            MtihaniAppExecutors
                .getInstance()
                .diskIO
                .execute {
                    MtihaniDatabase
                        .getInstance(
                            Objects.requireNonNull(app)?.applicationContext
                        )
                        .chatDao()
                        .addUser(user)
                }
        }

        @JvmStatic
        fun updateMessage(chat: Chat) {
            MtihaniAppExecutors
                .getInstance()
                .diskIO
                .execute {
                    MtihaniDatabase
                        .getInstance(
                            Objects.requireNonNull(app)?.applicationContext
                        )
                        .singleMessageDao()
                        .update(chat)
                }
            if (Constants.getUserId() != chat.sender) {
                val hashMap = HashMap<String, Any>()
                hashMap["seen"] = true
                hashMap["documentId"] = chat.documentId
                FirebaseFirestore
                    .getInstance()
                    .collection("messages")
                    .document(chat.documentId)
                    .update(hashMap)
            }
        }

        @JvmStatic
        fun downloadUsers() {
            val db = MtihaniDatabase.getInstance(Objects.requireNonNull(app)?.applicationContext)
            FirebaseFirestore
                .getInstance()
                .collection("Users")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnCompleteListener(
                    MtihaniAppExecutors
                        .getInstance()
                        .diskIO
                ) { task: Task<QuerySnapshot?> ->
                    if (task.isSuccessful) {
                        if (task.result != null) {
                            if (!task.result!!.isEmpty) {
                                db
                                    .usersDao()
                                    .saveUsersList(
                                        task.result!!.toObjects(
                                            User::class.java
                                        )
                                    )
                            } else {
                                Timber.tag(TAG).d("downloadUsers list is empty")
                            }
                        }
                    } else {
                        if (task.exception != null) {
                            Timber.tag(TAG).d("error downloadUsers%s", task.exception.toString())
                        }
                    }
                }
        }

        /**
         * This method gets fired when the database has zero messages
         * when it is the case then the first 200 messages will be downloaded from the server
         *
         * @param myId
         * @param userId
         */
        @JvmStatic
        @SuppressLint("TimberArgCount")
        fun onZeroItemsLoaded(myId: String?, userId: String?) {
            val db = MtihaniDatabase.getInstance(
                Objects.requireNonNull(app)?.applicationContext
            )
            val firestore = FirebaseFirestore.getInstance()
            val messages = firestore.collection("messages")
            val queryArrayList: MutableList<Task<QuerySnapshot>> = ArrayList()
            queryArrayList.add(
                messages.whereEqualTo("sender", myId)
                    .whereEqualTo("receiver", userId)
                    .whereLessThan("date", System.currentTimeMillis())
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(50)
                    .get()
            )
            queryArrayList.add(
                messages.whereEqualTo("sender", userId)
                    .whereEqualTo("receiver", myId)
                    .whereLessThan("date", System.currentTimeMillis())
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(50)
                    .get()
            )
            val combinedTask = Tasks.whenAllComplete(
                *queryArrayList
                    .toTypedArray<Task<*>>()
            )
            combinedTask.addOnCompleteListener(
                MtihaniAppExecutors.getInstance().diskIO
            ) { tasks: Task<List<Task<*>>?> ->
                if (tasks.result != null) {
                    for (task in tasks.result!!) {
                        if (task.isSuccessful) {
                            val snapshot = (task.result as QuerySnapshot)
                            if (!snapshot.isEmpty) {
                                val chatList: MutableList<Chat> = ArrayList()
                                for (document in snapshot.documents) {
                                    val chat = document.toObject(Chat::class.java)
                                    if (chat != null) {
                                        chat.documentId = document.id
                                        chatList.add(chat)
                                    }
                                }
                                Timber.tag(TAG)
                                    .d("onZeroItemLoaded chats received are: %s%s", chatList.size)
                                db.singleMessageDao()
                                    .saveChatsList(chatList)
                            } else {
                                Timber.tag(TAG).d("onZeroItemLoaded snapshot is empty")
                            }
                        } else {
                            if (task.exception != null) Timber.tag(TAG).d(task.exception.toString())
                        }
                    }
                }
            }
        }

        /**
         * This method will be fired after the first message has been loaded from the database
         * This method will seek to download any newer messages from the servers
         *
         * @param itemAtFront
         */
        @JvmStatic
        @SuppressLint("TimberArgCount")
        fun onItemAtFrontLoaded(itemAtFront: Chat, myId: String?, userId: String?) {
            val db = MtihaniDatabase.getInstance(
                Objects.requireNonNull(app)?.applicationContext
            )
            val firestore = FirebaseFirestore.getInstance()
            val messages = firestore.collection("messages")
            val queryArrayList: MutableList<Task<QuerySnapshot>> = ArrayList()
            queryArrayList.add(
                messages.whereEqualTo("sender", myId)
                    .whereEqualTo("receiver", userId)
                    .whereLessThan("date", itemAtFront.date)
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(50)
                    .get()
            )
            queryArrayList.add(
                messages.whereEqualTo("sender", userId)
                    .whereEqualTo("receiver", myId)
                    .whereLessThan("date", itemAtFront.date)
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(50)
                    .get()
            )
            val combinedTask = Tasks.whenAllComplete(
                *queryArrayList
                    .toTypedArray<Task<*>>()
            )
            combinedTask.addOnCompleteListener(
                MtihaniAppExecutors.getInstance().diskIO
            ) { tasks: Task<List<Task<*>>?> ->
                if (tasks.result != null) {
                    for (task in tasks.result!!) {
                        if (task.isSuccessful) {
                            val snapshot = (task.result as QuerySnapshot)
                            if (!snapshot.isEmpty) {
                                val chatList: MutableList<Chat> = ArrayList()
                                for (document in snapshot.documents) {
                                    val chat = document.toObject(Chat::class.java)
                                    if (chat != null) {
                                        chat.documentId = document.id
                                        chatList.add(chat)
                                    }
                                }
                                Timber.tag(TAG).d(
                                    "onItemAtFrontLoaded chats received are: " +
                                            "%s" + chatList.size
                                )
                                db.singleMessageDao()
                                    .saveChatsList(chatList)
                            } else {
                                Timber.tag(TAG).d("onItemAtFrontLoaded snapshot is empty")
                            }
                        } else {
                            if (task.exception != null) Timber.tag(TAG).d(task.exception.toString())
                        }
                    }
                }
            }
        }

        /**
         * This method will be fired when the database loads the last remaining item
         * This is a clear indication that the local database has run out of items
         * hence we fetch more from firebase
         *
         * @param itemAtEnd
         */
        @JvmStatic
        @SuppressLint("TimberArgCount")
        fun onItemAtEndLoaded(itemAtEnd: Chat, myId: String?, userId: String?) {
            val db = MtihaniDatabase.getInstance(
                Objects.requireNonNull(app)?.applicationContext
            )
            val firestore = FirebaseFirestore.getInstance()
            val messages = firestore.collection("messages")
            val queryArrayList: MutableList<Task<QuerySnapshot>> = ArrayList()
            queryArrayList.add(
                messages.whereEqualTo("sender", myId)
                    .whereEqualTo("receiver", userId)
                    .whereGreaterThan("date", itemAtEnd.date)
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(50)
                    .get()
            )
            queryArrayList.add(
                messages.whereEqualTo("sender", userId)
                    .whereEqualTo("receiver", myId)
                    .whereLessThan("date", itemAtEnd.date)
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(50)
                    .get()
            )
            val combinedTask = Tasks.whenAllComplete(
                *queryArrayList
                    .toTypedArray<Task<*>>()
            )
            combinedTask.addOnCompleteListener(
                MtihaniAppExecutors.getInstance().diskIO
            ) { tasks: Task<List<Task<*>>?> ->
                if (tasks.result != null) {
                    for (task in tasks.result!!) {
                        if (task.isSuccessful) {
                            val snapshot = (task.result as QuerySnapshot)
                            if (!snapshot.isEmpty) {
                                val chatList: MutableList<Chat> = ArrayList()
                                for (document in snapshot.documents) {
                                    val chat = document.toObject(Chat::class.java)
                                    if (chat != null) {
                                        chat.documentId = document.id
                                        chatList.add(chat)
                                    }
                                }
                                Timber.d("onItemAtEndLoaded chats received are: ", +chatList.size)
                                db.singleMessageDao()
                                    .saveChatsList(chatList)
                            } else {
                                Timber.d("onItemAtEndLoaded snapshot is empty")
                            }
                        } else {
                            if (task.exception != null) Timber.d(task.exception.toString())
                        }
                    }
                }
            }
        }
    }
}