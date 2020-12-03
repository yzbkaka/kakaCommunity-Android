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
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.kakacommunity.MyApplication;
import com.example.kakacommunity.R;
import com.example.kakacommunity.db.MyDataBaseHelper;
import com.example.kakacommunity.home.WebActivity;
import com.example.kakacommunity.model.Project;
import com.example.kakacommunity.model.ProjectTree;
import com.example.kakacommunity.utils.HttpUtil;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

import static com.example.kakacommunity.MainActivity.bottomNavigationView;
import static com.example.kakacommunity.constant.kakaCommunityConstant.ANDROID_ADDRESS;
import static com.example.kakacommunity.constant.kakaCommunityConstant.PROJECT_TOP;

public class ProjectFragment extends Fragment {

    private ProjectBroadcastReceiver projectBroadcastReceiver;

    private ProgressDialog progressDialog;

    private MyDataBaseHelper dataBaseHelper;

    private RefreshLayout refreshLayout;

    private RecyclerView recyclerView;

    private List<ProjectTree> projectTreeList = new ArrayList<>();

    private List<Project> projectList = new ArrayList<>();

    private ProjectAdapter projectAdapter;

    private volatile int tabNum = 0;

    private volatile int pageNum = 1;

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project, container, false);
        projectBroadcastReceiver = new ProjectBroadcastReceiver();
        dataBaseHelper = MyDataBaseHelper.getInstance();
        refreshLayout = (RefreshLayout) view.findViewById(R.id.project_swipe_refresh_layout);
        initRefreshView();
        recyclerView = (RecyclerView) view.findViewById(R.id.project_recycler_view);
        initRecyclerView();
        return view;
    }

    private void initRefreshView() {
        refreshLayout.setPrimaryColorsId(R.color.colorPrimary);
        refreshLayout.setRefreshHeader(new ClassicsHeader(MyApplication.getContext()).setAccentColorId(R.color.white));
        refreshLayout.setRefreshFooter(new ClassicsFooter(MyApplication.getContext()));
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                projectList.clear();
                getProjectJSON(1, 0);
                refreshlayout.finishRefresh();

            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                getProjectJSON(pageNum, tabNum);
                refreshlayout.finishLoadMore();

            }
        });
    }

    private void initRecyclerView() {
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        projectAdapter = new ProjectAdapter(projectList);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(projectAdapter);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        queryProjectTreeList();
        projectAdapter.setOnItemCLickListener(new ProjectAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String link = projectList.get(position).getLink();
                String title = projectList.get(position).getTitle();
                Intent intent = new Intent(MyApplication.getContext(), WebActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("url", link);
                startActivity(intent);
            }
        });
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
        }else {
            getProjectJSON(pageNum, tabNum);
        }
    }

    private void getProjectJSON(int page, int tab) {
        HttpUtil.OkHttpGET(ANDROID_ADDRESS + "/project" + "/list" + "/" + page + "/json?cid=" + projectTreeList.get(tab).getId(),
                new okhttp3.Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        closeProgressDialog();
                        Toast.makeText(MyApplication.getContext(), "获取数据失败", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseData = response.body().string();
                        parseProjectJSON(responseData);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                projectAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
        if (tabNum == projectList.size()) {
            tabNum = 0;
            page++;
        }
        if (tab == tabNum) {
            tabNum++;
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
                        getProjectJSON(pageNum, tabNum);
                    }
                });
            }
        });
    }

    private void parseProjectJSON(String responseData) {
        try {
            JSONObject jsonData = new JSONObject(responseData);
            JSONObject data = jsonData.getJSONObject("data");
            JSONArray datas = data.getJSONArray("datas");
            for (int i = 0; i < datas.length(); i++) {
                JSONObject jsonObject = datas.getJSONObject(i);
                Project project = new Project();
                project.setAuthor(jsonObject.getString("author"));
                project.setTitle(jsonObject.getString("title"));
                project.setImageLink(jsonObject.getString("envelopePic"));
                project.setChapterName(jsonObject.getString("chapterName"));
                project.setLink(jsonObject.getString("link"));
                project.setDate(jsonObject.getString("niceDate"));
                projectList.add(project);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    class ProjectBroadcastReceiver extends BroadcastReceiver {
        public ProjectBroadcastReceiver() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(PROJECT_TOP);
            getActivity().registerReceiver(this, intentFilter);
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            recyclerView.smoothScrollToPosition(0);
        }
    }
}
