package com.wadektech.mtihanirevise.adapter;

import android.annotation.SuppressLint;
import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.database.MtihaniDatabase;
import com.wadektech.mtihanirevise.room.ChatItem;
import com.wadektech.mtihanirevise.ui.MessageActivity;
import com.wadektech.mtihanirevise.utils.Constants;

import de.hdodenhof.circleimageview.CircleImageView;

//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;

public class ChatsAdapter extends PagedListAdapter<ChatItem, ChatsAdapter.ViewHolder> {
    private Context context;
    // private List<User> users ;
    private boolean isChatting;
    //private String theLastMessage ;
    private Handler mHandler;

    public ChatsAdapter(Context context, /*List<User> users,*/ boolean isChatting) {
        super(ChatItem.DIFF_CALLBACK);
        this.context = context;
        // this.users = users;
        this.isChatting = isChatting;
        mHandler = new Handler();


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new ChatsAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final ChatItem user = getItem(position);

        //holder.mStatus.setText (user.getUpdate ());
        holder.mUsername.setText(user.getUsername());
        holder.mTime.setText(user.getTime());
        if (user.getImageURL().equals("default")) {
            holder.mProfileImage.setImageResource(R.drawable.profile);
        } else {
            final int defaultImageResId = R.drawable.profile;
            Picasso.with(context)
                    .load(user.getImageURL())
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(holder.mProfileImage, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(context)
                                    .load(user.getImageURL())
                                    .error(defaultImageResId)
                                    .into(holder.mProfileImage);
                        }
                    });
        }
        // if (isChatting){
        holder.lastMessage(user.getUserId());
       /* }else {
            holder.mLastMessage.setVisibility(View.GONE);
        }*/

        if (isChatting) {
            if (user.getStatus().equals("online")) {
                holder.mStatusOn.setVisibility(View.VISIBLE);
                holder.mStatusOff.setVisibility(View.GONE);
            } else if (user.getStatus().equals("offline")) {
                holder.mStatusOn.setVisibility(View.GONE);
                holder.mStatusOff.setVisibility(View.VISIBLE);
            }
        } else {
            holder.mStatusOn.setVisibility(View.GONE);
            holder.mStatusOff.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Log.e("TAG", "Message");
            //just send all the data required to set up MessageActivity.class
            //we alredy have the user here so why fetch him up from firestore again?
            Intent intent = new Intent(context, MessageActivity.class);
            intent.putExtra("userid", user.getUserId());
            intent.putExtra("imageURL", user.getImageURL());
            intent.putExtra("userName", user.getUsername());
            intent.putExtra("time", user.getTime());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mUsername, mLastMessage, mStatus;
        public CircleImageView mProfileImage;
        public CircleImageView mStatusOff, mStatusOn;
        public TextView mTime;

        public ViewHolder(View itemView) {
            super(itemView);
            mUsername = itemView.findViewById(R.id.username);
            mProfileImage = itemView.findViewById(R.id.chat_user_profile);
            mStatusOff = itemView.findViewById(R.id.img_off);
            mStatusOn = itemView.findViewById(R.id.img_on);
            mLastMessage = itemView.findViewById(R.id.tv_last_msg);
            mStatus = itemView.findViewById(R.id.tv_status);
            mTime = itemView.findViewById(R.id.tv_timestamp);
        }

        private void lastMessage(String userid) {
            //final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
       /* theLastMessage = "default" ;
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    assert chat != null;
                    assert firebaseUser != null;
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())){
                        theLastMessage = chat.getMessage() ;
                    }
                }
                switch (theLastMessage){
                    case "default" :
                        mLastMessage.setText("No saved messages yet!");
                        break;

                    default:
                        mLastMessage.setText(theLastMessage);
                        break;
                }

                theLastMessage = "default" ;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

          /*  disposables.add(MtihaniDatabase
                    .getInstance(context
                            .getApplicationContext())
                    .singleMessageDao()
                    .getLastMessage(*//*firebaseUser.getUid()*//*Constants.getUserId(), userid)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(lastMessage -> {

                        if (lastMessage != null) {
                            mStatus.setText(lastMessage);
                        }else{
                            mStatus.setText("");
                        }

                    }, error ->
                    {Log.d(TAG, "database error in ChatsAdapter");
                        mStatus.setText("");
                    }));*/
            new Thread(() -> {

                final String lastMessage = MtihaniDatabase
                        .getInstance(context)
                        .singleMessageDao()
                        .getLastMessage(Constants.getUserId(), userid);


//                  Update the value background thread to UI thread
                mHandler.post(() -> {
                    if (lastMessage != null) {
                        mStatus.setText(lastMessage);
                    } else {
                        mStatus.setText("");
                    }


                });

            }).start();

        }
    }

}
