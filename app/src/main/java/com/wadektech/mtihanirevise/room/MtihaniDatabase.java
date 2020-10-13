package com.wadektech.mtihanirevise.room;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database (entities = {Chat.class}, version = 3, exportSchema = false)
public abstract class MtihaniDatabase extends RoomDatabase {

    private static final Object LOCK = new Object ();

    private static volatile MtihaniDatabase instance;

    public abstract ChatDao chatDao();

    // create another singleton
    // synchronized here means that only one thread can access this method at a time..
    public static synchronized MtihaniDatabase getInstance(Context ctx) {
        if (instance == null) {
            synchronized (LOCK) {
                instance = Room.databaseBuilder (ctx.getApplicationContext (),
                        MtihaniDatabase.class,
                        "mtihaniDb")
                        .fallbackToDestructiveMigration ()
                        .build ();
            }

        }
        return instance;
    }

}