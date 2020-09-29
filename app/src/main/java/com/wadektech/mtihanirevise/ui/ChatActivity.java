package com.wadektech.mtihanirevise.ui;

import androidx.lifecycle.ViewModelProviders;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.database.MtihaniDatabase;
import com.wadektech.mtihanirevise.fragments.ChatsFragment;
import com.wadektech.mtihanirevise.fragments.ProfileFragment;
import com.wadektech.mtihanirevise.fragments.UsersFragment;
import com.wadektech.mtihanirevise.room.Chat;
import com.wadektech.mtihanirevise.room.User;
import com.wadektech.mtihanirevise.utils.Constants;
import com.wadektech.mtihanirevise.viewmodels.ChatActivityViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class ChatActivity extends AppCompatActivity {
    TextView mUsername, mStatus;
   // FirebaseUser firebaseUser;
    DatabaseReference rootRef;
    TabLayout mTabLayout;
    ViewPager mViewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private Handler mHandler;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mUsername = findViewById(R.id.tv_username);
        mUsername.setText(Constants.getUserName());
        mHandler = new Handler();

        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

      mTabLayout = findViewById(R.id.main_tabs);
        mViewPager = findViewById(R.id.main_tabPager);
        mTabLayout.setTabTextColors(ColorStateList.valueOf(getResources().getColor(R.color.colorWhite)));
        ChatActivityViewModel viewModel = ViewModelProviders.of(this).get(ChatActivityViewModel.class);
        viewModel.downloadUsers();

    }

    @Override
    protected void onStart() {
        super.onStart();
        updateStatus("online");
        getUnreadCountFromRoom();
    }

    /**
     * List of chats has been received
     * make necessary changes to the titles
     * @param
     */

    public static class ViewPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    public void updateStatus(String status) {
        String saveCurrentTime, saveCurrentDate ;
        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());
        HashMap<String, Object> statusMap = new HashMap<>();
        statusMap.put("time", saveCurrentTime);
        statusMap.put("date", saveCurrentDate);
        statusMap.put("state", status);

       String currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
       rootRef.child("Users").child(currentUserId).child("status").updateChildren(statusMap);

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus("online");
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateStatus("online");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateStatus("offline");
    }

    private void getUnreadCountFromRoom() {
//      New thread to perform background operation
        new Thread(() -> {

           final int  unreadCount = MtihaniDatabase
                   .getInstance(ChatActivity.this)
                   .singleMessageDao()
                   .getUnreadCount(Constants.getUserId(),false);


//                  Update the value background thread to UI thread
                mHandler.post(() -> {

                    if(unreadCount==0){
                        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
                        viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
                        viewPagerAdapter.addFragment(new UsersFragment (), "Classroom");
                        viewPagerAdapter.addFragment(new ProfileFragment (), "Profile");
                        mViewPager.setAdapter(viewPagerAdapter);
                        mTabLayout.setupWithViewPager(mViewPager);
                    }else{
                        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
                        viewPagerAdapter.addFragment(new ChatsFragment(), "("+unreadCount+") Chats");
                        viewPagerAdapter.addFragment(new UsersFragment (), "Classroom");
                        viewPagerAdapter.addFragment(new ProfileFragment (), "Profile");
                        mViewPager.setAdapter(viewPagerAdapter);
                        mTabLayout.setupWithViewPager(mViewPager);
                    }

                });

        }).start();
    }

}

