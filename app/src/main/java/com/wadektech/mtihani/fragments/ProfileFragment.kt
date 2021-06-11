package com.wadektech.mtihani.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.wadektech.mtihani.R
import com.wadektech.mtihani.persistence.MtihaniRevise.Companion.app
import com.wadektech.mtihani.room.User
import com.wadektech.mtihani.ui.ChatActivity
import com.wadektech.mtihani.ui.StatusUpdate
import com.wadektech.mtihani.utils.Constants
import de.hdodenhof.circleimageview.CircleImageView
import timber.log.Timber
import java.util.*

//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment() {
    var profileImage: CircleImageView? = null
    var userName: TextView? = null
    private var statusDisplay: TextView? = null
    var userEmail: TextView? = null
    var databaseReference: DatabaseReference? = null
    var storageReference: StorageReference? = null
    private var imageUri: Uri? = null
    private var uploadTask: StorageTask<UploadTask.TaskSnapshot?>? = null
    private var profileId: String? = null
    private var firebaseUser: FirebaseUser? = null
    private var following : TextView? = null
    private var followers : TextView? = null

    override fun onStop() {
        super.onStop()
        val sharedPreferences = requireContext().getSharedPreferences(Constants.myPreferences,
                Context.MODE_PRIVATE)?.edit()
        sharedPreferences?.putString(Constants.profileId, firebaseUser?.uid)
        sharedPreferences?.apply()
    }

    override fun onPause() {
        super.onPause()
        val sharedPreferences = requireContext().getSharedPreferences(Constants.myPreferences,
                Context.MODE_PRIVATE)?.edit()
        sharedPreferences?.putString(Constants.profileId, firebaseUser?.uid)
        sharedPreferences?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        val sharedPreferences = requireContext().getSharedPreferences(Constants.myPreferences,
                Context.MODE_PRIVATE)?.edit()
        sharedPreferences?.putString(Constants.profileId, firebaseUser?.uid)
        sharedPreferences?.apply()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        getTotalFollowers()
        getTotalFollowings()
        checkCurrentUserFollowings()
        checkCurrentUserFollowers()

        firebaseUser = FirebaseAuth.getInstance().currentUser
        following = view.findViewById(R.id.following_value)
        followers = view.findViewById(R.id.followers_value)
        profileImage = view.findViewById(R.id.profile_image)
        userName = view.findViewById(R.id.username)
        val btnStatus = view.findViewById<ImageButton>(R.id.update)
        statusDisplay = view.findViewById(R.id.status_display)
        statusDisplay?.let {
            it.text = Constants.getStatus()
        }
        userName?.let {
            it.text = Constants.getUserName()
        }

        if (Constants.getFollowingCount() == ""){
            following?.text = "0"
        } else {
            following?.text = Constants.getFollowingCount()
        }

        if (Constants.getFollowerCount() == ""){
            followers?.text = "0"
        } else {
            followers?.text = Constants.getFollowerCount()
        }

        val navigateBack = view.findViewById<TextView>(R.id.nav_back)
        userEmail = view.findViewById(R.id.user_email)
        userEmail?.let {
            it.text = Constants.getEmail()
        }

        navigateBack.setOnClickListener { v: View? ->
            val intent = Intent(context, ChatActivity::class.java)
            startActivity(intent)
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser
        profileId = Constants.getUserProfileID()

        if (firebaseUser != null) {
            if (profileId == firebaseUser!!.uid) {
//                checkFollowersAndFollowings()
            }
        }

        btnStatus.setOnClickListener { v: View? ->
            val intent = Intent(context, StatusUpdate::class.java)
            startActivity(intent)
        }

        profileImage?.setOnClickListener { v: View? ->
            openImage()
        }

        FirebaseFirestore
                .getInstance()
                .collection("Users")
                .document(Constants.getUserId())
                .get()
                .addOnSuccessListener(requireActivity()) { snapshot: DocumentSnapshot? ->
                    if (snapshot != null) {
                        if (snapshot.exists()) {
                            val user = snapshot.toObject(User::class.java)
                            if (user != null) {
                                val status = user.update
                                val pfs = Objects.requireNonNull(app)
                                        ?.applicationContext
                                        ?.getSharedPreferences(Constants.myPreferences,
                                                Context.MODE_PRIVATE)
                                val editor = pfs?.edit()
                                editor?.putString(Constants.status, status)
                                editor?.apply()
                            }
                        }
                    }
                }

        storageReference = FirebaseStorage.getInstance().getReference("uploads")
        val pfs = requireActivity().getSharedPreferences(Constants.myPreferences,
                Context.MODE_PRIVATE)
        val imageURL = pfs.getString(Constants.imageURL, "")!!
        if (imageURL == "" || imageURL == "default") {
            FirebaseFirestore
                    .getInstance()
                    .collection("Users")
                    .document(Constants.getUserId())
                    .get()
                    .addOnCompleteListener { task: Task<DocumentSnapshot?> ->
                        if (task.isSuccessful) {
                            if (task.result != null) {
                                if (task.result!!.exists()) {
                                    val user = task.result!!.toObject(User::class.java)
                                    if (user != null) {
                                        userName?.let {
                                            it.text = user.username
                                        }
                                        if (user.imageURL == "default") {
                                            profileImage?.let {
                                                it.setImageResource(R.drawable.profile)
                                            }

                                        } else {
                                            val defaultImageResId = R.drawable.profile
                                            Picasso.with(context)
                                                    .load(user.imageURL)
                                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                                    .into(profileImage, object : Callback {
                                                        override fun onSuccess() {}
                                                        override fun onError() {
                                                            Picasso.with(activity)
                                                                    .load(user.imageURL)
                                                                    .error(defaultImageResId)
                                                                    .into(profileImage)
                                                        }
                                                    })
                                        }
                                    }
                                }
                            }
                        } else {
                            if (task.exception != null) {
                                Timber.d("error %s", task.exception.toString())
                            }
                        }
                    }
        } else {
            Picasso.with(activity)
                    .load(imageURL)
                    .error(R.drawable.profile)
                    .into(profileImage)
        }


        return view
    }

    private fun checkCurrentUserFollowers() {
        val dRef : DatabaseReference = FirebaseDatabase
                .getInstance()
                .reference
        val followersRef = dRef
                .child("Follow")
                .child(Constants.getUserId())
                .child("Followers")
        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val followersCount = snapshot.childrenCount.toString()
                    val sharedPreferences = requireContext()
                            .getSharedPreferences(Constants.myPreferences,
                                    Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString(Constants.followerCount, followersCount)
                    editor.apply()
                }
            }
        })
    }

    private fun checkCurrentUserFollowings() {
        val dRef : DatabaseReference = FirebaseDatabase
                .getInstance()
                .reference
        val followingRef = dRef
                .child("Follow")
                .child(Constants.getUserId())
                .child("Following")
        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val followingCount = snapshot.childrenCount.toString()
                    val sharedPreferences = requireContext()
                            .getSharedPreferences(Constants.myPreferences,
                                    Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString(Constants.followingCount, followingCount)
                    editor.apply()
                }
            }
        })
    }

    private fun getTotalFollowings() {
        val followingRef = profileId?.let {
            FirebaseDatabase
                    .getInstance()
                    .reference
                    .child("Follow")
                    .child(it)
                    .child("Following")
        }

        followingRef?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (profileId?.let { snapshot.child(it).exists() }!!) {
                    following?.text = snapshot.childrenCount.toString()
                }
            }

        })
    }

    private fun getTotalFollowers() {
        val followerRef = profileId?.let {
            FirebaseDatabase
                    .getInstance()
                    .reference
                    .child("Follow")
                    .child(it)
                    .child("Follower")
        }

        followerRef?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (profileId?.let { snapshot.child(it).exists() }!!) {
                    followers?.text = snapshot.childrenCount.toString()
                }
            }
        })
    }

    private fun openImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, IMAGE_REQUEST)
    }

    private fun getFileExtension(uri: Uri): String? {
        val contentResolver = requireContext().contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    private fun uploadImage() {
        val pDialog = ProgressDialog(context)
        pDialog.setMessage("Uploading image...")
        pDialog.show()
        if (imageUri != null) {
            val sReference = storageReference!!.child(System.currentTimeMillis()
                    .toString() + "." + getFileExtension(imageUri!!))
            uploadTask = sReference.putFile(imageUri!!)
            (uploadTask as UploadTask)
                    .continueWithTask(Continuation { task: Task<UploadTask.TaskSnapshot?> ->
                        if (!task.isSuccessful) {
                            throw Objects.requireNonNull(task.exception)!!
                        }
                        sReference.downloadUrl
                    }).addOnCompleteListener { task: Task<Uri?> ->
                        if (task.isSuccessful) {
                            val downloadUri = task.result!!
                            val mUri = downloadUri.toString()
                            val pfs = Objects.requireNonNull(app)
                                    ?.applicationContext
                                    ?.getSharedPreferences(Constants.myPreferences
                                            , Context.MODE_PRIVATE)
                            val editor = pfs?.edit()
                            editor?.putString(Constants.imageURL, mUri)
                            editor?.apply()
                            FirebaseFirestore
                                    .getInstance()
                                    .collection("Users")
                                    .document( /*firebaseUser.getUid()*/Constants.getUserId())
                                    .update("imageURL", mUri)
                            pDialog.dismiss()
                        } else {
                            Toast.makeText(context, "Upload Fail", Toast.LENGTH_SHORT).show()
                            pDialog.dismiss()
                        }
                    }.addOnFailureListener { e: Exception ->
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                        pDialog.dismiss()
                    }
        } else {
            Toast.makeText(context, "Please select image!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            if (uploadTask != null && uploadTask!!.isInProgress) {
                Toast.makeText(
                        context,
                        "Upload is in progress...",
                        Toast.LENGTH_SHORT).show()
            } else {
                uploadImage()
            }
        }
    }

    companion object {
        const val IMAGE_REQUEST = 1
    }
}