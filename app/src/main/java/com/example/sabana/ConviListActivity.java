package com.example.sabana;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ConviListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convi_list);
    }

    public void onClickButton_CU(View view){

        Intent intent = new Intent(getApplicationContext(), ItemListActivity.class);

        intent.putExtra("franchise", "CU");

        startActivity(intent);

    }

    public void onClickButton_GS25(View view){

        Intent intent = new Intent(getApplicationContext(), ItemListActivity.class);

        intent.putExtra("franchise", "GS25");

        startActivity(intent);

    }

    public void onClickButton_MiniStop(View view){

        Intent intent = new Intent(getApplicationContext(), ItemListActivity.class);

        intent.putExtra("franchise", "미니스톱");

        startActivity(intent);

    }

    public void onClickButton_SevenEleven(View view){

        Intent intent = new Intent(getApplicationContext(), ItemListActivity.class);

        intent.putExtra("franchise", "세븐일레븐");

        startActivity(intent);

    }

    public void onClickButton_Emart24(View view){

        Intent intent = new Intent(getApplicationContext(), ItemListActivity.class);

        intent.putExtra("franchise", "이마트24");

        startActivity(intent);

    }

}