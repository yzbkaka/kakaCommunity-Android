package com.example.kakacommunity.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kakacommunity.R;
import com.example.kakacommunity.model.Project;

import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {

    private List<Project> projectList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView author;
        ImageView imageView;
        TextView time;
        TextView title;
        TextView chapter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.project_item_layout);
            author = (TextView)itemView.findViewById(R.id.project_item_author);
            imageView = (ImageView)itemView.findViewById(R.id.project_item_image);
            time = (TextView)itemView.findViewById(R.id.project_item_date);
            title = (TextView)itemView.findViewById(R.id.project_item_title);
            chapter = (TextView)itemView.findViewById(R.id.project_item_chapter);
        }
    }

    public ProjectAdapter(List<Project> projectList) {
        this.projectList = projectList;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        
    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
