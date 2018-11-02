package com.wadektech.mtihanirevise.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wadektech.mtihanirevise.adapter.UserAdapter;
import com.wadektech.mtihanirevise.pojo.Chat;
import com.wadektech.mtihanirevise.pojo.User;
import com.wadektech.mtihanirevise.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {
    private RecyclerView recyclerView ;
    private UserAdapter userAdapter ;
    private List<User> users ;
    FirebaseUser firebaseUser ;
    DatabaseReference databaseReference ;

    private List<String> usersList ;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chats, container, false);
        recyclerView = v.findViewById(R.id.rv_chat_frag);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        usersList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("Chats") ;
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               usersList.clear();
               for (DataSnapshot snapshot : dataSnapshot.getChildren()){

                   Chat chat = snapshot.getValue(Chat.class);
                   assert chat != null;
                   if (chat.getSender().equals(firebaseUser.getUid())){
                       usersList.add(chat.getReceiver());
                   }
                   if (chat.getReceiver().equals(firebaseUser.getUid())){
                       usersList.add(chat.getSender());
                   }
               }
               readChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return v ;
    }
    public void readChats(){
        users = new ArrayList<>() ;
        databaseReference = FirebaseDatabase.getInstance().getReference("Users") ;
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              users.clear();
              //display one user on chat room
              for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                  User user = snapshot.getValue(User.class);
                  for (String id : usersList){
                      assert user != null;
                      if (user.getId().equals(id)){
                          if (users.size() != 0){
                              for (User user1 : users){
                                  if (!user.getId().equals(user1.getId())){
                                      users.add(user);
                                  }
                              }
                          }else {
                              users.add(user);
                          }
                      }
                  }
              }
              userAdapter = new UserAdapter(getContext(), users);
              recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
