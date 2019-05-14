package com.wadektech.mtihanirevise.repository;

import com.wadektech.mtihanirevise.room.User;

import java.util.ArrayList;

/**
 * implement the singleton pattern
 */
public class UserRepository {
    private ArrayList<User> allUsers = new ArrayList<> ();
    public static UserRepository iinstance ;
    private static UserRepository getInstance(){
        if (iinstance == null){
            iinstance = new UserRepository ();
        }
        return iinstance;
    }
    //get users from firebase

}
