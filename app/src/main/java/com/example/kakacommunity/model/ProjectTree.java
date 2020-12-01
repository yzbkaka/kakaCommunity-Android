package com.example.kakacommunity.model;

public class ProjectTree {

    /**
     * 查询id
     */
    private String id;

    /**
     * 查询项目分类名字
     */
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ProjectTree{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
