package com.wadektech.mtihani.chat.data.firebaseDataSource

import androidx.paging.PagedList.BoundaryCallback
import com.wadektech.mtihani.chat.data.localDatasource.room.Chat
import com.wadektech.mtihani.chat.data.repository.ChatsRepositoryImpl
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class MessagesBoundaryCallback(private val myId: String, private val userId: String) :
    BoundaryCallback<Chat>() {
    private val executor: Executor = Executors.newSingleThreadExecutor()
    private val helper = PagingRequestHelper(executor)
    private val chatsRepositoryImpl = ChatsRepositoryImpl()
    override fun onZeroItemsLoaded() {
        //database has no items. load items from server
        helper.runIfNotRunning(PagingRequestHelper.RequestType.BEFORE) {
            chatsRepositoryImpl.onZeroItemsLoaded(
                myId, userId
            )
        }
    }

    /**
     * Room database has loaded the first item.
     * we check if the server has any newer items and download them
     */
    override fun onItemAtFrontLoaded(itemAtFront: Chat) {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            chatsRepositoryImpl.onItemAtFrontLoaded(
                itemAtFront,
                myId,
                userId
            )
        }
    }

    /**
     * The last has been loaded, Room database has run out of items to display
     * so we fetch more from server
     */
    override fun onItemAtEndLoaded(itemAtEnd: Chat) {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
            chatsRepositoryImpl.onItemAtEndLoaded(
                itemAtEnd,
                myId,
                userId
            )
        }
    }
}