package application;

public enum Key {

    RIGHT(KeyType.DIRECTION, 0x01),
    LEFT(KeyType.DIRECTION, 0x02),
    UP(KeyType.DIRECTION, 0x04),
    DOWN(KeyType.DIRECTION, 0x08),
    A(KeyType.BUTTON, 0x01),
    B(KeyType.BUTTON, 0x02),
    SELECT(KeyType.BUTTON, 0x04),
    START(KeyType.BUTTON, 0x08);

    private KeyType keyType;
    private int mask;

    Key(KeyType type, int mask) {

        this.setKeyType(type);
        this.setMask(mask);
    }

    public KeyType getKeyType() {

        return keyType;
    }

    public void setKeyType(KeyType keyType) {

        this.keyType = keyType;
    }

    public int getMask() {

        return mask;
    }

    public void setMask(int mask) {

        this.mask = mask;
    }

    public enum KeyType {

        BUTTON, DIRECTION;
    }

}
