package calebpaul.quietpocket.models;

/**
 * Created by calebpaul on 2/10/17.
 */

public class Place {
    private String mLatitude;
    private String mLongitude;
    private String mName;

    public Place(String mLatitude, String mLongitude, String mName) {
        this.mLatitude = mLatitude;
        this.mLongitude = mLongitude;
        this.mName = mName;
    }

    public String getmLatitude() {
        return mLatitude;
    }

    public void setmLatitude(String mLatitude) {
        this.mLatitude = mLatitude;
    }

    public String getmLongitude() {
        return mLongitude;
    }

    public void setmLongitude(String mLongitude) {
        this.mLongitude = mLongitude;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }
}
