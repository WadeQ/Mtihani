package com.wadektech.mtihanirevise.ui;

import android.os.Bundle;
import android.text.Html;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.adapter.SliderAdapter;

public class MainSliderActivity extends AppCompatActivity {
    public ViewPager mSlideViewPager ;
    private LinearLayout mDotLayout ;
    public TextView[] mDot ;
    public SliderAdapter sliderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main_slider);

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

