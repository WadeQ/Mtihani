package com.wadektech.mtihanirevise.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.wadektech.mtihanirevise.R
import com.wadektech.mtihanirevise.fragments.MessageFragment
import com.wadektech.mtihanirevise.pojo.Status
import com.wadektech.mtihanirevise.room.ChatItem
import com.wadektech.mtihanirevise.room.User
import com.wadektech.mtihanirevise.ui.MessageActivity
import de.hdodenhof.circleimageview.CircleImageView
import timber.log.Timber

class UserAdapter(private val context: Context, private val isChatting: Boolean)
    : PagedListAdapter<User?, UserAdapter.ViewHolder>(User.DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
                .inflate(R.layout.classroom_user_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position)!!
        holder.mStatus.text = user.update
        holder.mUsername.text = user.username

         val rootRef : DatabaseReference = FirebaseDatabase
                .getInstance()
                .reference
         val userRef = rootRef
                .child("Users")
                .child(user.userId)
                .child("status")
        userRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(Status::class.java)
                    if (user != null) {
                        when(user.status){
                            "online" -> {
                                holder.mUserOnlineStatus.setTextColor(ContextCompat
                                        .getColor(context,
                                                R.color.green))
                                holder.mUserOnlineStatus.text = "online"
                            }
                            "offline" -> {
                                holder.mUserOnlineStatus.setTextColor(ContextCompat
                                        .getColor(context,
                                                R.color.green))
                                holder.mUserOnlineStatus.text = "offline"
                            }
                            else -> {
                                holder.mUserOnlineStatus.text = "offline"
                            }
                        }
                    }
            }

            @SuppressLint("BinaryOperationInTimber")
            override fun onCancelled(error: DatabaseError) {
                Timber.e("Error listening to realtime Status "+error.message)
            }
        })

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