package com.wadektech.mtihanirevise;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.wadektech.mtihanirevise.POJO.User;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {
      private CircleImageView imageView ;
      private TextView userName ;
      FirebaseUser firebaseUser ;
      DatabaseReference reference ;
      EditText editSend ;
      ImageButton btnSend ;

      Intent intent ;
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Toolbar topToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        topToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        imageView = findViewById(R.id.chat_user_profile);
        userName = findViewById(R.id.username);
        editSend = findViewById(R.id.et_send_message);
        btnSend = findViewById(R.id.btn_send_message);

        intent = getIntent() ;
        final String userid = intent.getStringExtra("userid") ;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
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

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
    }
    private void sendMessage(String sender , String receiver , String message){

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender" , sender) ;
        hashMap.put("receiver" , receiver) ;
        hashMap.put("message" , message) ;
        reference.child("Chats").push().setValue(hashMap) ;
    }
}
