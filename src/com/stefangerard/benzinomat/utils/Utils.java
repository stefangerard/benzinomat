package com.stefangerard.benzinomat.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.stefangerard.benzinomat.objects.FuelStop;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 * Klasse für Helper-Funktionen
 * 
 * @author Stefan Gerard
 *
 */
public class Utils {

    public final String streetMapKey = "street";
    public final String houseNumberMapKey = "houseNumber";

    public Utils() {

    }

    /**
     * Aufbereitung der JSON-Daten 'brand'
     * @param brand String
     * @param name String
     * @return String
     */
    public String prepareBrands(String brand, String name) {
        if (brand.isEmpty() || brand.equals(" ") || brand.equals("null") || brand == null) {
            brand = name;
        }
        return brand;
    }

    /**
     * Aufbereitung der JSON-Daten 'Street und Housenumber'
     * @param street String
     * @param houseNumber String
     * @return Map<String, String> - (streetMapKey, houseNumberMapKey)
     */
    public Map<String, String> prepareStreetAndHouseNumber(String street, String houseNumber) {
        String tmpHouseNumber = "";
        boolean numInStreet = false;
        String[] streetArray = street.split(" ");
        for (int i = 0; i < streetArray.length; i++) {
            streetArray[i] = Character.toUpperCase(streetArray[i].charAt(0))
                    + streetArray[i].substring(1).toLowerCase();
            if (streetArray[i].matches("^\\d+[a-zA-Z]*$")) {
                tmpHouseNumber = streetArray[i];
                numInStreet = true;
            } else {
                if (numInStreet) {
                    tmpHouseNumber = tmpHouseNumber + streetArray[i];
                } else {
                    if (i == 0 && i != streetArray.length - 1) {
                        street = streetArray[i];
                    } else {
                        street += " " + streetArray[i];
                    }
                }
            }
        }

        if (houseNumber.isEmpty() || houseNumber.equals(" ") || houseNumber == null || houseNumber.equals("null")) {
            houseNumber = tmpHouseNumber;
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put(streetMapKey, street);
        map.put(houseNumberMapKey, houseNumber);

        return map;
    }

    /**
     * Aufberietung der JSON-Daten 'place'
     * @param place String
     * @return String
     */
    public String preparePlace(String place) {
        return Character.toUpperCase(place.charAt(0)) + place.substring(1).toLowerCase();
    }

    /**
     * Aufbereitung der JSON-Daten 'OpeningTimes' - (DetailsActivity)
     * @param openingTimes String
     * @return List<Map<String, String>>
     */
    public List<Map<String, String>> prepareOpeningTimes(String openingTimes) {
        List<Map<String, String>> maps = new ArrayList<Map<String, String>>();

        try {
            JSONArray jsonArray = new JSONArray(openingTimes);
            for (int i = 0; i < jsonArray.length(); i++) {
                Map<String, String> map = new HashMap<String, String>();
                String start = jsonArray.getJSONObject(i).getString("start");
                if (start.matches("\\d{2}:\\d{2}:\\d{2}$")) {
                    start = start.substring(0, start.length() - 3);
                }
                String end = jsonArray.getJSONObject(i).getString("end");
                if (end.matches("\\d{2}:\\d{2}:\\d{2}$")) {
                    end = end.substring(0, end.length() - 3);
                }
                String text = jsonArray.getJSONObject(i).getString("text");
                map.put("start", start);
                map.put("end", end);
                map.put("text", text);
                maps.add(map);
            }
        } catch (JSONException e) {
            Log.e("error", "JSON OpeningHours");
        }
        return maps;
    }

    /**
     * gibt die bildschirmbreite in pixel zurück
     * @param context Context
     * @return int
     */
    public int getScreenWidth(Context context) {
        Map<String, Integer> map = getScreenResolution(context);
        return map.get("width");
    }

    /**
     * gibt die bildschirmhöhe in pixel zurück
     * @param context Context
     * @return int
     */
    public int getScreenHeight(Context context) {
        Map<String, Integer> map = getScreenResolution(context);
        return map.get("height");
    }

    /**
     * ermittelt die Bildschirmauflösung des Gerätes
     * @param context Context
     * @return Map<String, Integer>
     */
    private Map<String, Integer> getScreenResolution(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("width", width);
        map.put("height", height);

        return map;
    }

    //    public ShapeDrawable drawCircle(Context context, int width, int height, int color) {
    //
    //        //////Drawing oval & Circle programmatically /////////////
    //
    //        ShapeDrawable oval = new ShapeDrawable(new OvalShape());
    //        oval.setIntrinsicHeight(height);
    //        oval.setIntrinsicWidth(width);
    //        oval.getPaint().setColor(color);
    //        return oval;
    //    }

    /**
     * Rundet auf die Gewünschte Nachkommastelle
     * 
     * @param value double
     * @param places int (Anzahl der Nachkommastellen)
     * @return double
     */
    public double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Erstellt ein Alert mit einem 'OK'-button welcher den Alert wieder beendet
     * 
     * @param context Context
     * @param title String
     * @param message String
     */
    public void showAlert(final Context context, String title, String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        alertDialog.setTitle(title);

        alertDialog.setMessage(message);

        alertDialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    /**
     * liest die FuelStops aus den SharedPreferences aus und wandelt sie in eine Liste um 
     * 
     * @param activity Activity
     * @return List<FuelStop>
     */
    public List<FuelStop> getFuelStops(Activity activity) {
        List<FuelStop> fuelStops = new ArrayList<FuelStop>();
        String fuelStopsString = PersistantData.INSTANCE.getData(PersistantData.INSTANCE.fuelStopsKey, "", activity);
        if (!fuelStopsString.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(fuelStopsString);
                JSONArray fuelStopsArray = jsonObject.getJSONArray("fuelStops");

                for (int i = 0; i < fuelStopsArray.length(); i++) {
                    String date = fuelStopsArray.getJSONObject(i).getString("date");
                    String totalKilometers = fuelStopsArray.getJSONObject(i).getString("totalKilometers");
                    String price = fuelStopsArray.getJSONObject(i).getString("price");
                    String consumption = fuelStopsArray.getJSONObject(i).getString("consumption");
                    String quantity = fuelStopsArray.getJSONObject(i).getString("quantity");

                    FuelStop fuelStop = new FuelStop(date, Double.valueOf(totalKilometers), Double.valueOf(price),
                            Double.valueOf(consumption), Double.valueOf(quantity));
                    fuelStops.add(fuelStop);
                }
                return fuelStops;
            } catch (Exception e) {
                Log.e("error", "failed to load Fuel Stops");
            }
        }
        return null;
    }
}
