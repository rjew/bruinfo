package edu.ucla.bruinfo;


import android.support.v4.app.FragmentActivity;
import android.os.Bundle;


public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            // TODO: load Google Maps Fragment

            // Load InfoListViewFragment
            // TODO: pass in Google Places into the InfoListView
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.infoListViewFragment, new InfoListViewFragment())
                    .commit();
        }
    }
}
