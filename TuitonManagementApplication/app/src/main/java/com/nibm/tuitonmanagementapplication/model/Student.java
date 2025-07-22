package com.nibm.tuitonmanagementapplication.model;

public class Student {
    private String id;
    private long assignmentDate;
    private String courseName;
    private String studentBatch;
    private String studentEmail;
    private String studentId;
    private String studentModule;
    private String studentName;
    private String teacherEmail;
    private String teacherId;
    private String teacherName;
    private String teacherQualification;
    private String teacherSubject;

    // Empty constructor required for Firestore
    public Student() {}

    // 🔼 Getters
    public String getId() {
        return id;
    }

    public long getAssignmentDate() {
        return assignmentDate;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getStudentBatch() {
        return studentBatch;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentModule() {
        return studentModule;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getTeacherEmail() {
        return teacherEmail;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public String getTeacherQualification() {
        return teacherQualification;
    }

    public String getTeacherSubject() {
        return teacherSubject;
    }

    // 🔽 Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setAssignmentDate(long assignmentDate) {
        this.assignmentDate = assignmentDate;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setStudentBatch(String studentBatch) {
        this.studentBatch = studentBatch;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setStudentModule(String studentModule) {
        this.studentModule = studentModule;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public void setTeacherEmail(String teacherEmail) {
        this.teacherEmail = teacherEmail;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public void setTeacherQualification(String teacherQualification) {
        this.teacherQualification = teacherQualification;
    }

    public void setTeacherSubject(String teacherSubject) {
        this.teacherSubject = teacherSubject;
    }

    // 🔁 Aliased getters (used in filtering or display logic)
    public String getFullName() {
        return getStudentName();
    }

    public String getEmail() {
        return getStudentEmail();
    }

    public String getModule() {
        return getStudentModule();
    }

    public String getBatch() {
        return getStudentBatch();
    }
}
