package com.wadektech.mtihanirevise.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.adapter.MessageAdapter;
import com.wadektech.mtihanirevise.fragments.APIService;
import com.wadektech.mtihanirevise.notification.Client;
import com.wadektech.mtihanirevise.notification.Data;
import com.wadektech.mtihanirevise.notification.MyResponse;
import com.wadektech.mtihanirevise.notification.Sender;
import com.wadektech.mtihanirevise.notification.Token;
import com.wadektech.mtihanirevise.pojo.Chat;
import com.wadektech.mtihanirevise.pojo.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MessageActivity extends AppCompatActivity {
      private CircleImageView imageView ;
      private TextView userName , mTime  ;
      FirebaseUser firebaseUser ;
      DatabaseReference reference ;
      EditText editSend ;
      ImageButton btnSend ;
      MessageAdapter mAdapter ;
      List<Chat> chats ;
      RecyclerView mRecycler ;

      Intent intent ;
      private String myid;
      private String userid;
      boolean notify = false;

    APIService apiService ;

    private ValueEventListener seenListener ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        setTitle(null);
        Toolbar topToolBar = findViewById(R.id.main_app_bar);
        setSupportActionBar(topToolBar);
        if(getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        topToolBar.setNavigationOnClickListener(v -> startActivity(new Intent(MessageActivity.this, ChatActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));

        apiService = Client.getClient ("https://fcm.googleapis.com/").create (APIService.class);

        reference = FirebaseDatabase.getInstance ().getReference ("Chats");
        reference.keepSynced (true);

        imageView = findViewById(R.id.chat_user_profile);
        userName = findViewById(R.id.username);
        editSend = findViewById(R.id.et_send_message);
        btnSend = findViewById(R.id.btn_send_message);
        mRecycler = findViewById(R.id.rv_message);
        mTime = findViewById (R.id.tv_time);
        mRecycler.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(linearLayoutManager);


        intent = getIntent() ;
        final String userid = intent.getStringExtra("userid") ;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        btnSend.setOnClickListener(v -> {
            notify = true;
            String message = editSend.getText().toString();
            if (!message.equals("")){
                sendMessage(firebaseUser.getUid(), userid, message);
            }else {
                Toast.makeText(getApplicationContext(),"Blank message!", Toast.LENGTH_SHORT).show();
            }
            editSend.setText("");
        });

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class) ;
                if (user != null) {
                    userName.setText(user.getUsername());
                    mTime.setText ("Last seen " + user.getTime ());
                }
                if (user != null) {
                    if (user.getImageURL().equals("default")){
                       imageView.setImageResource(R.drawable.profile);
                    } else {
                        final int defaultImageResId = R.drawable.profile;
                        Picasso.with(getApplicationContext ())
                                .load(user.getImageURL())
                                .networkPolicy (NetworkPolicy.OFFLINE)
                                .into (imageView, new com.squareup.picasso.Callback () {
                                    @Override
                                    public void onSuccess() {

                                    }
                                    @Override
                                    public void onError() {
                                        Picasso.with (getApplicationContext ())
                                                .load (user.getImageURL ())
                                                .networkPolicy (NetworkPolicy.NO_CACHE)
                                                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).error (defaultImageResId)
                                                .into (imageView);
                                    }
                                });
                    }
                    readMessages(firebaseUser.getUid(), userid,user.getImageURL());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        seenMessage(userid);
    }
    private void seenMessage(final String userid){
      reference = FirebaseDatabase.getInstance().getReference("Chats");
      reference.keepSynced (true);
      seenListener = reference.addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                  Chat chat = snapshot.getValue(Chat.class);
                  assert chat != null;
                  if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)){
                          HashMap<String , Object> hashMap = new HashMap<>();
                          hashMap.put("isseen" , true);
                          snapshot.getRef().updateChildren(hashMap);
                  }
              }
          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {

          }
      });
    }
    private void sendMessage(String sender , final String receiver , String message){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference() ;
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender" , sender) ;
        hashMap.put("receiver" , receiver) ;
        hashMap.put("message" , message) ;
        hashMap.put("isseen" , false);
        reference.child("Chats").push().setValue(hashMap) ;
        final DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(firebaseUser.getUid())
                .child(userid);
        chatReference.keepSynced (true);
        chatReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatReference.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        final String msg = message;
        reference = FirebaseDatabase.getInstance ().getReference ("Users").child (firebaseUser.getUid ());
        reference.addValueEventListener (new ValueEventListener () {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               User user = dataSnapshot.getValue (User.class);
                if (notify) {
                    sendNotification (receiver, user.getUsername (), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void sendNotification(String receiver, final String username, final String message){
        DatabaseReference tokens = FirebaseDatabase.getInstance ().getReference ("Tokens");
        Query query = tokens.orderByKey ().equalTo (receiver);
        query.addValueEventListener (new ValueEventListener () {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              for (DataSnapshot snapshot : dataSnapshot.getChildren ()){
                  Token token = snapshot.getValue (Token.class);
                  Data data = new Data(firebaseUser.getUid (), R.drawable.livechat, username +":"+message, "New Message", userid);

                  assert token != null;
                  Sender sender = new Sender (data,token.getToken ());

                  apiService.sendNotification (sender)
                          .enqueue (new Callback<MyResponse> () {
                              @Override
                              public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                  if (response.code () == 200){
                                      assert response.body () != null;
                                      if (response.body ().success != 1){
                                       Toast.makeText (getApplicationContext (), "Failed!" ,Toast.LENGTH_SHORT).show ();
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
    private void readMessages(final String myid , final String userid, final String imageurl) {
        this.myid = myid;
        this.userid = userid;
        chats = new ArrayList<>() ;
         reference = FirebaseDatabase.getInstance().getReference("Chats") ;
         reference.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             chats.clear();
             for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                 Chat chat = snapshot.getValue(Chat.class);
                 assert chat != null;
                 if (chat.getReceiver() != null && chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                    chat.getSender() != null && chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                     chats.add(chat) ;
                 }
                 mAdapter = new MessageAdapter(MessageActivity.this, chats , imageurl);
                 mRecycler.setAdapter(mAdapter);
                 mRecycler.scrollToPosition (chats.size () -1);
             }
             }
             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {
             }
         });
    }
    private void currentUser(String userid){
        SharedPreferences.Editor editor = getSharedPreferences ("PREFS" , MODE_PRIVATE).edit () ;
        editor.putString ("currentuser", userid) ;
        editor.apply ();
    }

    @Override
    protected void onStart() {
        super.onStart ();
    }

    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String , Object> hashMap = new HashMap<>();
        hashMap.put("status" , status) ;
        reference.updateChildren(hashMap);
    }
     @Override
    protected void onResume(){
        super.onResume();
        status("online");
        currentUser (userid);
        updateTimeAndDate ();
    }
    @Override
    protected void onPause(){
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
        currentUser ("none");
        updateTimeAndDate ();
    }
    private void updateTimeAndDate(){
        String saveCurrentTime , saveCurrentDate ;
        Calendar calendar = Calendar.getInstance ();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentDate = new SimpleDateFormat ("MMM dd,yyyy");
        saveCurrentDate =currentDate.format (calendar.getTime ());

        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentTime = new SimpleDateFormat ("hh:mm a");
        saveCurrentTime =currentTime.format (calendar.getTime ());
        reference = FirebaseDatabase.getInstance ().getReference ("Users").child (firebaseUser.getUid ());
        HashMap<String , Object> hashMap = new HashMap<>();
        hashMap.put ("time" , saveCurrentTime);
        hashMap.put ("date" , saveCurrentDate);
        reference.updateChildren (hashMap);
    }
}
