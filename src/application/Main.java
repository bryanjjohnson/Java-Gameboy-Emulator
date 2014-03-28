package application;

import java.io.File;
import java.io.IOException;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TimelineBuilder;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    public static void main(String[] args) {

        launch(args);
    }

    final MemoryMap mmu = MemoryMap.getInstance();
    private Gameboy gameboy;

    private Color[] colors = { Color.rgb(255, 255, 255), Color.rgb(170, 170, 170), Color.rgb(85, 85, 85), Color.rgb(0, 0, 0) };

    final VBox vBox = new VBox();
    final MenuBar menuBar = new MenuBar();
    ImageView imageView = new ImageView();
    WritableImage wImage = new WritableImage(
            Gameboy.SCREEN_WIDTH * 2,
            Gameboy.SCREEN_HEIGHT * 2);

    @Override
    public void start(final Stage primaryStage) throws IOException {

        gameboy = new Gameboy();

        final Duration oneFrameDur = Duration.millis(17);
        final KeyFrame oneFrame = new KeyFrame(oneFrameDur,
                new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent event) {

                        gameboy.executeOneFrame();
                        updateImage();
                    }

                });

        final Timeline timeLine = TimelineBuilder.create()
                .cycleCount(Animation.INDEFINITE)
                .keyFrames(oneFrame)
                .build();

        primaryStage.setTitle("GameBoy Emulator");

        final Scene parentScene = new Scene(vBox);
        vBox.setMinSize(0, 0);

        Menu menuFile = new Menu("File");

        MenuItem menuLoadRom = new MenuItem("Load Rom");
        menuLoadRom.setAccelerator(KeyCombination.keyCombination("Ctrl+L"));
        menuLoadRom.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                FileChooser fileChooser = new FileChooser();

                // Set extension filter
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Rom files (*.gb)", "*.gb");
                fileChooser.getExtensionFilters().add(extFilter);

                // Show load file dialog
                File file = fileChooser.showOpenDialog(primaryStage);
                if (file != null) {

                    System.out.println("Got File: " + file.getName());

                    if (mmu.getCart().loadRom(file)) {

                        gameboy.powerUp();
                        timeLine.play();
                    } else {

                        System.out.println("Failed to load rom");
                    }
                }
            }
        });

        MenuItem menuClose = new MenuItem("Close");
        menuClose.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                timeLine.stop();
                stop();
                System.exit(0);
            }
        });

        menuFile.getItems().addAll(menuLoadRom, menuClose);

        Menu menuOptions = new Menu("Options");

        Menu menuZoom = new Menu("Zoom");

        ToggleGroup zoomToggleGroup = new ToggleGroup();

        RadioMenuItem radioZoom1 = new RadioMenuItem("x1");
        radioZoom1.setSelected(true);
        radioZoom1.setToggleGroup(zoomToggleGroup);
        radioZoom1.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                zoomView(1);
                // primaryStage.sizeToScene();
            }
        });

        RadioMenuItem radioZoom2 = new RadioMenuItem("x2");
        radioZoom2.setToggleGroup(zoomToggleGroup);
        radioZoom2.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                zoomView(2);
                // primaryStage.sizeToScene();
            }
        });

        RadioMenuItem radioZoom3 = new RadioMenuItem("x3");
        radioZoom3.setToggleGroup(zoomToggleGroup);
        radioZoom3.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                zoomView(3);
                // primaryStage.sizeToScene();
            }
        });

        menuZoom.getItems().addAll(radioZoom1, radioZoom2, radioZoom3);

        menuOptions.getItems().addAll(menuZoom);

        Menu menuHelp = new Menu("Help");

        MenuItem menuAbout = new MenuItem("About");

        MenuItem menuTileView = new MenuItem("Tile View");
        menuTileView.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                TileMapWindow testWindow = new TileMapWindow();
                testWindow.show();
            }
        });

        menuHelp.getItems().addAll(menuAbout, menuTileView);

        menuBar.getMenus().addAll(menuFile, menuOptions, menuHelp);

        imageView.setImage(wImage);

        vBox.getChildren().addAll(menuBar, imageView);

        parentScene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent event) {

                switch (event.getCode()) {
                case RIGHT:
                    mmu.keyPress(Key.RIGHT);
                    break;
                case LEFT:
                    mmu.keyPress(Key.LEFT);
                    break;
                case UP:
                    mmu.keyPress(Key.UP);
                    break;
                case DOWN:
                    mmu.keyPress(Key.DOWN);
                    break;
                case A:
                    mmu.keyPress(Key.A);
                    break;
                case S:
                    mmu.keyPress(Key.B);
                    break;
                case SPACE:
                    mmu.keyPress(Key.SELECT);
                    break;
                case ENTER:
                    mmu.keyPress(Key.START);
                    break;
                default:
                    break;
                }

            }
        });

        parentScene.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent event) {

                switch (event.getCode()) {
                case RIGHT:
                    mmu.keyRelease(Key.RIGHT);
                    break;
                case LEFT:
                    mmu.keyRelease(Key.LEFT);
                    break;
                case UP:
                    mmu.keyRelease(Key.UP);
                    break;
                case DOWN:
                    mmu.keyRelease(Key.DOWN);
                    break;
                case A:
                    mmu.keyRelease(Key.A);
                    break;
                case S:
                    mmu.keyRelease(Key.B);
                    break;
                case SPACE:
                    mmu.keyRelease(Key.SELECT);
                    break;
                case ENTER:
                    mmu.keyRelease(Key.START);
                    break;
                default:
                    break;
                }

            }
        });

        parentScene.getStylesheets().add(Main.class.getResource("application.css").toExternalForm());
        primaryStage.setScene(parentScene);
        primaryStage.setResizable(false);
        primaryStage.sizeToScene();
        primaryStage.show();

        zoomView(1);
    }

    private void updateImage() {

        PixelWriter pixelWriter = wImage.getPixelWriter();

        for (int y = 0; y < Gameboy.SCREEN_HEIGHT; y++) {

            for (int x = 0; x < Gameboy.SCREEN_WIDTH; x++) {

                pixelWriter.setColor(x * 2, y * 2, colors[gameboy.getScreen(x, y)]);
                pixelWriter.setColor(x * 2, y * 2 + 1, colors[gameboy.getScreen(x, y)]);
                pixelWriter.setColor(x * 2 + 1, y * 2, colors[gameboy.getScreen(x, y)]);
                pixelWriter.setColor(x * 2 + 1, y * 2 + 1, colors[gameboy.getScreen(x, y)]);
            }
        }
    }

    private void zoomView(int zoom) {

        imageView.setFitHeight(Gameboy.SCREEN_HEIGHT * zoom);
        imageView.setFitWidth(Gameboy.SCREEN_WIDTH * zoom);

        imageView.getScene().getWindow().sizeToScene();
    }

    @Override
    public void stop() {

        if (gameboy.isRomLoaded()) {

            mmu.getCart().saveRAM();
        }
        System.out.println("Closing");
    }

}
