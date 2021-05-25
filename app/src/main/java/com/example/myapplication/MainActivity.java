package com.example.myapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jcodecraeer.xrecyclerview.XRecyclerView;

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

public class MainActivity extends AppCompatActivity implements View.OnClickListener , LoaderManager.LoaderCallbacks {

    private String ipStr,portStr,topicStr,userName,password;
    private EditText send_content;
    private RecyclerView sub_content_recyclerView;
    private HistoryDB historyDB;
    private static String DB_Name = "history.db";
    private MyContentObserver myContentObserver;


    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1){
                Log.d("sgw_d", "MainActivity handleMessage: what == 1");
                insertData((String)msg.obj);
                //inflateRecycler(queryData());
                //sub_content_recyclerView.setText((String)msg.obj);
            }else if(msg.what == 2){
                Log.d("sgw_d", "MainActivity handleMessage: what == 2");
                Toast.makeText(MainActivity.this, "发布消息回调成功", Toast.LENGTH_SHORT).show();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        historyDB = new HistoryDB(this, HistoryDB.DB_Name);
        //insertData("Hellow World p!");
        //queryData();

        Button mBtSub = this.findViewById(R.id.bt_sub);
        Button mClear = this.findViewById(R.id.bt_clear);
        Button mBtSend = this.findViewById(R.id.bt_send);
        mBtSend.setOnClickListener(this);
        mClear.setOnClickListener(this);
        mBtSub.setOnClickListener(this);

        send_content = findViewById(R.id.send_content);
        sub_content_recyclerView = findViewById(R.id.sub_content_recycler);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // 设置RecyclerView的滚动方向
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        // 为RecyclerView设置布局管理器
        sub_content_recyclerView.setLayoutManager(layoutManager);

//        sub_content_recyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
//            @Override
//            public void onRefresh() {
//                //refresh data here
//                for(int i = 0; i < 15 ;i++){
//
//                }
//                //sub_content_recyclerView.refreshComplete();
//            }
//
//            @Override
//            public void onLoadMore() {
//                // load more data here
//            }
//        });


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitle("MQTT 测试");
        toolbar.setTitleTextColor(Color.WHITE);

        ipStr = (String)Utils.get(this,EditActivity.IP_ADDRESS,EditActivity.DEFAULT_IP);
        portStr = (String)Utils.get(this,EditActivity.IP_PORT,EditActivity.DEFAULT_PORT);
        topicStr = (String)Utils.get(this,EditActivity.TOPIC_NAME,EditActivity.DEFAULT_TOPIC);
        userName = (String)Utils.get(this,EditActivity.USER_NAME,EditActivity.DEFAULT_USERNAME);
        password = (String)Utils.get(this,EditActivity.PASSWORD,EditActivity.DEFAULT_PASSWORD);

        TextView info = this.findViewById(R.id.info);
        info.setText("地址:"+ipStr+"；端口："+portStr+"；主题："+topicStr);
        initMqtt();


        myContentObserver = new MyContentObserver(new Handler());
        getContentResolver().registerContentObserver(HistoryProvider.SUBCONTENTS_URI,true,myContentObserver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void insertData(String data) {

        Log.d("sgw_d", "MainActivity insertData: ");
        ContentValues values = new ContentValues();

        // 向该对象中插入键值对
        values.put(HistoryDB.SUB_CONTENT, data);

        getContentResolver().insert(HistoryProvider.SUBCONTENTS_URI, values);
        getContentResolver().notifyChange(HistoryProvider.SUBCONTENTS_URI,null);
    }


    private void inflateRecycler(Cursor cursor) {
        try {
            Log.d("sgw_d", "MainActivity inflateRecycler: ");
            CursorRecyclerViewAdapter<LineViewHolder> adapter =
                    new CursorRecyclerViewAdapter<>(this, cursor, R.layout.sub_content_line,
                            new int[]{1}, new String[]{ "sub_content"},
                            LineViewHolder.class.getConstructor(MainActivity.class,View.class));

            sub_content_recyclerView.setAdapter(adapter);
        }catch (Exception e){
            Log.d("sgw_d", "MainActivity inflateRecycler: Exception = "+e);
        }
    }

    public Cursor queryData(){
        Cursor cursor = historyDB.getReadableDatabase().rawQuery("select * from history order by _id desc", null);
        return cursor;
    }

    public void clearData(String topicStr){
        //String sql = "delete from " + HistoryDB.TABLE + " where topicStr = " + topicStr;
        String sql = "delete from " + HistoryDB.TABLE ;
//        historyDB.getReadableDatabase().execSQL("delete FROM "+HistoryDB.TABLE + " where sub_content = 'abc'");
        historyDB.getReadableDatabase().execSQL("delete FROM "+HistoryDB.TABLE);
        getContentResolver().notifyChange(HistoryProvider.SUBCONTENTS_URI,null);
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
            mMqttConnectOptions.setUserName(userName);
            //设置用户密码
            mMqttConnectOptions.setPassword(password.toCharArray());
            // 设置超时时间，单位：秒
            mMqttConnectOptions.setConnectionTimeout(10);
            // 心跳包发送间隔，单位：秒
            mMqttConnectOptions.setKeepAliveInterval(20);
            //设置回调
            mMqClint.setCallback(new PushCallBack(handler));
            //订阅消息
            connect();
        } catch (MqttException e) {
            Log.d("sgw_d", "MainActivity initMqtt: " + e);
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        getContentResolver().unregisterContentObserver(myContentObserver);
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
                Log.d("sgw_d", "MainActivity connect: "+e);
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
                        inflateRecycler(queryData());
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.bt_clear:
                clearData(topicStr);
                break;
            case R.id.bt_send:
                MqttTopic topic = mMqClint.getTopic(topicStr);
                MqttMessage message = new MqttMessage();
                message.setPayload(send_content.getText().toString().getBytes());
                try {
                    topic.publish(message);
                } catch (MqttException e) {
                    Log.d("sgw_d", "MainActivity onClick: bt_send = "+e);
                    e.printStackTrace();
                }
                break;
        }
    }

    @NonNull
    @org.jetbrains.annotations.NotNull
    @Override
    public Loader onCreateLoader(int id, @Nullable @org.jetbrains.annotations.Nullable Bundle args) {


        Loader loader = new CursorLoader(this);


        return null;
    }

    @Override
    public void onLoadFinished(@NonNull @org.jetbrains.annotations.NotNull Loader loader, Object data) {

    }

    @Override
    public void onLoaderReset(@NonNull @org.jetbrains.annotations.NotNull Loader loader) {

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
            Log.d("sgw_d",  "messageArrived: 接收消息回调："+message.toString());

            Message msg = handler.obtainMessage();
            msg.what = 1;
            msg.obj = message.toString();
            handler.sendMessage(msg);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.d("sgw_d", "deliveryComplete: 发布消息回调");
            handler.sendEmptyMessage(2);
        }


    }


    public class LineViewHolder extends RecyclerView.ViewHolder
    {
        //TextView titleView;
        TextView sub_content;
        public LineViewHolder(View itemView)
        {
            super(itemView);
            //titleView = itemView.findViewById(R.id.sub_id);
            sub_content = itemView.findViewById(R.id.sub_content);
        }
    }

    class MyContentObserver extends ContentObserver {
        public MyContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            Log.d("sgw_d", "MyContentObserver onChange: ");
        }

        @Override
        public void onChange(boolean selfChange, @Nullable Uri uri) {
            super.onChange(selfChange, uri);
            inflateRecycler(queryData());
        }
    }
}