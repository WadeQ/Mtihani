package com.wadektech.mtihanirevise.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wadektech.mtihanirevise.adapter.PdfAdapter;
import com.wadektech.mtihanirevise.pojo.PdfModel;
import com.wadektech.mtihanirevise.R;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.view.MenuItemCompat.expandActionView;
import static android.support.v4.view.MenuItemCompat.getActionView;


public class PaperPerSubject extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private RecyclerView mRecycler;
    public PdfAdapter mAdapter;
    private DatabaseReference mDatabase ;
    private FirebaseUser firebaseUser ;
    public TextView mStatus ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(null);
        setContentView(R.layout.activity_paper_per_subject);
        Toolbar topToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);
        topToolBar.setLogoDescription(getResources().getString(R.string.logo_desc));

        mRecycler = findViewById(R.id.rv_pdf);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));

        List<PdfModel> pdfListItems = getAllItemList();

        PdfAdapter mAdapter = new PdfAdapter(pdfListItems, PaperPerSubject.this);
        mRecycler.setAdapter(mAdapter);

        mStatus = findViewById (R.id.tv_status);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) getActionView(menu.findItem(R.id.action_search));
        searchView.setOnQueryTextListener(this);
        Drawable drawColor = getResources().getDrawable(R.drawable.searchcolor);
        searchView.setBackground(drawColor);
        Toolbar.LayoutParams params = new Toolbar.LayoutParams(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        searchView.setLayoutParams(params);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setIconified(false);
        searchView.setPadding(0, 32, 0, 0);
        searchView.setQueryHint("Search");
        searchView.clearFocus();
        expandActionView(searchItem);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private List<PdfModel> getAllItemList() {
        final List<PdfModel> allItems = new ArrayList<>();

        firebaseUser = FirebaseAuth.getInstance ().getCurrentUser ();
        mDatabase = FirebaseDatabase.getInstance ().getReference ("pdf") ;
        mDatabase.addValueEventListener (new ValueEventListener () {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allItems.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    PdfModel pdfModel = snapshot.getValue(PdfModel.class);
                    allItems.add(pdfModel);
            }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }) ;

       /* allItems.add(new PdfModel ("KCSE 2008", "2008"));
        allItems.add(new PdfModel ("KCSE 2009", "2008"));
        allItems.add(new PdfModel ("KCSE 2010", "2010"));
        allItems.add(new PdfModel ("KCSE 2011", "2011"));
        allItems.add(new PdfModel ("KCSE 2012", "2012"));
        allItems.add(new PdfModel ("KCSE 2013", "2013"));
        allItems.add(new PdfModel ("KCSE 2014", "2014"));
        allItems.add(new PdfModel ("KCSE 2015", "2015"));
        allItems.add(new PdfModel ("KCSE 2016", "2016"));
        allItems.add(new PdfModel ("KCSE 2017", "2017"));
        allItems.add(new PdfModel ("KCSE ANSWERS", "2018"));

        **/
        return allItems;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        newText = newText.toLowerCase();
        return true;

    }
}
