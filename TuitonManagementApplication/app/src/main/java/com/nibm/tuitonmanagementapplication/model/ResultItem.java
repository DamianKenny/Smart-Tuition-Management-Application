package com.nibm.tuitonmanagementapplication.model;

public class ResultItem {
    private String courseTitle;
    private String module;
    private String finalGrade;

    public ResultItem() {
        // Empty constructor needed for Firestore
    }

    public ResultItem(String courseTitle, String module, String finalGrade) {
        this.courseTitle = courseTitle;
        this.module = module;
        this.finalGrade = finalGrade;
    }

    // Getters and setters
    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getFinalGrade() { return finalGrade; }
    public void setFinalGrade(String finalGrade) { this.finalGrade = finalGrade; }
}