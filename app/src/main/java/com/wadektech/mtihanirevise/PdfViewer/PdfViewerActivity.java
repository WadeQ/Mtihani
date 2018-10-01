package com.wadektech.mtihanirevise.PdfViewer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.ScrollBar;
import com.wadektech.mtihanirevise.R;

public class PdfViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);
      //display PDF to viewer
        PDFView mPDF = findViewById(R.id.pdf_viewer);
        //enable scrolling using scrollbar
        ScrollBar mScroll = findViewById(R.id.pdf_scrollbar);
        mPDF.setScrollBar(mScroll);
        //enable vertical scrolling
        mScroll.setHorizontal(false);
    }
}
