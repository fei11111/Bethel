package com.fei.lovebethelview;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private LoveBethelLayout mLoveBethelLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLoveBethelLayout = findViewById(R.id.love_layout);
    }

    public void addLove(View view) {
        mLoveBethelLayout.addLoveView();
    }
}