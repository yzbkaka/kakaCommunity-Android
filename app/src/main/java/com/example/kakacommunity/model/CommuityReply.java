package com.example.kakacommunity.model;

import android.widget.ImageView;
import android.widget.TextView;

import com.example.kakacommunity.utils.StringUtil;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CommuityReply implements Serializable {

    private String id;

    private String imageUrl;

    private String name;

    private String time;

    private String content;

    private String replyCount;

    private List<CommentReply> commentReplyList = new ArrayList<>();

    public List<CommentReply> getCommentReplyList() {
        return commentReplyList;
    }

    public void setCommentReplyList(List<CommentReply> commentReplyList) {
        this.commentReplyList = commentReplyList;
    }

    public String getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(String replyCount) {
        this.replyCount = replyCount;
    }

    public String getId() {
        return id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
