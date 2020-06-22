package com.wadektech.mtihanirevise.room;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.wadektech.mtihanirevise.persistence.MtihaniRevise;

import java.util.List;


public class ChatViewModel extends ViewModel {

    private MtihaniDatabase mtihaniDatabase;

    public final LiveData<PagedList<Chat>> chats ;

    public ChatViewModel(){
        mtihaniDatabase = MtihaniDatabase.getInstance (MtihaniRevise.Companion.getApp ().getApplicationContext ());
        chats = new LivePagedListBuilder<> (mtihaniDatabase.chatDao ().getAllChats (), /* page size */ 20)
                .build ();

    }

    public void saveChatsToRoom(List<Chat> chats){
        mtihaniDatabase = MtihaniDatabase.getInstance (MtihaniRevise.Companion.getApp ().getApplicationContext ());
        mtihaniDatabase.chatDao ().insertChatsToDatabase (chats);
    }

}
