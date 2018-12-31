package com.wadektech.mtihanirevise.utils;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.pojo.SinglePDF;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InjectorUtils {
    public static SingleLiveEvent<String> provideSingleLiveEvent() {
        return new SingleLiveEvent<>();
    }
    public static ArrayAdapter<String> provideCategoryAdapter(Context context, String[] input) {
        return new ArrayAdapter<>(context, R.layout.spinner_item, R.id.txt, input);
    }

    public static Map<String,Object> provideStringHashMap() {
        return new HashMap<>();
    }

    public static SingleLiveEvent<List<SinglePDF>> provideListSingleLiveEvent() {
        return new SingleLiveEvent<>();
    }

    public static SingleLiveEvent<Integer> provideIntSingleLiveEvent() {
        return new SingleLiveEvent<>();
    }
}
