package test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import application.model.Word;

public class WordTest {

    @Test
    public void word() {

        int testInt = 0x1234;

        Word word = new Word(testInt);

        assertEquals(testInt, word.getWord());
        assertEquals(0x12, word.getHighByte());
        assertEquals(0x34, word.getLowByte());
    }

    @Test
    public void wordChangeHighByte() {

        int testInt = 0x1234;

        Word word = new Word(testInt);

        word.setHighByte(0x56);

        assertEquals(0x5634, word.getWord());
        assertEquals(0x56, word.getHighByte());
        assertEquals(0x34, word.getLowByte());
    }

    @Test
    public void wordChangeLowByte() {

        int testInt = 0x1234;

        Word word = new Word(testInt);

        word.setLowByte(0x56);

        assertEquals(0x1256, word.getWord());
        assertEquals(0x12, word.getHighByte());
        assertEquals(0x56, word.getLowByte());
    }

    @Test
    public void wordIncHighByte() {

        int testInt = 0xFF00;

        Word word = new Word(testInt);

        int temp = word.getHighByte() + 1;

        assertEquals(0x100, temp);

        word.setHighByte(word.getHighByte() + 1);

        assertEquals(0x0, word.getWord());
    }

}
