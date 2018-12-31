package com.wadektech.mtihanirevise.viewHolders;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.pdfViewer.ItemDetailActivity;

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
        //showPdf();
        //OPEN DETAIL ACTIVITY
        Intent intent = new Intent(view.getContext() , ItemDetailActivity.class);
        view.getContext().startActivity(intent);
    }
}
