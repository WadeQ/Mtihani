package com.wadektech.mtihani.core;

import android.annotation.SuppressLint;

import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import timber.log.Timber;

public class StatusUtils {

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void listenForUserStatus(){
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("Users")
                .whereEqualTo("userId", Constants.getUserId())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @SuppressLint("TimberArgCount")
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Timber.d("listen:error", e.getMessage());
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            Timber.d("Status listener: %s", dc.getDocument().getData());

                        }
                    }
                });
    }
}
