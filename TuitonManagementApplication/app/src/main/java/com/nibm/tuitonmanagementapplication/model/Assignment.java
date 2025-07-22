package com.nibm.tuitonmanagementapplication.model;

public class Assignment {
    private String id;
    private String title;
    private String course;
    private String description;
    private String fileUrl;
    private String batch;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }
}
