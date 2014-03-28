package application.sound.channel;

public class BaseChannel {

    private boolean on;
    private boolean count;
    private int length;
    private float freq;
    private int index;
    private int[] wave;

    public boolean isOn() {

        return on;
    }

    public void setOn(boolean on) {

        this.on = on;
    }

    public boolean isCount() {

        return count;
    }

    public void setCount(boolean count) {

        this.count = count;
    }

    public int getLength() {

        return length;
    }

    public void setLength(int length) {

        this.length = length;
    }

    public void decLength() {

        this.length--;
    }

    public float getFreq() {

        return freq;
    }

    public void setFreq(float freq) {

        this.freq = freq;
    }

    public int getIndex() {

        return index;
    }

    public void setIndex(int index) {

        this.index = index;
    }

    public void incIndex() {

        this.index++;
    }

    public int[] getWave() {

        return wave;
    }

    public void setWave(int[] wave) {

        this.wave = wave;
    }
}
