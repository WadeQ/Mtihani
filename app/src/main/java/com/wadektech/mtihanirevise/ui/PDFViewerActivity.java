package com.wadektech.mtihanirevise.ui;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.wadektech.mtihanirevise.R;

import java.io.File;

import static com.google.android.gms.ads.AdRequest.ERROR_CODE_NETWORK_ERROR;

public class PDFViewerActivity extends AppCompatActivity {
    private  String fileName;
    private int currentPage;
    private PDFView pdfView;
    private AdView mAdView;
    private boolean shouldReloadAd = true;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfviewer);

        fileName=getIntent().getStringExtra("fileName");
        if(savedInstanceState != null){
            if(savedInstanceState.containsKey("fileName"))
                fileName=savedInstanceState.getString("fileName");
            if(savedInstanceState.containsKey("currentPage"))
                currentPage=savedInstanceState.getInt("currentPage");
        }
        pdfView = findViewById(R.id.pdfViewer);
        File rootPath = new File(Environment.getExternalStorageDirectory(), "Mtihani");
        final File localFile = new File(rootPath, fileName);
        pdfView.fromFile(localFile)
               // .pages(0, 1, 2, 3, 4, 5) // all pages are displayed by default
                .enableSwipe(true) // allows to block changing pages using swipe
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .onRender(nbPages -> pdfView.jumpTo(currentPage))
                .defaultPage(0)
                .onError(t -> Toast.makeText(PDFViewerActivity.this, "PDF error", Toast.LENGTH_SHORT).show())
                .onPageError((page, t) -> Toast.makeText(PDFViewerActivity.this, "PDF page error", Toast.LENGTH_SHORT).show())
                .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                .password(null)
                .scrollHandle(null)
                .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                .spacing(4)
                .autoSpacing(false) // add dynamic spacing to fit each page on its own on the screen
                .pageFitPolicy(FitPolicy.WIDTH)
                .pageSnap(false) // snap pages to screen boundaries
                .pageFling(false) // make a fling change only a single page like ViewPager
                .nightMode(false) // toggle night mode
                .load();
        mAdView = findViewById(R.id.adView);
        MobileAds.initialize(this, getString(R.string.test_ap_id));
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                if (errorCode == ERROR_CODE_NETWORK_ERROR) {

                    if (shouldReloadAd) {
                        AdRequest adRequest = new AdRequest.Builder().build();
                        mAdView.loadAd(adRequest);
                        shouldReloadAd = !shouldReloadAd;
                    }
                }
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(fileName!= null)
            outState.putString("fileName",fileName);
        outState.putInt("currentPage",pdfView.getCurrentPage());
    }
}
