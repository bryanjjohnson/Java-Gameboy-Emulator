package application.sound.channel;

public class SquareWaveChannel extends BaseChannel {

    private Envelope volume;
    private int gbFreq;
    private int sweepIndex;
    private int sweepLength;
    private int sweepDirection;
    private int sweepShift;

    public Envelope getVolume() {

        return volume;
    }

    public void setVolume(Envelope volume) {

        this.volume = volume;
    }

    public int getGbFreq() {

        return gbFreq;
    }

    public void setGbFreq(int gbFreq) {

        this.gbFreq = gbFreq;
    }

    public int getSweepIndex() {

        return sweepIndex;
    }

    public void setSweepIndex(int sweepIndex) {

        this.sweepIndex = sweepIndex;
    }

    public void decSweepIndex() {

        this.sweepIndex--;
    }

    public int getSweepLength() {

        return sweepLength;
    }

    public void setSweepLength(int sweepLength) {

        this.sweepLength = sweepLength;
    }

    public int getSweepDirection() {

        return sweepDirection;
    }

    public void setSweepDirection(int sweepDirection) {

        this.sweepDirection = sweepDirection;
    }

    public int getSweepShift() {

        return sweepShift;
    }

    public void setSweepShift(int sweepShift) {

        this.sweepShift = sweepShift;
    }

    public void setWaveDuty(int waveDuty) {

        this.setWave(soundWavePattern[waveDuty]);
    }

    public final static int[][] soundWavePattern =
    {
            {
                    1, 1, 1, 1,
                    -1, -1, -1, -1,
                    -1, -1, -1, -1,
                    -1, -1, -1, -1,
                    -1, -1, -1, -1,
                    -1, -1, -1, -1,
                    -1, -1, -1, -1,
                    -1, -1, -1, -1 },
            {
                    1, 1, 1, 1,
                    1, 1, 1, 1,
                    -1, -1, -1, -1,
                    -1, -1, -1, -1,
                    -1, -1, -1, -1,
                    -1, -1, -1, -1,
                    -1, -1, -1, -1,
                    -1, -1, -1, -1 },
            {
                    1, 1, 1, 1,
                    1, 1, 1, 1,
                    1, 1, 1, 1,
                    1, 1, 1, 1,
                    -1, -1, -1, -1,
                    -1, -1, -1, -1,
                    -1, -1, -1, -1,
                    -1, -1, -1, -1 },
            {
                    1, 1, 1, 1,
                    1, 1, 1, 1,
                    1, 1, 1, 1,
                    1, 1, 1, 1,
                    1, 1, 1, 1,
                    1, 1, 1, 1,
                    -1, -1, -1, -1,
                    -1, -1, -1, -1 }
    };

}
