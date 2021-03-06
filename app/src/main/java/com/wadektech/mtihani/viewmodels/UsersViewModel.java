package com.wadektech.mtihani.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

import com.wadektech.mtihani.repository.MtihaniRepository;
import com.wadektech.mtihani.room.ChatItem;
import com.wadektech.mtihani.room.User;

public class UsersViewModel extends ViewModel {
    private final LiveData<PagedList<User>> usersList;
    private final LiveData<PagedList<ChatItem>> chatList;
   String singleUserEmail;

    public UsersViewModel() {
       usersList=MtihaniRepository.loadUsersFromRoom();
       chatList=MtihaniRepository.loadChatList();
    }

    public static void saveChatListUser(ChatItem user) {
        MtihaniRepository.saveChatListUser(user);
    }

    public LiveData<PagedList<User>> getUsersList() {
        return usersList;
    }
    public LiveData<PagedList<User>> getUsersByName(String filter) {
        return MtihaniRepository.searchUsersFromRoom(filter);
    }

    public LiveData<PagedList<ChatItem>> getChatList() {
        return chatList;
    }
    public String getCurrentUserEmail(){
        return singleUserEmail;
    }
}
