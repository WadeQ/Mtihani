package com.wadektech.mtihanirevise;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wadektech.mtihanirevise.Fragments.ChatsFragment;
import com.wadektech.mtihanirevise.Fragments.ProfileFragment;
import com.wadektech.mtihanirevise.Fragments.UsersFragment;
import com.wadektech.mtihanirevise.POJO.User;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {
      TextView mUsername ;
      FirebaseUser firebaseUser ;
      DatabaseReference reference ;
      TabLayout mTabLayout ;
      ViewPager mViewPager ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mUsername = findViewById(R.id.tv_username);
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    mUsername.setText(user.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
             mTabLayout = findViewById(R.id.main_tabs);
             mViewPager = findViewById(R.id.main_tabPager);

             ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
             viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
             viewPagerAdapter.addFragment(new UsersFragment(), "Classroom");
             viewPagerAdapter.addFragment(new ProfileFragment(), "Profile");

             mViewPager.setAdapter(viewPagerAdapter);
             mTabLayout.setupWithViewPager(mViewPager);
    }
    public class ViewPagerAdapter extends FragmentPagerAdapter{
           private ArrayList<Fragment> fragments ;
           private ArrayList<String> titles ;

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
        public void addFragment(Fragment fragment, String title){
            fragments.add(fragment);
            titles.add(title);
    }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }
}

