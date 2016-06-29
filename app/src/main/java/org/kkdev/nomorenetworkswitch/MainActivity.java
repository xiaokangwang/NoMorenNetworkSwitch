package org.kkdev.nomorenetworkswitch;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    /** Messenger for communicating with the service. */
    Messenger mService = null;

    /** Flag indicating whether we have called bind on the service. */
    boolean mBound;

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = new Messenger(service);
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };


    protected boolean is_enabled=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final TextView textView_current_Status = (TextView)findViewById(R.id.current_Status);

        final Button btn_disable_switch = (Button) findViewById(R.id.btn_disableswitch);
        final MainActivity me = this;

        Intent intent = new Intent(me,NetworkSwitcher.class);
        startService(intent);
        get_status_report();
        btn_disable_switch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                is_enabled=!is_enabled;
                if(!is_enabled){
                    textView_current_Status.setText("Switcher is being enabled, please wait....");
                    //sayHello();
                    Set_Switch_On();
                }else{
                    textView_current_Status.setText("Switcher is currently disabled.");
                    Set_Switch_Off();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, NetworkSwitcher.class), mConnection,
                Context.BIND_AUTO_CREATE);
        get_status_report();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, MainSettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sayHello() {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, NetworkSwitcher.MSG_SAY_HELLO, 0, 0);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    private void Set_Switch_On() {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, NetworkSwitcher.MSG_Set_Switch_On, 0, 0);
        msg.replyTo = new Messenger(new ResponseHandler());
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    private void Set_Switch_Off() {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, NetworkSwitcher.MSG_Set_Switch_Off, 0, 0);
        msg.replyTo = new Messenger(new ResponseHandler());
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void get_status_report() {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, NetworkSwitcher.MSG_Get_Status_Report, 0, 0);
        msg.replyTo = new Messenger(new ResponseHandler());
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    // This class handles the Service response
    class ResponseHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            int respCode = msg.what;
            final TextView textView_current_Status = (TextView)findViewById(R.id.current_Status);
            switch (respCode) {
                case NetworkSwitcher.MSG_Remote_Ind_Set: {
                    String result = msg.getData().getString("WritingData");
                    textView_current_Status.setText(result);
                    break;
                }
                case NetworkSwitcher.MSG_Remote_Status_Report:{
                    boolean remote_stat=msg.getData().getBoolean("Switch");
                    is_enabled=remote_stat;
                    break;
                }
            }
        }

    }


}
