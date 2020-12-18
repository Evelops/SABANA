package com.example.sabana;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ItemListAdapter extends BaseAdapter {

    Context mContext = null;
    LayoutInflater mLayoutInflater = null;
    List<ItemInfo> items;

    public ItemListAdapter(Context context, List<ItemInfo> items) {
        this.mContext = context;
        this.items = items;
        this.mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public ItemInfo getItem(int position) {
        return items.get(position);
    }

    @Override
    public View getView(int position, View converView, ViewGroup parent) {

        View view = mLayoutInflater.inflate(R.layout.activity_item, null);

        TextView product = view.findViewById(R.id.product);
        TextView saleType = view.findViewById(R.id.saleType);
        TextView price = view.findViewById(R.id.price);

        product.setText(items.get(position).getProduct());
        saleType.setText(items.get(position).getSaleTypeToString());
        price.setText(String.valueOf(items.get(position).getPrice()));

        return view;
    }


}
