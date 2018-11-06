package com.wadektech.mtihanirevise.ui;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.wadektech.mtihanirevise.adapter.MessageAdapter;
import com.wadektech.mtihanirevise.pojo.Chat;
import com.wadektech.mtihanirevise.pojo.User;
import com.wadektech.mtihanirevise.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MessageActivity extends AppCompatActivity {
      private CircleImageView imageView ;
      private TextView userName ;
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
        topToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MessageActivity.this, ChatActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
        imageView = findViewById(R.id.chat_user_profile);
        userName = findViewById(R.id.username);
        editSend = findViewById(R.id.et_send_message);
        btnSend = findViewById(R.id.btn_send_message);
        mRecycler = findViewById(R.id.rv_message);
        mRecycler.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(linearLayoutManager);


        intent = getIntent() ;
        final String userid = intent.getStringExtra("userid") ;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editSend.getText().toString();
                if (!message.equals("")){
                    sendMessage(firebaseUser.getUid(), userid, message);
                }else {
                    Toast.makeText(getApplicationContext(),"Blank message!", Toast.LENGTH_SHORT).show();
                }
                editSend.setText("");
            }
        });
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class) ;
                if (user != null) {
                    userName.setText(user.getUsername());
                }
                if (user != null) {
                    if (user.getImageURL().equals("default")){
                       imageView.setImageResource(R.drawable.profile);
                    } else {
                        Picasso.with(getApplicationContext())
                                .load(user.getImageURL())
                                .into(imageView);
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
    private void sendMessage(String sender , String receiver , String message){
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
             }
             }
             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {
             }
         });
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
    }
    @Override
    protected void onPause(){
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
    }
}
