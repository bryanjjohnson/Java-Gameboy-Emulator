package application.sound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import application.MemoryMap;
import application.sound.channel.Envelope;
import application.sound.channel.NoiseChannel;
import application.sound.channel.SquareWaveChannel;
import application.sound.channel.WaveChannel;

public class Sound {

    private static final int SAMPLE_RATE = 44100;
    private static final int SAMPLE_SIZE = 2;

    private MemoryMap mmu = MemoryMap.getInstance();

    private SourceDataLine line;

    private byte[][] soundBuffer = new byte[4][750];
    private byte[] soundBufferMix;
    private int soundBufferIndex;

    private int soundTimer;

    private SquareWaveChannel channel1;
    private SquareWaveChannel channel2;
    private WaveChannel channel3;
    private NoiseChannel channel4;

    public Sound() throws LineUnavailableException {

        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, SAMPLE_RATE, 8, 2, 2, SAMPLE_RATE, true);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {

            System.out.println("Line matching " + info + " is not supported.");
        }

        line = (SourceDataLine) AudioSystem.getLine(info);

        line.open(format);

        soundBufferMix = new byte[line.getBufferSize()];

        channel1 = new SquareWaveChannel();
        channel2 = new SquareWaveChannel();
        channel3 = new WaveChannel();
        channel4 = new NoiseChannel();
    }

    public void startAudio() {

        soundTimer = 0;
        line.start();

        channel1.setOn(false);
        channel2.setOn(false);
        channel3.setOn(false);
        channel4.setOn(false);
        soundBufferIndex = 0;
    }

    public void updateSound(int cyclesRun) {

        initChannels();

        soundTimer += cyclesRun;
        if (soundTimer >= 93)
        {
            soundTimer -= 93;

            if (mmu.isAllSoundOn()) {

                updateChannel1();
                updateChannel2();
                updateChannel3();
                updateChannel4();

                mixSound();

            } else {

                soundBufferMix[(soundBufferIndex * 2)] = (byte) 0;
                soundBufferMix[(soundBufferIndex * 2) + 1] = (byte) 0;
            }

            soundBufferIndex++;

            if (soundBufferIndex >= 750) {

                int numSamples;

                if (1500 >= line.available() * 2) {

                    numSamples = line.available() * 2;
                } else {

                    numSamples = 1500;
                }

                line.write(soundBufferMix, 0, numSamples);

                soundBufferIndex = 0;
            }

        }

    }

    private void initChannels() {

        initChannel1();
        initChannel2();
        initChannel3();
        initChannel4();
    }

    private void initChannel1() {

        if (mmu.isSoundReset(1)) {

            mmu.removeSoundReset(1);
            mmu.setSoundOn(1);

            int nr10 = mmu.io[mmu.NR10];
            int nr11 = mmu.io[mmu.NR11];
            int nr12 = mmu.io[mmu.NR12];
            int nr13 = mmu.io[mmu.NR13];
            int nr14 = mmu.io[mmu.NR14];

            channel1.setOn(true);
            channel1.setWaveDuty((nr11 >> 6) & 0x3);
            channel1.setIndex(0);

            channel1.setGbFreq((nr13 | ((nr14 & 0x7) << 8)) & 0x7FF);
            channel1.setFreq(131072 / (2048 - channel1.getGbFreq()));

            if ((nr14 & 0x40) == 0x40) // stop output at length
            {
                channel1.setCount(true);
                channel1.setLength(((64 - (nr11 & 0x3F)) * SAMPLE_RATE) / 256);
            }
            else
            {
                channel1.setCount(false);
            }

            Envelope volume = new Envelope();
            volume.setBase((nr12 >> 4) & 0x0F);
            volume.setDirection((nr12 & 0x8) == 0x8 ? 1 : 0);
            volume.setStepLength((nr12 & 0x7) * SAMPLE_RATE / 64);
            volume.setIndex(volume.getStepLength());
            channel1.setVolume(volume);

            channel1.setSweepLength(((nr10 >> 4) & 0x7) * SAMPLE_RATE / 128);
            channel1.setSweepIndex(channel1.getSweepLength());
            channel1.setSweepDirection((nr10 & 0x8) == 0x8 ? -1 : 1);
            channel1.setSweepShift(nr10 & 0x7);

        }

    }

    private void updateChannel1() {

        if (channel1.isOn())
        {
            channel1.incIndex();

            int i = (int) ((32 * channel1.getFreq() * channel1.getIndex()) / SAMPLE_RATE) % 32;
            int value = channel1.getWave()[i];
            soundBuffer[0][soundBufferIndex] = (byte) (value * channel1.getVolume().getBase());

            if (channel1.isCount() && channel1.getLength() > 0)
            {
                channel1.decLength();
                if (channel1.getLength() == 0)
                {
                    channel1.setOn(false);
                    mmu.setSoundOff(1);
                }
            }

            channel1.getVolume().handleSweep();

            if (channel1.getSweepIndex() > 0 && channel1.getSweepLength() > 0)
            {
                channel1.decSweepIndex();

                if (channel1.getSweepIndex() == 0)
                {
                    channel1.setSweepIndex(channel1.getSweepLength());
                    channel1.setGbFreq(channel1.getGbFreq() + (channel1.getGbFreq() >> channel1.getSweepShift()) * channel1.getSweepDirection());
                    if (channel1.getGbFreq() > 2047)
                    {
                        channel1.setOn(false);
                        mmu.setSoundOff(1);
                    }
                    else
                    {
                        mmu.io[mmu.NR13] = channel1.getGbFreq() & 0xFF;
                        mmu.io[mmu.NR14] = (mmu.io[mmu.NR14] & 0xF8) | ((channel1.getGbFreq() >> 8) & 0x7);
                        channel1.setFreq(131072 / (2048 - channel1.getGbFreq()));
                    }
                }
            }
        }
    }

    private void initChannel2() {

        if (mmu.isSoundReset(2)) {

            mmu.removeSoundReset(2);
            mmu.setSoundOn(2);

            int nr21 = mmu.io[mmu.NR21];
            int nr22 = mmu.io[mmu.NR22];
            int nr23 = mmu.io[mmu.NR23];
            int nr24 = mmu.io[mmu.NR24];

            channel2.setOn(true);
            channel2.setWaveDuty((nr21 >> 6) & 0x3);
            channel2.setIndex(0);

            int freqX = nr23 | ((nr24 & 0x7) << 8);
            channel2.setFreq(131072 / (2048 - freqX));

            if ((nr24 & 0x40) == 0x40) // stop output at length
            {
                channel2.setCount(true);
                channel2.setLength(((64 - (nr21 & 0x3F)) * SAMPLE_RATE) / 256);
            }
            else
            {
                channel2.setCount(false);
            }

            Envelope volume = new Envelope();
            volume.setBase((nr22 >> 4) & 0x0F);
            volume.setDirection((nr22 & 0x8) == 0x8 ? 1 : 0);
            volume.setStepLength((nr22 & 0x7) * SAMPLE_RATE / 64);
            volume.setIndex(volume.getStepLength());
            channel2.setVolume(volume);

        }
    }

    private void updateChannel2() {

        if (channel2.isOn())
        {
            channel2.incIndex();

            int i = (int) ((32 * channel2.getFreq() * channel2.getIndex()) / SAMPLE_RATE) % 32;
            int value = channel2.getWave()[i];
            soundBuffer[1][soundBufferIndex] = (byte) (value * channel2.getVolume().getBase());

            if (channel2.isCount() && channel2.getLength() > 0)
            {
                channel2.decLength();
                if (channel2.getLength() == 0)
                {
                    channel2.setOn(false);
                    mmu.setSoundOff(2);
                }
            }

            channel2.getVolume().handleSweep();
        }
    }

    private void initChannel3() {

        if (mmu.isSoundReset(3)) {

            mmu.removeSoundReset(3);
            mmu.setSoundOn(3);

            channel3.setIndex(0);

            int nr30 = mmu.io[mmu.NR30];
            int nr31 = mmu.io[mmu.NR31];
            int nr33 = mmu.io[mmu.NR33];
            int nr34 = mmu.io[mmu.NR34];

            channel3.setOn((nr30 & 0x80) == 0x80);

            int freqX3 = nr33 | ((nr34 & 0x7) << 8);
            channel3.setFreq(65536 / (2048 - freqX3));

            int[] channel3wav = new int[32];
            for (int i = 0x30; i < 0x40; i++)
            {
                channel3wav[((i - 0x30) * 2)] = ((mmu.io[i] >> 4) & 0xF);
                channel3wav[((i - 0x30) * 2) + 1] = (mmu.io[i] & 0xF);
            }
            channel3.setWave(channel3wav);

            if ((nr34 & 0x40) == 0x40) // stop output at length
            {
                channel3.setCount(true);
                channel3.setLength((256 - nr31) * SAMPLE_RATE / 256);
            }
            else
            {
                channel3.setCount(false);
            }
        }

    }

    private void updateChannel3() {

        if (channel3.isOn())
        {
            int nr30 = mmu.io[mmu.NR30];
            int nr32 = mmu.io[mmu.NR32];

            channel3.incIndex();

            int i = (int) ((32 * channel3.getFreq() * channel3.getIndex()) / 44100) % 32;
            int value = channel3.getWave()[i];
            if ((nr32 & 0x60) != 0x0) {

                value >>= (((nr32 >> 5) & 0x3) - 1);
            } else {

                value = 0;
            }
            value <<= 1;
            if ((nr30 & 0x80) == 0x80) {
                soundBuffer[2][soundBufferIndex] = (byte) (value - 0xF);
            } else {
                soundBuffer[2][soundBufferIndex] = 0;
            }

            if (channel3.isCount() && channel3.getLength() > 0)
            {
                channel3.decLength();
                if (channel3.getLength() == 0)
                {
                    channel3.setOn(false);
                    mmu.setSoundOff(3);
                }
            }
        }
    }

    private void initChannel4() {

        if (mmu.isSoundReset(4)) {

            mmu.removeSoundReset(4);
            mmu.setSoundOn(4);

            int nr41 = mmu.io[mmu.NR41];
            int nr42 = mmu.io[mmu.NR42];
            int nr43 = mmu.io[mmu.NR43];
            int nr44 = mmu.io[mmu.NR44];

            channel4.setOn(true);
            channel4.setIndex(0);

            if ((nr44 & 0x40) == 0x40) // stop output at length
            {
                channel4.setCount(true);
                channel4.setLength((64 - (nr41 & 0x3F)) * SAMPLE_RATE / 256);
            }
            else
            {
                channel4.setCount(false);
            }

            Envelope volume = new Envelope();
            volume.setBase((nr42 >> 4) & 0x0F);
            volume.setDirection((nr42 & 0x8) == 0x48 ? 1 : 0);
            volume.setStepLength((nr42 & 0x7) * SAMPLE_RATE / 64);
            volume.setIndex(volume.getStepLength());
            channel4.setVolume(volume);

            channel4.setShiftFreq(((nr43 >> 4) & 0xF) + 1);
            channel4.setCounterStep((nr43 & 0x8) == 0x8 ? 1 : 0);
            channel4.setDivRatio(nr43 & 0x7);
            if (channel4.getDivRatio() == 0)
                channel4.setDivRatio(0.5F);
            channel4.setFreq((int) (524288 / channel4.getDivRatio()) >> channel4.getShiftFreq());
        }

    }

    private void updateChannel4() {

        if (channel4.isOn())
        {
            channel4.incIndex();

            byte value = 0;
            if (channel4.getCounterStep() == 1)
            {
                int i = (int) ((channel4.getFreq() * channel4.getIndex()) / SAMPLE_RATE) % 0x7F;
                value = (byte) ((NoiseChannel.noise7[i >> 3] >> (i & 0x7)) & 0x1);
            }
            else
            {
                int i = (int) ((channel4.getFreq() * channel4.getIndex()) / SAMPLE_RATE) % 0x7FFF;
                value = (byte) ((NoiseChannel.noise15[i >> 3] >> (i & 0x7)) & 0x1);
            }
            soundBuffer[3][soundBufferIndex] = (byte) ((value * 2 - 1) * channel4.getVolume().getBase());

            if (channel4.isCount() && channel4.getLength() > 0)
            {
                channel4.decLength();
                if (channel4.getLength() == 0)
                {
                    channel4.setOn(false);
                    mmu.setSoundOff(4);
                }
            }

            channel4.getVolume().handleSweep();

        }
    }

    private void mixSound() {

        int leftAmp = 0;
        if (mmu.isSoundToTerminal(1, 2) && channel1.isOn())
            leftAmp += soundBuffer[0][soundBufferIndex];
        if (mmu.isSoundToTerminal(2, 2) && channel2.isOn())
            leftAmp += soundBuffer[1][soundBufferIndex];
        if (mmu.isSoundToTerminal(3, 2) && channel3.isOn())
            leftAmp += soundBuffer[2][soundBufferIndex];
        if (mmu.isSoundToTerminal(4, 2) && channel4.isOn())
            leftAmp += soundBuffer[3][soundBufferIndex];

        leftAmp *= mmu.getSoundLevel(2);
        leftAmp /= 4;

        int rightAmp = 0;
        if (mmu.isSoundToTerminal(1, 1) && channel1.isOn())
            rightAmp += soundBuffer[0][soundBufferIndex];
        if (mmu.isSoundToTerminal(2, 1) && channel2.isOn())
            rightAmp += soundBuffer[1][soundBufferIndex];
        if (mmu.isSoundToTerminal(3, 1) && channel3.isOn())
            rightAmp += soundBuffer[2][soundBufferIndex];
        if (mmu.isSoundToTerminal(4, 1) && channel4.isOn())
            rightAmp += soundBuffer[3][soundBufferIndex];

        rightAmp *= mmu.getSoundLevel(1);
        rightAmp /= 4;

        if (leftAmp > 127)
            leftAmp = 127;
        if (rightAmp > 127)
            rightAmp = 127;
        if (leftAmp < -127)
            leftAmp = -127;
        if (rightAmp < -127)
            rightAmp = -127;

        soundBufferMix[(soundBufferIndex * 2)] = (byte) leftAmp;
        soundBufferMix[(soundBufferIndex * 2) + 1] = (byte) rightAmp;

    }

}
