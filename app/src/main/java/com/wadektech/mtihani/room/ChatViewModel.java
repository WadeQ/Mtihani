package com.wadektech.mtihani.room;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.wadektech.mtihani.persistence.MtihaniRevise;

import java.util.List;
import java.util.Objects;


public class ChatViewModel extends ViewModel {
    private MtihaniDatabase mtihaniDatabase;
    public final LiveData<PagedList<Chat>> chats ;

    public ChatViewModel(){
        mtihaniDatabase = MtihaniDatabase
                .getInstance (Objects.requireNonNull(MtihaniRevise.Companion
                        .getApp())
                        .getApplicationContext ());
        chats = new LivePagedListBuilder<> (mtihaniDatabase
                .chatDao ()
                .getAllChats (), 20)
                .build ();

    }

    public void saveChatsToRoom(List<Chat> chats){
        mtihaniDatabase = MtihaniDatabase
                .getInstance (Objects.requireNonNull(MtihaniRevise.Companion
                        .getApp())
                        .getApplicationContext ());
        mtihaniDatabase
                .chatDao ()
                .insertChatsToDatabase (chats);
    }
}
