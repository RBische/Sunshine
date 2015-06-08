package fr.bischof.raphael.sunshine;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    private TextView mTvTitle;

    public DetailActivityFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detail, container, false);
        this.mTvTitle =(TextView)v.findViewById(R.id.tvTitle);
        return v;
    }

    public void fillTextView(String stringExtra) {
        mTvTitle.setText(stringExtra);
    }
}
