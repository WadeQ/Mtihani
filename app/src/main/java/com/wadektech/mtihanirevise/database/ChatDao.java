package com.wadektech.mtihanirevise.database;

import android.arch.paging.DataSource;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.wadektech.mtihanirevise.room.ChatItem;

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
