package com.wadektech.mtihanirevise.adapter;

import android.annotation.SuppressLint;
import androidx.paging.PagedListAdapter;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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
import com.squareup.picasso.Picasso;
import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.repository.MtihaniRepository;
import com.wadektech.mtihanirevise.room.Chat;
import com.wadektech.mtihanirevise.utils.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class MessageAdapter extends PagedListAdapter<Chat, MessageAdapter.ViewHolder> {
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    private Context context;
    private String imageurl;

    public MessageAdapter(Context context, String imageurl) {
        super(Chat.DIFF_CALLBACK);
        this.context = context;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
            return new ViewHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = getItem(position);
        if (chat != null) {
            holder.chatMessage.setText(chat.getMessage());
            if (imageurl.equals("default")) {
                holder.chatImage.setImageResource(R.drawable.profile);
            } else {
                Picasso.with(context)
                        .load(imageurl)
                        .into(holder.chatImage);
            }

            String saveCurrentTime, saveCurrentDate ;
            Calendar calendar = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
            saveCurrentDate = currentDate.format(calendar.getTime());
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
            saveCurrentTime = currentTime.format(calendar.getTime());
            holder.mSeen.setText(""+saveCurrentDate+", "+saveCurrentTime);
            chat.setSeen(true);
            MtihaniRepository.updateMessage(chat);

        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView chatMessage;
        public CircleImageView chatImage;
        public TextView mSeen, mStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            chatMessage = itemView.findViewById(R.id.chat_message);
            chatImage = itemView.findViewById(R.id.chat_profile);
            mSeen = itemView.findViewById(R.id.tv_last_seen);
            mStatus =itemView.findViewById(R.id.tv_time);

        }
    }

    @Override
    public int getItemViewType(int position) {
        Chat chat = getItem(position);
        if (chat != null) {
            if (Objects.requireNonNull(getItem(position)).getSender().equals(Constants.getUserId())) {
                return MSG_TYPE_RIGHT;
            } else {
                return MSG_TYPE_LEFT;
            }
        } else {
            return MSG_TYPE_RIGHT;
        }
    }

}
