package application;

public enum Interrupt {

	V_BLANK(0x1, 0x40), LCD_STAT(0x2, 0x48), TIMER(0x4, 0x50), SERIAL(0x8, 0x58), JOYPAD(
			0x10, 0x60);

	private int mask;
	private int vector;

	Interrupt(int mask, int vector) {

		this.mask = mask;
		this.vector = vector;
	}

	public int getMask() {

		return mask;
	}

	public void setMask(int mask) {

		this.mask = mask;
	}

	public int getVector() {

		return vector;
	}

	public void setVector(int vector) {

		this.vector = vector;
	}
}
