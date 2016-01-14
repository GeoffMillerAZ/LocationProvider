/**************************************************
* Program: Location Provider part 2
* Authors: Geoff Miller, Chad Brucato, Noah Thompson
* 
* Notes:
* This application updates locations and allows
* the user to save those locations to a list.
**************************************************/

package edu.niu.cs.cbrucato.locationprovider;

import java.io.Serializable;

public class SavedGPS implements Serializable{
    double lat, lon;
    /**
     * @return the lat
     */
    public double getLat() {
        return lat;
    }

    /**
     * @param lat the lat to set
     */
    public void setLat(double lat) {
        this.lat = lat;
    }

    /**
     * @return the lon
     */
    public double getLon() {
        return lon;
    }

    /**
     * @param lon the lon to set
     */
    public void setLon(double lon) {
        this.lon = lon;
    }

    String label;
    
    public SavedGPS(String inLabel, double inLat, double inLon){
        lat = inLat;
        lon = inLon;
        label = inLabel;
    }
}
