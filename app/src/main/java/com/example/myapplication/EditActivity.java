package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class EditActivity extends AppCompatActivity implements View.OnClickListener{

    private Button mBtSave,mBtCancel;
    private EditText ip,port,topic;

    public final static String IP_ADDRESS = "IP_ADDRESS";
    public final static String IP_PORT = "IP_PORT";
    public final static String TOPIC_NAME = "TOPIC_NAME";

    public final static String DEFAULT_IP = "10.119.119.149";
    public final static String DEFAULT_PORT = "1883";
    public final static String DEFAULT_TOPIC = "hello";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        mBtSave = this.findViewById(R.id.save);
        mBtCancel = this.findViewById(R.id.cancel);
        mBtCancel.setOnClickListener(this);
        mBtSave.setOnClickListener(this);

        ip = this.findViewById(R.id.ip_edit);
        port = this.findViewById(R.id.port_edit);
        topic = this.findViewById(R.id.topic_edit);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitle("MQTT 测试");
        toolbar.setTitleTextColor(Color.WHITE);

        String ipStr = (String)Utils.get(this,IP_ADDRESS,DEFAULT_IP);
        ip.setText(ipStr);
        String portStr = (String)Utils.get(this,IP_PORT,DEFAULT_PORT);
        port.setText(portStr);
        String topicStr = (String)Utils.get(this,TOPIC_NAME,DEFAULT_TOPIC);
        topic.setText(topicStr);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.save:
                Utils.put(this,IP_ADDRESS,ip.getText().toString());
                Utils.put(this,IP_PORT,port.getText().toString());
                Utils.put(this,TOPIC_NAME,topic.getText().toString());
                Intent it = new Intent();
                it.putExtra("isEdit", true);
                setResult(1001, it);
                finish();
                break;
            case R.id.cancel:
                finish();
                break;
        }
    }
}
