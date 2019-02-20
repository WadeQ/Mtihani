package com.wadektech.mtihanirevise.room;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.v7.util.DiffUtil;

@Entity(tableName ="chat_messages" )
public class Chat {
    @PrimaryKey(autoGenerate = true)
    private int id ;
    private String sender ;
    private String receiver ;
    private String message ;
    private boolean isseen ;
    private long date ;

    @Ignore
    public Chat() {
        //Empty constructor required by firebase and will be ignored by room
    }
    @Ignore
    public Chat(String sender, String receiver, String message, boolean isseen, long date) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen = isseen;
        this.date = date;
    }

    public Chat(int id, String sender, String receiver, String message , boolean isseen, long date) {
        this.id = id ;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen = isseen ;
        this.date = date ;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static DiffUtil.ItemCallback<Chat> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Chat> (){
                // Concert details may have changed if reloaded from the database,
                // but ID is fixed.
                @Override
                public boolean areItemsTheSame(Chat oldChats, Chat newChats) {
                    return oldChats.message.equals (newChats.message)  ;
                }

                @Override
                public boolean areContentsTheSame(Chat oldChats, Chat newChats) {
                    return oldChats.equals(newChats);
                }
            };
}
