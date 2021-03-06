package com.example.myapplication.view;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.EditActivity;
import com.example.myapplication.R;
import com.example.myapplication.Utils;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SendFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SendFragment extends Fragment implements SendMsgButton.OnSendClickListener{

    private EditText send_content;
    private Context mContext;
    private String ipStr,portStr,topicStr,userName,password;
    public static MqttClient mMqClint;
    public MqttConnectOptions mMqttConnectOptions;
    private SendMsgButton mSendMsgBtn;

    public SendFragment() {
        // Required empty public constructor
    }

    public static SendFragment newInstance() {
        SendFragment fragment = new SendFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflateFragment_send(inflater,container);
    }

    private View inflateFragment_send(LayoutInflater inflater, ViewGroup container){
        View rootview = inflater.inflate(R.layout.fragment_send, container, false);
        send_content = rootview.findViewById(R.id.send_content);
        mSendMsgBtn = rootview.findViewById(R.id.bt_send);
        mSendMsgBtn.setOnSendClickListener(this);
        //mSendMsgBtn.setOnClickListener(this);
        return rootview;
    }


    /**
     * ?????????mqtt
     */
    private void initMqtt() {
        String mClintId = System.currentTimeMillis() +
                String.valueOf(new Random().nextInt(100000));
        try {
            ipStr = (String) Utils.get(mContext, EditActivity.IP_ADDRESS,EditActivity.DEFAULT_IP);
            portStr = (String)Utils.get(mContext,EditActivity.IP_PORT,EditActivity.DEFAULT_PORT);
            topicStr = (String)Utils.get(mContext,EditActivity.TOPIC_NAME,EditActivity.DEFAULT_TOPIC);
            userName = (String)Utils.get(mContext,EditActivity.USER_NAME,EditActivity.DEFAULT_USERNAME);
            password = (String)Utils.get(mContext,EditActivity.PASSWORD,EditActivity.DEFAULT_PASSWORD);

            mMqClint = new MqttClient("tcp://"+ipStr+":"+portStr, mClintId, new MemoryPersistence());
            mMqttConnectOptions = new MqttConnectOptions();
            //????????????
            mMqttConnectOptions.setCleanSession(true);
            //???????????????
            mMqttConnectOptions.setUserName(userName);
            //??????????????????
            mMqttConnectOptions.setPassword(password.toCharArray());
            // ?????????????????????????????????
            mMqttConnectOptions.setConnectionTimeout(10);
            // ????????????????????????????????????
            mMqttConnectOptions.setKeepAliveInterval(20);
            //????????????
            mMqClint.setCallback(new SubFragment.PushCallBack(handler));
            //????????????
            connect();
        } catch (MqttException e) {
            Log.d("sgw_d", "MainActivity initMqtt: " + e);
            e.printStackTrace();
        }
    }

    /**
     * mqtt??????
     */
    public void connect() {

        new Thread(() -> {
            try {
                if (mMqClint != null) {
                    //????????????
                    mMqClint.connect(mMqttConnectOptions);
                }
            } catch (Exception e) {
                Log.d("sgw_d", "SendFragment connect: "+e);
                e.printStackTrace();
            }
        }).start();
    }


    /**
     * ????????????mqtt????????????
     */
    private void startReconnect() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                if (!mMqClint.isConnected()) {
                    connect();
                }
            }
        }, 0, 10 * 1000, TimeUnit.MILLISECONDS);
    }


    @Override
    public void onResume() {
        super.onResume();
        initMqtt();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (mMqClint != null) {
                //????????????
                mMqClint.disconnect();
                mMqClint= null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what == 2){
                Log.d("sgw_d", "SendFragment handleMessage: what == 2");
                Toast.makeText(mContext, "????????????????????????", Toast.LENGTH_SHORT).show();
            }
        }
    };

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()){
//            case R.id.bt_send:
//                MqttTopic topic = mMqClint.getTopic(topicStr);
//                MqttMessage message = new MqttMessage();
//                message.setPayload(send_content.getText().toString().getBytes());
//                try {
//                    topic.publish(message);
//                } catch (MqttException e) {
//                    Log.d("sgw_d", "MainActivity onClick: bt_send = "+e);
//                    e.printStackTrace();
//                }
//                break;
//        }
//
//    }

    public boolean publishMessage(){
                MqttTopic topic = mMqClint.getTopic(topicStr);
                MqttMessage message = new MqttMessage();
                message.setPayload(send_content.getText().toString().getBytes());
                try {
                    MqttDeliveryToken publish = topic.publish(message);
                    MqttWireMessage response = publish.getResponse();
                } catch (MqttException e) {
                    Log.d("sgw_d", "MainActivity onClick: bt_send = "+e);
                    return false;
                }
        return true;
    }

    private boolean validateComment() {
        if (TextUtils.isEmpty(send_content.getText())) {
            Toast.makeText(mContext, "???????????????", Toast.LENGTH_SHORT).show();
            mSendMsgBtn.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.shake_error));
            return false;
        }
        return true;
    }

    @Override
    public void onSendClickListener(View v) {
        if (validateComment()) {
            //Toast.makeText(mContext,send_content.getText(),SendMsgButton.RESET_STATE_DELAY_MILLIS).show();
            if (mMqClint.isConnected()){
                publishMessage();
                send_content.setText(null);
                mSendMsgBtn.setCurrentState(SendMsgButton.STATE_DONE);
            }else {
                Toast.makeText(mContext, "????????????!???????????????????????????;ip,?????????????????????.", Toast.LENGTH_SHORT).show();
            }

        }
    }
}