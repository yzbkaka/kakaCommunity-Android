package com.example.kakacommunity.project;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.kakacommunity.R;
import com.example.kakacommunity.db.MyDataBaseHelper;
import com.example.kakacommunity.model.ProjectTree;
import com.example.kakacommunity.utils.HttpUtil;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

import static com.example.kakacommunity.constant.kakaCommunityConstant.ANDROID_ADDRESS;

public class ProjectFragment extends Fragment {

    private TabLayout tabLayout;

    private ViewPager viewPager;

    private ProgressDialog progressDialog;

    private MyDataBaseHelper dataBaseHelper;

    private List<ProjectTree> projectTreeList = new ArrayList<>();

    private List<Fragment> fragmentList = new ArrayList<>();


    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project, container, false);
        dataBaseHelper = MyDataBaseHelper.getInstance();
        tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        viewPager = (ViewPager) view.findViewById(R.id.tab_view_pager);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        queryProjectTreeList();
        initViewPager();
    }

    private void queryProjectTreeList() {
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        Cursor cursor = db.query("ProjectTree", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex("id"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                ProjectTree projectTree = new ProjectTree();
                projectTree.setId(id);
                projectTree.setName(name);
                projectTreeList.add(projectTree);
            } while (cursor.moveToNext());
        }
        cursor.close();
        if (projectTreeList.size() == 0) {
            getProjectTreeJson();
        }
    }

    private void getProjectTreeJson() {
        showProgressDialog();
        HttpUtil.OkHttpGET(ANDROID_ADDRESS + "/project" + "/tree" + "/json", new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                parseProjectTreeJSON(responseData);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                    }
                });
            }
        });
    }

    private void parseProjectTreeJSON(String responseData) {
        try {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            JSONObject jsonData = new JSONObject(responseData);
            JSONArray datas = jsonData.getJSONArray("data");
            for (int i = 0; i < datas.length(); i++) {
                ContentValues contentValues = new ContentValues();
                ProjectTree projectTree = new ProjectTree();
                JSONObject jsonObject = datas.getJSONObject(i);
                String id = jsonObject.getString("id");
                String name = jsonObject.getString("name");
                contentValues.put("id", id);
                contentValues.put("name", name);
                projectTree.setId(id);
                projectTree.setName(name);
                db.insert("ProjectTree", null, contentValues);
                Log.e("projectTree", "id:" + id + "||" + "name:" + name);
                projectTreeList.add(projectTree);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initViewPager() {
        for (int i = 0; i < projectTreeList.size(); i++) {  //添加标签
            tabLayout.addTab(tabLayout.newTab().setText(projectTreeList.get(i).getName()));
            Fragment fragment = new ShowProjectFragment(projectTreeList.get(i).getId());
            fragmentList.add(fragment);
        }
        viewPager.setAdapter(new TabFragmentAdapter(getFragmentManager(), fragmentList, projectTreeList));
        tabLayout.setupWithViewPager(viewPager);
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
