package com.nibm.tuitonmanagementapplication.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Teacher implements Parcelable {
    private String id;
    private String fullName;
    private String email;
    private String phone;
    private String subject;
    private String qualification;
    private String experience;
    private String joinDate;

    public Teacher() {}

    protected Teacher(Parcel in) {
        id = in.readString();
        fullName = in.readString();
        email = in.readString();
        phone = in.readString();
        subject = in.readString();
        qualification = in.readString();
        experience = in.readString();
        joinDate = in.readString();
    }

    public static final Creator<Teacher> CREATOR = new Creator<Teacher>() {
        @Override
        public Teacher createFromParcel(Parcel in) {
            return new Teacher(in);
        }

        @Override
        public Teacher[] newArray(int size) {
            return new Teacher[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(fullName);
        dest.writeString(email);
        dest.writeString(phone);
        dest.writeString(subject);
        dest.writeString(qualification);
        dest.writeString(experience);
        dest.writeString(joinDate);
    }

    // Getters and setters (same as before)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    public String getJoinDate() { return joinDate; }
    public void setJoinDate(String joinDate) { this.joinDate = joinDate; }
}