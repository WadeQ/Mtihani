package com.wadektech.mtihanirevise.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import hotchemi.android.rate.AppRate.with
import timber.log.Timber
import java.util.*

class UserAdapter(private val context: Context, private val isChatting: Boolean)
    : PagedListAdapter<User?, UserAdapter.ViewHolder>(User.DIFF_CALLBACK) {
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
                .whereEqualTo("userId", user.userId)
                .addSnapshotListener { snapshots: QuerySnapshot?, e: FirebaseFirestoreException? ->
                    if (e != null) {
                        Timber.e("listen:error%s", e.message)
                        return@addSnapshotListener
                    }
                    for (dc in Objects.requireNonNull(snapshots)!!.documentChanges) {
                        Timber.d("Status listener: %s", dc.document.toObject(User::class.java).status)
                        val userStatus = dc.document.toObject(User::class.java)
                        when (userStatus.status) {
                            "online" -> {
                                holder.mUserOnlineStatus.setTextColor(ContextCompat.getColor(context, R.color.green))
                                holder.mUserOnlineStatus.text = "online"

                            }
                            "offline" -> {
                                holder.mUserOnlineStatus.text = "offline"
                            }
                            else -> {
                                holder.mUserOnlineStatus.text = ""+user.date+ ", "+user.time
                            }
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

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mUsername: TextView = itemView.findViewById(R.id.username)
        var mStatus: TextView = itemView.findViewById(R.id.tv_status)
        var mProfileImage: CircleImageView = itemView.findViewById(R.id.chat_user_profile)
        var mUserOnlineStatus: TextView = itemView.findViewById(R.id.tv_user_status)

    }
}