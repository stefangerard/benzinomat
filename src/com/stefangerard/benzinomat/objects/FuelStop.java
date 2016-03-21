package com.stefangerard.benzinomat.objects;

public class FuelStop {

    private String date;
    private double totalKilometers;
    private double price;
    private double consumption;
    private double quantity;

    public FuelStop(String date, double totalKilometers, double price, double consumption, double quantity) {
        this.date = date;
        this.totalKilometers = totalKilometers;
        this.price = price;
        this.consumption = consumption;
        this.quantity = quantity;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;

    }

    public double getTotalKilometers() {
        return totalKilometers;
    }

    public void setTotalKilometers(double totalKilometers) {
        this.totalKilometers = totalKilometers;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getConsumption() {
        return consumption;
    }

    public void setConsumption(double consumption) {
        this.consumption = consumption;
    }

    @Override
    public String toString() {
        return "{\"date\":\"" + date + "\",\"totalKilometers\":\"" + totalKilometers + "\",\"price\":\"" + price
                + "\",\"consumption\":\"" + consumption + "\",\"quantity\":\"" + quantity + "\"}";
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

}
