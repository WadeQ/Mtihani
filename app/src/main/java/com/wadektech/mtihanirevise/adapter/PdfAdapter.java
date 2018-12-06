package com.wadektech.mtihanirevise.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.pdfViewer.ItemDetailActivity;

import java.util.ArrayList;




public class PdfAdapter extends RecyclerView.Adapter<PdfAdapter.PdfViewHolder>{

    private ArrayList<String> pdfItems ;
    public Context context ;

    public PdfAdapter(ArrayList<String> pdfItems, Context context) {
        this.pdfItems = pdfItems;
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
        holder.mSubjectName.setText(pdfItems.get(position));
    }

    @Override
    public int getItemCount() {
        return pdfItems.size();
    }
    public class PdfViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView mSubjectName ;

        public PdfViewHolder(View itemView) {

            super(itemView);
            itemView.setOnClickListener(this);

            mSubjectName = itemView.findViewById(R.id.tv_subject_name);
        }

        @Override
        public void onClick(View view) {
            //showPdf();
            //OPEN DETAIL ACTIVITY
            Intent intent = new Intent(view.getContext() , ItemDetailActivity.class);
            view.getContext().startActivity(intent);
        }
    }
}
