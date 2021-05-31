package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.example.myapplication.view.SendFragment;
import com.example.myapplication.view.SubFragment;
import com.google.android.material.tabs.TabLayout;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.BuilderManager;
import com.nightonke.boommenu.ButtonEnum;


public class MainActivity extends AppCompatActivity {

    private static String DB_Name = "history.db";
    private TextView nainInfo;
    private String ipStr,portStr,topicStr,userName,password;
    private BoomMenuButton bmb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        for (int i = 0; i < pieceNumber; i++)
            bmb.addBuilder(BuilderManager.getHamButtonBuilderWithDifferentPieceColor());

        bmb.setOnSubButtonClickListener(new BoomMenuButton.OnSubButtonClickListener() {
            @Override
            public void onClick() {
                Log.d("sgw_d", "MainActivity onClick: ");
            }

//            @Override
//            public void onClick(int buttonIndex) {
//                // 返回被点击的子按钮下标
//            }
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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onMenuOpened(int featureId, Menu menu) {
//        Log.d("sgw_d", "MainActivity onMenuOpened: ");
//        return super.onMenuOpened(featureId, menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        Log.d("sgw_d", "MainActivity onOptionsItemSelected: ");
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_edit) {
//            Intent it= new Intent(MainActivity.this,EditActivity.class);
//            startActivityForResult(it,1001);
//            return true;
//        }
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            String hint = getResources().getString(R.string.app_name)+",版本号："+Utils.getVerName(this);
//            Toast.makeText(MainActivity.this, hint, Toast.LENGTH_SHORT).show();
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

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