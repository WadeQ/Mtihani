package com.wadektech.mtihani.database;

import androidx.paging.PagedList;
import androidx.annotation.NonNull;

import com.wadektech.mtihani.repository.MtihaniRepository;
import com.wadektech.mtihani.room.User;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import timber.log.Timber;

public class UsersBoundaryCallback extends PagedList.BoundaryCallback<User> {
    private Executor executor = Executors.newSingleThreadExecutor();
    private PagingRequestHelper helper = new PagingRequestHelper (executor);
    private static int monitor = 0;

    public UsersBoundaryCallback() {
    }

    @Override
    public void onZeroItemsLoaded() {
        //database has no items. load items from server
        helper.runIfNotRunning(PagingRequestHelper.RequestType.BEFORE, helperCallback ->
                MtihaniRepository.onZeroUsersLoaded());
    }
    /**
     * Room database has loaded the first item.
     * we check if the server has any newer items and download them
     */
    @Override
    public void onItemAtFrontLoaded(@NonNull User itemAtFront) {
        monitor++;
        Timber.d("onItemAtFrontLoaded called " + monitor + " times");
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL, helperCallback
                -> MtihaniRepository.onUserAtFrontLoaded(itemAtFront));
    }
    /**
     * The last has been loaded, Room database has run out of items to display
     * so we fetch more from server
     */
    @Override
    public void onItemAtEndLoaded(@NonNull User itemAtEnd) {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER, helperCallback ->
                MtihaniRepository.onUserAtEndLoaded(itemAtEnd));
    }
}
