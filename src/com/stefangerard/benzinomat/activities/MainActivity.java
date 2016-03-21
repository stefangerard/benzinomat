package com.stefangerard.benzinomat.activities;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.stefangerard.benzinomat.Api;
import com.stefangerard.benzinomat.CustomAdapter;
import com.stefangerard.benzinomat.GPSTracker;
import com.stefangerard.benzinomat.R;
import com.stefangerard.benzinomat.enums.UnderLayer;
import com.stefangerard.benzinomat.objects.FuelStation;
import com.stefangerard.benzinomat.utils.PersistantData;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements OnMapReadyCallback {

    private LocationManager locationManager;
    private boolean firstLaunch;
    private GoogleMap mMap;
    private boolean isChecked = false;
    private GPSTracker gpsTracker;
    private List<FuelStation> fuelStations = new ArrayList<FuelStation>();
    private CustomAdapter listAdapter;
    private ListView listViewMain = null;
    private boolean mapIsReady = false;
    private boolean mapAlreadyUpdated = false;
    private boolean listViewIsReady = false;
    private List<Marker> markers = new ArrayList<Marker>();
    private boolean mapIsShown = false;
    private SwipeRefreshLayout swipeContainer;
    private Context context;
    private RelativeLayout loadingRelativeLayout;
    private Button mapButton;
    private Button prizeSortButton;
    private Button distanceSortButton;
    private boolean actionBarOptionsMenuIsShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        Api.INSTANCE.setActivityTmp(this);
        initActivity();
    }

    /*
     * Da die ActionBar nicht die Android ActionBar ist, sondern eine eigene Implementiert wurde, wird dispatchTouchEvent benötigt um
     * das OptionsMenu auszublenden, sobald der User irgendwo anders auf den Screen Klickt.
     * 
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        LinearLayout actionBarOptionsBox = (LinearLayout) findViewById(R.id.mainActionBarOptionsBox);
        ImageButton actionBarOptionsButton = (ImageButton) findViewById(R.id.mainActionBarOptionsButton);
        Rect viewRect = new Rect();
        Rect viewRectButton = new Rect();
        actionBarOptionsBox.getGlobalVisibleRect(viewRect);
        actionBarOptionsButton.getGlobalVisibleRect(viewRectButton);
        if (!viewRect.contains((int) ev.getRawX(), (int) ev.getRawY())
                && !viewRectButton.contains((int) ev.getRawX(), (int) ev.getRawY())) {
            showActionBarOptionsMenu(false);
        }
        return super.dispatchTouchEvent(ev);
    }

    private void initActivity() {
        setContentView(R.layout.activity_main);

        //Der SwipeContainer ist die PullToRefresh Funktionalität 
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                loadData();
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);

        //MapButton ist der ToggleButton unten rechts in der Activity
        //bei klick wird die GoogleMap ein oder ausgeblendet
        mapButton = (Button) findViewById(R.id.mainMapButton);
        mapButton.setVisibility(View.INVISIBLE);
        mapButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Zuerst wird gebrüft ob eine Internetverbindung besteht
                if (Api.INSTANCE.isOnline(MainActivity.this)) {
                    //falls die Karte eingeblendet ist, wird sie ausgeblendet, ansonsten eingeblendet
                    if (mapIsShown) {
                        showMap(false);
                    } else {
                        showMap(true);
                    }
                }
            }
        });

        //PrizeSortButton und distanceSortButton sind die Buttons in der ActionBar
        prizeSortButton = (Button) findViewById(R.id.mainPriceButton);
        distanceSortButton = (Button) findViewById(R.id.mainDistanceButton);
        setSortButtonColor();

        prizeSortButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Zuerst wird geschaut ob die Sortiereinsetlleung zur Zeit 'Distance' ist, wenn ja wird die Sortiereinstellung auf 
                //'Price' geändert und gespeichert
                String sort = PersistantData.INSTANCE.getData(PersistantData.INSTANCE.sortKey, "dist",
                        MainActivity.this);
                if (sort.equals("dist")) {
                    PersistantData.INSTANCE.saveData(PersistantData.INSTANCE.sortKey, "price", MainActivity.this);
                    loadData();
                    setSortButtonColor();
                }
            }
        });

        distanceSortButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Zuerst wird geschaut ob die Sortiereinsetlleung zur Zeit 'Price' ist, wenn ja wird die Sortiereinstellung auf 
                //'Distance' geändert und gespeichert
                String sort = PersistantData.INSTANCE.getData(PersistantData.INSTANCE.sortKey, "dist",
                        MainActivity.this);
                if (sort.equals("price")) {
                    PersistantData.INSTANCE.saveData(PersistantData.INSTANCE.sortKey, "dist", MainActivity.this);
                    loadData();
                    setSortButtonColor();
                }
            }
        });

        showActionBarOptionsMenu(false);

        //actionBarOptionsButton ist der Button rechts in der ActionBar mit den 3 Punkten) Dieser Blendet das OptionsMenu ein oder aus
        ImageButton actionBarOptionsButton = (ImageButton) findViewById(R.id.mainActionBarOptionsButton);
        actionBarOptionsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (actionBarOptionsMenuIsShown) {
                    showActionBarOptionsMenu(false);
                } else {
                    showActionBarOptionsMenu(true);
                }

            }
        });

        //actionBarSettingsButton ist der Button 'Einstellungen' in dem OptionsMenü, wenn dieser geklickt wird
        //wird die SettingsActivity gestartet
        Button actionBarSettingsButton = (Button) findViewById(R.id.mainActionBarSettingsButton);
        actionBarSettingsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                redirectToSettingsActivity();
                showActionBarOptionsMenu(false);

            }
        });

        //actionBarStatisticsButton ist der Button 'Statistik' in dem OptionsMenü, wenn dieser geklickt wird
        //wird die StatisticActivity gestartet
        Button actionBarStatisticsButton = (Button) findViewById(R.id.mainActionBarStatistcsButton);
        actionBarStatisticsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                redirectToStatisticActivity();
                showActionBarOptionsMenu(false);
            }
        });

        Button actionBarContactButton = (Button) findViewById(R.id.mainActionBarContactButton);
        actionBarContactButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                redirectToContactActivity();
                showActionBarOptionsMenu(false);

            }
        });

        initListView();
        gpsTracker = new GPSTracker(MainActivity.this);
        loadData();
        initMap();
    }

    /**
     * Die Karte wird ein oder ausgeblendet
     * 
     * @param show - ein boolean object ob die Karte ein oder ausgeblendet werden soll
     */
    private void showMap(boolean show) {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.graph_container);
        LinearLayout.LayoutParams params = (LayoutParams) linearLayout.getLayoutParams();

        if (show) {
            params.weight = 1;
            mapIsShown = true;
        } else {
            params.weight = 0;
            mapIsShown = false;
        }
        linearLayout.setLayoutParams(params);
        linearLayout.requestLayout();
    }

    /**
     * Das ActionBar OptionsMenu wird ein oder ausgeblendet
     * 
     * @param value - ein boolean object ob das Menü ein oder ausgeblendet werden soll
     */
    private void showActionBarOptionsMenu(boolean value) {
        LinearLayout actionBarOptionsBox = (LinearLayout) findViewById(R.id.mainActionBarOptionsBox);
        if (value) {
            actionBarOptionsBox.setVisibility(View.VISIBLE);
            actionBarOptionsMenuIsShown = true;
        } else {
            actionBarOptionsBox.setVisibility(View.INVISIBLE);
            actionBarOptionsMenuIsShown = false;
        }
    }

    /**
     * Schaut welche Sortiereinstellung gesetzt ist und ändert die Farben der Button so ab,
     * dass entweder Der Distanz Button oder der Preis Button aktiv ist 
     */
    private void setSortButtonColor() {
        String sort = PersistantData.INSTANCE.getData(PersistantData.INSTANCE.sortKey, "dist", this);
        LinearLayout distanceLinearLayout = (LinearLayout) findViewById(R.id.mainDistanceButtonActionBar);
        if (sort.equals("dist")) {
            distanceSortButton.setTextColor(Color.WHITE);
            distanceLinearLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.brightblue));
            prizeSortButton.setTextColor(ContextCompat.getColor(this, R.color.brightblue));
            prizeSortButton.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
        } else if (sort.equals("price")) {
            distanceSortButton.setTextColor(ContextCompat.getColor(this, R.color.brightblue));
            distanceLinearLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
            prizeSortButton.setTextColor(Color.WHITE);
            prizeSortButton.setBackgroundColor(ContextCompat.getColor(this, R.color.brightblue));
        }
    }

    /**
     * loadData versucht eine JSON-Anfrage zu machen und die Daten anzuzeigen
     */
    private void loadData() {
        //Zuerst wird geschut ob eine Internetverbindung vorhanden ist
        if (Api.INSTANCE.isOnlineAndShowAlert(this)) {

            //Es wird geschaut welche Suchmethode eingestellt ist 
            if (searchCurrentLocation()) {
                //Es wird geschaut ob der aktuelle Standort ermittelt werden kann
                if (!gpsTracker.canGetLocation()) {
                    //Liste wird geleert und ein alert wird angezeigt 
                    showUnderlayerText(UnderLayer.NO_CONNECTION, false);
                    swipeContainer.setRefreshing(false);
                    fuelStations.clear();
                    listAdapter.notifyDataSetChanged();
                    gpsTracker.showSettingsAlert();
                    return;
                } else {
                    //breiten und längengrad werden ermittelt und gespeichert
                    double latitude = gpsTracker.getLatitude();
                    double longitude = gpsTracker.getLongitude();
                    saveLocation(latitude, longitude);
                }
            }
            //Im falle der allerersten Benutzung der App wird der Benutzer zuerst zu SettingsActivity geleitet 
            if (!isFirstLaunch()) {
                //ein Asynchroner Task wird gestartet in dem eine JSON-Anfrage stattfindet
                swipeContainer.setRefreshing(false);
                showUnderlayerText(UnderLayer.LOADING, false);
                new JsonAsyncTask().execute();
            } else {
                PersistantData.INSTANCE.saveData(PersistantData.INSTANCE.firstLaunchKey, "false", this);
                redirectToSettingsActivity();
            }
        } else {
            //Liste wird geleert
            showUnderlayerText(UnderLayer.NO_CONNECTION, false);
            swipeContainer.setRefreshing(false);
            fuelStations.clear();
            listAdapter.notifyDataSetChanged();
        }
    }

    /**
     * @return boolean - ob die App schon einmal gestartet worden ist
     */
    private boolean isFirstLaunch() {
        return (Boolean.valueOf(PersistantData.INSTANCE.getData(PersistantData.INSTANCE.firstLaunchKey, "true", this)));
    }

    /**
     * 
     * @return boolean - true wenn die Suchmethode 'aktueller Standpunkt' eingestellt ist, false - wenn nach PLZ/ort gesucht wird
     */
    private boolean searchCurrentLocation() {
        PersistantData data = PersistantData.INSTANCE;
        String location = data.getData(data.searchTypeKey, data.searchCurrentLocation, this);
        if (data.getData(data.searchTypeKey, data.searchCurrentLocation, this).equals(data.searchCurrentLocation)) {
            return true;
        }
        return false;
    }

    /**
     * blendet Text unter der ListView aus
     */
    private void hideUnderlayerText() {
        showUnderlayerText(null, true);
    }

    /**
     * Blendet einen Text, bzw Loading-ProgressBar unter der ListView ein
     * @param underLayer - Auswahl welche option angezeigt werden soll (NO_CONNECTION, LOADING, NO_FUELSTATIONS_FOUND
     * @param showMapButton - boolean ob die GoogleMap eingeblendet werden soll oder nicht
     */
    private void showUnderlayerText(UnderLayer underLayer, boolean showMapButton) {
        RelativeLayout underlayerRelativeLayout = (RelativeLayout) findViewById(R.id.mainRelativeLayoutUnderlayer);
        RelativeLayout loadingLayout = (RelativeLayout) findViewById(R.id.mainLoadingPanel);
        TextView underlayerTextView = (TextView) findViewById(R.id.mainUnderlayerTextView);
        loadingLayout.setVisibility(View.INVISIBLE);
        if (underLayer != null) {
            underlayerRelativeLayout.setVisibility(View.VISIBLE);
            switch (underLayer) {
            case NO_CONNECTION:
                underlayerTextView.setText(getResources().getString(R.string.mainUnderlayerNoConnection));
                showMap(false);
                break;
            case LOADING:
                underlayerRelativeLayout.setVisibility(View.INVISIBLE);
                loadingLayout.setVisibility(View.VISIBLE);
                break;
            case NO_FUELSTATIONS_FOUND:
                underlayerTextView.setText(getResources().getString(R.string.mainUnderlayerNotFound));
                showMap(false);
                break;
            default:
                break;
            }
        } else {
            underlayerRelativeLayout.setVisibility(View.INVISIBLE);
        }
        if (showMapButton) {
            mapButton.setVisibility(View.VISIBLE);
        } else {
            mapButton.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Speichert den Längen und breitengrad ab
     * @param latitude double
     * @param longitude double
     */
    private void saveLocation(double latitude, double longitude) {
        PersistantData data = PersistantData.INSTANCE;
        if (!data.getData(data.latitudeKey, MainActivity.this).equals(String.valueOf(latitude))) {
            data.saveData(data.latitudeKey, String.valueOf(latitude), MainActivity.this);
        }
        if (!data.getData(data.longitudeKey, MainActivity.this).equals(String.valueOf(longitude))) {
            data.saveData(data.longitudeKey, String.valueOf(longitude), MainActivity.this);
        }
    }

    /**
     * initialisierung der ListView
     */
    private void initListView() {
        fuelStations = Api.INSTANCE.getFuelStations();
        listAdapter = new CustomAdapter(this, fuelStations);
        listViewMain = (ListView) findViewById(R.id.listViewMain);
        listViewMain.setAdapter(listAdapter);

        listViewMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FuelStation fuelStation = (FuelStation) listViewMain.getItemAtPosition(position);
                //für den fall dass vorher eine ander position ausgewählt wird (nur relevant wenn die Karte eingeblendet ist)
                if (listAdapter.getSelectedPosition() != position) {
                    zoomToMarker(fuelStation);
                    listAdapter.setSelectedPosition(position);
                    listAdapter.notifyDataSetChanged();
                } else if (mapIsShown) {
                    goToDetailsView(fuelStation.getId(), fuelStation.getDistance());
                }
                if (!mapIsShown) {
                    goToDetailsView(fuelStation.getId(), fuelStation.getDistance());
                }
            }
        });
        listAdapter.setSelectedPosition(-1);

        listViewMain.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //wird benötigt damit die pullToRefresh funktionalität nur funktioniert wenn die list view komlett nach oben gesrollt wurde
                int topRowVerticalPosition = (listViewMain == null || listViewMain.getChildCount() == 0) ? 0
                        : listViewMain.getChildAt(0).getTop();
                swipeContainer.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });
    }

    /**
     * aktualisierung der ListView
     */
    private void updateListView() {
        fuelStations = Api.INSTANCE.getFuelStations();
        listAdapter.notifyDataSetChanged();
        listViewIsReady = true;
        listAdapter.setSelectedPosition(-1);
        listViewMain.smoothScrollToPositionFromTop(0, 0);
        updateMap();
    }

    /**
     * starten der SettingsActivity 
     */
    private void redirectToSettingsActivity() {
        Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
        myIntent.putExtra("key", "test"); //Optional parameters
        startActivityForResult(myIntent, 1);
    }

    /**
     * starten der StatistikActivity 
     */
    private void redirectToStatisticActivity() {
        Intent myIntent = new Intent(MainActivity.this, StatisticActivity.class);
        myIntent.putExtra("key", "test"); //Optional parameters
        startActivityForResult(myIntent, 1);
    }

    /**
     * starten der ContactActivity 
     */
    private void redirectToContactActivity() {
        Intent myIntent = new Intent(MainActivity.this, ContactActivity.class);
        myIntent.putExtra("key", "test"); //Optional parameters
        startActivityForResult(myIntent, 1);
    }

    /**
     * starten der DetailsActivity 
     */
    private void goToDetailsView(String id, double distance) {
        Intent myIntent = new Intent(MainActivity.this, DetailsActivity.class);
        myIntent.putExtra("id", id);
        myIntent.putExtra("distance", distance);
        startActivityForResult(myIntent, 1);
    }

    /*
     * wird aufgerufen, wenn der Backbutton der SettingsActivity geklickt wird
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String stredittext = data.getStringExtra("edittextvalue");
                loadData();
            }
        }
    }

    //MAP

    /**
     * initialisierung der Karte
     */
    private void initMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(new OnMapClickListener() {

            @Override
            public void onMapClick(LatLng arg0) {
                //liste wird deselektiert
                listAdapter.setSelectedPosition(-1);
                listAdapter.notifyDataSetChanged();
            }
        });
        mapIsReady = true;
        updateMap();
    }

    /**
     * aktualisierung der Karte
     */
    private void updateMap() {
        if (mapIsReady && listViewIsReady) {
            mMap.clear();
            mapAlreadyUpdated = true;
            markers.clear();
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            //Die karte zeigt die aktuelle Position (ansonsten würde beim ersten anzeigen der Karte die Komplette erde gezeigt)
            double latitude = Double
                    .valueOf(PersistantData.INSTANCE.getData(PersistantData.INSTANCE.latitudeKey, this));
            double longitude = Double
                    .valueOf(PersistantData.INSTANCE.getData(PersistantData.INSTANCE.longitudeKey, this));
            LatLng latLng = new LatLng(latitude, longitude);
            mMap.setMyLocationEnabled(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

            //Marker werden zur Karte hinzugefügt
            for (FuelStation fuelStation : fuelStations) {
                Marker marker = addMarkerToMap(fuelStation.getLatitude(), fuelStation.getLongitude(),
                        fuelStation.getBrand(), String.valueOf(fuelStation.getPrice()).replace(".", ",") + "€");
                markers.add(marker);
            }

            for (Marker marker : markers) {
                builder.include(marker.getPosition());
            }

            if (!markers.isEmpty()) {
                final LatLngBounds bounds = builder.build();
                //map zommet um alle marker anzuzeigen
                mMap.setOnMapLoadedCallback(new OnMapLoadedCallback() {

                    @Override
                    public void onMapLoaded() {
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 50);
                        mMap.animateCamera(cameraUpdate);
                    }
                });
            }

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                @Override
                public boolean onMarkerClick(Marker marker) {
                    //marher infoWindow wird angezeigt und der entsprchnde Listeneintrag wird makiert und die liste scrollt zu diesem Eintrag
                    marker.showInfoWindow();
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                    for (int i = 0; i < fuelStations.size(); i++) {
                        FuelStation fuelStation = fuelStations.get(i);
                        LatLng latLngFuelStation = new LatLng(fuelStation.getLatitude(), fuelStation.getLongitude());
                        if (latLngFuelStation.equals(marker.getPosition())) {
                            listAdapter.setSelectedPosition(i);
                            listAdapter.notifyDataSetChanged();
                            listViewMain.smoothScrollToPosition(i);
                        }
                    }
                    return true;
                }
            });

            //bei klick auf das InfoWindow wird die DetailsActivity gestartet
            mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

                @Override
                public void onInfoWindowClick(Marker marker) {
                    for (FuelStation fuelStation : fuelStations) {
                        LatLng latLng = new LatLng(fuelStation.getLatitude(), fuelStation.getLongitude());
                        if (marker.getPosition().equals(latLng)) {
                            goToDetailsView(fuelStation.getId(), fuelStation.getDistance());
                        }
                    }
                }
            });
        }

    }

    /**
     * Marker zur Karte hinzufügen
     * 
     * @param latitude double
     * @param longitude double
     * @param title String
     * @param snippet String
     * @return Marker object
     */
    private Marker addMarkerToMap(double latitude, double longitude, String title, String snippet) {
        return mMap
                .addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(title).snippet(snippet));
    }

    /**
     * zoomt auf die ausgewählte Tankstelle
     * @param fuelStation FuelStation
     */
    private void zoomToMarker(FuelStation fuelStation) {
        double lat = fuelStation.getLatitude();
        double lng = fuelStation.getLongitude();
        LatLng latLng = new LatLng(lat, lng);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
        showMarkerInfo(fuelStation);
    }

    /**
     * blendet das markerInfoWindow ein
     * @param fuelStation FuelStation
     */
    private void showMarkerInfo(FuelStation fuelStation) {
        double lat = fuelStation.getLatitude();
        double lng = fuelStation.getLongitude();
        LatLng latLng = new LatLng(lat, lng);
        for (Marker marker : markers) {
            LatLng latLngMarker = marker.getPosition();
            if (latLngMarker.equals(latLng)) {
                marker.showInfoWindow();
                return;
            }
        }
    }

    /**
     * Klasse in dem ein Asynchroner Task gestartet wird in dem eine JSON-Anfrage gestellt wird
     * @author Stefan Gerard
     *
     */
    public class JsonAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                swipeContainer.post(new Runnable() {

                    @Override
                    public void run() {
                        showUnderlayerText(UnderLayer.LOADING, false);
                        fuelStations.clear();
                        listAdapter.notifyDataSetChanged();
                        mMap.clear();

                    }
                });

                listViewIsReady = false;
                //JSON-Anfrage
                return Api.INSTANCE.getJSONObject();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (Api.INSTANCE.success.equals(result)) {
                swipeContainer.setRefreshing(false);
                updateListView();
                if (fuelStations.isEmpty()) {
                    showUnderlayerText(UnderLayer.NO_FUELSTATIONS_FOUND, false);
                } else {
                    hideUnderlayerText();
                }

            } else {
                swipeContainer.setRefreshing(false);
                if (Api.INSTANCE.isOnlineAndShowAlert(context)) {
                    hideUnderlayerText();
                } else {
                    showUnderlayerText(UnderLayer.NO_CONNECTION, false);
                }
            }
        }
    }
}
