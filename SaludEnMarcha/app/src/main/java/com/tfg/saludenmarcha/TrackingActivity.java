package com.tfg.saludenmarcha;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * TrackingActivity es una actividad que permite al usuario realizar el seguimiento de una actividad física.
 * Muestra un mapa con la ruta GPS recorrida, calcula la distancia total y el tiempo transcurrido.
 * Permite iniciar, pausar, reanudar y detener el seguimiento.
 * Los datos de la actividad se almacenan en Firebase Firestore.
 */
public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Variables para el mapa y la localización
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private PolylineOptions polylineOptions;
    private boolean isTracking = false;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Chronometer chronometer;
    private TextView distanceTextView; // TextView para mostrar la distancia
    private float totalDistance = 0; // Distancia total recorrida
    private Location lastLocation = null; // Última ubicación conocida
    private List<LatLng> pathPoints = new ArrayList<>(); // Lista para almacenar los puntos del camino
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    // Variables para los botones
    private Button startButton;
    private Button stopButton;
    private Button pauseButton;
    private Button stopButtonVolver;
    private Button resumeButton;

    private long timeElapsed;

    // Variables para Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CollectionReference pathPointsRef;
    private List<LatLngData> pathPointsData;
    private String raceId;
    private DocumentReference raceRef;
    private String activityType;
    private CarreraData carreraData;
    private List<GeoPoint> geoPoints;

    private static final String TAG = "TrackingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        EdgeToEdge.enable(this); // Ajuste de la interfaz para pantallas con bordes curvos

        // Inicialización de la interfaz de usuario
        initializeUI();

        // Solicitar permisos de ubicación si no se han concedido
        requestLocationPermissions();

        // Inicialización de Firebase y otras configuraciones
        initializeFirebase();

        // Configurar listeners para los botones
        setupListeners();
    }

    /**
     * Inicializa los componentes de la interfaz de usuario.
     */
    private void initializeUI() {
        chronometer = findViewById(R.id.chronometer);
        distanceTextView = findViewById(R.id.distanceTextView);

        pauseButton = findViewById(R.id.pauseButton);
        stopButton = findViewById(R.id.stopButton);
        stopButtonVolver = findViewById(R.id.stopButtonVolver);
        resumeButton = findViewById(R.id.resumeButton);
        startButton = findViewById(R.id.startButton);

        // Configuración inicial de los botones
        pauseButton.setVisibility(View.GONE);
        stopButton.setVisibility(View.GONE);
        stopButtonVolver.setVisibility(View.VISIBLE);
        resumeButton.setVisibility(View.GONE);

        // Ajuste de la vista para adaptarse a los bordes de la pantalla
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Solicita los permisos de ubicación necesarios para el seguimiento.
     */
    private void requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            }, REQUEST_LOCATION_PERMISSION);
        } else {
            initializeMap();
        }
    }

    /**
     * Inicializa Firebase y otras configuraciones relacionadas.
     */
    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        raceId = UUID.randomUUID().toString(); // Genera un ID único para la carrera
        carreraData = new CarreraData(); // Crear un nuevo objeto CarreraData para almacenar los datos de la carrera

        Intent intent = getIntent();
        activityType = intent.getStringExtra("tipoActividad");

        pathPointsData = new ArrayList<>(); // Crea una lista de LatLngData para almacenar los pathPoints
    }

    /**
     * Configura los listeners para los botones.
     */
    private void setupListeners() {
        resumeButton.setOnClickListener(v -> resumeTracking());
        stopButtonVolver.setOnClickListener(v -> navigateToMain());
        pauseButton.setOnClickListener(v -> pauseTracking());
        startButton.setOnClickListener(v -> startTracking());
        stopButton.setOnClickListener(v -> stopTracking());
    }

    /**
     * Reanuda el seguimiento de la actividad.
     */
    private void resumeTracking() {
        isTracking = true;
        chronometer.setBase(SystemClock.elapsedRealtime() - timeElapsed);
        chronometer.start();
        startLocationUpdates();
        resumeButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.VISIBLE);
        startButton.setVisibility(View.GONE);
    }

    /**
     * Pausa el seguimiento de la actividad.
     */
    private void pauseTracking() {
        isTracking = false;
        timeElapsed = SystemClock.elapsedRealtime() - chronometer.getBase();
        chronometer.stop();
        pauseLocationUpdates();
        pauseButton.setVisibility(View.GONE);
        resumeButton.setVisibility(View.VISIBLE);
        stopButtonVolver.setVisibility(View.GONE);
        stopButton.setVisibility(View.VISIBLE);
    }

    /**
     * Inicia el seguimiento de la actividad.
     */
    private void startTracking() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            isTracking = true;
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            startLocationUpdates();

            // Obtén la fecha y hora actual
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH empieza los meses desde 0 hay que añadir uno
            int year = calendar.get(Calendar.YEAR);
            int startHour = calendar.get(Calendar.HOUR_OF_DAY);
            int startMinute = calendar.get(Calendar.MINUTE);
            carreraData.setDay(day);
            carreraData.setMonth(month);
            carreraData.setYear(year);
            carreraData.setStartHour(startHour);
            carreraData.setStartMinute(startMinute);

            startButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
            stopButtonVolver.setVisibility(View.GONE);
            stopButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Detiene el seguimiento de la actividad y guarda los datos.
     */
    private void stopTracking() {
        isTracking = false;
        chronometer.stop();
        stopLocationUpdates();
        startButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.GONE);
        resumeButton.setVisibility(View.GONE);
        stopButtonVolver.setVisibility(View.GONE);

        // Obtén el tiempo transcurrido
        long timeElapsed = SystemClock.elapsedRealtime() - chronometer.getBase();
        Calendar calendar = Calendar.getInstance();
        int endHour = calendar.get(Calendar.HOUR_OF_DAY);
        int endMinute = calendar.get(Calendar.MINUTE);
        carreraData.setRaceId(raceId);
        carreraData.setTimeElapsed(timeElapsed);
        carreraData.setTotalDistance(totalDistance);
        carreraData.setEndHour(endHour);
        carreraData.setEndMinute(endMinute);
        carreraData.setActivityType(activityType);

        // Crear un Intent para pasar los detalles de la carrera y las coordenadas GPS al otro Activity
        Intent intent = new Intent(TrackingActivity.this, DetallesCarreraActivity.class);
        intent.putExtra("carreraData", carreraData);
        intent.putParcelableArrayListExtra("ruta_gps", (ArrayList<LatLng>) pathPoints);
        startActivity(intent);
    }

    /**
     * Inicializa el mapa y establece las configuraciones necesarias.
     */
    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            polylineOptions = new PolylineOptions().width(10).color(Color.RED);

            locationRequest = LocationRequest.create();
            locationRequest.setInterval(10000); // Establece el intervalo deseado para las actualizaciones de ubicación activas, en milisegundos.
            locationRequest.setFastestInterval(5000); // Establece la tasa más rápida para las actualizaciones de ubicación activas, en milisegundos.
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // Establece la prioridad de la solicitud.

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null || !isTracking) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        if (location != null && isTracking) {
                            LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            pathPoints.add(newLocation);
                            polylineOptions.add(newLocation); // Añadir solo el nuevo punto a la polylineOptions
                            mMap.clear();
                            mMap.addPolyline(polylineOptions);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 20));

                            // Calcular la distancia
                            if (lastLocation != null) {
                                totalDistance += lastLocation.distanceTo(location) / 1000; // distanceTo() devuelve la distancia en metros, así que la convertimos a kilómetros
                            }
                            lastLocation = location;

                            // Actualizar la distancia en la interfaz de usuario
                            distanceTextView.setText(String.format(Locale.getDefault(), "%.2f km", totalDistance));
                        }
                    }
                }
            };
        }
    }

    /**
     * Método llamado cuando el mapa está listo para ser usado.
     * @param googleMap La instancia de GoogleMap que se puede usar para modificar el mapa.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Obtiene la última ubicación conocida
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                    }
                });
    }

    /**
     * Inicia las actualizaciones de la ubicación.
     */
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    /**
     * Detiene las actualizaciones de la ubicación.
     */
    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    /**
     * Pausa las actualizaciones de la ubicación.
     */
    private void pauseLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    /**
     * Método llamado cuando se han solicitado permisos al usuario.
     * @param requestCode El código de solicitud pasado en requestPermissions(android.app.Activity, String[], int).
     * @param permissions La lista de permisos solicitados.
     * @param grantResults Los resultados de las solicitudes de permisos correspondientes.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && allPermissionsGranted(grantResults)) {
                // Todos los permisos han sido concedidos, recargar el Activity
                finish(); // Finaliza el Activity actual
                startActivity(getIntent()); // Inicia una nueva instancia del mismo Activity
            } else {
                // Manejar el caso donde los permisos no fueron concedidos
                Toast.makeText(this, "Los permisos son necesarios para el funcionamiento de la app.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Verifica si todos los permisos solicitados han sido concedidos.
     * @param grantResults Los resultados de las solicitudes de permisos correspondientes.
     * @return true si todos los permisos han sido concedidos, false en caso contrario.
     */
    private boolean allPermissionsGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Navega a la actividad principal.
     */
    private void navigateToMain() {
        Intent intent = new Intent(this, ActividadMenuActivity.class);
        startActivity(intent);
    }
}

