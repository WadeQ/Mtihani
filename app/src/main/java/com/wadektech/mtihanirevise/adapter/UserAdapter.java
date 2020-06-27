package com.wadektech.mtihanirevise.adapter;

import android.annotation.SuppressLint;
import androidx.paging.PagedListAdapter;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.room.Chat;
import com.wadektech.mtihanirevise.room.ChatItem;
import com.wadektech.mtihanirevise.room.User;
import com.wadektech.mtihanirevise.ui.MessageActivity;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

public class UserAdapter extends PagedListAdapter<User,UserAdapter.ViewHolder> {
    private Context context;
    private boolean isChatting ;

private final String TAG = "UserAdapter";
    public UserAdapter(Context context, boolean isChatting) {
        super(User.DIFF_CALLBACK);
        this.context = context;
        this.isChatting = isChatting ;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.classroom_user_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final User user = getItem(position);

        assert user != null;
        holder.mStatus.setText (user.getUpdate ());
        holder.mUsername.setText(user.getUsername());
        holder.mTime.setText (user.getTime ());
        if (user.getImageURL().equals("default") ){
            holder.mProfileImage.setImageResource(R.drawable.profile);
        }else {
            final int defaultImageResId = R.drawable.profile;
            Picasso.with(context)
                    .load(user.getImageURL())
                    .networkPolicy (NetworkPolicy.OFFLINE)
                    .into (holder.mProfileImage, new com.squareup.picasso.Callback () {
                        @Override
                        public void onSuccess() {

                        }
                        @Override
                        public void onError() {
                            Picasso.with (context)
                                    .load (user.getImageURL ())
                                    .fit()
                                    .error(defaultImageResId)
                                    .into (holder.mProfileImage);
                        }
                    });
        }

        holder.itemView.setOnClickListener(v -> {

            ChatItem item = new ChatItem(user.getUserId(),user.getUsername(),user.getImageURL(),
                    user.getStatus(),user.getSearch(),user.getUpdate(),user.getTime(),
            user.getDate());

            Intent intent = new Intent(context, MessageActivity.class);
            intent.putExtra("userid", user.getUserId());
            intent.putExtra("imageURL",user.getImageURL());
            intent.putExtra("userName",user.getUsername());
            intent.putExtra("time",user.getTime());
            intent.putExtra("mChatItem",item);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView mUsername , mLastMessage, mStatus;
        public CircleImageView mProfileImage ;
        public CircleImageView mStatusOff , mStatusOn;
        public TextView mTime ;

        public ViewHolder(View itemView) {
            super(itemView);
            mUsername = itemView.findViewById(R.id.username);
            mProfileImage = itemView.findViewById(R.id.chat_user_profile);
            mStatusOff = itemView.findViewById(R.id.img_off);
            mStatusOn = itemView.findViewById(R.id.img_on);
            mLastMessage = itemView.findViewById(R.id.tv_last_msg);
            mStatus = itemView.findViewById (R.id.tv_status);
            mTime = itemView.findViewById (R.id.tv_timestamp);
        }
    }

    @SuppressLint({"TimberArgCount", "SetTextI18n"})
    private void lastMessage(final String userid , final TextView mLastMessage){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference messages = firestore.collection("messages");

        List<Task<QuerySnapshot>> queryArrayList = new ArrayList<>();

        queryArrayList.add(messages.
                whereEqualTo("sender", userid)
                .whereEqualTo("receiver", firebaseUser.getUid())
                .whereLessThan("date", System.currentTimeMillis())
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(1)
                .get());

        queryArrayList.add(messages.
                whereEqualTo("sender", firebaseUser.getUid())
                .whereEqualTo("receiver", userid)
                .whereLessThan("date", System.currentTimeMillis())
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(1)
                .get());
        Task<List<Task<?>>> combinedTask = Tasks.whenAllComplete(queryArrayList
                .toArray(new Task[2]));
        combinedTask.addOnCompleteListener(
                tasks -> {
                    if (tasks.getResult() != null) {
                        List<Chat> lastMessageList = new ArrayList<>();
                        for (Task task : tasks.getResult()) {
                            if (task.isSuccessful()) {
                                QuerySnapshot snapshot = (QuerySnapshot) task.getResult();
                                assert snapshot != null;
                                if (!snapshot.isEmpty()) {
                                    List<Chat> chatList = snapshot.toObjects(Chat.class);
                                    lastMessageList.addAll(chatList);
                                    Timber.d("lastMessage chats received are: %s%s", chatList.size());

                                } else {
                                    Timber.d("lastMessage snapshot is empty");
                                }
                            } else {
                                if (task.getException() != null)
                                    Timber.d(task.getException().toString());
                            }

                        }
                        if (lastMessageList.size() != 0) {
                            if(lastMessageList.size()>1) {
                                if (lastMessageList.get(0).getDate() > lastMessageList.get(1).getDate()) {
                                    mLastMessage.setText(lastMessageList.get(0).getMessage());
                                } else {
                                    mLastMessage.setText(lastMessageList.get(1).getMessage());
                                }
                            }else {
                                mLastMessage.setText(lastMessageList.get(0).getMessage());
                            }
                        } else {
                            mLastMessage.setText("No saved messages yet!");
                        }
                    }

                });
    }
}
