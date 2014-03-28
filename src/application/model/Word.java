package application.model;

public class Word extends Number implements Comparable<Word> {

    private static final long serialVersionUID = 1984488711894273683L;

    private int word;

    public Word() {

    }

    public Word(int word) {

        this.word = word & 0xFFFF;
    }

    public int getWord() {

        return word & 0xFFFF;
    }

    public void setWord(int word) {

        this.word = word & 0xFFFF;
    }

    public int getHighByte() {

        return ((word >> 8) & 0xFF);
    }

    public void setHighByte(int b) {

        this.word = (((b & 0xFF) << 8) | (word & 0xFF));
    }

    public int getLowByte() {

        return (word & 0xFF);
    }

    public void setLowByte(int b) {

        this.word = ((word & 0xff00) | (b & 0xff));
    }

    public void inc() {

        this.word++;
        this.word &= 0xFFFF;
    }

    public void dec() {

        this.word--;
        this.word &= 0xFFFF;
    }

    @Override
    public double doubleValue() {

        return word;
    }

    @Override
    public float floatValue() {

        return word;
    }

    @Override
    public int intValue() {

        return word;
    }

    @Override
    public long longValue() {

        return word;
    }

    @Override
    public int compareTo(Word word) {

        return this.word - word.getWord();
    }

}
