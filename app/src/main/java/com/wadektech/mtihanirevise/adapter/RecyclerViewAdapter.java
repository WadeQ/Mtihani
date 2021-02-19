package com.wadektech.mtihanirevise.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.pojo.RowModel;

import java.util.List;

public class RecyclerViewAdapter extends
        RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolders> {
    private OnItemClickHandler mHandler;
    private List<RowModel> itemList;

    public RecyclerViewAdapter(List<RowModel> itemList, OnItemClickHandler mHandler) {
        this.itemList = itemList;
        this.mHandler = mHandler;

    }

    @NonNull
    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext()).inflate(R.layout.row_past_papers, null);
        return new RecyclerViewHolders(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolders holder, int position) {
        holder.examYear.setText(itemList.get(position).getYear());
        holder.examPhoto.setImageResource(itemList.get(position).getPhoto());
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }

    public interface OnItemClickHandler {
        void onGridItemClicked(String category);
    }

    public class RecyclerViewHolders extends
            RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView examYear;
        public ImageView examPhoto;

        public RecyclerViewHolders(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            examYear = itemView.findViewById(R.id.tv_kcse);
            examPhoto = itemView.findViewById(R.id.tv_subject_icon);
        }

        @Override
        public void onClick(View view) {
            mHandler.onGridItemClicked(itemList.get(getAdapterPosition()).getYear());

        }
    }

}