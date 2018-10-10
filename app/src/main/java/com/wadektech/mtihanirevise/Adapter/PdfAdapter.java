package com.wadektech.mtihanirevise.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wadektech.mtihanirevise.POJO.PdfListItems;
import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.ViewHolders.PdfViewHolder;

import java.util.List;

public class PdfAdapter extends RecyclerView.Adapter<PdfViewHolder>{
    List<PdfListItems> pdfListItems ;
    Context context ;

    public PdfAdapter(List<PdfListItems> pdfListItems, Context context) {
        this.pdfListItems = pdfListItems;
        this.context = context;
    }

    @NonNull
    @Override
    public PdfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View pdfView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_list_item , parent , false);
        return new PdfViewHolder(pdfView);
    }

    @Override
    public void onBindViewHolder(@NonNull PdfViewHolder holder, int position) {
        holder.mSubjectName.setText(pdfListItems.get(position).getSubject());
        holder.mPaperYear.setText(pdfListItems.get(position).getYear());
    }

    @Override
    public int getItemCount() {
        return pdfListItems.size();
    }
}
