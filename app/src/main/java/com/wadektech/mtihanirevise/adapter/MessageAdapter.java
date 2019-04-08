package com.wadektech.mtihanirevise.adapter;

import android.annotation.SuppressLint;
import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.repository.MtihaniRepository;
import com.wadektech.mtihanirevise.room.Chat;
import com.wadektech.mtihanirevise.utils.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;

public class MessageAdapter extends PagedListAdapter<Chat, MessageAdapter.ViewHolder> {
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    /*
    You do not to need to pass context from the activity
    as it is possible to get context within the adapter or
    viewHolder
     */
    private Context context;
    //chats below is unnecessary & has been commented out
    //private List<Chat> chats ;
    /*
    Consider naming your variables using the camelcase way. its the Java way
    For instance imageurl should be mImageUrl where m shows that it's a member variable
     */
    private String imageurl;

    // private FirebaseUser firebaseUser;

    public MessageAdapter(Context context,/* List<Chat> chats ,*/ String imageurl) {
        super(Chat.DIFF_CALLBACK);
        this.context = context;
        // this.chats = chats;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
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
            /**
             * What are you trying to achieve here?
             */
       /* if (position == chats.size()-1){
            if (chat.isIsseen()){
                holder.mSeen.setText("seen");
            }else {
                holder.mSeen.setText("Delivered");
            }
        }else {
            holder.mSeen.setVisibility(View.GONE);
        }
    }*/
            String strDateFormat = "hh:mm a";
            DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
            String formattedDate = dateFormat.format(new Date(chat.getDate()));
            holder.mSeen.setText(formattedDate);
            chat.setSeen(true);
            // Toast.makeText(context, "documentId is: "+chat.getDocumentId(), Toast.LENGTH_SHORT).show();
            MtihaniRepository.updateMessage(chat);
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView chatMessage;
        public CircleImageView chatImage;
        public TextView mSeen;

        public ViewHolder(View itemView) {
            super(itemView);
            chatMessage = itemView.findViewById(R.id.chat_message);
            chatImage = itemView.findViewById(R.id.chat_profile);
            mSeen = itemView.findViewById(R.id.tv_last_seen);

        }
    }

    @Override
    public int getItemViewType(int position) {
        // Log.d("MessageAdapter", "position is:" + position);
        // firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Chat chat = getItem(position);
        if (chat != null) {
            if (getItem(position).getSender().equals(Constants.getUserId())) {
                return MSG_TYPE_RIGHT;
            } else {
                return MSG_TYPE_LEFT;
            }
        } else {
            return MSG_TYPE_RIGHT;
        }
    }
}
