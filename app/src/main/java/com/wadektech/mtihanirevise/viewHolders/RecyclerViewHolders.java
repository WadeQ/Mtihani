package com.wadektech.mtihanirevise.viewHolders;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.ui.PaperPerSubject;

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

            case 2 :
                Intent intentTwo = new Intent(view.getContext() , PaperPerSubject.class);
                view.getContext().startActivity(intentTwo);
                break;

            case 3 :
                Intent intentThree = new Intent(view.getContext() , PaperPerSubject.class);
                view.getContext().startActivity(intentThree);
                break;

            case 4 :
                Intent intentFour = new Intent(view.getContext() , PaperPerSubject.class);
                view.getContext().startActivity(intentFour);
                break;

            case 5 :
                Intent intentFive = new Intent(view.getContext() , PaperPerSubject.class);
                view.getContext().startActivity(intentFive);
                break;

            case 6 :
                Intent intentSix = new Intent(view.getContext() , PaperPerSubject.class);
                view.getContext().startActivity(intentSix);
                break;

            case 7 :
                Intent intentSeven = new Intent(view.getContext() , PaperPerSubject.class);
                view.getContext().startActivity(intentSeven);
                break;

            case 8 :
                Intent intentEight = new Intent(view.getContext() , PaperPerSubject.class);
                view.getContext().startActivity(intentEight);
                break;

            case 9 :
                Intent intentNine = new Intent(view.getContext() , PaperPerSubject.class);
                view.getContext().startActivity(intentNine);
                break;

            case 10 :
                Intent intentTen = new Intent(view.getContext() , PaperPerSubject.class);
                view.getContext().startActivity(intentTen);
                break;

            case 11 :
                Intent intentEleven = new Intent(view.getContext() , PaperPerSubject.class);
                view.getContext().startActivity(intentEleven);
                break;

            case 12 :
                Intent intentTwelve = new Intent(view.getContext() , PaperPerSubject.class);
                view.getContext().startActivity(intentTwelve);
                break;
        }
    }
}