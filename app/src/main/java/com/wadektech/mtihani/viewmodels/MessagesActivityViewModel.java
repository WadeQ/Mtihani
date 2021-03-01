package com.wadektech.mtihani.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

import com.wadektech.mtihani.repository.MtihaniRepository;
import com.wadektech.mtihani.room.Chat;

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
        MtihaniRepository.saveNewMessages(chats);
    }

    public void sendMessageToFirebase(Chat chat) {
        MtihaniRepository.sendMessageToFirebase(chat);
    }
}
