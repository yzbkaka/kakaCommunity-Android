package com.example.kakacommunity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.kakacommunity.community.CommunityFragment;
import com.example.kakacommunity.home.HomeFragment;
import com.example.kakacommunity.mine.MineFragment;
import com.example.kakacommunity.project.ProjectFragment;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static boolean isShow = true;

    private ImageView search;

    public static BottomNavigationView bottomNavigationView;

    private ViewPager viewPager;

    private List<Fragment> fragmentList = new ArrayList<>();

    private FragmentPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        search = (ImageView)findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
        bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottom_navigation_view);
        viewPager = (ViewPager)findViewById(R.id.view_pager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragmentList.get(position);
            }

            @Override
            public int getCount() {
                return fragmentList.size();
            }
        };

        addFragment();
        bottomNavigationView.setOnNavigationItemSelectedListener(selectedListener);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(4);
    }

    /**
     * 添加内容fragment
     */
    private void addFragment() {
        fragmentList.add(new HomeFragment());
        fragmentList.add(new CommunityFragment());
        fragmentList.add(new ProjectFragment());
        fragmentList.add(new MineFragment());
    }



    /**
     * 底部设置监听
     */
    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            switch (item.getItemId()){
                case R.id.home:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.community:
                    viewPager.setCurrentItem(1);
                    return true;
                case R.id.project:
                    viewPager.setCurrentItem(2);
                    return true;
                case R.id.mine:
                    viewPager.setCurrentItem(3);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }


    /**
     * 利用反射，改变 item 中 mShiftingMode 的值 ,从而改变 BottomNavigationView 默认的效果
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("RestrictedApi")
    public void disableShiftMode(BottomNavigationView navigationView) {

        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navigationView.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);

            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(i);
                itemView.setShifting(false);
                itemView.setChecked(itemView.getItemData().isChecked());
            }

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
