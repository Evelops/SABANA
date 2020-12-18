package com.example.sabana;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;

import static java.lang.Thread.sleep;

public class ChatActivity extends AppCompatActivity {

    TextView textView;
    EditText editText;
    ListView listView;

    ArrayList<MessageItem> messageItems=new ArrayList<>();
    ChatAdapter adapter;

    //Firebase Database 관리 객체참조변수
    FirebaseDatabase firebaseDatabase;

    //'chat'노드의 참조객체 참조변수
    DatabaseReference chatRef;

    DatabaseReference chatInfoRef;

    // Chat Name
    String chatName, setChatName;
    String admin;
    final String NORM_SEARCH = "NORMAL_SEARCH";

    ArrayList<ChatInfo> chatInfoList = new ArrayList<>();
    ChatInfo adminChatInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getSupportActionBar().hide();

        // ChatList화면에서 넘어옴
        Intent intent = getIntent();
        chatName = intent.getStringExtra("chatName");
        admin = intent.getStringExtra("admin"); //intent.putExtra("admin", adminChatInfo.admin);

        textView = findViewById(R.id.tv_chatname);
        editText = findViewById(R.id.et);
        listView = findViewById(R.id.listview);
        adapter = new ChatAdapter(messageItems,getLayoutInflater());
        listView.setAdapter(adapter);

        setChatName = "[일반]  " + chatName;
        textView.setText(setChatName);

        //Firebase DB관리 객체와 'chat'노드 참조객체 얻어오기
        firebaseDatabase= FirebaseDatabase.getInstance();
        chatRef= firebaseDatabase.getReference("chat").child(NORM_SEARCH).child(chatName).child("message");

        chatInfoRef = firebaseDatabase.getReference("chat").child(NORM_SEARCH).child(chatName).child("INFO");

        //firebaseDB에서 채팅 메세지들 실시간 읽어오기..
        //'chat'노드에 저장되어 있는 데이터들을 읽어오기
        //chatRef에 데이터가 변경되는 것으 듣는 리스너 추가
        chatRef.addChildEventListener(new ChildEventListener() {
            //새로 추가된 것만 줌 ValueListener는 하나의 값만 바뀌어도 처음부터 다시 값을 줌
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                //새로 추가된 데이터(값 : MessageItem객체) 가져오기
                MessageItem messageItem= dataSnapshot.getValue(MessageItem.class);

                //새로운 메세지를 리스뷰에 추가하기 위해 ArrayList에 추가
                messageItems.add(messageItem);

                //리스트뷰를 갱신
                adapter.notifyDataSetChanged();
                listView.setSelection(messageItems.size()-1); //리스트뷰의 마지막 위치로 스크롤 위치 이동
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void clickFinish(View view){

        try
        {
            sleep(500);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        if (Profile.nickName.equals(admin)){

            String alertTitle = setChatName;
            String alertMessage = "거래가 완료되었습니까?";

            AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
            builder.setTitle(alertTitle);
            builder.setMessage(alertMessage);
            builder.setPositiveButton("예", new DialogInterface.OnClickListener() {

                // 삭제
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    DatabaseReference finishRef;
                    finishRef = firebaseDatabase.getReference("chat").child(NORM_SEARCH).child(chatName);

                    finishRef.removeValue();
                    finish();
                }

            });

            builder.setNeutralButton("취소", null);
            builder.create().show();

        }
        else {
            String message = "방장만 거래를 완료할 수 있습니다.";
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }




    }

    public void clickSend(View view) {

        //firebase DB에 저장할 값들( 닉네임, 메세지, 초기 위치, 시간)
        String nickName= Profile.nickName;

        String lat = Double.toString(Profile.locLatitude);
        String lon = Double.toString(Profile.locLongitude);

//        Log.d("locLongitude", Double.toString(Profile.locLatitude) );
//        Log.d("locLatitude", Double.toString(Profile.locLongitude) );

        String message= editText.getText().toString();

        //메세지 작성 시간 문자열로..
        Calendar calendar= Calendar.getInstance(); //현재 시간을 가지고 있는 객체
        String time=calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE); //14:16

        //firebase DB에 저장할 값(MessageItem객체) 설정
        MessageItem messageItem= new MessageItem(nickName,message,time,lat,lon);
        //'char'노드에 MessageItem객체를 통해
        chatRef.push().setValue(messageItem);

        //EditText에 있는 글씨 지우기
        editText.setText("");

        // SoftKeypad 안보이도록 설정
        InputMethodManager imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);

    }



}