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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CarreraHistorialActivity extends AppCompatActivity {
    // Declaración de variables para los TextViews y el botón
    private TextView tipoActividadText;
    private TextView distanciaTotalText;
    private TextView tiempoText;
    private TextView horaInicioText;
    private TextView horaFinText;
    private Button datePickerButton;
    private Button volverButton;
    private DocumentReference currentMealDocument;

    // Declaración de variables para Firestore
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    String idUser;
    private DocumentReference currentActivityDocument;
    private GoogleMap mMap;
    ArrayList<LatLng> rutaGps;
    List<GeoPoint> geoPoints;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_carrera_historial);

        // Inicialización de los TextViews y el botón
        tipoActividadText = findViewById(R.id.tipoActividadHistorial);
        distanciaTotalText = findViewById(R.id.totalDistanceHistorial);
        tiempoText = findViewById(R.id.timeElapsedHistorial);
        horaInicioText = findViewById(R.id.startTimeHistorial);
        horaFinText = findViewById(R.id.endTimeHistorial);
        datePickerButton = findViewById(R.id.date_picker_buttonHistorial);
        volverButton = findViewById(R.id.volverHistorialButton);

        // Inicialización de Firestore
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        idUser = mAuth.getCurrentUser().getUid();

        // Establecimiento del OnClickListener para el botón de selección de fecha
        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtención de la fecha actual
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                // Creación y visualización de un nuevo DatePickerDialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(CarreraHistorialActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                // El usuario ha seleccionado una fecha, por lo que se buscan las comidas de esa fecha en la base de datos
                                fetchActivitiesForDate(year, monthOfYear+1 , dayOfMonth);
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        volverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CarreraHistorialActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Método para buscar las actividades de una fecha específica en la base de datos
    private void fetchActivitiesForDate(int year, int month, int day) {
        // Limpieza de los TextViews antes de realizar la búsqueda
        tipoActividadText.setText("Tipo de actividad: ");
        distanciaTotalText.setText("Distancia total: ");
        tiempoText.setText("Tiempo transcurrido: ");
        horaInicioText.setText("Hora de inicio: ");
        horaFinText.setText("Hora de finalización: ");

        // Búsqueda en la base de datos de las actividades de la fecha seleccionada y actualización de los TextViews con estos datos
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
                            DocumentSnapshot document = documents.get(0);
                            showActivity(document);
                        } else {
                            // Hay más de una actividad, permitir al usuario elegir
                            showActivityOptions(documents);
                        }
                    } else {
                        Toast.makeText(CarreraHistorialActivity.this, "No hay datos de actividades para esta fecha", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CarreraHistorialActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showActivityOptions(List<DocumentSnapshot> activityDocuments) {
        // Crear un cuadro de diálogo para que el usuario elija la actividad
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccione una actividad");

        // Crear una lista de nombres de actividad para mostrar en el diálogo
        List<String> activityNames = new ArrayList<>();
        for (DocumentSnapshot document : activityDocuments) {
            Map<String, Object> activityData = document.getData();
            if (activityData != null && activityData.containsKey("activityType")) {
                activityNames.add((String) activityData.get("activityType"));
            }
        }

        // Convertir la lista a un array de cadenas
        final String[] activityNamesArray = activityNames.toArray(new String[0]);

        // Configurar el cuadro de diálogo para mostrar la lista de nombres de actividad y manejar la selección del usuario
        builder.setItems(activityNamesArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DocumentSnapshot selectedActivityDocument = activityDocuments.get(which);
                showActivity(selectedActivityDocument);
            }
        });

        // Mostrar el cuadro de diálogo
        builder.show();
    }

    private void showActivity(DocumentSnapshot document) {
        // Mostrar los detalles de la actividad seleccionada
        currentActivityDocument = document.getReference();
        Map<String, Object> activity = document.getData();
        tipoActividadText.setText("Tipo de actividad: " + activity.get("activityType"));
        //distanciaTotalText.setText("Distancia total: " + activity.get("totalDistance") + " km");
        double distanciaTotal = (double) activity.get("totalDistance");
        String distanciaFormateada = String.format("%.2f", distanciaTotal); // Formatea la distancia con dos decimales
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
            // Obtener la lista de GeoPoints
            geoPoints = (List<GeoPoint>) activity.get("routeGps");

            // Convertir los GeoPoints a LatLng
            rutaGps = new ArrayList<>();
            for (GeoPoint geoPoint : geoPoints) {
                LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                rutaGps.add(latLng);
            }

            // Dentro del método onCreate() de tu actividad
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment_historial);
            final Activity activityHistorial = CarreraHistorialActivity.this; // Almacena una referencia a la actividad actual

            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    if (ActivityCompat.checkSelfPermission(activityHistorial, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activityHistorial, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mMap.setMyLocationEnabled(true); // Habilitar la capa de mi ubicación
                    mMap.getUiSettings().setZoomControlsEnabled(true); // Habilitar controles de zoom

                    // Dibujar las polilíneas en el mapa
                    drawPolylineOnMap(rutaGps);
                    if (!rutaGps.isEmpty()) {
                        LatLng firstLocation = rutaGps.get(0);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 15));
                    }
                }
            });

        } else {
            Toast.makeText(CarreraHistorialActivity.this, "La actividad no contiene coordenadas GPS", Toast.LENGTH_SHORT).show();
        }
    }


    // Método para dibujar las polilíneas en el mapa
    private void drawPolylineOnMap(ArrayList<LatLng> rutaGpsLatLng) {
        // Agregar las polilíneas al mapa
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.BLUE);
        polylineOptions.width(5);

        for (LatLng point : rutaGpsLatLng) {
            polylineOptions.add(point);
        }

        mMap.addPolyline(polylineOptions);
    }

}