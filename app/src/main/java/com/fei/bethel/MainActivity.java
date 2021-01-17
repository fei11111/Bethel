package com.fei.bethel;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BethelView.attach(findViewById(R.id.tv), new BethelView.OnDisappearListener() {
            @Override
            public void onDismiss() {

            }
        });

    }
}