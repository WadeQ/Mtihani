package com.wadektech.mtihanirevise.ui

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.wadektech.mtihanirevise.R
import com.wadektech.mtihanirevise.database.MtihaniDatabase
import com.wadektech.mtihanirevise.fragments.ChatsFragment
import com.wadektech.mtihanirevise.fragments.ProfileFragment
import com.wadektech.mtihanirevise.fragments.UsersFragment
import com.wadektech.mtihanirevise.utils.Constants
import com.wadektech.mtihanirevise.viewmodels.ChatActivityViewModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {
    private var mUsername: TextView? = null
    var mStatus: TextView? = null
    // FirebaseUser firebaseUser;
    var rootRef: DatabaseReference? = null
    var mTabLayout: TabLayout? = null
    var mViewPager: ViewPager? = null
    private var viewPagerAdapter: ViewPagerAdapter? = null
    private var mHandler: Handler? = null
    var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        mUsername = findViewById(R.id.tv_username)
        mUsername?.text = Constants.getUserName()
        mHandler = Handler()
        rootRef = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        mTabLayout = findViewById(R.id.main_tabs)
        mViewPager = findViewById(R.id.main_tabPager)
        mTabLayout?.tabTextColors = ColorStateList
                .valueOf(resources.getColor(R.color.colorWhite))

        val viewModel = ViewModelProviders.of(this)
                .get(ChatActivityViewModel::class.java)
        viewModel.downloadUsers()

    }

    override fun onStart() {
        super.onStart()
        updateUserStatus("online")
        unreadCountFromRoom
    }

    /**
     * List of chats has been received
     * make necessary changes to the titles
     * @param
     */
    class ViewPagerAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm!!) {
        private val fragments: ArrayList<Fragment> = ArrayList()
        private val titles: ArrayList<String> = ArrayList()
        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }
    }

    override fun onResume() {
        super.onResume()
        updateUserStatus("online")
    }

    override fun onStop() {
        super.onStop()
        updateUserStatus("offline")
    }

    override fun onPause() {
        super.onPause()
        updateUserStatus("offline")
    }

    override fun onDestroy() {
        super.onDestroy()
        updateUserStatus("offline")
    }

    //Update the value background thread to UI thread
    //New thread to perform background operation
    private val unreadCountFromRoom: Unit
        get() {
//      New thread to perform background operation
            Thread {
                val unreadCount = MtihaniDatabase
                        .getInstance(this@ChatActivity)
                        .singleMessageDao()
                        .getUnreadCount(Constants.getUserId(), false)

//                  Update the value background thread to UI thread
                mHandler!!.post {
                    if (unreadCount == 0) {
                        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
                        viewPagerAdapter!!.addFragment(ChatsFragment(),
                                "Chats")
                        viewPagerAdapter!!.addFragment(UsersFragment(),
                                "Classroom")
                        viewPagerAdapter!!.addFragment(ProfileFragment(),
                                "Profile")
                        mViewPager!!.adapter = viewPagerAdapter
                        mTabLayout!!.setupWithViewPager(mViewPager)
                    } else {
                        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
                        viewPagerAdapter!!.addFragment(ChatsFragment(),
                                "($unreadCount) Chats")
                        viewPagerAdapter!!.addFragment(UsersFragment(),
                                "Classroom")
                        viewPagerAdapter!!.addFragment(ProfileFragment(),
                                "Profile")
                        mViewPager!!.adapter = viewPagerAdapter
                        mTabLayout!!.setupWithViewPager(mViewPager)
                    }
                }
            }.start()
        }

    @SuppressLint("SimpleDateFormat")
    private fun updateUserStatus(status: String) {
        val saveCurrentTime: String
        val calendar = Calendar.getInstance()
        val currentTime = SimpleDateFormat("hh:mm a")
        saveCurrentTime = currentTime.format(calendar.time)
        val saveCurrentDate : Long = System.currentTimeMillis()
        val hashMap = HashMap<String, Any>()
        hashMap["time"] = saveCurrentTime
        hashMap["date"] = saveCurrentDate
        hashMap["status"] = status
        FirebaseFirestore
                .getInstance()
                .collection("Users")
                .document(Constants.getUserId())
                .update(hashMap)
    }
}