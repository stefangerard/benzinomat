package com.stefangerard.benzinomat.objects;

import java.util.Map;

import com.stefangerard.benzinomat.utils.Utils;

public class FuelStationDetail {

    private String id;
    private String name;
    private String brand;
    private String street;
    private String houseNumber;
    private int postCode;
    private String place;
    private String overrides;
    private boolean isOpen;
    private double e5;
    private double e10;
    private double diesel;
    private double latitude;
    private double longitude;
    private String state;
    private String openingTimes;

    public FuelStationDetail() {

    }

    public FuelStationDetail(String id, String name, String brand, String street, String houseNumber, int postCode,
            String place, String overrides, boolean isOpen, double e5, double e10, double diesel, double latitude,
            double longitude, String state, String openingTimes) {
        super();
        Utils utils = new Utils();
        this.id = id;
        this.name = name;
        this.brand = utils.prepareBrands(brand, name);
        Map<String, String> map = utils.prepareStreetAndHouseNumber(street, houseNumber);
        this.street = map.get(utils.streetMapKey);
        this.houseNumber = map.get(utils.houseNumberMapKey);
        this.postCode = postCode;
        this.place = utils.preparePlace(place);
        this.overrides = overrides;
        this.isOpen = isOpen;
        this.e5 = e5;
        this.e10 = e10;
        this.diesel = diesel;
        this.latitude = latitude;
        this.longitude = longitude;
        this.state = state;
        this.openingTimes = openingTimes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public int getPostCode() {
        return postCode;
    }

    public void setPostCode(int postCode) {
        this.postCode = postCode;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getOverrides() {
        return overrides;
    }

    public void setOverrides(String overrides) {
        this.overrides = overrides;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public double getE5() {
        return e5;
    }

    public void setE5(double e5) {
        this.e5 = e5;
    }

    public double getE10() {
        return e10;
    }

    public void setE10(double e10) {
        this.e10 = e10;
    }

    public double getDiesel() {
        return diesel;
    }

    public void setDiesel(double diesel) {
        this.diesel = diesel;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getOpeningTimes() {
        return openingTimes;
    }

    public void setOpeningTimes(String openingTimes) {
        this.openingTimes = openingTimes;
    }

    @Override
    public String toString() {
        return "FuelStationDetail [id=" + id + ", name=" + name + ", brand=" + brand + ", street=" + street
                + ", houseNumber=" + houseNumber + ", postCode=" + postCode + ", place=" + place + ", overrides="
                + overrides + ", isOpen=" + isOpen + ", e5=" + e5 + ", e10=" + e10 + ", diesel=" + diesel
                + ", latitude=" + latitude + ", longitude=" + longitude + ", state=" + state + ", openingTimes="
                + openingTimes + "]";
    }

}
