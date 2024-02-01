package com.example.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class Filter implements Parcelable {
    private String filterId;
    private String filterName;
    private String filterType;
    private boolean isSelected = false;

    public static final String CATEGORY = "category", COMPANY = "company", CITY = "city", JOB_TYPE = "job_type", QUALIFICATION = "qualification", SALARY = "salary";

    public Filter() {

    }

    public Filter(String id, String name, String type, boolean isCheck) {
        this.filterId = id;
        this.filterName = name;
        this.filterType = type;
        this.isSelected = isCheck;
    }

    protected Filter(Parcel in) {
        filterId = in.readString();
        filterName = in.readString();
        filterType = in.readString();
        isSelected = in.readByte() != 0;
    }

    public static final Creator<Filter> CREATOR = new Creator<Filter>() {
        @Override
        public Filter createFromParcel(Parcel in) {
            return new Filter(in);
        }

        @Override
        public Filter[] newArray(int size) {
            return new Filter[size];
        }
    };

    public String getFilterId() {
        return filterId;
    }

    public String getFilterName() {
        return filterName;
    }

    public String getFilterType() {
        return filterType;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Filter)) return false;
        Filter that = (Filter) o;
        return isSelected == that.isSelected && Objects.equals(filterId, that.filterId) && Objects.equals(filterName, that.filterName) && Objects.equals(filterType, that.filterType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filterId, filterName, filterType, isSelected);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(filterId);
        parcel.writeString(filterName);
        parcel.writeString(filterType);
        parcel.writeByte((byte) (isSelected ? 1 : 0));
    }
}
