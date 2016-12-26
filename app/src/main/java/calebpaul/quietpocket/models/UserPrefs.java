package calebpaul.quietpocket.models;

import io.realm.RealmObject;

/**
 * Created by calebpaul on 12/26/16.
 */

public class UserPrefs extends RealmObject {

    private String realmLatLong;

    public String getRealmLatLong() {
        return realmLatLong;
    }

    public void setRealmLatLong(String realmLatLong) {
        this.realmLatLong = realmLatLong;
    }

}
