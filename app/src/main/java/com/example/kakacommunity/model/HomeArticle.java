package com.example.kakacommunity.model;

public class HomeArticle {

    private int id;

    /**
     * 分享人
     */
    private String shareUser;

    /**
     * 标题
     */
    private String title;

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShareUser() {
        return shareUser;
    }

    public void setShareUser(String shareUser) {
        this.shareUser = shareUser;
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
}
