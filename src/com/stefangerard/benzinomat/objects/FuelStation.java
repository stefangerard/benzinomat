package com.stefangerard.benzinomat.objects;

import java.util.Map;

import com.stefangerard.benzinomat.utils.Utils;

public class FuelStation {

    private String name;
    private double latitude;
    private double longitude;
    private String brand;
    private double distance;
    private double price;
    private String id;
    private String street;
    private String houseNumber;
    private int postCode;
    private String place;
    private boolean isOpen;

    public FuelStation(String name, double latitude, double longitude, String brand, double distance, double price,
            String id, String street, String houseNumber, int postCode, String place, boolean isOpen) {
        super();
        Utils utils = new Utils();
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.brand = utils.prepareBrands(brand, name);
        this.distance = distance;
        this.price = price;
        this.id = id;

        Map<String, String> map = utils.prepareStreetAndHouseNumber(street, houseNumber);
        this.street = map.get(utils.streetMapKey);
        this.houseNumber = map.get(utils.houseNumberMapKey);
        this.postCode = postCode;
        this.place = utils.preparePlace(place);
        this.isOpen = isOpen;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

}
