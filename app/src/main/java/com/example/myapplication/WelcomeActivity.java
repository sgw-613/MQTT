package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.sim.particleview.ParticleView;


/**
 * 作者： 巴掌 on 16/8/31 15:18
 * Github: https://github.com/JeasonWong
 */
public class WelcomeActivity extends AppCompatActivity {

    ParticleView mPvGithub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);

        getSupportActionBar().hide();
        mPvGithub = (ParticleView) findViewById(R.id.pv_github);

        mPvGithub.startAnim();

        mPvGithub.setOnParticleAnimListener(new ParticleView.ParticleAnimListener() {
            @Override
            public void onAnimationEnd() {
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                WelcomeActivity.this.startActivity(intent);
                finish();
            }
        });

    }
}
