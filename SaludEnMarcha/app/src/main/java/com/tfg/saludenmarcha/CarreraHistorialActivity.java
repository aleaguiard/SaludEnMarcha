package com.tfg.saludenmarcha;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.google.firebase.firestore.EventListener;
public class CarreraHistorialActivity extends AppCompatActivity {
    // Declaración de variables para los TextViews y botones
    private TextView tipoActividadText;
    private TextView distanciaTotalText;
    private TextView tiempoText;
    private TextView horaInicioText;
    private TextView horaFinText;
    private Button datePickerButton;
    private Button volverButton;
    private Button deleteActivityButton;

    // Declaración de variables para Firestore y autenticación
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String idUser;
    private DocumentReference currentActivityDocument;
    private GoogleMap mMap;
    private ArrayList<LatLng> rutaGps;
    private List<GeoPoint> geoPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrera_historial);
        EdgeToEdge.enable(this);  // Ajuste de la interfaz para pantallas con bordes curvos

        // Inicializar la interfaz de usuario
        initializeUI();

        // Configurar Firebase y obtener el ID del usuario actual
        initializeFirebase();

        // Establecer listeners para los componentes de la interfaz
        setupListeners();

        // Cargar la última actividad al iniciar
        fetchLatestActivity();
    }

    /**
     * Inicializa componentes de la interfaz de usuario.
     */
    private void initializeUI() {
        tipoActividadText = findViewById(R.id.tipoActividadHistorial);
        distanciaTotalText = findViewById(R.id.totalDistanceHistorial);
        tiempoText = findViewById(R.id.timeElapsedHistorial);
        horaInicioText = findViewById(R.id.startTimeHistorial);
        horaFinText = findViewById(R.id.endTimeHistorial);
        datePickerButton = findViewById(R.id.date_picker_buttonHistorial);
        volverButton = findViewById(R.id.volverHistorialButton);
        deleteActivityButton = findViewById(R.id.deleteActivityButton);
        deleteActivityButton.setVisibility(View.GONE); // Ocultar botón de eliminar al inicio

        // Ajuste de la vista para adaptarse a los bordes de la pantalla
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Configura Firebase y la autenticación.
     */
    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            idUser = currentUser.getUid();
        } else {
            // Redirigir al usuario a la pantalla de login o mostrar un mensaje adecuado
            Toast.makeText(this, "ID de usuario no encontrado.", Toast.LENGTH_SHORT).show();
        }
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Establece listeners para los componentes de la interfaz.
     */
    private void setupListeners() {
        datePickerButton.setOnClickListener(this::openDatePicker);
        volverButton.setOnClickListener(v -> navigateToMain());
        deleteActivityButton.setOnClickListener(this::deleteActivity);
    }

    /**
     * Abre el DatePicker para seleccionar una fecha.
     */
    private void openDatePicker(View view) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(CarreraHistorialActivity.this, (datePicker, y, m, d) -> {
            // El usuario ha seleccionado una fecha, se buscan las actividades de esa fecha en la base de datos
            fetchActivitiesForDate(y, m + 1, d);
        }, year, month, day).show();
    }

    /**
     * Navega a la actividad principal.
     */
    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
    }

    /**
     * Elimina la actividad actual de Firestore.
     */
    private void deleteActivity(View view) {
        // Verificar si hay una actividad seleccionada actualmente para eliminar
        if (currentActivityDocument != null) {
            currentActivityDocument.delete()
                    .addOnSuccessListener(aVoid -> {
                        // La actividad se eliminó correctamente
                        Toast.makeText(CarreraHistorialActivity.this, "La actividad se ha eliminado correctamente", Toast.LENGTH_SHORT).show();
                        // Limpiar los TextViews después de eliminar la actividad
                        clearActivityDetails();
                    })
                    .addOnFailureListener(e -> Toast.makeText(CarreraHistorialActivity.this, "Error al eliminar la actividad: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            // No hay actividad seleccionada para eliminar
            Toast.makeText(CarreraHistorialActivity.this, "No hay actividad seleccionada para eliminar", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Limpia los detalles de la actividad mostrados en la interfaz de usuario.
     */
    private void clearActivityDetails() {
        tipoActividadText.setText("Tipo de actividad: ");
        distanciaTotalText.setText("Distancia total: ");
        tiempoText.setText("Tiempo transcurrido: ");
        horaInicioText.setText("Hora de inicio: ");
        horaFinText.setText("Hora de finalización: ");
        // Limpiar el mapa después de eliminar la actividad
        if (mMap != null) {
            mMap.clear();
        }
        // Ocultar el botón de eliminación de actividad
        deleteActivityButton.setVisibility(View.GONE);
    }

    /**
     * Carga la actividad más reciente del usuario desde Firestore.
     */
    private void fetchLatestActivity() {
        // Verificar si el ID de usuario está disponible
        if (idUser == null || idUser.isEmpty()) {
            Toast.makeText(this, "ID de usuario no encontrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("activities")
                .whereEqualTo("idUser", idUser)
                .orderBy("id", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener((value, e) -> {
                    if (e != null) {
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        DocumentSnapshot document = value.getDocuments().get(0);
                        Long day = document.getLong("day");
                        Long month = document.getLong("month");
                        Long year = document.getLong("year");

                        if (day != null && month != null && year != null) {
                            String date = day + "/" + month + "/" + year;
                            Toast.makeText(CarreraHistorialActivity.this, "Mostrando la última actividad realizada el " + date, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(CarreraHistorialActivity.this, "Mostrando la última actividad realizada.", Toast.LENGTH_SHORT).show();
                        }

                        showActivity(document);
                    } else {
                        Toast.makeText(CarreraHistorialActivity.this, "No hay actividades registradas.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Busca actividades para una fecha específica en Firestore.
     */
    private void fetchActivitiesForDate(int year, int month, int day) {
        // Limpieza de los TextViews antes de realizar la búsqueda
        clearActivityDetails(); // Limpiar los detalles antes de la búsqueda

        db.collection("activities")
                .whereEqualTo("idUser", idUser)
                .whereEqualTo("day", day)
                .whereEqualTo("month", month)
                .whereEqualTo("year", year)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                        if (documents.size() == 1) {
                            // Solo hay una actividad, mostrarla
                            showActivity(documents.get(0));
                        } else {
                            // Hay más de una actividad, permitir al usuario elegir
                            showActivityOptions(documents);
                        }
                    } else {
                        Toast.makeText(CarreraHistorialActivity.this, "No hay datos de actividades para esta fecha", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(CarreraHistorialActivity.this, "Error: ", Toast.LENGTH_LONG).show());
    }

    /**
     * Muestra un diálogo para que el usuario elija entre varias actividades.
     */
    private void showActivityOptions(List<DocumentSnapshot> activityDocuments) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hay varias actividades ese mismo día. Seleccione una actividad");

        List<String> activityNames = new ArrayList<>();
        for (DocumentSnapshot document : activityDocuments) {
            Map<String, Object> activityData = document.getData();
            if (activityData != null && activityData.containsKey("activityType")) {
                String activityType = (String) activityData.get("activityType");
                String hour = activityData.get("startHour").toString();
                String minute = activityData.get("startMinute").toString();
                String activityInfo = activityType + " - " + hour + ":" + minute;
                activityNames.add(activityInfo);
            }
        }

        final String[] activityNamesArray = activityNames.toArray(new String[0]);
        builder.setItems(activityNamesArray, (dialog, which) -> showActivity(activityDocuments.get(which)));
        builder.show();
    }

    /**
     * Muestra los detalles de una actividad seleccionada.
     */
    private void showActivity(DocumentSnapshot document) {
        // Limpiar el mapa antes de mostrar la nueva actividad
        if (mMap != null) {
            mMap.clear();
        }
        // Mostrar el botón de eliminación de actividad solo después de seleccionar una actividad
        deleteActivityButton.setVisibility(View.VISIBLE);
        // Mostrar los detalles de la actividad seleccionada
        currentActivityDocument = document.getReference();
        Map<String, Object> activity = document.getData();
        tipoActividadText.setText("Tipo de actividad: " + activity.get("activityType"));
        double distanciaTotal = (double) activity.get("totalDistance");
        String distanciaFormateada = String.format("%.2f", distanciaTotal);
        distanciaTotalText.setText("Distancia total: " + distanciaFormateada + " km");

        long timeElapsedMs = (long) activity.get("timeElapsed");
        long hours = TimeUnit.MILLISECONDS.toHours(timeElapsedMs);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeElapsedMs) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeElapsedMs) % 60;
        tiempoText.setText(String.format("Tiempo transcurrido: %02d:%02d:%02d", hours, minutes, seconds));
        horaInicioText.setText("Hora de inicio: " + activity.get("startHour") + ":" + ((Number) activity.get("startMinute")).intValue());
        horaFinText.setText("Hora de finalización: " + activity.get("endHour") + ":" + ((Number) activity.get("endMinute")).intValue());

        // Verificar si la actividad tiene coordenadas GPS
        if (activity.containsKey("routeGps")) {
            geoPoints = (List<GeoPoint>) activity.get("routeGps");

            // Convertir los GeoPoints a LatLng
            rutaGps = new ArrayList<>();
            for (GeoPoint geoPoint : geoPoints) {
                LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                rutaGps.add(latLng);
            }

            // Inicializar el mapa y dibujar la ruta
            initializeMap();
        } else {
            Toast.makeText(this, "La actividad no contiene coordenadas GPS", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Inicializa el mapa y dibuja la ruta GPS.
     */
    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment_historial);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                mMap = googleMap;
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                drawPolylineOnMap(rutaGps);
                if (!rutaGps.isEmpty()) {
                    LatLng firstLocation = rutaGps.get(0);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 15));
                }
            });
        }
    }

    /**
     * Dibuja una polilínea en el mapa para mostrar la ruta GPS.
     */
    private void drawPolylineOnMap(ArrayList<LatLng> rutaGpsLatLng) {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        polylineOptions.width(5);

        for (LatLng point : rutaGpsLatLng) {
            polylineOptions.add(point);
        }

        if (mMap != null) {
            mMap.addPolyline(polylineOptions);
        }
    }
}

