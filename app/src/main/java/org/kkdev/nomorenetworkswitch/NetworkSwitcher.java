package org.kkdev.nomorenetworkswitch;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

public class NetworkSwitcher extends Service {
    int ONGOING_NOTIFICATION_ID = 1;
    public NetworkSwitcher() {
    }

    /** Command to the service to display a message */
    static final int MSG_SAY_HELLO = 1;

    static final int MSG_Start_Switch = 2;

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SAY_HELLO:
                    Log.i("org.kkdev","hello!");
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    private int show_noti(){
        Notification status_noti = new Notification.Builder(this)
                .setContentTitle(getText(R.string.app_noti_title))
                .setContentText("Initializing....")
                .setSmallIcon(R.drawable.noti_running)
                .build();
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(ONGOING_NOTIFICATION_ID, status_noti);
        startForeground(ONGOING_NOTIFICATION_ID, status_noti);
        return 0;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean should_run =true;
        if(should_run){
            show_noti();
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
