package com.stefangerard.benzinomat.activities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.stefangerard.benzinomat.R;
import com.stefangerard.benzinomat.objects.FuelStop;
import com.stefangerard.benzinomat.utils.PersistantData;
import com.stefangerard.benzinomat.utils.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class StatisticActivity extends Activity {

    private Utils utils = new Utils();
    private TableLayout tableLayout;
    private Button clearStatisticButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);
        clearStatisticButton = (Button) findViewById(R.id.statisticClearButton);
        tableLayout = (TableLayout) findViewById(R.id.statisticTableLayout);
        createHeaderTableRow(tableLayout);
        getAvgConsumption();
        getDataForCostLabels();
        updateTableLayout();

        //backButton in der ActionBar
        ImageButton backButton = (ImageButton) findViewById(R.id.statisticsActionBarBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Button addFuelStopButton = (Button) findViewById(R.id.statisticAddNewFuelStop);
        addFuelStopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(StatisticActivity.this, FuelStopActivity.class);
                myIntent.putExtra("key", "test");
                startActivityForResult(myIntent, 1);
            }
        });

        clearStatisticButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showClearAlert();
            }
        });

    }

    /*
     * wird aufgerufen wenn ein Tankstop in der FuelStopActivity hinzugefügt wurde
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                boolean success = data.getBooleanExtra("success", false);
                if (success) {
                    updateTableLayout();
                    getAvgConsumption();
                    getDataForCostLabels();
                }
            }
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /**
     * Der Durchnittsverbrauch wird berechnet und in der TextView angezeigt
     */
    private void getAvgConsumption() {
        TextView avgConsumptionTextView = (TextView) findViewById(R.id.statisticAvgConsumptionValue);
        avgConsumptionTextView.setText(getResources().getString(R.string.statisticTooLessData));

        List<FuelStop> fuelStops = utils.getFuelStops(this);
        if (fuelStops != null) {
            if (fuelStops.size() > 1) {
                double distance = fuelStops.get(fuelStops.size() - 1).getTotalKilometers()
                        - fuelStops.get(0).getTotalKilometers();
                double totalQuantity = 0;
                for (int i = 1; i < fuelStops.size(); i++) {
                    //Menge x 100 ÷ Distanz
                    totalQuantity += fuelStops.get(i).getQuantity();
                }
                double totalAvgConsumption = totalQuantity * 100 / distance;
                totalAvgConsumption = utils.round(totalAvgConsumption, 1);
                avgConsumptionTextView.setText(totalAvgConsumption + " l/km");
            }
        }
    }

    /**
     * Die Kosten des aktuellen und vorherigen Monats werden berrechnet und in den jeweiligen TextViews angezeigt
     */
    private void getDataForCostLabels() {
        TextView currentMonthCosts = (TextView) findViewById(R.id.statisticCurrentMonthCostsValue);
        TextView lastMonthCosts = (TextView) findViewById(R.id.statisticLastMonthCostsValue);

        double currentMonth = 0.0;

        double lastMonth = 0.0;
        List<FuelStop> fuelStops = utils.getFuelStops(this);
        if (fuelStops != null) {
            for (FuelStop fuelStop : fuelStops) {
                String date = new SimpleDateFormat("dd.MM.yy").format(new Date());
                date = date.substring(3);
                String fuelStopDate = fuelStop.getDate().substring(3);
                if (date.equals(fuelStopDate)) {
                    currentMonth = currentMonth + (fuelStop.getPrice() * fuelStop.getQuantity());
                } else {
                    int month = Integer.valueOf(date.substring(0, 2));
                    int year = Integer.valueOf(date.substring(3));
                    if (month > 1) {
                        month--;
                    } else {
                        month = 12;
                        year--;
                    }
                    if (year == -1) {
                        year = 99;
                    }
                    String monthStr = "";
                    if (month < 10) {
                        monthStr += "0";
                    }
                    monthStr += String.valueOf(month);
                    String yearStr = "";
                    if (year < 10) {
                        yearStr += "0";
                    }
                    yearStr += String.valueOf(year);
                    date = monthStr + "." + yearStr;
                    if (fuelStopDate.equals(date)) {
                        lastMonth = lastMonth + (fuelStop.getPrice() * fuelStop.getQuantity());
                    }
                }
            }
        }
        currentMonthCosts.setText(String.valueOf(utils.round(currentMonth, 2)).replace(".", ",") + " €");
        lastMonthCosts.setText(String.valueOf(utils.round(lastMonth, 2)).replace(".", ",") + " €");
    }

    /**
     * Das TebleLayout wird aktualisiert
     */
    private void updateTableLayout() {
        clearTableLayout();
        List<FuelStop> fuelStops = utils.getFuelStops(this);
        if (fuelStops != null) {
            if (!fuelStops.isEmpty()) {
                for (int i = fuelStops.size() - 1; i >= 0; i--) {
                    createTableRow(tableLayout, fuelStops.get(i));
                    if (fuelStops.size() > 1 && i != 0) {
                        addTableRowDivider(tableLayout);
                    }
                }
                tableLayout.setVisibility(View.VISIBLE);
                clearStatisticButton.setVisibility(View.VISIBLE);
            } else {
                tableLayout.setVisibility(View.GONE);
                clearStatisticButton.setVisibility(View.GONE);
            }
        } else {
            tableLayout.setVisibility(View.GONE);
            clearStatisticButton.setVisibility(View.GONE);
        }
    }

    /**
     * Die oberste Zeile der TableLayout wird erzeugt und hinzugefügt
     * @param tableLayout TableLayout
     */
    private void createHeaderTableRow(TableLayout tableLayout) {
        TableRow tableRow = new TableRow(this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT);
        tableRow.setLayoutParams(new TableRow.LayoutParams(params));

        tableRow.addView(createTextViewHeader(getResources().getString(R.string.statisticDate)));
        tableRow.addView(createTextViewHeader(getResources().getString(R.string.statisticKilometer)));
        tableRow.addView(createTextViewHeader(getResources().getString(R.string.statisticPrice)));
        tableRow.addView(createTextViewHeader(getResources().getString(R.string.statisticFuelQuantity)));
        tableRow.addView(createTextViewHeader(getResources().getString(R.string.statisticConsumption)));

        tableLayout.addView(tableRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
    }

    /**
     * Zeilen 2 - * werden erzeugt und dem TableLayout hinzugefügt 
     * @param tableLayout TableLayout
     * @param fuelStop FuelStop
     */
    private void createTableRow(TableLayout tableLayout, FuelStop fuelStop) {
        TableRow tableRow = new TableRow(this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT);
        tableRow.setLayoutParams(new TableRow.LayoutParams(params));

        int kilometers = (int) fuelStop.getTotalKilometers();
        tableRow.addView(createTextView(fuelStop.getDate()));
        tableRow.addView(createTextView(String.valueOf(kilometers) + " km"));
        tableRow.addView(createTextView(String.valueOf(fuelStop.getPrice()).replace(".", ",") + " €"));
        tableRow.addView(createTextView(String.valueOf(fuelStop.getQuantity()).replace(".", ",") + " l"));
        tableRow.addView(createTextView(String.valueOf(fuelStop.getConsumption()).replace(".", ",") + " l/100km"));

        tableLayout.addView(tableRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
    }

    /**
     * eine Trennlinie wird erzeugt und dem TableLayout hinzugefügt
     * @param tableLayout
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

    private TextView createTextView(String label) {
        return createTextView(label, false);
    }

    private TextView createTextViewHeader(String label) {
        return createTextView(label, true);
    }

    /**
     * eine TextView wird erzeugt
     * @param label String
     * @param header boolean
     * @return TextView
     */
    private TextView createTextView(String label, boolean header) {
        TextView textView = new TextView(this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        params.weight = 1;
        if (header) {
            params.topMargin = 5;
            params.bottomMargin = 10;
            textView.setTextSize(15);
        } else {
            params.topMargin = 5;
            params.bottomMargin = 5;
        }

        textView.setLayoutParams(params);
        textView.setText(label);
        return textView;
    }

    /**
     * Leert das TableLayout
     */
    private void clearTableLayout() {
        for (int i = tableLayout.getChildCount() - 1; i > 0; i--) {
            tableLayout.removeViewAt(i);

        }
    }

    /**
     * löscht die Statistik und aktualisiert alle notwendigen ui elemente
     */
    private void clearStatistic() {
        PersistantData.INSTANCE.saveData(PersistantData.INSTANCE.fuelStopsKey, "", StatisticActivity.this);
        updateTableLayout();
        getAvgConsumption();
        getDataForCostLabels();
    }

    /**
     * Alert wird angezeigt um die löschung der Statistik zu bestätigen
     */
    private void showClearAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getResources().getString(R.string.clearStatistic));
        alertDialog.setMessage(getResources().getString(R.string.statisticAlert));

        alertDialog.setPositiveButton(getResources().getString(R.string.statisticAlertResetting),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearStatistic();
                    }
                });

        alertDialog.setNegativeButton(getResources().getString(R.string.statisticAlertCancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }
}
