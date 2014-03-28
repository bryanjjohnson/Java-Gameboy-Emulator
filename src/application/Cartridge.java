package application;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Calendar;

import application.CartridgeType.Type;

import com.google.common.io.Files;

public class Cartridge {

    private String title;
    private String fileName;
    private CartridgeType type;
    private CartridgeSize size;
    private Mode mode;
    private boolean cgbMode = false;
    private boolean sgbMode = false;
    private int[][] romBanks;
    private int currRomBank;
    private boolean romModeEnabled;
    private boolean ramEnabled;
    private int ramSize;
    private int[] ram;
    private int currRamBank;
    private boolean rtcLatchEnabled;
    private Calendar startTime;
    private int rtcLatchSeconds;
    private int rtcLatchMinutes;
    private int rtcLatchHours;
    private int rtcLatchDays;
    private boolean rtcCarry;
    private boolean rtcHalt;
    private long rtcHaltTime;

    public boolean loadRom(File romFile) {

        fileName = Files.getNameWithoutExtension(romFile.getName());

        System.out.println("Loading Rom: " + fileName);

        try {

            byte[] data = Files.toByteArray(romFile);

            loadHeader(data);

        } catch (Exception e) {

            // TODO Auto-generated catch block
            e.printStackTrace();

            return false;
        }

        return true;
    }

    private void loadHeader(byte[] data) throws UnsupportedEncodingException {

        title = new String(Arrays.copyOfRange(data, 0x134, 0x0143),
                StandardCharsets.US_ASCII);
        System.out.println(title);

        String manufacturerCode = new String(Arrays.copyOfRange(data, 0x13F,
                0x0142), StandardCharsets.US_ASCII);
        System.out.println(manufacturerCode);

        cgbMode = ((data[0x143] & 0x80) == 0x80);
        System.out.println("CGB Flag: " + cgbMode);

        String licenseeCode = new String(
                Arrays.copyOfRange(data, 0x144, 0x0145),
                StandardCharsets.US_ASCII);
        System.out.println(licenseeCode);

        sgbMode = ((data[0x0146] & 0x3) == 0x3);
        System.out.println("SGB Flag: " + sgbMode);

        type = CartridgeType.getByIndex(data[0x0147]);
        System.out.println(type.getName());

        size = CartridgeSize.getByIndex(data[0x0148]);
        System.out.println(size.getName());

        switch (data[0x0149]) {

        case 0x00:
            ramSize = 0;
            break;
        case 0x01:
            ramSize = 0x800;
            break;
        case 0x02:
            ramSize = 0x2000;
            break;
        case 0x03:
            ramSize = 0x8000;
            break;
        case 0x04:
            ramSize = 0x20000;
            break;
        }
        if (type.getType().equals(CartridgeType.Type.ROM)) {

            ramSize = 0x2000;
        } else if (type.getType().equals(CartridgeType.Type.MBC2)) {

            ramSize = 512;
        }
        System.out.println("Ram Size: " + ramSize);

        ram = new int[ramSize];
        for (int i = 0; i < ramSize; i++)
            ram[i] = 0xFF;

        ramEnabled = true;
        currRamBank = 0;

        romBanks = new int[size.getNumBanks()][0x4000];

        for (int b = 0; b < size.getNumBanks(); b++) {
            for (int i = 0; i < 0x4000; i++) {

                romBanks[b][i] = data[(0x4000 * b) + i] & 0xFF;
            }
        }
        romModeEnabled = true;
        currRomBank = 1;

        System.out.println("Destination Code: " + data[0x014A]);
        System.out.println("Old Licensee Code: " + data[0x014B]);
        System.out.println("Version number: " + data[0x014C]);

        startTime = Calendar.getInstance();

        if (type.isBatteryUsed()) {

            loadRAM();
        }

    }

    public int readRom(int bankNum, int addr) {

        return romBanks[bankNum][addr] & 0xFF;
    }

    public void writeRom(int bankNum, int addr, int val) {

        romBanks[bankNum][addr] = (val & 0xFF);
    }

    public int readRam(int addr) {

        return ram[addr] & 0xFF;
    }

    public void writeRam(int addr, int val) {

        ram[addr] = (val & 0xFF);
    }

    public void enableRam() {

        ramEnabled = true;
    }

    public void disableRam() {

        ramEnabled = false;
    }

    public boolean isRamEnabled() {

        return ramEnabled;
    }

    public Type getType() {

        return type.getType();
    }

    public void setToRomMode() {

        romModeEnabled = true;
    }

    public void setToRamMode() {

        romModeEnabled = false;
    }

    public boolean isRomMode() {

        return romModeEnabled;
    }

    public int getCurrRomBank() {

        return currRomBank;
    }

    public void setCurrRomBank(int bankNum) {

        currRomBank = bankNum;
    }

    public int getCurrRamBank() {

        return currRamBank;
    }

    public void setCurrRamBank(int bankNum) {

        currRamBank = bankNum;
    }

    public boolean isCGB() {

        return cgbMode;
    }

    public boolean isSGB() {

        return sgbMode;
    }

    public boolean isRtcLatchEnabled() {

        return rtcLatchEnabled;
    }

    public void setRtcLatchEnabled(boolean rtcLatchEnabled) {

        this.rtcLatchEnabled = rtcLatchEnabled;
    }

    public Calendar getStartTime() {

        return startTime;
    }

    public void setStartTime(Calendar startTime) {

        this.startTime = startTime;
    }

    public long getRtcLatch() {

        long time = (1000 * rtcLatchSeconds) + (1000 * 60 * rtcLatchMinutes) + (1000 * 60 * 60 * rtcLatchHours) + (1000 * 60 * 60 * 24 * rtcLatchDays);
        if (rtcCarry) {
            time += (1000 * 60 * 60 * 24 * 511);
        }
        return time;
    }

    public void setRtcLatch(long rtcLatch) {

        rtcLatchSeconds = ((int) rtcLatch / (1000)) % 60;
        rtcLatchMinutes = ((int) rtcLatch / (1000 * 60)) % 60;
        rtcLatchHours = ((int) rtcLatch / (1000 * 60 * 60)) % 24;
        rtcLatchDays = ((int) rtcLatch / (1000 * 60 * 60 * 24));
        rtcCarry = rtcLatchDays > 0x1FF;
        rtcLatchDays %= 0x200;
    }

    public int getRtcLatchSeconds() {

        return rtcLatchSeconds;
    }

    public void setRtcLatchSeconds(int rtcLatchSeconds) {

        this.rtcLatchSeconds = rtcLatchSeconds;
    }

    public int getRtcLatchMinutes() {

        return rtcLatchMinutes;
    }

    public void setRtcLatchMinutes(int rtcLatchMinutes) {

        this.rtcLatchMinutes = rtcLatchMinutes;
    }

    public int getRtcLatchHours() {

        return rtcLatchHours;
    }

    public void setRtcLatchHours(int rtcLatchHours) {

        this.rtcLatchHours = rtcLatchHours;
    }

    public int getRtcLatchDays() {

        return rtcLatchDays;
    }

    public void setRtcLatchDays(int rtcLatchDays) {

        this.rtcLatchDays = rtcLatchDays;
        this.rtcCarry = rtcLatchDays > 0x1FF;
    }

    public boolean isRtcCarry() {

        return rtcCarry;
    }

    public void setRtcCarry(boolean rtcCarry) {

        this.rtcCarry = rtcCarry;
    }

    public boolean isRtcHalt() {

        return rtcHalt;
    }

    public void setRtcHalt(boolean rtcHalt) {

        this.rtcHalt = rtcHalt;
    }

    public long getRtcHaltTime() {

        return rtcHaltTime;
    }

    public void setRtcHaltTime(long rtcHaltTime) {

        this.rtcHaltTime = rtcHaltTime;
    }

    public enum Mode {
        CGB_SUPPORT, CGB_ONLY, GB;
    }

    public void loadRAM() {

        File ramFile = new File(System.getProperty("user.dir") + "/saves/" + fileName + ".sav");

        if (ramFile.exists() && !ramFile.isDirectory()) {

            System.out.println("Loading RAM: " + ramFile.getAbsolutePath());

            try {

                byte[] data = Files.toByteArray(ramFile);

                for (int i = 0; i < ram.length; i++) {

                    ram[i] = data[i] & 0xFF;
                }

                if (type.getType().equals(Type.MBC3)) {

                    startTime.setTimeInMillis(bytesToLong(Arrays.copyOfRange(data, ram.length, ram.length + 8)));

                    setRtcLatch(bytesToLong(Arrays.copyOfRange(data, ram.length + 8, ram.length + 16)));

                    startTime.setTimeInMillis(Calendar.getInstance().getTimeInMillis() + startTime.getTimeInMillis() - getRtcLatch());
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    public void saveRAM() {

        if (type.isBatteryUsed()) {

            try {

                String file = System.getProperty("user.dir") + "\\saves\\" + fileName + ".sav";
                System.out.println("Saving RAM: " + file);
                File ramFile = new File(file);

                if (!ramFile.exists()) {

                    ramFile.getParentFile().mkdirs();
                    ramFile.createNewFile();

                }

                int extra = 0;
                if (type.getType().equals(Type.MBC3)) {
                    extra += 16;
                }

                byte[] data = new byte[ram.length + extra];
                for (int i = 0; i < ram.length; i++) {

                    data[i] = (byte) (ram[i] & 0xFF);
                }

                if (type.getType().equals(Type.MBC3)) {

                    System.arraycopy(longToBytes(startTime.getTimeInMillis()), 0, data, ram.length, 8);

                    System.arraycopy(longToBytes(getRtcLatch()), 0, data, ram.length + 8, 8);
                }

                Files.write(data, ramFile);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public byte[] longToBytes(long x) {

        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(x);

        return buffer.array();
    }

    public long bytesToLong(byte[] bytes) {

        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(bytes);
        buffer.flip();// need flip

        return buffer.getLong();
    }
}
