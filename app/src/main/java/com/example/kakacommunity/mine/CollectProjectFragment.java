package com.example.kakacommunity.mine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.kakacommunity.R;
import com.example.kakacommunity.model.Project;
import com.example.kakacommunity.project.ProjectAdapter;

import java.util.ArrayList;
import java.util.List;

public class CollectProjectFragment extends Fragment {

    private RecyclerView recyclerView;

    private ProjectAdapter collectProjectAdapter;

    private List<Project> collectProjectList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collect_project, container, false);
        recyclerView = (RecyclerView)view.findViewById(R.id.collect_project_recycler_view);
        initRecyclerView();
        return view;
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
                /*String link = collectProjectList.get(position).getLink();
                String title = collectProjectList.get(position).getTitle();
                Intent intent = new Intent(MyApplication.getContext(), WebActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("url", link);
                startActivity(intent);*/
            }
        });
    }
}
