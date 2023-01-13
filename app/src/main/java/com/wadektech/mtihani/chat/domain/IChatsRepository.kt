package com.wadektech.mtihani.chat.domain

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.wadektech.mtihani.chat.data.localDatasource.room.Chat
import com.wadektech.mtihani.chat.data.localDatasource.room.ChatItem
import com.wadektech.mtihani.chat.data.localDatasource.room.User

interface IChatsRepository {

    fun loadMessagesFromRoom(myId: String, userId: String): LiveData<PagedList<Chat>>

    fun downloadMessages(myId: String, userId: String)

    fun loadUsersFromRoom(): LiveData<PagedList<User>>

    fun searchUsersFromRoom(filter: String?): LiveData<PagedList<User>>

    fun saveMessage(chat: Chat?)

    fun saveNewMessages(chats: List<Chat?>?)

    fun sendMessageToFirebase(chat: Chat?)

    fun onZeroUsersLoaded()

    fun onUserAtFrontLoaded(itemAtFront: User)

    fun onUserAtEndLoaded(itemAtEnd: User)

    fun loadChatList(): LiveData<PagedList<ChatItem>>

    fun saveChatListUser(user: ChatItem?)

    fun updateMessage(chat: Chat)

    fun downloadUsers()

    fun onZeroItemsLoaded(myId: String?, userId: String?)

    fun onItemAtFrontLoaded(itemAtFront: Chat, myId: String?, userId: String?)

    fun onItemAtEndLoaded(itemAtEnd: Chat, myId: String?, userId: String?)
}