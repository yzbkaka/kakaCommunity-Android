package com.example.kakacommunity.project;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.kakacommunity.MyApplication;
import com.example.kakacommunity.R;
import com.example.kakacommunity.db.MyDataBaseHelper;
import com.example.kakacommunity.header.PhoenixHeader;
import com.example.kakacommunity.home.WebActivity;
import com.example.kakacommunity.model.Project;
import com.example.kakacommunity.utils.ActivityUtil;
import com.example.kakacommunity.utils.HttpUtil;
import com.scwang.smart.refresh.footer.BallPulseFooter;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

import static com.example.kakacommunity.constant.kakaCommunityConstant.ANDROID_ADDRESS;
import static com.example.kakacommunity.constant.kakaCommunityConstant.PROJECT_TOP;
import static com.example.kakacommunity.constant.kakaCommunityConstant.TYPE_ARTICLE;
import static com.example.kakacommunity.constant.kakaCommunityConstant.TYPE_PROJECT;

public class ShowProjectFragment extends Fragment {

    private MyDataBaseHelper dataBaseHelper;

    private ProjectBroadcastReceiver projectBroadcastReceiver;

    private RefreshLayout refreshLayout;

    private RecyclerView recyclerView;

    private List<Project> projectList = new ArrayList<>();

    private ProjectAdapter projectAdapter;

    private volatile int curPage = 1;

    private String id;

    private ImageView errorImage;

    public ShowProjectFragment(String id) {
        this.id = id;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_project,container,false);
        errorImage = (ImageView)view.findViewById(R.id.project_error);
        dataBaseHelper = MyDataBaseHelper.getInstance();
        projectBroadcastReceiver = new ProjectBroadcastReceiver();
        refreshLayout = (RefreshLayout) view.findViewById(R.id.project_swipe_refresh_layout);
        initRefreshView();
        recyclerView = (RecyclerView) view.findViewById(R.id.project_recycler_view);
        initRecyclerView();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getProjectJSON(1);
        projectAdapter.setOnItemCLickListener(new ProjectAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                saveReadHistory(projectList.get(position));
                String link = projectList.get(position).getLink();
                String title = projectList.get(position).getTitle();
                Intent intent = new Intent(MyApplication.getContext(), WebActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("url", link);
                startActivity(intent);
            }
        });
        errorImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getProjectJSON(1);
            }
        });
    }

    private void initRefreshView() {
        refreshLayout.setPrimaryColorsId(R.color.colorPrimary);
        refreshLayout.setRefreshHeader(new PhoenixHeader(MyApplication.getContext()));
        refreshLayout.setRefreshFooter(new BallPulseFooter(MyApplication.getContext()).setSpinnerStyle(SpinnerStyle.Scale));
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                projectList.clear();
                getProjectJSON(1);
                curPage = 1;
                refreshlayout.finishRefresh();

            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                curPage++;
                getProjectJSON(curPage);
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

    private void getProjectJSON(int page) {
        HttpUtil.OkHttpGET(ANDROID_ADDRESS + "/project" + "/list" + "/" + page + "/json?cid=" + id,
                new okhttp3.Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        if(!ActivityUtil.isDestroy(getActivity())){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    errorImage.setVisibility(View.VISIBLE);
                                    Toast.makeText(MyApplication.getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseData = response.body().string();
                        parseProjectJSON(responseData);
                        if(!ActivityUtil.isDestroy(getActivity())) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    errorImage.setVisibility(View.GONE);
                                    projectAdapter.notifyDataSetChanged();
                                }
                            });
                        }
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

    private void saveReadHistory(Project project) {
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("type",TYPE_PROJECT);
        contentValues.put("author", project.getAuthor());
        contentValues.put("image_link",project.getImageLink());
        contentValues.put("title", project.getTitle());
        contentValues.put("link", project.getLink());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date();
        contentValues.put("read_date", dateFormat.format(date));
        contentValues.put("chapter_name", project.getChapterName());
        db.insert("History", null, contentValues);
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
