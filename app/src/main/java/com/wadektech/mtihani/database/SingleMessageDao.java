package com.wadektech.mtihani.database;

import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.wadektech.mtihani.room.Chat;

import java.util.List;

@Dao
public interface SingleMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void add(Chat message);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long updateMessage(Chat message);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveChatsList(List<Chat> list);

    @Update
    void update(Chat message);

    @Delete
    void deleteSingleMessage(Chat message);

    @Query("SELECT COUNT(id) FROM messages WHERE receiver =:userId AND seen =:status")
    int getUnreadCount(String userId, boolean status);

    @Query("SELECT * FROM messages WHERE (sender =:myId AND receiver =:userId) OR (sender =:userId AND receiver =:myId) ORDER BY date ASC")
    DataSource.Factory<Integer, Chat> getChatMessages(String myId, String userId);

    @Query("SELECT message FROM messages WHERE (sender =:myId AND receiver =:userId) OR (sender =:userId AND receiver =:myId) ORDER BY date DESC LIMIT 1")
    String getLastMessage(String myId, String userId);

    @Query("DELETE FROM messages")
    void deleteMessages();

}
