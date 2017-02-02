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

    public static void findPlaces(String queryText, Callback callback) {
        OkHttpClient client = new OkHttpClient.Builder().build();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(Constants.GOOGLE_PLACES_BASE_URL).newBuilder();
        urlBuilder.addQueryParameter("query", Constants.GOOGLE_PLACES_QUERY_PARAMS);
        urlBuilder.addQueryParameter("location", "42.3675294,-71.186966");
        urlBuilder.addQueryParameter("radius", "10000");
        urlBuilder.addQueryParameter("key", "AIzaSyA3JNepLgUVh8TB_TQqRnZOC_UFHQyOUz8");
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder().url(url).build();

        Log.v(TAG, ">>CALL URL<< - " + url);

        Call call = client.newCall(request);
        call.enqueue(callback);

    }
}
