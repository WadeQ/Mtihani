package com.wadektech.mtihanirevise.PdfViewer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.ScrollBar;
import com.wadektech.mtihanirevise.JSON.Downloader;
import com.wadektech.mtihanirevise.R;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ItemDetailActivity extends AppCompatActivity {
    PDFView pdfView;
    Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
      //display PDF to viewer
        pdfView = findViewById(R.id.pdf_viewer);
        //enable scrolling using scrollbar
        ScrollBar mScroll = findViewById(R.id.pdf_scrollbar);
        pdfView.setScrollBar(mScroll);
        //enable vertical scrolling
        mScroll.setHorizontal(false);

        Intent i=this.getIntent();

        //RECEIVE DATA
        String name=i.getExtras().getString("NAME_KEY");
        String pdf=i.getExtras().getString("EMAIL_KEY");
        String username=i.getExtras().getString("USERNAME_KEY");

        String extStorageDirectory = Environment.getExternalStorageDirectory()
                .toString();
        File folder = new File(extStorageDirectory, "pdf");
        folder.mkdir();
        File file = new File(folder, "Read.pdf");
        try {
            file.createNewFile();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Downloader.DownloadFile(pdf, file);
        showPdf();
    }
    public void showPdf() {
        File file = new File(Environment.getExternalStorageDirectory()+"/Mypdf/Read.pdf");

        PackageManager packageManager = getPackageManager();
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        testIntent.setType("application/pdf");
        List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(file);
        //pdfView.fromUri(uri);
        intent.setDataAndType(uri, "application/pdf");
        startActivity(intent);
    }
}
