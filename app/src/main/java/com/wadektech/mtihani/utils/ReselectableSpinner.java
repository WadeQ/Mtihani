package com.wadektech.mtihani.utils;

import android.content.Context;
import android.util.AttributeSet;

import java.util.Objects;

public class ReselectableSpinner extends androidx.appcompat.widget.AppCompatSpinner {
    public ReselectableSpinner(Context context) {
        super(context);
    }

    public ReselectableSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ReselectableSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setSelection(int position, boolean animate) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position, animate);
        if (sameSelected) {
            // Spinner does not call the OnItemSelectedListener if the same item is selected, so do it manually now
            // Timber.d("setSelection advanced called");

            Objects.requireNonNull(getOnItemSelectedListener()).onItemSelected(this, getSelectedView(), position, getSelectedItemId());

        }
    }

    @Override
    public void setSelection(int position) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position);
        if (sameSelected) {
            // Timber.d("setSelection called");
            // Spinner does not call the OnItemSelectedListener if the same item is selected, so do it manually now
            Objects.requireNonNull(getOnItemSelectedListener()).onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }
}
