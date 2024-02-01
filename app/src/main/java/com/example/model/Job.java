package com.example.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Job {

    @SerializedName("post_id")
    private String jobId;
    @SerializedName("post_title")
    private String jobTitle;
    @SerializedName("post_image")
    private String jobImage;
    @SerializedName("address")
    private String jobAddress;
    @SerializedName("salary")
    private String jobSalary;
    @SerializedName("location")
    private String jobLocation;
    @SerializedName("designation")
    private String jobDesignation;
    @SerializedName("skills")
    private String jobSkills;
    @SerializedName("applied_date")
    private String jobAppliedDate;
    @SerializedName("applied_status")
    private boolean isJobAppliedSeen;
    @SerializedName("date")
    private String jobDate;
    @SerializedName("phone")
    private String jobPhone;
    @SerializedName("email")
    private String jobEmail;
    @SerializedName("website")
    private String jobWebsite;
    @SerializedName("description")
    private String jobDescription;
    @SerializedName("qualification")
    private String jobQualification;
    @SerializedName("job_work_days")
    private String jobWorkDays;
    @SerializedName("job_work_time")
    private String jobWorkTime;
    @SerializedName("vacancy")
    private String jobVacancy;
    @SerializedName("job_type")
    private String jobType;
    @SerializedName("favourite")
    private boolean isJobSaved = false;
    @SerializedName("similar_jobs")
    public ArrayList<Job> similarJobList = new ArrayList<>();


    public Job() {
    }

    public String getJobId() {
        return jobId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getJobImage() {
        return jobImage;
    }

    public String getJobAddress() {
        return jobAddress;
    }

    public String getJobSalary() {
        return jobSalary;
    }

    public String getJobLocation() {
        return jobLocation;
    }

    public String getJobDesignation() {
        return jobDesignation;
    }

    public String getJobSkills() {
        return jobSkills;
    }

    public String getJobAppliedDate() {
        return jobAppliedDate;
    }

    public boolean isJobAppliedSeen() {
        return isJobAppliedSeen;
    }

    public String getJobDate() {
        return jobDate;
    }

    public String getJobPhone() {
        return jobPhone;
    }

    public String getJobEmail() {
        return jobEmail;
    }

    public String getJobWebsite() {
        return jobWebsite;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public String getJobQualification() {
        return jobQualification;
    }

    public String getJobWorkDays() {
        return jobWorkDays;
    }

    public String getJobWorkTime() {
        return jobWorkTime;
    }

    public String getJobVacancy() {
        return jobVacancy;
    }

    public String getJobType() {
        return jobType;
    }

    public boolean isJobSaved() {
        return isJobSaved;
    }

    public void setJobSaved(boolean jobSaved) {
        isJobSaved = jobSaved;
    }

    public ArrayList<Job> getSimilarJobList() {
        return similarJobList;
    }
}
