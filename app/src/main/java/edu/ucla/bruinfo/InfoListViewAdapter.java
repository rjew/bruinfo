package edu.ucla.bruinfo;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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

        // Inflate the layout for a single info list item
        LayoutInflater inflater = LayoutInflater.from(mContext);
        infoListItem = inflater.inflate(mLayoutResourceId, parent, false);

        // Get a reference to the different view elements we wish to update
        TextView linkTextView = (TextView) infoListItem.findViewById(R.id.linkText);
        TextView linkURLView = (TextView) infoListItem.findViewById(R.id.linkHref);

        // Set the proper link text and URL
        InfoListItem infoListItemData = mInfoListItems.get(position);
        linkTextView.setText(infoListItemData.mLinkText);
        linkURLView.setText(infoListItemData.mLinkURL);

        return infoListItem;
    }
}

