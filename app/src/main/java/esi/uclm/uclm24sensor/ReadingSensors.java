package esi.uclm.uclm24sensor;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ReadingSensors extends IntentService implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private volatile Socket socket;
    private static final int SERVERPORT = 5000;
    private static final String SERVER_IP = "10.0.2.2";

    private OutputStream outputStream;
    private long lastUpdateTime = 0;
    private int contador;

    public ReadingSensors() {
        super("ReadingSensors");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("Reading sensor", "Prueba desde el servicio");
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {
            Log.i("ReadingSensors","No se ha detecttado sensor de acelerómetro en este dispositivo");
        }
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        new Thread(new ClientThread()).start();//Thread para el websocket
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        contador++;
        if (contador == 300){
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastUpdateTime > 10000) {
                    lastUpdateTime = currentTime;
                    String message = "Accelerometer data - X: " + event.values[0] + ", Y: " + event.values[1] + ", Z: " + event.values[2]+ "\n";
                    sendInformationToSocket(message);
                }
            }
            contador = 0;
        }

    }

    public void sendInformationToSocket(final String message){
        new Thread(() -> {
            try {
                if (socket != null && !socket.isClosed()) {
                    outputStream = socket.getOutputStream();
                    outputStream.write(message.getBytes());
                    outputStream.flush();
                } else {
                    Log.e("ReadingSensors", "Socket is null or closed");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Handle accuracy changes here
    }

    public class LocalBinder extends Binder {
        ReadingSensors getService() {
            return ReadingSensors.this;
        }
    }

    class ClientThread implements Runnable {
        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVERPORT);
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

    }
}
