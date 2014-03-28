package application;

import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class TileMapWindow extends Stage {

    private WritableImage tileImage = new WritableImage(128, 194);

    private Color[] colors = { Color.rgb(255, 255, 255), Color.rgb(170, 170, 170), Color.rgb(85, 85, 85), Color.rgb(0, 0, 0) };

    public TileMapWindow() {

        this.setTitle("My New Stage Title");

        VBox vBox = new VBox();
        Scene scene = new Scene(vBox);

        ImageView imageView = new ImageView();
        imageView.setFitHeight(194 * 2);
        imageView.setFitWidth(128 * 2);

        drawTileMap();
        imageView.setImage(tileImage);

        vBox.getChildren().addAll(imageView);

        this.setScene(scene);
    }

    private void drawTileMap() {

        MemoryMap mmu = MemoryMap.getInstance();

        PixelWriter pixelWriter = tileImage.getPixelWriter();

        for (int tileNum = 0; tileNum < 384; tileNum++) {

            int tileAddr = tileNum * 16;

            for (int y = 0; y < 8; y++) {

                int data1 = mmu.vram[0][tileAddr + (y * 2)];
                int data2 = mmu.vram[0][tileAddr + (y * 2) + 1];

                for (int x = 0; x < 8; x++) {

                    int colorNumber = (data2 & (1 << (7 - x))) != 0 ? 0x2 : 0;
                    colorNumber |= (data1 & (1 << (7 - x))) != 0 ? 1 : 0;

                    pixelWriter.setColor(((tileNum % 16) * 8) + x, ((tileNum >> 4) * 8) + y, colors[colorNumber]);
                }
            }
        }
    }

}
