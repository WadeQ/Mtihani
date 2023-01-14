package com.wadektech.mtihani.chat.data.repository

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.wadektech.mtihani.app.MtihaniRevise
import com.wadektech.mtihani.chat.data.firebaseDataSource.MessagesBoundaryCallback
import com.wadektech.mtihani.chat.data.firebaseDataSource.UsersBoundaryCallback
import com.wadektech.mtihani.chat.data.localDatasource.MtihaniDatabase
import com.wadektech.mtihani.chat.data.localDatasource.room.Chat
import com.wadektech.mtihani.chat.data.localDatasource.room.ChatItem
import com.wadektech.mtihani.chat.data.localDatasource.room.User
import com.wadektech.mtihani.chat.domain.repository.IChatsRepository
import com.wadektech.mtihani.core.Constants
import com.wadektech.mtihani.core.MtihaniAppExecutors
import timber.log.Timber
import java.util.*


class ChatsRepositoryImpl: IChatsRepository {

    override fun loadMessagesFromRoom(myId: String, userId: String): LiveData<PagedList<Chat>> {
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
                    Objects.requireNonNull(MtihaniRevise.app)
                        ?.applicationContext
                )
                .singleMessageDao()
                .getChatMessages(myId, userId), pagedListConfig
        )
            .setBoundaryCallback(boundaryCallback)
            .build()
    }

    @SuppressLint("TimberArgCount")
    override fun downloadMessages(myId: String, userId: String) {
        val db = MtihaniDatabase
            .getInstance(
                Objects.requireNonNull(MtihaniRevise.app)?.applicationContext
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
                            Timber.tag("ChatsRepositoryImpl").d("message chats received are: %s%s", chatList.size)
                            db.singleMessageDao()
                                .saveChatsList(chatList)
                        } else {
                            Timber.tag("ChatsRepositoryImpl").d("messages snapshot is empty")
                        }
                    } else {
                        if (task.exception != null) Timber.tag("ChatsRepositoryImpl")
                            .d(task.exception.toString())
                    }
                }
            }
        }
    }

    override fun loadUsersFromRoom(): LiveData<PagedList<User>> {
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
                    Objects.requireNonNull(MtihaniRevise.app)
                        ?.applicationContext
                )
                .usersDao()
                .getAllUsers(Constants.getUserId()), pagedListConfig
        )
            .setBoundaryCallback(boundaryCallback)
            .build()
    }

    override fun searchUsersFromRoom(filter: String?): LiveData<PagedList<User>> {
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
                    Objects.requireNonNull(MtihaniRevise.app)?.applicationContext
                )
                .usersDao()
                .searchUsers(filter), pagedListConfig
        )
            .setBoundaryCallback(boundaryCallback)
            .build()
    }

    override fun saveMessage(chat: Chat?) {
        MtihaniAppExecutors
            .getInstance()
            .diskIO
            .execute {
                MtihaniDatabase
                    .getInstance(
                        Objects.requireNonNull(MtihaniRevise.app)?.applicationContext
                    )
                    .singleMessageDao()
                    .add(chat)
            }
    }

    override fun saveNewMessages(chats: List<Chat?>?) {
        MtihaniAppExecutors
            .getInstance()
            .diskIO
            .execute {
                MtihaniDatabase
                    .getInstance(
                        Objects.requireNonNull(MtihaniRevise.app)?.applicationContext
                    )
                    .singleMessageDao()
                    .saveChatsList(chats)
            }
    }

    override fun sendMessageToFirebase(chat: Chat?) {
        FirebaseFirestore.getInstance()
            .collection("messages")
            .document()
            .set(chat!!)
    }

    override fun onZeroUsersLoaded() {
        val db = MtihaniDatabase
            .getInstance(
                Objects.requireNonNull(MtihaniRevise.app)?.applicationContext
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
                            Timber.tag("ChatsRepositoryImpl").d("onZeroUsersLoaded list is empty")
                        }
                    }
                } else {
                    if (task.exception != null) {
                        Timber.tag("ChatsRepositoryImpl")
                            .d("error onZeroUsersLoaded%s", task.exception.toString())
                    }
                }
            }
    }

    override fun onUserAtFrontLoaded(itemAtFront: User) {
        val db = MtihaniDatabase
            .getInstance(
                Objects.requireNonNull(MtihaniRevise.app)
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
                            Timber.tag("ChatsRepositoryImpl").d("onUserAtFrontLoaded list is empty")
                        }
                    }
                } else {
                    if (task.exception != null) {
                        Timber.tag("ChatsRepositoryImpl")
                            .d("error onUserAtFrontLoaded%s", task.exception.toString())
                    }
                }
            }
    }

    override fun onUserAtEndLoaded(itemAtEnd: User) {
        val db = MtihaniDatabase
            .getInstance(
                Objects.requireNonNull(MtihaniRevise.app)?.applicationContext
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
                            Timber.tag("ChatsRepositoryImpl").d("onUserAtEndLoaded list is empty")
                        }
                    }
                } else {
                    if (task.exception != null) {
                        Timber.tag("ChatsRepositoryImpl").d(
                            "error onUserAtEndLoaded%s",
                            task.exception.toString()
                        )
                    }
                }
            }
    }

    override fun loadChatList(): LiveData<PagedList<ChatItem>> {
        val pagedListConfig = PagedList.Config.Builder()
            .setPrefetchDistance(5)
            .setEnablePlaceholders(true)
            .setPageSize(50)
            .build()
        return LivePagedListBuilder(
            MtihaniDatabase
                .getInstance(
                    Objects.requireNonNull(MtihaniRevise.app)?.applicationContext
                )
                .chatDao()
                .allChatUsers, pagedListConfig
        )
            .build()
    }

    override fun saveChatListUser(user: ChatItem?) {
        MtihaniAppExecutors
            .getInstance()
            .diskIO
            .execute {
                MtihaniDatabase
                    .getInstance(
                        Objects.requireNonNull(MtihaniRevise.app)?.applicationContext
                    )
                    .chatDao()
                    .addUser(user)
            }
    }

    override fun updateMessage(chat: Chat) {
        MtihaniAppExecutors
            .getInstance()
            .diskIO
            .execute {
                MtihaniDatabase
                    .getInstance(
                        Objects.requireNonNull(MtihaniRevise.app)?.applicationContext
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

    override fun downloadUsers() {
        val db = MtihaniDatabase.getInstance(Objects.requireNonNull(MtihaniRevise.app)?.applicationContext)
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
                            Timber.tag("ChatsRepositoryImpl").d("downloadUsers list is empty")
                        }
                    }
                } else {
                    if (task.exception != null) {
                        Timber.tag("ChatsRepositoryImpl").d("error downloadUsers%s", task.exception.toString())
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
    @SuppressLint("TimberArgCount")
    override fun onZeroItemsLoaded(myId: String?, userId: String?) {
        val db = MtihaniDatabase.getInstance(
            Objects.requireNonNull(MtihaniRevise.app)?.applicationContext
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
                            Timber.tag("ChatsRepositoryImpl")
                                .d("onZeroItemLoaded chats received are: %s%s", chatList.size)
                            db.singleMessageDao()
                                .saveChatsList(chatList)
                        } else {
                            Timber.tag("ChatsRepositoryImpl").d("onZeroItemLoaded snapshot is empty")
                        }
                    } else {
                        if (task.exception != null) Timber.tag("ChatsRepositoryImpl").d(task.exception.toString())
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
    @SuppressLint("TimberArgCount", "BinaryOperationInTimber")
    override fun onItemAtFrontLoaded(itemAtFront: Chat, myId: String?, userId: String?) {
        val db = MtihaniDatabase.getInstance(
            Objects.requireNonNull(MtihaniRevise.app)?.applicationContext
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
                            Timber.tag("ChatsRepository").d(
                                "onItemAtFrontLoaded chats received are: " +
                                        "%s" + chatList.size
                            )
                            db.singleMessageDao()
                                .saveChatsList(chatList)
                        } else {
                            Timber.tag("ChatsRepositoryImpl").d("onItemAtFrontLoaded snapshot is empty")
                        }
                    } else {
                        if (task.exception != null) Timber.tag("ChatsRepositoryImpl").d(task.exception.toString())
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
    @SuppressLint("TimberArgCount")
    override fun onItemAtEndLoaded(itemAtEnd: Chat, myId: String?, userId: String?) {
        val db = MtihaniDatabase.getInstance(
            Objects.requireNonNull(MtihaniRevise.app)?.applicationContext
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