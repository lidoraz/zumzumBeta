//package com.example.administrator.zimzumbeta;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.logging.Logger;
//
//import android.app.Activity;
//import android.media.MediaPlayer;
//import android.media.MediaRecorder;
//import android.os.Bundle;
//import android.os.Environment;
//import android.view.MotionEvent;
//import android.view.View;
//import android.widget.Button;
//
//public class AudioOnTouchActivity extends Activity {
//
//    private static final String ZUMZUM_RECORD ="zumzumRecord" ;
//    Button b1;
//    private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
//    private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
//    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
//    private MediaRecorder recorder = null;
//    private int currentFormat = 0;
//    private int output_formats[] = { MediaRecorder.OutputFormat.MPEG_4,             MediaRecorder.OutputFormat.THREE_GPP };
//    private String file_exts[] = { AUDIO_RECORDER_FILE_EXT_MP4, AUDIO_RECORDER_FILE_EXT_3GP };
//    /** Called when the activity is first created. */
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        b1=(Button)findViewById(R.id.record);
//        b1.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                // TODO Auto-generated method stub
//                switch(event.getAction()){
//                    case MotionEvent.ACTION_DOWN:
//                        AppLog.logString("Start Recording");
//                        startRecording();
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        AppLog.logString("stop Recording");
//                        stopRecording();
//                        break;
//                }
//                return false;
//            }
//        });
//
//        Button play = findViewById(R.id.play);
//        play.setOnClickListener(new View.OnClickListener(){
//
//            @Override
//            public void onClick(View view) {
//                MediaPlayer mp = new MediaPlayer();
//                try {
//                    FileInputStream is = new FileInputStream(new File(getFilename()));
//                    mp.setDataSource(is.getFD());
//                    mp.start();
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        });
//    }
//
//    private String getFilename(){
//        String filepath = Environment.getExternalStorageDirectory().getPath();
//        File file = new File(filepath,AUDIO_RECORDER_FOLDER);
//
//        if(!file.exists()){
//            file.mkdirs();
//        }
//
//        return (file.getAbsolutePath() + "/" + ZUMZUM_RECORD + file_exts[currentFormat]);
//    }
//
//    private void startRecording(){
//        recorder = new MediaRecorder();
//        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        recorder.setOutputFormat(output_formats[currentFormat]);
//        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//        recorder.setOutputFile(getFilename());
//        recorder.setOnErrorListener(errorListener);
//        recorder.setOnInfoListener(infoListener);
//
//        try {
//            recorder.prepare();
//            recorder.start();
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
//        @Override
//        public void onError(MediaRecorder mr, int what, int extra) {
//            AppLog.logString("Error: " + what + ", " + extra);
//        }
//    };
//
//    private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
//        @Override
//        public void onInfo(MediaRecorder mr, int what, int extra) {
//            AppLog.logString("Warning: " + what + ", " + extra);
//        }
//    };
//
//    private void stopRecording(){
//        if(null != recorder){
//            recorder.stop();
//            recorder.reset();
//            recorder.release();
//
//            recorder = null;
//        }
//    }
//}