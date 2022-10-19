package com.wadektech.mtihani.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChatsToDatabase(List<Chat> chats);

    // LiveData updates the list automatically if changes are observed
    @Query("SELECT * FROM messages ORDER BY date")
    androidx.paging.DataSource.Factory<Integer, Chat> getAllChats();
}
