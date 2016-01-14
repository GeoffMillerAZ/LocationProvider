/**************************************************
 * Program: Location Provider part 2
 * Authors: Geoff Miller, Chad Brucato, Noah Thompson
 * 
 * Notes:
 * This application updates locations and allows
 * the user to save those locations to a list.
 **************************************************/

package edu.niu.cs.cbrucato.locationprovider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private Location currLoc;
    private Button btnSave;
    private ArrayList<SavedGPS> savedList;
    private ListView listview;
    private String newLabel;

    /**************************************************
     * onPause
     * 
     * Notes:
     * This saves data when the application is paused
     **************************************************/
    @Override
    protected void onPause() {
        super.onPause();
        String dir;
        dir = Environment.getExternalStorageDirectory().getPath();
        File file = new File(dir + "/savedList.txt");

        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        boolean keep = true;

        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new IOException("Unable to create file");
                }
            }
            // else { //prompt user to confirm overwrite }

            fos = new FileOutputStream(file);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(savedList);
        } catch (Exception e) {
            keep = false;
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG)
                    .show();
            e.printStackTrace();
        } finally {
            try {
                if (oos != null)
                    oos.close();
                if (fos != null)
                    fos.close();
                if (keep == false)
                    file.delete();
            } catch (Exception e) { /* do nothing */
            }
        }
    };

    /**************************************************
     * onResume
     * 
     * Notes:
     * This loads data when the application is resumed
     **************************************************/
    @Override
    protected void onResume() {
        super.onResume();
        String dir;
        dir = Environment.getExternalStorageDirectory().getPath();
        File myFile = new File(dir + "/savedList.txt");

        FileInputStream fos = null;
        ObjectInputStream oos = null;
        boolean keep = true;

        try {
            fos = new FileInputStream(myFile);
            oos = new ObjectInputStream(fos);
            this.savedList = (ArrayList<SavedGPS>) oos.readObject();
            final MyAdapter adapter = new MyAdapter(this, savedList);
            listview.setAdapter(adapter);
        } catch (Exception e) {
            keep = false;
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG)
                    .show();
        } finally {
            try {
                if (oos != null)
                    oos.close();
                if (fos != null)
                    fos.close();
                if (keep == false)
                    myFile.delete();
            } catch (Exception e) { /* do nothing */
            }
        }
    };

    /**************************************************
     * onCreate
     * 
     * Notes:
     * This overrides the onCreate method to run when
     * the application is created.
     **************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // instantiate the list of saved data
        savedList = new ArrayList<SavedGPS>();

        LocationManager lm; // declare location variable
        String svcName = Context.LOCATION_SERVICE; // using location service
        lm = (LocationManager) getSystemService(svcName);
        Criteria cr = new Criteria();
        cr.setAccuracy(Criteria.ACCURACY_COARSE); // set type of
                                                  // accuracy(highest)
        cr.setPowerRequirement(Criteria.POWER_LOW); // requirements and switches
        cr.setAltitudeRequired(false);
        cr.setBearingRequired(false);
        cr.setSpeedRequired(false);
        cr.setCostAllowed(true);
        String provider = lm.getBestProvider(cr, true); // declare provider

        Location l = lm.getLastKnownLocation(provider);
        updateWithNewLocation(l); // what to update

        int t = 100; // milliseconds
        int distance = 1; // meters

        lm.requestLocationUpdates(provider, t, distance, locationListener); // requests
                                                                            // update
                                                                            // with
                                                                            // declared
                                                                            // variables

        btnSave = (Button) findViewById(R.id.btnSave);
        /**************************************************
         * Event Handler - btnSave onClick
         * 
         * Notes:
         * This prompts the user with a dialog to enter
         * in a description. If the user clicks OK then
         * the location is saved with the label entered.
         * If the user clicks cancel then nothing occurs.
         **************************************************/
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newLabel = "";
                AlertDialog.Builder builder = new AlertDialog.Builder(v
                        .getContext());
                builder.setTitle("Location Description");
                final EditText input = new EditText(v.getContext());
                input.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
                builder.setView(input);
                builder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                newLabel = input.getText().toString();
                                savedList.add(new SavedGPS(newLabel, currLoc.getLatitude(), currLoc.getLongitude()));
                                listview.invalidateViews();
                            }
                        });
                builder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                dialog.cancel();
                            }
                        });

                builder.show();
            }
        });

        listview = (ListView) findViewById(R.id.listView);

        final MyAdapter adapter = new MyAdapter(this, savedList);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                    int position, long id) {
            }
        });
    }

    /**************************************************
     * MyAdapter
     * 
     * Notes:
     * This is the adapter that will be used to update
     * the views on the listview.
     **************************************************/
    public class MyAdapter extends BaseAdapter {
        private final Context context;
        private final ArrayList<SavedGPS> values;

        public MyAdapter(Context inContext, ArrayList<SavedGPS> inValues) {
            // super();
            this.context = inContext;
            this.values = inValues;
        }

        /**************************************************
         * getView
         * 
         * Notes:
         * This processes the creation of the views.
         **************************************************/
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            double lat = values.get(position).getLat();
            double lng = values.get(position).getLon();
            String latLongString = lat + ", " + lng;

            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.list_item, parent, false);
            TextView txtvLabel = (TextView) rowView
                    .findViewById(R.id.secondLine);
            TextView txtvCoords = (TextView) rowView
                    .findViewById(R.id.firstLine);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
            txtvLabel.setText(latLongString);
            txtvCoords.setText(values.get(position).label);

            return rowView;
        }

        /**************************************************
         * getCount
         * 
         * Notes:
         * determins the views count
         **************************************************/
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return values.size();
        }

        /**************************************************
         * getItem
         * 
         * Notes:
         * gets an item from the views
         **************************************************/
        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return values.get(position);
        }

        /**************************************************
         * getItemId
         * 
         * Notes:
         * gets the item id of a view
         **************************************************/
        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }
    }

    /**************************************************
     * Location Listener
     * 
     * Notes:
     * Listens to see if the location has changed and
     * if it has it calls onLocationChanged to update.
     **************************************************/
    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location); // calls for update
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    /**************************************************
     * updateWithNewLocation
     * 
     * Notes:
     * Take a Location parameter that is the current
     * location. It then updates the GUI to show the
     * new location.
     **************************************************/
    private void updateWithNewLocation(Location l) {
        currLoc = l;
        TextView tv; // define textview
        tv = (TextView) findViewById(R.id.myLocationOut); // where to put
                                                          // myLocationOut
        String latLongString = "No location found"; // what to say of no
                                                    // locaiton
        if (l != null) // if there is location...
        {
            double lat = l.getLatitude();
            double lng = l.getLongitude();
            latLongString = "Lat: " + lat + "\nLong: " + lng; // get location
                                                              // and display
                                                              // with
        }
        tv.setText(latLongString); // send to GUI
    }

}
