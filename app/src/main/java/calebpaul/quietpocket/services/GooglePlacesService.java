package calebpaul.quietpocket.services;

import android.util.Log;

import calebpaul.quietpocket.Constants;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by calebpaul on 1/31/17.
 */

public class GooglePlacesService {
    public static final String TAG = GooglePlacesService.class.getSimpleName();


    // TODO - weight query with current location...
    // TODO - process response
    // TODO - handle "W/okhttp3.OkHttpClient: A connection to https://maps.googleapis.com/ was leaked. Did you forget to close a response body?" error

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

//    public String processPlaces(Response response) {
//        String place = null;
//        try {
//
//            String jsonData = response.body().string();
//
//            if (response.isSuccessful()) {
//                JSONObject placeJSON = new JSONObject(jsonData);
//                JSONArray resultsJSON = placeJSON.getJSONarray("results");
//
//                String latitude = ;
//                String longitude = ;
//                String name = ;
//
//                place = results.JSON.getJSONObject(0)
//            }
//        } catch () {
//
//        } catch () {
//
//        }
//        return place;
//    }

}