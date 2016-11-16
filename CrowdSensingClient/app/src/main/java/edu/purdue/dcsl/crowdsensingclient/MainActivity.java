package edu.purdue.dcsl.crowdsensingclient;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    public static String Jmsg = "edu.purdue.dcsl.crowdsensingclient.JSON_RESULT";
    private static String task_json;
    private Intent controlLoggerIntent;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File f = new File(getActivity().getFilesDir(), "controlLog.dat");
        if(!f.exists())
        {
            System.out.println("control log file created");
            new File(getActivity().getFilesDir(),"controlLog.dat"); // getActivity().getFilesDir(),
        }
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput("controlLog.dat", Context.MODE_PRIVATE);
            String string = "This is control log file";
            outputStream.write(string.getBytes());
            outputStream.close();
        }
        catch (Exception e)
        {
            System.out.println("creating control log file failed.");
        }

        // setup control info logging for every half hour
        controlLoggerIntent = new Intent(getActivity(), ControlLogger.class);
        alarmIntent = PendingIntent.getBroadcast(getBaseContext(), 0, controlLoggerIntent, 0);
        alarmMgr = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                AlarmManager.INTERVAL_HALF_HOUR,
                AlarmManager.INTERVAL_HALF_HOUR, alarmIntent);
    }
    private boolean processTask(String tskJson)
    {
        System.out.println("Task JSON received: ");
        System.out.println(tskJson);

        // call the relevant sensors here
        return true;
    }

    public JSONObject getSensorJson(String sensorName)
            // sensorNames: Baro, Accel, Gyro
    {
        //TODO: Logic error here. return reading values or control values?
        Reading rd = new Reading();
        SensorReader sr = new SensorReader(MainActivity.this);
        rd.setRname(sensorName);

        /*
        ListcontrolReadings = sr.getCtl(sensorName);
        float[] controlReadings = new float[ListcontrolReadings.size()];
        int j = 0;
        for (Float f : ListcontrolReadings) {
            controlReadings[j++] = (f != null ? f : Float.NaN);
        }*/
        if (sensorName == "Gyro") rd.setAxisReading(sr.getGyro());
        else if (sensorName == "Baro") rd.setAxisReading(sr.getBaro());
        else if (sensorName == "Accel") rd.setAxisReading(sr.getAccl());
        else if (sensorName == "GPS") rd.setAxisReading(sr.getGPS());
        //TODO: what is R1?

        //rd.setR1(controlReadings[0]);
        for(int i = 0; i < 10; i++)
            rd.addMetaData("Access: " + i + "pm");
        return JsonUtil.toJson(rd);
    }
    public JSONObject getControlJson()
    {

        try
        {
            File f = new File(getActivity().getFilesDir(), "controlLog.dat");
            FileInputStream is = new FileInputStream(f);
            byte[] bytes = new byte[1024];
            int n = is.read(bytes);
            is.close();

            String controlInfo = new String(bytes, 0, n);
            String[] controlEntry = controlInfo.split("\n");
            if(controlEntry[0] != null)
            {
                Reading r = new Reading();
                //TODO: what is setR1 used for?
                r.setR1(Double.valueOf(controlEntry[0]) );
                r.setRname("battery");
                return JsonUtil.toJson(r);
            }
            return null;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage() );
            System.out.println("Reading from log file failed");
            return null;
        }
    }
    public void SyncServer(View view)
    {
        try
        {
            JSONObject jl = getSensorJson("Gyro");
            JSONObject ja = getSensorJson("Accel");
            TextView tv = (TextView)findViewById(R.id.statusBox);
            tv.setText("Sending Readings & Control Info...");
            tv.append("\n");
            JSONArray sArr = new JSONArray();
            sArr.put(jl);
            sArr.put(ja);
            JSONObject jc_b = getControlJson();
            JSONArray cArr = new JSONArray();
            cArr.put(jc_b);

            JSONObject finalRes = new JSONObject();
            finalRes.put("Sensors", sArr );
            finalRes.put("Control", cArr);

            tv.append(finalRes.toString());

            serverExchange("35.160.36.179", 21567, finalRes.toString() );
        /*Intent intent = new Intent(MainActivity.this, DisplayMessageActivity.class);
        intent.putExtra(Jmsg, jr);
        startActivity(intent);*/
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
            System.out.println("Sync Server exception");
        }

    }
    private void serverExchange(final String host, final int port, final String data) {
        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    Socket socket = new Socket(host, port);

                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write((data).getBytes());
                    outputStream.flush();

                    InputStream is = socket.getInputStream();
                    byte[] bytes = new byte[1024];
                    int n = is.read(bytes);
                    task_json = new String(bytes, 0, n);
                    processTask(task_json);

                    is.close();
                    socket.close();
                }
                catch (Exception e)
                {
                    System.out.println("Socket creation failed");
                }
            }
        };
        thread.start();
    }
    private Activity getActivity()
    {
        return MainActivity.this;
    }
}
