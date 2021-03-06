package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.HashMap;


public class EditActivity extends AppCompatActivity implements View.OnClickListener{

    private Button mBtSave,mBtCancel;
    private EditText ip,port,topic,username,password;
    private ImageView Password_Image;

    private EditText ftp_ip,ftp_port,ftp_user,ftp_password,ftp_path;

    public final static String IP_ADDRESS = "IP_ADDRESS";
    public final static String IP_PORT = "IP_PORT";
    public final static String TOPIC_NAME = "TOPIC_NAME";
    public final static String USER_NAME = "USER_NAME";
    public final static String PASSWORD = "PASSWORD";

//    public final static String DEFAULT_IP = "10.119.119.149";
//    public final static String DEFAULT_PORT = "1883";
//    public final static String DEFAULT_TOPIC = "hello";

    public final static String DEFAULT_IP = "172.16.6.31";
    public final static String DEFAULT_PORT = "1883";
    public final static String DEFAULT_TOPIC = "publish/nx/nx/to_dev";

    public final static String DEFAULT_USERNAME = "admin";
    public final static String DEFAULT_PASSWORD = "password";


    public final static String FTP_IP_KEY = "FTP_IP_ADDRESS";
    public final static String FTP_PORT_KEY = "FTP_IP_PORT";
    public final static String FTP_PATH_KEY = "FTP_TOPIC_NAME";
    public final static String FTP_USER_KEY = "FTP_USER_NAME";
    public final static String FTP_PASSWORD_KEY = "FTP_PASSWORD";

    public final static String FTP_DEFAULT_IP = "58.33.172.147";
    public final static int FTP_DEFAULT_PORT = 21;
    public final static String FTP_DEFAULT_PATH = "/sim";  //
    public final static String FTP_DEFAULT_USERNAME = "admin";
    public final static String FTP_DEFAULT_PASSWORD = "simftp";

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
        username = this.findViewById(R.id.userName);
        password = this.findViewById(R.id.password);
        Password_Image = this.findViewById(R.id.password_image);

        ftp_ip = findViewById(R.id.ftp_ip);
        ftp_port = findViewById(R.id.ftp_port);
        ftp_user = findViewById(R.id.ftp_user);
        ftp_password = findViewById(R.id.ftp_password);
        ftp_path = findViewById(R.id.ftp_path);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitle("MQTT ??????");
        toolbar.setTitleTextColor(Color.WHITE);

        String ipStr = (String)Utils.get(this,IP_ADDRESS,DEFAULT_IP);
        ip.setText(ipStr);
        String portStr = (String)Utils.get(this,IP_PORT,DEFAULT_PORT);
        port.setText(portStr);
        String topicStr = (String)Utils.get(this,TOPIC_NAME,DEFAULT_TOPIC);
        topic.setText(topicStr);
        String user_name = (String)Utils.get(this,USER_NAME,DEFAULT_USERNAME);
        username.setText(user_name);
        String pass_word = (String)Utils.get(this,PASSWORD,DEFAULT_PASSWORD);
        password.setText(pass_word);
        setPasswordVisible();

        String ftp_ip_str = (String)Utils.get(this,FTP_IP_KEY,FTP_DEFAULT_IP);
        int ftp_port_str = (int)Utils.get(this,FTP_PORT_KEY,FTP_DEFAULT_PORT);
        String ftp_user_str = (String)Utils.get(this,FTP_USER_KEY,FTP_DEFAULT_USERNAME);
        String ftp_password_str = (String)Utils.get(this,FTP_PASSWORD_KEY,FTP_DEFAULT_PASSWORD);
        String ftp_path_str = (String)Utils.get(this,FTP_PATH_KEY,FTP_DEFAULT_PATH);
        ftp_ip.setText(ftp_ip_str);
        ftp_port.setText(String.valueOf(ftp_port_str));
        ftp_user.setText(ftp_user_str);
        ftp_password.setText(ftp_password_str);
        ftp_path.setText(ftp_path_str);

    }

    //??????????????????
    private boolean isPwdVisible = false;
    /**
     * ????????????????????????
     */
    private void setPasswordVisible() {
        //ImageView????????????
        Password_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //?????????????????????????????????
                isPwdVisible = !isPwdVisible;
                //????????????????????????
                if (isPwdVisible) {
                    //?????????????????????????????????????????????
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    Password_Image.setImageResource(R.drawable.show_pwd_image);
                } else {
                    //?????????????????????????????????????????????
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    Password_Image.setImageResource(R.drawable.hide_pwd_image);
                }
                //??????????????????????????????????????????????????????????????????
                password.setSelection(password.getText().toString().length());
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.save:
                Log.d("sgw_dd", "EditActivity onClick: save");

                HashMap<String, Object> hashMap = new HashMap<>();
//                hashMap.put(IP_ADDRESS, ip.getText().toString());

                hashMap.put(IP_ADDRESS,ip.getText().toString());
                hashMap.put(IP_PORT,port.getText().toString());
                hashMap.put(TOPIC_NAME,topic.getText().toString());
                hashMap.put(USER_NAME,username.getText().toString());
                hashMap.put(PASSWORD,password.getText().toString());

                //ftp
                hashMap.put(FTP_IP_KEY,ftp_ip.getText().toString());
                hashMap.put(FTP_PORT_KEY,Integer.parseInt(ftp_port.getText().toString().trim()));
                hashMap.put(FTP_PATH_KEY,ftp_path.getText().toString());
                hashMap.put(FTP_USER_KEY,ftp_user.getText().toString());
                hashMap.put(FTP_PASSWORD_KEY,ftp_password.getText().toString());

                Utils.puts(this,hashMap);

//                //mqtt??????
//                Utils.put(this,IP_ADDRESS,ip.getText().toString());
//                Utils.put(this,IP_PORT,port.getText().toString());
//                Utils.put(this,TOPIC_NAME,topic.getText().toString());
//                Utils.put(this,USER_NAME,username.getText().toString());
//                Utils.put(this,PASSWORD,password.getText().toString());
//
//                //ftp
//                Utils.put(this,FTP_IP_KEY,ftp_ip.getText().toString());
//                Utils.put(this,FTP_PORT_KEY,Integer.parseInt(ftp_port.getText().toString().trim()));
//                Utils.put(this,FTP_PATH_KEY,ftp_path.getText().toString());
//                Utils.put(this,FTP_USER_KEY,ftp_user.getText().toString());
//                Utils.put(this,FTP_PASSWORD_KEY,ftp_password.getText().toString());

                Intent it = new Intent();
                it.putExtra("isEdit", true);
                setResult(1001, it);

                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        //?????????????????????????????????
                        finish();
                    }
                }, 300); //????????????
                //finish();
                break;
            case R.id.cancel:
                finish();
                break;
        }
    }
}
