package com.wadektech.mtihani.adapter;


import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.wadektech.mtihani.ui.MainSliderActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainSliderActivityTest {

    @Rule
    public ActivityTestRule<MainSliderActivity> mActivityTestRule = new ActivityTestRule<>(MainSliderActivity.class);

    @Test
    public void mainSliderActivityTest() {
    }

}
