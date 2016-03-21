package com.stefangerard.benzinomat.activities;

import java.util.List;
import java.util.Map;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.stefangerard.benzinomat.Api;
import com.stefangerard.benzinomat.R;
import com.stefangerard.benzinomat.objects.FuelStationDetail;
import com.stefangerard.benzinomat.utils.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class DetailsActivity extends Activity implements OnMapReadyCallback {

    private TableLayout openingHoursTableLayout;
    private FuelStationDetail fuelStationDetail;
    private RelativeLayout relativeLayout;
    private ScrollView scrollView;
    private RelativeLayout loadingRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        initActionBar();

        relativeLayout = (RelativeLayout) findViewById(R.id.detailsRelativeLayoutUnderlayer);
        scrollView = (ScrollView) findViewById(R.id.detailsScrollView);
        loadingRelativeLayout = (RelativeLayout) findViewById(R.id.detailsRelativeLayoutUnderlayerLoading);
        relativeLayout.setVisibility(View.INVISIBLE);
        scrollView.setVisibility(View.INVISIBLE);

        //ReloadButton, falls beim Aufruf der DetailsActivity keine Internetverbindung mehr vorhanden ist
        loadingRelativeLayout.setVisibility(View.INVISIBLE);
        Button reloadButton = (Button) findViewById(R.id.reloadButton);
        reloadButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                load();

            }
        });
        load();

    }

    /**
     * initialisierung der ActionBar
     */
    private void initActionBar() {
        ImageButton backButton = (ImageButton) findViewById(R.id.detailsActionBarBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });
    }

    /**
     * initialisierung der GoogleMap
     */
    private void initMap() {
        Utils utils = new Utils();
        LinearLayout mapContainer = (LinearLayout) findViewById(R.id.detailsMapcontainer);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (utils.getScreenHeight(this) - 70) / 3);
        mapContainer.setLayoutParams(params);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.detailsMap);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        double latitude = fuelStationDetail.getLatitude();
        double longitude = fuelStationDetail.getLongitude();
        LatLng fuelStation = new LatLng(latitude, longitude);
        String title = fuelStationDetail.getBrand();
        String snippet = fuelStationDetail.getStreet() + " " + fuelStationDetail.getHouseNumber() + ", "
                + fuelStationDetail.getPostCode() + " " + fuelStationDetail.getPlace();
        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(fuelStation, 15));

        //Marker hinzufügen
        MarkerOptions markerOptions = new MarkerOptions().title(title).snippet(snippet).position(fuelStation);
        final Marker marker = map.addMarker(markerOptions);

        map.setOnMapClickListener(new OnMapClickListener() {

            @Override
            public void onMapClick(LatLng arg0) {
                marker.hideInfoWindow();
            }
        });

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                marker.hideInfoWindow();
            }
        });
    }

    /**
     * ein Asynchroner Task wird gestartet in dem eine JSON-Anfrage stattfindet
     */
    private void load() {
        if (Api.INSTANCE.isOnlineAndShowAlert(this)) {
            relativeLayout.setVisibility(View.INVISIBLE);
            Bundle extras = getIntent().getExtras();
            String id;
            if (extras == null) {
                id = null;
            } else {
                id = extras.getString("id");
                loadingRelativeLayout.setVisibility(View.VISIBLE);
                new JsonAsyncTask().execute(id);
            }
        } else {
            noInternetConnectionVisible(true);
        }
    }

    /**
     * die View wird aktualisiert
     */
    private void updateView() {
        Utils utils = new Utils();
        fuelStationDetail = Api.INSTANCE.getFuelStationDetail();

        //Alle TextViews etc werden mit den entsprechnenden Daten befüllt

        TextView fuelstationNameTextView = (TextView) findViewById(R.id.detailsFuelStationName);
        fuelstationNameTextView.setText(fuelStationDetail.getBrand());

        TextView isOpenTextView = (TextView) findViewById(R.id.detailsIsOpen);

        if (fuelStationDetail.isOpen()) {
            isOpenTextView.setText(getResources().getString(R.string.open));
            isOpenTextView.setTextColor(ContextCompat.getColor(this, R.color.green));
        } else {
            isOpenTextView.setText(getResources().getString(R.string.closed));
            isOpenTextView.setTextColor(ContextCompat.getColor(this, R.color.red));
        }

        TextView streetHousenumberTextView = (TextView) findViewById(R.id.detailsStreetHousenumber);
        streetHousenumberTextView.setText(fuelStationDetail.getStreet() + " " + fuelStationDetail.getHouseNumber());

        TextView zipCityTextView = (TextView) findViewById(R.id.detailsZipCity);
        zipCityTextView.setText(fuelStationDetail.getPostCode() + " " + fuelStationDetail.getPlace());

        //Die mitgelieferten Distanz von der MainActivity
        Bundle extras = getIntent().getExtras();
        String distance = null;
        if (extras != null) {
            try {
                double dist = extras.getDouble("distance");
                distance = String.valueOf(dist);
            } catch (Exception e) {

            }
        }
        if (distance == null) {
            distance = "0,0";
        }
        distance = distance.replace(".", ",");
        TextView distanceTextView = (TextView) findViewById(R.id.detailsDistance);
        distanceTextView.setText(distance + " km");

        Button startDirectionButton = (Button) findViewById(R.id.detailsButtonStartDirection);
        startDirectionButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Route wird gestartet
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr=" + fuelStationDetail.getLatitude() + ","
                                + fuelStationDetail.getLongitude()));
                startActivity(intent);
            }
        });

        Button addFuelStop = (Button) findViewById(R.id.detailsButtonAddFuelStop);
        addFuelStop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showFuelAlert();
            }
        });

        List<Map<String, String>> openingHours = utils.prepareOpeningTimes(fuelStationDetail.getOpeningTimes());

        openingHoursTableLayout = (TableLayout) findViewById(R.id.detailsOpeningTableLayout);
        clearTableLayout(openingHoursTableLayout);
        for (int i = 0; i < openingHours.size(); i++) {
            createTableRow(openingHoursTableLayout, openingHours.get(i));

            if (openingHours.size() > 1 && openingHours.size() - 1 != i) {
                addTableRowDivider(openingHoursTableLayout);
            }
        }

        TextView dieselPrice = (TextView) findViewById(R.id.detailsPriceDieselValue);
        TextView e10Price = (TextView) findViewById(R.id.detailsPriceE10Value);
        TextView e5Price = (TextView) findViewById(R.id.detailsPriceE5Value);

        dieselPrice.setText(Html.fromHtml(getPriceLastDigitSup(fuelStationDetail.getDiesel())));
        e10Price.setText(Html.fromHtml(getPriceLastDigitSup(fuelStationDetail.getE10())));
        e5Price.setText(Html.fromHtml(getPriceLastDigitSup(fuelStationDetail.getE5())));

        noInternetConnectionVisible(false);
        initMap();
    }

    /**
     * letzte Ziffer wird hochgestellt
     * @param value double
     * @return String in html-format
     */
    private String getPriceLastDigitSup(double value) {
        String price = String.valueOf(value);
        String lastDigit = price.substring(price.length() - 1, price.length());
        price = price.substring(0, price.length() - 1);
        price = price.replace(".", ",") + "<sup><small>" + lastDigit + "</small></sup>€";
        return price;
    }

    //eine neue TableRow wird erstellt
    private void createTableRow(TableLayout tableLayout, Map<String, String> openingHours) {
        TableRow tableRow = new TableRow(this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        tableRow.setLayoutParams(new TableRow.LayoutParams(params));

        tableRow.addView(createTextView(openingHours.get("text"), true));
        tableRow.addView(createTextView((openingHours.get("start") + " " + getResources().getString(R.string.clock)
                + " - " + openingHours.get("end") + " " + getResources().getString(R.string.clock)), false));

        tableLayout.addView(tableRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
    }

    /**
     * ein TableRow Divider wird erstellt und dem TableLayout hinzugefügt
     * @param tableLayout TableLayout
     */
    private void addTableRowDivider(TableLayout tableLayout) {
        TableRow tableRow = new TableRow(this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 100);

        tableRow.setLayoutParams(new TableRow.LayoutParams(params));

        LinearLayout LinearLayout = new LinearLayout(this);
        LinearLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.brightblue));

        TableRow.LayoutParams LLParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1);
        LLParams.weight = 1;
        LLParams.bottomMargin = 5;
        LinearLayout.setLayoutParams(LLParams);
        tableRow.addView(LinearLayout);

        tableLayout.addView(tableRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));

    }

    /**
     * eine TextView wird erstellt.
     * @param label String
     * @param left boolean - gravity start oder end
     * @return TextView
     */
    private TextView createTextView(String label, boolean left) {
        TextView textView = new TextView(this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        if (left) {
            params.gravity = Gravity.START;
        } else {
            params.gravity = Gravity.END;
        }
        params.weight = 1;
        textView.setLayoutParams(params);
        textView.setText(label);
        return textView;
    }

    /**
     * TableLayout wird geleert
     * @param tableLayout TableLayout
     */
    private void clearTableLayout(TableLayout tableLayout) {
        for (int i = tableLayout.getChildCount() - 1; i > 0; i--) {
            tableLayout.removeViewAt(i);
        }
    }

    /**
     * Zeigt Alert an, welche Spritsorte getankt worden ist
     * mit den Buttons wird jeweils die FuelStopActivity gestartet und der entsprechende Preis übergeben
     */
    private void showFuelAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getResources().getString(R.string.fuelType));
        alertDialog.setMessage(getResources().getString(R.string.fuelTypeUsed));

        alertDialog.setPositiveButton(getResources().getString(R.string.diesel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (fuelStationDetail != null) {
                    Intent myIntent = new Intent(DetailsActivity.this, FuelStopActivity.class);
                    myIntent.putExtra(FuelStopActivity.fuelPriceKey,
                            String.valueOf(fuelStationDetail.getDiesel()).replace(".", ","));
                    startActivityForResult(myIntent, 1);
                } else {
                    dialog.cancel();
                }
            }
        });

        alertDialog.setNegativeButton(getResources().getString(R.string.e10), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (fuelStationDetail != null) {
                    Intent myIntent = new Intent(DetailsActivity.this, FuelStopActivity.class);
                    myIntent.putExtra(FuelStopActivity.fuelPriceKey, String.valueOf(fuelStationDetail.getE10()));
                    startActivityForResult(myIntent, 1);
                } else {
                    dialog.cancel();
                }
            }
        });

        alertDialog.setNeutralButton(getResources().getString(R.string.e5), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (fuelStationDetail != null) {
                    Intent myIntent = new Intent(DetailsActivity.this, FuelStopActivity.class);
                    myIntent.putExtra(FuelStopActivity.fuelPriceKey, String.valueOf(fuelStationDetail.getE5()));
                    startActivityForResult(myIntent, 1);
                } else {
                    dialog.cancel();
                }

            }
        });

        alertDialog.show();
    }

    /**
     * Zeigt an ob eine Internetverbindung vorhanden ist
     * @param value boolean
     */
    private void noInternetConnectionVisible(boolean value) {
        loadingRelativeLayout.setVisibility(View.INVISIBLE);
        if (value) {
            scrollView.setVisibility(View.INVISIBLE);
            relativeLayout.setVisibility(View.VISIBLE);
        } else {
            scrollView.setVisibility(View.VISIBLE);
            relativeLayout.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Klasse in der ein Asynchroner Task gestertet wird in dem eine JSON Anfrage statt findet
     * @author Stefan Gerard
     *
     */
    public class JsonAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                //JSON Anfrage
                return Api.INSTANCE.getJSONObject(params[0]);
            } catch (Exception e) {
                noInternetConnectionVisible(true);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (Api.INSTANCE.success.equals(result)) {
                updateView();
            } else {
                noInternetConnectionVisible(true);
            }
        }
    }
}
