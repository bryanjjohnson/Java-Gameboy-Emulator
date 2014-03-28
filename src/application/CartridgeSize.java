package application;

public enum CartridgeSize {

    KB_32(0x00, "32KByte (no ROM banking)", 2),
    KB_64(0x01, "64KByte (4 banks)", 4), KB_128(0x02, "128KByte (8 banks)", 8),
    KB_256(0x03, "256KByte (16 banks)", 16),
    KB_512(0x04, "512KByte (32 banks)", 32),
    MB_1(0x05, "1MByte (64 banks) - only 63 banks used by MBC1", 64),
    MB_2(0x06, "2MByte (128 banks) - only 125 banks used by MBC1", 128),
    MB_4(0x07, "4MByte (256 banks)", 256),
    MB_1_1(0x52, "1.1MByte (72 banks)", 72),
    MB_1_2(0x53, "1.2MByte (80 banks)", 80),
    MB_1_5(0x54, "1.5MByte (96 banks)", 96);

    private int index;
    private String name;
    private int numBanks;

    CartridgeSize(int index, String name, int numBanks) {

        this.index = index;
        this.name = name;
        this.numBanks = numBanks;
    }

    public int getIndex() {

        return index;
    }

    public void setIndex(int index) {

        this.index = index;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public int getNumBanks() {

        return numBanks;
    }

    public void setNumBanks(int numBanks) {

        this.numBanks = numBanks;
    }

    public static CartridgeSize getByIndex(int index) {

        for (CartridgeSize size : values()) {

            if (size.getIndex() == index) {

                return size;
            }
        }

        return null;
    }
}
