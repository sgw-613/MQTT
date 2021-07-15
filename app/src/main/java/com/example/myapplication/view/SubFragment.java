package com.example.myapplication.view;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.CursorRecyclerViewAdapter;
import com.example.myapplication.EditActivity;
import com.example.myapplication.MainActivity;
import com.example.myapplication.database.HistoryDB;
import com.example.myapplication.database.HistoryProvider;
import com.example.myapplication.R;
import com.example.myapplication.Utils;
import com.example.myapplication.ftp.FtpUtil;
import com.nightonke.boommenu.Util;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.File;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * A simple {@link Fragment} subclass.
 *
 * create an instance of this fragment.
 */
public class SubFragment extends Fragment implements View.OnClickListener{

    private int fragment_id;
    private static String FRG_ID = "fragment_id";

    private String ipStr,portStr,topicStr,userName,password;
    private RecyclerView sub_content_recyclerView;
    private HistoryDB historyDB;
    private MyContentObserver myContentObserver;
    //private TextView textinfo;

    public static MqttClient mMqClint;
    public MqttConnectOptions mMqttConnectOptions;

    private Context mContext;
    private Button mBtSub;
    public SubFragment() {
        // Required empty public constructor
    }


    public static SubFragment newInstance(int id) {
        SubFragment fragment = new SubFragment();
        Bundle args = new Bundle();
        args.putInt(FRG_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragment_id = getArguments().getInt(FRG_ID);

        mContext = getContext();
        historyDB = new HistoryDB(mContext, HistoryDB.DB_Name);
        myContentObserver = new MyContentObserver(new Handler());
        mContext.getContentResolver().registerContentObserver(HistoryProvider.SUBCONTENTS_URI,true,myContentObserver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflateFragment_sub(inflater,container);
    }

    private View inflateFragment_sub(LayoutInflater inflater, ViewGroup container){

        View rootview = inflater.inflate(R.layout.fragment_sub, container, false);
        Log.d("sgw_d", "SubFragment inflateFragment_sub: ");

        //textinfo = rootview.findViewById(R.id.info);

        mBtSub = rootview.findViewById(R.id.bt_sub);
        Button mClear = rootview.findViewById(R.id.bt_clear);
        //Button ftp_btn = rootview.findViewById(R.id.bt_ftp);
        mClear.setOnClickListener(this);
        mBtSub.setOnClickListener(this);
        //ftp_btn.setOnClickListener(this);
        sub_content_recyclerView = rootview.findViewById(R.id.sub_content_recycler);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        // 设置RecyclerView的滚动方向
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        // 为RecyclerView设置布局管理器
        sub_content_recyclerView.setLayoutManager(layoutManager);
        return rootview;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d("sgw_d", "SubFragment onResume: ");

        if (MainActivity.Is_Edit){
            MainActivity activity= (MainActivity) getActivity();
            //activity.reLoadFragView();
            MainActivity.Is_Edit = false;
        }

        initMqtt();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("sgw_d", "SubFragment onActivityResult: resultCode="+requestCode);
        if(requestCode == 1001){
            if(data == null)
                return;
            boolean isEdit = data.getBooleanExtra("isEdit",false);
            if(isEdit){
                //recreate();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mContext.getContentResolver().unregisterContentObserver(myContentObserver);
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
            if(msg.what == 1){
                Log.d("sgw_d", "SubFragment handleMessage: what == 1");
                Log.d("sgw_d", "SubFragment handleMessage: obj = "+(String)msg.obj);
                insertData((String)msg.obj);
                //inflateRecycler(queryData());
                //sub_content_recyclerView.setText((String)msg.obj);
            }else if(msg.what == 2){
                Log.d("sgw_d", "SubFragment handleMessage: what == 2");
                Toast.makeText(mContext, "发布消息回调成功", Toast.LENGTH_SHORT).show();

            }
        }
    };

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
            Log.d("sgw_d", "SubFragment initMqtt: userName = "+userName);
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
            Log.d("sgw_d", "SubFragment initMqtt: " + e);
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
                Log.d("sgw_d", "SubFragment connect: "+e);
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

    private void inflateRecycler(Cursor cursor) {
        try {
            Log.d("sgw_d", "SubFragment inflateRecycler: ");
            CursorRecyclerViewAdapter adapter =
                    new CursorRecyclerViewAdapter(getActivity(),getActivity(), cursor, R.layout.sub_content_line,
                            new int[]{1}, new String[]{ "sub_content","recycler_image"},
                            null);
//            CursorRecyclerViewAdapter.LineViewHolder.class.getConstructor(SubFragment.class, View.class)
            Log.d("sgw_d", "SubFragment inflateRecycler:setAdapter ");
            sub_content_recyclerView.setAdapter(adapter);
        }catch (Exception e){
            Log.d("sgw_d", "SubFragment inflateRecycler: Exception = "+e);
        }
    }


    public void insertData(String data) {

        Log.d("sgw_d", "SubFragment insertData: ");
        ContentValues values = new ContentValues();

        // 向该对象中插入键值对
        values.put(HistoryDB.SUB_CONTENT, data);

        mContext.getContentResolver().insert(HistoryProvider.SUBCONTENTS_URI, values);
        mContext.getContentResolver().notifyChange(HistoryProvider.SUBCONTENTS_URI,null);
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
        mContext.getContentResolver().notifyChange(HistoryProvider.SUBCONTENTS_URI,null);
    }

    private boolean clearOrNot(){
        new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure?")
                .setContentText("Won't be able to recover this file!")
                .setConfirmText("Yes,delete it!")
                .show();
        return false;
    }


    private void showNormalDialog(){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(mContext);
        //normalDialog.setIcon(R.drawable.icon_dialog);
        normalDialog.setTitle("删除记录！");
        normalDialog.setMessage("您确定要删除历史记录吗?");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        clearData(topicStr);
                    }
                });
        normalDialog.setNegativeButton("关闭",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });
        // 显示
        normalDialog.show();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_sub:
                if (mMqClint.isConnected()){
                    try {
                        if (mBtSub.getText().equals("开始订阅")){
                            mBtSub .setText("暂停订阅");
                            mMqClint.subscribe(topicStr,0);
                            inflateRecycler(queryData());
                        }else if (mBtSub.getText().equals("暂停订阅")){
                            mBtSub .setText("开始订阅");
                            mMqClint.unsubscribe(topicStr);
                        }
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.bt_clear:
                showNormalDialog();
                break;
        }
    }

//    public void getFtpFile(){
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                String ftpHost = "10.119.119.67";
//                String ftpUserName = "testftp";
//                String ftpPassword = "1234";
//                int ftpPort = 21;
//                String ftpPath = "/home/testftp/test/1.txt";
//                byte[] file_cont = new FtpUtil().getFtpFileBytes(ftpHost, ftpUserName, ftpPassword, ftpPort, ftpPath);
//                String file_s = new String(file_cont);
//                System.out.println(file_s);
//                Log.d("sgw_d", "SubFragment Start_Ftp: file_s ="+file_s);
//            }
//        }).start();
//    }


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




    public static class PushCallBack implements MqttCallback {

        private Handler handler;

        PushCallBack(Handler handler){
            this.handler = handler;
        }

        @Override
        public void connectionLost(Throwable throwable) {
            Log.i("sgw", "connectionLost: 链接丢失");
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


//    public class LineViewHolder extends RecyclerView.ViewHolder {
//        TextView sub_content;
//        ImageView recycler_image;
//
//        public LineViewHolder(View itemView) {
//            super(itemView);
//            sub_content = itemView.findViewById(R.id.sub_content);
//            recycler_image = itemView.findViewById(R.id.recycler_image);
//
//        }
//    }




}