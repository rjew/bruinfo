package edu.ucla.bruinfo;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String googleSearchURL = "https://google.com/search?q=Royce+Hall";

        Log.d("URL: ", googleSearchURL);
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
//            mProgressDialog = new ProgressDialog(MainActivity.this);
//            mProgressDialogg.setTitle("Android Basic JSoup Tutorial");
//            mProgressDialog.setMessage("Loading...");
//            mProgressDialog.setIndeterminate(false);
//            mProgressDialog.show();
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
            for (int i = 0; i < this.numSearchResultLinks; i++) {
                Element searchResultLink = this.searchResultLinks.get(i);
                String linkHref = searchResultLink.attr("href");
                String linkText = searchResultLink.text();

                Log.v("linkHref: ", linkHref);
                Log.v("linkText: ", linkText);
            }
            // Set title into TextView
           // TextViewTextViewTextView txttitle = (TextView) findViewById(R.id.titletxt);
//            txttitle.setText(title);
//            mProgressDialog.dismiss();
        }
    }
}
