package fr.bischof.raphael.sunshine.customviews;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;

import fr.bischof.raphael.sunshine.R;

/**
 * Created by biche on 08/08/2015.
 */
public class LocationEditTextPreference extends EditTextPreference {
    private static final int DEFAULT_MINIMUM_LOCATION_LENGTH = 0;
    private int mMinLength;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LocationEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.LocationEditTextPreference,0,0);
        try{
            mMinLength = a.getInteger(R.styleable.LocationEditTextPreference_minLength,DEFAULT_MINIMUM_LOCATION_LENGTH);
        }finally {
            a.recycle();
        }
    }

    public LocationEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.LocationEditTextPreference,0,0);
        try{
            mMinLength = a.getInteger(R.styleable.LocationEditTextPreference_minLength,DEFAULT_MINIMUM_LOCATION_LENGTH);
        }finally {
            a.recycle();
        }
    }

    public LocationEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.LocationEditTextPreference,0,0);
        try{
            mMinLength = a.getInteger(R.styleable.LocationEditTextPreference_minLength,DEFAULT_MINIMUM_LOCATION_LENGTH);
        }finally {
            a.recycle();
        }
    }

    public LocationEditTextPreference(Context context) {
        super(context);
    }


    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        EditText text = getEditText();
        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (getDialog()instanceof AlertDialog){
                    AlertDialog dialog = (AlertDialog) getDialog();
                    Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    // Check if the EditText is empty
                    if (s.length() < mMinLength) {
                        // Disable OK button
                        positiveButton.setEnabled(false);
                    } else {
                        // Re-enable the button.
                        positiveButton.setEnabled(true);
                    }
                }
            }
        });
    }
}
