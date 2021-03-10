package com.wadektech.mtihani.ui;

import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.wadektech.mtihani.R;
import com.wadektech.mtihani.adapter.RecyclerViewAdapter;
import com.wadektech.mtihani.auth.LoginActivity;
import com.wadektech.mtihani.persistence.MtihaniRevise;
import com.wadektech.mtihani.pojo.RowModel;
import com.wadektech.mtihani.utils.Constants;
import com.wadektech.mtihani.viewmodels.ChatActivityViewModel;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PastPapersActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        RecyclerViewAdapter.OnItemClickHandler {
    private GridLayoutManager lLayout;
    FirebaseAuth mAuth;
    CircleImageView userProfile;
    GoogleApiClient mGoogleApiClient;
    private Uri imageUri;
    DatabaseReference databaseReference;
    // FirebaseUser firebaseUser;
    StorageReference storageReference;
    private SwipeRefreshLayout mSwipe;
    NiftyDialogBuilder materialDesignAnimatedDialog;
    private Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_papers);
        Toolbar topToolBar = findViewById(R.id.toolbar);
        mSwipe = findViewById(R.id.mSwipe);
        topToolBar.setTitle(null);
        setSupportActionBar(topToolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        materialDesignAnimatedDialog = NiftyDialogBuilder.getInstance(this);

        ChatActivityViewModel viewModel = ViewModelProviders.of(this).get(ChatActivityViewModel.class);
        viewModel.loadUserUnreadChats(Constants.getUserId());
        mHandler = new Handler();

        FirebaseDatabase.getInstance().getReference("Users").keepSynced(true);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestId()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In Api and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();
        userProfile = findViewById(R.id.profileImage);

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        List<RowModel> rowListItem = getAllItemList();
        lLayout = new GridLayoutManager(PastPapersActivity.this, 2);

        RecyclerView rView = findViewById(R.id.recycler_view);
        rView.setHasFixedSize(true);
        rView.setLayoutManager(lLayout);

        RecyclerViewAdapter rcAdapter = new RecyclerViewAdapter (rowListItem, this);
        rView.setAdapter(rcAdapter);
        mSwipe.setOnRefreshListener(() -> mSwipe.setRefreshing(false));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menu_chat) {
            //send intent to chat activity
            startActivity(new Intent(getApplicationContext(), ChatActivity.class));
            return true;
        }
        if (id == R.id.menuSettings){
            //send intent to settings activity
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private List<RowModel> getAllItemList() {
        List<RowModel> allItems = new ArrayList<>();
        allItems.add(new RowModel("KCSE 1995", R.drawable.pdf));
        allItems.add(new RowModel("KCSE 1996", R.drawable.pdf));
        allItems.add(new RowModel("KCSE 1997", R.drawable.pdf));
        allItems.add(new RowModel("KCSE 1998", R.drawable.pdf));
        allItems.add(new RowModel("KCSE 1999", R.drawable.pdf));
        allItems.add(new RowModel("KCSE 2000", R.drawable.pdf));
        allItems.add(new RowModel("KCSE 2001", R.drawable.pdf));
        allItems.add(new RowModel("KCSE 2002", R.drawable.pdf));
        allItems.add(new RowModel("KCSE 2003", R.drawable.pdf));
        allItems.add(new RowModel("KCSE 2004", R.drawable.pdf));
        allItems.add(new RowModel("KCSE 2005", R.drawable.pdf));
        allItems.add(new RowModel("KCSE 2006", R.drawable.pdf));
        allItems.add(new RowModel("KCSE 2007", R.drawable.pdf));
        allItems.add(new RowModel("KCSE 2008", R.drawable.pdf));
        allItems.add(new RowModel("KCSE 2009", R.drawable.pdf));
        allItems.add(new RowModel("KCSE 2010", R.drawable.pdf));
        allItems.add(new RowModel("KCSE 2011", R.drawable.pdf));
        allItems.add(new RowModel("KCSE 2012", R.drawable.pdf));
        allItems.add(new RowModel("KCSE 2013", R.drawable.pdf));
        allItems.add(new RowModel("KCSE 2014", R.drawable.pdf));
        allItems.add(new RowModel("KCSE 2015", R.drawable.pdf));
        allItems.add(new RowModel("KCSE 2016", R.drawable.pdf));
        allItems.add(new RowModel("KCSE 2017", R.drawable.pdf));
        allItems.add(new RowModel("KCSE 2018", R.drawable.pdf));
        allItems.add(new RowModel("KCSE 2019", R.drawable.pdf));
        allItems.add(new RowModel("KCSE ANSWERS", R.drawable.pdf));

        return allItems;
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*
        if user is not signed in
        open login activity
        */
        if (mAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        if (Constants.getImageURL().equals("default")) {
            userProfile.setImageResource(R.drawable.profile);
        } else {
            final int defaultImageResId = R.drawable.profile;
            Picasso.with(MtihaniRevise
                    .Companion
                    .getApp ())
                    .load(Constants.getImageURL())
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(userProfile, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(PastPapersActivity.this)
                                    .load(Constants.getImageURL())
                                    .error(defaultImageResId)
                                    // .networkPolicy (NetworkPolicy.NO_CACHE)
                                    //.memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).error (defaultImageResId)
                                    .into(userProfile);
                        }
                    });
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "Connection Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGridItemClicked(String category) {
        Intent intent = new Intent(this, PaperPerSubject.class);
        intent.putExtra("category", category);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainSliderActivity.class);
        startActivity(intent);
    }
}
