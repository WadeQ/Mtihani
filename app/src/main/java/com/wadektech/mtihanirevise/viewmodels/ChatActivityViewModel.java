package com.wadektech.mtihanirevise.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.wadektech.mtihanirevise.persistence.MtihaniRevise;
import com.wadektech.mtihanirevise.repository.MtihaniRepository;
import com.wadektech.mtihanirevise.room.Chat;
import com.wadektech.mtihanirevise.room.User;
import com.wadektech.mtihanirevise.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class ChatActivityViewModel extends ViewModel {
    private MutableLiveData<User> currentUser;
    private MutableLiveData<List<Chat>> unreadList;
    private MutableLiveData<String> emptyUnreadList;
    private MutableLiveData<String> returningUser;

    public ChatActivityViewModel() {
        currentUser = new MutableLiveData<> ();
        unreadList= new MutableLiveData<> ();
        emptyUnreadList= new MutableLiveData<> ();
        returningUser = new MutableLiveData<> ();
    }

    public void loadUserFromFirestore(String userId) {
        FirebaseFirestore
                .getInstance()
                .collection("Users")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            if (!task.getResult().isEmpty()) {
                                currentUser.setValue(task.getResult().toObjects(User.class).get(0));
                            }
                        }
                    } else {
                        if (task.getException() != null) {
                            Log.d("ChatActivityViewModel", "error is:" + task.getException().toString());
                        }
                    }
                });

    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }
    public void loadUserUnreadChats(String userId) {
        FirebaseFirestore
                .getInstance()
                .collection("messages")
                .whereEqualTo("receiver", userId)
                .whereEqualTo("seen",false)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            if (!task.getResult().isEmpty()) {
                                unreadList.setValue(task.getResult().toObjects(Chat.class));
                                List<Chat> chatList = new ArrayList<>();
                                for(DocumentSnapshot document:task.getResult().getDocuments()){
                                    Chat chat = document.toObject(Chat.class);
                                    if(chat != null){
                                        chat.setDocumentId(document.getId());
                                        chatList.add(chat);
                                    }
                                }
                                MtihaniRepository.saveNewMessages(chatList);
                            }else{
                                Timber.d("unread list is empty");

                                emptyUnreadList.setValue("empty!");
                            }
                        }
                    } else {
                        if (task.getException() != null) {
                            Timber.d("error is:%s", task.getException().toString());
                        }
                    }
                });

    }

    public LiveData<String> getEmptyUnreadList() {
        return emptyUnreadList;
    }

    public LiveData<List<Chat>> getUnreadList() {
        return unreadList;
    }

    public LiveData<String> getReturningUser() {
        return returningUser;
    }

    public void signInReturningUser(String email){
        FirebaseFirestore
                .getInstance()
                .collection("Users")
                .whereEqualTo("email",email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            if (!task.getResult().isEmpty()) {
                            User user = task.getResult().toObjects(User.class).get(0);
                            if(user != null){
                                SharedPreferences pfs = Objects.requireNonNull(MtihaniRevise
                                        .Companion
                                        .getApp())
                                        .getApplicationContext()
                                        .getSharedPreferences(Constants.myPreferences,Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = pfs.edit();
                                editor.putString(Constants.userName,user.getUsername());
                                editor.putString(Constants.email,user.getEmail());
                                editor.putString(Constants.userId,user.getUserId());
                                editor.putString(Constants.imageURL,user.getImageURL());
                                editor.apply();
                                Timber.d("userId is" + user.getUserId()
                                        + " username is: " + user.getUsername() + " email is: " + user.getEmail()
                                        + " imageURL is " + user.getImageURL());
                                returningUser.setValue("success");
                            }else{
                                returningUser.setValue("fail");
                            }
                            }else{
                                returningUser.setValue("fail");
                            }
                        }
                    } else {
                        returningUser.setValue("error");
                        if (task.getException() != null) {
                            Timber.d("error is:%s", task.getException().toString());
                        }
                    }
                });
    }

    public void downloadUsers() {
        MtihaniRepository.downloadUsers();
    }
}
