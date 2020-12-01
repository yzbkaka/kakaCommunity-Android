package com.example.kakacommunity.project;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.kakacommunity.MyApplication;
import com.example.kakacommunity.R;
import com.example.kakacommunity.model.Project;
import com.example.kakacommunity.utils.StringUtil;

import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {

    private List<Project> projectList;

    private OnItemClickListener onItemClickListener;

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
            author = (TextView) itemView.findViewById(R.id.project_item_author);
            imageView = (ImageView) itemView.findViewById(R.id.project_item_image);
            time = (TextView) itemView.findViewById(R.id.project_item_date);
            title = (TextView) itemView.findViewById(R.id.project_item_title);
            chapter = (TextView) itemView.findViewById(R.id.project_item_chapter);
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
        Project project = projectList.get(position);
        holder.author.setText(project.getAuthor());
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.default_project_img)  //图片加载出来前，显示的图片
                .fallback( R.drawable.default_project_img)  //url为空的时候,显示的图片
                .error(R.drawable.default_project_img);  //图片加载失败后，显示的图片
        Glide.with(MyApplication.getContext())
                .load(project.getImageLink())
                .apply(options)
                .into(holder.imageView);
        Log.e("link", project.getImageLink());
        holder.time.setText(project.getDate());
        holder.title.setText(project.getTitle());
        holder.chapter.setText(project.getChapterName());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemCLickListener(OnItemClickListener onItemCLickListener) {
        this.onItemClickListener = onItemCLickListener;
    }
}
