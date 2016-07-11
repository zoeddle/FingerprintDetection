package com.example.carola.fingerprintdetection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Carola on 11.07.16.
 */
public class NodeAdapter extends ArrayAdapter<Node> {
    public NodeAdapter(Context context, ArrayList<Node> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Node user = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_listview, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.tv_listIdName);
        TextView tvHome = (TextView) convertView.findViewById(R.id.tv_listSearchName);
        // Populate the data into the template view using the data object
        tvName.setText(user.name);
        tvHome.setText(user.searchName);
        // Return the completed view to render on screen
        return convertView;
    }
}
