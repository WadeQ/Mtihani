package com.wadektech.mtihanirevise.ViewHolders;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wadektech.mtihanirevise.UI.PaperPerSubject;
import com.wadektech.mtihanirevise.R;

public class RecyclerViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView examYear;
    public ImageView examPhoto;

    public RecyclerViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        examYear = itemView.findViewById(R.id.tv_kcse);
        examPhoto= itemView.findViewById(R.id.tv_subject_icon);
    }

    @Override
    public void onClick(View view) {
        switch (getAdapterPosition()){
            case 0 :
                Intent intent = new Intent(view.getContext() , PaperPerSubject.class);
                view.getContext().startActivity(intent);
                break;

            case 1 :
                Intent intentOne = new Intent(view.getContext() , PaperPerSubject.class);
                view.getContext().startActivity(intentOne);
                break;
        }
    }
}