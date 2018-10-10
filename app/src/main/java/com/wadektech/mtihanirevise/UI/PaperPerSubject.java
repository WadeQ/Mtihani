package com.wadektech.mtihanirevise.UI;

import android.app.SearchManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.wadektech.mtihanirevise.Adapter.PdfAdapter;
import com.wadektech.mtihanirevise.POJO.PdfListItems;
import com.wadektech.mtihanirevise.R;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.view.MenuItemCompat.expandActionView;
import static android.support.v4.view.MenuItemCompat.getActionView;
public class PaperPerSubject extends AppCompatActivity {
    private RecyclerView mRecycler ;
    private RecyclerView.Adapter mAdapter ;

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

        List<PdfListItems> pdfListItems = getAllItemList();

        PdfAdapter mAdapter = new PdfAdapter(pdfListItems, PaperPerSubject.this);
        mRecycler.setAdapter(mAdapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }
        Drawable drawColor = getResources().getDrawable(R.drawable.searchcolor);
        searchView.setBackground( drawColor );
        Toolbar.LayoutParams params = new Toolbar.LayoutParams(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        searchView.setLayoutParams(params);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setIconified(false);
        searchView.setPadding(0,32,0,0);
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
    private List<PdfListItems> getAllItemList (){
        List<PdfListItems> allItems = new ArrayList<>();
        allItems.add(new PdfListItems("KCSE 1996 - 2009" , "1996 - 2009"));
        allItems.add(new PdfListItems("KCSE 2006", "2006"));
        allItems.add(new PdfListItems("KCSE 2007", "2007"));
        allItems.add(new PdfListItems("KCSE 2008", "2008"));
        allItems.add(new PdfListItems("KCSE 2009", "2008"));
        allItems.add(new PdfListItems("KCSE 2010", "2010"));
        allItems.add(new PdfListItems("KCSE 2011", "2011"));
        allItems.add(new PdfListItems("KCSE 2012", "2012"));
        allItems.add(new PdfListItems("KCSE 2013", "2013"));
        allItems.add(new PdfListItems("KCSE 2014", "2014"));
        allItems.add(new PdfListItems("KCSE 2015", "2015"));
        allItems.add(new PdfListItems("KCSE 2016", "2016"));
        allItems.add(new PdfListItems("KCSE 2017", "2017"));
        allItems.add(new PdfListItems("SBC MOCK PAPERS", "2018"));

        return allItems;
    }
}
