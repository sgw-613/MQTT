package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class EditActivity extends AppCompatActivity implements View.OnClickListener{

    private Button mBtSave,mBtCancel;
    private EditText ip,port,topic,username,password;
    private ImageView Password_Image;

    public final static String IP_ADDRESS = "IP_ADDRESS";
    public final static String IP_PORT = "IP_PORT";
    public final static String TOPIC_NAME = "TOPIC_NAME";
    public final static String USER_NAME = "USER_NAME";
    public final static String PASSWORD = "PASSWORD";

    public final static String DEFAULT_IP = "10.119.119.149";
    public final static String DEFAULT_PORT = "1883";
    public final static String DEFAULT_TOPIC = "hello";
    public final static String DEFAULT_USERNAME = "admin";
    public final static String DEFAULT_PASSWORD = "password";

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
        String user_name = (String)Utils.get(this,USER_NAME,DEFAULT_USERNAME);
        username.setText(user_name);
        String pass_word = (String)Utils.get(this,PASSWORD,DEFAULT_PASSWORD);
        password.setText(pass_word);
        setPasswordVisible();
    }

    //密碼是否可見
    private boolean isPwdVisible = false;
    /**
     * 设置密码是否可见
     */
    private void setPasswordVisible() {
        //ImageView点击事件
        Password_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //修改密码是否可见的状态
                isPwdVisible = !isPwdVisible;
                //設置密碼是否可見
                if (isPwdVisible) {
                    //设置密码为明文，并更改眼睛图标
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    Password_Image.setImageResource(R.drawable.show_pwd_image);
                } else {
                    //设置密码为暗文，并更改眼睛图标
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    Password_Image.setImageResource(R.drawable.hide_pwd_image);
                }
                //设置光标位置的代码需放在设置明暗文的代码后面
                password.setSelection(password.getText().toString().length());
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.save:
                Utils.put(this,IP_ADDRESS,ip.getText().toString());
                Utils.put(this,IP_PORT,port.getText().toString());
                Utils.put(this,TOPIC_NAME,topic.getText().toString());
                Utils.put(this,USER_NAME,username.getText().toString());
                Utils.put(this,PASSWORD,password.getText().toString());
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
