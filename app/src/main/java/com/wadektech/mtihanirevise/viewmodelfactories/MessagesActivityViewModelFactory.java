package com.wadektech.mtihanirevise.viewmodelfactories;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.wadektech.mtihanirevise.viewmodels.MessagesActivityViewModel;

public class MessagesActivityViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final String myId,userId;

    public MessagesActivityViewModelFactory(String myId, String userId){
        this.myId=myId;
        this.userId = userId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass){
        //noinspection unchecked
        return(T)new MessagesActivityViewModel(myId,userId);
    }
}
