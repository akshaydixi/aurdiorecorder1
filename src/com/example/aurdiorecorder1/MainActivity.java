package com.example.aurdiorecorder1;

import java.io.DataOutputStream;
import android.util.Log;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.loopj.android.http.*;



public class MainActivity extends Activity {
        private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
        private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
        private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
        private AsyncHttpClient client;
        private static final String UPLOADURL = "ENTER UPLOAD URL HERE";
        private RequestParams REQUESTPARAMS;
        private AsyncHttpResponseHandler RESPONSEHANDLER;
        private MediaRecorder recorder = null;
        private int currentFormat = 0;
        private String FILENAME;
        private int output_formats[] = { MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.OutputFormat.THREE_GPP };
        private String file_exts[] = { AUDIO_RECORDER_FILE_EXT_MP4, AUDIO_RECORDER_FILE_EXT_3GP }; 
        private int serverResponseCode = 0;
        
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = new AsyncHttpClient();
        RESPONSEHANDLER = new AsyncHttpResponseHandler(){
        	@Override
        	public void onSuccess(String response){
        		AppLog.logString(response);
        	}
        };
        
        setButtonHandlers();
        enableButtons(false);
        setFormatButtonCaption();
    
    }
    
    

        private void setButtonHandlers() {
                ((Button)findViewById(R.id.btnStart)).setOnClickListener(btnClick);
        ((Button)findViewById(R.id.btnStop)).setOnClickListener(btnClick);
        ((Button)findViewById(R.id.btnFormat)).setOnClickListener(btnClick);
        }
        
        private void enableButton(int id,boolean isEnable){
                ((Button)findViewById(id)).setEnabled(isEnable);
        }
        
        private void enableButtons(boolean isRecording) {
                enableButton(R.id.btnStart,!isRecording);
                enableButton(R.id.btnFormat,!isRecording);
                enableButton(R.id.btnStop,isRecording);
        }
        
        private void setFormatButtonCaption(){
                ((Button)findViewById(R.id.btnFormat)).setText(getString(R.string.audio_format) + " (" + file_exts[currentFormat] + ")");
        }
        
        private String getFilename(){
                String filepath = Environment.getExternalStorageDirectory().getPath();
                File file = new File(filepath,AUDIO_RECORDER_FOLDER);
                
                if(!file.exists()){
                        file.mkdirs();
                }
                long millis = System.currentTimeMillis();
                FILENAME = (file.getAbsolutePath() + "/" + millis + file_exts[currentFormat]);
                return (file.getAbsolutePath() + "/" + millis + file_exts[currentFormat]);
                
        }
        
        private void startRecording(){
                recorder = new MediaRecorder();
                AppLog.logString("HERE1");
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(output_formats[currentFormat]);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                recorder.setOutputFile(getFilename());
                AppLog.logString("HERE2");
                recorder.setOnErrorListener(errorListener);
                recorder.setOnInfoListener(infoListener);
                
                try {
                        recorder.prepare();
                        AppLog.logString("HERE3");
                        recorder.start();
                        AppLog.logString("HERE4");
                } catch (IllegalStateException e) {
                        e.printStackTrace();
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }
        
        private void stopRecording(){
                if(null != recorder){
                        recorder.stop();
                        recorder.reset();
                        recorder.release();
                        
                        recorder = null;
                }
        }
        
        private void displayFormatDialog(){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                String formats[] = {"MPEG 4", "3GPP"};
                
                builder.setTitle(getString(R.string.choose_format_title))
                           .setSingleChoiceItems(formats, currentFormat, new DialogInterface.OnClickListener() {
                                
                                public void onClick(DialogInterface dialog, int which) {
                                        currentFormat = which;
                                        setFormatButtonCaption();
                                        
                                        dialog.dismiss();
                                }
                           })
                           .show();
        }
        
        private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
                public void onError(MediaRecorder mr, int what, int extra) {
                        AppLog.logString("Error: " + what + ", " + extra);
                }
        };
        
        private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
                public void onInfo(MediaRecorder mr, int what, int extra) {
                        AppLog.logString("Warning: " + what + ", " + extra);
                }
        };
    
    private View.OnClickListener btnClick = new View.OnClickListener() {
                public void onClick(View v) {
                        switch(v.getId()){
                                case R.id.btnStart:{
                                        AppLog.logString("Start Recording");
                                        
                                        enableButtons(true);
                                        startRecording();
                                                        
                                        break;
                                }
                                case R.id.btnStop:{
                                        AppLog.logString("Start Recording");
                                        
                                        enableButtons(false);
                                        stopRecording();
                                        uploadFile(FILENAME);
                                        break;
                                }
                                case R.id.btnFormat:{
                                        displayFormatDialog();
                                        
                                        break;
                                }
                        }
                }
        };
        
        
        public int uploadFile(String sourceFileUri) {
            
            
            String fileName = sourceFileUri;
    
            HttpURLConnection conn = null;
            DataOutputStream dos = null;  
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024; 
            File sourceFile = new File(sourceFileUri); 
             
            if (!sourceFile.isFile()) {
                 
                  
                 Log.e("uploadFile", "Source File not exist :"
                                     +FILENAME);
                  
                 runOnUiThread(new Runnable() {
                     public void run() {
                
                     }
                 }); 
                  
                 return 0;
              
            }
            else
            {
                 try { 
                      
                       // open a URL connection to the Servlet
                     FileInputStream fileInputStream = new FileInputStream(sourceFile);
                     URL url = new URL(UPLOADURL);
                      
                     // Open a HTTP  connection to  the URL
                     conn = (HttpURLConnection) url.openConnection(); 
                     conn.setDoInput(true); // Allow Inputs
                     conn.setDoOutput(true); // Allow Outputs
                     conn.setUseCaches(false); // Don't use a Cached Copy
                     conn.setRequestMethod("POST");
                     conn.setRequestProperty("Connection", "Keep-Alive");
                     conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                     conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                     conn.setRequestProperty("uploaded_file", fileName); 
                      
                     dos = new DataOutputStream(conn.getOutputStream());
            
                     dos.writeBytes(twoHyphens + boundary + lineEnd); 
                     dos.writeBytes("Content-Disposition: form-data; name='uploaded_file';filename='"+ fileName + "'" + lineEnd);
                      
                     dos.writeBytes(lineEnd);
            
                     // create a buffer of  maximum size
                     bytesAvailable = fileInputStream.available(); 
            
                     bufferSize = Math.min(bytesAvailable, maxBufferSize);
                     buffer = new byte[bufferSize];
            
                     // read file and write it into form...
                     bytesRead = fileInputStream.read(buffer, 0, bufferSize);  
                        
                     while (bytesRead > 0) {
                          
                       dos.write(buffer, 0, bufferSize);
                       bytesAvailable = fileInputStream.available();
                       bufferSize = Math.min(bytesAvailable, maxBufferSize);
                       bytesRead = fileInputStream.read(buffer, 0, bufferSize);   
                        
                      }
            
                     // send multipart form data necesssary after file data...
                     dos.writeBytes(lineEnd);
                     dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            
                     // Responses from the server (code and message)
                     serverResponseCode = conn.getResponseCode();
                     String serverResponseMessage = conn.getResponseMessage();
                       
                     Log.i("uploadFile", "HTTP Response is : "
                             + serverResponseMessage + ": " + serverResponseCode);
                      
                     if(serverResponseCode == 200){
                          
                         runOnUiThread(new Runnable() {
                              public void run() {
                                   
                                  String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
                                                
                    
                                   
                                  
                                  Toast.makeText(MainActivity.this, "File Upload Complete.", 
                                               Toast.LENGTH_SHORT).show();
                              }
                          });                
                     }    
                      
                     //close the streams //
                     fileInputStream.close();
                     dos.flush();
                     dos.close();
                       
                } catch (MalformedURLException ex) {
                     
                      
                    ex.printStackTrace();
                     
                    runOnUiThread(new Runnable() {
                        public void run() {
                        
                            Toast.makeText(MainActivity.this, "MalformedURLException", 
                                                                Toast.LENGTH_SHORT).show();
                        }
                    });
                     
                    Log.e("Upload file to server", "error: " + ex.getMessage(), ex);  
                } catch (Exception e) {
                     
                     
                    e.printStackTrace();
                     
                    runOnUiThread(new Runnable() {
                        public void run() {
                           Toast.makeText(MainActivity.this, "Got Exception : see logcat ", 
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.e("Upload file to server Exception", "Exception : "
                                                     + e.getMessage(), e);  
                }
                return serverResponseCode; 
                 
             } // End else block 
           } 
}
