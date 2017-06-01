package edu.ucla.bruinfo;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Alfred Lucero on 5/25/2017.
 */

public class InfoListViewFragment extends Fragment {
    public static final String TAG = InfoListViewFragment.class.getName();
    private ListView mInfoListView;
    private InfoListViewAdapter mInfoListViewAdapter;
    private List<InfoListItem> mInfoListItems;
    private List<InfoListItem> mInfoListItemsTemp;
    private final Semaphore lock = new Semaphore(1);

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

        return mainView;
    }

    public void updateInfoListView(List<String> googleSearchURLs, List<String> imageURLs) {
        new ParseGoogleSearches(googleSearchURLs, imageURLs);
    }

    private class ParseGoogleSearches {
        private int mNumSearchQueries;
        private int mNumSearchQueriesFinished;

        public ParseGoogleSearches(List<String> googleSearchURLs, List<String> imageURLs) {
            this.mNumSearchQueries = googleSearchURLs.size();
            this.mNumSearchQueriesFinished = 0;

            //
            try {
                lock.acquire();
            } catch(InterruptedException e) {
                Log.e("Exception", e.toString());
            }

            Log.i(TAG, "After acquire lock");

            mInfoListItemsTemp = new ArrayList<InfoListItem>();

            for (int i = 0; i < this.mNumSearchQueries; i++) {
                String googleSearchURL = googleSearchURLs.get(i);
                String imageURL = imageURLs.get(i);
                //new ParseGoogleSearch(googleSearchURL, imageURL).execute();
                if(Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
                    new ParseGoogleSearch(googleSearchURL, imageURL).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    new ParseGoogleSearch(googleSearchURL, imageURL).execute();
                }
            }
        }

        // Helper asynchronous class that runs the Jsoup HTML parsing of Google Search Results
        // given a Google search URL
        private class ParseGoogleSearch extends AsyncTask<Void, Void, Void> {
            private String mGoogleSearchURL;
            private String mImageURL;
            private Elements mSearchResultLinks;
            private int mNumSearchResultLinks;

            public ParseGoogleSearch(String googleSearchURL, String imageURL) {
                this.mGoogleSearchURL = googleSearchURL;
                this.mImageURL = imageURL;
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
                for (int i = 0; i < mNumSearchResultLinks; i++) {
                    Element searchResultLink = mSearchResultLinks.get(i);
                    String linkURL = searchResultLink.attr("href");
                    String linkText = searchResultLink.text();

                    InfoListItem infoListItem = new InfoListItem(linkText, linkURL, mImageURL);
                    mInfoListItemsTemp.add(infoListItem);
                }

                // Keep track of the number of search queries finished
                synchronized(this) {
                    mNumSearchQueriesFinished++;

                    // After all search queries finished, set adapter with new mInfoListItems
                    if (mNumSearchQueriesFinished == mNumSearchQueries) {
                        //if (mInfoListViewAdapter == null) {

                        if(getActivity() == null)
                            return;

                        getActivity().runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                                              mInfoListItems = mInfoListItemsTemp;

                                              mInfoListViewAdapter = new InfoListViewAdapter(getActivity().getApplicationContext(),
                                                      R.layout.fragment_info_list_item, mInfoListItems);

                                              mInfoListView.setAdapter(mInfoListViewAdapter);

                                              Log.i(TAG, "Release lock");
                                              lock.release();
                                          }
                                      });

                    }
                }
            }
        }
    }
}
