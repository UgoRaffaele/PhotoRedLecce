package com.ugopiemontese.photoredlecce.utils;

public class PhotoRed {
			 
	private int id;
    private String address;
    private Double lat, lng; 
    
    public PhotoRed() {}
 
    public PhotoRed(String address, Double lat, Double lng) {
    	
        super();
        
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        
    }
    
    public int getId() {
    	return this.id;
    }
    
    public String getAddress() {
    	return this.address;
    }
    
    public Double getLat() {
    	return this.lat;
    }
    
    public Double getLng() {
    	return this.lng;
    }
	
    public void setId(int id) {
    	this.id = id;
    }
    
    public void setAddress(String address) {
    	this.address = address;
    }
    
    public void setLat(Double lat) {
    	this.lat = lat;
    }
    
    public void setLng(Double lng) {
    	this.lng = lng;
    }
    
}