package com.wadektech.mtihani.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.wadektech.mtihani.BuildConfig
import com.wadektech.mtihani.R
import com.wadektech.mtihani.adapter.MessageAdapter
import com.wadektech.mtihani.database.MtihaniDatabase
import com.wadektech.mtihani.fragments.APIService
import com.wadektech.mtihani.notification.*
import com.wadektech.mtihani.pojo.Status
import com.wadektech.mtihani.room.*
import com.wadektech.mtihani.utils.Constants
import com.wadektech.mtihani.utils.InjectorUtils
import com.wadektech.mtihani.utils.StorageUtil
import com.wadektech.mtihani.viewmodels.MessagesActivityViewModel
import com.wadektech.mtihani.viewmodels.UsersViewModel
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Response
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MessageActivity : AppCompatActivity() {
    private var imageView: CircleImageView? = null
    private var userName: TextView? = null
    private var mTime: TextView? = null

    //FirebaseUser firebaseUser;
    var reference: DatabaseReference? = null
    var editSend: EditText? = null
    var btnSend: ImageButton? = null
    var mSendImageMessage: ImageButton? = null
    var mAdapter: MessageAdapter? = null

    // List<Chat> chats;
    var mRecycler: RecyclerView? = null
    private var mChatItem: ChatItem? = null
    private var shouldAddChatUser = true
    var mtihaniDatabase: MtihaniDatabase? = null
    private var chatViewModel: ChatViewModel? = null
    private var smoothScroller: SmoothScroller? = null
    private var myid: String? = null
    private var status: String? = null
    private var date: Long? = null
    private var formattedDate: String? = null
    private var userid: String? = null
    private var userNameString: String? = null
    private var time: String? = null
    var notify = false
    var apiService: APIService? = null
    private var imageURL: String? = null
    private var mViewModel: MessagesActivityViewModel? = null
    private var imageUri: Uri? = null



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        title = null
        val topToolBar = findViewById<Toolbar>(R.id.main_app_bar)
        setSupportActionBar(topToolBar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
        topToolBar.setNavigationOnClickListener { v: View? ->
            startActivity(Intent(this@MessageActivity,
                    ChatActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }

        subscribeToLiveUpdates()

        apiService = Client.getClient("https://fcm.googleapis.com/")
                .create(APIService::class.java)
        reference = FirebaseDatabase.getInstance().getReference("Chats")
        reference!!.keepSynced(true)
        imageView = findViewById(R.id.chat_user_profile)
        userName = findViewById(R.id.username)
        editSend = findViewById(R.id.et_send_message)
        btnSend = findViewById(R.id.btn_send_message)
        mRecycler = findViewById(R.id.rv_message)
        mSendImageMessage = findViewById(R.id.btn_send_image)
        mTime = findViewById(R.id.tv_time)

        intent = intent
        mChatItem = intent.getParcelableExtra("mChatItem")
        mChatItem?.let {
            userid = it.userId
            imageURL = it.imageURL
            userNameString = it.username
            time = it.time
            status = it.status
            date = it.date
        }

        formattedDate = DateFormat.getDateInstance().format(date)

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("mChatItem")) {
                mChatItem = savedInstanceState.getParcelable("mChatItem")
            }
        }

        myid = Constants.getUserId()
        mSendImageMessage?.setOnClickListener{
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
            startActivityForResult(Intent.createChooser(intent,
                    "Choose Image"), IMAGE_REQUEST)
        }

        //setting up recyclerview
        mRecycler?.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false)
        linearLayoutManager.stackFromEnd = true
        mRecycler?.layoutManager = linearLayoutManager
        //creating the adapter
        mAdapter = MessageAdapter(this@MessageActivity, imageURL)
        //getting viewmodel
        mRecycler?.adapter = mAdapter
        chatViewModel = ViewModelProviders.of(this)
                .get(ChatViewModel::class.java)
        //observing the pagelist from viewmodel
        chatViewModel!!.chats.observe(this, Observer { pagedList:
                                                       PagedList<Chat?> -> mAdapter!!
                                                    .submitList(pagedList) })
        btnSend?.setOnClickListener {
            notify = true
            val message = editSend?.text.toString().trim {
                it <= ' '
            }

            if (message != "") {
                sendMessage(Constants.getUserId(), userid, message)
            } else {
                Toast.makeText(this, "Blank message!",
                        Toast.LENGTH_SHORT).show()
            }
            editSend?.setText("")
        }

        newIncomingMessageListener(Constants.getUserId(), userid) //listen for new messages
        val factory = InjectorUtils
                .provideMessagesViewModelFactory(myid, userid)
        mViewModel = ViewModelProviders.of(this, factory)
                .get(MessagesActivityViewModel::class.java)
        smoothScroller = object : LinearSmoothScroller(this) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_END
            }
        }

        mAdapter!!.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                smoothScroller?.targetPosition = positionStart
                Objects.requireNonNull(mRecycler?.layoutManager)!!.
                startSmoothScroll(smoothScroller)
            }
        })

        mViewModel!!.messagesList.observe(this, Observer { pagedList:
                                                           PagedList<Chat?> -> mAdapter!!.
                                                            submitList(pagedList) })
        userName?.text = userNameString

        if (imageURL == "default") {
            imageView?.setImageResource(R.drawable.profile)
        } else {
            val defaultImageResId = R.drawable.profile
            Picasso.with(applicationContext)
                    .load(imageURL)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(imageView, object : Callback {
                        override fun onSuccess() {}
                        override fun onError() {
                            Picasso.with(this@MessageActivity)
                                    .load(imageURL)
                                    .error(defaultImageResId)
                                    .into(imageView)
                        }
                    })
        }
    }

    private fun onMessagesReceived(chats: PagedList<Chat>?) {
        if (chats != null) {
            monitor++
            smoothScroller = object : LinearSmoothScroller(this) {
                override fun getVerticalSnapPreference(): Int {
                    return SNAP_TO_END
                }
            }
            mAdapter!!.registerAdapterDataObserver(object : AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    // mLinearLayout.scrollToPosition(positionStart);
                    smoothScroller?.targetPosition = positionStart
                    Objects.requireNonNull(mRecycler!!.layoutManager)!!.
                    startSmoothScroll(smoothScroller)
                }
            })
            mAdapter!!.submitList(chats)
            mRecycler!!.adapter = mAdapter
        }
    }

    private fun sendMessage(sender: String, receiver: String?, message: String) {
        val chat = Chat(sender, receiver, message, false,
                System.currentTimeMillis(), "")
        if (mViewModel != null) {
            if (shouldAddChatUser) {
                UsersViewModel.saveChatListUser(mChatItem)
                shouldAddChatUser = !shouldAddChatUser
            }
            mViewModel!!.saveMessage(chat)
            mViewModel!!.sendMessageToFirebase(chat)
        }
        sendNotification(receiver, userNameString, message)
    }

    private fun sendNotification(receiver: String?, username: String?, message: String) {
        val tokens = FirebaseDatabase
                .getInstance()
                .getReference("Tokens")
        tokens.keepSynced(true)
        val query = tokens
                .orderByKey()
                .equalTo(receiver)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val token = snapshot.getValue(Token::class.java)
                    val data = Data(myid, R.drawable.livechat,
                            "$username:$message", "New Message", userid)
                    if (BuildConfig.DEBUG && token == null) {
                        error("Assertion failed")
                    }

                    val sender = Sender(data, token!!.token)
                    apiService!!.sendNotification(sender)
                            .enqueue(object : retrofit2.Callback<MyResponse?> {
                                override fun onResponse(call: Call<MyResponse?>,
                                                        response: Response<MyResponse?>) {
                                    if (response.code() == 200) {
                                        if (BuildConfig.DEBUG && response.body() == null) {
                                            error("Assertion failed")
                                        }
                                        if (response.body()!!.success != 1) {
                                            Toast.makeText(applicationContext, "Failed!",
                                                    Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }

                                override fun onFailure(call: Call<MyResponse?>, t: Throwable) {}
                            })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun currentUser(userid: String?) {
        val editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit()
        editor.putString("currentuser", userid)
        editor.apply()
    }

    override fun onStart() {
        super.onStart()
        Timber.d("ONSTART")
        updateUserStatus("online")
        subscribeToLiveUpdates()
    }

    override fun onStop() {
        super.onStop()
        updateUserStatus("offline")
        subscribeToLiveUpdates()
    }

    override fun onResume() {
        super.onResume()
        //        updateStatus("online");
        currentUser(userid)
        updateUserStatus("online")
        subscribeToLiveUpdates()
        newIncomingMessageListener(Constants.getUserId(), userid)
    }

    override fun onRestart() {
        super.onRestart()
        updateUserStatus("online")
        subscribeToLiveUpdates()
        newIncomingMessageListener(Constants.getUserId(), userid)
    }

    override fun onPause() {
        super.onPause()
        currentUser("none")
        updateUserStatus("offline")
        subscribeToLiveUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        updateUserStatus("offline")
        subscribeToLiveUpdates()
    }

    @SuppressLint("SimpleDateFormat")
    private fun updateUserStatus(status: String) {
        val saveCurrentTime: String
        val calendar = Calendar.getInstance()
        val currentTime = SimpleDateFormat("hh:mm a")
        saveCurrentTime = currentTime.format(calendar.time)
        val saveCurrentDate : Long = System.currentTimeMillis()
        val hashMap = HashMap<String, Any>()
        hashMap["time"] = saveCurrentTime
        hashMap["date"] = saveCurrentDate
        hashMap["status"] = status
        FirebaseFirestore
                .getInstance()
                .collection("Users")
                .document(myid!!)
                .update(hashMap)
    }

    @SuppressLint("SetTextI18n")
    private fun subscribeToLiveUpdates(){
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("Users")
                .whereEqualTo("userId", userid)
                .addSnapshotListener { value: QuerySnapshot?,
                                       error: FirebaseFirestoreException? ->
                    if (error != null) {
                        Timber.e("listen:error%s", error.message)
                        return@addSnapshotListener
                    }
                    for (dc in value!!.documentChanges) {
                        val userStatus = dc.document.toObject(User::class.java).status
                        when (userStatus) {
                            "online" -> {
                                mTime?.setTextColor(ContextCompat
                                        .getColor(this@MessageActivity,
                                                R.color.colorAccent))
                                mTime?.text = getString(R.string.active_now)
                            }

                            "offline" -> {
                                mTime?.setTextColor(ContextCompat
                                        .getColor(this@MessageActivity,
                                                R.color.colorAccent))
                                mTime?.text = "Active $formattedDate, $time"
                            }
                            else -> {
                                mTime?.text = "offline"
                            }
                        }
                    }
                }
    }

    private fun listenToFirebaseRealtimeStatus(){
        val rootRef : DatabaseReference = FirebaseDatabase
                .getInstance()
                .reference
        val userRef = rootRef
                .child("Users")
                .child(userid!!)
                .child("status")
        userRef.addValueEventListener(object : ValueEventListener{
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(Status::class.java)
                    if (user != null) {
                        when(user.status){
                            "online" -> {
                                mTime?.setTextColor(ContextCompat
                                        .getColor(this@MessageActivity,
                                        R.color.colorAccent))
                                mTime?.text = getString(R.string.active_now)
                            }
                            "offline" -> {
                                mTime?.setTextColor(ContextCompat
                                        .getColor(this@MessageActivity,
                                        R.color.colorAccent))
                                mTime?.text = "Active "+user.date+", "+user.time
                            }
                            else -> {
                                mTime?.text = "offline"
                            }
                        }
                    }
            }

            @SuppressLint("BinaryOperationInTimber")
            override fun onCancelled(error: DatabaseError) {
                Timber.e("Error listening to realtime Status "+error.message)
            }
        })
    }

    /**
     * This is the listener that listens for new messages.
     * By the way the new messages we are interested in are only the messages
     * coming from the sender. certainly we don't want to listen for our own messages
     *
     * @param myId
     * @param userId
     */
    private fun newIncomingMessageListener(myId: String, userId: String?) {
        val db = FirebaseFirestore.getInstance()
        val messagesRef = db.collection("messages")
        val query = messagesRef
                .whereEqualTo("sender", userId)
                .whereEqualTo("receiver", myId)
                .whereGreaterThan("date",
                        System.currentTimeMillis() - 30 * 60 * 1000)
                .orderBy("date", Query.Direction.DESCENDING)
        query.addSnapshotListener(this) { snapshots: QuerySnapshot?,
                                          e: FirebaseFirestoreException? ->
            if (e != null) {
                Timber.d("error while listening %s", e.toString())
                return@addSnapshotListener
            }
            if (snapshots != null && !snapshots.isEmpty) {
                if (mViewModel != null) {
                    val chatList: MutableList<Chat> = ArrayList()
                    for (document in snapshots.documents) {
                        val chat = document.toObject(Chat::class.java)
                        if (chat != null) {
                            chat.documentId = document.id
                            chatList.add(chat)
                        }
                    }
                    mViewModel!!.saveNewMessages(chatList)
                } else {
                    // Toast.makeText(this, "viewmodel is null", Toast.LENGTH_SHORT).show();
                }
            } else {
                //Toast.makeText(this, "snap is null or empty", Toast.LENGTH_SHORT).show();
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mChatItem != null) outState.putParcelable("mChatItem", mChatItem)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data
                != null && data.data != null) {
            imageUri = data.data
            val selectedImagePath = imageUri
            val selectedImageBmp = MediaStore.Images.Media.getBitmap(contentResolver,
                    selectedImagePath)
            val outputStream = ByteArrayOutputStream()
            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val selectedImageBytes = outputStream.toByteArray()
            StorageUtil.uploadMessageImage(selectedImageBytes) { imagePath ->

            }
        }
    }

    companion object {
        private const val IMAGE_REQUEST = 2366
        // private String imageurl;
        private var monitor = 0
    }
}