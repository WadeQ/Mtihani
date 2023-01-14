package com.wadektech.mtihani.chat.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.wadektech.mtihani.chat.data.localDatasource.room.ChatItem
import com.wadektech.mtihani.chat.data.localDatasource.room.User
import com.wadektech.mtihani.chat.data.repository.ChatsRepositoryImpl
import com.wadektech.mtihani.core.repository.MtihaniRepository


class UsersViewModel : ViewModel() {
    private val chatsRepositoryImpl = ChatsRepositoryImpl()
    val usersList: LiveData<PagedList<User>> = chatsRepositoryImpl.loadUsersFromRoom()
    val chatList: LiveData<PagedList<ChatItem>> = chatsRepositoryImpl.loadChatList()

    fun getUsersByName(filter: String?): LiveData<PagedList<User>> {
        return chatsRepositoryImpl.searchUsersFromRoom(filter)
    }

    companion object {
        private val chatsRepositoryImpl = ChatsRepositoryImpl()
        fun saveChatListUser(user: ChatItem?) {
            chatsRepositoryImpl.saveChatListUser(user)
        }
    }
}