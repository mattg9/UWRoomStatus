package app.band.runawaynation.matth.uwroomstatus.model;

import android.os.Parcel;
import android.os.Parcelable;

public class CourseItem implements Parcelable {
    public void setClassNumber(String classNumber) {
        this.classNumber = classNumber;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getClassNumber() {

        return classNumber;
    }

    public String getSubject() {
        return subject;
    }

    private String classNumber;
    private String subject;

   @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.classNumber);
        dest.writeString(this.subject);
    }

    protected CourseItem(Parcel in) {
        this.classNumber = in.readString();
        this.subject = in.readString();
    }

    public static final Parcelable.Creator<CourseItem> CREATOR = new Parcelable.Creator<CourseItem>() {
        @Override
        public CourseItem createFromParcel(Parcel source) {
            return new CourseItem(source);
        }

        @Override
        public CourseItem[] newArray(int size) {
            return new CourseItem[size];
        }
    };
}