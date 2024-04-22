package esi.uclm.uclm24sensor;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private ReadingSensors mBoundService;
    private boolean mIsBound;
    Intent message;

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
    void doUnbindService(){
        if(mIsBound){
            unbindService(mConnetion);
            mIsBound = false;
        }
    }

    void doBindService(){
        message = new Intent(this, ReadingSensors.class);
        mIsBound = true;
    }

    public void onClickStart(View v){
        if (v.getId()==R.id.btnStart){
            Log.i("SensingActivity", "Launching the service ReadingSensors\n");
            doBindService();
            this.startService(message);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

}