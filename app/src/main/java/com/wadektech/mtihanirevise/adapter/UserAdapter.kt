package com.wadektech.mtihanirevise.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.wadektech.mtihanirevise.R
import com.wadektech.mtihanirevise.room.ChatItem
import com.wadektech.mtihanirevise.room.User
import com.wadektech.mtihanirevise.ui.MessageActivity
import com.wadektech.mtihanirevise.utils.Constants
import de.hdodenhof.circleimageview.CircleImageView
import timber.log.Timber
import java.util.*

class UserAdapter(private val context: Context, private val isChatting: Boolean) : PagedListAdapter<User?, UserAdapter.ViewHolder>(User.DIFF_CALLBACK) {
    private val TAG = "UserAdapter"
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.classroom_user_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position)!!
        holder.mStatus.text = user.update
        holder.mUsername.text = user.username

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("Users")
                .whereEqualTo("userId", Constants.getUserId())
                .addSnapshotListener { snapshots: QuerySnapshot?, e: FirebaseFirestoreException? ->
                    if (e != null) {
                        Timber.e("listen:error%s", e.message)
                        return@addSnapshotListener
                    }
                    for (dc in Objects.requireNonNull(snapshots)!!.documentChanges) {
                        Timber.d("Status listener: %s", dc.document.data)
                        if (user.status == "online") {
                            holder.mTime.setTextColor(ContextCompat.getColor(context, R.color.green))
                            holder.mTime.text = "online"
                        }
                    }
                }
        if (user.imageURL == "default") {
            holder.mProfileImage.setImageResource(R.drawable.profile)
        } else {
            val defaultImageResId = R.drawable.profile
            Picasso.with(context)
                    .load(user.imageURL)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(holder.mProfileImage, object : Callback {
                        override fun onSuccess() {}
                        override fun onError() {
                            Picasso.with(context)
                                    .load(user.imageURL)
                                    .fit()
                                    .error(defaultImageResId)
                                    .into(holder.mProfileImage)
                        }
                    })
        }
        holder.itemView.setOnClickListener { v: View? ->
            val item = ChatItem(user.userId, user.username, user.imageURL,
                    user.status, user.search, user.update, user.time,
                    user.date)
            val intent = Intent(context, MessageActivity::class.java)
            intent.putExtra("userid", user.userId)
            intent.putExtra("imageURL", user.imageURL)
            intent.putExtra("userName", user.username)
            intent.putExtra("time", user.time)
            intent.putExtra("mChatItem", item)
            intent.putExtra("status", user.status)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mUsername: TextView
        var mLastMessage: TextView
        var mStatus: TextView
        var mProfileImage: CircleImageView
        var mStatusOff: CircleImageView
        var mStatusOn: CircleImageView
        var mTime: TextView
        var mStatusUpdate: TextView

        init {
            mUsername = itemView.findViewById(R.id.username)
            mProfileImage = itemView.findViewById(R.id.chat_user_profile)
            mStatusOff = itemView.findViewById(R.id.img_off)
            mStatusOn = itemView.findViewById(R.id.img_on)
            mLastMessage = itemView.findViewById(R.id.tv_last_msg)
            mStatus = itemView.findViewById(R.id.tv_status)
            mTime = itemView.findViewById(R.id.tv_timestamp)
            mStatusUpdate = itemView.findViewById(R.id.tv_time)
        }
    }
}