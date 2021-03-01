package com.wadektech.mtihani.database;

import androidx.paging.PagedList;
import androidx.annotation.NonNull;

import com.wadektech.mtihani.repository.MtihaniRepository;
import com.wadektech.mtihani.room.Chat;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MessagesBoundaryCallback extends PagedList.BoundaryCallback<Chat> {
    private Executor executor = Executors.newSingleThreadExecutor();
    private PagingRequestHelper helper = new PagingRequestHelper (executor);
    private String myId,userId;


    public MessagesBoundaryCallback(String myId,String userId) {
        this.myId=myId;
        this.userId=userId;
    }

    @Override
    public void onZeroItemsLoaded() {
        //database has no items. load items from server
        helper.runIfNotRunning(PagingRequestHelper.RequestType.BEFORE, helperCallback ->
                MtihaniRepository.onZeroItemsLoaded(myId,userId));

    }

    /**
     * Room database has loaded the first item.
     * we check if the server has any newer items and download them
     */
    @Override
    public void onItemAtFrontLoaded(@NonNull Chat itemAtFront) {

        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL, helperCallback
                -> MtihaniRepository.onItemAtFrontLoaded(itemAtFront,myId,userId));

    }

    /**
     * The last has been loaded, Room database has run out of items to display
     * so we fetch more from server
     */
    @Override
    public void onItemAtEndLoaded(@NonNull Chat itemAtEnd) {

        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER, helperCallback ->
                MtihaniRepository.onItemAtEndLoaded(itemAtEnd,myId,userId));
    }


}

