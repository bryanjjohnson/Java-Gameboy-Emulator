package application.sound.channel;

public class Envelope {

    private int base;
    private int direction;
    private int stepLength;
    private int index;

    public int getBase() {

        return base;
    }

    public void setBase(int base) {

        this.base = base;
    }

    public int getDirection() {

        return direction;
    }

    public void setDirection(int direction) {

        this.direction = direction;
    }

    public int getStepLength() {

        return stepLength;
    }

    public void setStepLength(int stepLength) {

        this.stepLength = stepLength;
    }

    public int getIndex() {

        return index;
    }

    public void setIndex(int index) {

        this.index = index;
    }

    public void handleSweep() {

        if (index > 0)
        {
            index--;

            if (index == 0)
            {
                index = stepLength;

                if (direction == 1 && base < 0xF) {

                    base++;
                } else if (direction == 0 && base > 0) {

                    base--;
                }
            }
        }

    }
}
