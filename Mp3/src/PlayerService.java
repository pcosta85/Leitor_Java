import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;

public class PlayerService {
    private MediaPlayer mediaPlayer;

    public void carregar(File ficheiro, Runnable onReady, Runnable onEnd, java.util.function.Consumer<String> onError) {
        pararEDestruir();

        try {
            Media media = new Media(ficheiro.toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setOnReady(onReady);
            mediaPlayer.setOnEndOfMedia(onEnd);
            mediaPlayer.setOnError(() -> {
                if (media.getError() != null) {
                    onError.accept(media.getError().getMessage());
                } else {
                    onError.accept("Erro desconhecido na reprodução.");
                }
            });
        } catch (Exception e) {
            onError.accept(e.getMessage());
        }
    }

    public void play() {
        if (mediaPlayer != null) mediaPlayer.play();
    }

    public void pause() {
        if (mediaPlayer != null) mediaPlayer.pause();
    }

    public void stop() {
        if (mediaPlayer != null) mediaPlayer.stop();
    }

    public void seek(Duration duration) {
        if (mediaPlayer != null) mediaPlayer.seek(duration);
    }

    public void setVolume(double volume) {
        if (mediaPlayer != null) mediaPlayer.setVolume(volume);
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void pararEDestruir() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }
}