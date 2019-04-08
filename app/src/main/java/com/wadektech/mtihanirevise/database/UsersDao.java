package com.wadektech.mtihanirevise.database;

import android.arch.paging.DataSource;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.wadektech.mtihanirevise.room.User;

import java.util.List;

@Dao
public interface UsersDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addUser(User user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long updateUser(User user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveUsersList(List<User> list);

    @Update
    void update(User user);

    @Delete
    void deleteSingleUser(User user);

    @Query("SELECT COUNT(id) FROM users")
    int getTotalNumberOfUsers();


    @Query("SELECT * FROM users WHERE userId NOT LIKE :userId ORDER BY date DESC")
    DataSource.Factory<Integer, User> getAllUsers(String userId);

    @Query("SELECT * FROM users WHERE search LIKE :filter ORDER BY date DESC")
    DataSource.Factory<Integer, User> searchUsers(String filter);

    @Query("DELETE FROM users")
    void deleteUsersTable();
}
