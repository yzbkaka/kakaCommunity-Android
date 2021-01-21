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
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.kakacommunity.R;
import com.example.kakacommunity.base.BaseFragment;
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

    private ImageView imageView;

    private ViewPager viewPager;

    private MyDataBaseHelper dataBaseHelper;

    private List<ProjectTree> projectTreeList = new ArrayList<>();

    private List<Fragment> fragmentList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project, container, false);
        dataBaseHelper = MyDataBaseHelper.getInstance();
        tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        imageView = (ImageView) view.findViewById(R.id.classify);
        viewPager = (ViewPager) view.findViewById(R.id.tab_view_pager);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        queryProjectTreeList();
        initViewPager();
    }


    @Override
    public void onResume() {
        super.onResume();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPopWindows(v);
            }
        });
    }

    private void startPopWindows(View v) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_pop_windows, null);
        PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.classify_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 4));
        RecycleViewGridAdapter adapter = new RecycleViewGridAdapter(popupWindow);
        recyclerView.setAdapter(adapter);
        popupWindow.showAsDropDown(v);

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
        //showProgressDialog();
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
                        initViewPager();
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
                //String name = jsonObject.getString("name");
                String name = String.valueOf(Html.fromHtml(jsonObject.getString("name")));
                contentValues.put("id", id);
                contentValues.put("name", name);
                projectTree.setId(id);
                projectTree.setName(name);
                db.insert("ProjectTree", null, contentValues);
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

    class RecycleViewGridAdapter extends RecyclerView.Adapter<RecycleViewGridAdapter.ViewHolder> {

        private PopupWindow popupWindow;

        public RecycleViewGridAdapter(PopupWindow popupWindow) {
            this.popupWindow = popupWindow;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private TextView textView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = (TextView) itemView.findViewById(R.id.item_classify);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.classify_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.textView.setText(projectTreeList.get(position).getName());
            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewPager.setCurrentItem(position);
                    popupWindow.dismiss();
                }
            });
        }

        @Override
        public int getItemCount() {
            return projectTreeList.size();
        }
    }
}
