package org.kkdev.nomorenetworkswitch;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.StrictMode;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class NetworkSwitcher extends Service {
    boolean Switch_Stat=false;
    int ONGOING_NOTIFICATION_ID = 1;
    public NetworkSwitcher() {
    }
    Messenger messager;
    /** Command to the service to display a message */
    static final int MSG_SAY_HELLO = 1;

    static final int MSG_Set_Switch_On = 2;
    static final int MSG_Set_Switch_Off = 3;
    static final int MSG_Remote_Ind_Set = 4;
    static final int MSG_Get_Status_Report = 5;
    static final int MSG_Remote_Status_Report = 6;
    static final int MSG_network_status_alter = 7;
    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg)  {
            switch (msg.what) {
                case MSG_SAY_HELLO:
                    Log.i("org.kkdev","hello!");
                    break;
                case MSG_Set_Switch_On:
                    try{
                        Message resp = Message.obtain(null, MSG_Remote_Ind_Set);
                        Bundle bResp = new Bundle();
                        bResp.putString("WritingData", "Switching on is progressing");
                        resp.setData(bResp);
                        resign_noti();
                        show_noti("Started~ Making the initial probe...");
                        Switch_Stat=true;
                        messager=msg.replyTo;
                        msg.replyTo.send(resp);
                        probe_network();

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case MSG_Set_Switch_Off:
                    try{
                        Message resp = Message.obtain(null, MSG_Remote_Ind_Set);
                        Bundle bResp = new Bundle();
                        bResp.putString("WritingData", "Switching off is progressing");
                        resp.setData(bResp);
                        //Thread.sleep(0);
                        //Thread.sleep(0);
                        resign_noti();
                        Switch_Stat=false;
                        messager=msg.replyTo;
                        msg.replyTo.send(resp);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case MSG_Get_Status_Report:
                    Message resp = Message.obtain(null, MSG_Remote_Status_Report);
                    Bundle bResp = new Bundle();
                    bResp.putBoolean("Switch", Switch_Stat);
                    try {
                        msg.replyTo.send(resp);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    remoteWrite("Switcher Operating normally.");
                    messager=msg.replyTo;
                    break;
                case MSG_network_status_alter:
                    probe_network();
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
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        super.onCreate();
    }


    private int show_noti(String ctxtxt){
        Notification status_noti = new Notification.Builder(this)
                .setContentTitle(getText(R.string.app_noti_title))
                .setContentText(ctxtxt)
                .setSmallIcon(R.drawable.noti_running)
                .build();
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(ONGOING_NOTIFICATION_ID, status_noti);
        startForeground(ONGOING_NOTIFICATION_ID, status_noti);
        return 0;
    }
    private int resign_noti(){
        stopForeground(true);
        return 0;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*boolean should_run =true;
        if(should_run){
            show_noti("Initializing");
        }*/
        return super.onStartCommand(intent, flags, startId);
    }

    private boolean login(){
        SharedPreferences settings = getSharedPreferences("org.kkdev.nomorenetworkswitch_preferences", MODE_MULTI_PROCESS);
        String cueb_username = settings.getString("cueb_username", "");
        String cueb_password = settings.getString("cueb_password","");
        boolean login_enabled = settings.getBoolean("use_my_cred", true);
        if(cueb_username.isEmpty()||cueb_password.isEmpty()||!login_enabled){
            resign_noti();
            show_noti("Wifi: no login credential are configured. Login Aborted.");
            return false;
        }



        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://219.224.70.81/cgi-bin/do_login");
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
        //n=100is_pad=1type=1uname=32015070144pass=343drop=0x=113y=35
        nameValuePair.add(new BasicNameValuePair("n", "100"));
        nameValuePair.add(new BasicNameValuePair("is_pad", "1"));
        nameValuePair.add(new BasicNameValuePair("type", "1"));
        nameValuePair.add(new BasicNameValuePair("uname", cueb_username));
        nameValuePair.add(new BasicNameValuePair("pass", cueb_password));
        nameValuePair.add(new BasicNameValuePair("x", "113"));
        nameValuePair.add(new BasicNameValuePair("y", "35"));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));

        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        try {
            HttpResponse response = httpClient.execute(httpPost);
            // write response to log
            //Log.d("Http Post Response:", response.toString());
            //String resp=response.getStatusLine().getReasonPhrase();
            String resp = EntityUtils.toString(response.getEntity());
            if(resp.length()!=0){
                //resign_noti();
                //show_noti("Wifi, login resp:" + resp + "%" + resp.length());
                remoteWrite("Wifi, login resp:"+resp+"%"+resp.length());
                switch (resp.length()){
                    case 6:
                        //Incorrect user name
                        show_noti("Wifi, login failed: Incorrect username");
                        break;
                    case 5:
                        //Incorrect password:
                        show_noti("Wifi, login failed: Incorrect password");
                        break;
                    case 89:
                        //OK:
                        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        show_noti("Wifi, signed in: "+wifiInfo.getSSID());
                        break;
                    default:
                        show_noti("Wifi, login failed: Unexpected response.");
                }
            }
        } catch (ClientProtocolException e) {
            // Log exception
            e.printStackTrace();
        } catch (IOException e) {
            // Log exception
            e.printStackTrace();
        }
        return false;
    }
    private void probe_network(){
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        //Network[] mobNetInfo = connectivityManager.getAllNetworks();
        if ( activeNetInfo != null )
        {
            //Toast.makeText(context, "Active Network Type : " + activeNetInfo.getTypeName(), Toast.LENGTH_SHORT).show();
            boolean isWiFi = activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI;
            boolean isMobile = activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE;

            Log.d("Probe", "Probing...");
            resign_noti();
            if(isWiFi){
                show_noti("Wifi, additional probe required.");
                Log.d("Probe", "WiFi?");
                if(is_network_CUEB()){
                    resign_noti();
                    show_noti("Wifi, CUEB_WLAN, logining pending.");
                    login();
                }else{
                    resign_noti();
                    show_noti("Wifi, not recognized.");
                }
            }else if(isMobile){
                show_noti("Mobile connection detected, idle.");
            }


        }else{
            resign_noti();
            show_noti("No network, nothing to do.");

        }
    }

    private void remoteWrite(String ctx){
        if (messager==null){return;}
        try{
            Message resp = Message.obtain(null, MSG_Remote_Ind_Set);
            Bundle bResp = new Bundle();
            bResp.putString("WritingData", ctx);
            resp.setData(bResp);
            messager.send(resp);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private boolean is_network_CUEB(){
        Log.d("Probe", "WiFi? is it CUEB_WLAN?");
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        Log.d("wifiInfo", wifiInfo.toString());
        Log.d("SSID", wifiInfo.getSSID());
        //remoteWrite("Wifi:"+"\n"+wifiInfo.getSSID()+"\n"+wifiInfo.getBSSID());
        SharedPreferences settings = getSharedPreferences("org.kkdev.nomorenetworkswitch_preferences",MODE_MULTI_PROCESS);
        String adds = settings.getString("addit_wlan_ssid","");
        Log.d("Probe", "WiFi? is ssid =  CUEB_WLAN or "+adds);
        Log.d("Addit_wlan_ssid", adds);
        Log.i("Org.kkdev", adds);
        remoteWrite("Wifi:" + "\n" + wifiInfo.getSSID() + "\n" + wifiInfo.getBSSID()+"\n"+adds);
        if(wifiInfo.getSSID().equals(adds)||wifiInfo.getSSID().equals("\"CUEB_WLAN\"")){

            return true;
        }else{
            return false;
        }

    }


}
