package com.example.tts_ex;

public enum PlayState {
    PLAY("재생중"), WAIT("일시정지"), STOP("멈춤");

    private String state;

    PlayState(String state){
        this.state = state;
    }

    public String getState(){
        return state;
    }

    public boolean isStopping(){
        return this == STOP;
    }

    public boolean isWaiting(){
        return this == WAIT;
    }

    public boolean isPlaying(){
        return this == PLAY;
    }
}
