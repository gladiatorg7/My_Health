package com.example.vangelis.my_health;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.Handler;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.w3c.dom.Text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import zephyr.android.HxMBT.BTClient;
import zephyr.android.HxMBT.ZephyrProtocol;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 */
public class Connection extends android.support.v4.app.Fragment implements View.OnClickListener {
    /** Called when the activity is first created. */
    BluetoothAdapter adapter = null;
    BTClient _bt;
    ZephyrProtocol _protocol;
    NewConnectedListener _NConnListener;
    private final int HEART_RATE = 0x100;
    private final int INSTANT_SPEED = 0x101;
    private final int HEART_BEAT_TS = 0x103;
    private static Context context;
    View rootView;
    protected Activity activity;
    private static FileWriter outputStreamWriter;
    public int counter=0;
    private LineChart mChart;
    public Connection() {
        // Required empty public constructor
        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File (sdCard.getAbsolutePath() + "/dir1/dir2");
            dir.mkdirs();
            File file = new File(dir, "filename.txt");

            outputStreamWriter =  new FileWriter(file);

//            String afilpath =Environment.getExternalStorageDirectory().toString();
//            File myDir=new File(afilpath+"/E-Complain");
//            myDir.mkdirs();
//
//            File file = new File(myDir, "vaggos.txt");

//            outputStreamWriter =  new FileWriter(file);

            Log.d("FileWriter", "File writer with " + dir.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (Activity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {




        rootView =inflater.inflate(R.layout.fragment_connection, container, false);
      //  TextView tvHeart=(TextView) getActivity().findViewById(R.id.labelHeartRate);
      //  TextView tvSpeed=(TextView) getActivity().findViewById(R.id.labelInstantSpeed);

        /*Sending a message to android that we are going to initiate a pairing request*/
        IntentFilter filter = new IntentFilter("android.bluetooth.device.action.PAIRING_REQUEST");
        /*Registering a new BTBroadcast receiver from the Main Activity context with pairing request event*/
        getActivity().registerReceiver(new Connection.BTBroadcastReceiver(), filter);
        // Registering the BTBondReceiver in the application that the status of the receiver has changed to Paired
        IntentFilter filter2 = new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED");
        getActivity().registerReceiver(new Connection.BTBondReceiver(), filter2);

        //Obtaining the handle to act on the CONNECT button
        //  TextView tv = (TextView) findViewById(R.id.labelStatusMsg);
        //  String ErrorText  = "Not Connected to HxM ! !";
        //  tv.setText(ErrorText);


        //adapter = BluetoothAdapter.getDefaultAdapter();


        Button btnConnect = (Button) rootView.findViewById(R.id.connect_button);

        //if (btnConnect != null) {
           btnConnect.setOnClickListener(new OnClickListener() {
               @Override
               public void onClick(View v) {

                   String BhMacID = "00:07:80:9D:8A:E8";
                   //String BhMacID = "00:07:80:88:F6:BF";
                   adapter = BluetoothAdapter.getDefaultAdapter();
                   //if(!adapter.isEnabled()){
                   //     adapter.enable();
                   //}
                   Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

                   if (pairedDevices.size() > 0)
                   {
                       for (BluetoothDevice device : pairedDevices)
                       {
                           if (device.getName().startsWith("HXM"))
                           {
                               BluetoothDevice btDevice = device;
                               BhMacID = btDevice.getAddress();
                               break;

                           }
                       }





                   }

                   //BhMacID = btDevice.getAddress();
                   BluetoothDevice Device = adapter.getRemoteDevice(BhMacID);
                   String DeviceName = Device.getName();
                   _bt = new BTClient(adapter, BhMacID);
                   _NConnListener = new NewConnectedListener(Newhandler,Newhandler);
                   _bt.addConnectedEventListener(_NConnListener);
                   ((MainActivity)getActivity()).set_bt(_bt);
                   TextView tv1 = (TextView)getActivity().findViewById(R.id.hRate);
                   tv1.setText("000");

                   tv1 = (TextView)getActivity().findViewById(R.id.iSpeed);
                   tv1.setText("0.0");

                   //tv1 = 	(EditText)findViewById(R.id.labelSkinTemp);
                   //tv1.setText("0.0");

                   //tv1 = 	(EditText)findViewById(R.id.labelPosture);
                   //tv1.setText("000");

                   //tv1 = 	(EditText)findViewById(R.id.labelPeakAcc);
                   //tv1.setText("0.0");
                   if(_bt.IsConnected())
                   {
                       _bt.start();
                       TextView tv = (TextView) getActivity().findViewById(R.id.labelStatusMsg);
                       String ErrorText  = "Connected to HxM "+DeviceName;
                       tv.setText(ErrorText);

                       //Reset all the values to 0s

                   }
                   else
                   {
                       TextView tv = (TextView) getActivity().findViewById(R.id.labelStatusMsg);
                       String ErrorText  = "Unable to Connect !";
                       tv.setText(ErrorText);
                   }
                   getFragmentManager().popBackStack();
                  //FragmentManager manager =getSupportFragmentManager();
                  // getActivity().getFragmentManager().beginTransaction().hide(getActivity().getFragmentManager().findFragmentById(R.id.fragment_connection)).commit();

               }
           });

       // }
        /*Obtaining the handle to act on the DISCONNECT button*/
        final Button btnDisconnect = (Button) rootView.findViewById(R.id.disconnect_button);
        if (btnDisconnect != null) {
            btnDisconnect.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
					/*Reset the global variables*/

                    //if(adapter.isEnabled()){
                    //    adapter.disable();
                    //}

                    TextView tv = (TextView) getActivity().findViewById(R.id.labelStatusMsg);
                    String ErrorText  = "Disconnected from HxM!";
                    tv.setText(ErrorText);

                    if(((MainActivity)getActivity()).get_bt()!=null){
                        _bt=((MainActivity)getActivity()).get_bt();
                        /*This disconnects listener from acting on received messages*/
                        _bt.removeConnectedEventListener(_NConnListener);
                        /*Close the communication with the device & throw an exception if failure*/
                        _bt.Close();
                    }
                    getFragmentManager().popBackStack();
                }
            });
        }

        return rootView;
    }


    @Override
    public void onClick(View v) {

    }


    private class BTBondReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();
            BluetoothDevice device = adapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
            Log.d("Bond state", "BOND_STATED = " + device.getBondState());
        }
    }
    private class BTBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BTIntent", intent.getAction());
            Bundle b = intent.getExtras();
            Log.d("BTIntent", b.get("android.bluetooth.device.extra.DEVICE").toString());
            Log.d("BTIntent", b.get("android.bluetooth.device.extra.PAIRING_VARIANT").toString());
            try {
                BluetoothDevice device = adapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
                Method m = BluetoothDevice.class.getMethod("convertPinToBytes", new Class[] {String.class} );
                byte[] pin = (byte[])m.invoke(device, "1234");
                m = device.getClass().getMethod("setPin", new Class [] {pin.getClass()});
                Object result = m.invoke(device, pin);
                Log.d("BTTest", result.toString());
            } catch (SecurityException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (NoSuchMethodException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    final  Handler Newhandler = new Handler (){
        public void handleMessage(Message msg)
        {

            try {
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File (sdCard.getAbsolutePath() + "/dir1/dir2");
                dir.mkdirs();
                File file = new File(dir, "filename.txt");

                outputStreamWriter =  new FileWriter(file, true);
                Log.d("FileWriter", "File writer with " + dir.getAbsolutePath());

            } catch (IOException e) {
                e.printStackTrace();
            }
            TextView tv;
            switch (msg.what)
            {
                case HEART_RATE:

                    String HeartRatetext = msg.getData().getString("HeartRate");

                    counter++;
                    try {
                        outputStreamWriter.write(counter+ ":" + HeartRatetext);
                        Log.d("FileWriter", "Grafw");

                    }
                    catch (IOException e) {
                        Log.e("Exception", "File write failed: " + e.toString());
                    }
                    try {
                        outputStreamWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    tv = (TextView)activity.findViewById(R.id.hRate);

                    System.out.println("Heart Rate Info is "+ HeartRatetext);
                    if (tv != null)tv.setText(HeartRatetext);
                    break;

                case INSTANT_SPEED:
                    String InstantSpeedtext = msg.getData().getString("InstantSpeed");
                    tv = (TextView)activity.findViewById(R.id.iSpeed);
                    if (tv != null)tv.setText(InstantSpeedtext);

                    break;

                case HEART_BEAT_TS:
                    String HeartTS = msg.getData().getString("HeartBeatTS");
                    Log.d("FileWriter", "File writer with " +HeartTS);
                    try {
                        outputStreamWriter.write(HeartTS+"-");
                        Log.d("FileWriter", "Grafw");

                    }
                    catch (IOException e) {
                        Log.e("Exception", "File write failed: " + e.toString());
                    }
                    try {
                        outputStreamWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


            }
        }



    };

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

    }
}
