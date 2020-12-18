package com.example.sabana;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {

    // TMap API Key
    String API_key = "l7xx97cfc10b259149a78f176bd0fd0048c1";

    // TMapView
    // TMapGPSManager
    TMapView tMapView = null;
    TMapGpsManager tMapGPS = null;

    boolean isFirst = false;

    // Convenience Store Database
    // Build by SQLite
    // Loc : assets - ConvDB.db
    public List<ConvenienceStore> convenienceStoreList;

    // RealTime Database
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference normalSearchRef;
    DatabaseReference quickSearchRef;

    ArrayList<String> chatNameList = new ArrayList<>();
    //ArrayList<ChatInfo> chatInfoList = new ArrayList<>();

    final String NORM_SEARCH = "NORMAL_SEARCH";
    final String QUICK_SEARCH = "QUICK_SEARCH";

    double nowLatitude = 0;
    double nowLongitude = 0;

    double tempLatitude = 37.4504394337849;
    double tempLongitude = 126.65522606913497;

    int memberNum = 0;
    int limitNum = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // * Tmap View * //

        tMapView = new TMapView(this);

        // Linear Layout Class
        LinearLayout linearLayoutTmap = (LinearLayout)findViewById(R.id.linearLayoutTmap);

        // API Key
        tMapView.setSKTMapApiKey(API_key);

        // Initial Setting of TMap View
        tMapView.setZoomLevel(17);
        tMapView.setIconVisibility(true);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
        tMapView.setLocationPoint(tempLongitude, tempLatitude);
        tMapView.setCenterPoint(tempLongitude, tempLatitude);

        // * Marker * //

        // Load Database
        initLoadDatabase();
        initLoadItemListDataBase();

        // View Multiple Marker
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.markerline_yellow);
        for (int i = 0; i < convenienceStoreList.size(); i++){

            String tempStr = convenienceStoreList.get(i).storeName;
            String tempAdr = convenienceStoreList.get(i).address;
            double tempLat = convenienceStoreList.get(i).latitude;
            double tempLon = convenienceStoreList.get(i).longitude;

            // Location of Conve Store
            TMapPoint tempTMapPoint = new TMapPoint(tempLat, tempLon);

            // TMap Marker Class
            TMapMarkerItem mTMapMarkerItem = new TMapMarkerItem();

            // Initial Setting of Marker
            mTMapMarkerItem.setIcon(bitmap);
            mTMapMarkerItem.setPosition(0.5f, 1.0f); // Set Center of Marker's Location
            mTMapMarkerItem.setTMapPoint(tempTMapPoint); // Location of Coord
            mTMapMarkerItem.setName(tempStr);

            // Use Balloon View
            mTMapMarkerItem.setCanShowCallout(true);
            mTMapMarkerItem.setCalloutTitle(tempStr); // Main Message
            mTMapMarkerItem.setCalloutSubTitle(tempAdr); // Sub Message
            mTMapMarkerItem.setAutoCalloutVisible(false);

            // TMap View
            tMapView.addMarkerItem("conviStoreLoc" + i, mTMapMarkerItem); // to create multiple marker -> use _id + i

        }

        // TMap View
        linearLayoutTmap.addView(tMapView);

        // * GPS * //
        // GPS with T Map
        tMapGPS = new TMapGpsManager(this);

        // Initial Setting
        tMapGPS.setMinTime(1000);
        tMapGPS.setMinDistance(10);
        tMapGPS.setProvider(tMapGPS.NETWORK_PROVIDER);
        // tMapGPS.setProvider(tMapGPS.GPS_PROVIDER);

        // Request For permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        tMapGPS.OpenGps();

        // Chat List 긁어오기
        // Chat NORM_SEARCH

        normalSearchRef = firebaseDatabase.getReference("chat").child(NORM_SEARCH);

        normalSearchRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                String temp = snapshot.getKey();
                chatNameList.add(temp);

                // child
                final DatabaseReference normalSearchRefChild = normalSearchRef.child(temp).child("INFO");

                //Log.e("log", "why...? : " + temp);

                final ArrayList<ChatInfo> chatInfoList = new ArrayList<>();

                normalSearchRefChild.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        ChatInfo getChatInfo = snapshot.getValue(ChatInfo.class);
                        chatInfoList.add(getChatInfo);

                        //Log.e("log", "prev...? : " + previousChildName);

                        // Log.e("please i beg u : chat admin : ", chatInfoList.get(0).admin);
                        // Log.e("another beg : chat name : ", chatInfoList.get(0).chatName);

                        // key -> chat list name

                        Bitmap chatListBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.markerline_green);

                        double tempLat = Double.parseDouble(chatInfoList.get(0).lat);
                        double tempLon = Double.parseDouble(chatInfoList.get(0).lon);
                        String tempChatName = chatInfoList.get(0).chatName;
                        String tempAdminName = chatInfoList.get(0).admin;

                        TMapPoint chatListTMapPoint = new TMapPoint(tempLat, tempLon);

                        TMapMarkerItem chatListTMapMarkerItem = new TMapMarkerItem();

                        chatListTMapMarkerItem.setIcon(chatListBitmap);
                        chatListTMapMarkerItem.setPosition(0.5f, 1.0f);
                        chatListTMapMarkerItem.setTMapPoint(chatListTMapPoint);
                        chatListTMapMarkerItem.setName(tempChatName);

                        chatListTMapMarkerItem.setCanShowCallout(true);
                        chatListTMapMarkerItem.setCalloutTitle(tempChatName);
                        chatListTMapMarkerItem.setCalloutSubTitle(tempAdminName);
                        chatListTMapMarkerItem.setAutoCalloutVisible(false);

                        tMapView.addMarkerItem("chatList" + tempChatName, chatListTMapMarkerItem);

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    

    // to Control Real-time GPS
    @Override
    public void onLocationChange(Location location) {
        tMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
        tMapView.setCenterPoint(location.getLongitude(), location.getLatitude());

        nowLatitude = location.getLatitude();
        nowLongitude = location.getLongitude();

    }


    // * Database * //
    // Load Database
    private void initLoadDatabase(){

        // DataAdapter -> DB Helper
        DataAdapter mDBHelper = new DataAdapter(getApplicationContext());
        mDBHelper.CreateDatabase();
        mDBHelper.OpenDatabase();

        convenienceStoreList = mDBHelper.getTableData();

        mDBHelper.close();
    }

    private void initLoadItemListDataBase(){

        // DataAdapter -> DB Helper
        ItemListDBHelper dbHelper = new ItemListDBHelper(getApplicationContext());
        dbHelper.CreateDatabase();
        dbHelper.openDataBase();

        // itemList = dbHelper.getItemList("CU");

        dbHelper.close();

    }

    //johnxx1
    public void onButtonSearchClick(View view){
        Intent intent = new Intent(this, ChatListActivity.class);
        intent.putExtra("nowLatitude", nowLatitude);
        intent.putExtra("nowLongitude", nowLongitude);
        startActivity(intent);
    }

    public void onButtonConvClick(View view){

        Intent intent = new Intent(this, ConviListActivity.class);
        startActivity(intent);
    }


    public void onButtonQuickStartClick(View view){

        String franchiseName = "CU";
        String productName = "생수";
        int chatListNum = 0;
        int saleMemberNum = 3; // Sale 1+1, 또는 2+1

        // "구현필요" 지정 숫자가 넘으면, 다른 채팅방으로 옮기기

        String chatName = franchiseName + "_" + productName + "_" + chatListNum;

        DatabaseReference memberRef;
        final DatabaseReference selectRef;
        final DatabaseReference limitRef;

        firebaseDatabase = FirebaseDatabase.getInstance();
        memberRef = firebaseDatabase.getReference("chat").child(QUICK_SEARCH).child(chatName).child("MEMBER");
        limitRef = firebaseDatabase.getReference("chat").child(QUICK_SEARCH).child(chatName).child("LIMIT");
        selectRef = firebaseDatabase.getReference("chat").child(QUICK_SEARCH).child(chatName).child("SELECT");

        // Limit
        memberNum = 0;

        memberRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                memberNum++;

                String temp = snapshot.getKey();
                Log.e("log", memberNum + " " + temp);

                if(memberNum >= limitNum){
                    limitRef.child("LIMIT_FLAG").setValue(true);
                    QuickSearch.limitFlag = true;
                }
                else{
                    limitRef.child("LIMIT_FLAG").setValue(false);
                    //selectRef.child("SELECT_FLAG").setValue(false);
                    //selectRef.child("SELECT_STORE").setValue("NULL");
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference selectFlagRef = selectRef.child("SELECT_FLAG");

        selectFlagRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Log.e("memberNum", memberNum + "");
                if (memberNum == 1){
                    selectRef.child("SELECT_FLAG").setValue(false);
                    selectRef.child("SELECT_STORE").setValue("NULL");
                    selectRef.child("SELECT_STOREID").setValue(-1);
                }

                try
                {
                    sleep(10);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }


                String temp = snapshot.getValue().toString();

                if (temp.equals("false")){
                    QuickSearch.selectFlag = false;
                    Log.e("selectFlagRef", temp);
                }
                else {
                    QuickSearch.selectFlag = true;
                    Log.e("selectFlagRef", temp);

                    DatabaseReference selectStoreIDRef = selectRef.child("SELECT_STOREID");

                    selectStoreIDRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            String storeID = snapshot.getValue().toString();
                            Log.e("selectStoreID", storeID);
                            QuickSearch.selectConvi_id = Integer.parseInt(storeID);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        setQuickSearch(chatName);

        try
        {
            sleep(1000);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, ChatActivity2.class);
        intent.putExtra("franchiseName", franchiseName);
        intent.putExtra("productName", productName);
        intent.putExtra("chatListNum", Integer.toString(chatListNum));
        intent.putExtra("saleMemberNum", Integer.toString(saleMemberNum));
        startActivity(intent);

    }

    public void setQuickSearch(String chatName){

        // nickname 으로 입력
        String nickName = Profile.nickName;

        String limitTime = "1000";
        String lat = Double.toString(Profile.locLatitude);
        String lon = Double.toString(Profile.locLongitude);

        quickSearchRef = firebaseDatabase.getReference("chat").child(QUICK_SEARCH).child(chatName).child("MEMBER").child(nickName);

        quickSearchRef.child("nickName").setValue(nickName);
        quickSearchRef.child("limitTime").setValue(limitTime);
        quickSearchRef.child("lat").setValue(lat);
        quickSearchRef.child("lon").setValue(lon);

    }

    public void onButtonLocSetClick(View view){

        Profile.locLatitude = nowLatitude;
        Profile.locLongitude = nowLongitude;

        String message = "위치를 ( " + nowLatitude + " , " + nowLongitude + " ) 으로 갱신했습니다.";

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

        // 위치 갱신
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference profileRef = firebaseDatabase.getReference("profiles");

        profileRef.child(Profile.nickName).child("lon").setValue(Profile.locLongitude);
        profileRef.child(Profile.nickName).child("lat").setValue(Profile.locLatitude);

    }

    public void onButtonpyonyClick(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://pyony.com/"));
        startActivity(intent);
    }




}


