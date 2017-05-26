package edu.ucla.bruinfo;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Alfred Lucero on 5/18/2017.
 */

public class InfoListViewAdapter extends ArrayAdapter<InfoListItem> {
    Context mContext;
    int mLayoutResourceId;
    List<InfoListItem> mInfoListItems;


    public InfoListViewAdapter(Context mContext, int mLayoutResourceId, List<InfoListItem> mInfoListItems) {
        super(mContext, mLayoutResourceId, mInfoListItems);
        this.mContext = mContext;
        this.mLayoutResourceId = mLayoutResourceId;
        this.mInfoListItems = mInfoListItems;
    }

    @Override
    public InfoListItem getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View infoListItem = convertView;
        InfoListItemHolder infoListItemHolder = null;

        // If we currently do not have an infoListItem View to reuse,
        // create a new infoListItem View
        if (infoListItem == null) {
            // Inflate the layout for a single info list item
            LayoutInflater inflater = LayoutInflater.from(mContext);
            infoListItem = inflater.inflate(mLayoutResourceId, parent, false);

            // Get a reference to the different view elements we wish to update in our holder
            infoListItemHolder = new InfoListItemHolder();
            infoListItemHolder.mLinkTextView = (TextView) infoListItem.findViewById(R.id.linkText);
            infoListItemHolder.mLinkURLView = (TextView) infoListItem.findViewById(R.id.linkURL);
            infoListItemHolder.mLinkImageView = (ImageView) infoListItem.findViewById(R.id.linkImage);

            infoListItem.setTag(infoListItemHolder);
        } else {
            // Otherwise, use an existing infoListItem View
            infoListItemHolder = (InfoListItemHolder) infoListItem.getTag();
        }

        // Set the proper link text, URL, and image
        InfoListItem infoListItemData = mInfoListItems.get(position);
        infoListItemHolder.mLinkTextView.setText(infoListItemData.mLinkText);
        infoListItemHolder.mLinkURLView.setText(infoListItemData.mLinkURL);

        final ImageView imageView = infoListItemHolder.mLinkImageView;
        Picasso.with(mContext)
                .load("https://java.sogeti.nl/JavaBlog/wp-content/uploads/2009/04/android_icon_256.png")
                .placeholder(R.drawable.ic_sync_black_24dp)
                .error(R.drawable.ic_sync_problem_black_24dp)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(mContext)
                                .load("https://java.sogeti.nl/JavaBlog/wp-content/uploads/2009/04/android_icon_256.png")
                                .error(R.drawable.ic_sync_problem_black_24dp)
                                .into(imageView, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Log.v("Picasso","Could not fetch image");
                                    }
                                });
                    }
                });

/*        if (infoListItemData.mLinkImage != "") {
            int resId = mContext.getResources().getIdentifier(infoListItemData.mLinkImage, "mipmap", mContext.getPackageName());
            infoListItemHolder.mLinkImageView.setImageResource(resId);
        }*/

        return infoListItem;
    }

    private static class InfoListItemHolder {
        TextView mLinkTextView;
        TextView mLinkURLView;
        ImageView mLinkImageView;
    }
}

