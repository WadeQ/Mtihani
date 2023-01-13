package com.wadektech.mtihani.chat.data.localDatasource.room;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.recyclerview.widget.DiffUtil;

@Entity(tableName = "messages", indices = {@Index(value = {"date"}, unique = true)})
public class Chat {
    @PrimaryKey(autoGenerate = true)
    private int id ;
    private String sender ;
    private String receiver ;
    private String message ;
    private boolean seen ;
    private long date ;
    private String documentId;

    @Ignore
    public Chat() {
        //Empty constructor required by firebase and will be ignored by room
    }
    @Ignore
    public Chat(String sender, String receiver, String message, boolean seen, long date
    ,String documentId) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.seen = seen;
        this.date = date;
        this.documentId=documentId;
    }

    public Chat(int id, String sender, String receiver, String message , boolean seen, long date
    ,String documentId) {
        this.id = id ;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.seen = seen ;
        this.date = date ;
        this.documentId= documentId;
    }


    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
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

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public boolean getSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static DiffUtil.ItemCallback<Chat> DIFF_CALLBACK = new DiffUtil.ItemCallback<Chat>() {
        @Override
        public boolean areItemsTheSame(Chat oldItem, Chat newItem) {
            return oldItem.getMessage().equals(newItem.getMessage())
                    && oldItem.getDate()== newItem.getDate()
                    && oldItem.documentId.equals(newItem.documentId);

        }

        @Override
        public boolean areContentsTheSame(Chat oldItem, Chat newItem) {
            return oldItem.equals(newItem);
        }


    };

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Chat chat = (Chat) obj;
        return message.equals(chat.message) &&
                date== chat.date
                && sender.equals(chat.sender);
    }

    @Override
    public int hashCode() {
        int hash = 17;
        return hash*String.valueOf(date).hashCode()*message.hashCode()*sender.hashCode();
    }
}
