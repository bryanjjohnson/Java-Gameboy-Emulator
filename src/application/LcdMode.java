package application;

public enum LcdMode {

    H_BLANK(0x0, "H-Blank", 204), V_BLANK(0x1, "V-Blank", 456), OAM_RAM(0x2,
            "Searching OAM-RAM", 80), DATA_TRANSFER(0x3,
            "Transfering Data to LCD Driver", 172);

    private int index;
    private String name;
    private int cycles;

    LcdMode(int index, String name, int cycles) {

        this.index = index;
        this.name = name;
        this.setCycles(cycles);
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

    public int getCycles() {

        return cycles;
    }

    public void setCycles(int cycles) {

        this.cycles = cycles;
    }

    public static LcdMode getByIndex(int index) {

        for (LcdMode size : values()) {

            if (size.getIndex() == index) {

                return size;
            }
        }

        return null;
    }
}
