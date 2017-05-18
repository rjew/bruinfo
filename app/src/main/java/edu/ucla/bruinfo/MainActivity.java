package edu.ucla.bruinfo;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView mInfoListView;
    private InfoListViewAdapter mInfoListViewAdapter;
    private List<InfoListItem> mInfoListItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_list_view);

        mInfoListView = (ListView) findViewById(R.id.infoListView);

        mInfoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v("LINK", "Hello from " + position);
            }
        });
        String googleSearchURL = "https://google.com/search?q=Royce+Hall";

        Log.d("URL: ", googleSearchURL);
        mInfoListItems = new ArrayList<InfoListItem>();
        new ParseGoogleSearch(googleSearchURL).execute();

    }

    // ParseGoogleSearch AsyncTask
    private class ParseGoogleSearch extends AsyncTask<Void, Void, Void> {
        String googleSearchURL;
        Elements searchResultLinks;
        int numSearchResultLinks;

        public ParseGoogleSearch(String googleSearchURL) {
            this.googleSearchURL = googleSearchURL;
            this.numSearchResultLinks = 0;
            this.searchResultLinks = null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Retrieve Google search results HTML document to be parsed
                Document doc = Jsoup.connect(googleSearchURL).get();

                // Parse out the search result links to populate info ListView
                this.searchResultLinks = doc.select("h3.r > a");
                this.numSearchResultLinks = this.searchResultLinks.size();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Retrieve URL links and descriptions of Google search results
            // Add an infoListItem for each text/URL combo
            for (int i = 0; i < this.numSearchResultLinks; i++) {
                Element searchResultLink = this.searchResultLinks.get(i);
                String linkURL = searchResultLink.attr("href");
                String linkText = searchResultLink.text();


                InfoListItem infoListItem = new InfoListItem(linkText, linkURL);
                mInfoListItems.add(infoListItem);
            }

            // Set the infoListViewAdapter with all the infoListViewItems
            mInfoListViewAdapter = new InfoListViewAdapter(getApplicationContext(),
                    R.layout.info_list_item, mInfoListItems);

            if (mInfoListView != null) {
                mInfoListView.setAdapter(mInfoListViewAdapter);
            }
        }
    }
}
