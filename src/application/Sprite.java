package application;

public class Sprite implements Comparable<Sprite> {

    private int index;
    private int x;
    private int y;
    private int tileNum;
    private int attributeFlags;

    public Sprite() {

    }

    public Sprite(int index) {

        this.index = index;
    }

    public int getIndex() {

        return index;
    }

    public void setIndex(int index) {

        this.index = index;
    }

    public int getX() {

        return x;
    }

    public void setX(int x) {

        this.x = x;
    }

    public int getY() {

        return y;
    }

    public void setY(int y) {

        this.y = y;
    }

    public int getTileNum() {

        return tileNum;
    }

    public void setTileNum(int tileNum) {

        this.tileNum = tileNum;
    }

    public int getAttributeFlags() {

        return attributeFlags;
    }

    public void setAttributeFlags(int attributeFlags) {

        this.attributeFlags = attributeFlags;
    }

    public boolean isObjAboveBG() {

        return (attributeFlags & 0x80) == 0x0;
    }

    public boolean yFlip() {

        return (attributeFlags & 0x40) == 0x40;
    }

    public boolean xFlip() {

        return (attributeFlags & 0x20) == 0x20;
    }

    public int gbPalNum() {

        return (attributeFlags & 0x10) >> 4;
    }

    public int getTileBank() {

        return (attributeFlags & 0x8) >> 3;
    }

    public int cbgPalNum() {

        return attributeFlags & 0x7;
    }

    @Override
    public int compareTo(Sprite sprite) {

        if (this.x == sprite.getX()) {

            return sprite.getIndex() - this.index;
        } else {

            return sprite.getX() - this.x;
        }
    }

}
