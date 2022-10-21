package com.wadektech.mtihani.ui;

import android.Manifest;
import androidx.lifecycle.ViewModelProviders;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wadektech.mtihani.R;
import com.wadektech.mtihani.adapter.SinglePDFAdapter;
import com.wadektech.mtihani.pojo.PdfModel;
import com.wadektech.mtihani.pojo.SinglePDF;
import com.wadektech.mtihani.viewmodels.PaperPerSubjectViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static androidx.core.view.MenuItemCompat.expandActionView;
import static androidx.core.view.MenuItemCompat.getActionView;

import timber.log.Timber;

//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;


public class PaperPerSubject extends AppCompatActivity implements SearchView.OnQueryTextListener,
        SinglePDFAdapter.OnSinglePDFClickHandler {
    private static final String TAG = "PaperPerSubject";
    //public PdfAdapter mAdapter;
    //private FirebaseUser firebaseUser;
    public TextView mStatus;
    private String category;
    private SinglePDFAdapter mSinglePDFAdapter;
    private List<SinglePDF> singlePDFList;
    private SwipeRefreshLayout mSwipe;
    private PaperPerSubjectViewModel viewModel;
    private static final int GALLERY_REQUEST_CODE = 23;
    private SinglePDF singlePDF;
    private TextView progressTextView;
    private Button cancelBtn, openBtn;
    private ProgressBar progressBar;
    private AlertDialog mDialog;
    private InterstitialAd interstitialAd;
    private RewardedAd mRewardedAd;
    //creating Object of Rewarded Ad Load Callback
    RewardedAdLoadCallback rewardedAdLoadCallback;
    //creating Object of Rewarded Ad Callback
    RewardedAdCallback rewardedAdCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(null);
        setContentView(R.layout.activity_paper_per_subject);
        Toolbar topToolBar = findViewById(R.id.toolbar);
        mSwipe = findViewById(R.id.swipe);
        mSwipe.setRefreshing(true);
        setSupportActionBar(topToolBar);
        topToolBar.setLogoDescription(getResources().getString(R.string.logo_desc));
        category = getIntent().getStringExtra("category");
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("category"))
                category = savedInstanceState.getString("category");
        }
        RecyclerView mRecycler = findViewById(R.id.rv_pdf);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));

        List<PdfModel> pdfListItems = getAllItemList();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                //init completed
                Timber.d("onInitializationComplete: %s", initializationStatus.toString());
            }
        });

        mRewardedAd = new RewardedAd( this, getString(R.string.rewarded_ad_id) ) ;
        loadRewardedVideoAd();

        // creating  RewardedAdLoadCallback for Rewarded Ad with some 2 Override methods
        rewardedAdLoadCallback =new RewardedAdLoadCallback(){
            @Override
            public void onRewardedAdLoaded() {
                // Showing a simple Toast message to user when Rewarded Ad Failed to Load
                Toast.makeText (PaperPerSubject.this, "Rewarded Ad is Loaded", Toast.LENGTH_LONG).show() ;
            }

            @Override
            public void onRewardedAdFailedToLoad( LoadAdError adError) {
                // Showing a simple Toast message to user when Rewarded Ad Failed to Load
                Toast.makeText (PaperPerSubject.this, "Rewarded Ad failed to load with error "+adError.getMessage(), Toast.LENGTH_LONG).show() ;
            }
        };

        mStatus = findViewById(R.id.tv_status);
        //loading pdfs based on selected year
        singlePDFList = new ArrayList<>();
        LinearLayoutManager manager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,false);
        mRecycler.setLayoutManager(manager);
        mSinglePDFAdapter = new SinglePDFAdapter(singlePDFList, this);
        mRecycler.setAdapter(mSinglePDFAdapter);
        viewModel = ViewModelProviders.of(this)
                .get(PaperPerSubjectViewModel.class);
        viewModel.downloadPDFsPerCategory(category);
        viewModel.getPdfPerCategoryResponse().observe(this, this::onPDFsReceived);
        viewModel.getPdfsDownloadResponse().observe(this, this::onResponseReceived);
        viewModel.getSinglePDFDownloadResponse().observe(this,
                this::onPDFDownloadProgressReceived);
        viewModel.getProgressUpdate().observe(this,this::onProgressUpdate);
        mSwipe.setOnRefreshListener(() -> viewModel.downloadPDFsPerCategory(category));

    }

    @Override
    protected void onResume() {
        loadRewardedVideoAd();
        super.onResume();
    }

    @SuppressLint("SetTextI18n")
    private void onProgressUpdate(Integer integer) {
        if(progressTextView != null)
            progressTextView.setText(integer+"% downloaded");
    }

    private void onPDFDownloadProgressReceived(String response) {
        if (response != null) {
            if(progressBar != null)progressBar.setVisibility(View.INVISIBLE);
            if(response.equals("success")){
                if(openBtn != null)
                    openBtn.setVisibility(View.VISIBLE);
            }
            Toast.makeText(this, response, Toast.LENGTH_SHORT).show();
        }
    }

    private void onResponseReceived(String response) {
        if (response != null) {
            mSwipe.setRefreshing(false);
            if (!response.equals("loaded"))
                Toast.makeText(this, response, Toast.LENGTH_SHORT).show();
        }
    }

    private void onPDFsReceived(List<SinglePDF> singlePDFS) {
        if (singlePDFS != null) {
            if (singlePDFS.size() > 0) {
                singlePDFList.clear();
                singlePDFList.addAll(singlePDFS);
                mSinglePDFAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) getActionView(menu.findItem(R.id.action_search));
        searchView.setOnQueryTextListener(this);
        Drawable drawColor = getResources().getDrawable(R.drawable.searchcolor);
        Toolbar.LayoutParams params = new Toolbar.LayoutParams(Toolbar.LayoutParams.MATCH_PARENT,
                Toolbar.LayoutParams.MATCH_PARENT);
        searchView.setLayoutParams(params);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint("Search");
        searchView.setQueryHint(Html.fromHtml("<font color = #b71c1c>" + getResources().getString(R.string.hintSearch) + "</font>"));
        ImageView icon = searchView.findViewById(androidx.appcompat.R.id.search_button);
        icon.setColorFilter(Color.RED);
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

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("pdf");
        mDatabase.addValueEventListener(new ValueEventListener () {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allItems.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PdfModel pdfModel = snapshot.getValue(PdfModel.class);
                    allItems.add(pdfModel);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return allItems;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // filter recycler view when query submitted
        mSinglePDFAdapter.getFilter().filter(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // filter recycler view when text is changed
        mSinglePDFAdapter.getFilter().filter(newText);
        return true;

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (category != null)
            outState.putString("category", category);
    }

    @Override
    public void onSinglePDFClicked(SinglePDF singlePDF) {
        this.singlePDF = singlePDF;
        //check for permissions and fire at Cloud Storage for the PDF file
        preCheck();
    }

    private void preCheck() {
        //check permissions first!
        int gallery_permission = ContextCompat
                .checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (gallery_permission != PackageManager.PERMISSION_GRANTED) {
                //check if permission has ever been denied
                if (ActivityCompat
                        .shouldShowRequestPermissionRationale(this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Permission to access files is required for Mtihani Revise to save PDF files inside your device storage")
                            .setTitle("Permission Required")
                            .setPositiveButton("OK", (dialog, which) -> ask_Gallery_Permission())
                            .setNegativeButton("CANCEL", (dialog, which) -> {
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    //first time this permission is being asked
                    ask_Gallery_Permission();
                }
            } else {
                //permission is granted.proceed to download PDF
                if (singlePDF != null) {
                    File rootPath = new File(Environment.getExternalStorageDirectory(), "Mtihani");
                    final File localFile = new File(rootPath, singlePDF.getFileName());
                    if(!localFile.exists()) {
                        downloadMonitor();
                        viewModel.downloadPDF(singlePDF.getFileName());
                    }else{
                        loadRewardedVideoAd();
                    }
                }
            }

        } else {
            //download PDF. device is pre mashmallow
            if (singlePDF != null) {
                File rootPath = new File(Environment.getExternalStorageDirectory(), "Mtihani");
                final File localFile = new File(rootPath, singlePDF.getFileName());
                if(!localFile.exists()) {
                    downloadMonitor();
                    viewModel.downloadPDF(singlePDF.getFileName());
                }else{
                    loadRewardedVideoAd();
                }
            }
        }
    }

    public void ask_Gallery_Permission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                GALLERY_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == GALLERY_REQUEST_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (singlePDF != null) {
                        File rootPath = new File(Environment.getExternalStorageDirectory(), "Mtihani");
                        final File localFile = new File(rootPath, singlePDF.getFileName());
                        if(!localFile.exists()) {
                            downloadMonitor();
                            viewModel.downloadPDF(singlePDF.getFileName());
                        }else{
                            loadRewardedVideoAd();
                        }
                    }
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    private void downloadMonitor() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(this);
        View mView = layoutInflaterAndroid.inflate(R.layout.custom_progress_item, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(this);
        alertDialogBuilderUserInput.setView(mView);

        cancelBtn = mView.findViewById(R.id.cancelBtn);
        openBtn = mView.findViewById(R.id.openBtn);
        openBtn.setVisibility(View.INVISIBLE);
        progressTextView=mView.findViewById(R.id.progress_tv);
        progressBar=mView.findViewById(R.id.progressBar);
        cancelBtn.setOnClickListener(v -> {
            if(mDialog != null)
                mDialog.dismiss();
        });

        openBtn.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            showRewardedAd();
        });

        alertDialogBuilderUserInput
                .setCancelable(false);
        mDialog = alertDialogBuilderUserInput.create();
        mDialog.show();
    }

    private void loadRewardedVideoAd(){
        // Creating  an Ad Request
        AdRequest adRequest = new AdRequest.Builder().build();
        // load Rewarded Ad with the Request
        mRewardedAd.loadAd(adRequest, rewardedAdLoadCallback);
    }

    private void showRewardedAd() {
        if (mRewardedAd.isLoaded()) {
            //creating the Rewarded Ad Callback and showing the user appropriate message
            rewardedAdCallback = new RewardedAdCallback() {@Override
            public void onRewardedAdOpened() {
                // Showing a simple Toast message to user when Rewarded Ad is opened
                Toast.makeText(PaperPerSubject.this, "Rewarded Ad is Opened", Toast.LENGTH_LONG).show();
            }

                @Override
                public void onRewardedAdClosed() {
                    progressBar.setVisibility(View.INVISIBLE);
                    mDialog.dismiss();
                    Intent intent = new Intent(PaperPerSubject.this,
                            PastPapersActivity.class);
//                    intent.putExtra("fileName",singlePDF.getFileName());
                    startActivity(intent);
                }

                @Override
                public void onUserEarnedReward(@NonNull com.google.android.gms.ads.rewarded.RewardItem rewardItem) {
                    progressBar.setVisibility(View.INVISIBLE);
                    mDialog.dismiss();
                    Intent intent = new Intent(PaperPerSubject.this,
                            PDFViewerActivity.class);
                    intent.putExtra("fileName",singlePDF.getFileName());
                    startActivity(intent);
                }

                @Override
                public void onRewardedAdFailedToShow(AdError adError) {
                    // Showing a simple Toast message to user when Rewarded Ad Failed to Show
                    Toast.makeText(PaperPerSubject.this, "Rewarded Ad failed to show due to error:" + adError.toString(), Toast.LENGTH_LONG).show();
                    loadRewardedVideoAd();
                }
            };

            //showing the ad Rewarded Ad if it is loaded
            mRewardedAd.show(PaperPerSubject.this, rewardedAdCallback);
            // Showing a simple Toast message to user when an Rewarded ad is shown to the user
            Toast.makeText(PaperPerSubject.this, "Rewarded Ad  is loaded and Now showing ad  ", Toast.LENGTH_LONG).show();

        }
        else {
            //Load the Rewarded ad if it is not loaded
            loadRewardedVideoAd();
            // Showing a simple Toast message to user when Rewarded ad is not loaded
            Toast.makeText(PaperPerSubject.this, "Rewarded Ad is not Loaded ", Toast.LENGTH_LONG).show();

        }

    }

    private void loadAd(){
        mSwipe.setRefreshing(true);
        MobileAds.initialize(this, getString(R.string.banner_ad_id));
        interstitialAd = new InterstitialAd(PaperPerSubject.this);
        interstitialAd.setAdUnitId(getString(R.string.interstitial_ad_id));
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                mSwipe.setRefreshing(false);
                interstitialAd.show();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                mSwipe.setRefreshing(false);
                Intent intent = new Intent(PaperPerSubject.this,
                        PDFViewerActivity.class);
                intent.putExtra("fileName",singlePDF.getFileName());
                startActivity(intent);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdLeftApplication() {
                mSwipe.setRefreshing(false);
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the interstitial ad is closed.
                mSwipe.setRefreshing(false);
                Intent intent = new Intent(PaperPerSubject.this,
                        PDFViewerActivity.class);
                intent.putExtra("fileName",singlePDF.getFileName());
                startActivity(intent);
            }
        });
    }

}
