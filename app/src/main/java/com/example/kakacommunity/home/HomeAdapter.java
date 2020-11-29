package com.example.kakacommunity.home;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kakacommunity.R;
import com.example.kakacommunity.model.HomeArticle;

import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    private List<HomeArticle> homeArticleList;

    private OnItemClickListener onItemClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView author;
        TextView time;
        TextView title;
        TextView chapter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.home_item_layout);
            author = (TextView)itemView.findViewById(R.id.home_item_author);
            time = (TextView)itemView.findViewById(R.id.home_item_time);
            title = (TextView)itemView.findViewById(R.id.home_item_title);
            chapter = (TextView)itemView.findViewById(R.id.home_item_chapter);
        }
    }

    public HomeAdapter(List<HomeArticle> homeArticleList) {
        this.homeArticleList = homeArticleList;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        HomeArticle homeArticle = homeArticleList.get(position);
        holder.author.setText(homeArticle.getAuthor());
        holder.time.setText(homeArticle.getNiceDate());
        holder.title.setText(homeArticle.getTitle());
        holder.chapter.setText(homeArticle.getChapterName());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return homeArticleList.size();
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemCLickListener(OnItemClickListener onItemCLickListener){
        this.onItemClickListener = onItemCLickListener;
    }
}