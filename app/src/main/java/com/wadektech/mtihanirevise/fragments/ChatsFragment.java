package com.wadektech.mtihanirevise.fragments;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.adapter.ChatsAdapter;
import com.wadektech.mtihanirevise.notification.Token;
import com.wadektech.mtihanirevise.utils.Constants;
import com.wadektech.mtihanirevise.viewmodels.UsersViewModel;

//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {
    private RecyclerView recyclerView ;
  private ChatsAdapter mAdapter;
    //FirebaseUser firebaseUser ;
    DatabaseReference databaseReference ;

    //private List<Chatlist> usersList ;

    public ChatsFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chats, container, false);
        recyclerView = v.findViewById(R.id.rv_chat_frag);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatList() ;

        updateToken (FirebaseInstanceId.getInstance ().getToken ());

        return v ;
    }
    private void updateToken(String token){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance ().getReference ("Tokens");
        Token token1 = new Token (token);
        databaseReference.child (/*firebaseUser.getUid ()*/Constants.getUserId()).setValue (token1);
    }

    private void chatList() {

        UsersViewModel viewModel = ViewModelProviders.of(getActivity())
                .get(UsersViewModel.class);
        viewModel.getChatList().observe(this,usersList->{
            mAdapter = new ChatsAdapter (getActivity(),true);
            recyclerView.setAdapter(mAdapter);
            mAdapter.submitList(usersList);
        });
    }

}
