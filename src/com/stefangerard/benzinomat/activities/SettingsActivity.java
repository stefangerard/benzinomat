package com.stefangerard.benzinomat.activities;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.stefangerard.benzinomat.GPSTracker;
import com.stefangerard.benzinomat.R;
import com.stefangerard.benzinomat.utils.PersistantData;
import com.stefangerard.benzinomat.utils.Utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TableRow;

public class SettingsActivity extends Activity {

    private Spinner searchRadiusSpinner;
    private Spinner fuelTypeSpinner;
    private Spinner resultCountSpinner;

    private boolean isChanged = false;
    private PersistantData data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        data = PersistantData.INSTANCE;

        initActionBar();
        initSearchLocation();
        initAllSpinner();
        setSpinnerValues();
        addListenerOnSpinnerItemSelection();

    }

    /**
     * initialisierung der custom ActionBar
     */
    private void initActionBar() {
        ImageButton backButton = (ImageButton) findViewById(R.id.settingsActionBarBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });

    }

    /**
     * initialisierung aller DropDown Menüs (Spinner)
     */
    private void initAllSpinner() {
        searchRadiusSpinner = (Spinner) findViewById(R.id.spinnerSearchRadius);
        fuelTypeSpinner = (Spinner) findViewById(R.id.spinnerFuelType);
        resultCountSpinner = (Spinner) findViewById(R.id.spinnerResultCount);
    }

    /**
     * initialisierung der Suchmethode
     */
    private void initSearchLocation() {
        //Google Places Api fragment
        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager()
                .findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setText(data.getData(data.searchOtherLocationName, "", this));

        //Google Places Api fragment wird ein oder ausgeblendet, je nachdem welche Suchmethode eingestellt ist
        if (data.getData(data.searchTypeKey, data.searchCurrentLocation, this).equals(data.searchCurrentLocation)) {
            hideOtherLocationSearch(true);
        } else {
            if (data.getData(data.searchOtherLocationName, "", this).isEmpty()) {
                data.saveData(data.searchTypeKey, data.searchCurrentLocation, this);
                hideOtherLocationSearch(true);
            } else {
                hideOtherLocationSearch(false);
            }
        }

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.settingsLocationRadioGroup);
        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (checkedId == R.id.settingsRadioButtonSearchCurrentLoc) {
                    hideOtherLocationSearch(true);
                    GPSTracker gpsTracker = new GPSTracker(SettingsActivity.this);
                    if (gpsTracker.canGetLocation()) {
                        data.saveData(data.searchOtherLocationName, "", SettingsActivity.this);
                        autocompleteFragment.setText("");
                        data.saveData(data.searchTypeKey, data.searchCurrentLocation, SettingsActivity.this);
                    } else {
                        gpsTracker.showSettingsAlert();
                        hideOtherLocationSearch(false);
                    }

                } else if (checkedId == R.id.settingsRadioButtonSearchOtherLoc) {
                    hideOtherLocationSearch(false);
                }
            }
        });

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {

            @Override
            public void onPlaceSelected(Place place) {
                //Nach erfolgreicher Suche über die Google Places API werden Breiten- und Längengrad, Name des Ortes gespeichert
                //ebenfalls wird die suchmethode geändert
                data.saveData(data.latitudeKey, String.valueOf(place.getLatLng().latitude), SettingsActivity.this);
                data.saveData(data.longitudeKey, String.valueOf(place.getLatLng().longitude), SettingsActivity.this);
                data.saveData(data.searchOtherLocationName, String.valueOf(place.getName()), SettingsActivity.this);
                data.saveData(data.searchTypeKey, data.searchOtherLocation, SettingsActivity.this);
                autocompleteFragment.setText(place.getAddress());
            }

            @Override
            public void onError(Status status) {

            }

        });
    }

    /**
     * Wenn searchCurrentLocation true ist wird die Zeile mit dem Google Places API fragment ausgeblendet. Ansonsten wird sie eingeblendet
     * @param searchCurrentLocation boolean
     */
    private void hideOtherLocationSearch(boolean searchCurrentLocation) {
        RadioButton currentSearchRadioButton = (RadioButton) findViewById(R.id.settingsRadioButtonSearchCurrentLoc);
        RadioButton otherSearchRadioButton = (RadioButton) findViewById(R.id.settingsRadioButtonSearchOtherLoc);
        TableRow searchFragmentTableRow = (TableRow) findViewById(R.id.settingsTableRowSearchFragment);
        if (searchCurrentLocation) {
            searchFragmentTableRow.setVisibility(View.GONE);
            currentSearchRadioButton.setChecked(true);
        } else {
            searchFragmentTableRow.setVisibility(View.VISIBLE);
            otherSearchRadioButton.setChecked(true);
        }
    }

    /**
     * zu jedem spinner wird ein Listener hinzugefügt
     */
    public void addListenerOnSpinnerItemSelection() {
        searchRadiusSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String itemStr = parent.getItemAtPosition(position).toString();
                String tmp = itemStr.substring(0, itemStr.indexOf(" "));
                if (!data.getData(data.searchRadiusKey, SettingsActivity.this).equals(tmp)) {
                    data.saveData(data.searchRadiusKey, tmp, SettingsActivity.this);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        fuelTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String itemStr = parent.getItemAtPosition(position).toString();
                if (!data.getData(data.fuelTypeKey, SettingsActivity.this).equals(itemStr)) {
                    data.saveData(data.fuelTypeKey, itemStr, SettingsActivity.this);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        resultCountSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String itemStr = parent.getItemAtPosition(position).toString();
                if (itemStr.equals(getResources().getString(R.string.settingsAllResults))) {
                    if (!data.getData(data.resultCountKey, SettingsActivity.this).equals("-1")) {
                        data.saveData(data.resultCountKey, "-1", SettingsActivity.this);
                    }
                } else {
                    itemStr = itemStr.substring(itemStr.indexOf(" ") + 1);
                    itemStr = itemStr.substring(0, itemStr.indexOf(" "));
                    if (!data.getData(data.resultCountKey, SettingsActivity.this).equals(itemStr)) {
                        data.saveData(data.resultCountKey, itemStr, SettingsActivity.this);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * die spinner Values werden gesetzt
     */
    private void setSpinnerValues() {

        //searchRadius
        String radiusTmp = data.getData(data.searchRadiusKey, SettingsActivity.this);
        if (radiusTmp.isEmpty()) {
            radiusTmp = "5";
            data.saveData(data.searchRadiusKey, radiusTmp, SettingsActivity.this);
        }
        searchRadiusSpinner
                .setSelection(((ArrayAdapter) searchRadiusSpinner.getAdapter()).getPosition(radiusTmp + " km"));

        //fuel type
        String type = data.getData(data.fuelTypeKey, SettingsActivity.this);
        if (type.isEmpty()) {
            type = "Super E5";
            data.saveData(data.fuelTypeKey, type, SettingsActivity.this);
        }
        fuelTypeSpinner.setSelection(((ArrayAdapter) fuelTypeSpinner.getAdapter()).getPosition(type));

        //result count
        String count = data.getData(data.resultCountKey, SettingsActivity.this);
        if (count.isEmpty()) {
            count = "25";
            data.saveData(data.resultCountKey, count, SettingsActivity.this);
        }
        if (count.equals("-1")) {
            count = getResources().getString(R.string.settingsAllResults);
        } else {
            count = getResources().getString(R.string.settingsMax) + " " + count + " "
                    + getResources().getString(R.string.settingsResults);
        }
        resultCountSpinner.setSelection(((ArrayAdapter) resultCountSpinner.getAdapter()).getPosition(count));

    }

    @Override
    public void onBackPressed() {
        //Falls der Benutzer zurück gehen will und PLZ/Ort ausgewählt hat, jedoch keinen Ort eingetragen hat
        //wird der Vorgang abgebrochen und ein Alert angezeigt
        RadioButton radioButtonPlzPlace = (RadioButton) findViewById(R.id.settingsRadioButtonSearchOtherLoc);
        if (radioButtonPlzPlace.isChecked() && data.getData(data.searchOtherLocationName, "", this).isEmpty()) {
            Utils utils = new Utils();
            utils.showAlert(this, getResources().getString(R.string.settingsSearch),
                    getResources().getString(R.string.settingsAlert));
            data.saveData(data.searchTypeKey, data.searchCurrentLocation, this);
        } else {
            Intent intent = new Intent();
            intent.putExtra("edittextvalue", "value_here");
            setResult(RESULT_OK, intent);
            finish();
        }
    }

}
