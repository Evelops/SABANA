package com.example.sabana;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.sabana.fcmsendservice.FcmSend;
import com.example.sabana.fcmsendservice.RestSendCallService;
import com.example.sabana.fcmsendservice.SendResult;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static java.lang.Thread.sleep;

public class ChatListActivity extends AppCompatActivity {

    ListView chat_list;
    EditText user_chat;
    String chatName;
    double nowLatitude;
    double nowLongitude;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference();
    DatabaseReference chatInfoRef;

    final String NORM_SEARCH = "NORMAL_SEARCH";
    final String QUCICK_SEARCH = "QUICK_SEARCH";

    final String API_CALL_URL = "https://fcm.googleapis.com";

    ArrayList<ChatInfo> chatInfoList = new ArrayList<>();
    ArrayList<String> chatNameList = new ArrayList<>();

    ChatInfo adminChatInfo;
    String intent_admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatlist);

        getSupportActionBar().hide();

        chat_list = (ListView) findViewById(R.id.chat_list);
        user_chat = (EditText) findViewById(R.id.user_chat);

        //현재 위치
        //위도
        nowLatitude = getIntent().getDoubleExtra("nowLatitude", 0);
        //경도
        nowLongitude = getIntent().getDoubleExtra("nowLongitude", 0);

        showChatList();

        try
        {
            sleep(500);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        chat_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                chatName = chatNameList.get(position);
                Log.e("Chatname", chatName);
                nextChat();

            }
        });


    }

    public void onButtonNextClick(View view){
        if (user_chat.getText().toString().equals("")) return;

        chatName = user_chat.getText().toString();
        setChatInfo();

        String message = "현재 위치 (" + Profile.locLatitude + " , " + Profile.locLongitude + ") 로 새로운 채팅방 " + chatName + " 이 개설됩니다. ";
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

        //주변 사용자에게 Push
        pushNotify(chatName);

        try
        {
            sleep(500);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        nextChat();
    }

    public void nextChat(){

//        chatName = user_chat.getText().toString();
//        setChatInfo();
//        //주변 사용자에게 Push
//        pushNotify(chatName);

        Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
        intent.putExtra("chatName", chatName);

        if (chatInfoList.size() > 0){
            intent_admin = chatInfoList.get(0).admin;
            Log.e("intent_admin", intent_admin);
            intent.putExtra("admin", intent_admin);
        }

        startActivity(intent);

    }

    private void showChatList(){

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        chat_list.setAdapter(adapter);

        databaseReference.child("chat").child(NORM_SEARCH).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.e("log", "dataSnapshot.getKey() : " + snapshot.getKey());
                adapter.add(snapshot.getKey());
                chatNameList.add(snapshot.getKey());
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

    public void setChatInfo(){

        chatInfoRef = firebaseDatabase.getReference("chat").child(NORM_SEARCH).child(chatName).child("INFO");

        //firebase DB에 저장할 값들  // admin, distance, time, lat, lon
        String admin = Profile.nickName;
        String distance = "1000";
        String lat = Double.toString(Profile.locLatitude);
        String lon = Double.toString(Profile.locLongitude);

        //메세지 작성 시간 문자열로..
        Calendar calendar= Calendar.getInstance(); //현재 시간을 가지고 있는 객체
        String time = calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE); //14:16

        // stack 쌓아서 0으로 접근
        ChatInfo chatinfo = new ChatInfo(chatName, admin, distance, time, lat, lon);
        chatInfoRef.push().setValue(chatinfo);

        chatInfoRef = firebaseDatabase.getReference("chat").child(NORM_SEARCH).child(chatName).child("INFO");
        chatInfoRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                ChatInfo getChatInfo = snapshot.getValue(ChatInfo.class);
                chatInfoList.add(getChatInfo);
//
//                adminChatInfo = chatInfoList.get(0);
//                intent_admin = adminChatInfo.admin;
//
//                Log.e("admin", adminChatInfo.admin);
//                Log.d("admin", adminChatInfo.lat);
//                Log.d("admin", adminChatInfo.lon);
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

    private void pushNotify(String chatName) {

        final String AUTHORIZATION = "key=AAAAmWU-3hY:APA91bEzYRwTbe9TGiF8TK3JYgfHwaMZ_REdbFDmx" +
                "t-InG6Cpg8N0xvi5asQOLGNNkdCI1b7VcQA66UVvxgzQe4yC5coqqNQobttHPpEjkiuVkO" +
                "WUz-uZZN52QNmr9tm38fuBTX9MUNK";

        final String REFER_PROFILE  = "profiles";
        final String TITLE = "반경 500m에 새로운 방이 개설 되었습니다.";
        final String BODY = " : " + chatName;

        firebaseDatabase.getReference(REFER_PROFILE).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                LoginUser user = snapshot.getValue(LoginUser.class);
                if(user != null) {

                    Location nowLocation = generateLocation(nowLatitude, nowLongitude);
                    Location otherLocation = generateLocation(user.getLat(), user.getLon());

                    double distance = getDistance(nowLocation, otherLocation);

                    if (distance <= 500 && user.getToken() != null && !user.getToken().equals(FirebaseInstanceId.getInstance().getToken())) {
                        FcmSend fcmSend = new FcmSend(user.getToken(), new FcmSend.SendData(TITLE, BODY));
                        RestSendCallService sendService = getRestSendCallService();
                        Call<SendResult> call = sendService.send(AUTHORIZATION, fcmSend);

                        call.enqueue(new Callback<SendResult>() {
                            @Override
                            public void onResponse(Call<SendResult> call, Response<SendResult> response) {

                                if(response.isSuccessful() && response.body() != null) {
                                    Log.d("Push Alarm", "Success " + String.valueOf(response.body().getSuccess()));
                                } else {

                                    Log.d("Push Alarm", "Fail Response" + response.message());
                                    System.out.println(response.code());
                                }
                            }

                            @Override
                            public void onFailure(Call<SendResult> call, Throwable t) {
                                Log.d("Push Alarm", "Fail to Call, Throwable" + t.getMessage());
                            }
                        });
                    }
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

    }

    private RestSendCallService getRestSendCallService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_CALL_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(RestSendCallService.class);
    }

    private double getDistance(Location l1, Location l2) {
        return l1.distanceTo(l2);
    }

    private Location generateLocation(double latitude, double longitude) {
        Location location = new Location("location");
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        return location;
    }

}