package com.wadektech.mtihanirevise.fragments;


import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.adapter.UserAdapter;
import com.wadektech.mtihanirevise.room.User;
import com.wadektech.mtihanirevise.utils.Constants;
import com.wadektech.mtihanirevise.viewmodels.UsersViewModel;

import java.util.ArrayList;
import java.util.List;

//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;

/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> users;
    private EditText mSearch;
    private UsersViewModel viewModel;

    public UsersFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        recyclerView = view.findViewById(R.id.rv_classroom);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        users = new ArrayList<>();
        // readUsers();
        viewModel = ViewModelProviders.of(requireActivity()).get(UsersViewModel.class);
        viewModel.getUsersList().observe(getViewLifecycleOwner(), usersList -> {
            userAdapter = new UserAdapter (getContext(), /*users ,*/ true);
            recyclerView.setAdapter(userAdapter);
            userAdapter.submitList(usersList);
        });
        mSearch = view.findViewById(R.id.tv_search_users);
        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 0) {
                    searchUsers(s.toString().toLowerCase() + "%");
                } else {
                    searchUsers("");
                }
            }
        });

        return view;

    }

    private void searchUsers(String s) {
        if (s.equals("")) {
            userAdapter = null;
            viewModel = ViewModelProviders.of(this).get(UsersViewModel.class);
            viewModel.getUsersList().observe(this, usersList -> {
                userAdapter = new UserAdapter (getActivity(), /*users ,*/ true);
                recyclerView.setAdapter(userAdapter);
                userAdapter.submitList(usersList);
            });
        } else {
            userAdapter = null;
            viewModel = ViewModelProviders.of(this).get(UsersViewModel.class);
            viewModel.getUsersByName(s).observe(this, usersList -> {
                userAdapter = new UserAdapter (getActivity(), /*users ,*/ true);
                recyclerView.setAdapter(userAdapter);
                userAdapter.submitList(usersList);
            });
        }
    }

    /**
     * Replaced method. no longer necessary
     */
    private void readUsers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener () {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mSearch.getText().toString().equals("")) {
                    users.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        assert user != null;
                       // assert firebaseUser != null;
                        if (user.getUserId() != null && !user.getUserId().equals(Constants.getUserId())) {
                            users.add(user);
                        }
                    }
                    userAdapter = new UserAdapter (getContext(), /*users,*/ false);
                    recyclerView.setAdapter(userAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
