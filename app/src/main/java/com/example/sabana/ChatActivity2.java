package com.example.sabana;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPoint;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static java.lang.Thread.sleep;

public class ChatActivity2 extends AppCompatActivity {

    EditText editText;
    ListView listView;
    TextView textView;

    ArrayList<MessageItem> messageItems = new ArrayList<>();
    ChatAdapter adapter;

    FirebaseDatabase firebaseDatabase; // Firebase Database 관리 참조
    DatabaseReference chatRef;
    DatabaseReference selectRef;

    // intent
    String franchiseName;
    String productName;
    int chatListNum;
    int saleMemberNum;

    double timeLossCost  = 0;
    double distanceLossCost = 0;

    int maxTime = 987654321; // min
    int maxDistance = 987654321; // km

    String chatName;
    String setChatName;

    final String QUICK_SEARCH = "QUICK_SEARCH";

    // boolean limitFlag = false;

    public List<ConvenienceStore> convenienceStoreList;
    public List<ConvenienceStore> franchiseStoreList;

    public List<MemberInfo> memberInfoList;
    public ArrayList<SelectConvi> selectConviList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat2);

        getSupportActionBar().hide();

        editText = findViewById(R.id.et2); // et : Normal Search, et2 : Fast Search
        listView = findViewById(R.id.listview2);
        textView = findViewById(R.id.tv_chatname2);

        // 12.03 수정 필요 물건이름 넘겨받기
        Intent intent = getIntent();

        franchiseName = intent.getStringExtra("franchiseName");
        productName = intent.getStringExtra("productName");
        chatListNum = Integer.parseInt(intent.getStringExtra("chatListNum"));
        saleMemberNum = Integer.parseInt(intent.getStringExtra("saleMemberNum"));

        // Convi DB 긁어오기
        // franchise에 일치하는 것만,,,!
        initLoadDatabase();

        // 채팅방 이름 : CU_생수
        chatName = franchiseName + "_" + productName + "_" + chatListNum;
        setChatName = "[빠른찾기]  " + franchiseName + " " + productName;
        textView.setText(setChatName);

        // getSupportActionBar().setTitle(chatName);

        adapter = new ChatAdapter(messageItems, getLayoutInflater());
        listView.setAdapter(adapter);

        // 채팅 읽어오기
        firebaseDatabase = FirebaseDatabase.getInstance();
        chatRef = firebaseDatabase.getReference("chat").child(QUICK_SEARCH).child(chatName).child("message");

        chatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                MessageItem messageItem = snapshot.getValue(MessageItem.class);

                messageItems.add(messageItem); // add new message to array list

                // 리스트뷰 갱신
                adapter.notifyDataSetChanged();
                listView.setSelection(messageItems.size() - 1); // scroll 위치 이동

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


        // 최적 편의점 찾기
        // 정해진 인원 다 찼는지 확인
        DatabaseReference limitRef;
        limitRef = firebaseDatabase.getReference("chat").child(QUICK_SEARCH).child(chatName).child("LIMIT");

        // REF CODE addListenerForSingleValueEvent()
        limitRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String temp = snapshot.getValue().toString();
                String limitIsTrue = "{LIMIT_FLAG=true}";
                //Log.e("getValue", temp);

                if (temp.equals(limitIsTrue)){
                    Log.e("is this same?", temp);
                    QuickSearch.limitFlag = true;
                }
                else{
                    QuickSearch.limitFlag = false;
                    QuickSearch.selectFlag = false;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // 참여 member list 가져오기
        DatabaseReference memberRef;
        memberRef = firebaseDatabase.getReference("chat").child(QUICK_SEARCH).child(chatName).child("MEMBER");

        selectRef = firebaseDatabase.getReference("chat").child(QUICK_SEARCH).child(chatName).child("SELECT");

        memberInfoList = new ArrayList<>();

        memberRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                String temp = snapshot.getKey();
                // Log.e("temp", temp);

                DatabaseReference tempRef;
                tempRef = firebaseDatabase.getReference("chat").child(QUICK_SEARCH).child(chatName).child("MEMBER").child(temp);

                tempRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        MemberInfo getMemberInfo = snapshot.getValue(MemberInfo.class);
                        memberInfoList.add(getMemberInfo);

                        Log.e("getMemberInfo", getMemberInfo.lat + " " + getMemberInfo.lon);
                        Log.e("MemberInfoList", memberInfoList.size() + "");

                        // * Loss Function 구하는 코드 작성하기 * //

                        if (memberInfoList.size() == saleMemberNum && QuickSearch.selectFlag == false && QuickSearch.limitFlag == true){

                            String message = "편의점 선정 진행중입니다. 조금만 기다려주세요";
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                            int testEpoch = convenienceStoreList.size();

                            // * 일 사용량을 넘기고 싶지 않아요,,, //
                            //for (int i = 0; i < convenienceStoreList.size(); i++ ){
                            for (int i = 0; i < testEpoch; i++ ){

                                int tempID = convenienceStoreList.get(i)._id;
                                double tempConviLat = convenienceStoreList.get(i).latitude;
                                double tempConviLon = convenienceStoreList.get(i).longitude;

                                // Loss Cost 계산
                                timeLossCost = 0;
                                distanceLossCost = 0;

                                for (int j = 0; j < saleMemberNum; j++){

                                    // main thread 처리 위해
                                    try
                                    {
                                        sleep(600);
                                    } catch (InterruptedException e)
                                    {
                                        e.printStackTrace();
                                    }

                                    Log.d("store, member",  i + " , " + j ); // sec

                                    double tempMemberLat = Double.parseDouble(memberInfoList.get(j).lat);
                                    double tempMemberLon = Double.parseDouble(memberInfoList.get(j).lon);

                                    // Total Time, Total Distance 계산
                                    TMapData tMapData = new TMapData();
                                    TMapPoint tMapPointStart = new TMapPoint(tempConviLat, tempConviLon);
                                    TMapPoint tMapPointEnd = new TMapPoint(tempMemberLat, tempMemberLon);

                                    tMapData.findPathDataAllType(TMapData.TMapPathType.PEDESTRIAN_PATH, tMapPointStart, tMapPointEnd, new TMapData.FindPathDataAllListenerCallback() {
                                        @Override
                                        public void onFindPathDataAll(Document document) {

                                            Element root = document.getDocumentElement();

                                            // Total Time과 Total Distance 계산
                                            int totalTime = 0;
                                            //int totalDistance = 0;

                                            NodeList nodeListPlacemark = root.getElementsByTagName("Placemark");

                                            for( int ii=0; ii<nodeListPlacemark.getLength(); ii++ ) {

                                                NodeList nodeListPlacemarkItem = nodeListPlacemark.item(ii).getChildNodes();

                                                for( int jj=0; jj<nodeListPlacemarkItem.getLength(); jj++ ) {

                                                    if( nodeListPlacemarkItem.item(jj).getNodeName().equals("tmap:time") ) {
                                                        String temp = nodeListPlacemarkItem.item(jj).getTextContent().trim();
                                                        totalTime += Integer.parseInt(temp);
                                                        //Log.d("debug", temp );
                                                    }

//                                                    if ( nodeListPlacemarkItem.item(j).getNodeName().equals("tmap:distance")){
//                                                        String temp = nodeListPlacemarkItem.item(j).getTextContent().trim();
//                                                        totalDistance += Integer.parseInt(temp);
//                                                        //Log.d("debug", temp);
//
//                                                    }
                                                }
                                            }

                                            // Loss Cost 계산
                                            timeLossCost += totalTime/60;
                                            //distanceLossCost += totalDistance/1000 * totalDistance/1000;

                                            Log.d("total time",  Integer.toString(totalTime) ); // sec
                                            //Log.d("total distance", Integer.toString(totalDistance) ); // m

                                        }
                                    });

                                }


                                // Loss Cost 게산 (최종)
                                timeLossCost = timeLossCost / saleMemberNum;
                                // distanceLossCost = distanceLossCost / saleMemberNum;
                                if (timeLossCost > 0){
                                    SelectConvi tempConvi = new SelectConvi(i, timeLossCost);
                                    selectConviList.add(tempConvi);

                                    Log.d("timeLossCost", i + ": " + timeLossCost + "");

                                    if (i == testEpoch - 1){
                                        selectConviStore();
                                        QuickSearch.selectFlag = true;
                                    }


                                }

                            }
                        }


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

    public void clickFinish(View view){

        String alertTitle = setChatName;
        String alertMessage = "거래가 완료되었습니까?";

        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity2.this);
        builder.setTitle(alertTitle);
        builder.setMessage(alertMessage);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {

            // 삭제
            @Override
            public void onClick(DialogInterface dialog, int which) {

                DatabaseReference finishRef;
                finishRef = firebaseDatabase.getReference("chat").child(QUICK_SEARCH).child(chatName);

                finishRef.removeValue();
                finish();
            }

        });

        builder.setNeutralButton("취소", null);
        builder.create().show();

    }

    public void clickSend(View view){

        String nickName = Profile.nickName;
        String lat = Double.toString(Profile.locLatitude);
        String lon = Double.toString(Profile.locLongitude);

        String message = editText.getText().toString();

        Calendar calendar = Calendar.getInstance();
        String time = calendar.get(Calendar.HOUR_OF_DAY) + " : " + calendar.get(Calendar.MINUTE);

        // MessageItem 설정, chatRef에 MessageItem 스택
        MessageItem messageItem = new MessageItem(nickName, message, time, lat, lon);
        chatRef.push().setValue(messageItem);

        // EditText 초기화
        editText.setText("");

        // SoftKeypad 안보이도록 설정
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

    }

    public void selectConviStore(){

        Log.e("ll", ""+ selectConviList.size());

        double maxTime = 987654321;

        for (int i = 0; i < selectConviList.size(); i++){

            double tempLoss = selectConviList.get(i).totalTimeCost * selectConviList.get(i).totalTimeCost / saleMemberNum;
            Log.e("tempLoss", tempLoss + "");

            if (tempLoss <= maxTime){
                maxTime = tempLoss;

                QuickSearch.selectConvi_id = i;

                String selectStoreName = convenienceStoreList.get(QuickSearch.selectConvi_id).storeName;

                selectRef.child("SELECT_FLAG").setValue(true);
                selectRef.child("SELECT_STOREID").setValue(QuickSearch.selectConvi_id);
                selectRef.child("SELECT_STORE").setValue(selectStoreName);

            }

        }

    }

    public void clickSelect(View view){

        if (QuickSearch.selectFlag == true){

            String selectStoreName = convenienceStoreList.get(QuickSearch.selectConvi_id).storeName;
            String selectAddress = convenienceStoreList.get(QuickSearch.selectConvi_id).address;
            String selectConviLat = Double.toString(convenienceStoreList.get(QuickSearch.selectConvi_id).latitude);
            String selectConviLon = Double.toString(convenienceStoreList.get(QuickSearch.selectConvi_id).longitude);

            Intent intent = new Intent(this, SelectConviMap.class);

            intent.putExtra("selectStoreName", selectStoreName);
            intent.putExtra("selectAddress", selectAddress);
            intent.putExtra("selectConviLat", selectConviLat);
            intent.putExtra("selectConviLon", selectConviLon);

            startActivity(intent);

        }
        else if (memberInfoList.size() < saleMemberNum){

            String message = "";

            if (saleMemberNum == 2){
                message += "1+1";
            }
            else if (saleMemberNum == 3){
                message += "2+1";
            }
            message += " 상품을 위해 " + memberInfoList.size() + " 명의 참여자가 참여했습니다.";
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

        }
        else {
            String message = "아직 편의점 선정이 진행중입니다. 조금만 기다려주세요.";
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

        }


    }


    // Load Database
    private void initLoadDatabase(){

        // DataAdapter -> DB Helper
        DataAdapter mDBHelper = new DataAdapter(getApplicationContext());
        mDBHelper.CreateDatabase();
        mDBHelper.OpenDatabase();

        convenienceStoreList = mDBHelper.getTableData();

        mDBHelper.close();

        Log.e("convi", convenienceStoreList.size()+"");
        for (int i = 0; i < convenienceStoreList.size(); i++){

            String franchise = convenienceStoreList.get(i).franchise;

            if (franchise.equals(franchiseName) == false){
                //Log.d("remove?" , + i + " : " + franchise + " vs " + franchiseName);
                convenienceStoreList.remove(i);
                i--;
            }

        }

        Log.e("franchise", convenienceStoreList.size()+"");




    }




}