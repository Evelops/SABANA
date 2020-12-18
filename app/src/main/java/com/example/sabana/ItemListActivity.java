package com.example.sabana;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;

import static java.lang.Thread.sleep;

public class ItemListActivity extends AppCompatActivity {

    ListView listView;
    String franchise;

    public List<ItemInfo> itemList;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference quickSearchRef;

    final String QUICK_SEARCH = "QUICK_SEARCH";

    int memberNum = 0;
    int limitNum = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        getSupportActionBar().hide();

        listView = findViewById(R.id.item_list_view);

        ItemListDBHelper dbHelper = new ItemListDBHelper(getApplicationContext());
        dbHelper.openDataBase();
        // ItemListFacade itemListFacade = new ItemListFacade(getApplicationContext());

        if(getIntent() != null) {
            franchise = getIntent().getExtras().getString("franchise");

            final ItemListAdapter itemListAdapter =
                    new ItemListAdapter(getApplicationContext(), dbHelper.getItemList(franchise));

            listView.setAdapter(itemListAdapter);

        }

        dbHelper.close();

        initLoadItemListDataBase();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String temp = itemList.get(position).product + " " + itemList.get(position).price;
                Toast.makeText(getApplicationContext(), temp, Toast.LENGTH_SHORT).show();

                String franchiseName = franchise;
                String productName = itemList.get(position).product;
                int chatListNum = 0;
                int saleMemberNum = itemList.get(position).saleType + 1; // Sale 1+1, 또는 2+1
                limitNum = saleMemberNum;

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
                        else{

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

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                setQuickSearch(chatName);

                try
                {
                    sleep(500);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                Log.d("saleMemberNum", saleMemberNum + "");

                Intent intent = new Intent(ItemListActivity.this, ChatActivity2.class);
                intent.putExtra("franchiseName", franchiseName);
                intent.putExtra("productName", productName);
                intent.putExtra("chatListNum", Integer.toString(chatListNum));
                intent.putExtra("saleMemberNum", Integer.toString(saleMemberNum));
                startActivity(intent);

            }
        });

    }

    private void initLoadItemListDataBase(){

        // DataAdapter -> DB Helper
        ItemListDBHelper dbHelper = new ItemListDBHelper(getApplicationContext());
        dbHelper.CreateDatabase();
        dbHelper.openDataBase();

        itemList = dbHelper.getItemList(franchise);

        dbHelper.close();

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



}