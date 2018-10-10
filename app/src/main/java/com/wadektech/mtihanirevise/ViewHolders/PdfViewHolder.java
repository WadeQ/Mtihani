package com.wadektech.mtihanirevise.ViewHolders;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.wadektech.mtihanirevise.PdfViewer.PdfViewerActivity;
import com.wadektech.mtihanirevise.R;

public class PdfViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
   public   TextView mSubjectName , mPaperYear;

     public PdfViewHolder(View itemView) {

        super(itemView);
        itemView.setOnClickListener(this);

        mSubjectName = itemView.findViewById(R.id.tv_subject_name);
        mPaperYear = itemView.findViewById(R.id.tv_year);
    }

    @Override
    public void onClick(View view) {

        Intent intent = new Intent(view.getContext() , PdfViewerActivity.class);
        view.getContext().startActivity(intent);
    }
}
