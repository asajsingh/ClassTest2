package com.example.ajay.classtest2;
import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;import android.widget.Button;
import android.widget.Toast;
import android.os.Environment;
import java.io.IOException;
import java.util.Random;
import android.widget.TextView;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;



public class MainActivity extends AppCompatActivity {

    private static final String TAG="MainActivity";
    Button buttonStart, buttonStop, buttonPlayLastRecord,buttonStopPlay;
    Thread runner;
    private static double mEMA=0.0;
    private static final double EMA_FILTER=0.6;
    public static final int RequestPermissionCode=1;
    TextView textView;
    String AudioSavePath=null;
    MediaRecorder mediaRecorder;
    Random random ;

    String RandomAudioFileName = "ABCDEFGHIJKLMNOP";
    MediaPlayer mediaPlayer;

    public double soundDb(double ampl)
    {
        return 20*Math.log10(getAmplitudeEMA()/ampl);
    }
    public double getAmplitude()
    {
        if(mediaRecorder!=null)
        {
            return mediaRecorder.getMaxAmplitude();
        }
        else
            return 0;
    }

    public double getAmplitudeEMA()
    {
        double amp=getAmplitude();
        mEMA=EMA_FILTER*amp+(1.0-EMA_FILTER)*mEMA;
        return (int)mEMA;
    }

   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonStart=(Button) findViewById(R.id.button1);
        buttonStop=(Button) findViewById(R.id.button2);
        buttonPlayLastRecord=(Button) findViewById(R.id.button3);
        buttonStopPlay=(Button) findViewById(R.id.button4);

        buttonStop.setEnabled(false);
        buttonPlayLastRecord.setEnabled(false);
        buttonStopPlay.setEnabled(false);
        textView= (TextView)findViewById(R.id.textView);


       if(runner==null)
       {
           runner=new Thread()
           {
               public void run()
               {
                   while(runner!=null)
                   {
                       try
                       {
                           Thread.sleep(1000);
                           Log.i(TAG,"run: Tock");
                       }
                       catch(InterruptedException e)
                       {
                           e.printStackTrace();
                       }
                       runOnUiThread(new Runnable(){
                           @Override
                           public void run() {
                               textView.setText(Double.toString(soundDb(1))+"db");
                           }
                       });
                   }

               }
           };
           runner.start();
           Log.d(TAG,"onCreate: runner started");
       }
        random = new Random();

        buttonStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    if(CheckPermission())
                    {
                        AudioSavePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
                                CreateRandomAudioFileName(5) + "AudioRecording.3gp";;;

                        MediaRecorderReady();
                        try {
                            mediaRecorder.prepare();
                            mediaRecorder.start();
                        } catch (IllegalStateException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        buttonStart.setEnabled(false);
                        buttonStop.setEnabled(true);

                        Toast.makeText(MainActivity.this, "Recording started",
                                Toast.LENGTH_LONG).show();
                    } else {
                        requestPermission();
                    }

                }




            }

        );

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaRecorder.stop();
                buttonStop.setEnabled(false);
                buttonPlayLastRecord.setEnabled(true);
                buttonStart.setEnabled(true);
                buttonStopPlay.setEnabled(false);

                Toast.makeText(MainActivity.this, "Recording Completed",
                        Toast.LENGTH_LONG).show();
            }
        });

        buttonPlayLastRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) throws IllegalArgumentException,
                    SecurityException, IllegalStateException {

                buttonStop.setEnabled(false);
                buttonStart.setEnabled(false);
                buttonStopPlay.setEnabled(true);

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(AudioSavePath);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();
                Toast.makeText(MainActivity.this, "Recording Playing",
                        Toast.LENGTH_LONG).show();
            }
        });

        buttonStopPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonStop.setEnabled(false);
                buttonStart.setEnabled(true);
                buttonStopPlay.setEnabled(false);
                buttonPlayLastRecord.setEnabled(true);

                if(mediaPlayer != null){
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    MediaRecorderReady();
                }
            }
        });


    }
    public void MediaRecorderReady(){
        mediaRecorder=new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(AudioSavePath);
    }
    public String CreateRandomAudioFileName(int string){
        StringBuilder stringBuilder = new StringBuilder( string );
        int i = 0 ;
        while(i < string ) {
            stringBuilder.append(RandomAudioFileName.
                    charAt(random.nextInt(RandomAudioFileName.length())));

            i++ ;
        }
        return stringBuilder.toString();
    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @ NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length> 0) {
                    boolean StoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(MainActivity.this, "Permission Granted",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }
    public boolean CheckPermission()
    {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }
}
