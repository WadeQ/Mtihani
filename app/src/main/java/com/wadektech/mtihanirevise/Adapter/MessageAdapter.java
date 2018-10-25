package com.wadektech.mtihanirevise.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import com.wadektech.mtihanirevise.POJO.Chat;
import com.wadektech.mtihanirevise.R;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends  RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    public static final int MSG_TYPE_LEFT = 0 ;
    public static final int MSG_TYPE_RIGHT = 1 ;
    private Context context;
    private List<Chat> chats ;
    private String imageurl ;

    private FirebaseUser firebaseUser ;

    public MessageAdapter(Context context, List<Chat> chats , String imageurl) {
        this.context = context;
        this.chats = chats;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = chats.get(position) ;
        holder.chatMessage.setText(chat.getMessage());
        if (imageurl.equals("default") ){
            holder.chatImage.setImageResource(R.drawable.profile);
        }else {
            Picasso.with(context)
                    .load(imageurl)
                    .into(holder.chatImage);
        }
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
           public TextView chatMessage ;
           public CircleImageView chatImage ;

        public ViewHolder(View itemView) {
            super(itemView);
            chatMessage = itemView.findViewById(R.id.chat_message);
            chatImage = itemView.findViewById(R.id.chat_profile);
        }
    }
    @Override
    public long getItemId(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chats.get(position).getSender().equals(firebaseUser.getUid())){
                return MSG_TYPE_RIGHT ;
        } else {
            return MSG_TYPE_LEFT ;
        }
    }
}
