package com.example.vangelis.my_health;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import zephyr.android.HxMBT.BTClient;
import zephyr.android.HxMBT.ZephyrProtocol;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RelativeLayout chartLayout;
    private LineChart mChart;
    int hrate;
    TextView tv;
    ToggleButton toggleButton ;
    private static FileWriter outputStreamWriter;

    /** Called when the activity is first created. */
    BluetoothAdapter adapter = null;

    ZephyrProtocol _protocol;
    NewConnectedListener _NConnListener;
    private final int HEART_RATE = 0x100;
    private final int INSTANT_SPEED = 0x101;
    BTClient _bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        toggleButton= (ToggleButton)findViewById(R.id.toggleButton);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            TextView labelmsg=(TextView) findViewById(R.id.labelStatusMsg);
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    if(labelmsg.getText().equals("") || (labelmsg.getText().equals("Disconnected from HxM!"))){
                        toggleButton.setChecked(false);
                        Toast.makeText(getApplicationContext(),"Please connect to Zephyr BT HxM",Toast.LENGTH_LONG).show();
                    }
                    else{
                        File sdCard = Environment.getExternalStorageDirectory();
                        File dir = new File (sdCard.getAbsolutePath() + "/dir1/dir2");
                        dir.mkdirs();
                        String filename =Calendar.getInstance().getTime().toString()+".txt";
                        EditText filenameT=(EditText) findViewById(R.id.filename);
                        filenameT.setText(filename);
                        File file = new File(dir, filename);

                        try {
                            outputStreamWriter =  new FileWriter(file);

                            Log.d("FileWriter", "File writer with " + dir.getAbsolutePath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(getApplicationContext(),"The Logging started",Toast.LENGTH_LONG).show();
                    }

                } else {
                    // The toggle is disabled
                    if(labelmsg.getText().equals("")){
                        try {
                            outputStreamWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(getApplicationContext(),"The Logging stoped",Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        chartLayout= (RelativeLayout) findViewById(R.id.chartLayout);
        //create line chart
        mChart=new LineChart(this);
        //add to chartLayout
        chartLayout.addView(mChart,1200,1700);


        //customize line chart
        mChart.setDescription("");
        mChart.setNoDataText("No Data for the moment");

        //enable value highlight
        mChart.setHighlightPerTapEnabled(true);
        //enable touch gestures
        mChart.setTouchEnabled(true);

        //we want also enable scalling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(true);

        // enable pinch zoom to avoid  scaling x and y axis separately
        mChart.setPinchZoom(true);

        //alternative background color
        mChart.setBackgroundColor(Color.LTGRAY);

        //work on data
        LineData data =new LineData();
        data.setValueTextColor(Color.WHITE);

        //add data to line chart
        mChart.setData(data);

        //get legend obgect
        Legend l = mChart.getLegend();

        //customize legend
        l.setForm(Legend.LegendForm.CIRCLE);
        l.setTextColor(Color.WHITE);

        XAxis xl =mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);

        YAxis yl =mChart.getAxisLeft();
        yl.setTextColor(Color.WHITE);
        yl.setAxisMaxValue(200f);
        yl.setAxisMinValue(-1f);
        yl.setDrawGridLines(true);

        YAxis yl2 =mChart.getAxisRight();
        yl2.setEnabled(false);


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }

    //=============================================================
    @Override
    protected void onResume(){
        super.onResume();
        // now we're going to simulate real time data addition

        new Thread(new Runnable() {
            @Override
            public void run() {
                //add 100 entries
                while(true){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv = (TextView) findViewById(R.id.hRate);
                            hrate=Integer.parseInt((String)tv.getText());
                            addEntry(hrate);//chart is notified of update in addEntry method
                        }
                    });
                    //pause between adds
                    try {
                        Thread.sleep(10);          
                    } catch (InterruptedException e) {
                        //manage error ...
                    }
                }
            }
        }).start();

    }

    //create method to entry values in line chart
    public void addEntry(int hrate){
        LineData data=mChart.getData();


        if(data!=null){
            LineDataSet set = (LineDataSet) data.getDataSetByIndex(0);

            if(set ==null){
                //creation if null
                set =createSet();
                data.addDataSet(set);
            }

            //add a new random value
            data.addXValue("");
            data.addEntry(new Entry((float)hrate,set.getEntryCount()),0);

            //notify chart data have changed
            mChart.notifyDataSetChanged();
            //limit number of visible entries
            mChart.setVisibleXRange(0,6);

            //scroll to the last entry
            mChart.moveViewToX(data.getXValCount()-7);

        }
    }

    //method to create set
    private LineDataSet createSet(){

        LineDataSet set =new LineDataSet(null,"SPL Db");
        set.setDrawCubic(true);
        //set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(2f);
        set.setCircleSize(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244,117,177));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextColor(10);

        return set;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.connection) {
            Connection connectionFragment =new Connection();
            FragmentManager manager =getSupportFragmentManager();

        //    manager.beginTransaction().show(connectionFragment).addToBackStack(null).commit();
            manager.beginTransaction().replace(R.id.content_main,connectionFragment,connectionFragment.getTag()).addToBackStack(null).commit();


        } else if (id == R.id.my_history) {
            History historyFragment =new History();
            FragmentManager manager =getSupportFragmentManager();

            manager.beginTransaction().replace(R.id.content_main,historyFragment,historyFragment.getTag()).addToBackStack(null).commit();

        } else if (id == R.id.send_data) {
            Send sendFragment =new Send();
            FragmentManager manager =getSupportFragmentManager();

            manager.beginTransaction().replace(R.id.content_main,sendFragment,sendFragment.getTag()).addToBackStack(null).commit();
        } else if (id == R.id.contact_us) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public BTClient get_bt(){return this._bt;}
    public void set_bt(BTClient _bt){this._bt=_bt;}

    public LineChart getChart(){return mChart;}

    }
