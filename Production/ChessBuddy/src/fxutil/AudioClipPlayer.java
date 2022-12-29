package fxutil;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class AudioClipPlayer {

    private final MediaPlayer mediaPlayer;
    private final AudioClip audioClip;
    private final int period; //in seconds
    private Timeline soundLoop;

    public AudioClipPlayer(String path) {
        period = 15;
        mediaPlayer = new MediaPlayer(new Media(path));
        audioClip = new AudioClip(path);
    }

    public AudioClipPlayer(String path, int periodIn) {
        period = periodIn;
        mediaPlayer = new MediaPlayer(new Media(path));
        audioClip = new AudioClip(path);
    }

    public void startLoop() {
        mediaPlayer.setMute(true);
        mediaPlayer.play();
        mediaPlayer.setOnEndOfMedia(mediaPlayer::stop);
        soundLoop = new Timeline();
        soundLoop.getKeyFrames().add(new KeyFrame(Duration.seconds(period), e -> mediaPlayer.play()));
        soundLoop.setCycleCount(Timeline.INDEFINITE);
        soundLoop.play();
    }

    public void stopLoop() {
        soundLoop.stop();
    }

    public void play() {
        audioClip.play();
    }

}
