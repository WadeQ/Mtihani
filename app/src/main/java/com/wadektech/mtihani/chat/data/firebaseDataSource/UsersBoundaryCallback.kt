package com.wadektech.mtihani.chat.data.firebaseDataSource

import androidx.paging.PagedList.BoundaryCallback
import com.wadektech.mtihani.chat.data.localDatasource.room.User
import com.wadektech.mtihani.chat.data.repository.ChatsRepositoryImpl
import timber.log.Timber
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class UsersBoundaryCallback : BoundaryCallback<User>() {
    private val executor: Executor = Executors.newSingleThreadExecutor()
    private val helper = PagingRequestHelper(executor)
    private val chatsRepositoryImpl = ChatsRepositoryImpl()

    override fun onZeroItemsLoaded() {
        //database has no items. load items from server
        helper.runIfNotRunning(PagingRequestHelper.RequestType.BEFORE) {
            chatsRepositoryImpl.onZeroUsersLoaded() }
    }

    /**
     * Room database has loaded the first item.
     * we check if the server has any newer items and download them
     */
    override fun onItemAtFrontLoaded(itemAtFront: User) {
        monitor++
        Timber.d("%s times", "onItemAtFrontLoaded called " + monitor)
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            chatsRepositoryImpl.onUserAtFrontLoaded(
                itemAtFront
            )
        }
    }

    /**
     * The last has been loaded, Room database has run out of items to display
     * so we fetch more from server
     */
    override fun onItemAtEndLoaded(itemAtEnd: User) {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
            chatsRepositoryImpl.onUserAtEndLoaded(
                itemAtEnd
            )
        }
    }

    companion object {
        private var monitor = 0
    }
}