package com.danganhkhoa.flamealert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.res.ResourcesCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    public static final String CHANNEL_ID = "Channel_1";
    TextView _FeelLike, _Temperature, _Smoke, _Humidity, _DHTSensorState, _SmokeSensorState;
    View _FeelLikeLayout, _TemperLayout, _HumLayout, _SmokeLayout, _DHTLayout, _SmokeSensorLayout;
//    Button _nof;
    DatabaseReference _MainReference;
    String error[] = {"DHT", "Smoke"},
            state[] = {"Flammable", "Via_Esp"},
            value[] = {"Heat_Index", "Humidity", "Smoke_Index", "Temperature"},
            dbChild[] = {"Error", "State", "Value"};
    Boolean ErrorState[] = {false, false}, StateValue[] = {false, false};
    Integer SensorValue[] = {0, 0, 0, 0};
    private int notificationId = 1;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _FeelLike = findViewById(R.id.feellike_text);
        _Temperature = findViewById(R.id.temperature_text);
        _Smoke = findViewById(R.id.smoke_text);
        _Humidity = findViewById(R.id.humidity_text);
        _DHTSensorState = findViewById(R.id.dht_state);
        _SmokeSensorState = findViewById(R.id.smoke_sensor_state);
        _FeelLikeLayout = findViewById(R.id.feellike_layout);
        _TemperLayout = findViewById(R.id.temp_layout);
        _HumLayout = findViewById(R.id.hum_layout);
        _SmokeLayout = findViewById(R.id.smoke_layout);
        _DHTLayout = findViewById(R.id.dhtstate_layout);
        _SmokeSensorLayout = findViewById(R.id.smokesensor_statelayout);
//        _nof = findViewById(R.id.notificatuon_btn);
        _MainReference = FirebaseDatabase.getInstance().getReference();
//        TextView _ValueView[] = {_FeelLike, _Humidity, _Smoke, _Temperature},
//                _ErrorView[] = {_DHTSensorState, _SmokeSensorState};
        context = getApplicationContext();
        createNotificationChannel();
        syncData(_MainReference);
//        _nof.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                senNotification();
//            }
//        });
//        setValueView(_ValueView);
//        setErrorView(_ErrorView);
    }

    void syncData(DatabaseReference reff){
        reff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Fetch data
                for(int i = 0; i < error.length; i++){
                    ErrorState[i] = Boolean.parseBoolean(snapshot.child(dbChild[0]).child(error[i]).getValue().toString());
                    StateValue[i] = Boolean.parseBoolean(snapshot.child(dbChild[1]).child(state[i]).getValue().toString());
                }
                for(int i = 0; i < value.length; i++){
//                    System.out.println(snapshot.child(dbChild[2]).getValue());
                    SensorValue[i] = Integer.parseInt(snapshot.child(dbChild[2]).child(value[i]).getValue().toString());
                }
//                Set error view
//                DHT State
                if(!ErrorState[0]){
                    _DHTSensorState.setText("Tốt");
                    _DHTLayout.setBackground(ResourcesCompat
                            .getDrawable(getResources()
                                    , R.drawable.shape_module_good
                                    , null));
                }else {
                    _DHTSensorState.setText("Hỏng");
                    _DHTLayout.setBackground(ResourcesCompat
                            .getDrawable(getResources()
                                    , R.drawable.shape_module
                                    , null));
                }
//                Smoke Sensor State
                if(!ErrorState[1]){
                    _SmokeSensorState.setText("Tốt");
                    _SmokeSensorLayout.setBackground(ResourcesCompat
                            .getDrawable(getResources()
                                    , R.drawable.shape_module_good
                                    , null));
                }else {
                    _SmokeSensorState.setText("Hỏng");
                    _SmokeSensorLayout.setBackground(ResourcesCompat
                            .getDrawable(getResources()
                                    , R.drawable.shape_module
                                    , null));
                }
//                Set value view
                if(!StateValue[0] && SensorValue[0] < 80 && SensorValue[3] < 60 && SensorValue[2] < 800){
                    _FeelLike.setText("Mọi thứ vẫn bình thường...");
                    _FeelLikeLayout.setBackground(ResourcesCompat
                            .getDrawable(getResources()
                                    , R.drawable.shape_everythingisgood
                                    , null));
                } else{
                    _FeelLike.setText("Chà, hình như nhà đang cháy...");
                    _FeelLikeLayout.setBackground(ResourcesCompat
                            .getDrawable(getResources()
                                    , R.drawable.shape_notgood
                                    , null));
                    senNotification();
                }
//                Set Temperature view
                _Temperature.setText(SensorValue[3] + "°C");
                if(SensorValue[3] > 60){
                    _TemperLayout.setBackground(ResourcesCompat
                            .getDrawable(getResources()
                                    , R.drawable.shape_temper_infire
                                    , null));
                }else if(SensorValue[3] > 30){
                    _TemperLayout.setBackground(ResourcesCompat
                            .getDrawable(getResources()
                                    , R.drawable.shape_temper_sohot
                                    , null));
                }else{
                    _TemperLayout.setBackground(ResourcesCompat
                            .getDrawable(getResources()
                                    , R.drawable.shape_temper
                                    , null));
                }
//                Set Smoke view
                _Smoke.setText(SensorValue[2].toString());
                if(SensorValue[2] > 800){
                    _SmokeLayout.setBackground(ResourcesCompat
                            .getDrawable(getResources()
                                    , R.drawable.shape_smoke_alot
                                    , null));
                }else if(SensorValue[2] > 500){
                    _SmokeLayout.setBackground(ResourcesCompat
                            .getDrawable(getResources()
                                    , R.drawable.shape_smoke_alittle
                                    , null));
                }else {
                    _SmokeLayout.setBackground(ResourcesCompat
                            .getDrawable(getResources()
                                    , R.drawable.shape_smoke
                                    , null));
                }
//                Set Humidity view
                _Humidity.setText(SensorValue[1].toString());
                if(SensorValue[1] > 40){
                    _Humidity.setTextColor(Color.WHITE);
                    _HumLayout.setBackground(ResourcesCompat
                            .getDrawable(getResources()
                                    , R.drawable.shape_humidity
                                    , null));
                }else{
                    _Humidity.setTextColor(Color.BLACK);
                    _HumLayout.setBackground(ResourcesCompat
                            .getDrawable(getResources()
                                    , R.drawable.shape_humidity_dried
                                    , null));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    void setValueView(TextView valueView[]){
        for(int i = 0; i < valueView.length; i++){
            valueView[i].setText(SensorValue[i]);
        }
    }
    void setErrorView(TextView errorView[]){
        for(int i = 0; i < errorView.length; i++){
            if(!ErrorState[i]){
                errorView[i].setText("Tốt");
            }else{
                errorView[i].setText("Hỏng");
            }
        }
    }
    void senNotification(){

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.shape_notgood)
                .setContentTitle("Warning!")
                .setContentText("Nhà đang cháy! Gọi 114 ngay!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, notification.setOnlyAlertOnce(true).build());
    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}