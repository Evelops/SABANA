package com.example.sabana;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;

public class SelectConviMap extends AppCompatActivity {

    String API_key = "l7xx97cfc10b259149a78f176bd0fd0048c1";

    String selectStoreName;
    String selectAddress;
    double selectConviLat;
    double selectConviLon;

    // TMapView
    TMapView tMapView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_convi_map);

        /*
            intent.putExtra("selectStoreName", selectStoreName);
            intent.putExtra("selectAddress", selectAddress);
            intent.putExtra("selectConviLat", selectConviLat);
            intent.putExtra("selectConviLon", selectConviLon);
         */

        Intent intent = getIntent();

        selectStoreName = intent.getStringExtra("selectStoreName");
        selectAddress = intent.getStringExtra("selectAddress");
        selectConviLat = Double.parseDouble(intent.getStringExtra("selectConviLat"));
        selectConviLon = Double.parseDouble(intent.getStringExtra("selectConviLon"));

        Log.e("log", selectConviLat + " , " + selectConviLon);
        String message = "편의점이 선정되었습니다. : " + selectStoreName + "";

        tMapView = new TMapView(this);

        LinearLayout linearLayoutTmap = (LinearLayout)findViewById(R.id.linearLayoutTmap2);
        tMapView.setSKTMapApiKey(API_key);

        tMapView.setZoomLevel(19);
        tMapView.setIconVisibility(true);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
        tMapView.setCenterPoint(selectConviLon, selectConviLat);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.markerline_blue);

        TMapPoint selectTMapPoint = new TMapPoint(selectConviLat, selectConviLon);
        TMapMarkerItem mTMapMarkerItem = new TMapMarkerItem();

        mTMapMarkerItem.setIcon(bitmap);
        mTMapMarkerItem.setPosition(0.5f, 1.0f);
        mTMapMarkerItem.setTMapPoint(selectTMapPoint);
        mTMapMarkerItem.setName(selectStoreName);

        mTMapMarkerItem.setCanShowCallout(true);
        mTMapMarkerItem.setCalloutTitle(selectStoreName);
        mTMapMarkerItem.setCalloutSubTitle(selectAddress);
        mTMapMarkerItem.setAutoCalloutVisible(false);

        tMapView.addMarkerItem("select", mTMapMarkerItem);
        linearLayoutTmap.addView(tMapView);



    }
}