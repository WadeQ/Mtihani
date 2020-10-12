package com.wadektech.mtihanirevise.ui;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
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
import com.wadektech.mtihanirevise.room.Status;
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
    private static final int IMAGE_REQUEST = 2366;
    private CircleImageView imageView;
    private TextView userName, mTime ;
    //FirebaseUser firebaseUser;
    DatabaseReference reference;
    EditText editSend;
    ImageButton btnSend, mSendImageMessage;
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
    private String myid, status, date;
    private String userid;
    private String userNameString;
    private String time;
    boolean notify = false;
    APIService apiService;
    private ChatDao chatDao;
    private String imageURL;
    private ValueEventListener seenListener;
    private MessagesActivityViewModel mViewModel;
    private String checker = "";
    private Uri imageUri ;
    private String myImageUrl = "" ;
    private StorageTask imageUploadTask ;

    public MessageActivity() {}


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
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
        mSendImageMessage = findViewById(R.id.btn_send_image);
        mTime = findViewById(R.id.tv_time);
        intent = getIntent();
        mChatItem = intent.getParcelableExtra("mChatItem");
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("mChatItem")) {
                mChatItem = savedInstanceState.getParcelable("mChatItem");
            }
        }

        mSendImageMessage.setOnClickListener(v -> {
            CharSequence[] options = new CharSequence[]{
                    "images",
                    "Pdf Files",
                    "Ms Word Files/Docs"
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select file type...");
            builder.setItems(options, (DialogInterface dialog, int which) -> {
                if (which == 0){
                    checker = "image";
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Choose Image"), IMAGE_REQUEST);
                } else if (which == 1){
                    checker = "pdf";
                } else {
                    checker = "docx";
                }
            });
        });


        getUserStatus();

        userid = intent.getStringExtra("userid");
        imageURL = intent.getStringExtra("imageURL");
        userNameString = intent.getStringExtra("userName");
        time = intent.getStringExtra("time");
        status = intent.getStringExtra("status");
        this.myid = Constants.getUserId();

        //setting up recyclerview
        mRecycler.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(linearLayoutManager);
        //creating the adapter
        mAdapter = new MessageAdapter (MessageActivity.this,imageURL);//is this necessary at this point?
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

    @SuppressLint("SetTextI18n")
    private void getUserStatus(){
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference usersRef = firestore.collection("Users").document();
        usersRef.addSnapshotListener((documentSnapshot, e) -> {
            assert documentSnapshot != null;
            if (e != null && documentSnapshot.exists()){
                String status = documentSnapshot.getString("status");
//                Timber.d("getUserStatus: %s", status);
                Toast.makeText(getApplicationContext(), "Updated status is: "+status, Toast.LENGTH_LONG).show();
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
        updateTimeAndDate("online");
//        updateStatus("online");
        getUserStatus();
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateTimeAndDate("offline");
//        updateStatus("offline");
    }

    @Override
    protected void onResume() {
        super.onResume();
//        updateStatus("online");
        getUserStatus();
        currentUser(userid);
        updateTimeAndDate("online");
        newIncomingMessageListener(Constants.getUserId(), userid);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateTimeAndDate("online");
        getUserStatus();
//        updateStatus("online");
        newIncomingMessageListener(Constants.getUserId(), userid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //reference.removeEventListener(seenListener);
//        updateStatus("offline");
        getUserStatus();
        currentUser("none");
        updateTimeAndDate("offline");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateTimeAndDate("offline");
//        updateStatus("offline");
        getUserStatus();
    }

    private void updateTimeAndDate(String status) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userRef = dbRef.child("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        String saveCurrentTime, saveCurrentDate ;
        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("time", saveCurrentTime);
        hashMap.put("date", saveCurrentDate);
        hashMap.put("status", status);
        userRef.updateChildren(hashMap);
//        FirebaseFirestore
//                .getInstance()
//                .collection("Users")
//                .document(myid)
//                .update(hashMap);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            imageUri = data.getData();
            if (!checker.equals("image")){

            } if (checker.equals("image")){
                StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("Image Messages");
                StorageReference = imageRef.child()
                sendMessage(Constants.getUserId(), userid, message);

            } else {
                Toast.makeText(getApplicationContext(), "Nothing selected! Please select file..",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

}

