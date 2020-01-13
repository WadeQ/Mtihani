package com.wadektech.mtihanirevise.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wadektech.mtihanirevise.R;

public class SliderAdapter extends PagerAdapter {
    Context context;
    LayoutInflater layoutInflater;

    public SliderAdapter(Context context) {
        this.context = context ;
    }
    //creating arrays for our slider view
    private int[] slide_images = {
            R.drawable.knecgirl,
            R.drawable.starehe,
            R.drawable.chatgirl
    };
    private String[] slide_headings = {
            "Timely Revision",
            "Offline Accessibility",
            "Live Chat"
    };
    private String[] slide_desc = {
            "Access all KCSE past exam papers from 2001 to 2018 at the comfort of your phone.Mtihani revise leverages accessibility with convenience to easen revision. ",
            "Retrieve, read and store your KCSE papers for future access without reusing data! Sounds like the real deal huh? Enjoy Mtihani Revise on the go!",
            "Enjoy realtime chatting with your fellow students and discuss, tackle questions, discover potentials and together scale the heights of academic excellence"
    };
    @Override
    public int getCount() {
        return slide_headings.length;
    }
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View view = layoutInflater.inflate(R.layout.activity_slider, container, false);


        ImageView slideImageView = view.findViewById(R.id.slide_image);
        TextView slideHeading = view.findViewById(R.id.slide_heading);
        TextView slideDescription = view.findViewById(R.id.slide_desc);


        slideImageView.setImageResource(slide_images[position]);
        slideHeading.setText(slide_headings[position]);
        slideDescription.setText(slide_desc[position]);

        container.addView(view);
        return view;

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ConstraintLayout) object);
    }
}
