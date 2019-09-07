package com.example.serviceexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnStart, btnStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = (Button) findViewById(R.id.buttonStart);
        btnStop = (Button) findViewById(R.id.buttonStop);


        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if(view == btnStart){
            startService(new Intent(this, MyService.class));

        }else if(view == btnStop) {
            stopService(new Intent(this, MyService.class));
        }

    }
}
