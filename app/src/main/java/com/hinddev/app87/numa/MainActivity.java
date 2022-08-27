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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext= this;
        //Step 1 : Initial Nuwa API Object
        mClientId = new IClientId(this.getPackageName());
        mRobotAPI = new NuwaRobotAPI(this, mClientId);

        //Grant permission
        mPermissionRequest = new AIPermissionRequest();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestMulti();
            Log.d(TAG, "request all needed　permissions");
        }




        mStartDemoBtn = (Button) findViewById(R.id.mStartDemoBtn);
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

                String imageUri ="data:image/png;base64,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";

                mRobotAPI.UnityFaceManager().showMedia("image", imageUri, "Look at the face. Copy the happy face.", "2000", new IonCompleteListener() {
                    @Override
                    public void onComplete(String s) throws RemoteException {

                        Intent intent = new Intent(mContext , TeacherBoardActivity.class);
                        startActivityForResult(intent , 100);

                    }

                    @Override
                    public IBinder asBinder() {
                        return null;
                    }
                });
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
