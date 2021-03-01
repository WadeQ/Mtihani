package com.wadektech.mtihani.room;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;


@Entity(tableName = "chatList", indices = {@Index(value = {"userId"}, unique = true)})
public class ChatItem implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String userId;
    private String username ;
    private String imageURL ;
    private String status ;
    private String search ;
    private String update ;
    private String time ;
    private long date ;

    @Ignore
    public ChatItem(String userId, String username, String imageURL, String status, String search,
                String update, String time , long date) {
        this.userId = userId;
        this.username = username;
        this.imageURL = imageURL;
        this.status = status;
        this.search = search ;
        this.update = update ;
        this.time = time ;
        this.date = date;
    }

    public ChatItem(long id, String userId, String username, String imageURL, String status,
                String search, String update, String time, long date) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.imageURL = imageURL;
        this.status = status;
        this.search = search;
        this.update = update;
        this.time = time;
        this.date = date;
    }

    @Ignore
    public ChatItem() {
        //Empty constructor is a must for firestore to work!
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
    public static DiffUtil.ItemCallback<ChatItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<ChatItem>() {
        @Override
        public boolean areItemsTheSame(ChatItem oldItem, ChatItem newItem) {
            return oldItem.userId.equals(newItem.userId)
                    && oldItem.date == newItem.getDate()
                    && oldItem.imageURL.equals(newItem.getImageURL());

        }

        @Override
        public boolean areContentsTheSame(ChatItem oldItem, @NonNull ChatItem newItem) {
            return oldItem.equals(newItem);
        }


    };

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChatItem mUser = (ChatItem) obj;
        return userId.equals(mUser.userId) &&
                date== mUser.date && username.equals(mUser.username);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.userId);
        dest.writeString(this.username);
        dest.writeString(this.imageURL);
        dest.writeString(this.status);
        dest.writeString(this.search);
        dest.writeString(this.update);
        dest.writeString(this.time);
        dest.writeLong(this.date);
    }

    protected ChatItem(Parcel in) {
        this.id = in.readLong();
        this.userId = in.readString();
        this.username = in.readString();
        this.imageURL = in.readString();
        this.status = in.readString();
        this.search = in.readString();
        this.update = in.readString();
        this.time = in.readString();
        this.date = in.readLong();
    }

    public static final Creator<ChatItem> CREATOR = new Creator<ChatItem>() {
        @Override
        public ChatItem createFromParcel(Parcel source) {
            return new ChatItem (source);
        }

        @Override
        public ChatItem[] newArray(int size) {
            return new ChatItem[size];
        }
    };
}
