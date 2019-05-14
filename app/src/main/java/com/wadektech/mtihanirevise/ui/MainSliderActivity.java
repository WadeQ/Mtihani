package com.wadektech.mtihanirevise.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.adapter.SliderAdapter;
import com.wadektech.mtihanirevise.auth.LoginActivity;
import com.wadektech.mtihanirevise.utils.Constants;

public class MainSliderActivity extends AppCompatActivity {

    public ViewPager mSlideViewPager ;
    private LinearLayout mDotLayout ;
    public TextView[] mDot ;
    public SliderAdapter sliderAdapter;
    public Button slideViewButton;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main_slider);


        mAuth = FirebaseAuth.getInstance ();

        slideViewButton = findViewById (R.id.slideViewButton);
        slideViewButton.setOnClickListener (v -> {
            if (mAuth.getCurrentUser () != null) {
                if (!Constants.getUserName ().equals ("")
                        && !Constants.getUserId ().equals ("")
                        && !Constants.getEmail ().equals ("")
                        && !Constants.getImageURL ().equals ("")) {

                    Intent intent = new Intent (MainSliderActivity.this, PastPapersActivity.class);
                    finish ();
                    startActivity (intent);

                } else {
                    //Toast.makeText (this, "slider is the culprit!", Toast.LENGTH_SHORT).show ();
                    Intent intent = new Intent (MainSliderActivity.this, LoginActivity.class);
                    finish ();
                    startActivity (intent);
                }

            } else {
                Intent intent = new Intent (MainSliderActivity.this, LoginActivity.class);
                finish ();
                startActivity (intent);
            }
        });

        mSlideViewPager = findViewById (R.id.slideViewPager);
        mDotLayout = findViewById (R.id.dotsLinearLayout);

        sliderAdapter = new SliderAdapter (this);
        mSlideViewPager.setAdapter (sliderAdapter);

        addDotsIndicator (0);
        mSlideViewPager.addOnPageChangeListener (viewListener);

    }

        public void addDotsIndicator ( int position) {
            mDot = new TextView[3];
            mDotLayout.removeAllViews ();

            for (int i = 0; i < mDot.length; i++) {
                mDot[i] = new TextView (this);
                mDot[i].setText (Html.fromHtml ("&#8226;"));
                mDot[i].setTextSize (45);
                mDot[i].setTextColor (getResources ().getColor (R.color.colorTransparent));

                mDotLayout.addView (mDot[i]);

            }

            if (mDot.length > 0) {
                mDot[position].setTextColor (getResources ().getColor (R.color.colorMain));
            }
        }
                ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener () {
                    @Override
                    public void onPageScrolled(int i, float v, int i1) {

                    }

                    @Override
                    public void onPageSelected(int i) {
                        addDotsIndicator (i);
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                };
}

