package com.example.ortprimeraapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private LocationManager locationManager;
    private static final int MY_PERMISSIONS_REQUEST_ACCESSLOCATION = 999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.textView);
        textView.setText("Iniciando app....");
    }

    @Override
    protected void onStart() {
        super.onStart();

        preguntarporPermisos();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Siempre que la app deja de estar activa debo avisarle al GPS que deje de escuchar
        /*
         * El gps consume mucha bateria, prueben comentar la linea de abajo y veran que el simbolo
         * de gps queda encendido, esto sucedera hasta que la app sea descartada por android luego de un
         * periodo largo de inactividad
         * */
        locationManager.removeUpdates(this);
    }

    private void preguntarporPermisos() {
        /* solo necesitamos aprobacion manual
        /* cuando el sdk del celular es a partir de Android 6.0 (nivel de API 23)
        /* en este caso pasa directo si el SDK es antiguo
        */
        TextView textView = findViewById(R.id.textView);

        final String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if (ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, permissions[1]) != PackageManager.PERMISSION_GRANTED
        ) {

            //el permiso no fue dado
            // necesita una explicacion del porque usara ese permiso?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
            ) {
                new AlertDialog.Builder(this)
                        .setTitle("Localizacion")
                        .setMessage("Necesitamos su permiso para obtener las coordenadas gps o por red celular")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this, permissions, MY_PERMISSIONS_REQUEST_ACCESSLOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                //no necesita explicacion, se piden los permisos directamente
                ActivityCompat.requestPermissions(MainActivity.this, permissions, MY_PERMISSIONS_REQUEST_ACCESSLOCATION);
            }
        } else {
            // Los permisos fueron aceptados
            iniciarGeolocalizacion();
        }

    }

    private void iniciarGeolocalizacion() {
        TextView textView = findViewById(R.id.textView);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        final boolean networkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!gpsEnabled) {
            Toast.makeText(MainActivity.this, "El gps no esta activo", Toast.LENGTH_LONG).show();
        }
        if (!networkProvider) {
            Toast.makeText(MainActivity.this, "El posicionamiento por red no esta activo", Toast.LENGTH_LONG).show();
        }
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);

        Location location = null;
        try {
            textView.setText("Iniciando localizacion");
            location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                escribirCalleyNumero(location);
            }
            locationManager.requestLocationUpdates(provider, 500, 1, this);
        } catch (SecurityException e) {
            textView.setText("Error en Seguridad:" + e.getMessage());
        } catch (Exception ex) {
            textView.setText("Error grave:" + ex.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        TextView textView = findViewById(R.id.textView);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESSLOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    iniciarGeolocalizacion();
                } else {
                    textView.setText("Permiso de localizacion denagado, no se veran las coordenadas");
                }
                return;
            }
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        escribirCalleyNumero(location);
    }

    private void escribirCalleyNumero(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        TextView textView = findViewById(R.id.textView);
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder();
                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("");
                }
                textView.setText(returnedAddress.getThoroughfare() + " " + returnedAddress.getFeatureName()+"\n(" + location.getLatitude() + "," + location.getLongitude() + ")");

            } else {
                textView.setText("no se pudo obtener la calle y nro de las coordenadas : (" + location.getLatitude() + "," + location.getLongitude() + ")");
            }
        } catch (IOException e) {
            textView.setText("ocurrio un error al obtener la calle y nro: " + e.getMessage());
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        TextView textView = findViewById(R.id.textView);
        textView.setText("Status Changed:" + s);
    }

    @Override
    public void onProviderEnabled(String s) {
        TextView textView = findViewById(R.id.textView);
        textView.setText("Proveedor Enabled:" + s);
    }

    @Override
    public void onProviderDisabled(String s) {
        TextView textView = findViewById(R.id.textView);
        textView.setText("Proveedor Disabled:" + s);
    }
}
