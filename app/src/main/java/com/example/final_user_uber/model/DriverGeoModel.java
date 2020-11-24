package com.example.final_user_uber.model;

import com.firebase.geofire.GeoLocation;

/**
 * Created by QuocKM on 22,November,2020
 * EbizWorld company,
 * HCMCity, VietNam.
 */
public class DriverGeoModel {
    private String key;
    private GeoLocation geoLocation;
    private DriverInfoModel driverInfoModel;

    public DriverGeoModel() {
    }

    public DriverGeoModel(String key, GeoLocation geoLocation) {
        this.key = key;
        this.geoLocation = geoLocation;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public DriverInfoModel getDriverInfoModel() {
        return driverInfoModel;
    }

    public void setDriverInfoModel(DriverInfoModel driverInfoModel) {
        this.driverInfoModel = driverInfoModel;
    }
}
