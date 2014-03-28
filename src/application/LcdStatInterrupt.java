package application;

public enum LcdStatInterrupt {

	H_BLANK(0x08), V_BLANK(0x10), OAM(0x20), LYC_LY(0x40);

	private int mask;

	LcdStatInterrupt(int mask) {

		this.mask = mask;
	}

	public int getMask() {

		return mask;
	}

	public void setMask(int mask) {

		this.mask = mask;
	}

}
