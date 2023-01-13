package com.wadektech.mtihani.chat.data.localDatasource.room;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

@Entity(tableName = "users", indices = {@Index(value = {"userId"}, unique = true)})
public class User {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String userId;
    private String username ;
    private String imageURL ;
    private String status ;
    private String search ;
    private String update ;
    private String email;
    private String time ;
    private long date ;

    @Ignore
    public User(String userId, String username, String imageURL, String status, String search,
                String update, String time , long date, String email) {
        this.userId = userId;
        this.username = username;
        this.imageURL = imageURL;
        this.status = status;
        this.search = search ;
        this.update = update ;
        this.time = time ;
        this.date = date;
        this.email=email;
    }

    public User(long id, String userId, String username, String imageURL, String status,
                String search, String update, String time, long date,String email) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.imageURL = imageURL;
        this.status = status;
        this.search = search;
        this.update = update;
        this.time = time;
        this.date = date;
        this.email = email;
    }

    @Ignore
    public User() {
        //Don't delete. empty constructor is a must for firestore to work!
    }

    public long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
    public static DiffUtil.ItemCallback<User> DIFF_CALLBACK = new DiffUtil.ItemCallback<User>() {
        @Override
        public boolean areItemsTheSame(User oldItem, User newItem) {
            return oldItem.userId.equals(newItem.userId)
                    && oldItem.date == newItem.getDate()
                    && oldItem.imageURL.equals(newItem.getImageURL());
        }

        @Override
        public boolean areContentsTheSame(User oldItem, @NonNull User newItem) {
            return oldItem.equals(newItem);
        }
    };

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User mUser = (User) obj;
        return userId.equals(mUser.userId) &&
                date== mUser.date && username.equals(mUser.username);
    }
}
