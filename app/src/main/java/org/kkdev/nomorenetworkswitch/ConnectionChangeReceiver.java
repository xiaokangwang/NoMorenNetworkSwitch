package org.kkdev.nomorenetworkswitch;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

/**
 * Created by shelikhoo on 6/28/16.
 */
public class ConnectionChangeReceiver extends BroadcastReceiver
{

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
    @Override
    public void onReceive( Context context, Intent intent )
    {
        Intent intentx = new Intent(context,NetworkSwitcher.class);
        intentx.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startService(intentx);

        //context.bindService(new Intent(context, NetworkSwitcher.class), mConnection,
        //        Context.BIND_AUTO_CREATE);
        IBinder bd= peekService(context,intentx);
        mService = new Messenger(bd);
        mBound = true;
        Message msg = Message.obtain(null, NetworkSwitcher.MSG_network_status_alter);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

 /*       ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        //Network[] mobNetInfo = connectivityManager.getAllNetworks();
        if ( activeNetInfo != null )
        {
            //Toast.makeText(context, "Active Network Type : " + activeNetInfo.getTypeName(), Toast.LENGTH_SHORT).show();
            boolean isWiFi = activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI;
            boolean isMobile = activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE;




        }else{


        }*/
            /*if( mobNetInfo != null )
            {
                Toast.makeText( context, "Mobile Network Type : " + mobNetInfo[0].toString(), Toast.LENGTH_SHORT ).show();
            }*/
    }
}