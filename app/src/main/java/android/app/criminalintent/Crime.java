package android.app.criminalintent;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class Crime {
    private UUID mId;           // holds the crime id
    private String mTitle;      // holds title of crime
    private Date mDate;         // date of the crime
    private int mRequiresPolice; // if crime requires police
    private boolean mSolved;    // is the crime solved?
    private Time mTime;
    private String mSuspect;

    public Crime() {
        this(UUID.randomUUID());
        mDate = new Date();
        mTime = new Time(Calendar.HOUR, Calendar.MINUTE, Calendar.SECOND);
    }

    public Crime(UUID id){
        mDate = new Date();
        mId = id;
        mTime = new Time(Calendar.HOUR, Calendar.MINUTE, Calendar.SECOND);
    }

    public String getPhotoFileName(){
        return "ING_" + getId().toString() + ".jpg";
    }


    public UUID getId() {
        return mId;
    }

    public String getSuspect() {
        return mSuspect; }

    public void setSuspect(String suspect) {
        mSuspect = suspect; }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Date getDate(){
        return mDate;
    }


    public void setDate(Date date) {
        mDate = date;
    }

    public Time getTime() {
        return mTime; }

    public void setTime(Time time){
        mTime = time; }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }

    public void setPolice(int setPolice) {
        mRequiresPolice = setPolice; }

    public int getRequiresPolice() {
        return mRequiresPolice; }


}
