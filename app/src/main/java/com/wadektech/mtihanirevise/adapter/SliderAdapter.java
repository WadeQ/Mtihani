package com.wadektech.mtihanirevise.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.auth.LoginActivity;
import com.wadektech.mtihanirevise.ui.MainSliderActivity;
import com.wadektech.mtihanirevise.ui.PastPapersActivity;
import com.wadektech.mtihanirevise.utils.Constants;

public class SliderAdapter extends PagerAdapter {
    Context context;
    LayoutInflater layoutInflater;
    FirebaseAuth mAuth = FirebaseAuth.getInstance ();

    public SliderAdapter(Context context) {
        this.context = context ;
    }
    //creating arrays for our slider view
    private int[] slide_images = {
            R.drawable.timely_slider,
            R.drawable.offline_slider,
            R.drawable.chat_slider
    };

    @Override
    public int getCount() {
        return slide_images.length;
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
        slideImageView.setImageResource(slide_images[position]);

       view.setOnClickListener(v -> {
           if (mAuth.getCurrentUser() != null){
               if (!Constants.getUserName ().equals ("")
                       && !Constants.getUserId ().equals ("")
                       && !Constants.getEmail ().equals ("")
                       && !Constants.getImageURL ().equals ("")) {

                   Intent intent = new Intent (context, PastPapersActivity.class);
                   context.startActivity(intent);
               }else {
                   Intent intent = new Intent (context, LoginActivity.class);
                   context.startActivity(intent);
               }
           }else {
               Intent intent = new Intent (context, LoginActivity.class);
               context.startActivity(intent);
           }
       });
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ConstraintLayout) object);
    }
}
