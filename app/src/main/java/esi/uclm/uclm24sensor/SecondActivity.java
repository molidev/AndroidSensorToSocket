package esi.uclm.uclm24sensor;

import static androidx.activity.EdgeToEdge.*;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SecondActivity  extends AppCompatActivity {

    private ReadingSensors mBoundService;
    private boolean mIsBound;
    Intent message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enable(this);
        setContentView(R.layout.second_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.stopActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Log.i("uclm_sensors", "Launching the service ReadingSensors\n");
        doBindService();
    }

    void doBindService(){
        Intent intent = new Intent(this, ReadingSensors.class);
        bindService(intent, mConnetion, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService(){
        if(mIsBound){
            unbindService(mConnetion);
            mIsBound = false;
        }
    }

    private ServiceConnection mConnetion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBoundService = ((ReadingSensors.LocalBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBoundService = null;
        }
    };

    public void onClickStop(View v){
        if (v.getId()==R.id.btnStop){
            doUnbindService();
            Intent intent = new Intent(SecondActivity.this, ReadingSensors.class);
            stopService(intent);

            intent = new Intent(SecondActivity.this, MainActivity.class);
            startActivity(intent);
            if(!mIsBound){
                Log.i("uclm_sensors", "Service was stopped sucessfully");
            }
        }
    }

}
