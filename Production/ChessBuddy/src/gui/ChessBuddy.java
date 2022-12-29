package gui;

import fxutil.AudioClipPlayer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChessBuddy extends Application {

    public static final String TITLE = "ChessBuddy";
    public static final String APP_ICON_PATH = "/resources/icon.png";
    public static final String SOUND_CLIP_PATH = ChessBuddy.class.getResource("/resources/movepiece.wav").toExternalForm();
    public static final String CONFIG_NAME = "ChessLiteConfig"; //file and config info
    public static final String DEFAULT_CONFIG_PATH = "/resources/DefaultConfig.dat";
    public static final String FOLDER = System.getProperty("file.separator") + ".ChessLiteDat";
    public static final String CONFIG_DIR = System.getProperty("user.home") + FOLDER;
    public static final String CONFIG_PATH = System.getProperty("user.home") +
            FOLDER + System.getProperty("file.separator") + CONFIG_NAME + ".dat";
    public static final String[] AVAILABLE_PATHS = {"classic"};
    public static final int BROWN = 0;
    private final double height = 760;
    private final double width = height*1.52;
    private final double scale = height/860;
    private final AudioClipPlayer clip = new AudioClipPlayer(SOUND_CLIP_PATH); //sound clip
    private String path = "classic"; //path for piece package
    private int colorTheme = BROWN; //color theme

    public AudioClipPlayer getClip() {
        return clip;
    }

    public String getPath() {
        return path;
    }

    public int getColorTheme() {
        return colorTheme;
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

    public double getScale() {
        return scale;
    }

    public void changeConfigData(String str) {
        writeStringToFile(str, CONFIG_PATH);
    }

    public boolean contains(String[] arr, String stringIn) {
        for(String string : arr) {
            if(string.equals(stringIn)) {
                return true;
            }
        }
        return false;
    }

    public void writeStringToFile(String string, String path) {
        File file = new File(path);
        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file,false))) {
                writer.write(string);
            }
        } catch (IOException ex) {
            Logger.getLogger(ChessBuddy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void copyToFile(BufferedReader reader, File file) {
        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                StringBuilder contents = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null) {
                    contents.append(line);
                    contents.append("\n");
                }
                writer.write(contents.toString());
                reader.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(ChessBuddy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void init() {
        File dir = new File(CONFIG_DIR);
        dir.mkdir();
        File file = new File(CONFIG_PATH);
        try {
            if(file.createNewFile()) {
                BufferedReader defaultConfigReader = new BufferedReader(new InputStreamReader(
                        ChessBuddy.class.getResourceAsStream(DEFAULT_CONFIG_PATH)));
                copyToFile(defaultConfigReader,file);
            } else {
                if(file.canRead()) {
                    String[] data = new String[2];
                    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                        String line;
                        int i = 0;
                        while ((line = br.readLine()) != null) {
                            data[i] = line;
                            i++;
                        }
                    }
                    String str = data[0];
                    if(contains(AVAILABLE_PATHS,str)) {
                        this.path = str;
                    }
                    int colorNum = (Integer.parseInt(data[1]));
                    if(colorNum >= BROWN) {
                        colorTheme = colorNum;
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ChessBuddy.class.getName()).log(Level.SEVERE, null, ex);
        }
        clip.startLoop();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.getIcons().add(new Image(APP_ICON_PATH));
        primaryStage.setTitle(TITLE);
        primaryStage.setHeight(height);
        primaryStage.setWidth(width);
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest((WindowEvent event) -> {
            Platform.exit();
            System.exit(0);
        });
        Pane root = createPlayPane(primaryStage);
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(ChessBuddy.class.getResource("/resources/chess.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public int getPieceSelection(String pathIn) {
        int i = 0;
        for(String str : AVAILABLE_PATHS) {
            if(str.equals(pathIn)) {
                return i;
            }
            i++;
        }
        return 0;
    }

    public Pane createPlayPane(Stage stage) {
        Game controller = Game.constructGame(true, stage, this);
        return controller.getRoot();
    }


    public static void main(String[] args) {
        launch(args);
    }

}
