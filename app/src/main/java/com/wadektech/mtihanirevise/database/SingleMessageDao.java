package com.wadektech.mtihanirevise.database;

import android.arch.paging.DataSource;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.wadektech.mtihanirevise.room.Chat;

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
