package application;

public enum CartridgeType {

    ROM(0x0, "ROM ONLY", Type.ROM, false, false),
    MBC1(0x01, "MBC1", Type.MBC1, false, false),
    MBC1_RAM(0x02, "MBC1+RAM", Type.MBC1, true, false),
    MBC1_RAM_BATTERY(0x03, "MBC1+RAM+BATTERY", Type.MBC1, true, true),
    MBC2(0x05, "MBC2", Type.MBC2, false, false),
    MBC2_BATTERY(0x06, "MBC2+BATTERY", Type.MBC2, false, true),
    ROM_RAM(0x08, "ROM+RAM", Type.ROM, true, false),
    ROM_RAM_BATTERY(0x09, "ROM+RAM+BATTERY", Type.ROM, true, true),
    MMM01(0x0B, "MMM01", Type.MMM01, false, false),
    MMM01_RAM(0x0C, "MMM01+RAM", Type.MMM01, true, false),
    MMM01_RAM_BATTERY(0x0D, "MMM01+RAM+BATTERY", Type.MMM01, true, true),
    MBC3_TIMER_BATTERY(0x0F, "MBC3+TIMER+BATTERY", Type.MBC3, false, true),
    MBC3_TIMER_RAM_BATTERY(0x10, "MBC3+TIMER+RAM+BATTERY", Type.MBC3, true, true),
    MBC3(0x11, "MBC3", Type.MBC3, false, false),
    MBC3_RAM(0x12, "MBC3+RAM", Type.MBC3, true, false),
    MBC3_RAM_BATTERY(0x13, "MBC3+RAM+BATTERY", Type.MBC3, true, true),
    MBC4(0x15, "MBC4", Type.MBC4, false, false),
    MBC4_RAM(0x16, "MBC4+RAM", Type.MBC4, true, false),
    MBC4_RAM_BATTERY(0x17, "MBC4+RAM+BATTERY", Type.MBC4, true, true),
    MBC5(0x19, "MBC5", Type.MBC5, false, false),
    MBC5_RAM(0x1A, "MBC5+RAM", Type.MBC5, true, false),
    MBC5_RAM_BATTERY(0x1B, "MBC5+RAM+BATTERY", Type.MBC5, true, true),
    MBC5_RUMBLE(0x1C, "MBC5+RUMBLE", Type.MBC5, false, false),
    MBC5_RUMBLE_RAM(0x1D, "MBC5+RUMBLE+RAM", Type.MBC5, true, false),
    MBC5_RUMBLE_RAM_BATTEYR(0x1E, "MBC5+RUMBLE+RAM+BATTERY", Type.MBC5, true, true),
    POCKET_CARMERA(0xFC, "POCKET CAMERA", Type.POCKET_CAMERA, false, false),
    BANDAU_TAMA5(0xFD, "BANDAI TAMA5", Type.BANDAI_TAMA5, false, false),
    HUC3(0xFE, "HuC3", Type.HuC3, false, false),
    HUC1_RAM_BATTERY(0xFF, "HuC1+RAM+BATTERY", Type.HuC1, true, true);

    private int index;
    private String name;
    private Type type;
    private boolean ramUsed;
    private boolean batteryUsed;

    CartridgeType(int index, String name, Type type, boolean ramUsed, boolean batteryUsed) {

        this.index = index;
        this.name = name;
        this.setType(type);
        this.ramUsed = ramUsed;
        this.batteryUsed = batteryUsed;
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

    public Type getType() {

        return type;
    }

    public void setType(Type type) {

        this.type = type;
    }

    public boolean isRamUsed() {

        return ramUsed;
    }

    public void setRamUsed(boolean ramUsed) {

        this.ramUsed = ramUsed;
    }

    public boolean isBatteryUsed() {

        return batteryUsed;
    }

    public void setBatteryUsed(boolean batteryUsed) {

        this.batteryUsed = batteryUsed;
    }

    public static CartridgeType getByIndex(int index) {

        for (CartridgeType type : values()) {

            if (type.getIndex() == index) {

                return type;
            }
        }

        return null;
    }

    public enum Type {
        ROM, MBC1, MMM01, MBC2, MBC3, MBC4, MBC5, POCKET_CAMERA, BANDAI_TAMA5, HuC1, HuC3;
    }
}
