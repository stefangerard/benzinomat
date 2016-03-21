package com.stefangerard.benzinomat.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.stefangerard.benzinomat.R;
import com.stefangerard.benzinomat.objects.FuelStop;
import com.stefangerard.benzinomat.utils.PersistantData;
import com.stefangerard.benzinomat.utils.Utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class FuelStopActivity extends Activity {

    public static final String fuelPriceKey = "fuelPrice";

    private String valueFromDetailsActivity;
    private List<FuelStop> fuelStops;
    private Utils utils = new Utils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_stop);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Bundle extras = getIntent().getExtras();
        valueFromDetailsActivity = null;
        if (extras != null) {
            valueFromDetailsActivity = extras.getString(fuelPriceKey);
        }
        fuelStops = utils.getFuelStops(this);
        if (fuelStops == null) {
            fuelStops = new ArrayList<FuelStop>();
        }
        initActionBar();
        initAddFuelStop();
    }

    /**
     * initialisierung der Custom ActionBar
     */
    private void initActionBar() {
        ImageButton backButton = (ImageButton) findViewById(R.id.fuelStopActionBarBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    /**
     * initialisierung der Funktion einen Tankstop hinzuzufügen
     */
    private void initAddFuelStop() {
        Button addFuelStopButton = (Button) findViewById(R.id.fuelStopAddNewFuelStop);
        final EditText kilometersEditText = (EditText) findViewById(R.id.fuelStopkilometerEditText);
        final EditText priceEditText = (EditText) findViewById(R.id.fuelStopPricePerLiterEditText);
        final EditText quantityEditText = (EditText) findViewById(R.id.fuelStopFueledQuantityEditText);

        //Falls ein Wert zur FuelStopActivity übergeben wurde wird das Preis EditText feld damit gefüllt
        //(nur der Fall wenn von der DetailsActivity die FuelStopActivity gestartet wird)
        if (valueFromDetailsActivity != null) {
            valueFromDetailsActivity.replace(".", ",");
            priceEditText.setText(valueFromDetailsActivity);
        }

        addFuelStopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String kilometersStr = kilometersEditText.getText().toString();
                String priceStr = priceEditText.getText().toString();
                String quantityStr = quantityEditText.getText().toString();

                //überprüfung ob alle TextFelder ausgefüllt worden sind
                if (!kilometersStr.isEmpty() && !priceStr.isEmpty() && !quantityStr.isEmpty()) {
                    double kilometers = 0.0;
                    double price = 0.0;
                    double quantity = 0.0;
                    try {
                        kilometers = Double.valueOf(kilometersStr);
                    } catch (Exception e) {
                        utils.showAlert(FuelStopActivity.this, getResources().getString(R.string.error),
                                getResources().getString(R.string.fuelStopAlertKilometer));
                        return;
                    }

                    try {
                        price = Double.valueOf(priceStr.replace(",", "."));
                    } catch (Exception e) {
                        utils.showAlert(FuelStopActivity.this, getResources().getString(R.string.error),
                                getResources().getString(R.string.fuelStopAlertPrice));
                        return;
                    }

                    try {
                        quantity = Double.valueOf(quantityStr.replace(",", "."));
                    } catch (Exception e) {
                        utils.showAlert(FuelStopActivity.this, getResources().getString(R.string.error),
                                getResources().getString(R.string.fuelStopAlertQuantity));
                        return;
                    }

                    String date = new SimpleDateFormat("dd.MM.yy").format(new Date());
                    //überprüfung, ob der Kilometerstand höher ist als der zuvor eingetragene
                    if (fuelStops.size() > 0) {
                        if (fuelStops.get(fuelStops.size() - 1).getTotalKilometers() >= kilometers) {
                            utils.showAlert(FuelStopActivity.this, getResources().getString(R.string.error),
                                    getResources().getString(R.string.fuelStopAlertLessKilometer));
                            return;
                        }

                        double lastTotalKilometers = fuelStops.get(fuelStops.size() - 1).getTotalKilometers();
                        double distance = kilometers - lastTotalKilometers;
                        double consumption = quantity * 100 / distance;
                        consumption = utils.round(consumption, 1);
                        addFuelStop(date, kilometers, price, consumption, quantity);
                    } else {
                        addFuelStop(date, kilometers, price, 0, quantity);
                    }
                    kilometersEditText.setText("");
                    priceEditText.setText("");
                    quantityEditText.setText("");
                    goBack();
                } else {
                    utils.showAlert(FuelStopActivity.this, getResources().getString(R.string.error),
                            getResources().getString(R.string.fuelStopAlertFillAllFields));
                }
            }
        });
    }

    /**
     * hinzugügen eines Tankstops
     * 
     * @param date String
     * @param totalKilometers double
     * @param price double
     * @param consumtion double
     * @param quantity double
     */
    private void addFuelStop(String date, double totalKilometers, double price, double consumtion, double quantity) {
        FuelStop fuelStop = new FuelStop(date, totalKilometers, price, consumtion, quantity);
        fuelStops.add(fuelStop);
        saveFuelStops();
    }

    /**
     * Alle TankStops aus der FuelStop ArrayListe werden als JSON Objekt abgespeichert
     */
    private void saveFuelStops() {
        String fuelStopsStr = "{\"fuelStops\":[";
        int count = 0;
        for (FuelStop fuelStop : fuelStops) {
            fuelStopsStr += fuelStop.toString();
            if (count != fuelStops.size() - 1) {
                fuelStopsStr += ",";
            }
            count++;
        }
        fuelStopsStr += "]}";
        PersistantData.INSTANCE.saveData(PersistantData.INSTANCE.fuelStopsKey, fuelStopsStr, this);
    }

    /**
     * Activity wird beendet
     */
    private void goBack() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Intent returnIntent = new Intent();
        returnIntent.putExtra("success", true);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
