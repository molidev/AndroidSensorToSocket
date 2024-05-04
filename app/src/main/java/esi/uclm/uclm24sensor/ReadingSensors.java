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

public class ReadingSensors extends IntentService implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private volatile Socket socket;
    private static final int SERVERPORT = 5000;
    private static final String SERVER_IP = "10.0.2.2";

    private OutputStream outputStream;
    private long lastUpdateTime = 0;
    private int dataObtained;

    public ReadingSensors() {
        super("ReadingSensors");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {
            Log.i("uclm_sensors", "We can´t detect any accelerometer sensor\n");
        }
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        new Thread(new ClientThread()).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            Log.i("uclm_sensors", "There's an issue with closing the socket\n");
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        dataObtained++;
        if (dataObtained == 10){
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastUpdateTime > 10000) {
                    lastUpdateTime = currentTime;
                    String message = "Accelerometer data - X: " + event.values[0] + ", Y: " + event.values[1] + ", Z: " + event.values[2]+ "\n";
                    Log.i("uclm_sensors", "[Data obtained] --> " + message + "\n");
                    sendInformationToSocket(message);
                }
            }
            dataObtained = 0;
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
                    Log.i("uclm_sensors", "Socket is null or closed");
                }
            } catch (IOException e) {
                Log.i("uclm_sensors", "There is one exception on method sendInformationToSocket: " + e.getMessage() + "\n");
            }
        }).start();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
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
            } catch (IOException e1) {
                Log.i("uclm_sensors", "There is one exception on ClientThread: " + e1.getMessage() + "\n");
            }
        }
    }


}
