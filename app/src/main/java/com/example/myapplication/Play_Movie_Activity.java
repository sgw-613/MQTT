package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

public class Play_Movie_Activity extends AppCompatActivity {
        private VideoView videoView;
        private MediaController mController;
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_play_movie);

            getSupportActionBar().hide();
            // 获取界面上的VideoView组件
            videoView = findViewById(R.id.video);
            // 创建MediaController对象
            mController = new MediaController(this);
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0x123);
        }

        @Override public void onRequestPermissionsResult(int requestCode,
                                                         @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == 0x123 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 设为横屏
                //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                String filename = getIntent().getExtras().getString("filename");

                File video = new File(Utils.getLocalFilePath(filename));
                if (video.exists()) {
                    Log.d("sgw_d", "MainActivity onRequestPermissionsResult: start");
                    videoView.setVideoPath(video.getAbsolutePath()); // ①
                    // 设置videoView与mController建立关联
                    videoView.setMediaController(mController);  // ②
                    // 设置mController与videoView建立关联
                    mController.setMediaPlayer(videoView);  // ③
                    // 让VideoView获取焦点
                    videoView.requestFocus();
                    videoView.start(); // 开始播放
                    Log.d("sgw_d", "MainActivity onRequestPermissionsResult: end");
                }
            }
        }
    }