package com.wadektech.mtihanirevise.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.wadektech.mtihanirevise.room.Chat;
import com.wadektech.mtihanirevise.room.ChatItem;
import com.wadektech.mtihanirevise.room.User;


@Database(entities = {Chat.class,User.class, ChatItem.class}, version = 3, exportSchema = false)
public abstract class MtihaniDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "mtihani";
    private static final Object LOCK = new Object();
    private static volatile MtihaniDatabase sInstance;

    public static MtihaniDatabase getInstance(Context context) {
        if (sInstance == null) {

            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(context.getApplicationContext(),
                            MtihaniDatabase.class,
                            MtihaniDatabase.DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }

            }
        }
        return sInstance;
    }

    public abstract SingleMessageDao singleMessageDao();
    public abstract UsersDao usersDao();
    public abstract ChatDao chatDao();

}
