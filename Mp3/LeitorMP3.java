import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LeitorMP3 extends Application {

    private MediaPlayer mediaPlayer;
    private final List<File> playlist = new ArrayList<>();
    private int indiceAtual = -1;

    private Label lblTitulo;
    private Label lblTempoAtual;
    private Label lblTempoTotal;
    private Label lblEstado;

    private Slider sliderProgresso;
    private Slider sliderVolume;

    private ListView<String> listViewPlaylist;

    private Button btnPlay;
    private Button btnPause;
    private Button btnStop;
    private Button btnAnterior;
    private Button btnSeguinte;
    private Button btnAdicionar;

    private boolean arrastandoProgresso = false;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Leitor MP3");
        stage.getIcons().add(new Image(new File("icon.png").toURI().toString()));

        lblTitulo = new Label("Nenhuma música selecionada");
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        lblEstado = new Label("Pronto");
        lblEstado.setStyle("-fx-font-size: 12px; -fx-text-fill: #bbbbbb;");

        lblTempoAtual = new Label("00:00");
        lblTempoAtual.setStyle("-fx-text-fill: white;");

        lblTempoTotal = new Label("00:00");
        lblTempoTotal.setStyle("-fx-text-fill: white;");

        sliderProgresso = new Slider();
        sliderProgresso.setMin(0);
        sliderProgresso.setValue(0);
        sliderProgresso.setMaxWidth(Double.MAX_VALUE);

        sliderVolume = new Slider(0, 100, 50);
        sliderVolume.setPrefWidth(180);

        listViewPlaylist = new ListView<>();
        listViewPlaylist.setPrefHeight(220);
        listViewPlaylist.setStyle(
                "-fx-control-inner-background: #1e1e1e;" +
                "-fx-font-size: 13px;" +
                "-fx-text-fill: white;"
        );

        btnAdicionar = criarBotao("Adicionar");
        btnAnterior = criarBotao("⏮");
        btnPlay = criarBotao("▶");
        btnPause = criarBotao("⏸");
        btnStop = criarBotao("⏹");
        btnSeguinte = criarBotao("⏭");

        btnAdicionar.setOnAction(e -> adicionarMusicas(stage));

        btnPlay.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.play();
                lblEstado.setText("A reproduzir");
            }
        });

        btnPause.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
                lblEstado.setText("Em pausa");
            }
        });

        btnStop.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                lblEstado.setText("Parado");
            }
        });

        btnAnterior.setOnAction(e -> tocarAnterior());
        btnSeguinte.setOnAction(e -> tocarSeguinte());

        listViewPlaylist.setOnMouseClicked(e -> {
            int indiceSelecionado = listViewPlaylist.getSelectionModel().getSelectedIndex();
            if (indiceSelecionado >= 0) {
                indiceAtual = indiceSelecionado;
                carregarMusica(indiceAtual, true);
            }
        });

        sliderVolume.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue() / 100.0);
            }
        });

        sliderProgresso.setOnMousePressed(e -> arrastandoProgresso = true);

        sliderProgresso.setOnMouseReleased(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(Duration.seconds(sliderProgresso.getValue()));
            }
            arrastandoProgresso = false;
        });

        VBox topo = new VBox(6, lblTitulo, lblEstado);
        topo.setPadding(new Insets(10));
        topo.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 10;");

        HBox barraTempo = new HBox(10, lblTempoAtual, sliderProgresso, lblTempoTotal);
        barraTempo.setAlignment(Pos.CENTER);
        HBox.setHgrow(sliderProgresso, Priority.ALWAYS);

        HBox barraVolume = new HBox(10, new Label("Volume"), sliderVolume);
        barraVolume.setAlignment(Pos.CENTER_LEFT);
        ((Label) barraVolume.getChildren().get(0)).setStyle("-fx-text-fill: white;");

        HBox controles = new HBox(12, btnAnterior, btnPlay, btnPause, btnStop, btnSeguinte);
        controles.setAlignment(Pos.CENTER);

        VBox centro = new VBox(12,
                btnAdicionar,
                listViewPlaylist,
                barraTempo,
                barraVolume,
                controles
        );
        centro.setPadding(new Insets(15));
        centro.setStyle("-fx-background-color: #252525; -fx-background-radius: 10;");

        VBox root = new VBox(15, topo, centro);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #121212;");

        Scene scene = new Scene(root, 720, 500);
        stage.setScene(scene);
        stage.show();
    }

    private Button criarBotao(String texto) {
        Button btn = new Button(texto);
        btn.setPrefWidth(90);
        btn.setPrefHeight(36);
        btn.setStyle(
                "-fx-background-color: #3a3a3a;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
        );
        return btn;
    }

    private void adicionarMusicas(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar músicas MP3");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Ficheiros MP3", "*.mp3")
        );

        List<File> ficheiros = fileChooser.showOpenMultipleDialog(stage);

        if (ficheiros != null && !ficheiros.isEmpty()) {
            for (File ficheiro : ficheiros) {
                playlist.add(ficheiro);
                listViewPlaylist.getItems().add(ficheiro.getName());
            }

            if (indiceAtual == -1) {
                indiceAtual = 0;
                listViewPlaylist.getSelectionModel().select(indiceAtual);
                carregarMusica(indiceAtual, false);
            }

            lblEstado.setText("Músicas adicionadas à playlist");
        }
    }

    private void carregarMusica(int indice, boolean tocarAutomaticamente) {
        if (indice < 0 || indice >= playlist.size()) {
            return;
        }

        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }

            File ficheiro = playlist.get(indice);
            Media media = new Media(ficheiro.toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            lblTitulo.setText("♪ " + ficheiro.getName());
            lblEstado.setText("Música carregada");

            mediaPlayer.setOnReady(() -> {
                Duration duracaoTotal = media.getDuration();
                sliderProgresso.setMax(duracaoTotal.toSeconds());
                lblTempoTotal.setText(formatarTempo(duracaoTotal));
                lblTempoAtual.setText("00:00");

                mediaPlayer.setVolume(sliderVolume.getValue() / 100.0);

                if (tocarAutomaticamente) {
                    mediaPlayer.play();
                    lblEstado.setText("A reproduzir");
                }
            });

            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!arrastandoProgresso) {
                    sliderProgresso.setValue(newTime.toSeconds());
                }
                lblTempoAtual.setText(formatarTempo(newTime));
            });

            mediaPlayer.setOnEndOfMedia(this::tocarSeguinte);
            listViewPlaylist.getSelectionModel().select(indiceAtual);

        } catch (Exception ex) {
            mostrarErro("Erro ao carregar a música: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void tocarSeguinte() {
        if (playlist.isEmpty()) {
            return;
        }

        indiceAtual++;
        if (indiceAtual >= playlist.size()) {
            indiceAtual = 0;
        }

        carregarMusica(indiceAtual, true);
    }

    private void tocarAnterior() {
        if (playlist.isEmpty()) {
            return;
        }

        indiceAtual--;
        if (indiceAtual < 0) {
            indiceAtual = playlist.size() - 1;
        }

        carregarMusica(indiceAtual, true);
    }

    private String formatarTempo(Duration duration) {
        if (duration == null || duration.isUnknown()) {
            return "00:00";
        }

        int totalSegundos = (int) Math.floor(duration.toSeconds());
        int minutos = totalSegundos / 60;
        int segundos = totalSegundos % 60;

        return String.format("%02d:%02d", minutos, segundos);
    }

    private void mostrarErro(String mensagem) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Ocorreu um problema");
            alert.setContentText(mensagem);
            alert.showAndWait();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}