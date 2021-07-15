package com.example.myapplication;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.database.HistoryDB;
import com.example.myapplication.database.HistoryProvider;
import com.example.myapplication.view.SendFragment;
import com.example.myapplication.view.SubFragment;
import com.google.android.material.tabs.TabLayout;
import com.nightonke.boommenu.BoomButtons.BoomButton;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.BuilderManager;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.OnBoomListener;


public class MainActivity extends AppCompatActivity {

    private static String DB_Name = "history.db";
    private TextView nainInfo;
    private String ipStr,portStr,topicStr,userName,password;
    private BoomMenuButton bmb;
    private static final int REQUEST_CODE = 1024;
    private Context mContext;

    public void insertData(String data) {

        Log.d("sgw_d", "SubFragment insertData: ");
        ContentValues values = new ContentValues();

        // 向该对象中插入键值对
        values.put(HistoryDB.SUB_CONTENT, data);

        mContext.getContentResolver().insert(HistoryProvider.SUBCONTENTS_URI, values);
        mContext.getContentResolver().notifyChange(HistoryProvider.SUBCONTENTS_URI,null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        requestPermission();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitle("MQTT 测试");
        toolbar.setTitleTextColor(Color.WHITE);

        bmb = (BoomMenuButton) findViewById(R.id.bmb);
        assert bmb != null;
        bmb.setButtonEnum(ButtonEnum.Ham);
        int pieceNumber = bmb.getPiecePlaceEnum().pieceNumber();
        Log.d("sgw_d", "MainActivity onCreate: pieceNumber = "+pieceNumber);
        for (int i = 0; i < pieceNumber; i++){
            Log.d("sgw_d", "MainActivity onCreate: addBuilder");
            HamButton.Builder Ham_Button;
            switch (i) {
                case 0:
                    Ham_Button = BuilderManager.getHamButtonBuilderWithDifferentPieceColor();
                    Ham_Button.normalImageRes(R.drawable.ic_edit)
                        .normalTextRes(R.string.action_addr)
                            .normalTextColor(Color.BLACK)
                            .subNormalTextColor(Color.BLACK)
                        .subNormalTextRes(R.string.sub_action_addr)
                        .pieceColor(Color.WHITE)
                        .imagePadding(new Rect(35,35,35,35));
                    Log.d("sgw_d", "MainActivity onCreate: 0");
                    bmb.addBuilder(Ham_Button);
                    break;
                case 1:
                    Ham_Button = BuilderManager.getHamButtonBuilderWithDifferentPieceColor();
                    Ham_Button.normalImageRes(R.drawable.help)
                            .normalTextRes(R.string.action_settings)
                            .subNormalTextRes(R.string.sub_action_settings)
                            .normalTextColor(Color.BLACK)
                            .subNormalTextColor(Color.BLACK)
                            .pieceColor(Color.WHITE)
                            .imagePadding(new Rect(35,35,35,35));
                    Log.d("sgw_d", "MainActivity onCreate: 1");
                    bmb.addBuilder(Ham_Button);
                    break;
            }

        }

        bmb.setOnBoomListener(new OnBoomListener() {
            @Override
            public void onClicked(int index, BoomButton boomButton) {
                switch (index){
                    case 0:
                        //编辑
                        Intent it= new Intent(MainActivity.this,EditActivity.class);
                        startActivityForResult(it,1001);
                        break;
                    case 1:
                        String hint = getResources().getString(R.string.app_name)+",版本号："+Utils.getVerName(mContext);
                        Toast.makeText(MainActivity.this, hint, Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onBackgroundClick() {

            }

            @Override
            public void onBoomWillHide() {

            }

            @Override
            public void onBoomDidHide() {

            }

            @Override
            public void onBoomWillShow() {

            }

            @Override
            public void onBoomDidShow() {

            }
        });




        nainInfo = findViewById(R.id.main_info);
        ipStr = (String) Utils.get(this, EditActivity.IP_ADDRESS,EditActivity.DEFAULT_IP);
        portStr = (String)Utils.get(this,EditActivity.IP_PORT,EditActivity.DEFAULT_PORT);
        topicStr = (String)Utils.get(this,EditActivity.TOPIC_NAME,EditActivity.DEFAULT_TOPIC);
        userName = (String)Utils.get(this,EditActivity.USER_NAME,EditActivity.DEFAULT_USERNAME);
        password = (String)Utils.get(this,EditActivity.PASSWORD,EditActivity.DEFAULT_PASSWORD);
        nainInfo.setText("地址:"+ipStr+"；端口："+portStr+"；主题："+topicStr);

        // 获取界面上ViewPager组件
        ViewPager viewPager = findViewById(R.id.container);
        // 创建SectionsPagerAdapter对象
        SectionsPagerAdapter pagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // 为ViewPager组件设置Adapter
        viewPager.setAdapter(pagerAdapter);
        // 获取界面上的TabLayout组件
        TabLayout tabLayout = findViewById(R.id.tabs);
        // 设置从ViewPager到TabLayout的关联
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        // 设置从TabLayout到ViewPager的关联
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 先判断有没有权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //writeFile();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        } else {
            //writeFile();
        }
    }

    public static boolean Is_Edit = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1001){
            if(data == null)
                return;
            boolean isEdit = data.getBooleanExtra("isEdit",false);
            if(isEdit){
                Is_Edit = true;
                //recreate();
            }
        }
    }

//    /*重新加载布局*/
//    public void reLoadFragView(){
//        /*现将该fragment从fragmentList移除*/
//        if (fragmentList.contains(dashboardFragment)){
//            fragmentList.remove(dashboardFragment);
//        }
//        /*从FragmentManager中移除*/
//        getSupportFragmentManager().beginTransaction().remove(dashboardFragment).commit();
//
//        /*重新创建*/
//        dashboardFragment=new DashboardFragment();
//        /*添加到fragmentList*/
//        fragmentList.add(dashboardFragment);
//
//        /*显示*/
//        showFragment(dashboardFragment,DASHBOARD_FRAGMENT_KEY);
//
//    }


    private final static String TAG = MainActivity.class.getSimpleName();



    // 定义FragmentPagerAdapter的子类
    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {
        SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }
        // 根据位置返回指定的Fragment
        @Override
        public Fragment getItem(int position)
        {
            if (position == 0){
                return SubFragment.newInstance(position);
            }
            return SendFragment.newInstance();
        }
        // 该方法的返回值决定该Adapter包含多少项
        @Override
        public int getCount()
        {
            return 2;
        }
    }
}