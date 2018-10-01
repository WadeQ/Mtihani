package com.wadektech.mtihanirevise.Adapter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.wadektech.mtihanirevise.POJO.Upload;
import com.wadektech.mtihanirevise.R;

import java.util.List;

public class PaperPerSubjectAdapter extends AppCompatActivity {
    //the listview
    RecyclerView mRecyclerView;

    //database reference to get uploads data
    DatabaseReference mDatabaseReference;

    //list to store uploads data
    List<Upload> uploadList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_per_subject);

    }
}
