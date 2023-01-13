package com.wadektech.mtihani.notification.domain;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.wadektech.mtihani.core.Constants;
import com.wadektech.mtihani.notification.domain.models.Token;

public class MyFirebaseIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh ();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance ().getCurrentUser ();
        String refreshToken = FirebaseInstanceId.getInstance ().getToken ();
        if (firebaseUser != null){
            updateToken(refreshToken);
        }
    }

    private void updateToken(String refreshToken) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance ().getCurrentUser ();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance ().getReference ("Tokens");
        Token token = new Token (refreshToken);
        assert firebaseUser != null;
        if(Constants.getUserId().equals("")) {
            databaseReference.child(firebaseUser.getUid()).setValue(token);
        }else{
            databaseReference.child(Constants.getUserId()).setValue(token);
        }
    }
}
