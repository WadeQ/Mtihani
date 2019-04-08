package com.wadektech.mtihanirevise.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.PagedList;

import com.wadektech.mtihanirevise.repository.MtihaniRepository;
import com.wadektech.mtihanirevise.room.Chat;

import java.util.List;

public class MessagesActivityViewModel extends ViewModel {
    private String myId,userId;
    private LiveData<PagedList<Chat>> messagesList;

    public MessagesActivityViewModel(String myId, String userId) {
        this.myId = myId;
        this.userId = userId;
        messagesList= MtihaniRepository.loadMessagesFromRoom(myId,userId);
    }

    public LiveData<PagedList<Chat>> getMessagesList() {
        return messagesList;
    }


    public void saveMessage(Chat chat) {
        MtihaniRepository.saveMessage(chat);
    }

    public void saveNewMessages(List<Chat> chats) {
        //MVVM at work. only the repository should interact with database directly
        MtihaniRepository.saveNewMessages(chats);
    }

    public void sendMessageToFirebase(Chat chat) {
        MtihaniRepository.sendMessageToFirebase(chat);
    }
}
