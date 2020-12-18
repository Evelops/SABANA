package com.example.sabana;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.storage.FirebaseStorage;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;


import org.w3c.dom.Text;

public class FirstActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {

    EditText etName;

    boolean isFirst = true;
    boolean isChanged = false;

    TMapGpsManager tMapGPS = null;
    TMapData tMapData = new TMapData();

    Handler handler = new Handler();
    String address;

    // Test Setting

    // 1. 인하대학교 4호관
//     double tempLatitude = 37.4504394337849;
//     double tempLongitude = 126.65522606913497;

    // 2. 인하대학교 5호관
    double tempLatitude = 37.4518826300358;
    double tempLongitude = 126.65284022807845;

    // 3. 스타벅스
//    double tempLatitude = 37.451819861916455;
//    double tempLongitude = 126.65500512227067;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        // Initial Setting
        Profile.locLatitude = 0;
        Profile.locLongitude = 0;

        // GPS 설정
        // Request For permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // * GPS * //
        // GPS with T Map
        tMapGPS = new TMapGpsManager(this);

        // Initial Setting
        tMapGPS.setMinTime(1000);
        tMapGPS.setMinDistance(10);
        tMapGPS.setProvider(tMapGPS.NETWORK_PROVIDER);
        // tMapGPS.setProvider(tMapGPS.GPS_PROVIDER);

        tMapGPS.OpenGps();

        // Firebase
        etName = findViewById(R.id.et_name);

        loadData();
        if (Profile.nickName != null) {
            etName.setText(Profile.nickName);
            isFirst = false;
        }

    }

    public void onButtonStartClick(View view){

        // Old Code
        etName = findViewById(R.id.et_name);

        if (Profile.nickName != etName.toString()) {
            isChanged = true;
        }

        if(!isChanged && !isFirst){
            // Chat Activity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        else {
            saveData();

            // 저장 완료, Chat Activity로 전환
            Intent intent = new Intent(FirstActivity.this, MainActivity.class);
            startActivity(intent);
        }

    }


    void saveData(){
        Profile.nickName = etName.getText().toString();

        // 1. Firebase Database에 nickName 저장
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference profileRef = firebaseDatabase.getReference("profiles");

        // 닉네임을 key 식별자로 하고, 닉네임을 값으로 저장
        profileRef.child(Profile.nickName).child("id").setValue(Profile.nickName);
        profileRef.child(Profile.nickName).child("lon").setValue(Profile.locLongitude);
        profileRef.child(Profile.nickName).child("lat").setValue(Profile.locLatitude);
        profileRef.child(Profile.nickName).child("token").setValue( FirebaseInstanceId.getInstance().getToken());
        //FCM 토큰 저장
        // 2. Phone에 NickName 저장
        SharedPreferences preferences = getSharedPreferences("account", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("nickName", Profile.nickName);
        editor.putString("latitude", Double.toString(Profile.locLatitude));
        editor.putString("longitude", Double.toString(Profile.locLongitude));
        editor.commit();

        Log.d("locLongitude", Double.toString(Profile.locLatitude) );
        Log.d("locLatitude", Double.toString(Profile.locLongitude) );

    }

    void loadData(){

        SharedPreferences preferences = getSharedPreferences("account", MODE_PRIVATE);
        Profile.nickName = preferences.getString("nickName", null);

    }
    public void onButtonLocClick(View view){

        Profile.locLatitude = tempLatitude;
        Profile.locLongitude = tempLongitude;

        String message = Double.toString(Profile.locLatitude) + " , " + Double.toString(Profile.locLongitude);

        TextView text = (TextView)findViewById(R.id.textView);
        text.setText(message);

        saveData();

        // 11.23 수정 필요 -> 왜인지 리버스 지오코딩이 안먹힘

//        tMapData.convertGpsToAddress(tempLatitude, tempLongitude, new TMapData.ConvertGPSToAddressListenerCallback() {
//            @Override
//            public void onConvertToGPSToAddress(String s) {
//                address = s;
//
//                Log.d("s", s);
//                Log.d("address", address);
//
//            }
//        });
//
//        Toast.makeText(FirstActivity.this, "주소 : " + address, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onLocationChange(Location location) {

        tempLatitude = location.getLatitude();
        tempLongitude = location.getLongitude();

    }


}

