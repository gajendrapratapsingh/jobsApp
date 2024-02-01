package com.example.util;

public class Events {
    public static class Intro {

    }

    public static class ProfileUpdate {

    }

    public static class SaveJob {
        private String jobId;
        private boolean isSave;
        private boolean isRemoved = false;

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public boolean isSave() {
            return isSave;
        }

        public void setSave(boolean save) {
            isSave = save;
        }

        public boolean isRemoved() {
            return isRemoved;
        }

        public void setRemoved(boolean removed) {
            isRemoved = removed;
        }
    }
}
