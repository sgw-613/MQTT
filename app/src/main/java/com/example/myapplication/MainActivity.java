package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private String ipStr,portStr,topicStr;
    private EditText send_content;
    private TextView sub_content;
    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1){
                sub_content.setText((String)msg.obj);
            }else if(msg.what ==2){
                Toast.makeText(MainActivity.this, "发布消息回调成功", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button mBtSub = this.findViewById(R.id.bt_sub);
        Button mBtSend = this.findViewById(R.id.bt_send);
        mBtSend.setOnClickListener(this);
        mBtSub.setOnClickListener(this);

        send_content = findViewById(R.id.send_content);
        sub_content = findViewById(R.id.sub_content);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitle("MQTT 测试");
        toolbar.setTitleTextColor(Color.WHITE);

        ipStr = (String)Utils.get(this,EditActivity.IP_ADDRESS,EditActivity.DEFAULT_IP);
        portStr = (String)Utils.get(this,EditActivity.IP_PORT,EditActivity.DEFAULT_PORT);
        topicStr = (String)Utils.get(this,EditActivity.TOPIC_NAME,EditActivity.DEFAULT_TOPIC);

        TextView info = this.findViewById(R.id.info);
        info.setText("地址:"+ipStr+"；端口："+portStr+"；主题："+topicStr);
        initMqtt();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_edit) {
            Intent it= new Intent(MainActivity.this,EditActivity.class);
            startActivityForResult(it,1001);
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            String hint = getResources().getString(R.string.app_name)+",版本号："+Utils.getVerName(this);
            Toast.makeText(MainActivity.this, hint, Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1001){
            if(data == null)
                return;
            boolean isEdit = data.getBooleanExtra("isEdit",false);
            if(isEdit){
                recreate();
            }
        }
    }

    public static MqttClient mMqClint;
    public MqttConnectOptions mMqttConnectOptions;
    private final static String TAG = MainActivity.class.getSimpleName();

    /**
     * 初始化mqtt
     */
    private void initMqtt() {
        String mClintId = System.currentTimeMillis() +
                String.valueOf(new Random().nextInt(100000));
        try {
            mMqClint = new MqttClient("tcp://"+ipStr+":"+portStr, mClintId, new MemoryPersistence());
            mMqttConnectOptions = new MqttConnectOptions();
            //清除缓存
            mMqttConnectOptions.setCleanSession(true);
            //设置用户名
           // mMqttConnectOptions.setUserName();
            //设置用户密码
            //mMqttConnectOptions.setPassword();
            // 设置超时时间，单位：秒
            mMqttConnectOptions.setConnectionTimeout(10);
            // 心跳包发送间隔，单位：秒
            mMqttConnectOptions.setKeepAliveInterval(20);
            //设置回调
            mMqClint.setCallback(new PushCallBack(handler));
            //订阅消息
            connect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
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
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 定时检查mqtt是否连接
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
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_sub:
                if (mMqClint.isConnected()){
                    try {
                        mMqClint.subscribe(topicStr,0);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.bt_send:
                MqttTopic topic = mMqClint.getTopic(topicStr);
                MqttMessage message = new MqttMessage();
                message.setPayload(send_content.getText().toString().getBytes());
                try {
                    topic.publish(message);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public static class PushCallBack implements MqttCallback {

        private Handler handler;

        PushCallBack(Handler handler){
            this.handler = handler;
        }

        @Override
        public void connectionLost(Throwable throwable) {
            Log.i(TAG, "connectionLost: 链接丢失");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            Log.i(TAG, "messageArrived: 接收消息回调："+message.toString());

            Message msg = handler.obtainMessage();
            msg.what = 1;
            msg.obj = message.toString();
            handler.sendMessage(msg);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.i(TAG, "deliveryComplete: 发布消息回调");
            handler.sendEmptyMessage(2);
        }


    }
}