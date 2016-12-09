package com.vu.viet.iceapp;


import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class CustomList extends ArrayAdapter<Contact> {

    private final Activity context;
    private ArrayList<Contact> contact_item;
    private final Integer imageId;

    CustomList(Activity context,
               ArrayList<Contact> contact_item, Integer imageId) {
        super(context, R.layout.single_contactview, contact_item);
        this.context = context;
        this.contact_item = contact_item;
        this.imageId = imageId;

    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.single_contactview, parent, false);

        TextView nameView = (TextView) rowView.findViewById(R.id.name);
        TextView numberView = (TextView) rowView.findViewById(R.id.phone_number);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);

        nameView.setText(contact_item.get(position).getName());
        numberView.setText(contact_item.get(position).getPhone_number());
        imageView.setImageResource(imageId);

        return rowView;
    }
}