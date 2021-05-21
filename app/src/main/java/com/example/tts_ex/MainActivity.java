package com.example.tts_ex;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.speech.tts.TextToSpeech;

import android.os.Bundle;
import android.speech.tts.UtteranceProgressListener;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collection;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextPlayer, View.OnClickListener{
    private TextToSpeech tts;
    private Button btnPlay;
    private Button btnPause;
    private Button btnStop;
    private EditText etText;
    private TextView tvText;
    private PlayState playState = PlayState.STOP;
    private Spannable spannable;
    private final BackgroundColorSpan colorSpan = new BackgroundColorSpan(Color.YELLOW);
    private int standbyIndex = 0;
    private int lastPlayIndex = 0;
    private final Bundle params = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPlay = findViewById(R.id.bt_play);
        btnPause = findViewById(R.id.bt_pause);
        btnStop = findViewById(R.id.bt_stop);
        etText = findViewById(R.id.et_text);
        tvText = findViewById(R.id.tv_text);

        btnPlay.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnStop.setOnClickListener(this);

        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, null);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    int result = tts.setLanguage(Locale.KOREA);

                    if(result == tts.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Toast.makeText(MainActivity.this, "이 언어는 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        btnPlay.setEnabled(true);
                        tts.setPitch(0.7f);
                        tts.setSpeechRate(1.2f);
                    }
                } else{
                    Log.e("TTS", "Initialization Failed");
                }
            }
        });

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {

            }

            @Override
            public void onDone(String utteranceId) {
                clearAll();
            }

            @Override
            public void onError(String utteranceId) {
                Log.e("TTS", "재생 중 에러 발생");
            }

            @Override
            public void onRangeStart(String utteranceId, int start, int end, int frame) {
                changeHighlight(standbyIndex + start, standbyIndex + end);
                lastPlayIndex = start;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_play:
                startPlay();
                break;
            case R.id.bt_pause:
                pausePlay();
                break;
            case R.id.bt_stop:
                stopPlay();
                break;
        }
    }

    @Override
    public void startPlay() {
        String content = etText.getText().toString();

        if(playState.isStopping() && !tts.isSpeaking()){
            setContentFromEditText(content);
            startSpeak(content);
        } else if(playState.isWaiting()){
            standbyIndex += lastPlayIndex;
            startSpeak(content.substring(standbyIndex));
        }
        playState = PlayState.PLAY;
    }

    @Override
    public void pausePlay() {
        if(playState.isPlaying()){
            playState = PlayState.WAIT;
            tts.stop();
        }
    }

    @Override
    public void stopPlay() {
        tts.stop();
        clearAll();
    }

    private void changeHighlight(final int start, final int end){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spannable.setSpan(colorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        });
    }

    private void setContentFromEditText(String content){
        tvText.setText(content, TextView.BufferType.SPANNABLE);
        spannable = (SpannableString)tvText.getText();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startSpeak(final String text){
        tts.speak(text, TextToSpeech.QUEUE_ADD, params, text);
    }

    private void clearAll(){
        playState = PlayState.STOP;
        standbyIndex = 0;
        lastPlayIndex = 0;

        if(spannable != null){
            changeHighlight(0, 0); // remove highlight
        }
    }

    @Override
    protected void onPause() {
        if(playState.isPlaying()){
            pausePlay();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if(playState.isWaiting()){
            startPlay();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if(tts != null){
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}