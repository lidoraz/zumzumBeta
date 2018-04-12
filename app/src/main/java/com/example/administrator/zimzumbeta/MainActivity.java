package com.example.administrator.zimzumbeta;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.mobile.client.AWSMobileClient;

import org.w3c.dom.Text;

public class MainActivity extends Activity {


    private Handler mhandler;
    private static final String ZUMZUM_RECORD ="zumzumRecord" ;
    Button b1;
    private int TOAST = 0;
    private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
    private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private MediaRecorder recorder = null;
    private int currentFormat = 0;
    private int output_formats[] = { MediaRecorder.OutputFormat.MPEG_4,             MediaRecorder.OutputFormat.THREE_GPP };
    private String file_exts[] = { AUDIO_RECORDER_FILE_EXT_MP4, AUDIO_RECORDER_FILE_EXT_3GP };

    /** Called when the activity is first created. */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);



        mhandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
//                if(msg.arg1==TOAST){
//                    String time = msg.getData().getString("time");
//                    Toast.makeText(MainActivity.this,time,Toast.LENGTH_SHORT).show();
//                    AppLog.logString("handleMessage: toast - " +time);
//                }
                Bundle b = msg.getData();

                Button button = findViewById(b.getInt("id"));
                button.setText("Record\n\n\n"+b.getString("time"));

            }
        };

        b1 = (Button) findViewById(R.id.record);
        final long[] startTime = {0};

        b1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        AppLog.logString("Start Recording");
                        startRecording();
                        b1.setBackgroundColor(Color.RED);
                        startTime[0] = System.currentTimeMillis();

                        //start a view which updates every second;
                        showRecordTimer(startTime, startTime[0]);

                        break;
                    case MotionEvent.ACTION_UP:
                        AppLog.logString("stop Recording");
                        stopRecording();
                        startTime[0] = 0;
                        //mhandler.sendMessage((new Message()).setData((new Bundle()).putString("id",R.id.record);)
                        b1.setText("Record");
                        b1.setBackgroundColor(Color.WHITE);
                        break;
                }
                return false;
            }
        });

        Button play = findViewById(R.id.play);


        play.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                MediaPlayer mp = new MediaPlayer();
                try {
                    File inputFile = new File(getFilename());
                    FileInputStream is = new FileInputStream(inputFile);
                    AppLog.logString("Opened file: is exists:" + inputFile.exists());

                    mp.setDataSource(is.getFD());
                    mp.prepare();
                    mp.start();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    private void showRecordTimer(long[] timer,long startTime) {
        new Thread(()->{
            while(timer[0]!=0){
                Message msg = new Message();
                Bundle b = new Bundle();
                b.putString("time",Long.toString((System.currentTimeMillis()-startTime)/1000));
                b.putInt("id",R.id.record);
                msg.setData(b);
                mhandler.sendMessage(msg);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }

    private String getFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + ZUMZUM_RECORD + file_exts[currentFormat]);
    }

    private void startRecording(){
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(output_formats[currentFormat]);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(getFilename());
        recorder.setOnErrorListener(errorListener);
        recorder.setOnInfoListener(infoListener);

        try {
            recorder.prepare();
            recorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            AppLog.logString("Error: " + what + ", " + extra);
        }
    };

    private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            AppLog.logString("Warning: " + what + ", " + extra);
        }
    };

    private void stopRecording(){
        if(null != recorder){
            recorder.stop();
            recorder.reset();
            recorder.release();
            AWSMobileClient.getInstance().initialize(this).execute();
            uploadWithTransferUtility();
            recorder = null;
        }
    }
    private void uploadWithTransferUtility(){
        TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") String uuid = tManager.getDeviceId();
        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
                        .build();
        TransferObserver uploadObserver =
//                transferUtility.upload(
//                        "s3Folder/s3Key.txt",
//                        new File("/path/to/file/localFile.txt"));
                transferUtility.upload("zumzum-beta",uuid+"_"+Long.toString(System.currentTimeMillis()),new File(getFilename()));

        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d("YourActivity", "ID:" + id + " bytesCurrent: " + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
            }

        });
        // If you prefer to poll for the data, instead of attaching a
        // listener, check for the state and progress in the observer.
        if (TransferState.COMPLETED == uploadObserver.getState()) {
            // Handle a completed upload.
        }
        Log.d("uploadSerivce",uploadObserver.getState().toString());
        while(uploadObserver.getState() ==TransferState.WAITING){
            Log.d("upload service","in progress ........");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        while(uploadObserver.getState() ==TransferState.IN_PROGRESS){
            Log.d("upload service","in progress ........");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        Log.d("YourActivity", "Bytes Transferrred: " + uploadObserver.getBytesTransferred());
        Log.d("YourActivity", "Bytes Total: " + uploadObserver.getBytesTotal());
    }
}