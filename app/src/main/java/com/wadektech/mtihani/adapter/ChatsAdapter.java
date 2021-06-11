package com.wadektech.mtihani.adapter;

import androidx.core.content.ContextCompat;
import androidx.paging.PagedListAdapter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.wadektech.mtihani.R;
import com.wadektech.mtihani.database.MtihaniDatabase;
import com.wadektech.mtihani.pojo.Status;
import com.wadektech.mtihani.room.ChatItem;
import com.wadektech.mtihani.ui.MessageActivity;
import com.wadektech.mtihani.utils.Constants;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsAdapter extends PagedListAdapter<ChatItem, ChatsAdapter.ViewHolder> {
    private final Context context;
    private boolean isChatting;
    private final Handler mHandler;

    public ChatsAdapter(Context context, boolean isChatting) {
        super(ChatItem.DIFF_CALLBACK);
        this.context = context;
        this.isChatting = isChatting;
        mHandler = new Handler();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.chats_users_item, parent, false);
        return new ChatsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ChatItem user = getItem(position);
        assert user != null;
        holder.mUsername.setText(user.getUsername());

        DatabaseReference dRef = FirebaseDatabase
                .getInstance()
                .getReference();
        dRef.child("Users")
                .child(user.getUserId())
                .child("status")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Status user = snapshot.getValue(Status.class);
                        if (user != null) {
                            switch (user.getStatus()) {
                                case "online":
                                    holder.mTime.setTextColor(ContextCompat
                                            .getColor(context, R.color.green));
                                    holder.mTime.setText("online");
                                    break;

                                case "offline":
                                    holder.mTime.setTextColor(ContextCompat
                                            .getColor(context, R.color.green));
                                    holder.mTime.setText(" "+ user.getDate());
                                    break;

                                default:
                                    holder.mTime.setText("offline");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

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

        if (user.getUserId() != Constants.getUserId()){
            holder.lastMessage(user.getUserId());
        }

        holder.itemView.setOnClickListener(v -> {
            ChatItem chatItem = new ChatItem(
                    user.getUserId(), user.getUsername(), user.getImageURL(),
                    user.getStatus(), user.getSearch(), user.getUpdate(), user.getTime(),
                    user.getDate());
            Intent intent = new Intent(context, MessageActivity.class);
            intent.putExtra("userid", user.getUserId());
            intent.putExtra("imageURL", user.getImageURL());
            intent.putExtra("userName", user.getUsername());
            intent.putExtra("time", user.getTime());
            intent.putExtra("mChatItem", chatItem);
            intent.putExtra("status", user.getStatus());
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
            mTime = itemView.findViewById(R.id.tv_time_status);

        }

        private void lastMessage(String userid) {
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
