package com.stefangerard.benzinomat.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Klasse für das Verwalten der gespeicherten Einstellungen
 * 
 * @author Stefan Gerard
 *
 */
public class PersistantData {

    public static final PersistantData INSTANCE = new PersistantData();

    private SharedPreferences sharedPref;
    private static final String sharedDataFileName = "com.stefangerard.benzinomat";

    //Keys
    public final String searchRadiusKey = "searchRadius";
    public final String fuelTypeKey = "fuelType";
    public final String resultCountKey = "resultCount";
    public final String latitudeKey = "latitude";
    public final String longitudeKey = "longitude";
    public final String firstLaunchKey = "firstLaunch";
    public final String sortKey = "sort";
    public final String fuelStopsKey = "fuelStops";
    public final String searchTypeKey = "searchType";
    public final String searchCurrentLocation = "currentLocation";
    public final String searchOtherLocation = "otherLocation";
    public final String searchOtherLocationName = "otherLocationName";

    public PersistantData() {

    }

    /**
     * speichert die Einstellungen
     * @param key String
     * @param value String
     * @param activity Activity
     */
    public void saveData(String key, String value, Activity activity) {
        sharedPref = activity.getSharedPreferences(sharedDataFileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * holt die Daten
     * @param key String
     * @param defValue String (Default Wert, falls in den SharedPreferences das keyValue Paar noch nicht vorhanden ist
     * @param activity Activity
     * @return String
     */
    public String getData(String key, String defValue, Activity activity) {
        sharedPref = activity.getSharedPreferences(sharedDataFileName, Context.MODE_PRIVATE);
        return sharedPref.getString(key, defValue);
    }

    /**
     * holt die Daten (Diesmal ohne den default value. Hier wird ein leerer String zurückgegeben wenn das KeyValue Paar noch nciht vorhanden ist
     * @param key String
     * @param activity Activity
     * @return String
     */
    public String getData(String key, Activity activity) {
        return getData(key, "", activity);
    }
}
