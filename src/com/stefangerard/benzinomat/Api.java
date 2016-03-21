package com.stefangerard.benzinomat;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.stefangerard.benzinomat.objects.FuelStation;
import com.stefangerard.benzinomat.objects.FuelStationDetail;
import com.stefangerard.benzinomat.utils.PersistantData;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;

/**
 * Klasse in der alle API-calls verwaltet werden sowie sonstige Punkte die mit der API zu tun haben
 * @author Stefan Gerard
 *
 */
public class Api {

    public static final Api INSTANCE = new Api();

    public final String success = "success";
    private final static String TAG_STATIONS = "stations";
    private final static String TAG_STATION = "station";
    private List<FuelStation> fuelStations = new ArrayList<FuelStation>();
    private FuelStationDetail fuelStationDetail;
    private Activity activityTmp;

    private String apiUrl = "https://creativecommons.tankerkoenig.de/json";
    private String normalCall = "/list.php";
    private String detailCall = "/detail.php";
    private String apiKey = "<tankerkoenig api key goes here>";
    private double latitude;
    private double longitude;
    private int radius;
    private String sort;
    private String fuelType;
    private int resultCount;

    public Api() {
        sort = "dist";
    }

    /**
     * Setzt die globalen Variablen
     * 
     * @param latitudeStr String
     * @param longitudeStr  String
     * @param radiusStr  String
     * @param fuelTypeStr  String
     * @param resultCountStr  String
     * @param sort  String
     */
    public void setValues(String latitudeStr, String longitudeStr, String radiusStr, String fuelTypeStr,
            String resultCountStr, String sort) {
        this.latitude = Double.valueOf(latitudeStr);
        this.longitude = Double.valueOf(longitudeStr);

        radius = Integer.valueOf(radiusStr);
        if (radius > 25) {
            radius = 25;
        }
        fuelTypeStr = fuelTypeStr.toLowerCase();
        if (!fuelTypeStr.equals("diesel")) {
            fuelTypeStr = fuelTypeStr.substring(fuelTypeStr.indexOf(" ") + 1);
        }
        fuelType = fuelTypeStr;
        resultCount = Integer.valueOf(resultCountStr);
        this.sort = sort;
    }

    /**
     * Holt sich alle daten aus dem Speicher und setzt die globalen Variablen über setValues
     */
    private void fetchSavedData() {
        setValues(PersistantData.INSTANCE.getData(PersistantData.INSTANCE.latitudeKey, "49.260579", activityTmp),
                PersistantData.INSTANCE.getData(PersistantData.INSTANCE.longitudeKey, "7.360411", activityTmp),
                PersistantData.INSTANCE.getData(PersistantData.INSTANCE.searchRadiusKey, activityTmp),
                PersistantData.INSTANCE.getData(PersistantData.INSTANCE.fuelTypeKey, activityTmp),
                PersistantData.INSTANCE.getData(PersistantData.INSTANCE.resultCountKey, activityTmp),
                PersistantData.INSTANCE.getData(PersistantData.INSTANCE.sortKey, "dist", activityTmp));

    }

    /**
     * URL für den Api-Call wird erzeugt
     * @param id String (optional, wenn nicht benötigt id 'null' setzten
     * @return String
     */
    public String getCompleteApiUrl(String id) {
        String completeUrl = null;
        //id ist nicht 'null' wenn ein API call für die DetailsActivity gemacht wird
        if (id == null) {
            fetchSavedData();
            completeUrl = apiUrl + normalCall + "?lat=" + String.valueOf(latitude) + "&lng=" + String.valueOf(longitude)
                    + "&rad=" + String.valueOf(radius) + "&sort=" + this.sort + "&type=" + this.fuelType + "&apikey="
                    + apiKey;
        } else {
            completeUrl = apiUrl + detailCall + "?id=" + id + "&apikey=" + apiKey;
        }
        return completeUrl;

    }

    public String getJSONObject() {
        return getJSONObject(null);
    }

    /**
     * Ein API call wird durchgeführt
     * @param id String (optional, wenn nicht benötigt id 'null' setzten)
     * @return String (JSON Objekt)
     */
    public String getJSONObject(String id) {
        //id ist nicht 'null' wenn ein API call für die DetailsActivity gemacht wird
        if (id == null) {
            fuelStations.clear();
        }
        URL url = null;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL(getCompleteApiUrl(id));
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String data = readStream(in);

            JSONObject jsonObject = new JSONObject(data);
            return handleJsonArray(jsonObject, id);
        } catch (Exception e) {
            Log.e("error", "status 200; error");
            return null;
        } finally {
            urlConnection.disconnect();
        }
    }

    private String handleJsonArray(JSONObject jsonObject, String id) {
        /*  
         * id ist nicht 'null' wenn ein API call für die DetailsActivity gemacht wird
         * wenn id = null ist (das ist der API call der von der MainActivity stattfindet) muss anders mit dem JSONObject umgegangen werden,
         * da es in diesem Fall ein JSONArray ist
         */
        if (id == null) {
            return handleJsonArray(jsonObject);
        } else {
            try {
                JSONObject station = jsonObject.getJSONObject(TAG_STATION);
                id = station.getString("id");
                String name = station.getString("name");
                String brand = station.getString("brand");
                String street = station.getString("street");
                String houseNumber = station.getString("houseNumber");
                String postCode = station.getString("postCode");
                String place = station.getString("place");
                String overrides = station.getString("overrides");
                String isOpen = station.getString("isOpen");
                String e5 = station.getString("e5");
                String e10 = station.getString("e10");
                String diesel = station.getString("diesel");
                String lat = station.getString("lat");
                String lng = station.getString("lng");
                String state = station.getString("state");
                String openingTimes = station.getString("openingTimes");

                fuelStationDetail = new FuelStationDetail(id, name, brand, street, houseNumber,
                        Integer.valueOf(postCode), place, overrides, Boolean.valueOf(isOpen), Double.valueOf(e5),
                        Double.valueOf(e10), Double.valueOf(diesel), Double.valueOf(lat), Double.valueOf(lng), state,
                        openingTimes);
                return success;

            } catch (Exception e) {
                return null;
            }
        }
    }

    private String handleJsonArray(JSONObject jsonObject) {
        try {
            JSONArray stations = jsonObject.getJSONArray(TAG_STATIONS);
            for (int i = 0; i < stations.length(); i++) {
                String name = stations.getJSONObject(i).getString("name");
                String lat = stations.getJSONObject(i).getString("lat");
                String lng = stations.getJSONObject(i).getString("lng");
                String brand = stations.getJSONObject(i).getString("brand");
                String distance = stations.getJSONObject(i).getString("dist");
                String price = stations.getJSONObject(i).getString("price");
                String id = stations.getJSONObject(i).getString("id");
                String street = stations.getJSONObject(i).getString("street");
                String houseNumber = stations.getJSONObject(i).getString("houseNumber");
                String postCode = stations.getJSONObject(i).getString("postCode");
                String place = stations.getJSONObject(i).getString("place");
                String isOpen = stations.getJSONObject(i).getString("isOpen");

                if (i == (resultCount)) {
                    break;
                }
                FuelStation fuelStation = new FuelStation(name, Double.valueOf(lat), Double.valueOf(lng), brand,
                        Double.valueOf(distance), Double.valueOf(price), id, street, houseNumber,
                        Integer.valueOf(postCode), place, Boolean.valueOf(isOpen));
                fuelStations.add(fuelStation);
            }
            return success;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * wandelt InputStream in String um
     * @param in InputStream
     * @return String
     */
    private String readStream(InputStream in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public List<FuelStation> getFuelStations() {
        return fuelStations;
    }

    public void setFuelStations(List<FuelStation> fuelStations) {
        this.fuelStations = fuelStations;
    }

    public void setActivityTmp(Activity activityTmp) {
        this.activityTmp = activityTmp;
    }

    public FuelStationDetail getFuelStationDetail() {
        return fuelStationDetail;
    }

    /**
     * schaut ob eine Internetverbindung vorhanden ist, falls nicht, wird ein alert angezeigt
     * @param context Context
     * @return boolean
     */
    public boolean isOnlineAndShowAlert(Context context) {
        boolean isOnline = isOnline(context);
        if (!isOnline) {
            showSettingsAlert(context);
        }
        return isOnline;

    }

    /**
     * prüft ob das Gerät eine Internetverbindung hat
     * @param context Context
     * @return boolean
     */
    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (!(netInfo != null && netInfo.isConnectedOrConnecting())) {
            return false;
        }
        return true;
    }

    /**
     * Zeigt einen Alert an, dass keine Internetverbindung vorhanden ist
     * @param context Context
     */
    private void showSettingsAlert(final Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(context.getResources().getString(R.string.apiInternetConnection));
        alertDialog.setMessage(context.getResources().getString(R.string.apiAlertNoInternet));
        alertDialog.setPositiveButton(context.getResources().getString(R.string.title_activity_settings),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        context.startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton(context.getResources().getString(R.string.statisticAlertCancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

}
