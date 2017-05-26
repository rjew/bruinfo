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
import java.util.concurrent.CountDownLatch;

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

        mInfoListItems = new ArrayList<InfoListItem>();
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

        ArrayList<String> googleSearchURLs = new ArrayList<String>();
        googleSearchURLs.add("https://www.google.com/search?q=Royce+Hall");
        googleSearchURLs.add("https://www.google.com/search?q=Powell+Libary");
        updateInfoListView(googleSearchURLs);

        return mainView;
    }

    public void updateInfoListView(ArrayList<String> googleSearchURLs) {
        new ParseGoogleSearches(googleSearchURLs);
    }

    private class ParseGoogleSearches {
        private int mNumSearchQueries;
        private int mNumSearchQueriesFinished;

        public ParseGoogleSearches(ArrayList<String> googleSearchURLs) {
            this.mNumSearchQueries = googleSearchURLs.size();
            this.mNumSearchQueriesFinished = 0;

            // Clear the old InfoListItems
            mInfoListItems.clear();

            for (int i = 0; i < this.mNumSearchQueries; i++) {
                String googleSearchURL = googleSearchURLs.get(i);
                new ParseGoogleSearch(googleSearchURL).execute();
            }
        }

        // Helper asynchronous class that runs the Jsoup HTML parsing of Google Search Results
        // given a Google search URL
        private class ParseGoogleSearch extends AsyncTask<Void, Void, Void> {
            private String mGoogleSearchURL;
            private Elements mSearchResultLinks;
            private int mNumSearchResultLinks;


            public ParseGoogleSearch(String googleSearchURL) {
                this.mGoogleSearchURL = googleSearchURL;
                this.mNumSearchResultLinks = 0;
                this.mSearchResultLinks = null;
            }

            public void setGoogleSearchURL(String googleSearchURL) {
                this.mGoogleSearchURL = googleSearchURL;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    // Retrieve Google search results HTML document to be parsed
                    Document doc = Jsoup.connect(this.mGoogleSearchURL).get();

                    // Parse out the search result links to populate info ListView
                    this.mSearchResultLinks = doc.select("h3.r > a");
                    this.mNumSearchResultLinks = this.mSearchResultLinks.size();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                // Retrieve URL links and descriptions of Google search results
                // Add an infoListItem for each text/URL combo
                for (int i = 0; i < this.mNumSearchResultLinks; i++) {
                    Element searchResultLink = this.mSearchResultLinks.get(i);
                    String linkURL = searchResultLink.attr("href");
                    String linkText = searchResultLink.text();

                    InfoListItem infoListItem = new InfoListItem(linkText, linkURL, "");
                    mInfoListItems.add(infoListItem);
                }

                // Keep track of the number of search queries finished
                synchronized(this) {
                    mNumSearchQueriesFinished++;

                    // After all search queries finished, set adapter with new mInfoListItems
                    if (mNumSearchQueriesFinished == mNumSearchQueries) {
                        mInfoListViewAdapter = new InfoListViewAdapter(getActivity().getApplicationContext(),
                                R.layout.fragment_info_list_item, mInfoListItems);

                        if (mInfoListView != null) {
                            mInfoListView.setAdapter(mInfoListViewAdapter);
                        }
                    }
                }
            }
        }
    }
}
