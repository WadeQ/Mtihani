package com.wadektech.mtihanirevise.ui;

import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.adapter.MessageAdapter;
import com.wadektech.mtihanirevise.database.MtihaniDatabase;
import com.wadektech.mtihanirevise.fragments.APIService;
import com.wadektech.mtihanirevise.notification.Client;
import com.wadektech.mtihanirevise.notification.Data;
import com.wadektech.mtihanirevise.notification.MyResponse;
import com.wadektech.mtihanirevise.notification.Sender;
import com.wadektech.mtihanirevise.notification.Token;
import com.wadektech.mtihanirevise.room.Chat;
import com.wadektech.mtihanirevise.room.ChatDao;
import com.wadektech.mtihanirevise.room.ChatItem;
import com.wadektech.mtihanirevise.room.ChatViewModel;
import com.wadektech.mtihanirevise.room.User;
import com.wadektech.mtihanirevise.utils.Constants;
import com.wadektech.mtihanirevise.utils.InjectorUtils;
import com.wadektech.mtihanirevise.viewmodelfactories.MessagesActivityViewModelFactory;
import com.wadektech.mtihanirevise.viewmodels.MessagesActivityViewModel;
import com.wadektech.mtihanirevise.viewmodels.UsersViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;


public class MessageActivity extends AppCompatActivity {
    private CircleImageView imageView;
    private TextView userName, mTime;
    //FirebaseUser firebaseUser;
    DatabaseReference reference;
    EditText editSend;
    ImageButton btnSend;
    MessageAdapter mAdapter;
    // List<Chat> chats;
    RecyclerView mRecycler;
    private ChatItem mChatItem;
    private boolean shouldAddChatUser = true;
    MtihaniDatabase mtihaniDatabase;
    private ChatViewModel chatViewModel;
    private RecyclerView.SmoothScroller smoothScroller;
    // private String imageurl;
    private static int monitor = 0;
    Intent intent;
    private String myid;
    private String userid;
    private String userNameString;
    private String time;
    boolean notify = false;
    APIService apiService;
    private ChatDao chatDao;
    private String imageURL;
    private ValueEventListener seenListener;
    private MessagesActivityViewModel mViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        setTitle(null);
        Toolbar topToolBar = findViewById(R.id.main_app_bar);
        setSupportActionBar(topToolBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        topToolBar.setNavigationOnClickListener(v -> startActivity(new Intent(MessageActivity.this, ChatActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.keepSynced(true);

        imageView = findViewById(R.id.chat_user_profile);
        userName = findViewById(R.id.username);
        editSend = findViewById(R.id.et_send_message);
        btnSend = findViewById(R.id.btn_send_message);
        mRecycler = findViewById(R.id.rv_message);
        mTime = findViewById(R.id.tv_time);
        intent = getIntent();
        mChatItem = intent.getParcelableExtra("mChatItem");
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("mChatItem")) {
                mChatItem = savedInstanceState.getParcelable("mChatItem");
            }
        }

        getUserStatus();

        userid = intent.getStringExtra("userid");
        imageURL = intent.getStringExtra("imageURL");
        userNameString = intent.getStringExtra("userName");
        time = intent.getStringExtra("time");
        this.myid = Constants.getUserId();

        //setting up recyclerview
        mRecycler.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(linearLayoutManager);
        //creating the adapter
        mAdapter = new MessageAdapter (MessageActivity.this,/* chats ,*/ imageURL);//is this necessary at this point?
        //getting viewmodel
        mRecycler.setAdapter(mAdapter);
        chatViewModel = ViewModelProviders.of(this).get(ChatViewModel.class);
        //observing the pagelist from viewmodel
        chatViewModel.chats.observe(this, mAdapter::submitList);
        // firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        btnSend.setOnClickListener(v -> {
            notify = true;
            String message = editSend.getText().toString().trim();
            if (!message.equals("")) {
                sendMessage(Constants.getUserId(), userid, message);
            } else {
                //Avoid toasting using application context
                Toast.makeText(/*getApplicationContext()*/this, "Blank message!", Toast.LENGTH_SHORT).show();
            }
            editSend.setText("");
        });

        newIncomingMessageListener(Constants.getUserId(), userid);//listen for new messages
        MessagesActivityViewModelFactory factory = InjectorUtils
                .provideMessagesViewModelFactory(myid, userid);
        mViewModel = ViewModelProviders.of(this, factory)
                .get(MessagesActivityViewModel.class);
        smoothScroller = new LinearSmoothScroller(this) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_END;
            }
        };

        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                // mLinearLayout.scrollToPosition(positionStart);
                smoothScroller.setTargetPosition(positionStart);
                Objects.requireNonNull(mRecycler.getLayoutManager()).startSmoothScroll(smoothScroller);
            }
        });
        mViewModel.getMessagesList().observe(this, mAdapter::submitList);
        userName.setText(userNameString);
//        mTime.setText("Last seen " + time);

        if (imageURL.equals("default")) {
            imageView.setImageResource(R.drawable.profile);
        } else {
            final int defaultImageResId = R.drawable.profile;
            Picasso.with(getApplicationContext())
                    .load(imageURL)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(imageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {

                        }
                        
                        @Override
                        public void onError() {
                            Picasso.with(MessageActivity.this)
                                    .load(imageURL)
                                    .error(defaultImageResId)
                                    .into(imageView);
                        }
                    });
        }

    }

    private void getUserStatus(){
        FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
        assert mAuth != null;
        String currentUser = mAuth.getUid();
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userRef = rootRef.child("Users").child(currentUser);
        userRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("status").hasChild("state")){
                        String status = Objects.requireNonNull(dataSnapshot.child("status").child("state").getValue()).toString();
                        String date = Objects.requireNonNull(dataSnapshot.child("status").child("date").getValue()).toString();
                        String time = Objects.requireNonNull(dataSnapshot.child("status").child("time").getValue()).toString();

                        if (status.equals("online")) {
                            mTime.setText("online");
                        } else if (status.equals("offline")) {
                            mTime.setText("Last seen " + date + ", " + time);
                        } else {
                            mTime.setText("offline");
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void onMessagesReceived(PagedList<Chat> chats) {
        Timber.d("messages received" + monitor + " times");
        if (chats != null) {
            monitor++;
            smoothScroller = new LinearSmoothScroller(this) {
                @Override
                protected int getVerticalSnapPreference() {
                    return LinearSmoothScroller.SNAP_TO_END;
                }
            };

            mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    // mLinearLayout.scrollToPosition(positionStart);
                    smoothScroller.setTargetPosition(positionStart);
                    Objects.requireNonNull(mRecycler.getLayoutManager()).startSmoothScroll(smoothScroller);

                }
            });
            mAdapter.submitList(chats);
            mRecycler.setAdapter(mAdapter);
        }
    }

    private void seenMessage(final String userid) {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.keepSynced(true);
        seenListener = reference.addValueEventListener(new ValueEventListener () {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    assert chat != null;
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String sender, final String receiver, String message) {

        Chat chat = new Chat(sender, receiver, message, false, System.currentTimeMillis(), "");

        if (mViewModel != null) {
            if(shouldAddChatUser){
                UsersViewModel.saveChatListUser(mChatItem);
                shouldAddChatUser = !shouldAddChatUser;
            }
            mViewModel.saveMessage(chat);
            mViewModel.sendMessageToFirebase(chat);
        }
        final String msg = message;
        sendNotification(receiver, userNameString, msg);

    }

    private void sendNotification(String receiver, final String username, final String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        tokens.keepSynced(true);
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener () {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(myid, R.drawable.livechat, username + ":" + message, "New Message", userid);

                    assert token != null;
                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        assert response.body() != null;
                                        if (response.body().success != 1) {
                                            Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void currentUser(String userid) {
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Timber.d("ONSTART");
        updateTimeAndDate();
//        updateStatus("online");
//        getUserStatus();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        updateStatus("offline");
    }

    @Override
    protected void onResume() {
        super.onResume();
//        updateStatus("online");
//        getUserStatus();
        currentUser(userid);
        updateTimeAndDate();
        newIncomingMessageListener(Constants.getUserId(), userid);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateTimeAndDate();
//        getUserStatus();
//        updateStatus("online");
        newIncomingMessageListener(Constants.getUserId(), userid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //reference.removeEventListener(seenListener);
//        updateStatus("offline");
//        getUserStatus();
        currentUser("none");
        updateTimeAndDate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        updateStatus("offline");
//        getUserStatus();
    }

    private void updateTimeAndDate() {
        //reference = FirebaseDatabase.getInstance().getReference("Users").child(myid);
        String delegate = "hh:mm aaa";
        String time = (String) DateFormat.format(delegate, Calendar.getInstance().getTime());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("time", time/*String.valueOf(ServerValue.TIMESTAMP)*/);
        // reference.updateChildren(hashMap);
        FirebaseFirestore
                .getInstance()
                .collection("Users")
                .document(myid)
                .update(hashMap);
    }

    /**
     * This is the listener that listens for new messages.
     * By the way the new messages we are interested in are only the messages
     * coming from the sender. certainly we don't want to listen for our own messages
     *
     * @param myId
     * @param userId
     */
    private void newIncomingMessageListener(String myId, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference messagesRef = db.collection("messages");
        com.google.firebase.firestore.Query query = messagesRef
                .whereEqualTo("sender", userId)
                .whereEqualTo("receiver", myId)
                .whereGreaterThan("date", System.currentTimeMillis() - (30 * 60 * 1000))
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING);
        query.addSnapshotListener(this, (snapshots, e) -> {
            if (e != null) {
                Timber.d("error while listening %s", e.toString());
                return;
            }
            if (snapshots != null && !snapshots.isEmpty()) {
                if (mViewModel != null) {
                    List<Chat> chatList = new ArrayList<>();
                    for (DocumentSnapshot document : snapshots.getDocuments()) {
                        Chat chat = document.toObject(Chat.class);
                        if (chat != null) {
                            chat.setDocumentId(document.getId());
                            chatList.add(chat);
                        }
                    }
                    mViewModel.saveNewMessages(chatList);

                } else {
                    // Toast.makeText(this, "viewmodel is null", Toast.LENGTH_SHORT).show();
                }
            } else {
                //Toast.makeText(this, "snap is null or empty", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mChatItem != null)
            outState.putParcelable("mChatItem", mChatItem);
    }

//    public void updateStatus(String status) {
//        String saveCurrentTime, saveCurrentDate ;
//        Calendar calendar = Calendar.getInstance();
//        FirebaseAuth mAuth = FirebaseAuth.getInstance();
//        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
//        @SuppressLint("SimpleDateFormat")
//        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
//        saveCurrentDate = currentDate.format(calendar.getTime());
//        @SuppressLint("SimpleDateFormat")
//        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
//        saveCurrentTime = currentTime.format(calendar.getTime());
//        HashMap<String, Object> statusMap = new HashMap<>();
//        statusMap.put("time", saveCurrentTime);
//        statusMap.put("date", saveCurrentDate);
//        statusMap.put("state", status);
//
//        String currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
//        rootRef.child("Users").child(currentUserId).child("status").updateChildren(statusMap);
//
//    }
}

