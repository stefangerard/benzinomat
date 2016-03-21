package com.stefangerard.benzinomat;

import java.util.List;

import com.stefangerard.benzinomat.objects.FuelStation;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Custom Adapter von der ListView in der MainActivity
 * 
 * @author Stefan Gerard
 *
 */
public class CustomAdapter extends ArrayAdapter<FuelStation> {

    private int selectedPosition;
    private Context context;

    public CustomAdapter(Context context, List<FuelStation> fuelStations) {
        super(context, R.layout.custom_row, fuelStations);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.custom_row, parent, false);

        FuelStation fuelStation = getItem(position);

        TextView nameLabel = (TextView) customView.findViewById(R.id.customRowGasStationLabel);
        TextView priceLabel = (TextView) customView.findViewById(R.id.customRowPriceLabel);
        TextView streetLabel = (TextView) customView.findViewById(R.id.customRowStreetLabel);
        TextView cityLabel = (TextView) customView.findViewById(R.id.customRowCityLabel);
        TextView distanceLabel = (TextView) customView.findViewById(R.id.customRowDistanceLabel);

        nameLabel.setText(fuelStation.getBrand());
        String price = String.valueOf(fuelStation.getPrice());
        String lastDigit = price.substring(price.length() - 1, price.length());
        price = price.substring(0, price.length() - 1);
        price = price.replace(".", ",") + "<sup><small>" + lastDigit + "</small></sup>€";
        priceLabel.setText(Html.fromHtml(price));
        streetLabel.setText(fuelStation.getStreet() + " " + fuelStation.getHouseNumber());
        cityLabel.setText(String.valueOf(fuelStation.getPostCode()) + " " + fuelStation.getPlace());
        distanceLabel.setText(String.valueOf(fuelStation.getDistance()).replace(".", ",") + " km");

        if (position == selectedPosition) {
            customView.setBackgroundColor(ContextCompat.getColor(context, R.color.brightblue));
        }

        return customView;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

}
