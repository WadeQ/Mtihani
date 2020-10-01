package com.wadektech.mtihanirevise.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.adapter.MessageAdapter;
import com.wadektech.mtihanirevise.database.MtihaniDatabase;
import com.wadektech.mtihanirevise.room.ChatDao;
import com.wadektech.mtihanirevise.room.ChatItem;
import com.wadektech.mtihanirevise.room.ChatViewModel;
import com.wadektech.mtihanirevise.viewmodels.MessagesActivityViewModel;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MessageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MessageFragment extends Fragment {
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

    public MessageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_message, container, false);
        Toolbar topToolBar = view.findViewById(R.id.main_app_bar);

        return view;
    }
}