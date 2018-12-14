package com.wadektech.mtihanirevise.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.wadektech.mtihanirevise.pojo.Chat;
import com.wadektech.mtihanirevise.ui.MessageActivity;
import com.wadektech.mtihanirevise.pojo.User;
import com.wadektech.mtihanirevise.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{
    private Context context;
    private List<User> users ;
    private boolean isChatting ;
    private String theLastMessage ;

    public UserAdapter(Context context, List<User> users, boolean isChatting) {
        this.context = context;
        this.users = users;
        this.isChatting = isChatting ;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final User user = users.get(position) ;

        holder.mStatus.setText (user.getUpdate ());
        holder.mUsername.setText(user.getUsername());
        holder.mTime.setText (user.getTime ());
       // holder.mDate.setText (user.getDate ());

        if (user.getImageURL().equals("default") ){
            holder.mProfileImage.setImageResource(R.drawable.profile);
        }else {
            final int defaultImageResId = R.drawable.profile;
            Picasso.with(context)
                    .load(user.getImageURL())
                    .networkPolicy (NetworkPolicy.OFFLINE)
                    .into (holder.mProfileImage, new Callback () {
                        @Override
                        public void onSuccess() {

                        }
                        @Override
                        public void onError() {
                            Picasso.with (context)
                                    .load (user.getImageURL ())
                                    .networkPolicy (NetworkPolicy.NO_CACHE)
                                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).error (defaultImageResId)
                                    .into (holder.mProfileImage);
                        }
                    });
        }
        if (isChatting){
           lastMessage(user.getId() , holder.mLastMessage);
        }else {
            holder.mLastMessage.setVisibility(View.GONE);
        }

        if (isChatting){
            if (user.getStatus().equals("online")){
                holder.mStatusOn.setVisibility(View.VISIBLE);
                holder.mStatusOff.setVisibility(View.GONE);
            }else if (user.getStatus().equals("offline")){
                holder.mStatusOn.setVisibility(View.GONE);
                holder.mStatusOff.setVisibility(View.VISIBLE);
            }
        }else {
            holder.mStatusOn.setVisibility(View.GONE);
            holder.mStatusOff.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("TAG","Message");
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("userid", user.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
           public TextView mUsername , mLastMessage, mStatus;
           public CircleImageView mProfileImage ;
           public CircleImageView mStatusOff , mStatusOn;
           public TextView mTime , mDate;

        public ViewHolder(View itemView) {
            super(itemView);
            mUsername = itemView.findViewById(R.id.username);
            mProfileImage = itemView.findViewById(R.id.chat_user_profile);
            mStatusOff = itemView.findViewById(R.id.img_off);
            mStatusOn = itemView.findViewById(R.id.img_on);
            mLastMessage = itemView.findViewById(R.id.tv_last_msg);
            mStatus = itemView.findViewById (R.id.tv_status);
            mTime = itemView.findViewById (R.id.tv_timestamp);
            mDate = itemView.findViewById (R.id.tv_datestamp);
        }
    }
    private void lastMessage(final String userid , final TextView mLastMessage){
       theLastMessage = "default" ;
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
        });
    }
}
