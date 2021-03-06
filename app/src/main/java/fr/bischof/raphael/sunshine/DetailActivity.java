package fr.bischof.raphael.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (savedInstanceState == null) {

            // The detail Activity called via intent.  Inspect the intent for forecast data.
            Intent intent = getIntent();
            if (intent != null) {
                Bundle args = new Bundle();
                args.putBoolean(DetailFragment.DETAIL_TRANSITION_ANIMATION, true);
                args.putParcelable(DetailFragment.DETAIL_URI, intent.getData());
                DetailFragment fragment = new DetailFragment();
                fragment.setArguments(args);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment, fragment).commit();
                supportPostponeEnterTransition();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
