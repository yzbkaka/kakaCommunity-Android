package com.example.kakacommunity.project;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.kakacommunity.base.MyApplication;
import com.example.kakacommunity.R;
import com.example.kakacommunity.db.MyDataBaseHelper;
import com.example.kakacommunity.model.Project;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.example.kakacommunity.constant.kakaCommunityConstant.TYPE_PROJECT;
import static com.example.kakacommunity.constant.kakaCommunityConstant.isReadHistory;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {

    private MyDataBaseHelper dataBaseHelper;

    private List<Project> projectList;

    private OnItemClickListener onItemClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView author;
        ImageView imageView;
        TextView time;
        TextView title;
        TextView chapter;
        ImageView collect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.project_item_layout);
            author = (TextView) itemView.findViewById(R.id.project_item_author);
            imageView = (ImageView) itemView.findViewById(R.id.project_item_image);
            time = (TextView) itemView.findViewById(R.id.project_item_date);
            title = (TextView) itemView.findViewById(R.id.project_item_title);
            chapter = (TextView) itemView.findViewById(R.id.project_item_chapter);
            collect = (ImageView)itemView.findViewById(R.id.project_item_collect);
        }
    }

    public ProjectAdapter(List<Project> projectList) {
        dataBaseHelper = MyDataBaseHelper.getInstance();
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
                .fallback(R.drawable.default_project_img)  //url为空的时候,显示的图片
                .error(R.drawable.default_project_img);  //图片加载失败后，显示的图片
        Glide.with(MyApplication.getContext())
                .load(project.getImageLink())
                .apply(options)
                .into(holder.imageView);
        holder.time.setText(project.getDate());
        if (isReadHistory) {
            holder.time.setVisibility(View.VISIBLE);
        } else {
            holder.time.setVisibility(View.GONE);
        }
        holder.title.setText(project.getTitle());
        holder.chapter.setText(project.getChapterName());
        if(project.isCollect()) {
            holder.collect.setImageResource(R.drawable.iscollect);
        }
        holder.collect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!project.isCollect()) {
                    String only = project.getLink();
                    if(queryOnly(only)) {
                        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("type",TYPE_PROJECT);
                        contentValues.put("author", project.getAuthor());
                        contentValues.put("image_link",project.getImageLink());
                        contentValues.put("title", project.getTitle());
                        contentValues.put("link", project.getLink());
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        Date date = new Date();
                        contentValues.put("save_date", dateFormat.format(date));
                        contentValues.put("chapter_name", project.getChapterName());
                        db.insert("Collect", null, contentValues);
                        Toast.makeText(MyApplication.getContext(), "收藏成功", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(MyApplication.getContext(), "已经收藏过啦", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    onItemClickListener.onItemCollectClick(position);
                }
            }
        });
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

    private boolean queryOnly(String only) {
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        Cursor cursor = db.query("Collect", null, "link = ?", new String[]{only}, null, null, null);
        if (cursor.getCount() == 0) {  //没有收藏过
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;  //收藏过
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemCollectClick(int position);
    }

    public void setOnItemCLickListener(OnItemClickListener onItemCLickListener) {
        this.onItemClickListener = onItemCLickListener;
    }
}
