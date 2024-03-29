package com.wadektech.mtihani.chat.presentation.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.StorageTask
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.wadektech.mtihani.R
import com.wadektech.mtihani.chat.presentation.adapters.MessageAdapter
import com.wadektech.mtihani.chat.data.localDatasource.MtihaniDatabase
import com.wadektech.mtihani.chat.data.localDatasource.room.Chat
import com.wadektech.mtihani.chat.data.localDatasource.room.ChatDao
import com.wadektech.mtihani.chat.data.localDatasource.room.ChatItem
import com.wadektech.mtihani.chat.presentation.viewmodels.ChatViewModel
import com.wadektech.mtihani.notification.*
import com.wadektech.mtihani.pdf.domain.pojo.Status
import com.wadektech.mtihani.core.Constants
import com.wadektech.mtihani.core.InjectorUtils
import com.wadektech.mtihani.core.StorageUtil
import com.wadektech.mtihani.notification.data.APIService
import com.wadektech.mtihani.notification.data.Client
import com.wadektech.mtihani.notification.domain.MyResponse
import com.wadektech.mtihani.notification.domain.models.Data
import com.wadektech.mtihani.notification.domain.models.Sender
import com.wadektech.mtihani.notification.domain.models.Token
import com.wadektech.mtihani.chat.presentation.viewmodels.MessagesActivityViewModel
import com.wadektech.mtihani.chat.presentation.viewmodels.UsersViewModel
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Response
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MessageFragment : BottomSheetDialogFragment() {
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
    private var smoothScroller: RecyclerView.SmoothScroller? = null
    private var myid: String? = null
    private var status: String? = null
    private val date: String? = null
    private var userid: String? = null
    private var userNameString: String? = null
    private var time: String? = null
    var notify = false
    var apiService: APIService? = null
    private val chatDao: ChatDao? = null
    private var imageURL: String? = null
    private val seenListener: ValueEventListener? = null
    private var mViewModel: MessagesActivityViewModel? = null
    private val checker = ""
    private var imageUri: Uri? = null
    private val myImageUrl = ""
    private val imageUploadTask: StorageTask<*>? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_message, container, false)
        val topToolBar = view.findViewById<Toolbar>(R.id.main_app_bar)
        (activity as AppCompatActivity).setSupportActionBar(topToolBar)
        if ((activity as AppCompatActivity).supportActionBar != null) {
            (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            (activity as AppCompatActivity).supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
        topToolBar.setNavigationOnClickListener { v: View? ->
            startActivity(Intent(requireContext(),
                    ChatActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }

        apiService = Client.getClient("https://fcm.googleapis.com/")
                .create(APIService::class.java)
        reference = FirebaseDatabase.getInstance().getReference("Chats")
        reference!!.keepSynced(true)
        imageView = view.findViewById(R.id.chat_user_profile)
        userName = view.findViewById(R.id.username)
        editSend = view.findViewById(R.id.et_send_message)
        btnSend = view.findViewById(R.id.btn_send_message)
        mRecycler = view.findViewById(R.id.rv_message)
        mSendImageMessage = view.findViewById(R.id.btn_send_image)
        mTime = view.findViewById(R.id.tv_time)

        val mArgs = arguments
        val item: ChatItem? = mArgs!!.getParcelable("mChatItem")
        item?.let {
            userid = it.userId
            imageURL = it.imageURL
            userNameString = it.username
            time = it.time
            status = it.status
        }

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
                    "Choose Image"), IMAGE_REQUEST
            )
        }

        //setting up recyclerview
        mRecycler?.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL, false)
        linearLayoutManager.stackFromEnd = true
        mRecycler?.layoutManager = linearLayoutManager
        //creating the adapter
        mAdapter =
            MessageAdapter(
                requireContext(),
                imageURL
            )
        //getting viewmodel
        mRecycler?.adapter = mAdapter
        chatViewModel = ViewModelProviders.of(this)
                .get(ChatViewModel::class.java)
        //observing the pagelist from viewmodel
        chatViewModel!!.chats.observe(requireActivity(),
                androidx.lifecycle.Observer {
            mAdapter!!.submitList(it)
        })

        btnSend?.setOnClickListener {
            notify = true
            val message = editSend?.text.toString().trim {
                it <= ' '
            }

            if (message != "") {
                sendMessage(Constants.getUserId(), userid, message)
            } else {
                Toast.makeText(requireContext(), "Blank message!",
                        Toast.LENGTH_SHORT).show()
            }
            editSend?.setText("")
        }

        newIncomingMessageListener(Constants.getUserId(), userid) //listen for new messages
        val factory = InjectorUtils
                .provideMessagesViewModelFactory(myid, userid)
        mViewModel = ViewModelProviders.of(this, factory)
                .get(MessagesActivityViewModel::class.java)
        smoothScroller = object : LinearSmoothScroller(requireContext()) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_END
            }
        }

        mAdapter!!.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                smoothScroller?.targetPosition = positionStart
                Objects.requireNonNull(mRecycler?.layoutManager)!!.
                startSmoothScroll(smoothScroller)
            }
        })

        mViewModel!!.messagesList.observe(requireActivity(),
                androidx.lifecycle.Observer {
            mAdapter!!.submitList(it)
        })

        userName?.text = userNameString

        if (imageURL == "default") {
            imageView?.setImageResource(R.drawable.profile)
        } else {
            val defaultImageResId = R.drawable.profile
            Picasso.with(requireContext())
                    .load(imageURL)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(imageView, object : Callback {
                        override fun onSuccess() {}
                        override fun onError() {
                            Picasso.with(requireContext())
                                    .load(imageURL)
                                    .error(defaultImageResId)
                                    .into(imageView)
                        }
                    })
        }

        return view
    }

    private fun onMessagesReceived(chats: PagedList<Chat>?) {
        if (chats != null) {
            monitor++
            smoothScroller = object : LinearSmoothScroller(requireContext()) {
                override fun getVerticalSnapPreference(): Int {
                    return SNAP_TO_END
                }
            }
            mAdapter!!.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
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
        val chat = Chat(
            sender, receiver, message, false,
            System.currentTimeMillis(), ""
        )
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
                    val data =
                        Data(
                            myid, R.drawable.livechat,
                            "$username:$message", "New Message", userid
                        )
                    if (BuildConfig.DEBUG && token == null) {
                        error("Assertion failed")
                    }

                    val sender =
                        Sender(
                            data,
                            token!!.token
                        )
                    apiService!!.sendNotification(sender)
                            .enqueue(object : retrofit2.Callback<MyResponse?> {
                                override fun onResponse(call: Call<MyResponse?>,
                                                        response: Response<MyResponse?>) {
                                    if (response.code() == 200) {
                                        if (BuildConfig.DEBUG && response.body() == null) {
                                            error("Assertion failed")
                                        }
                                        if (response.body()!!.success != 1) {
                                            Toast.makeText(requireContext(), "Failed!",
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
        val editor = activity?.getSharedPreferences("PREFS",
                AppCompatActivity.MODE_PRIVATE)?.edit()
        editor?.putString("currentuser", userid)
        editor?.apply()
    }

    override fun onStart() {
        super.onStart()
        Timber.d("ONSTART")
        updateTimeAndDate("online")
        listenToFirebaseRealtimeStatus()
    }

    override fun onStop() {
        super.onStop()
        updateTimeAndDate("offline")
        listenToFirebaseRealtimeStatus()
    }

    override fun onResume() {
        super.onResume()
        //        updateStatus("online");
        currentUser(userid)
        updateTimeAndDate("online")
        listenToFirebaseRealtimeStatus()
        newIncomingMessageListener(Constants.getUserId(), userid)
    }

    override fun onPause() {
        super.onPause()
        //reference.removeEventListener(seenListener);
//        updateStatus("offline");
        currentUser("none")
        updateTimeAndDate("offline")
        listenToFirebaseRealtimeStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        updateTimeAndDate("offline")
        listenToFirebaseRealtimeStatus()
    }

    private fun updateTimeAndDate(status: String) {
        val dbRef = FirebaseDatabase.getInstance().reference
        val userRef = dbRef
                .child("Users")
                .child(myid!!)
                .child("status")
        val saveCurrentTime: String
        val saveCurrentDate: String
        val calendar = Calendar.getInstance()
        @SuppressLint("SimpleDateFormat")
        val currentDate = SimpleDateFormat("MMM dd")
        saveCurrentDate = currentDate.format(calendar.time)
        @SuppressLint("SimpleDateFormat")
        val currentTime = SimpleDateFormat("hh:mm a")
        saveCurrentTime = currentTime.format(calendar.time)
        val hashMap = HashMap<String, Any>()
        hashMap["time"] = saveCurrentTime
        hashMap["date"] = saveCurrentDate
        hashMap["status"] = status
        userRef.updateChildren(hashMap)

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
                                    .getColor(requireContext(),
                                            R.color.colorAccent))
                            mTime?.text = getString(R.string.active_now)
                        }
                        "offline" -> {
                            mTime?.setTextColor(ContextCompat
                                    .getColor(requireContext(),
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
        query.addSnapshotListener(requireActivity()) { snapshots: QuerySnapshot?,
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
        if (requestCode == IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data
                != null && data.data != null) {
            imageUri = data.data
            val selectedImagePath = imageUri
            val selectedImageBmp = MediaStore.Images.Media.getBitmap(activity?.contentResolver,
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