import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LeitorMP3 extends Application {

    private MediaPlayer mediaPlayer;

    // Agora a playlist usa apenas a estrutura pedida no projeto
    private final PlaylistDupla playlist = new PlaylistDupla();

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
    private Button btnRemover;
    private Button btnShuffle;
    private Button btnRepeat;
    private Button btnClearPlaylist;

    private boolean arrastandoProgresso = false;
    private boolean shuffleMode = false;
    private RepeatMode repeatMode = RepeatMode.NONE;

    private static final String PLAYLIST_FILE = "playlist.txt";
    private static final String CONFIG_FILE = "config/config.txt";

    private int currentUserId = -1;
    private String currentUsername = null;

    private enum RepeatMode {
        NONE("Repeat: OFF"),
        ONE("Repeat: ONE"),
        ALL("Repeat: ALL");

        private final String label;

        RepeatMode(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    @Override
    public void start(Stage stage) {
        try {
            inicializarUI(stage);
            carregarConfiguracao();
            stage.setOnCloseRequest(e -> {
                salvarConfiguracao();
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.dispose();
                }
            });
        } catch (Exception ex) {
            mostrarErro("Erro ao inicializar aplicação: " + ex.getMessage());
        }
    }

    private void inicializarUI(Stage stage) {
        stage.setTitle("Leitor MP3");
        carregarIcon(stage);

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
                "-fx-text-fill: white;");

        btnAdicionar = criarBotao("Adicionar");
        btnRemover = criarBotao("Remover");
        btnAnterior = criarBotao("⏮");
        btnPlay = criarBotao("▶");
        btnPause = criarBotao("⏸");
        btnStop = criarBotao("⏹");
        btnSeguinte = criarBotao("⏭");
        btnShuffle = criarBotao("🔀");
        btnRepeat = criarBotao("🔁");
        btnClearPlaylist = criarBotao("Limpar");

        configurarBotoes();
        configurarSliders();
        configurarPlaylist();

        MenuBar menuBar = createMenuBar(stage);

        VBox topo = new VBox(6, lblTitulo, lblEstado);
        topo.setPadding(new Insets(10));
        topo.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 10;");

        HBox barraTempo = new HBox(10, lblTempoAtual, sliderProgresso, lblTempoTotal);
        barraTempo.setAlignment(Pos.CENTER);
        HBox.setHgrow(sliderProgresso, Priority.ALWAYS);

        Label lblVolume = new Label("Volume");
        lblVolume.setStyle("-fx-text-fill: white;");
        HBox barraVolume = new HBox(10, lblVolume, sliderVolume);
        barraVolume.setAlignment(Pos.CENTER_LEFT);

        HBox botoesGestao = new HBox(8, btnAdicionar, btnRemover, btnClearPlaylist);
        botoesGestao.setAlignment(Pos.CENTER);

        HBox controles = new HBox(8, btnAnterior, btnPlay, btnPause, btnStop, btnSeguinte, btnShuffle, btnRepeat);
        controles.setAlignment(Pos.CENTER);

        VBox centro = new VBox(12,
                botoesGestao,
                listViewPlaylist,
                barraTempo,
                barraVolume,
                controles);
        centro.setPadding(new Insets(15));
        centro.setStyle("-fx-background-color: #252525; -fx-background-radius: 10;");

        VBox conteudo = new VBox(15, topo, centro);
        conteudo.setPadding(new Insets(15));

        VBox root = new VBox(menuBar, conteudo);
        root.setStyle("-fx-background-color: #121212;");

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 750, 600);
        stage.setScene(scene);
        stage.show();
    }

    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: #1a1a1a;");

        Menu fileMenu = new Menu("File");

        MenuItem loginItem = new MenuItem("Login");
        loginItem.setOnAction(e -> showLoginDialog());

        MenuItem registerItem = new MenuItem("Register");
        registerItem.setOnAction(e -> showRegisterDialog());

        MenuItem savePlaylistItem = new MenuItem("Save Playlist to Cloud");
        savePlaylistItem.setOnAction(e -> savePlaylistToCloud());

        MenuItem loadPlaylistItem = new MenuItem("Load Playlist from Cloud");
        loadPlaylistItem.setOnAction(e -> loadPlaylistFromCloud());

        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setOnAction(e -> logout());

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> stage.close());

        fileMenu.getItems().addAll(
                loginItem,
                registerItem,
                new SeparatorMenuItem(),
                savePlaylistItem,
                loadPlaylistItem,
                new SeparatorMenuItem(),
                logoutItem,
                new SeparatorMenuItem(),
                exitItem
        );

        menuBar.getMenus().add(fileMenu);
        return menuBar;
    }

    private void showLoginDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Login");
        dialog.setHeaderText("Login to your account");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle("-fx-control-inner-background: #3a3a3a; -fx-text-fill: white;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-control-inner-background: #3a3a3a; -fx-text-fill: white;");

        Label u = new Label("Username:");
        Label p = new Label("Password:");
        u.setStyle("-fx-text-fill: white;");
        p.setStyle("-fx-text-fill: white;");

        grid.add(u, 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(p, 0, 1);
        grid.add(passwordField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setStyle("-fx-background-color: #2a2a2a;");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String username = usernameField.getText();
                String password = passwordField.getText();

                if (username == null || username.isBlank() || password == null || password.isBlank()) {
                    mostrarErro("Username and password cannot be empty");
                    return null;
                }

                if (DatabaseUtil.authenticateUser(username, password)) {
                    currentUsername = username;
                    currentUserId = DatabaseUtil.getUserId(username);
                    atualizarEstado("Logged in as: " + username);
                    mostrarAviso("Login successful!");
                    return username;
                } else {
                    mostrarErro("Invalid credentials");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showRegisterDialog() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Register");
        dialog.setHeaderText("Create a new account");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle("-fx-control-inner-background: #3a3a3a; -fx-text-fill: white;");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setStyle("-fx-control-inner-background: #3a3a3a; -fx-text-fill: white;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-control-inner-background: #3a3a3a; -fx-text-fill: white;");

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm Password");
        confirmField.setStyle("-fx-control-inner-background: #3a3a3a; -fx-text-fill: white;");

        Label u = new Label("Username:");
        Label e = new Label("Email:");
        Label p = new Label("Password:");
        Label c = new Label("Confirm:");
        u.setStyle("-fx-text-fill: white;");
        e.setStyle("-fx-text-fill: white;");
        p.setStyle("-fx-text-fill: white;");
        c.setStyle("-fx-text-fill: white;");

        grid.add(u, 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(e, 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(p, 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(c, 0, 3);
        grid.add(confirmField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setStyle("-fx-background-color: #2a2a2a;");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String username = usernameField.getText();
                String email = emailField.getText();
                String password = passwordField.getText();
                String confirm = confirmField.getText();

                if (username == null || username.isBlank()
                        || email == null || email.isBlank()
                        || password == null || password.isBlank()) {
                    mostrarErro("All fields are required");
                    return false;
                }

                if (!password.equals(confirm)) {
                    mostrarErro("Passwords do not match");
                    return false;
                }

                if (password.length() < 6) {
                    mostrarErro("Password must be at least 6 characters");
                    return false;
                }

                if (DatabaseUtil.registerUser(username, email, password)) {
                    mostrarAviso("Registration successful! You can now login.");
                    return true;
                } else {
                    mostrarErro("Registration failed. Username or email may already exist.");
                }
            }
            return false;
        });

        dialog.showAndWait();
    }

    private void savePlaylistToCloud() {
        if (currentUserId == -1) {
            mostrarErro("Please login first to save playlists to the cloud");
            return;
        }

        if (playlist.estaVazia()) {
            mostrarAviso("A playlist está vazia");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Save Playlist");
        dialog.setHeaderText("Enter playlist name");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField playlistNameField = new TextField();
        playlistNameField.setPromptText("Playlist name");

        grid.add(new Label("Playlist Name:"), 0, 0);
        grid.add(playlistNameField, 1, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String playlistName = playlistNameField.getText();

                if (playlistName == null || playlistName.isBlank()) {
                    mostrarErro("Playlist name cannot be empty");
                    return null;
                }

                List<String> songs = new ArrayList<>();
                for (File file : playlist.paraLista()) {
                    songs.add(file.getAbsolutePath());
                }

                if (DatabaseUtil.savePlaylist(currentUserId, playlistName, songs)) {
                    mostrarAviso("Playlist '" + playlistName + "' saved successfully!");
                    atualizarEstado("Playlist saved to cloud");
                    return playlistName;
                } else {
                    mostrarErro("Failed to save playlist");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void loadPlaylistFromCloud() {
        if (currentUserId == -1) {
            mostrarErro("Please login first to load playlists from the cloud");
            return;
        }

        List<String> playlistNames = DatabaseUtil.getUserPlaylists(currentUserId);
        if (playlistNames.isEmpty()) {
            mostrarAviso("No playlists found in cloud");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Load Playlist");
        dialog.setHeaderText("Select a playlist to load");

        ComboBox<String> playlistCombo = new ComboBox<>();
        playlistCombo.getItems().addAll(playlistNames);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        grid.add(new Label("Select Playlist:"), 0, 0);
        grid.add(playlistCombo, 1, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String selectedPlaylist = playlistCombo.getValue();
                if (selectedPlaylist == null) {
                    mostrarErro("Please select a playlist");
                    return null;
                }

                List<String> songs = DatabaseUtil.loadPlaylist(currentUserId, selectedPlaylist);

                executarComSeguranca(() -> {
                    pararReproducaoAtual();
                    playlist.limpar();

                    for (String songPath : songs) {
                        File file = new File(songPath);
                        if (file.exists()) {
                            playlist.adicionar(file);
                        }
                    }

                    atualizarListView();

                    if (!playlist.estaVazia()) {
                        playlist.definirAtualPorIndice(0);
                        listViewPlaylist.getSelectionModel().select(0);
                        carregarMusicaAtual(false);
                    } else {
                        lblTitulo.setText("Nenhuma música selecionada");
                        lblTempoAtual.setText("00:00");
                        lblTempoTotal.setText("00:00");
                    }

                    atualizarEstado("Playlist '" + selectedPlaylist + "' loaded from cloud");
                    mostrarAviso("Playlist loaded successfully!");
                    salvarPlaylist();
                });

                return selectedPlaylist;
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void logout() {
        if (currentUserId == -1) {
            mostrarAviso("You are not logged in");
            return;
        }

        currentUserId = -1;
        currentUsername = null;
        atualizarEstado("Logged out");
        mostrarAviso("You have been logged out");
    }

    private void carregarIcon(Stage stage) {
        try {
            File iconFile = new File("resources/icon.png");
            if (!iconFile.exists()) {
                iconFile = new File("resources/icon.png");
            }

            if (iconFile.exists()) {
                Image icon = new Image(iconFile.toURI().toString());
                if (!icon.isError()) {
                    stage.getIcons().add(icon);
                }
            } else {
                System.out.println("Aviso: Ficheiro de ícone não encontrado em: " + iconFile.getAbsolutePath());
            }
        } catch (Exception ex) {
            System.out.println("Aviso: Erro ao carregar ícone: " + ex.getMessage());
        }
    }

    private void configurarBotoes() {
        btnAdicionar.setOnAction(e -> adicionarMusicas());
        btnRemover.setOnAction(e -> removerMusicaSelecionada());
        btnClearPlaylist.setOnAction(e -> limparPlaylist());

        btnPlay.setOnAction(e -> executarComSeguranca(() -> {
            if (mediaPlayer != null) {
                mediaPlayer.play();
                atualizarEstado("A reproduzir");
            } else if (!playlist.estaVazia()) {
                if (playlist.getAtual() == null) {
                    playlist.definirAtualPorIndice(0);
                }
                carregarMusicaAtual(true);
            }
        }));

        btnPause.setOnAction(e -> executarComSeguranca(() -> {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
                atualizarEstado("Em pausa");
            }
        }));

        btnStop.setOnAction(e -> executarComSeguranca(() -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                atualizarEstado("Parado");
            }
        }));

        btnAnterior.setOnAction(e -> tocarAnterior());
        btnSeguinte.setOnAction(e -> tocarSeguinte());

        btnShuffle.setOnAction(e -> ativarShuffle());
        btnRepeat.setOnAction(e -> alternarRepeat());
    }

    private void configurarSliders() {
        sliderVolume.valueProperty().addListener((obs, oldVal, newVal) -> executarComSeguranca(() -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue() / 100.0);
            }
        }));

        sliderProgresso.setOnMousePressed(e -> arrastandoProgresso = true);

        sliderProgresso.setOnMouseReleased(e -> executarComSeguranca(() -> {
            arrastandoProgresso = false;
            if (mediaPlayer != null && mediaPlayer.getStatus() != MediaPlayer.Status.UNKNOWN) {
                Duration duracao = mediaPlayer.getMedia().getDuration();
                if (duracao != null && !duracao.isUnknown()) {
                    double tempoSeek = sliderProgresso.getValue();
                    if (tempoSeek <= duracao.toSeconds()) {
                        mediaPlayer.seek(Duration.seconds(tempoSeek));
                    }
                }
            }
        }));
    }

    private void configurarPlaylist() {
        listViewPlaylist.setOnMouseClicked(e -> {
            int indiceSelecionado = listViewPlaylist.getSelectionModel().getSelectedIndex();
            if (indiceSelecionado >= 0) {
                playlist.definirAtualPorIndice(indiceSelecionado);
                carregarMusicaAtual(true);
            }
        });
    }

    private void adicionarMusicas() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar músicas MP3");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Ficheiros MP3", "*.mp3"),
                new FileChooser.ExtensionFilter("Ficheiros de Áudio", "*.wav", "*.flac", "*.aac"),
                new FileChooser.ExtensionFilter("Todos os ficheiros", "*.*"));

        Stage primaryStage = (Stage) btnAdicionar.getScene().getWindow();
        List<File> ficheiros = fileChooser.showOpenMultipleDialog(primaryStage);

        if (ficheiros != null && !ficheiros.isEmpty()) {
            executarComSeguranca(() -> {
                boolean primeiraAdicao = playlist.estaVazia();

                for (File ficheiro : ficheiros) {
                    if (ficheiro.exists()) {
                        playlist.adicionar(ficheiro);
                    }
                }

                atualizarListView();

                if (primeiraAdicao && !playlist.estaVazia()) {
                    playlist.definirAtualPorIndice(0);
                    listViewPlaylist.getSelectionModel().select(0);
                    carregarMusicaAtual(false);
                }

                atualizarEstado("Músicas adicionadas à playlist (" + playlist.tamanho() + " total)");
                salvarPlaylist();
            });
        }
    }

    private void removerMusicaSelecionada() {
        int indice = listViewPlaylist.getSelectionModel().getSelectedIndex();
        if (indice < 0) {
            mostrarAviso("Selecione uma música para remover");
            return;
        }

        executarComSeguranca(() -> {
            int indiceAtualAntes = playlist.getIndiceAtual();
            boolean removendoAtual = indice == indiceAtualAntes;

            boolean removido = playlist.removerPorIndice(indice);
            if (!removido) {
                mostrarErro("Não foi possível remover a música selecionada");
                return;
            }

            atualizarListView();

            if (playlist.estaVazia()) {
                pararReproducaoAtual();
                lblTitulo.setText("Nenhuma música selecionada");
                lblTempoAtual.setText("00:00");
                lblTempoTotal.setText("00:00");
                sliderProgresso.setValue(0);
            } else {
                int novoIndice = playlist.getIndiceAtual();
                if (novoIndice >= 0) {
                    listViewPlaylist.getSelectionModel().select(novoIndice);
                }

                if (removendoAtual) {
                    carregarMusicaAtual(false);
                }
            }

            atualizarEstado("Música removida");
            salvarPlaylist();
        });
    }

    private void limparPlaylist() {
        if (playlist.estaVazia()) {
            mostrarAviso("A playlist está vazia");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar");
        confirmacao.setHeaderText("Limpar Playlist");
        confirmacao.setContentText("Tem a certeza que deseja limpar toda a playlist?");

        if (confirmacao.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            executarComSeguranca(() -> {
                pararReproducaoAtual();
                playlist.limpar();
                listViewPlaylist.getItems().clear();
                lblTitulo.setText("Nenhuma música selecionada");
                lblTempoAtual.setText("00:00");
                lblTempoTotal.setText("00:00");
                sliderProgresso.setValue(0);
                atualizarEstado("Playlist limpa");
                salvarPlaylist();
            });
        }
    }

    private void ativarShuffle() {
        shuffleMode = !shuffleMode;
        if (shuffleMode) {
            btnShuffle.setStyle(
                    "-fx-background-color: #4a9eff;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 8;");
            atualizarEstado("Shuffle: ATIVADO");
        } else {
            btnShuffle.setStyle(estiloBotaoPadrao());
            atualizarEstado("Shuffle: DESATIVADO");
        }
    }

    private void alternarRepeat() {
        RepeatMode[] modes = RepeatMode.values();
        int index = repeatMode.ordinal();
        repeatMode = modes[(index + 1) % modes.length];

        if (repeatMode == RepeatMode.NONE) {
            btnRepeat.setStyle(estiloBotaoPadrao());
        } else {
            btnRepeat.setStyle(
                    "-fx-background-color: #4a9eff;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 8;");
        }

        atualizarEstado(repeatMode.getLabel());
    }

    private void atualizarListView() {
        listViewPlaylist.getItems().clear();
        for (File f : playlist.paraLista()) {
            listViewPlaylist.getItems().add(f.getName());
        }
    }

    private void carregarMusicaAtual(boolean tocarAutomaticamente) {
        File ficheiro = playlist.getAtual();
        if (ficheiro == null) {
            return;
        }

        executarComSeguranca(() -> {
            try {
                pararReproducaoAtual();

                if (!ficheiro.exists()) {
                    mostrarErro("Ficheiro não encontrado: " + ficheiro.getName());
                    int indice = playlist.getIndiceAtual();
                    if (indice >= 0) {
                        playlist.removerPorIndice(indice);
                        atualizarListView();
                    }
                    return;
                }

                Media media = new Media(ficheiro.toURI().toString());
                mediaPlayer = new MediaPlayer(media);

                lblTitulo.setText("♪ " + ficheiro.getName());
                atualizarEstado("Carregando...");

                mediaPlayer.setOnReady(() -> executarComSeguranca(() -> {
                    Duration duracaoTotal = media.getDuration();
                    if (duracaoTotal != null && !duracaoTotal.isUnknown()) {
                        sliderProgresso.setMax(duracaoTotal.toSeconds());
                        lblTempoTotal.setText(formatarTempo(duracaoTotal));
                    } else {
                        sliderProgresso.setMax(1);
                        lblTempoTotal.setText("--:--");
                    }

                    lblTempoAtual.setText("00:00");
                    sliderProgresso.setValue(0);
                    mediaPlayer.setVolume(sliderVolume.getValue() / 100.0);

                    int indiceAtual = playlist.getIndiceAtual();
                    if (indiceAtual >= 0) {
                        listViewPlaylist.getSelectionModel().select(indiceAtual);
                    }

                    if (tocarAutomaticamente) {
                        mediaPlayer.play();
                        atualizarEstado("A reproduzir");
                    } else {
                        atualizarEstado("Música carregada");
                    }
                }));

                mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) ->
                        executarComSeguranca(() -> {
                            if (!arrastandoProgresso) {
                                sliderProgresso.setValue(newTime.toSeconds());
                            }
                            lblTempoAtual.setText(formatarTempo(newTime));
                        })
                );

                mediaPlayer.setOnEndOfMedia(this::aoTerminarMusica);

                mediaPlayer.setOnError(() -> {
                    String erro = media.getError() != null ? media.getError().getMessage() : "Erro desconhecido";
                    mostrarErro("Erro ao reproduzir: " + erro);
                });

            } catch (Exception ex) {
                mostrarErro("Erro ao carregar a música: " + ex.getMessage());
            }
        });
    }

    private void aoTerminarMusica() {
        if (repeatMode == RepeatMode.ONE) {
            carregarMusicaAtual(true);
            return;
        }
        tocarSeguinte();
    }

    private void tocarSeguinte() {
        if (playlist.estaVazia()) {
            return;
        }

        if (shuffleMode) {
            int indiceAleatorio = (int) (Math.random() * playlist.tamanho());
            playlist.definirAtualPorIndice(indiceAleatorio);
            carregarMusicaAtual(true);
            return;
        }

        File atualAntes = playlist.getAtual();
        File atualDepois = playlist.avancar();

        if (atualAntes == atualDepois) {
            if (repeatMode == RepeatMode.ALL) {
                playlist.definirAtualPorIndice(0);
                carregarMusicaAtual(true);
            } else {
                atualizarEstado("Fim da playlist");
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                }
            }
            return;
        }

        carregarMusicaAtual(true);
    }

    private void tocarAnterior() {
        if (playlist.estaVazia()) {
            return;
        }

        File atualAntes = playlist.getAtual();
        File atualDepois = playlist.voltar();

        if (atualAntes == atualDepois && repeatMode == RepeatMode.ALL) {
            playlist.definirAtualPorIndice(playlist.tamanho() - 1);
        }

        carregarMusicaAtual(true);
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

    private void salvarPlaylist() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PLAYLIST_FILE))) {
            for (File ficheiro : playlist.paraLista()) {
                writer.write(ficheiro.getAbsolutePath());
                writer.newLine();
            }
        } catch (IOException ex) {
            System.err.println("Erro ao guardar playlist: " + ex.getMessage());
        }
    }

    private void carregarPlaylist() {
        try (BufferedReader reader = new BufferedReader(new FileReader(PLAYLIST_FILE))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                File ficheiro = new File(linha);
                if (ficheiro.exists()) {
                    playlist.adicionar(ficheiro);
                }
            }
            atualizarListView();
        } catch (IOException ex) {
            System.out.println("Nenhuma playlist guardada anterior encontrada.");
        }
    }

    private void salvarConfiguracao() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIG_FILE))) {
            writer.write("volume=" + sliderVolume.getValue());
            writer.newLine();
            writer.write("shuffle=" + shuffleMode);
            writer.newLine();
            writer.write("repeat=" + repeatMode.ordinal());
            writer.newLine();
            writer.write("indiceAtual=" + playlist.getIndiceAtual());
            writer.newLine();
        } catch (IOException ex) {
            System.err.println("Erro ao guardar configuração: " + ex.getMessage());
        }
    }

    private void carregarConfiguracao() {
        int indiceGuardado = -1;

        try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                if (linha.startsWith("volume=")) {
                    double volume = Double.parseDouble(linha.substring(7));
                    sliderVolume.setValue(volume);
                } else if (linha.startsWith("shuffle=")) {
                    shuffleMode = Boolean.parseBoolean(linha.substring(8));
                } else if (linha.startsWith("repeat=")) {
                    int modoRepeat = Integer.parseInt(linha.substring(7));
                    if (modoRepeat >= 0 && modoRepeat < RepeatMode.values().length) {
                        repeatMode = RepeatMode.values()[modoRepeat];
                    }
                } else if (linha.startsWith("indiceAtual=")) {
                    String valor = linha.split("=", 2)[1].trim();
                    indiceGuardado = Integer.parseInt(valor);
                }
            }
        } catch (IOException ex) {
            System.out.println("Nenhuma configuração guardada anterior encontrada.");
        }

        if (shuffleMode) {
            btnShuffle.setStyle(
                    "-fx-background-color: #4a9eff;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 8;");
        }

        if (repeatMode != RepeatMode.NONE) {
            btnRepeat.setStyle(
                    "-fx-background-color: #4a9eff;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 8;");
        }

        carregarPlaylist();

        if (!playlist.estaVazia()) {
            if (indiceGuardado >= 0 && indiceGuardado < playlist.tamanho()) {
                playlist.definirAtualPorIndice(indiceGuardado);
            } else {
                playlist.definirAtualPorIndice(0);
            }

            int indiceAtual = playlist.getIndiceAtual();
            if (indiceAtual >= 0) {
                listViewPlaylist.getSelectionModel().select(indiceAtual);
            }

            carregarMusicaAtual(false);
        }
    }

    private void pararReproducaoAtual() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    private void atualizarEstado(String mensagem) {
        Platform.runLater(() -> lblEstado.setText(mensagem));
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

    private void mostrarAviso(String mensagem) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aviso");
            alert.setHeaderText("Informação");
            alert.setContentText(mensagem);
            alert.showAndWait();
        });
    }

    private void executarComSeguranca(Runnable tarefa) {
        try {
            if (Platform.isFxApplicationThread()) {
                tarefa.run();
            } else {
                Platform.runLater(tarefa);
            }
        } catch (Exception ex) {
            mostrarErro("Erro: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private Button criarBotao(String texto) {
        Button btn = new Button(texto);
        btn.setPrefWidth(90);
        btn.setPrefHeight(36);
        btn.setStyle(estiloBotaoPadrao());
        return btn;
    }

    private String estiloBotaoPadrao() {
        return "-fx-background-color: #3a3a3a;" +
               "-fx-text-fill: white;" +
               "-fx-font-size: 13px;" +
               "-fx-font-weight: bold;" +
               "-fx-background-radius: 8;" +
               "-fx-cursor: hand;";
    }

    public static void main(String[] args) {
        launch(args);
    }
}