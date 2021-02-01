package com.example.kakacommunity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.example.kakacommunity.community.CommunityFragment;
import com.example.kakacommunity.home.HomeFragment;
import com.example.kakacommunity.mine.MineFragment;
import com.example.kakacommunity.project.ProjectFragment;
import com.example.kakacommunity.search.SearchActivity;
import com.example.kakacommunity.service.FDWatchService;
import com.example.kakacommunity.utils.NativeMethodHelper;
import com.example.kakacommunity.utils.PermissionsUtil;
import com.example.kakacommunity.view.NoScrollViewPager;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.example.kakacommunity.constant.kakaCommunityConstant.COMMUNITY_ADD;
import static com.example.kakacommunity.constant.kakaCommunityConstant.HOME_TOP;
import static com.example.kakacommunity.constant.kakaCommunityConstant.PROJECT_TOP;

public class MainActivity extends AppCompatActivity {

    private ImageView search;

    public static BottomNavigationView bottomNavigationView;

    private NoScrollViewPager viewPager;

    private List<Fragment> fragmentList = new ArrayList<>();

    private FragmentPagerAdapter pagerAdapter;

    private FloatingActionButton floatingActionButton;

    private FloatingActionButton addButton;

    private FragmentManager fragmentManager;

    private MineFragment mineFragment;

    private NativeMethodHelper nativeMethodHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermissions();
        initHook();
        initView();

    }

    /**
     * 动态申请权限
     */
    private void getPermissions() {
        PermissionsUtil.getInstance().checkPermissions(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                new PermissionsUtil.IPermissionsResult() {
                    @Override
                    public void passPermissions() {
                        Log.e("yzbkaka", "get all permissions successful!");
                    }

                    @Override
                    public void forbidPermissions() {
                        Log.e("yzbkaka", "failed to get all permissions!");
                    }
                });
    }

    /**
     * 权限回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsUtil.getInstance().onRequestPermissionResult(this, requestCode, permissions, grantResults);
    }

    /**
     * 初始化hook操作
     */
    private void initHook() {
        nativeMethodHelper = NativeMethodHelper.getInstance();
        nativeMethodHelper.init();
        nativeMethodHelper.getEnv();
        nativeMethodHelper.startHook();
        initService();
    }

    /**
     * 启动监控服务
     */
    private void initService() {
        Intent intent = new Intent(this, FDWatchService.class);
        startService(intent);
    }

    /**
     * 初始化View
     */
    private void initView() {
        fragmentManager = getSupportFragmentManager();
        search = (ImageView) findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floating_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = getFragment();
                Intent intent;
                if (fragment != null) {
                    if (fragment instanceof HomeFragment) {
                        intent = new Intent(HOME_TOP);
                        sendBroadcast(intent);
                    }
                    if (fragment instanceof ProjectFragment) {
                        intent = new Intent(PROJECT_TOP);
                        sendBroadcast(intent);
                    }
                }
            }
        });
        addButton = (FloatingActionButton)findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = getFragment();
                Intent intent;
                if (fragment instanceof CommunityFragment) {
                    intent = new Intent(COMMUNITY_ADD);
                    sendBroadcast(intent);
                }
            }
        });
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation_view);
        viewPager = (NoScrollViewPager) findViewById(R.id.view_pager);
        viewPager.setNoScroll(true);  //设置不滑动
        viewPager.setOffscreenPageLimit(1);  //设置预加载1个
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        viewPager.setOnTouchListener(new View.OnTouchListener() {  //禁止ViewPager滑动
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
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
     * 获取当前展示的fragment
     */
    public Fragment getFragment() {
        List<Fragment> fragments = fragmentManager.getFragments();
        for (int i = 0; i < fragments.size(); i++) {
            Fragment fragment = fragments.get(i);
            if (fragment != null && fragment.isAdded() && fragment.isMenuVisible()) {
                return fragment;
            }
        }
        return null;
    }


    /**
     * 底部设置监听
     */
    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.home:
                    viewPager.setCurrentItem(0);
                    addButton.setVisibility(View.GONE);
                    floatingActionButton.setVisibility(View.VISIBLE);
                    return true;
                case R.id.community:
                    viewPager.setCurrentItem(1);
                    floatingActionButton.setVisibility(View.GONE);
                    addButton.setVisibility(View.VISIBLE);
                    return true;
                case R.id.project:
                    viewPager.setCurrentItem(2);
                    addButton.setVisibility(View.GONE);
                    floatingActionButton.setVisibility(View.VISIBLE);
                    return true;
                case R.id.mine:
                    viewPager.setCurrentItem(3);
                    addButton.setVisibility(View.GONE);
                    floatingActionButton.setVisibility(View.GONE);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

   /* *//**
     * 利用反射，改变 item 中 mShiftingMode 的值 ,从而改变 BottomNavigationView 默认的效果
     *//*
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
    }*/
}
