package com.tfg.saludenmarcha;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PesoActivity es una actividad que permite al usuario registrar y gestionar sus datos de peso corporal diario.
 * El usuario puede ingresar su peso, seleccionar una fecha específica y guardar los datos en Firebase Firestore.
 * También permite visualizar un gráfico con la evolución del peso y regresar a la actividad principal.
 */
public class PesoActivity extends AppCompatActivity {
    // Variables de interfaz de usuario para entrada y botones
    private EditText pesoInput, alturaInput;
    private Button saveButton, datePickerButton, volverButton, graficaPesoButton;

    // Variables para manejo de Firebase Firestore y autenticación
    private FirebaseFirestore db;
    private AnyChartView anyChartView;
    private FirebaseAuth mAuth;
    private String idUser;

    // Variables para almacenar la fecha seleccionada
    private int selectedDay, selectedMonth, selectedYear;

    // Variable para almacenar el peso y el ID de la actividad
    private Long height = null;
    private long idActividad;
    private boolean isChartVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peso);
        EdgeToEdge.enable(this);

        initializeUI();
        initializeFirebase();
        setupListeners();

        //Pie pie = AnyChart.pie(); //grafico  circular
        // Cargar los datos de peso desde Firebase
        loadWeightData();
        loadHeightData();

    }

    /**
     * Inicializa componentes de la interfaz de usuario.
     */
    private void initializeUI() {
        pesoInput = findViewById(R.id.peso_text);
        pesoInput.requestFocus();
        alturaInput = findViewById(R.id.altura_text);
        saveButton = findViewById(R.id.buttonSavePeso);
        datePickerButton = findViewById(R.id.peso_picker_button);
        graficaPesoButton = findViewById(R.id.graficaPesoButton);
        volverButton = findViewById(R.id.volverPesoButton);
        anyChartView = findViewById(R.id.any_chart_peso);
        anyChartView.setVisibility(View.GONE);

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
            //
            // obtenerIdMasAltoActividad();  // Llama a obtener el ID más alto al iniciar
        } else {
            // Redirigir al usuario a la pantalla de login o mostrar un mensaje adecuado
        }
        db = FirebaseFirestore.getInstance();
        obtenerIdMasAltoActividad();
    }

    /**
     * Establece listeners para los componentes de la interfaz.
     */
    private void setupListeners() {
        datePickerButton.setOnClickListener(this::openDatePicker);
        saveButton.setOnClickListener(this::saveAllData);
        graficaPesoButton.setOnClickListener(v -> showGraphic());
        volverButton.setOnClickListener(v -> navigateToMain());
    }
    private void saveAllData(View view) {
        saveWeight(view);
        saveHeightData();
    }

    /**
     * Abre el DatePicker para seleccionar una fecha.
     */
    private void openDatePicker(View view) {
        // Construir un CalendarConstraints para limitar la selección de fechas
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setValidator(DateValidatorPointForward.now());  // Opcional: Restringir fechas pasadas

        // Crear un MaterialDatePicker
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Selecciona una fecha");
        builder.setCalendarConstraints(constraintsBuilder.build());

        MaterialDatePicker<Long> datePicker = builder.build();

        // Mostrar el DatePicker
        datePicker.show(getSupportFragmentManager(), datePicker.toString());

        // Configurar el callback para manejar la fecha seleccionada
        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);

            selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
            selectedMonth = calendar.get(Calendar.MONTH) + 1;  // Ajustar por índice base 0
            selectedYear = calendar.get(Calendar.YEAR);

            Toast.makeText(this, "Fecha seleccionada: " + selectedDay + "/" + selectedMonth + "/" + selectedYear, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Guarda el peso en Firestore.
     */
    private void saveWeight(View view) {
        String text = pesoInput.getText().toString();

        if (text.isEmpty()) {
            Toast.makeText(this, "El campo de peso no puede estar vacío.", Toast.LENGTH_SHORT).show();
            return;
        }

        long peso = 0;
        try {
            peso = Long.parseLong(text);
            if (peso < 1 || peso > 250) {
                Toast.makeText(this, "El peso debe estar entre 1 y 250.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, ingresa un número válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDay == 0 || selectedMonth == 0 || selectedYear == 0) {
            Toast.makeText(this, "Por favor, selecciona una fecha", Toast.LENGTH_SHORT).show();
            return;
        }
        double alturaMetros = height / 100.0; // Asegúrate de que height está en centímetros
        double imc = peso / (alturaMetros * alturaMetros); // Calcula el IMC
        String resultadoIMC = classifyIMC(imc); // Clasifica el IMC
        Map<String, Object> pesoData = new HashMap<>();
        pesoData.put("idUser", idUser);
        pesoData.put("weight", peso);
        pesoData.put("day", selectedDay);
        pesoData.put("month", selectedMonth);
        pesoData.put("year", selectedYear);
        pesoData.put("id", idActividad);
        pesoData.put("imc", imc); // Guarda el IMC también
        pesoData.put("resultIMC", resultadoIMC); // Guarda el resultado del IMC (Bajo, Normal, Alto

        db.collection("weights").add(pesoData)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Peso añadido correctamente.", Toast.LENGTH_SHORT).show();
                    // Actualizar el gráfico después de guardar los datos
                    pesoInput.setText("");
                    loadWeightData();
                    // Esperar 3 segundos antes de actualizar el gráfico, para permitir que los datos se guarden en Firestore
                    //new Handler().postDelayed(this::loadWeightData, 19000);
                    startActivity(new Intent(this, PesoActivity.class));

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar el peso.", Toast.LENGTH_LONG).show();
                });
    }


    /**
     * Muestra u oculta la gráfica de peso.
     */
    private void showGraphic() {
        if (isChartVisible) {
            anyChartView.setVisibility(View.GONE);
            isChartVisible = false;
        } else {
            anyChartView.setVisibility(View.VISIBLE);
            isChartVisible = true;
        }
    }

    /**
     * Vuelve a la actividad principal.
     */
    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
    }

    /**
     * Obtiene el ID más alto registrado en la colección 'weights' y prepara el siguiente ID para una nueva entrada.
     * Este método asume que la colección 'weights' utiliza un campo llamado 'id' para almacenar un identificador numérico.
     */
    private void obtenerIdMasAltoActividad() {
        // Consulta a la colección 'weights' para encontrar el documento con el ID más alto para el usuario actual.
        db.collection("weights")
                .whereEqualTo("idUser", idUser)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        // Verificar si hay un error al escuchar los cambios en la base de datos.
                        if (e != null) {
                            return;
                        }

                        // Crear una referencia a la colección 'weights'.
                        CollectionReference activitiesRef = db.collection("weights");

                        // Crear una consulta para obtener el documento con el ID más alto, ordenando de forma descendente y limitando a 1 resultado.
                        Query query = activitiesRef.orderBy("id", Query.Direction.DESCENDING).limit(1);

                        // Ejecutar la consulta y manejar el resultado.
                        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    // Verificar si se encontraron documentos.
                                    if (!task.getResult().isEmpty()) {
                                        idActividad = 0;
                                        // Iterar sobre los documentos recuperados (en este caso solo uno).
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            // Verificar si el documento contiene el campo 'id'.
                                            if (document.contains("id")) {
                                                // Obtener el valor del campo 'id' y preparar el próximo ID.
                                                long highestId = document.getLong("id");
                                                if (highestId > Integer.MAX_VALUE) {
                                                    System.out.println("El ID excede el máximo valor para un int");
                                                } else {
                                                    idActividad = highestId + 1;
                                                    System.out.println("El ID de la próxima actividad será: " + idActividad);
                                                }
                                            } else {
                                                System.out.println("Documento no contiene el campo 'id'.");
                                            }
                                        }
                                    } else {
                                        System.out.println("No se encontraron documentos.");
                                    }
                                } else {
                                    System.out.println("Error obteniendo documentos: " + task.getException());
                                }
                            }
                        });
                    }
                });
    }



    /**
     * Carga los datos de peso desde Firestore para el usuario actual.
     * Ordena los documentos por el campo 'id' en orden descendente y limita a los 20 documentos más recientes.
     * Cada entrada se agrega a una lista de datos que se utiliza para actualizar el gráfico.
     */
    private void loadWeightData() {
        // Verificar si el ID de usuario está disponible.
        if (idUser == null || idUser.isEmpty()) {
            Toast.makeText(this, "ID de usuario no encontrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("PesoActivity", "Cargando datos para el usuario: " + idUser);

        // Consulta a la colección 'weights' para obtener los datos de peso del usuario actual, ordenados por 'id' en orden descendente.
        db.collection("weights")
                .whereEqualTo("idUser", idUser)
                .orderBy("id", Query.Direction.DESCENDING)
                .limit(20)  // Limitar a los 20 documentos con los IDs más altos para el usuario específico.
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        // Verificar si hay un error al escuchar los cambios en la base de datos.
                        if (e != null) {
                            return;
                        }

                        // Si hay resultados, procesar los documentos recuperados.
                        if (value != null) {
                            List<DataEntry> entries = new ArrayList<>();
                            List<DataEntry> imcEntry = new ArrayList<>();
                            List<DataEntry> imcResultEntry = new ArrayList<>();
                            // Iterar sobre los documentos recuperados.
                            for (QueryDocumentSnapshot document : value) {
                                Log.d("PesoActivity", "Documento recuperado: " + document.getData());

                                // Verificar si el documento tiene los campos 'weight', 'day', 'month', 'year', y 'id'.
                                if (document.contains("weight") && document.contains("day") && document.contains("month") && document.contains("year") && document.contains("id")) {
                                    Long weightNumber = document.getLong("weight");
                                    Long imcNumber = document.getLong("imc");
                                    Long day = document.getLong("day");
                                    Long month = document.getLong("month");
                                    Long year = document.getLong("year");
                                    Long id = document.getLong("id");
                                    String imcResult = document.getString("resultIMC");

                                    Log.d("PesoActivity", "ID: " + id + " Weight: " + weightNumber + " Date: " + day + "/" + month + "/" + year);

                                    if (weightNumber != null && day != null && month != null && year != null) {
                                        // Ensamblar la fecha en un formato legible y diferenciar entre pesos del mismo día usando 'id'.
                                        String date = day + "/" + month + "/" + year + " (" + " IMC: "+imcResult + ")";
                                        entries.add(new ValueDataEntry(date, weightNumber));
                                        imcEntry.add(new ValueDataEntry(date, imcNumber));
                                    }
                                } else {
                                    Log.d("PesoActivity", "Documento sin 'weight', 'day', 'month', 'year' o 'id': " + document.getId());
                                }
                            }
                            Log.d("PesoActivity", "Número de entradas: " + entries.size());
                            if (!entries.isEmpty()) {
                                // Asegurarse de actualizar el gráfico en el hilo principal.
                                runOnUiThread(() -> updateChart(entries, imcEntry));
                            } else {
                                Toast.makeText(PesoActivity.this, "No se encontraron datos de peso.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }


    /**
     * Actualiza el gráfico de líneas con los datos proporcionados.
     *
     * @param entries Lista de entradas de datos que se utilizarán para actualizar el gráfico.
     */
    private void updateChart(List<DataEntry> entries, List<DataEntry> imcEntries) {
        // Obtener la vista del gráfico desde el layout.
        AnyChartView anyChartView = findViewById(R.id.any_chart_peso);

        // Crear una instancia del gráfico de líneas.
        Cartesian cartesian = AnyChart.line();

        // Establecer el título del gráfico.
        cartesian.title("Evolución del Peso");

        // Invertir el eje X
        cartesian.xScale().inverted(true);

        // Crear una serie de líneas y agregar los datos a la serie.
        Line series = cartesian.line(entries);
        series.name("Peso");
        series.stroke("2 #0000FF"); // Establece el color de la línea del peso a azul

        Line imcSeries = cartesian.line(imcEntries);
        imcSeries.name("IMC");
        imcSeries.stroke("2 #FF0000"); // Establece el color de la línea del IMC a rojo

        // Configurar el gráfico con los datos de la serie.
        anyChartView.setChart(cartesian);

        // Forzar la invalidación de la vista para asegurar que se actualice visualmente.
        anyChartView.invalidate();
    }


    //para la altura

    private void loadHeightData() {
        if (idUser == null || idUser.isEmpty()) {
            Toast.makeText(this, "ID de usuario no encontrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference heightDoc = db.collection("heights").document(idUser);

        heightDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    //Log.d("PesoActivity", "Documento de altura recuperado: " + document.getData());
                    Long heightNumber = document.getLong("height");
                    //Toast.makeText(this, "Altura cargada." + heightNumber +" cm", Toast.LENGTH_SHORT).show();
                    height = heightNumber;
                    if (heightNumber != null) {
                        alturaInput.setText(String.valueOf(heightNumber));
                    } else {
                        alturaInput.setText("");
                    }
                } else {
                    //Toast.makeText(PesoActivity.this, "No se encontraron datos de altura.", Toast.LENGTH_SHORT).show();
                    alturaInput.setText("");
                }
            } else {
                //Toast.makeText(PesoActivity.this, "Error al cargar los datos de altura.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void saveHeightData() {
        String alturaStr = alturaInput.getText().toString();
        if (alturaStr.isEmpty()) {
            Toast.makeText(this, "El campo de altura no puede estar vacío.", Toast.LENGTH_SHORT).show();
            return;
        }

        long altura;
        try {
            altura = Long.parseLong(alturaStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, ingresa un número válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Guarda o actualiza el documento con la altura del usuario.
        Map<String, Object> data = new HashMap<>();
        data.put("height", altura);

        db.collection("heights").document(idUser) // Usar idUser como ID del documento.
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    //Toast.makeText(this, "Altura guardada correctamente.", Toast.LENGTH_SHORT).show();
                    alturaInput.setText(String.valueOf(altura)); // Actualizar la vista con el valor guardado
                    loadHeightData(); // Recargar los datos para reflejar los cambios
                    //startActivity(new Intent(this, PesoActivity.class)); // Cambiar de actividad si es necesario
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar la altura.", Toast.LENGTH_LONG).show());
    }

    // Función para clasificar el IMC
    private String classifyIMC(double imc) {
        if (imc < 18.5) return "Bajo";
        if (imc >= 18.5 && imc <= 24.9) return "Normal";
        return "Alto";
    }

}
