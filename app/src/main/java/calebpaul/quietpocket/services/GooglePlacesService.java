package calebpaul.quietpocket.services;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import calebpaul.quietpocket.Constants;
import calebpaul.quietpocket.models.Place;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by calebpaul on 1/31/17.
 */

public class GooglePlacesService {
    public static final String TAG = GooglePlacesService.class.getSimpleName();


    // TODO - weight query with current location
    // TODO - handle "W/okhttp3.OkHttpClient: A connection to https://maps.googleapis.com/ was leaked. Did you forget to close a response body?" error
    // TODO - handle next page token

    public static void findPlaces(String queryText, Callback callback) {
        OkHttpClient client = new OkHttpClient.Builder().build();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(Constants.GOOGLE_PLACES_BASE_URL).newBuilder();
        urlBuilder.addQueryParameter(Constants.GOOGLE_PLACES_QUERY, queryText);
        urlBuilder.addQueryParameter(Constants.GOOGLE_PLACES_LOCATION, "42.3675294,-71.186966");
        urlBuilder.addQueryParameter(Constants.GOOGLE_PLACES_RADIUS, "10000");
        urlBuilder.addQueryParameter(Constants.GOOGLE_PLACES_KEY, Constants.GOOGLE_PLACES_API_KEY);
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder().url(url).build();

        Log.v(TAG, ">>CALL URL<< - " + url);

        Call call = client.newCall(request);
        call.enqueue(callback);

    }

    public static ArrayList<Place> processPlaces(Response response) {

        ArrayList<Place> newPlaces = new ArrayList<>();

        try {

            String jsonData = response.body().string();

            if (response.isSuccessful()) {
                JSONObject places = new JSONObject(jsonData);
                JSONArray resultsJSON = places.getJSONArray("results");

                for (int i = 0; i < resultsJSON.length(); i++) {

                    String latitude = resultsJSON.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lat");
                    String longitude = resultsJSON.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lng");
                    String name = resultsJSON.getJSONObject(i).getString("name");

                    Place newPlace = new Place(latitude, longitude, name);
                    Log.v(TAG, ">>Place<< " + newPlace.getmName() + ": " + newPlace.getmLatitude() + "," + newPlace.getmLongitude());
                    newPlaces.add(newPlace);

                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newPlaces;
    }

}
