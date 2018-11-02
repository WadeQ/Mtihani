package com.wadektech.mtihanirevise.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wadektech.mtihanirevise.pojo.RowModel;
import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.viewHolders.RecyclerViewHolders;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewHolders> {

    private List<RowModel> itemList;
    private Context context;

    public RecyclerViewAdapter(Context context, List<RowModel> itemList) {
        this.itemList = itemList;
        this.context = context;

    }

    @NonNull
    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_past_papers, null);
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

}