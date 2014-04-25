package application;

import javax.sound.sampled.LineUnavailableException;

import application.sound.Sound;

public class Gameboy {

    public static final int SCREEN_WIDTH = 160;
    public static final int SCREEN_HEIGHT = 144;

    private Z80 z80;
    private Sound sound;
    private MemoryMap mmu;

    private int lcdModeTimer;
    private boolean frameComplete;
    private boolean vBlankPending;
    private boolean romLoaded = false;

    private int[][] bgScreen = new int[SCREEN_HEIGHT][SCREEN_WIDTH];
    private int[][] screen = new int[SCREEN_HEIGHT][SCREEN_WIDTH];

    public Gameboy() {

        z80 = new Z80();
        try {
            sound = new Sound();
        } catch (LineUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mmu = MemoryMap.getInstance();
    }

    public void powerUp() {

        z80.init();
        mmu.powerUp();

        lcdModeTimer = 0;
        vBlankPending = false;
        romLoaded = true;

        sound.startAudio();
    }

    public void executeOneFrame() {

        frameComplete = false;

        while (!frameComplete) {

            int cycles = z80.execute();

            updateTimer(cycles);
            updateLCDStatus(cycles);
            sound.updateSound(cycles);

            z80.checkInterrupts();
        }
    }

    public void updateTimer(int cyclesRun) {

        mmu.incDIVTimer(cyclesRun);

        if (mmu.isTimerStart()) {

            mmu.incTIMATimer(cyclesRun);
        }
    }

    public void updateLCDStatus(int cyclesRun) {

        lcdModeTimer += cyclesRun;

        LcdMode currMode = mmu.getCurrLcdMode();
        if (lcdModeTimer > currMode.getCycles()) {

            lcdModeTimer -= currMode.getCycles();

            LcdMode nextMode = currMode;
            switch (currMode) {
            case H_BLANK:
                updateScreen();
                mmu.incLY();

                if (mmu.LY() >= 144) {

                    vBlankPending = true;
                    nextMode = LcdMode.V_BLANK;
                } else {

                    if (mmu.lcdStatInterruptEnabled(LcdStatInterrupt.OAM)
                            && mmu.isScreenOn()) {

                        mmu.setInterrupt(Interrupt.LCD_STAT);
                    }
                    nextMode = LcdMode.OAM_RAM;
                }
                break;
            case V_BLANK:
                mmu.incLY();

                if (mmu.LY() >= 144) {

                } else {

                    frameComplete = true;

                    if (mmu.lcdStatInterruptEnabled(LcdStatInterrupt.OAM)
                            && mmu.isScreenOn()) {

                        mmu.setInterrupt(Interrupt.LCD_STAT);
                    }
                    nextMode = LcdMode.OAM_RAM;
                }
                break;
            case OAM_RAM:
                nextMode = LcdMode.DATA_TRANSFER;
                break;
            case DATA_TRANSFER:

                if (mmu.lcdStatInterruptEnabled(LcdStatInterrupt.H_BLANK)
                        && mmu.isScreenOn()) {

                    mmu.setInterrupt(Interrupt.LCD_STAT);
                }

                nextMode = LcdMode.H_BLANK;

                if (mmu.getCart().isCGB()) {

                    int cycles = mmu.handleDmaTransfer();
                    updateTimer(cycles);
                    lcdModeTimer += cycles;
                    sound.updateSound(cycles);
                }
                break;
            }

            if (nextMode != currMode) {

                mmu.setLcdMode(nextMode);
            }
        }

        if (lcdModeTimer >= 24 && vBlankPending) {

            vBlankPending = false;
            if (mmu.isScreenOn()) {
                mmu.setInterrupt(Interrupt.V_BLANK);
                if (mmu.lcdStatInterruptEnabled(LcdStatInterrupt.V_BLANK)) {

                    mmu.setInterrupt(Interrupt.LCD_STAT);
                }
            }
        }

    }

    private void updateScreen() {

        updateBG();
        updateWindow();
        updateSprites();

    }

    private void updateBG() {

        int ly = mmu.LY();

        if (mmu.bgDisplayEnabled() && mmu.isScreenOn()) {

            int yPos = (mmu.SCY() + ly) % 256;

            for (int x = 0; x < SCREEN_WIDTH; x++) {

                int xPos = (mmu.SCX() + x) % 256;

                int row = (yPos >> 3) & 0xFF;
                int col = (xPos >> 3) & 0xFF;

                int tileAddr = mmu.getTileAddr(mmu.getBgTileNum(row, col));

                int tileInfo = mmu.getBgTileInfo(row, col);

                // each vertical line takes up two bytes of memory
                int line = (yPos % 8) * 2;

                int tileBank = 0;
                if (mmu.getCart().isCGB()) {

                    tileBank = (tileInfo & 0x8) >> 3;
                }

                int data1 = mmu.vram[tileBank][tileAddr + line] & 0xFF;
                int data2 = mmu.vram[tileBank][tileAddr + line + 1] & 0xFF;

                int colorBit = 7 - (xPos % 8);
                // combine data 2 and data 1 to get the colour id for this pixel
                int colorNumber = ((data2 & (1 << colorBit)) == 0) ? 0 : 0x2;
                colorNumber |= ((data1 & (1 << colorBit)) == 0) ? 0 : 1;

                // finally get color from palette and draw
                int color = getColor(colorNumber, mmu.BGP());

                bgScreen[ly][x] = color;
                screen[ly][x] = color;
            }

        } else {

            for (int x = 0; x < 160; x++) {
                int color = getColor(0, mmu.BGP());
                // draw
                bgScreen[ly][x] = color;
                screen[ly][x] = color;
            }
        }
    }

    private void updateWindow() {

        int ly = mmu.LY();
        int wy = mmu.WY();

        if (mmu.windowDisplayEnabled() && mmu.isScreenOn() && (ly >= wy)) {

            int yPos = ly - wy;

            int wx = mmu.WX();

            for (int x = 0; x < 160; x++) {

                if (x >= (wx - 7)) {

                    int xPos = x - (wx - 7);

                    int row = (yPos >> 3) & 0xFF;
                    int col = (xPos >> 3) & 0xFF;

                    int tileAddr = mmu.getTileAddr(mmu.getWindowTileNum(row,
                            col));

                    // each vertical line takes up two bytes of memory
                    int line = (yPos % 8) * 2;

                    int tileInfo = mmu.getWindowTileInfo(row, col);

                    int tileBank = 0;
                    if (mmu.getCart().isCGB()) {

                        tileBank = (tileInfo & 0x8) >> 3;
                    }

                    int data1 = mmu.vram[tileBank][tileAddr + line] & 0xFF;
                    int data2 = mmu.vram[tileBank][tileAddr + line + 1] & 0xFF;

                    int colorBit = 7 - (xPos % 8);
                    // combine data 2 and data 1 to get the colour id for this
                    // pixel
                    int colorNumber = ((data2 & (1 << colorBit)) == 0) ? 0
                            : 0x2;
                    colorNumber |= ((data1 & (1 << colorBit)) == 0) ? 0 : 1;

                    // finally get color from palette and draw
                    int color = getColor(colorNumber, mmu.BGP());

                    bgScreen[ly][x] = color;
                    screen[ly][x] = color;
                }
            }
        }
    }

    private void updateSprites() {

        if (mmu.objDisplayEnabled() && mmu.isScreenOn()) {

            int ly = mmu.LY();
            int objSize = mmu.objSize();
            int bgColor0 = getColor(0, mmu.BGP());

            Sprite[] sprites = mmu.getSprites();

            for (Sprite sprite : sprites) {

                int tileNum = sprite.getTileNum();
                if (objSize == 16)
                    tileNum &= 0xFE;

                if ((ly >= sprite.getY() - 16) && (ly < (sprite.getY() - 16 + objSize))) {

                    int line = ly - (sprite.getY() - 16);

                    if (sprite.yFlip())
                        line = objSize - line - 1;

                    // each vertical line takes up two bytes of memory
                    line *= 2;

                    int dataAddress = (tileNum * 16) + line;

                    int tileBank = 0;
                    if (mmu.getCart().isCGB()) {

                        tileBank = sprite.getTileBank();
                    }

                    int data1 = mmu.vram[tileBank][dataAddress];
                    int data2 = mmu.vram[tileBank][dataAddress + 1];

                    for (int pixel = 7; pixel >= 0; pixel--) {

                        int colorBit = pixel;

                        int pos = 7 - pixel + sprite.getX() - 8;
                        if (pos > 159 || pos < 0)
                            continue;

                        if (sprite.xFlip())
                            colorBit = 7 - colorBit;

                        // combine data 2 and data 1 to get the colour id for
                        // this pixel
                        int colorNumber = ((data2 & (1 << colorBit)) == 0) ? 0
                                : 0x2;
                        colorNumber |= ((data1 & (1 << colorBit)) == 0) ? 0 : 1;

                        if (colorNumber == 0)
                            continue;

                        // get color from palette
                        int palette;
                        if (sprite.gbPalNum() == 1)
                            palette = mmu.OBP1();
                        else
                            palette = mmu.OBP0();

                        int color = getColor(colorNumber, palette);

                        if (sprite.isObjAboveBG() || bgScreen[ly][pos] == bgColor0)
                            screen[ly][pos] = color;
                    }
                }
            }
        }

    }

    private int getColor(int colorNum, int palette) {

        return ((palette >> (2 * colorNum)) & 0x03);
    }

    public int getScreen(int x, int y) {

        return screen[y][x];
    }

    public boolean isRomLoaded() {

        return romLoaded;
    }

}
