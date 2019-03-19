package com.wadektech.mtihanirevise.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import com.wadektech.mtihanirevise.room.User;

import java.util.List;

public class UsersViewModel extends ViewModel {
    public MutableLiveData<List<User>> users ;
    public LiveData<List<User>> getAllUsers() {
        return users ;

    }
}
