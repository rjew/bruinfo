package edu.ucla.bruinfo;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alfred Lucero on 5/25/2017.
 */

public class InfoListViewFragment extends Fragment {
    private ListView mInfoListView;
    private InfoListViewAdapter mInfoListViewAdapter;
    private List<InfoListItem> mInfoListItems;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_info_list_view, container, false);

        mInfoListView = (ListView) mainView.findViewById(R.id.infoListView);

        // On click of an infoListItem, open up a WebView to load the corresponding
        // Google search result link passed to another InfoWebView activity by an intent extra
        mInfoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String linkURL = mInfoListItems.get(position).mLinkURL;

                Intent intent = new Intent(getActivity().getApplicationContext(), InfoWebViewActivity.class);
                intent.putExtra("linkURL", linkURL);
                startActivity(intent);
            }
        });

        // Parse Google search results based on places around the user
        // and fill the InfoListView with related links
        String googleSearchURL = "https://google.com/search?q=Royce+Hall";
        mInfoListItems = new ArrayList<InfoListItem>();
        new ParseGoogleSearch(googleSearchURL).execute();

        return mainView;
    }

    // Helper asynchronous class that runs the Jsoup HTML parsing of Google Search Results
    // given a Google search URL
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


                InfoListItem infoListItem = new InfoListItem(linkText, linkURL, "");
                mInfoListItems.add(infoListItem);
            }

            // Set the infoListViewAdapter with all the infoListViewItems
            mInfoListViewAdapter = new InfoListViewAdapter(getActivity().getApplicationContext(),
                    R.layout.fragment_info_list_item, mInfoListItems);

            if (mInfoListView != null) {
                mInfoListView.setAdapter(mInfoListViewAdapter);
            }
        }
    }
}
