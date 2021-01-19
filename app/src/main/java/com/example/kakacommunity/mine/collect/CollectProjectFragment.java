package com.example.kakacommunity.mine.collect;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.kakacommunity.R;
import com.example.kakacommunity.base.MyApplication;
import com.example.kakacommunity.db.MyDataBaseHelper;
import com.example.kakacommunity.home.WebActivity;
import com.example.kakacommunity.model.Project;
import com.example.kakacommunity.project.ProjectAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.example.kakacommunity.constant.kakaCommunityConstant.TYPE_PROJECT;

public class CollectProjectFragment extends Fragment {

    private MyDataBaseHelper dataBaseHelper;

    private RecyclerView recyclerView;

    private ProjectAdapter collectProjectAdapter;

    private List<Project> collectProjectList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collect_project, container, false);
        dataBaseHelper = MyDataBaseHelper.getInstance();
        recyclerView = (RecyclerView)view.findViewById(R.id.collect_project_recycler_view);
        queryCollectProject();
        initRecyclerView();
        return view;
    }

    private void queryCollectProject() {
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        Cursor cursor = db.query("Collect", null, "type = ?", new String[]{TYPE_PROJECT}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Project project = new Project();
                String author = cursor.getString(cursor.getColumnIndex("author"));
                project.setAuthor(author);
                String imageLink = cursor.getString(cursor.getColumnIndex("image_link"));
                project.setImageLink(imageLink);
                String title = cursor.getString(cursor.getColumnIndex("title"));
                project.setTitle(title);
                String link = cursor.getString(cursor.getColumnIndex("link"));
                project.setLink(link);
                String niceDate = cursor.getString(cursor.getColumnIndex("save_date"));
                project.setDate(niceDate);
                String chapterName = cursor.getString(cursor.getColumnIndex("chapter_name"));
                project.setChapterName(chapterName);
                project.setCollect(true);
                collectProjectList.add(project);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void initRecyclerView() {
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        collectProjectAdapter = new ProjectAdapter(collectProjectList);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(collectProjectAdapter);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        collectProjectAdapter.setOnItemCLickListener(new ProjectAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String link = collectProjectList.get(position).getLink();
                String title = collectProjectList.get(position).getTitle();
                Intent intent = new Intent(MyApplication.getContext(), WebActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("url", link);
                startActivity(intent);
            }

            @Override
            public void onItemCollectClick(int position) {
                String only = collectProjectList.get(position).getLink();
                collectProjectList.remove(position);
                collectProjectAdapter.notifyDataSetChanged();
                Toast.makeText(MyApplication.getContext(), "取消收藏成功", Toast.LENGTH_SHORT).show();
                deleteCollectProject(only);
            }
        });
    }

    private void deleteCollectProject(String only) {
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        db.delete("Collect", "link = ?", new String[]{only});
    }
}
