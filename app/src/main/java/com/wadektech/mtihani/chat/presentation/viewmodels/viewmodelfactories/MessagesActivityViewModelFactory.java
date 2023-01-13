package com.wadektech.mtihani.chat.presentation.viewmodels.viewmodelfactories;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

import com.wadektech.mtihani.chat.presentation.viewmodels.MessagesActivityViewModel;

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
