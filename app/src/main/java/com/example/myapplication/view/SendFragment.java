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
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Random;

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
    private TextView textinfo;
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
        //textinfo = rootview.findViewById(R.id.info);
        send_content = rootview.findViewById(R.id.send_content);
        mSendMsgBtn = rootview.findViewById(R.id.bt_send);
        mSendMsgBtn.setOnSendClickListener(this);
        //mSendMsgBtn.setOnClickListener(this);
        return rootview;
    }


    /**
     * 初始化mqtt
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
            //textinfo.setText("地址:"+ipStr+"；端口："+portStr+"；主题："+topicStr);

            mMqClint = new MqttClient("tcp://"+ipStr+":"+portStr, mClintId, new MemoryPersistence());
            mMqttConnectOptions = new MqttConnectOptions();
            //清除缓存
            mMqttConnectOptions.setCleanSession(true);
            //设置用户名
            mMqttConnectOptions.setUserName(userName);
            //设置用户密码
            mMqttConnectOptions.setPassword(password.toCharArray());
            // 设置超时时间，单位：秒
            mMqttConnectOptions.setConnectionTimeout(10);
            // 心跳包发送间隔，单位：秒
            mMqttConnectOptions.setKeepAliveInterval(20);
            //设置回调
            mMqClint.setCallback(new SubFragment.PushCallBack(handler));
            //订阅消息
            connect();
        } catch (MqttException e) {
            Log.d("sgw_d", "MainActivity initMqtt: " + e);
            e.printStackTrace();
        }
    }

    /**
     * mqtt链接
     */
    public void connect() {

        new Thread(() -> {
            try {
                if (mMqClint != null) {
                    //开始链接
                    mMqClint.connect(mMqttConnectOptions);
                }
            } catch (Exception e) {
                Log.d("sgw_d", "MainActivity connect: "+e);
                e.printStackTrace();
            }
        }).start();
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
                //开始链接
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
                Log.d("sgw_d", "MainActivity handleMessage: what == 2");
                Toast.makeText(mContext, "发布消息回调成功", Toast.LENGTH_SHORT).show();
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

    private boolean validateComment() {
        if (TextUtils.isEmpty(send_content.getText())) {
            Toast.makeText(mContext, "请输入内容", Toast.LENGTH_SHORT).show();
            mSendMsgBtn.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.shake_error));
            return false;
        }
        return true;
    }

    @Override
    public void onSendClickListener(View v) {
        if (validateComment()) {
            //Toast.makeText(mContext,send_content.getText(),SendMsgButton.RESET_STATE_DELAY_MILLIS).show();
            send_content.setText(null);
            mSendMsgBtn.setCurrentState(SendMsgButton.STATE_DONE);
        }
    }
}