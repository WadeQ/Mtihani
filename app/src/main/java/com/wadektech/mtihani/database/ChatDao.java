package com.wadektech.mtihani.database;

import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.wadektech.mtihani.room.ChatItem;

import java.util.List;

@Dao
public interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addUser(ChatItem user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long updateUser(ChatItem user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveUsersList(List<ChatItem> list);

    @Update
    void update(ChatItem user);

    @Delete
    void deleteSingleUser(ChatItem user);


    @Query("SELECT * FROM chatList  ORDER BY date DESC")
    DataSource.Factory<Integer, ChatItem> getAllChatUsers();

    @Query("DELETE FROM chatList")
    void deleteChatList();

}
