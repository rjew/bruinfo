package edu.ucla.bruinfo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }
        });
    }
}
