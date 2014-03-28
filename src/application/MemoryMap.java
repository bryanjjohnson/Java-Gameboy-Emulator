package application;

import java.util.Arrays;
import java.util.Calendar;

import application.Key.KeyType;
import application.model.Word;

public class MemoryMap {

    private static MemoryMap instance;

    protected MemoryMap() {

    }

    public static synchronized MemoryMap getInstance() {

        if (instance == null) {

            instance = new MemoryMap();
        }

        if (cart == null) {

            cart = new Cartridge();
        }

        return instance;
    }

    public static final int JOYP = 0x00;
    public static final int SB = 0x01;
    public static final int SC = 0x01;
    public static final int DIV = 0x04;
    public static final int TIMA = 0x05;
    public static final int TMA = 0x06;
    public static final int TAC = 0x07;
    public static final int IF = 0x0F;
    public static final int NR10 = 0x10;
    public static final int NR11 = 0x11;
    public static final int NR12 = 0x12;
    public static final int NR13 = 0x13;
    public static final int NR14 = 0x14;
    public static final int NR21 = 0x16;
    public static final int NR22 = 0x17;
    public static final int NR23 = 0x18;
    public static final int NR24 = 0x19;
    public static final int NR30 = 0x1A;
    public static final int NR31 = 0x1B;
    public static final int NR32 = 0x1C;
    public static final int NR33 = 0x1D;
    public static final int NR34 = 0x1E;
    public static final int NR41 = 0x20;
    public static final int NR42 = 0x21;
    public static final int NR43 = 0x22;
    public static final int NR44 = 0x23;
    public static final int NR50 = 0x24;
    public static final int NR51 = 0x25;
    public static final int NR52 = 0x26;
    public static final int LCDC = 0x40;
    public static final int STAT = 0x41;
    public static final int SCY = 0x42;
    public static final int SCX = 0x43;
    public static final int LY = 0x44;
    public static final int LYC = 0x45;
    public static final int DMA = 0x46;
    public static final int BGP = 0x47;
    public static final int OBP0 = 0x48;
    public static final int OBP1 = 0x49;
    public static final int WY = 0x4A;
    public static final int WX = 0x4B;

    public static final int VBK = 0x4F;
    public static final int HDMA1 = 0x51;
    public static final int HDMA2 = 0x52;
    public static final int HDMA3 = 0x53;
    public static final int HDMA4 = 0x54;
    public static final int HDMA5 = 0x55;
    public static final int BGPI = 0x68;
    public static final int BGPD = 0x69;
    public static final int OBPI = 0x6A;
    public static final int OBPD = 0x6B;
    public static final int SVBK = 0x70;

    private static Cartridge cart;
    public static int[][] vram = new int[2][0x2000];
    public static int[][] wram = new int[8][0x1000];
    public static int[] oam = new int[0xA0];
    public static int[] io = new int[0x80];
    public static int[] hram = new int[0x7F];
    public static int IE = 0x00;

    public static Sprite[] sprites = new Sprite[40];

    private int[] bgPalette = new int[0x40];
    private int[] obPalette = new int[0x40];

    private static final int DIV_RATE = 256;

    private int divTimer;
    private int timaTimer;

    private int joyDirection;
    private int joyButton;

    private int currVramBank;
    private int currWramBank;

    private Word sourceAddr;
    private Word destAddr;

    public void powerUp() {

        joyDirection = 0xF;
        joyButton = 0xF;

        io[JOYP] = 0xFF;
        io[TIMA] = 0x00;
        io[TMA] = 0x00;
        io[TAC] = 0x00;
        io[NR10] = 0x80;
        io[NR11] = 0xBF;
        io[NR12] = 0xF3;
        io[NR14] = 0xBF;
        io[NR21] = 0x3F;
        io[NR22] = 0x00;
        io[NR24] = 0xBF;
        io[NR30] = 0x7F;
        io[NR31] = 0xFF;
        io[NR32] = 0x9F;
        io[NR34] = 0xBF;
        io[NR41] = 0xFF;
        io[NR42] = 0x00;
        io[NR43] = 0x00;
        io[NR44] = 0xBF;
        io[NR50] = 0x77;
        io[NR51] = 0xF3;
        io[NR52] = 0xF1;
        if (cart.isSGB()) {

            io[NR52] = 0xF0;
        }
        io[LCDC] = 0x91;
        io[SCY] = 0x00;
        io[SCX] = 0x00;
        io[LYC] = 0x00;
        io[BGP] = 0xFC;
        io[OBP0] = 0xFF;
        io[OBP1] = 0xFF;
        io[WY] = 0x00;
        io[WX] = 0x00;
        io[HDMA5] = 0xFF;
        IE = 0x00;

        divTimer = 0;
        timaTimer = 0;

        currVramBank = 0;
        currWramBank = 1;

        for (int i = 0; i < 40; i++) {

            sprites[i] = new Sprite(i);
        }

        sourceAddr = new Word();
        destAddr = new Word();
    }

    public int readSignedByte(Word addr) {

        int val = readByte(addr);

        if (val > 127)
            val -= 256;

        return val;
    }

    public int readByte(Word addr) {

        int val = 0;

        switch (addr.getWord() & 0xF000) {

        case 0x0000:
        case 0x1000:
        case 0x2000:
        case 0x3000:
            val = cart.readRom(0, addr.getWord());
            break;

        case 0x4000:
        case 0x5000:
        case 0x6000:
        case 0x7000:
            val = cart.readRom(cart.getCurrRomBank(), addr.getWord() - 0x4000);
            break;

        case 0x8000:
        case 0x9000:
            val = vram[currVramBank][addr.getWord() - 0x8000];
            break;

        case 0xA000:
        case 0xB000:
            if (!cart.isRamEnabled()) {

                return 0xFF;
            }

            switch (cart.getType()) {

            case ROM:
                val = cart.readRam(addr.getWord() - 0xA000);
                break;

            case MBC1:
            case MBC5:
                val = cart.readRam((cart.getCurrRamBank() * 0x2000) + addr.getWord() - 0xA000);
                break;

            case MBC2:
                if (addr.getWord() < 0xA200) {

                    val = cart.readRam(addr.getWord() - 0xA000) & 0x0F;
                }
                break;

            case MBC3:

                switch (cart.getCurrRamBank() & 0xF) {
                case 0x0:
                case 0x1:
                case 0x2:
                case 0x3:
                    val = cart.readRam((cart.getCurrRamBank() * 0x2000) + addr.getWord() - 0xA000);
                    break;
                case 0x8:
                    val = cart.getRtcLatchSeconds();
                    break;
                case 0x9:
                    val = cart.getRtcLatchMinutes();
                    break;
                case 0xA:
                    val = cart.getRtcLatchHours();
                    break;
                case 0xB:
                    val = (cart.getRtcLatchDays() & 0xFF);
                    break;
                case 0xC:
                    int temp = (cart.getRtcLatchDays() & 0x100) >> 8;
                    if (cart.isRtcCarry()) {
                        temp |= 0x80;
                    }
                    if (cart.isRtcHalt()) {
                        temp |= 0x40;
                    }
                    break;
                }

                break;

            default:
                val = cart.readRam(addr.getWord() - 0xA000);
                break;
            }
            break;

        case 0xC000:
            val = wram[0][addr.getWord() - 0xC000];
            break;
        case 0xD000:
            val = wram[currWramBank][addr.getWord() - 0xD000];
            break;

        case 0xE000:
            val = wram[0][addr.getWord() - 0xE000];
            break;

        case 0xF000:

            if (addr.getWord() < 0xFE00) {

                val = wram[currWramBank][addr.getWord() - 0xF000];

            } else if (addr.getWord() < 0xFEA0) {

                val = oam[addr.getWord() - 0xFE00];

            } else if (addr.getWord() < 0xFF00) {

                // Not used

            } else if (addr.getWord() == 0xFF00) {

                if ((~io[JOYP] & (1 << 4)) == (1 << 4)) {

                    return (joyDirection | 0xF0);
                } else if ((~io[JOYP] & (1 << 5)) == (1 << 5)) {

                    return (joyButton | 0xF0);
                }

            } else if (addr.getWord() < 0xFF80) {

                val = io[addr.getWord() - 0xFF00];

                if ((addr.getWord() - 0xFF00) == BGPD) {

                    val = bgPalette[io[BGPI] & 0x3F];
                }

                if ((addr.getWord() - 0xFF00) == OBPD) {

                    val = obPalette[io[OBPI] & 0x3F];
                }

            } else if (addr.getWord() < 0xFFFF) {

                val = hram[addr.getWord() - 0xFF80];
            } else {

                val = IE;

            }

            break;

        default:
            System.out.println("Attempting to read from unrecognized address: "
                    + Integer.toHexString(addr.getWord()));
            break;
        }

        return (val & 0xFF);
    }

    public void writeByte(Word addr, int val) {

        val &= 0xFF;

        switch (addr.getWord() & 0xF000) {

        case 0x0000:
        case 0x1000:
            switch (cart.getType()) {

            case MBC1:
            case MBC3:
            case MBC5:
                if ((val & 0xF) == 0xA) {

                    cart.enableRam();
                } else {

                    cart.disableRam();
                }
                break;

            case MBC2:
                if ((addr.getWord() & 0x0100) == 0x0) {
                    if ((val & 0xF) == 0xA) {

                        cart.enableRam();
                    } else {

                        cart.disableRam();
                    }
                }
                break;

            default:
                break;
            }
            break;

        case 0x2000:
        case 0x3000:
            switch (cart.getType()) {

            case MBC1:
                val &= 0x1F;
                if (val == 0) {

                    val++;
                }
                cart.setCurrRomBank((cart.getCurrRomBank() & 0x60) | val);
                break;

            case MBC2:
                if ((addr.getWord() & 0x0100) == 0x0) {
                    cart.setCurrRomBank(val & 0x0F);
                }
                break;

            case MBC3:
                val &= 0x7F;
                if (val == 0) {

                    val++;
                }
                cart.setCurrRomBank(val);
                break;

            case MBC5:
                if (addr.getWord() < 0x3000) {

                    cart.setCurrRomBank((cart.getCurrRomBank() & 0x100) | (val & 0xFF));
                } else {

                    cart.setCurrRomBank((cart.getCurrRomBank() & 0xFF) | ((val & 0x1) << 8));
                }
                break;

            default:
                break;
            }
            break;

        case 0x4000:
        case 0x5000:
            switch (cart.getType()) {

            case MBC1:
                if (cart.isRomMode()) {

                    cart.setCurrRomBank((cart.getCurrRomBank() & 0x1F)
                            | ((val & 0x03) << 5));

                    if (cart.getCurrRomBank() == 0x20
                            || cart.getCurrRomBank() == 0x40
                            || cart.getCurrRomBank() == 0x60) {

                        cart.setCurrRomBank(cart.getCurrRomBank() + 1);
                    }
                } else {

                    cart.setCurrRamBank(val & 0x03);
                }
                break;

            case MBC3:
            case MBC5:
                cart.setCurrRamBank(val & 0xF);
                break;

            default:
                break;
            }
            break;

        case 0x6000:
        case 0x7000:
            switch (cart.getType()) {

            case MBC1:
                if ((val & 0x1) == 0x0) {

                    cart.setToRomMode();
                } else {

                    cart.setToRamMode();
                }
                break;

            case MBC3:
                if (cart.isRamEnabled()) {

                    if (val == 0x01) {

                        cart.setRtcLatch(Calendar.getInstance().compareTo(cart.getStartTime()));
                    } else if (val == 0x00) {

                        cart.setRtcLatchEnabled(true);
                    } else {

                        cart.setRtcLatchEnabled(false);
                    }
                }
                break;

            default:
                break;
            }
            break;

        case 0x8000:
        case 0x9000:
            vram[currVramBank][addr.getWord() - 0x8000] = val;
            break;

        case 0xA000:
        case 0xB000:
            if (!cart.isRamEnabled()) {

                return;
            }
            switch (cart.getType()) {
            case ROM:
                cart.writeRam(addr.getWord() - 0xA000, val);
                break;

            case MBC1:
            case MBC5:
                cart.writeRam(
                        (cart.getCurrRamBank() * 0x2000)
                                + addr.getWord() - 0xA000, val);
                break;

            case MBC2:
                if (addr.getWord() < 0xA200) {

                    cart.writeRam(addr.getWord() - 0xA000, (val & 0x0F));
                }
                break;

            case MBC3:
                switch (cart.getCurrRamBank() & 0xF) {
                case 0x0:
                case 0x1:
                case 0x2:
                case 0x3:
                    cart.writeRam((cart.getCurrRamBank() * 0x2000) + addr.getWord() - 0xA000, val);
                    break;
                case 0x8:
                    cart.setRtcLatchSeconds(val);
                    break;
                case 0x9:
                    cart.setRtcLatchMinutes(val);
                    break;
                case 0xA:
                    cart.setRtcLatchHours(val);
                    break;
                case 0xB:
                    cart.setRtcLatchDays((cart.getRtcLatchDays() & 0x100) | (val & 0xFF));
                    break;
                case 0xC:
                    cart.setRtcLatchDays((cart.getRtcLatchDays() & 0xFF) | ((val & 0x1) << 8));
                    boolean isHalt = (val & 0x40) == 0x40;
                    cart.setRtcCarry((val & 0x80) == 0x80);
                    if (isHalt)
                        cart.setRtcHaltTime(cart.getRtcLatch());
                    if (cart.isRtcHalt() && !isHalt)
                    {
                        long haltTime = cart.getRtcLatch() - cart.getRtcHaltTime();
                        cart.getStartTime().setTimeInMillis(cart.getStartTime().getTimeInMillis() + haltTime);
                    }
                    cart.setRtcHalt(isHalt);
                    break;
                }

                break;

            default:
                cart.writeRam(addr.getWord() - 0xA000, val);
                break;
            }
            break;

        case 0xC000:
            wram[0][addr.getWord() - 0xC000] = val;
            break;
        case 0xD000:
            wram[currWramBank][addr.getWord() - 0xD000] = val;
            break;

        case 0xE000:
            wram[0][addr.getWord() - 0xE000] = val;
            break;

        case 0xF000:

            if (addr.getWord() < 0xFE00) {

                wram[currWramBank][addr.getWord() - 0xF000] = val;

            } else if (addr.getWord() < 0xFEA0) {

                oam[addr.getWord() - 0xFE00] = val;
                updateSprite(addr.getWord() - 0xFE00, val);

            } else if (addr.getWord() < 0xFF00) {

            } else if (addr.getWord() < 0xFF80) {

                int ioAddr = (addr.getWord() - 0xFF00) & 0xFF;

                // SB
                if (ioAddr == SB) {
                    System.out.print((char) val);
                }

                // DIV
                if (ioAddr == DIV) {

                    val = 0x0;
                    divTimer = 0;
                }

                // TAC
                if (ioAddr == TAC) {

                    val &= 0x07;
                    if (((val & 0x3) != (io[TAC] & 0x3)) || ((val & 0x4) == 0)) {

                        timaTimer = 0;
                        io[TIMA] = io[TMA];
                    }
                }

                // IF
                if (ioAddr == IF) {
                    val |= 0xE0;
                }

                // STAT
                if (ioAddr == STAT) {

                    val = (val & ~0x07) | (io[STAT] & 0x07);
                }

                // LY
                if (ioAddr == LY) {

                    val = 0x0;
                }

                // LCDC
                if (ioAddr == LCDC) {

                    boolean screenOn = (val & 0x80) == 0x80;
                    if (isScreenOn() && !screenOn) {
                        io[LY] = 0;
                        io[STAT] &= ~0x03;

                        checkLYC();

                        // reset lcdModeTimer;
                    } else if (!isScreenOn() && screenOn) {

                        // reset lcdModeTimer;
                    }
                }

                if (ioAddr == DMA) {

                    for (int c = 0x00; c <= 0x9F; c++) {

                        Word tempAddr = new Word();
                        tempAddr.setHighByte(val);
                        tempAddr.setLowByte(c);
                        oam[c] = readByte(tempAddr);
                        updateSprite(c, oam[c]);
                    }
                }

                if (ioAddr == VBK && cart.isCGB()) {

                    currVramBank = val & 0x1;
                }

                // NEW DMA for CGB
                if (ioAddr == HDMA5 && cart.isCGB()) {

                    sourceAddr.setHighByte(io[HDMA1]);
                    sourceAddr.setLowByte(io[HDMA2] & 0xF0);

                    destAddr.setHighByte((io[HDMA3] & 0x1F));
                    destAddr.setLowByte(io[HDMA4] & 0xF0);

                    if ((val & 0x80) == 0) {

                        if ((io[HDMA5] & 0x80) == 0) {

                            val |= 0x80;

                        } else {

                            int len = (((val & 0x7F) + 1) << 4);

                            for (int c = 0; c < len; c++) {

                                vram[currVramBank][destAddr.getWord()] = readByte(sourceAddr);
                                destAddr.inc();
                                sourceAddr.inc();
                            }

                            val = 0xFF;
                        }

                    } else {

                        val &= ~0x80;
                    }

                }

                if ((addr.getWord() - 0xFF00) == BGPD) {

                    bgPalette[io[BGPI] & 0x3F] = val;

                    if ((io[BGPI] & 0x80) == 0x80) {

                        io[BGPI]++;
                        io[BGPI] &= ~0x40;
                    }
                }

                if ((addr.getWord() - 0xFF00) == OBPD) {

                    obPalette[io[OBPI] & 0x3F] = val;

                    if ((io[OBPI] & 0x80) == 0x80) {

                        io[OBPI]++;
                        io[OBPI] &= ~0x40;
                    }
                }

                if (ioAddr == SVBK && cart.isCGB()) {

                    currWramBank = val & 0x7;
                    if (currWramBank == 0)
                        currWramBank++;
                }

                io[ioAddr] = val;

                // LYC
                if (ioAddr == LYC) {

                    checkLYC();
                }

            } else if (addr.getWord() < 0xFFFF) {

                hram[addr.getWord() - 0xFF80] = val;

            } else {

                IE = val;

            }

            break;

        default:
            System.out.println("Attempting to read from unrecognized address: "
                    + Integer.toHexString(addr.getWord()));
            break;
        }

    }

    private void updateSprite(int addr, int val) {

        val &= 0xFF;
        int index = addr >> 2;
        int i;
        for (i = 0; i < 40; i++) {

            if (sprites[i].getIndex() == index) {

                break;
            }
        }

        switch (addr & 0x3) {
        case 0:
            sprites[i].setY(val);
            break;
        case 1:
            sprites[i].setX(val);
            Arrays.sort(sprites);
            break;
        case 2:
            sprites[i].setTileNum(val);
            break;
        case 3:
            sprites[i].setAttributeFlags(val);
            break;
        }

    }

    public Cartridge getCart() {

        return cart;
    }

    public boolean isScreenOn() {

        return (io[LCDC] & 0x80) == 0x80;
    }

    public boolean isInterruptTriggered() {

        return (IE & io[IF]) != 0;
    }

    public boolean isInterruptEnabled(Interrupt interrupt) {

        return (IE & interrupt.getMask()) == interrupt.getMask();
    }

    public boolean isInterruptSet(Interrupt interrupt) {

        return (io[IF] & interrupt.getMask()) == interrupt.getMask();
    }

    public void setInterrupt(Interrupt interrupt) {

        io[IF] |= interrupt.getMask();
    }

    public void disableInterrupt(Interrupt interrupt) {

        io[IF] &= ~interrupt.getMask();
    }

    public void incDIVTimer(int cyclesRun) {

        divTimer += cyclesRun;
        if (divTimer >= DIV_RATE) {

            divTimer -= DIV_RATE;
            incDIV();
        }
    }

    public void incDIV() {

        io[DIV]++;
    }

    public boolean isTimerStart() {

        return ((io[TAC] & 0x4) == 0x4);
    }

    private int[] TAC_CYCLES = { 1024, 16, 64, 256 };

    public int getTacCycles() {

        return TAC_CYCLES[(io[TAC] & 0x03)];
    }

    public void incTIMATimer(int cyclesRun) {

        timaTimer += cyclesRun;
        while (timaTimer > getTacCycles()) {

            timaTimer -= getTacCycles();
            incTIMA();
        }

    }

    public void incTIMA() {

        if (io[TIMA] == 0xFF) {

            io[TIMA] = io[TMA];

            setInterrupt(Interrupt.TIMER);
        } else {

            io[TIMA]++;
        }
    }

    public boolean lcdEnabled() {

        return ((io[LCDC] & 0x80) == 0x80);
    }

    public boolean windowDisplayEnabled() {

        return ((io[LCDC] & 0x20) == 0x20);
    }

    public int getTileAddr(int tileNum) {

        int tileAddr = 0;
        tileNum &= 0xFF;

        if ((io[LCDC] & 0x10) == 0x10) {

            tileAddr = 0x0;

        } else {

            tileAddr = 0x1000;
            if (tileNum > 127)
                tileNum -= 256;
        }
        tileAddr += (tileNum * 16);

        return tileAddr;
    }

    public int windowTileMapAddress() {

        if ((io[LCDC] & 0x40) == 0x0) {

            return 0x1800;
        } else {

            return 0x1C00;
        }
    }

    public int getWindowTileNum(int row, int col) {

        return vram[currVramBank][windowTileMapAddress() + (row * 32) + col];
    }

    public int getBgTileNum(int row, int col) {

        return vram[currVramBank][bgTileMapAddress() + (row * 32) + col];
    }

    public int bgTileMapAddress() {

        if ((io[LCDC] & 0x8) == 0x0) {

            return 0x1800;
        } else {

            return 0x1C00;
        }
    }

    public int objSize() {

        return (io[LCDC] & 0x4) == 0x4 ? 16 : 8;
    }

    public boolean objDisplayEnabled() {

        return ((io[LCDC] & 0x02) == 0x02);
    }

    public boolean bgDisplayEnabled() {

        return ((io[LCDC] & 0x01) == 0x01);
    }

    public LcdMode getCurrLcdMode() {

        return LcdMode.getByIndex(io[STAT] & 0x3);
    }

    public void setLcdMode(LcdMode nextMode) {

        io[STAT] = ((io[STAT] & ~0x3) | nextMode.getIndex());
        io[STAT] |= 0x80;
    }

    public boolean lcdStatInterruptEnabled(LcdStatInterrupt interrupt) {

        return (io[STAT] & interrupt.getMask()) == interrupt.getMask();
    }

    public int LY() {

        return io[LY];
    }

    public void incLY() {

        io[LY]++;
        io[LY] %= 154;

        checkLYC();
    }

    public void checkLYC() {

        if (io[LY] == io[LYC]) {

            io[STAT] |= 0x04;

            if (lcdStatInterruptEnabled(LcdStatInterrupt.LYC_LY)
                    && isScreenOn()) {

                setInterrupt(Interrupt.LCD_STAT);
            }
        } else {

            io[STAT] &= ~0x4;
        }
    }

    public int SCY() {

        return io[SCY];
    }

    public int SCX() {

        return io[SCX];
    }

    public int WY() {

        return io[WY];
    }

    public int WX() {

        return io[WX];
    }

    public int BGP() {

        return io[BGP];
    }

    public int OBP0() {

        return io[OBP0];
    }

    public int OBP1() {

        return io[OBP1];
    }

    public Sprite[] getSprites() {

        return sprites;
    }

    public void keyPress(Key key) {

        int oldJoyDirection = joyDirection & 0xF;
        int oldJoyButton = joyButton & 0xF;

        if (key.getKeyType() == KeyType.DIRECTION) {

            joyDirection &= ~key.getMask();
            joyDirection &= 0xF;
        } else if (key.getKeyType() == KeyType.BUTTON) {

            joyButton &= ~key.getMask();
            joyButton &= 0xF;
        }

        if ((~io[JOYP] & (1 << 4)) == (1 << 4)) {

            if (oldJoyDirection != joyDirection) {

                setInterrupt(Interrupt.JOYPAD);
            }
        } else if ((~io[JOYP] & (1 << 5)) == (1 << 5)) {

            if (oldJoyButton != joyButton) {

                setInterrupt(Interrupt.JOYPAD);
            }
        }
    }

    public void keyRelease(Key key) {

        if (key.getKeyType() == KeyType.DIRECTION) {

            joyDirection |= key.getMask();
            joyDirection &= 0xF;
        } else if (key.getKeyType() == KeyType.BUTTON) {

            joyButton |= key.getMask();
            joyButton &= 0xF;
        }
    }

    public boolean isAllSoundOn() {

        return (io[NR52] & 0x80) == 0x80;
    }

    public boolean isSoundOn(int soundNum) {

        int mask = 1 << (soundNum - 1);

        return (io[NR52] & mask) == mask;
    }

    public void setSoundOn(int soundNum) {

        int mask = 1 << (soundNum - 1);

        io[NR52] |= mask;
    }

    public void setSoundOff(int soundNum) {

        int mask = 1 << (soundNum - 1);

        io[NR52] &= ~mask;
    }

    public boolean isSoundToTerminal(int soundNum, int soundOutput) {

        int mask = 1 << ((soundNum - 1) + (soundOutput - 1) * 4);

        return (io[NR51] & mask) == mask;
    }

    public int getSoundLevel(int soundOutput) {

        return (io[NR50] >> ((soundOutput - 1) * 4)) & 0x7;
    }

    public boolean isSoundReset(int soundNum) {

        switch (soundNum) {
        case 1:
            return (io[NR14] & 0x80) == 0x80;
        case 2:
            return (io[NR24] & 0x80) == 0x80;
        case 3:
            return (io[NR34] & 0x80) == 0x80;
        case 4:
            return (io[NR44] & 0x80) == 0x80;
        }

        return false;
    }

    public void removeSoundReset(int soundNum) {

        switch (soundNum) {
        case 1:
            io[NR14] &= 0x7F;
            break;
        case 2:
            io[NR24] &= 0x7F;
            break;
        case 3:
            io[NR34] &= 0x7F;
            break;
        case 4:
            io[NR44] &= 0x7F;
            break;
        }

    }

    public int handleDmaTransfer() {

        if ((io[HDMA5] & 0x80) == 0) {

            for (int i = 0; i < 0x10; i++) {

                vram[currVramBank][destAddr.getWord()] = readByte(sourceAddr);
                destAddr.inc();
                sourceAddr.inc();
            }

            io[HDMA5]--;
            io[HDMA5] &= 0xFF;

            return 8;
        }

        return 0;
    }
}
