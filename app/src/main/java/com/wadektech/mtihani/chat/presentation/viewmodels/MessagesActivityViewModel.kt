package com.wadektech.mtihani.chat.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.wadektech.mtihani.chat.data.localDatasource.room.Chat
import com.wadektech.mtihani.chat.data.repository.ChatsRepositoryImpl



class MessagesActivityViewModel(private val myId: String, private val userId: String) :
    ViewModel() {
    val messagesList: LiveData<PagedList<Chat>>
    private val chatsRepositoryImpl = ChatsRepositoryImpl()

    init {
        messagesList = chatsRepositoryImpl.loadMessagesFromRoom(myId, userId)
    }

    fun saveMessage(chat: Chat?) {
        chatsRepositoryImpl.saveMessage(chat)
    }

    fun saveNewMessages(chats: List<Chat?>?) {
        chatsRepositoryImpl.saveNewMessages(chats)
    }

    fun sendMessageToFirebase(chat: Chat?) {
        chatsRepositoryImpl.sendMessageToFirebase(chat)
    }
}