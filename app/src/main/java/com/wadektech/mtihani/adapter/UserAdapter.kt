package com.wadektech.mtihani.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.wadektech.mtihani.R
import com.wadektech.mtihani.fragments.ProfileFragment
import com.wadektech.mtihani.pojo.Status
import com.wadektech.mtihani.room.ChatItem
import com.wadektech.mtihani.room.User
import com.wadektech.mtihani.ui.MessageActivity
import com.wadektech.mtihani.utils.Constants
import com.wadektech.mtihani.utils.snackbar
import de.hdodenhof.circleimageview.CircleImageView
import timber.log.Timber

class UserAdapter(private val context: Context, private val isChatting: Boolean)
    : PagedListAdapter<User?, UserAdapter.ViewHolder>(User.DIFF_CALLBACK) {
    val firebaseUser : FirebaseUser? = FirebaseAuth.getInstance().currentUser

    private lateinit var materialDesignAnimatedDialog: NiftyDialogBuilder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
                .inflate(R.layout.classroom_user_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        materialDesignAnimatedDialog = NiftyDialogBuilder.getInstance(context)
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
                            holder.mUserOnlineStatus.text = "online"
                        }

                        "offline" -> {
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
            val followerRef = firebaseUser?.uid.let {
                FirebaseDatabase
                    .getInstance()
                    .reference
                    .child("Follow")
                    .child(it.toString())
                    .child("Followers")
            }

            followerRef.addValueEventListener(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context,
                        "Something went wrong, request not successful.",
                        Toast.LENGTH_LONG).show()
                    Timber.d("Follow request failed with ${error.message}")
                }

                @SuppressLint("UseCompatLoadingForDrawables")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child(user.userId).exists()){
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
                    else {
                        requestFollowBack()
                    }
                }
            })
        }

        holder.mProfileImage.setOnClickListener {
            //storing user details for quick access later
            val pfs: SharedPreferences = context.getSharedPreferences(Constants.myPreferences,
                    Context.MODE_PRIVATE)
            val editor = pfs.edit()
            editor.putString(Constants.profileId, user.userId)
            editor.apply()
        }

        holder.followBtn.setOnClickListener {
            if (holder.followBtn.text.toString() == "Follow"){
                firebaseUser?.uid.let {
                    FirebaseDatabase
                            .getInstance()
                            .reference
                            .child("Follow")
                            .child(it.toString())
                            .child("Following")
                            .child(user.userId)
                            .setValue(true)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful){
                                    firebaseUser?.uid.let { firebaseUser ->
                                        FirebaseDatabase
                                                .getInstance()
                                                .reference
                                                .child("Follow")
                                                .child(user.userId)
                                                .child("Followers")
                                                .child(firebaseUser.toString())
                                                .setValue(true)
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful){

                                                    }
                                                }
                                    }
                                }
                            }
                }
            } else {

                firebaseUser?.uid.let {
                    FirebaseDatabase
                            .getInstance()
                            .reference
                            .child("Follow")
                            .child(it.toString())
                            .child("Following")
                            .child(user.userId)
                            .removeValue()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful){
                                    firebaseUser?.uid.let { firebaseUser ->
                                        FirebaseDatabase
                                                .getInstance()
                                                .reference
                                                .child("Follow")
                                                .child(user.userId)
                                                .child("Followers")
                                                .child(firebaseUser.toString())
                                                .removeValue()
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful){

                                                    }
                                                }
                                    }
                                }
                            }
                }
            }
        }

        checkUserFollowStatus(user.userId,holder.followBtn)

    }

    private fun requestFollowBack() {
        materialDesignAnimatedDialog
            .withTitle("REQUEST FOLLOW BACK")
            .withMessage("The user you want to message does not follow you, " +
                    "please send FOLLOW REQUEST to enable chats")
            .withDialogColor("#d35400")
            .withButton1Text("REQUEST")
            .isCancelableOnTouchOutside(true)
            .withButton2Text("Cancel")
            .withDuration(700)
            .withEffect(Effectstype.Fall)
            .setButton1Click { sendFollowRequest() }
            .setButton2Click { materialDesignAnimatedDialog.dismiss() }
        materialDesignAnimatedDialog.show()
    }

    private fun sendFollowRequest() {
        Toast.makeText(
            context,
            "Follow back request has been sent successfully," +
                    " after user accepts you will able to chat.",
            Toast.LENGTH_LONG).show()
        materialDesignAnimatedDialog.dismiss()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mUsername: TextView = itemView.findViewById(R.id.username)
        var mStatus: TextView = itemView.findViewById(R.id.tv_status)
        var mProfileImage: CircleImageView = itemView.findViewById(R.id.chat_user_profile)
        var mUserOnlineStatus: TextView = itemView.findViewById(R.id.tv_user_status)
        var followBtn : Button = itemView.findViewById(R.id.follow_btn)

    }

    private fun checkUserFollowStatus(userId: String, followBtn: Button) {
        val followingRef = firebaseUser?.uid.let {
            FirebaseDatabase
                    .getInstance()
                    .reference
                    .child("Follow")
                    .child(it.toString())
                    .child("Following")
        }

        followingRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context,
                        "Something went wrong, request not successful.",
                        Toast.LENGTH_LONG).show()
                Timber.d("Follow request failed with ${error.message}")
            }

            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(userId).exists()){
                    followBtn.text = "Unfollow"
                }
                else {
                    followBtn.text = "Follow"
                }

            }
        })
    }
}