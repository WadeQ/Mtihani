package com.wadektech.mtihanirevise.utils;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.pojo.SinglePDF;
import com.wadektech.mtihanirevise.viewmodelfactories.MessagesActivityViewModelFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InjectorUtils {
    public static SingleLiveEvent<String> provideSingleLiveEvent() {
        return new SingleLiveEvent<>();
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
    public static MessagesActivityViewModelFactory provideMessagesViewModelFactory(String myId, String userId){
        return new MessagesActivityViewModelFactory (myId,userId);
    }
}
