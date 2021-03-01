package com.wadektech.mtihani.utils;

import com.wadektech.mtihani.pojo.SinglePDF;
import com.wadektech.mtihani.viewmodelfactories.MessagesActivityViewModelFactory;

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
