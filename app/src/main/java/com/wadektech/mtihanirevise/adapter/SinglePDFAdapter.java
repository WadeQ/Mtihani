package com.wadektech.mtihanirevise.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.pojo.SinglePDF;
import java.util.ArrayList;
import java.util.List;


public class SinglePDFAdapter extends RecyclerView.Adapter<SinglePDFAdapter.SinglePDFAdapterViewHolder> implements Filterable {
    private List<SinglePDF> pdfList;
    private List<SinglePDF>pdfListFiltered;
    private OnSinglePDFClickHandler mHandler;

    public SinglePDFAdapter(List<SinglePDF> pdfList,OnSinglePDFClickHandler mHandler) {
        this.pdfList = pdfList;
        this.mHandler=mHandler;
        this.pdfListFiltered=pdfList;
    }

    @NonNull
    @Override
    public SinglePDFAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View pdfView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_pdf_item, parent, false);
        return new SinglePDFAdapterViewHolder(pdfView);
    }

    @Override
    public void onBindViewHolder(@NonNull SinglePDFAdapterViewHolder holder, int position) {
        holder.onBind(pdfListFiltered.get(position));
    }

    @Override
    public int getItemCount() {
        return pdfListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    pdfListFiltered = pdfList;
                } else {
                    List<SinglePDF> filteredList = new ArrayList<>();
                    for (SinglePDF row : pdfList) {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getFileName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    pdfListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = pdfListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                pdfListFiltered = (ArrayList<SinglePDF>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface OnSinglePDFClickHandler{
        void onSinglePDFClicked(SinglePDF singlePDF);
    }

    public class SinglePDFAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
            private TextView fileName;

        public SinglePDFAdapterViewHolder(View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.filename_tv);
            itemView.setOnClickListener(this);
        }

        private void onBind(SinglePDF singlePDF) {
            fileName.setText(singlePDF.getFileName());
        }

        @Override
        public void onClick(View v) {
            mHandler.onSinglePDFClicked(pdfListFiltered.get(getAdapterPosition()));
        }
    }
}
