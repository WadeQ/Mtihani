package com.wadektech.mtihanirevise.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChatsToDatabase(List<Chat> chats);

    // LiveData updates the list automatically if changes are observed
    @Query("SELECT * FROM chat_messages")
    android.arch.paging.DataSource.Factory<Integer, Chat> getAllChats();
}
