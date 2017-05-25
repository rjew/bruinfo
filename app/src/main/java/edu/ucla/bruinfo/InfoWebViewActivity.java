package edu.ucla.bruinfo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class InfoWebViewActivity extends AppCompatActivity {

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_web_view);

        mWebView = (WebView) findViewById(R.id.infoWebView);

        // Retrieve the Google search result link from the InfoListItem
        // from the intent extra passed from MainActivity
        Intent intent = getIntent();
        String linkURL = intent.getStringExtra("linkURL");

        // Load the retrieved link URL in the infoWebView
        mWebView.loadUrl(linkURL);
    }
}
