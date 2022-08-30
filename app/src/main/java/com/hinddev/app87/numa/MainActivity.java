package com.hinddev.app87.numa;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.hinddev.app87.numa.util.AIPermissionRequest;
import com.nuwarobotics.service.IClientId;
import com.nuwarobotics.service.agent.NuwaRobotAPI;
import com.nuwarobotics.service.agent.RobotEventListener;
import com.nuwarobotics.service.agent.VoiceEventListener;
import com.nuwarobotics.service.facecontrol.IonCompleteListener;
import com.nuwarobotics.service.facecontrol.UnityFaceCallback;
import com.nuwarobotics.service.facecontrol.utils.ServiceConnectListener;

import java.util.ArrayList;


import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    NuwaRobotAPI mRobotAPI;
    IClientId mClientId;

    boolean mSDKinit = false;

    Button mStartDemoBtn;


    private boolean mTts_complete = true;
    private boolean mTts_finish = false;

    Handler mHandler = new Handler();
    private int mCmdStep = 0 ;
    Context mContext ;
    private AIPermissionRequest mPermissionRequest;

    ArrayList<String> cmdTTS = new ArrayList<String>() {{
        add("");
        add("Let’s play Emotion Imitation Game!");
        add("You will see pictures of different facial expressions. Copy the emotion you see from the picture.");
        add("Let’s begin!");

    }};
    VoiceEventListener.SpeakState mSpeakState = VoiceEventListener.SpeakState.NONE;
    private ImageView imageViewHappyFace ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext= this;
        //Step 1 : Initial Nuwa API Object
        mClientId = new IClientId(this.getPackageName());
        mRobotAPI = new NuwaRobotAPI(this, mClientId);
        Log.d(TAG, "onCreate: Tesssssssssst");

        //Grant permission
        mPermissionRequest = new AIPermissionRequest();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestMulti();
            Log.d(TAG, "request all needed　permissions");
        }




        mStartDemoBtn = (Button) findViewById(R.id.mStartDemoBtn);
        imageViewHappyFace = findViewById(R.id.imageHappyFace);
//        findViewById(R.id.button5).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(MainActivity.this , TeacherBoardActivity.class));
//            }
//        });
        mStartDemoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick to start start demo") ;
                //Step 3 : reset command step and trigger action start thread
                mCmdStep = 0 ;
                mHandler.post(robotAction);//play next action
            }
        });


        //Step 2 : Register receive Robot Event
        Log.d(TAG, "register EventListener ");
        mRobotAPI.registerRobotEventListener(robotEventListener);//listen callback of robot service event


    }
    ServiceConnectListener FaceControlConnect = new ServiceConnectListener() {
        @Override
        public void onConnectChanged(ComponentName componentName, boolean b) {
            //isBindFace = b;
            Log.d(TAG, "faceService onbind : " + b);
            //Step 4 : register face callback
            mRobotAPI.UnityFaceManager().registerCallback(mUnityFaceCallback);
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // release Nuwa Robot SDK resource
        mRobotAPI.release();
        mRobotAPI.stopSensor(NuwaRobotAPI.SENSOR_NONE);

    }

    RobotEventListener robotEventListener = new RobotEventListener() {
        @Override
        public void onWikiServiceStart() {
            // Nuwa Robot SDK is ready now, you call call Nuwa SDK API now.
            Log.d(TAG, "onWikiServiceStart, robot ready to be control");

            mRobotAPI.registerVoiceEventListener(voiceEventListener);//listen callback of robot voice related event
            //Allow user start demo after service ready
            mRobotAPI.initFaceControl(mContext, mContext.getClass().getName(), FaceControlConnect);
            mRobotAPI.requestSensor(NuwaRobotAPI.SENSOR_DROP);
            mSDKinit = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Allow user click button.
                    mStartDemoBtn.setEnabled(true);//when service ready, we start allow user start API function call
                }
            });
            if (mTts_finish) {


                imageViewHappyFace.setVisibility(View.VISIBLE);
                mStartDemoBtn.setVisibility(View.GONE);
              Handler handler =  new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(mContext , TeacherBoardActivity.class);
                        startActivityForResult(intent , 100);
                    }
                }, 5000);



            }

        }

        @Override
        public void onWikiServiceStop() {

        }

        @Override
        public void onWikiServiceCrash() {

        }

        @Override
        public void onWikiServiceRecovery() {

        }

        @Override
        public void onStartOfMotionPlay(String s) {

        }

        @Override
        public void onPauseOfMotionPlay(String s) {

        }

        @Override
        public void onStopOfMotionPlay(String s) {

        }

        @Override
        public void onCompleteOfMotionPlay(String s) {

        }

        @Override
        public void onPlayBackOfMotionPlay(String s) {

        }

        @Override
        public void onErrorOfMotionPlay(int i) {

        }

        @Override
        public void onPrepareMotion(boolean b, String s, float v) {

        }

        @Override
        public void onCameraOfMotionPlay(String s) {

        }

        @Override
        public void onGetCameraPose(float v, float v1, float v2, float v3, float v4, float v5, float v6, float v7, float v8, float v9, float v10, float v11) {

        }

        @Override
        public void onTouchEvent(int i, int i1) {

        }

        @Override
        public void onPIREvent(int i) {

        }

        @Override
        public void onTap(int i) {

        }

        @Override
        public void onLongPress(int i) {

        }

        @Override
        public void onWindowSurfaceReady() {

        }

        @Override
        public void onWindowSurfaceDestroy() {

        }

        @Override
        public void onTouchEyes(int i, int i1) {

        }

        @Override
        public void onRawTouch(int i, int i1, int i2) {

        }

        @Override
        public void onFaceSpeaker(float v) {

        }

        @Override
        public void onActionEvent(int i, int i1) {

        }

        @Override
        public void onDropSensorEvent(int i) {

            if (i==1){
                Toast.makeText(mContext, "Drop in Robot ", Toast.LENGTH_SHORT).show();

            }
            int val = mRobotAPI.getDropSensorOfNumber();


        }

        @Override
        public void onMotorErrorEvent(int i, int i1) {

        }
    };

    Runnable robotAction = new Runnable() {
        @Override
        public void run() {
            String current_tts = cmdTTS.get(mCmdStep);

            Log.d(TAG,"Action Step "+mCmdStep+" TTS:"+current_tts);
            //Config waiting flag first.   (Example : use to wait two callback ready)
            if(current_tts != "") mTts_complete = false;


            //Start play tts and motion if need
            if(current_tts != "") mRobotAPI.startTTS(current_tts , Locale.ENGLISH.toString());
            mRobotAPI.UnityFaceManager().showUnity();




            while(mTts_complete == false ){
                //wait both action complete
            }

            mCmdStep ++ ;//next action step
            if(mCmdStep < cmdTTS.size()) {
                mHandler.post(robotAction);//play next action
                mTts_finish = false;
            }else{
                mRobotAPI.motionReset();//Reset Robot pose to default
                mTts_finish = true;
                mRobotAPI.UnityFaceManager().hideFace();

            }
        }
    };



    VoiceEventListener voiceEventListener = new VoiceEventListener() {
        @Override
        public void onWakeup(boolean isError, String score, float direction) {

        }

        @Override
        public void onTTSComplete(boolean isError) {
            Log.d(TAG, "onTTSComplete:" + !isError);
            mTts_complete = true;
//            setText("onTTSComplete, " + !isError, false);

//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mStartBtn.setEnabled(true);
//                    mStopBtn.setEnabled(false);
//                }
//            });
        }

        @Override
        public void onSpeechRecognizeComplete(boolean isError, ResultType iFlyResult, String json) {

        }

        @Override
        public void onSpeech2TextComplete(boolean isError, String json) {

        }

        @Override
        public void onMixUnderstandComplete(boolean isError, ResultType resultType, String s) {

        }

        @Override
        public void onSpeechState(ListenType listenType, SpeechState speechState) {

        }

        @Override
        public void onSpeakState(SpeakType speakType, SpeakState speakState) {
            Log.d(TAG, "onSpeakState:" + speakType + ", state:" + speakState);
        }

        @Override
        public void onGrammarState(boolean isError, String s) {

        }

        @Override
        public void onListenVolumeChanged(ListenType listenType, int i) {

        }

        @Override
        public void onHotwordChange(HotwordState hotwordState, HotwordType hotwordType, String s) {

        }
    };


    public void requestMulti() {
        mPermissionRequest.requestMultiPermissions(this, mPermissionGrant);
    }

    private AIPermissionRequest.PermissionGrant mPermissionGrant = new AIPermissionRequest.PermissionGrant() {
        @Override
        public void onPermissionGranted(int requestCode) {
            switch (requestCode) {
                case AIPermissionRequest.CODE_READ_PHONE_STATE:
                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_READ_PHONE_STATE", Toast.LENGTH_SHORT).show();
                    break;
                case AIPermissionRequest.CODE_RECORD_AUDIO:
                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_RECORD_AUDIO", Toast.LENGTH_SHORT).show();
                    break;
                case AIPermissionRequest.CODE_READ_EXTERNAL_STORAGE:
                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_READ_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show();
                    break;
                case AIPermissionRequest.CODE_WRITE_EXTERNAL_STORAGE:
                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_WRITE_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
    UnityFaceCallback mUnityFaceCallback = new UnityFaceCallback(){
        @Override
        public void on_touch_left_eye() {
            Log.d("FaceControl", "on_touch_left_eye()");
        }

        @Override
        public void on_touch_right_eye() {
            Log.d("FaceControl", "on_touch_right_eye()");
        }

        @Override
        public void on_touch_nose() {
            Log.d("FaceControl", "on_touch_nose()");
        }

        @Override
        public void on_touch_mouth() {
            Log.d("FaceControl", "on_touch_mouth()");
        }

        @Override
        public void on_touch_head() {
            Log.d("FaceControl", "on_touch_head()");
        }

        @Override
        public void on_touch_left_edge() {
            Log.d("FaceControl", "on_touch_left_edge()");
        }

        @Override
        public void on_touch_right_edge() {
            Log.d("FaceControl", "on_touch_right_edge()");
        }

        @Override
        public void on_touch_bottom() {
            Log.d("FaceControl", "on_touch_bottom()");
        }

    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode==100){
            switch (resultCode){
                case RESULT_OK:
                    if (mSDKinit){
                        mRobotAPI.startTTS("Game Finish" , Locale.ENGLISH.toString());
                    }
                    break;

                case RESULT_CANCELED:
                    restartActivity();
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }
    public void restartActivity(){
        Intent mIntent = getIntent();
        finish();
        startActivity(mIntent);
    }
}
