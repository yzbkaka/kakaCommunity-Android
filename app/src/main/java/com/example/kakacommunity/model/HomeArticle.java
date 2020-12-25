package com.example.kakacommunity.model;

import com.example.kakacommunity.utils.StringUtil;

public class HomeArticle {

    private int id;

    /**
     * 是否是“新”
     */
    private boolean fresh;

    /**
     * 作者
     */
    private String author;

    /**
     * 标题
     */
    private String title;


    private String content;

    /**
     * 文章链接
     */
    private String link;

    /**
     * 分享时间
     */
    private String niceDate;

    /**
     * 父分类
     */
    private String superChapterName;

    /**
     * 子分类
     */
    private String chapterName;

    /**
     * 标签
     */
    private String tag;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isFresh() {
        return fresh;
    }

    public void setFresh(boolean fresh) {
        this.fresh = fresh;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getNiceDate() {
        return niceDate;
    }

    public void setNiceDate(String niceDate) {
        this.niceDate = niceDate;
    }

    public String getSuperChapterName() {
        return superChapterName;
    }

    public void setSuperChapterName(String superChapterName) {
        this.superChapterName = superChapterName;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    @Override
    public String toString() {
        return "HomeArticle{" +
                "id=" + id +
                ", author='" + author + '\'' +
                ", title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", niceDate='" + niceDate + '\'' +
                ", superChapterName='" + superChapterName + '\'' +
                ", chapterName='" + chapterName + '\'' +
                '}';
    }
}
