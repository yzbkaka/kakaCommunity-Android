package com.example.kakacommunity.project;

import android.app.ProgressDialog;
import android.content.ContentValues;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.kakacommunity.MyApplication;
import com.example.kakacommunity.R;
import com.example.kakacommunity.db.MyDataBaseHelper;
import com.example.kakacommunity.model.Project;
import com.example.kakacommunity.model.ProjectTree;
import com.example.kakacommunity.utils.HttpUtil;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

import static com.example.kakacommunity.constant.kakaCommunityConstant.ANDROID_ADDRESS;

public class ProjectFragment extends Fragment {

    private ProgressDialog progressDialog;

    private MyDataBaseHelper dataBaseHelper;

    private RefreshLayout refreshLayout;

    private RecyclerView recyclerView;

    private List<ProjectTree> projectTreeList = new ArrayList<>();

    private List<Project> projectList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project, container, false);
        dataBaseHelper = MyDataBaseHelper.getInstance();
        refreshLayout = (RefreshLayout) view.findViewById(R.id.project_swipe_refresh_layout);
        recyclerView = (RecyclerView) view.findViewById(R.id.project_recycler_view);
        queryProjectTreeList();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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
                closeProgressDialog();
                Toast.makeText(MyApplication.getContext(), "获取数据失败", Toast.LENGTH_SHORT).show();
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

    private void showProgressDialog() {
        if(progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
