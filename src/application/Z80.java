package application;

import application.model.Word;

public class Z80 {

    private static final int Z_FLAG = 0x80;
    private static final int N_FLAG = 0x40;
    private static final int H_FLAG = 0x20;
    private static final int C_FLAG = 0x10;

    private Word AF;
    private Word BC;
    private Word DE;
    private Word HL;
    private Word SP;
    private Word PC;

    private boolean stop;
    private boolean halt;
    private boolean setIME;
    private boolean disableIME;
    private boolean IME;

    private int cyclesRun;

    private MemoryMap mmu;

    public Z80() {

        mmu = MemoryMap.getInstance();

        init();
    }

    public void init() {

        halt = false;

        setIME = false;
        disableIME = false;
        IME = false;

        PC = new Word(0x0100);
        SP = new Word(0xFFFE);
        AF = new Word(0x01B0);
        if (mmu.getCart().isCGB()) {

            AF.setHighByte(0x11);
        }
        BC = new Word(0x0013);
        DE = new Word(0x00D8);
        HL = new Word(0x014D);
    }

    public int execute() {

        cyclesRun = 0;

        if (!halt) {

            int opCode = mmu.readByte(PC);

            PC.inc();

            cyclesRun += cycles[opCode];
            executeOpCode(opCode);
        } else {

            cyclesRun += 4;
        }

        return cyclesRun;
    }

    public void checkInterrupts() {

        if (mmu.isInterruptTriggered()) {

            halt = false;

            if (IME) {

                IME = false;
                // PUSH PC
                SP.dec();
                mmu.writeByte(SP, PC.getHighByte());
                SP.dec();
                mmu.writeByte(SP, PC.getLowByte());

                if (mmu.isInterruptEnabled(Interrupt.V_BLANK)
                        && mmu.isInterruptSet(Interrupt.V_BLANK)) {

                    handleInterrupt(Interrupt.V_BLANK);

                } else if (mmu.isInterruptEnabled(Interrupt.LCD_STAT)
                        && mmu.isInterruptSet(Interrupt.LCD_STAT)) {

                    handleInterrupt(Interrupt.LCD_STAT);

                } else if (mmu.isInterruptEnabled(Interrupt.TIMER)
                        && mmu.isInterruptSet(Interrupt.TIMER)) {

                    handleInterrupt(Interrupt.TIMER);

                } else if (mmu.isInterruptEnabled(Interrupt.SERIAL)
                        && mmu.isInterruptSet(Interrupt.SERIAL)) {

                    handleInterrupt(Interrupt.SERIAL);

                } else if (mmu.isInterruptEnabled(Interrupt.JOYPAD)
                        && mmu.isInterruptSet(Interrupt.JOYPAD)) {

                    handleInterrupt(Interrupt.JOYPAD);

                }
            }
        }

        if (setIME) {
            setIME = false;
            IME = true;
        }

        if (disableIME) {
            disableIME = false;
            IME = false;
        }
    }

    private void handleInterrupt(Interrupt interrupt) {

        mmu.disableInterrupt(interrupt);
        PC.setWord(interrupt.getVector());
    }

    private void executeOpCode(int opCode) {

        Word addr = new Word();
        int carry = 0;
        int temp = 0;
        int F = AF.getLowByte();

        switch (opCode & 0xFF) {

        /* NOP */
        case 0x00:
        case 0x49:
        case 0xD3:
        case 0xDB:
        case 0xDD:
        case 0xE3:
        case 0xE4:
        case 0xEB:
        case 0xEC:
        case 0xED:
        case 0xF4:
        case 0xFC:
        case 0xFD:
            break;

        /* ADD A,n */
        case 0x80:
            addA(BC.getHighByte());
            break;

        case 0x81:
            addA(BC.getLowByte());
            break;

        case 0x82:
            addA(DE.getHighByte());
            break;

        case 0x83:
            addA(DE.getLowByte());
            break;

        case 0x84:
            addA(HL.getHighByte());
            break;

        case 0x85:
            addA(HL.getLowByte());
            break;

        case 0x86:
            addA(mmu.readByte(HL));
            break;

        case 0x87:
            addA(AF.getHighByte());
            break;

        case 0xC6:
            addA(mmu.readByte(PC));
            PC.inc();
            break;

        /* ADC A,n */
        case 0x88:
            adcA(BC.getHighByte());
            break;

        case 0x89:
            adcA(BC.getLowByte());
            break;

        case 0x8A:
            adcA(DE.getHighByte());
            break;

        case 0x8B:
            adcA(DE.getLowByte());
            break;

        case 0x8C:
            adcA(HL.getHighByte());
            break;

        case 0x8D:
            adcA(HL.getLowByte());
            break;

        case 0x8E:
            adcA(mmu.readByte(HL));
            break;

        case 0x8F:
            adcA(AF.getHighByte());
            break;

        case 0xCE:
            adcA(mmu.readByte(PC));
            PC.inc();
            break;

        /* ADD HL,n */
        case 0x09:
            addHL16bit(BC);
            break;

        case 0x19:
            addHL16bit(DE);
            break;

        case 0x29:
            addHL16bit(HL);
            break;

        case 0x39:
            addHL16bit(SP);
            break;

        /* ADD SP,n */
        case 0xE8:
            SP.setWord(addSP(mmu.readSignedByte(PC)));
            PC.inc();
            break;

        /* AND n */
        case 0xA0:
            andA(BC.getHighByte());
            break;

        case 0xA1:
            andA(BC.getLowByte());
            break;

        case 0xA2:
            andA(DE.getHighByte());
            break;

        case 0xA3:
            andA(DE.getLowByte());
            break;

        case 0xA4:
            andA(HL.getHighByte());
            break;

        case 0xA5:
            andA(HL.getLowByte());
            break;

        case 0xA6:
            andA(mmu.readByte(HL));
            break;

        case 0xA7:
            andA(AF.getHighByte());
            break;

        case 0xE6:
            andA(mmu.readByte(PC));
            PC.inc();
            break;

        /* CALL */
        case 0xC4:
            if ((F & Z_FLAG) == 0) {
                addr.setLowByte(mmu.readByte(PC));
                PC.inc();
                addr.setHighByte(mmu.readByte(PC));
                PC.inc();
                SP.dec();
                mmu.writeByte(SP, PC.getHighByte());
                SP.dec();
                mmu.writeByte(SP, PC.getLowByte());
                PC.setWord(addr.getWord());
            } else {
                PC.inc();
                PC.inc();
                cyclesRun -= 12;
            }
            break;

        case 0xCC:
            if ((F & Z_FLAG) != 0) {
                addr.setLowByte(mmu.readByte(PC));
                PC.inc();
                addr.setHighByte(mmu.readByte(PC));
                PC.inc();
                SP.dec();
                mmu.writeByte(SP, PC.getHighByte());
                SP.dec();
                mmu.writeByte(SP, PC.getLowByte());
                PC.setWord(addr.getWord());
            } else {
                PC.inc();
                PC.inc();
                cyclesRun -= 12;
            }
            break;

        case 0xCD:
            addr.setLowByte(mmu.readByte(PC));
            PC.inc();
            addr.setHighByte(mmu.readByte(PC));
            PC.inc();
            SP.dec();
            mmu.writeByte(SP, PC.getHighByte());
            SP.dec();
            mmu.writeByte(SP, PC.getLowByte());
            PC.setWord(addr.getWord());
            break;

        case 0xD4:
            if ((F & C_FLAG) == 0) {
                addr.setLowByte(mmu.readByte(PC));
                PC.inc();
                addr.setHighByte(mmu.readByte(PC));
                PC.inc();
                SP.dec();
                mmu.writeByte(SP, PC.getHighByte());
                SP.dec();
                mmu.writeByte(SP, PC.getLowByte());
                PC.setWord(addr.getWord());
            } else {
                PC.inc();
                PC.inc();
                cyclesRun -= 12;
            }
            break;

        case 0xDC:
            if ((F & C_FLAG) != 0) {
                addr.setLowByte(mmu.readByte(PC));
                PC.inc();
                addr.setHighByte(mmu.readByte(PC));
                PC.inc();
                SP.dec();
                mmu.writeByte(SP, PC.getHighByte());
                SP.dec();
                mmu.writeByte(SP, PC.getLowByte());
                PC.setWord(addr.getWord());
            } else {
                PC.inc();
                PC.inc();
                cyclesRun -= 12;
            }
            break;

        /* CCF */
        case 0x3F:
            F = (F & (Z_FLAG | C_FLAG)) ^ C_FLAG;
            AF.setLowByte(F);
            break;

        /* CP */
        case 0xB8:
            cpA(BC.getHighByte());
            break;

        case 0xB9:
            cpA(BC.getLowByte());
            break;

        case 0xBA:
            cpA(DE.getHighByte());
            break;

        case 0xBB:
            cpA(DE.getLowByte());
            break;

        case 0xBC:
            cpA(HL.getHighByte());
            break;

        case 0xBD:
            cpA(HL.getLowByte());
            break;

        case 0xBE:
            cpA(mmu.readByte(HL));
            break;

        case 0xBF:
            cpA(AF.getHighByte());
            break;

        case 0xFE:
            cpA(mmu.readByte(PC));
            PC.inc();
            break;

        /* CPL */
        case 0x2F:
            AF.setHighByte(AF.getHighByte() ^ 0xFF);
            F |= H_FLAG;
            F |= N_FLAG;
            AF.setLowByte(F);
            break;

        /* DDA */
        case 0x27:
            temp = AF.getHighByte();

            if ((F & N_FLAG) == 0) {
                if ((F & H_FLAG) == H_FLAG || (temp & 0xF) > 9)
                    temp += 0x06;

                if ((F & C_FLAG) == C_FLAG || temp > 0x9F)
                    temp += 0x60;
            } else {
                if ((F & H_FLAG) == H_FLAG)
                    temp = ((temp - 6) & 0xFF);

                if ((F & C_FLAG) == C_FLAG)
                    temp -= 0x60;
            }

            F &= ~(H_FLAG | Z_FLAG);

            if ((temp & 0x100) == 0x100)
                F |= C_FLAG;

            temp &= 0xFF;

            if (temp == 0)
                F |= Z_FLAG;

            AF.setLowByte(F);

            AF.setHighByte(temp);
            break;

        /* DEC 8 bit */
        case 0x05:
            BC.setHighByte(dec8bit(BC.getHighByte()));
            break;

        case 0x0D:
            BC.setLowByte(dec8bit(BC.getLowByte()));
            break;

        case 0x15:
            DE.setHighByte(dec8bit(DE.getHighByte()));
            break;

        case 0x1D:
            DE.setLowByte(dec8bit(DE.getLowByte()));
            break;

        case 0x25:
            HL.setHighByte(dec8bit(HL.getHighByte()));
            break;

        case 0x2D:
            HL.setLowByte(dec8bit(HL.getLowByte()));
            break;

        case 0x35:
            mmu.writeByte(HL, dec8bit(mmu.readByte(HL)));
            break;

        case 0x3D:
            AF.setHighByte(dec8bit(AF.getHighByte()));
            break;

        /* DEC 16 bit */
        case 0x0B:
            BC.dec();
            break;

        case 0x1B:
            DE.dec();
            break;

        case 0x2B:
            HL.dec();
            break;

        case 0x3B:
            SP.dec();
            break;

        /* HALT */
        case 0x76:
            halt = true;
            break;

        /* INC 8 bit */
        case 0x04:
            BC.setHighByte(inc8bit(BC.getHighByte()));
            break;

        case 0x0C:
            BC.setLowByte(inc8bit(BC.getLowByte()));
            break;

        case 0x14:
            DE.setHighByte(inc8bit(DE.getHighByte()));
            break;

        case 0x1C:
            DE.setLowByte(inc8bit(DE.getLowByte()));
            break;

        case 0x24:
            HL.setHighByte(inc8bit(HL.getHighByte()));
            break;

        case 0x2C:
            HL.setLowByte(inc8bit(HL.getLowByte()));
            break;

        case 0x34:
            mmu.writeByte(HL, inc8bit(mmu.readByte(HL)));
            break;

        case 0x3C:
            AF.setHighByte(inc8bit(AF.getHighByte()));
            break;

        /* INC nn */
        case 0x03:
            BC.inc();
            break;

        case 0x13:
            DE.inc();
            break;

        case 0x23:
            HL.inc();
            break;

        case 0x33:
            SP.inc();
            break;

        /* Jump (Relative) */
        case 0x18:
            temp = mmu.readSignedByte(PC);
            PC.inc();
            PC.setWord(PC.getWord() + temp);
            break;

        case 0x20:
            if ((F & Z_FLAG) == 0) {

                temp = mmu.readSignedByte(PC);
                PC.inc();
                PC.setWord(PC.getWord() + temp);
            } else {

                PC.inc();
                cyclesRun -= 4;
            }
            break;

        case 0x28:
            if ((F & Z_FLAG) != 0) {

                temp = mmu.readSignedByte(PC);
                PC.inc();
                PC.setWord(PC.getWord() + temp);
            } else {

                PC.inc();
                cyclesRun -= 4;
            }
            break;

        case 0x30:
            if ((F & C_FLAG) == 0) {

                temp = mmu.readSignedByte(PC);
                PC.inc();
                PC.setWord(PC.getWord() + temp);
            } else {

                PC.inc();
                cyclesRun -= 4;
            }
            break;

        case 0x38:
            if ((F & C_FLAG) != 0) {

                temp = mmu.readSignedByte(PC);
                PC.inc();
                PC.setWord(PC.getWord() + temp);
            } else {

                PC.inc();
                cyclesRun -= 4;
            }
            break;

        /* JUMP */
        case 0xC2:
            if ((F & Z_FLAG) == 0) {

                addr.setLowByte(mmu.readSignedByte(PC));
                PC.inc();
                addr.setHighByte(mmu.readSignedByte(PC));
                PC.inc();

                PC.setWord(addr.getWord());
            } else {

                PC.inc();
                PC.inc();
                cyclesRun -= 4;
            }
            break;

        case 0xC3:
            addr.setLowByte(mmu.readSignedByte(PC));
            PC.inc();
            addr.setHighByte(mmu.readSignedByte(PC));
            PC.inc();

            PC.setWord(addr.getWord());
            break;

        case 0xCA:
            if ((F & Z_FLAG) != 0) {

                addr.setLowByte(mmu.readSignedByte(PC));
                PC.inc();
                addr.setHighByte(mmu.readSignedByte(PC));
                PC.inc();

                PC.setWord(addr.getWord());
            } else {

                PC.inc();
                PC.inc();
                cyclesRun -= 4;
            }
            break;

        case 0xD2:
            if ((F & C_FLAG) == 0) {

                addr.setLowByte(mmu.readSignedByte(PC));
                PC.inc();
                addr.setHighByte(mmu.readSignedByte(PC));
                PC.inc();

                PC.setWord(addr.getWord());
            } else {

                PC.inc();
                PC.inc();
                cyclesRun -= 4;
            }
            break;

        case 0xDA:
            if ((F & C_FLAG) != 0) {

                addr.setLowByte(mmu.readSignedByte(PC));
                PC.inc();
                addr.setHighByte(mmu.readSignedByte(PC));
                PC.inc();

                PC.setWord(addr.getWord());
            } else {

                PC.inc();
                PC.inc();
                cyclesRun -= 4;
            }
            break;

        case 0xE9:
            PC.setWord(HL.getWord());
            break;

        /*
         * Load Commands 8bit
         */

        /* LD nn,n - Put value nn into n */
        case 0x06:
            BC.setHighByte(mmu.readByte(PC));
            PC.inc();
            break;

        case 0x0E:
            BC.setLowByte(mmu.readByte(PC));
            PC.inc();
            break;

        case 0x16:
            DE.setHighByte(mmu.readByte(PC));
            PC.inc();
            break;

        case 0x1E:
            DE.setLowByte(mmu.readByte(PC));
            PC.inc();
            break;

        case 0x26:
            HL.setHighByte(mmu.readByte(PC));
            PC.inc();
            break;

        case 0x2E:
            HL.setLowByte(mmu.readByte(PC));
            PC.inc();
            break;

        /* LD r1,r2 - Put value r2 into r1 */
        case 0x40:
            BC.setHighByte(BC.getHighByte());
            break;

        case 0x41:
            BC.setHighByte(BC.getLowByte());
            break;

        case 0x42:
            BC.setHighByte(DE.getHighByte());
            break;

        case 0x43:
            BC.setHighByte(DE.getLowByte());
            break;

        case 0x44:
            BC.setHighByte(HL.getHighByte());
            break;

        case 0x45:
            BC.setHighByte(HL.getLowByte());
            break;

        case 0x46:
            BC.setHighByte(mmu.readByte(HL));
            break;

        case 0x47:
            BC.setHighByte(AF.getHighByte());
            break;

        case 0x48:
            BC.setLowByte(BC.getHighByte());
            break;

        case 0x4A:
            BC.setLowByte(DE.getHighByte());
            break;

        case 0x4B:
            BC.setLowByte(DE.getLowByte());
            break;

        case 0x4C:
            BC.setLowByte(HL.getHighByte());
            break;

        case 0x4D:
            BC.setLowByte(HL.getLowByte());
            break;

        case 0x4E:
            BC.setLowByte(mmu.readByte(HL));
            break;

        case 0x4F:
            BC.setLowByte(AF.getHighByte());
            break;

        case 0x50:
            DE.setHighByte(BC.getHighByte());
            break;

        case 0x51:
            DE.setHighByte(BC.getLowByte());
            break;

        case 0x52:
            DE.setHighByte(DE.getHighByte());
            break;

        case 0x53:
            DE.setHighByte(DE.getLowByte());
            break;

        case 0x54:
            DE.setHighByte(HL.getHighByte());
            break;

        case 0x55:
            DE.setHighByte(HL.getLowByte());
            break;

        case 0x56:
            DE.setHighByte(mmu.readByte(HL));
            break;

        case 0x57:
            DE.setHighByte(AF.getHighByte());
            break;

        case 0x58:
            DE.setLowByte(BC.getHighByte());
            break;

        case 0x59:
            DE.setLowByte(BC.getLowByte());
            break;

        case 0x5A:
            DE.setLowByte(DE.getHighByte());
            break;

        case 0x5B:
            DE.setLowByte(DE.getLowByte());
            break;

        case 0x5C:
            DE.setLowByte(HL.getHighByte());
            break;

        case 0x5D:
            DE.setLowByte(HL.getLowByte());
            break;

        case 0x5E:
            DE.setLowByte(mmu.readByte(HL));
            break;

        case 0x5F:
            DE.setLowByte(AF.getHighByte());
            break;

        case 0x60:
            HL.setHighByte(BC.getHighByte());
            break;

        case 0x61:
            HL.setHighByte(BC.getLowByte());
            break;

        case 0x62:
            HL.setHighByte(DE.getHighByte());
            break;

        case 0x63:
            HL.setHighByte(DE.getLowByte());
            break;

        case 0x64:
            HL.setHighByte(HL.getHighByte());
            break;

        case 0x65:
            HL.setHighByte(HL.getLowByte());
            break;

        case 0x66:
            HL.setHighByte(mmu.readByte(HL));
            break;

        case 0x67:
            HL.setHighByte(AF.getHighByte());
            break;

        case 0x68:
            HL.setLowByte(BC.getHighByte());
            break;

        case 0x69:
            HL.setLowByte(BC.getLowByte());
            break;

        case 0x6A:
            HL.setLowByte(DE.getHighByte());
            break;

        case 0x6B:
            HL.setLowByte(DE.getLowByte());
            break;

        case 0x6C:
            HL.setLowByte(HL.getHighByte());
            break;

        case 0x6D:
            HL.setLowByte(HL.getLowByte());
            break;

        case 0x6E:
            HL.setLowByte(mmu.readByte(HL));
            break;

        case 0x6F:
            HL.setLowByte(AF.getHighByte());
            break;

        case 0x70:
            mmu.writeByte(HL, BC.getHighByte());
            break;

        case 0x71:
            mmu.writeByte(HL, BC.getLowByte());
            break;

        case 0x72:
            mmu.writeByte(HL, DE.getHighByte());
            break;

        case 0x73:
            mmu.writeByte(HL, DE.getLowByte());
            break;

        case 0x74:
            mmu.writeByte(HL, HL.getHighByte());
            break;

        case 0x75:
            mmu.writeByte(HL, HL.getLowByte());
            break;

        case 0x36:
            mmu.writeByte(HL, mmu.readByte(PC));
            PC.inc();
            break;

        /* LD A,n - Put value n into A */
        case 0x78:
            AF.setHighByte(BC.getHighByte());
            break;

        case 0x79:
            AF.setHighByte(BC.getLowByte());
            break;

        case 0x7A:
            AF.setHighByte(DE.getHighByte());
            break;

        case 0x7B:
            AF.setHighByte(DE.getLowByte());
            break;

        case 0x7C:
            AF.setHighByte(HL.getHighByte());
            break;

        case 0x7D:
            AF.setHighByte(HL.getLowByte());
            break;

        case 0x7F:
            AF.setHighByte(AF.getHighByte());
            break;

        case 0x0A:
            AF.setHighByte(mmu.readByte(BC));
            break;

        case 0x1A:
            AF.setHighByte(mmu.readByte(DE));
            break;

        case 0x7E:
            AF.setHighByte(mmu.readByte(HL));
            break;

        case 0xFA:
            addr.setLowByte(mmu.readByte(PC));
            PC.inc();
            addr.setHighByte(mmu.readByte(PC));
            PC.inc();
            AF.setHighByte(mmu.readByte(addr));
            break;

        case 0x3E:
            AF.setHighByte(mmu.readByte(PC));
            PC.inc();
            break;

        /* LD n,A */
        case 0x02:
            mmu.writeByte(BC, AF.getHighByte());
            break;

        case 0x12:
            mmu.writeByte(DE, AF.getHighByte());
            break;

        case 0x77:
            mmu.writeByte(HL, AF.getHighByte());
            break;

        case 0xEA:
            addr.setLowByte(mmu.readByte(PC));
            PC.inc();
            addr.setHighByte(mmu.readByte(PC));
            PC.inc();
            mmu.writeByte(addr, AF.getHighByte());
            break;

        /* LD A,(C) */
        case 0xF2:
            addr.setWord(0xFF00 + BC.getLowByte());
            AF.setHighByte(mmu.readByte(addr));
            PC.inc();
            break;

        /* LD (C),A */
        case 0xE2:
            addr.setWord(0xFF00 + BC.getLowByte());
            mmu.writeByte(addr, AF.getHighByte());
            break;

        /* LDD A,(HL) */
        case 0x3A:
            AF.setHighByte(mmu.readByte(HL));
            HL.dec();
            break;

        /* LDD (HL),A */
        case 0x32:
            mmu.writeByte(HL, AF.getHighByte());
            HL.dec();
            break;

        /* LDI A,(HL) */
        case 0x2A:
            AF.setHighByte(mmu.readByte(HL));
            HL.inc();
            break;

        /* LDI (HL),A */
        case 0x22:
            mmu.writeByte(HL, AF.getHighByte());
            HL.inc();
            break;

        /* LDH (n),A */
        case 0xE0:
            addr.setWord(0xFF00 + mmu.readByte(PC));
            mmu.writeByte(addr, AF.getHighByte());
            PC.inc();
            break;

        /* LDH A,(n) */
        case 0xF0:
            addr.setWord(0xFF00 + mmu.readByte(PC));
            AF.setHighByte(mmu.readByte(addr));
            PC.inc();
            break;
        /*
         * Load Commands 16bit
         */

        /* LD n,nn */
        case 0x01:
            BC.setLowByte(mmu.readByte(PC));
            PC.inc();
            BC.setHighByte(mmu.readByte(PC));
            PC.inc();
            break;

        case 0x11:
            DE.setLowByte(mmu.readByte(PC));
            PC.inc();
            DE.setHighByte(mmu.readByte(PC));
            PC.inc();
            break;

        case 0x21:
            HL.setLowByte(mmu.readByte(PC));
            PC.inc();
            HL.setHighByte(mmu.readByte(PC));
            PC.inc();
            break;

        case 0x31:
            SP.setLowByte(mmu.readByte(PC));
            PC.inc();
            SP.setHighByte(mmu.readByte(PC));
            PC.inc();
            break;

        /* LD SP,HL */
        case 0xF9:
            SP.setWord(HL.getWord());
            break;

        /* LDHL SP,n */
        case 0xF8:
            HL.setWord(addSP(mmu.readSignedByte(PC)));
            PC.inc();
            break;

        /* LD (nn),SP */
        case 0x08:
            addr = new Word();
            addr.setLowByte(mmu.readByte(PC));
            PC.inc();
            addr.setHighByte(mmu.readByte(PC));
            PC.inc();
            mmu.writeByte(addr, SP.getLowByte());
            addr.inc();
            mmu.writeByte(addr, SP.getHighByte());
            break;

        /* OR */
        case 0xB0:
            orA(BC.getHighByte());
            break;

        case 0xB1:
            orA(BC.getLowByte());
            break;

        case 0xB2:
            orA(DE.getHighByte());
            break;

        case 0xB3:
            orA(DE.getLowByte());
            break;

        case 0xB4:
            orA(HL.getHighByte());
            break;

        case 0xB5:
            orA(HL.getLowByte());
            break;

        case 0xB6:
            orA(mmu.readByte(HL));
            break;

        case 0xB7:
            orA(AF.getHighByte());
            break;

        case 0xF6:
            orA(mmu.readByte(PC));
            PC.inc();
            break;

        /* POP nn */
        case 0xF1:
            AF.setLowByte(mmu.readByte(SP));
            SP.inc();
            AF.setHighByte(mmu.readByte(SP));
            SP.inc();
            AF.setLowByte(AF.getLowByte() & 0xF0);
            break;

        case 0xC1:
            BC.setLowByte(mmu.readByte(SP));
            SP.inc();
            BC.setHighByte(mmu.readByte(SP));
            SP.inc();
            break;

        case 0xD1:
            DE.setLowByte(mmu.readByte(SP));
            SP.inc();
            DE.setHighByte(mmu.readByte(SP));
            SP.inc();
            break;

        case 0xE1:
            HL.setLowByte(mmu.readByte(SP));
            SP.inc();
            HL.setHighByte(mmu.readByte(SP));
            SP.inc();
            break;

        /* PUSH nn */
        case 0xF5:
            SP.dec();
            mmu.writeByte(SP, AF.getHighByte());
            SP.dec();
            mmu.writeByte(SP, AF.getLowByte());
            break;

        case 0xC5:
            SP.dec();
            mmu.writeByte(SP, BC.getHighByte());
            SP.dec();
            mmu.writeByte(SP, BC.getLowByte());
            break;

        case 0xD5:
            SP.dec();
            mmu.writeByte(SP, DE.getHighByte());
            SP.dec();
            mmu.writeByte(SP, DE.getLowByte());
            break;

        case 0xE5:
            SP.dec();
            mmu.writeByte(SP, HL.getHighByte());
            SP.dec();
            mmu.writeByte(SP, HL.getLowByte());
            break;

        /* RET */
        case 0xC9:
            PC.setLowByte(mmu.readByte(SP));
            SP.inc();
            PC.setHighByte(mmu.readByte(SP));
            SP.inc();
            break;

        /* RET cc */
        case 0xC0: // RET NZ
            if ((F & Z_FLAG) == 0) {

                PC.setLowByte(mmu.readByte(SP));
                SP.inc();
                PC.setHighByte(mmu.readByte(SP));
                SP.inc();

            } else {

                cyclesRun -= 12;
            }
            break;

        case 0xC8:// RET Z
            if ((F & Z_FLAG) != 0) {

                PC.setLowByte(mmu.readByte(SP));
                SP.inc();
                PC.setHighByte(mmu.readByte(SP));
                SP.inc();

            } else {

                cyclesRun -= 12;
            }
            break;

        case 0xD0: // RET NC
            if ((F & C_FLAG) == 0) {

                PC.setLowByte(mmu.readByte(SP));
                SP.inc();
                PC.setHighByte(mmu.readByte(SP));
                SP.inc();

            } else {

                cyclesRun -= 12;
            }
            break;

        case 0xD8:// RET C
            if ((F & C_FLAG) != 0) {

                PC.setLowByte(mmu.readByte(SP));
                SP.inc();
                PC.setHighByte(mmu.readByte(SP));
                SP.inc();

            } else {

                cyclesRun -= 12;
            }
            break;

        /* RETI */
        case 0xD9:
            setIME = true;
            PC.setLowByte(mmu.readByte(SP));
            SP.inc();
            PC.setHighByte(mmu.readByte(SP));
            SP.inc();
            break;

        /* RLA */
        case 0x17:
            carry = (F & C_FLAG) >> 4;
            AF.setLowByte((AF.getHighByte() & 0x80) >> 3);
            AF.setHighByte((AF.getHighByte() << 1) | carry);
            break;

        /* RLCA */
        case 0x07:
            carry = (AF.getHighByte() & 0x80) >> 7;
            AF.setLowByte(carry << 4);
            AF.setHighByte((AF.getHighByte() << 1) | carry);
            break;

        /* RRA */
        case 0x1F:
            carry = (F & C_FLAG) >> 4;
            AF.setLowByte((AF.getHighByte() & 0x01) << 4);
            AF.setHighByte((carry << 7) | (AF.getHighByte() >> 1));
            break;

        /* RRCA */
        case 0x0F:
            carry = (AF.getHighByte() & 0x01);
            AF.setLowByte(carry << 4);
            AF.setHighByte((carry << 7) | (AF.getHighByte() >> 1));
            break;

        /* RST */
        case 0xC7:
            SP.dec();
            mmu.writeByte(SP, PC.getHighByte());
            SP.dec();
            mmu.writeByte(SP, PC.getLowByte());
            PC.setWord(0x00);
            break;

        case 0xCF:
            SP.dec();
            mmu.writeByte(SP, PC.getHighByte());
            SP.dec();
            mmu.writeByte(SP, PC.getLowByte());
            PC.setWord(0x08);
            break;

        case 0xD7:
            SP.dec();
            mmu.writeByte(SP, PC.getHighByte());
            SP.dec();
            mmu.writeByte(SP, PC.getLowByte());
            PC.setWord(0x10);
            break;

        case 0xDF:
            SP.dec();
            mmu.writeByte(SP, PC.getHighByte());
            SP.dec();
            mmu.writeByte(SP, PC.getLowByte());
            PC.setWord(0x18);
            break;

        case 0xE7:
            SP.dec();
            mmu.writeByte(SP, PC.getHighByte());
            SP.dec();
            mmu.writeByte(SP, PC.getLowByte());
            PC.setWord(0x20);
            break;

        case 0xEF:
            SP.dec();
            mmu.writeByte(SP, PC.getHighByte());
            SP.dec();
            mmu.writeByte(SP, PC.getLowByte());
            PC.setWord(0x28);
            break;

        case 0xF7:
            SP.dec();
            mmu.writeByte(SP, PC.getHighByte());
            SP.dec();
            mmu.writeByte(SP, PC.getLowByte());
            PC.setWord(0x30);
            break;

        case 0xFF:
            SP.dec();
            mmu.writeByte(SP, PC.getHighByte());
            SP.dec();
            mmu.writeByte(SP, PC.getLowByte());
            PC.setWord(0x38);
            break;

        /* SCF */
        case 0x37:
            F &= Z_FLAG;
            F |= C_FLAG;
            AF.setLowByte(F);
            break;

        /* STOP */
        case 0x10:
            PC.inc();
            break;

        /* DI */
        case 0xF3:
            disableIME = true;
            break;

        /* IE */
        case 0xFB:
            setIME = true;
            break;

        /* SUB n */
        case 0x90:
            subA(BC.getHighByte());
            break;

        case 0x91:
            subA(BC.getLowByte());
            break;

        case 0x92:
            subA(DE.getHighByte());
            break;

        case 0x93:
            subA(DE.getLowByte());
            break;

        case 0x94:
            subA(HL.getHighByte());
            break;

        case 0x95:
            subA(HL.getLowByte());
            break;

        case 0x96:
            subA(mmu.readByte(HL));
            break;

        case 0x97:
            subA(AF.getHighByte());
            break;

        case 0xD6:
            subA(mmu.readByte(PC));
            PC.inc();
            break;

        /* SBC A,n */
        case 0x98:
            sbcA(BC.getHighByte());
            break;

        case 0x99:
            sbcA(BC.getLowByte());
            break;

        case 0x9A:
            sbcA(DE.getHighByte());
            break;

        case 0x9B:
            sbcA(DE.getLowByte());
            break;

        case 0x9C:
            sbcA(HL.getHighByte());
            break;

        case 0x9D:
            sbcA(HL.getLowByte());
            break;

        case 0x9E:
            sbcA(mmu.readByte(HL));
            break;

        case 0x9F:
            sbcA(AF.getHighByte());
            break;

        case 0xDE:
            sbcA(mmu.readByte(PC));
            PC.inc();
            break;

        /* XOR */
        case 0xA8:
            xorA(BC.getHighByte());
            break;

        case 0xA9:
            xorA(BC.getLowByte());
            break;

        case 0xAA:
            xorA(DE.getHighByte());
            break;

        case 0xAB:
            xorA(DE.getLowByte());
            break;

        case 0xAC:
            xorA(HL.getHighByte());
            break;

        case 0xAD:
            xorA(HL.getLowByte());
            break;

        case 0xAE:
            xorA(mmu.readByte(HL));
            break;

        case 0xAF:
            xorA(AF.getHighByte());
            break;

        case 0xEE:
            xorA(mmu.readByte(PC));
            PC.inc();
            break;

        case 0xCB:
            int op = mmu.readByte(PC);
            PC.inc();
            cyclesRun += cyclesCB[op];
            executeCBOpCode(op);
            break;

        default:
            System.out.println("Unrecognized OpCode: "
                    + Integer.toHexString(opCode));
            break;
        }
    }

    private void executeCBOpCode(int op) {

        switch (op) {

        case 0x00:
            BC.setHighByte(rlc(BC.getHighByte()));
            break;

        case 0x01:
            BC.setLowByte(rlc(BC.getLowByte()));
            break;

        case 0x02:
            DE.setHighByte(rlc(DE.getHighByte()));
            break;

        case 0x03:
            DE.setLowByte(rlc(DE.getLowByte()));
            break;

        case 0x04:
            HL.setHighByte(rlc(HL.getHighByte()));
            break;

        case 0x05:
            HL.setLowByte(rlc(HL.getLowByte()));
            break;

        case 0x06:
            mmu.writeByte(HL, rlc(mmu.readByte(HL)));
            break;

        case 0x07:
            AF.setHighByte(rlc(AF.getHighByte()));
            break;

        case 0x08:
            BC.setHighByte(rrc(BC.getHighByte()));
            break;

        case 0x09:
            BC.setLowByte(rrc(BC.getLowByte()));
            break;

        case 0x0A:
            DE.setHighByte(rrc(DE.getHighByte()));
            break;

        case 0x0B:
            DE.setLowByte(rrc(DE.getLowByte()));
            break;

        case 0x0C:
            HL.setHighByte(rrc(HL.getHighByte()));
            break;

        case 0x0D:
            HL.setLowByte(rrc(HL.getLowByte()));
            break;

        case 0x0E:
            mmu.writeByte(HL, rrc(mmu.readByte(HL)));
            break;

        case 0x0F:
            AF.setHighByte(rrc(AF.getHighByte()));
            break;

        case 0x10:
            BC.setHighByte(rl(BC.getHighByte()));
            break;

        case 0x11:
            BC.setLowByte(rl(BC.getLowByte()));
            break;

        case 0x12:
            DE.setHighByte(rl(DE.getHighByte()));
            break;

        case 0x13:
            DE.setLowByte(rl(DE.getLowByte()));
            break;

        case 0x14:
            HL.setHighByte(rl(HL.getHighByte()));
            break;

        case 0x15:
            HL.setLowByte(rl(HL.getLowByte()));
            break;

        case 0x16:
            mmu.writeByte(HL, rl(mmu.readByte(HL)));
            break;

        case 0x17:
            AF.setHighByte(rl(AF.getHighByte()));
            break;

        case 0x18:
            BC.setHighByte(rr(BC.getHighByte()));
            break;

        case 0x19:
            BC.setLowByte(rr(BC.getLowByte()));
            break;

        case 0x1A:
            DE.setHighByte(rr(DE.getHighByte()));
            break;

        case 0x1B:
            DE.setLowByte(rr(DE.getLowByte()));
            break;

        case 0x1C:
            HL.setHighByte(rr(HL.getHighByte()));
            break;

        case 0x1D:
            HL.setLowByte(rr(HL.getLowByte()));
            break;

        case 0x1E:
            mmu.writeByte(HL, rr(mmu.readByte(HL)));
            break;

        case 0x1F:
            AF.setHighByte(rr(AF.getHighByte()));
            break;

        case 0x20:
            BC.setHighByte(sla(BC.getHighByte()));
            break;

        case 0x21:
            BC.setLowByte(sla(BC.getLowByte()));
            break;

        case 0x22:
            DE.setHighByte(sla(DE.getHighByte()));
            break;

        case 0x23:
            DE.setLowByte(sla(DE.getLowByte()));
            break;

        case 0x24:
            HL.setHighByte(sla(HL.getHighByte()));
            break;

        case 0x25:
            HL.setLowByte(sla(HL.getLowByte()));
            break;

        case 0x26:
            mmu.writeByte(HL, sla(mmu.readByte(HL)));
            break;

        case 0x27:
            AF.setHighByte(sla(AF.getHighByte()));
            break;

        case 0x28:
            BC.setHighByte(sra(BC.getHighByte()));
            break;

        case 0x29:
            BC.setLowByte(sra(BC.getLowByte()));
            break;

        case 0x2A:
            DE.setHighByte(sra(DE.getHighByte()));
            break;

        case 0x2B:
            DE.setLowByte(sra(DE.getLowByte()));
            break;

        case 0x2C:
            HL.setHighByte(sra(HL.getHighByte()));
            break;

        case 0x2D:
            HL.setLowByte(sra(HL.getLowByte()));
            break;

        case 0x2E:
            mmu.writeByte(HL, sra(mmu.readByte(HL)));
            break;

        case 0x2F:
            AF.setHighByte(sra(AF.getHighByte()));
            break;

        case 0x30:
            BC.setHighByte(swap(BC.getHighByte()));
            break;

        case 0x31:
            BC.setLowByte(swap(BC.getLowByte()));
            break;

        case 0x32:
            DE.setHighByte(swap(DE.getHighByte()));
            break;

        case 0x33:
            DE.setLowByte(swap(DE.getLowByte()));
            break;

        case 0x34:
            HL.setHighByte(swap(HL.getHighByte()));
            break;

        case 0x35:
            HL.setLowByte(swap(HL.getLowByte()));
            break;

        case 0x36:
            mmu.writeByte(HL, swap(mmu.readByte(HL)));
            break;

        case 0x37:
            AF.setHighByte(swap(AF.getHighByte()));
            break;

        case 0x38:
            BC.setHighByte(srl(BC.getHighByte()));
            break;

        case 0x39:
            BC.setLowByte(srl(BC.getLowByte()));
            break;

        case 0x3A:
            DE.setHighByte(srl(DE.getHighByte()));
            break;

        case 0x3B:
            DE.setLowByte(srl(DE.getLowByte()));
            break;

        case 0x3C:
            HL.setHighByte(srl(HL.getHighByte()));
            break;

        case 0x3D:
            HL.setLowByte(srl(HL.getLowByte()));
            break;

        case 0x3E:
            mmu.writeByte(HL, srl(mmu.readByte(HL)));
            break;

        case 0x3F:
            AF.setHighByte(srl(AF.getHighByte()));
            break;

        case 0x40:
            bit(0, BC.getHighByte());
            break;

        case 0x41:
            bit(0, BC.getLowByte());
            break;

        case 0x42:
            bit(0, DE.getHighByte());
            break;

        case 0x43:
            bit(0, DE.getLowByte());
            break;

        case 0x44:
            bit(0, HL.getHighByte());
            break;

        case 0x45:
            bit(0, HL.getLowByte());
            break;

        case 0x46:
            bit(0, mmu.readByte(HL));
            break;

        case 0x47:
            bit(0, AF.getHighByte());
            break;

        case 0x48:
            bit(1, BC.getHighByte());
            break;

        case 0x49:
            bit(1, BC.getLowByte());
            break;

        case 0x4A:
            bit(1, DE.getHighByte());
            break;

        case 0x4B:
            bit(1, DE.getLowByte());
            break;

        case 0x4C:
            bit(1, HL.getHighByte());
            break;

        case 0x4D:
            bit(1, HL.getLowByte());
            break;

        case 0x4E:
            bit(1, mmu.readByte(HL));
            break;

        case 0x4F:
            bit(1, AF.getHighByte());
            break;

        case 0x50:
            bit(2, BC.getHighByte());
            break;

        case 0x51:
            bit(2, BC.getLowByte());
            break;

        case 0x52:
            bit(2, DE.getHighByte());
            break;

        case 0x53:
            bit(2, DE.getLowByte());
            break;

        case 0x54:
            bit(2, HL.getHighByte());
            break;

        case 0x55:
            bit(2, HL.getLowByte());
            break;

        case 0x56:
            bit(2, mmu.readByte(HL));
            break;

        case 0x57:
            bit(2, AF.getHighByte());
            break;

        case 0x58:
            bit(3, BC.getHighByte());
            break;

        case 0x59:
            bit(3, BC.getLowByte());
            break;

        case 0x5A:
            bit(3, DE.getHighByte());
            break;

        case 0x5B:
            bit(3, DE.getLowByte());
            break;

        case 0x5C:
            bit(3, HL.getHighByte());
            break;

        case 0x5D:
            bit(3, HL.getLowByte());
            break;

        case 0x5E:
            bit(3, mmu.readByte(HL));
            break;

        case 0x5F:
            bit(3, AF.getHighByte());
            break;

        case 0x60:
            bit(4, BC.getHighByte());
            break;

        case 0x61:
            bit(4, BC.getLowByte());
            break;

        case 0x62:
            bit(4, DE.getHighByte());
            break;

        case 0x63:
            bit(4, DE.getLowByte());
            break;

        case 0x64:
            bit(4, HL.getHighByte());
            break;

        case 0x65:
            bit(4, HL.getLowByte());
            break;

        case 0x66:
            bit(4, mmu.readByte(HL));
            break;

        case 0x67:
            bit(4, AF.getHighByte());
            break;

        case 0x68:
            bit(5, BC.getHighByte());
            break;

        case 0x69:
            bit(5, BC.getLowByte());
            break;

        case 0x6A:
            bit(5, DE.getHighByte());
            break;

        case 0x6B:
            bit(5, DE.getLowByte());
            break;

        case 0x6C:
            bit(5, HL.getHighByte());
            break;

        case 0x6D:
            bit(5, HL.getLowByte());
            break;

        case 0x6E:
            bit(5, mmu.readByte(HL));
            break;

        case 0x6F:
            bit(5, AF.getHighByte());
            break;

        case 0x70:
            bit(6, BC.getHighByte());
            break;

        case 0x71:
            bit(6, BC.getLowByte());
            break;

        case 0x72:
            bit(6, DE.getHighByte());
            break;

        case 0x73:
            bit(6, DE.getLowByte());
            break;

        case 0x74:
            bit(6, HL.getHighByte());
            break;

        case 0x75:
            bit(6, HL.getLowByte());
            break;

        case 0x76:
            bit(6, mmu.readByte(HL));
            break;

        case 0x77:
            bit(6, AF.getHighByte());
            break;

        case 0x78:
            bit(7, BC.getHighByte());
            break;

        case 0x79:
            bit(7, BC.getLowByte());
            break;

        case 0x7A:
            bit(7, DE.getHighByte());
            break;

        case 0x7B:
            bit(7, DE.getLowByte());
            break;

        case 0x7C:
            bit(7, HL.getHighByte());
            break;

        case 0x7D:
            bit(7, HL.getLowByte());
            break;

        case 0x7E:
            bit(7, mmu.readByte(HL));
            break;

        case 0x7F:
            bit(7, AF.getHighByte());
            break;

        case 0x80:
            BC.setHighByte(res(0, BC.getHighByte()));
            break;

        case 0x81:
            BC.setLowByte(res(0, BC.getLowByte()));
            break;

        case 0x82:
            DE.setHighByte(res(0, DE.getHighByte()));
            break;

        case 0x83:
            DE.setLowByte(res(0, DE.getLowByte()));
            break;

        case 0x84:
            HL.setHighByte(res(0, HL.getHighByte()));
            break;

        case 0x85:
            HL.setLowByte(res(0, HL.getLowByte()));
            break;

        case 0x86:
            mmu.writeByte(HL, res(0, mmu.readByte(HL)));
            break;

        case 0x87:
            AF.setHighByte(res(0, AF.getHighByte()));
            break;

        case 0x88:
            BC.setHighByte(res(1, BC.getHighByte()));
            break;

        case 0x89:
            BC.setLowByte(res(1, BC.getLowByte()));
            break;

        case 0x8A:
            DE.setHighByte(res(1, DE.getHighByte()));
            break;

        case 0x8B:
            DE.setLowByte(res(1, DE.getLowByte()));
            break;

        case 0x8C:
            HL.setHighByte(res(1, HL.getHighByte()));
            break;

        case 0x8D:
            HL.setLowByte(res(1, HL.getLowByte()));
            break;

        case 0x8E:
            mmu.writeByte(HL, res(1, mmu.readByte(HL)));
            break;

        case 0x8F:
            AF.setHighByte(res(1, AF.getHighByte()));
            break;

        case 0x90:
            BC.setHighByte(res(2, BC.getHighByte()));
            break;

        case 0x91:
            BC.setLowByte(res(2, BC.getLowByte()));
            break;

        case 0x92:
            DE.setHighByte(res(2, DE.getHighByte()));
            break;

        case 0x93:
            DE.setLowByte(res(2, DE.getLowByte()));
            break;

        case 0x94:
            HL.setHighByte(res(2, HL.getHighByte()));
            break;

        case 0x95:
            HL.setLowByte(res(2, HL.getLowByte()));
            break;

        case 0x96:
            mmu.writeByte(HL, res(2, mmu.readByte(HL)));
            break;

        case 0x97:
            AF.setHighByte(res(2, AF.getHighByte()));
            break;

        case 0x98:
            BC.setHighByte(res(3, BC.getHighByte()));
            break;

        case 0x99:
            BC.setLowByte(res(3, BC.getLowByte()));
            break;

        case 0x9A:
            DE.setHighByte(res(3, DE.getHighByte()));
            break;

        case 0x9B:
            DE.setLowByte(res(3, DE.getLowByte()));
            break;

        case 0x9C:
            HL.setHighByte(res(3, HL.getHighByte()));
            break;

        case 0x9D:
            HL.setLowByte(res(3, HL.getLowByte()));
            break;

        case 0x9E:
            mmu.writeByte(HL, res(3, mmu.readByte(HL)));
            break;

        case 0x9F:
            AF.setHighByte(res(3, AF.getHighByte()));
            break;

        case 0xA0:
            BC.setHighByte(res(4, BC.getHighByte()));
            break;

        case 0xA1:
            BC.setLowByte(res(4, BC.getLowByte()));
            break;

        case 0xA2:
            DE.setHighByte(res(4, DE.getHighByte()));
            break;

        case 0xA3:
            DE.setLowByte(res(4, DE.getLowByte()));
            break;

        case 0xA4:
            HL.setHighByte(res(4, HL.getHighByte()));
            break;

        case 0xA5:
            HL.setLowByte(res(4, HL.getLowByte()));
            break;

        case 0xA6:
            mmu.writeByte(HL, res(4, mmu.readByte(HL)));
            break;

        case 0xA7:
            AF.setHighByte(res(4, AF.getHighByte()));
            break;

        case 0xA8:
            BC.setHighByte(res(5, BC.getHighByte()));
            break;

        case 0xA9:
            BC.setLowByte(res(5, BC.getLowByte()));
            break;

        case 0xAA:
            DE.setHighByte(res(5, DE.getHighByte()));
            break;

        case 0xAB:
            DE.setLowByte(res(5, DE.getLowByte()));
            break;

        case 0xAC:
            HL.setHighByte(res(5, HL.getHighByte()));
            break;

        case 0xAD:
            HL.setLowByte(res(5, HL.getLowByte()));
            break;

        case 0xAE:
            mmu.writeByte(HL, res(5, mmu.readByte(HL)));
            break;

        case 0xAF:
            AF.setHighByte(res(5, AF.getHighByte()));
            break;

        case 0xB0:
            BC.setHighByte(res(6, BC.getHighByte()));
            break;

        case 0xB1:
            BC.setLowByte(res(6, BC.getLowByte()));
            break;

        case 0xB2:
            DE.setHighByte(res(6, DE.getHighByte()));
            break;

        case 0xB3:
            DE.setLowByte(res(6, DE.getLowByte()));
            break;

        case 0xB4:
            HL.setHighByte(res(6, HL.getHighByte()));
            break;

        case 0xB5:
            HL.setLowByte(res(6, HL.getLowByte()));
            break;

        case 0xB6:
            mmu.writeByte(HL, res(6, mmu.readByte(HL)));
            break;

        case 0xB7:
            AF.setHighByte(res(6, AF.getHighByte()));
            break;

        case 0xB8:
            BC.setHighByte(res(7, BC.getHighByte()));
            break;

        case 0xB9:
            BC.setLowByte(res(7, BC.getLowByte()));
            break;

        case 0xBA:
            DE.setHighByte(res(7, DE.getHighByte()));
            break;

        case 0xBB:
            DE.setLowByte(res(7, DE.getLowByte()));
            break;

        case 0xBC:
            HL.setHighByte(res(7, HL.getHighByte()));
            break;

        case 0xBD:
            HL.setLowByte(res(7, HL.getLowByte()));
            break;

        case 0xBE:
            mmu.writeByte(HL, res(7, mmu.readByte(HL)));
            break;

        case 0xBF:
            AF.setHighByte(res(7, AF.getHighByte()));
            break;

        case 0xC0:
            BC.setHighByte(set(0, BC.getHighByte()));
            break;

        case 0xC1:
            BC.setLowByte(set(0, BC.getLowByte()));
            break;

        case 0xC2:
            DE.setHighByte(set(0, DE.getHighByte()));
            break;

        case 0xC3:
            DE.setLowByte(set(0, DE.getLowByte()));
            break;

        case 0xC4:
            HL.setHighByte(set(0, HL.getHighByte()));
            break;

        case 0xC5:
            HL.setLowByte(set(0, HL.getLowByte()));
            break;

        case 0xC6:
            mmu.writeByte(HL, set(0, mmu.readByte(HL)));
            break;

        case 0xC7:
            AF.setHighByte(set(0, AF.getHighByte()));
            break;

        case 0xC8:
            BC.setHighByte(set(1, BC.getHighByte()));
            break;

        case 0xC9:
            BC.setLowByte(set(1, BC.getLowByte()));
            break;

        case 0xCA:
            DE.setHighByte(set(1, DE.getHighByte()));
            break;

        case 0xCB:
            DE.setLowByte(set(1, DE.getLowByte()));
            break;

        case 0xCC:
            HL.setHighByte(set(1, HL.getHighByte()));
            break;

        case 0xCD:
            HL.setLowByte(set(1, HL.getLowByte()));
            break;

        case 0xCE:
            mmu.writeByte(HL, set(1, mmu.readByte(HL)));
            break;

        case 0xCF:
            AF.setHighByte(set(1, AF.getHighByte()));
            break;

        case 0xD0:
            BC.setHighByte(set(2, BC.getHighByte()));
            break;

        case 0xD1:
            BC.setLowByte(set(2, BC.getLowByte()));
            break;

        case 0xD2:
            DE.setHighByte(set(2, DE.getHighByte()));
            break;

        case 0xD3:
            DE.setLowByte(set(2, DE.getLowByte()));
            break;

        case 0xD4:
            HL.setHighByte(set(2, HL.getHighByte()));
            break;

        case 0xD5:
            HL.setLowByte(set(2, HL.getLowByte()));
            break;

        case 0xD6:
            mmu.writeByte(HL, set(2, mmu.readByte(HL)));
            break;

        case 0xD7:
            AF.setHighByte(set(2, AF.getHighByte()));
            break;

        case 0xD8:
            BC.setHighByte(set(3, BC.getHighByte()));
            break;

        case 0xD9:
            BC.setLowByte(set(3, BC.getLowByte()));
            break;

        case 0xDA:
            DE.setHighByte(set(3, DE.getHighByte()));
            break;

        case 0xDB:
            DE.setLowByte(set(3, DE.getLowByte()));
            break;

        case 0xDC:
            HL.setHighByte(set(3, HL.getHighByte()));
            break;

        case 0xDD:
            HL.setLowByte(set(3, HL.getLowByte()));
            break;

        case 0xDE:
            mmu.writeByte(HL, set(3, mmu.readByte(HL)));
            break;

        case 0xDF:
            AF.setHighByte(set(3, AF.getHighByte()));
            break;

        case 0xE0:
            BC.setHighByte(set(4, BC.getHighByte()));
            break;

        case 0xE1:
            BC.setLowByte(set(4, BC.getLowByte()));
            break;

        case 0xE2:
            DE.setHighByte(set(4, DE.getHighByte()));
            break;

        case 0xE3:
            DE.setLowByte(set(4, DE.getLowByte()));
            break;

        case 0xE4:
            HL.setHighByte(set(4, HL.getHighByte()));
            break;

        case 0xE5:
            HL.setLowByte(set(4, HL.getLowByte()));
            break;

        case 0xE6:
            mmu.writeByte(HL, set(4, mmu.readByte(HL)));
            break;

        case 0xE7:
            AF.setHighByte(set(4, AF.getHighByte()));
            break;

        case 0xE8:
            BC.setHighByte(set(5, BC.getHighByte()));
            break;

        case 0xE9:
            BC.setLowByte(set(5, BC.getLowByte()));
            break;

        case 0xEA:
            DE.setHighByte(set(5, DE.getHighByte()));
            break;

        case 0xEB:
            DE.setLowByte(set(5, DE.getLowByte()));
            break;

        case 0xEC:
            HL.setHighByte(set(5, HL.getHighByte()));
            break;

        case 0xED:
            HL.setLowByte(set(5, HL.getLowByte()));
            break;

        case 0xEE:
            mmu.writeByte(HL, set(5, mmu.readByte(HL)));
            break;

        case 0xEF:
            AF.setHighByte(set(5, AF.getHighByte()));
            break;

        case 0xF0:
            BC.setHighByte(set(6, BC.getHighByte()));
            break;

        case 0xF1:
            BC.setLowByte(set(6, BC.getLowByte()));
            break;

        case 0xF2:
            DE.setHighByte(set(6, DE.getHighByte()));
            break;

        case 0xF3:
            DE.setLowByte(set(6, DE.getLowByte()));
            break;

        case 0xF4:
            HL.setHighByte(set(6, HL.getHighByte()));
            break;

        case 0xF5:
            HL.setLowByte(set(6, HL.getLowByte()));
            break;

        case 0xF6:
            mmu.writeByte(HL, set(6, mmu.readByte(HL)));
            break;

        case 0xF7:
            AF.setHighByte(set(6, AF.getHighByte()));
            break;

        case 0xF8:
            BC.setHighByte(set(7, BC.getHighByte()));
            break;

        case 0xF9:
            BC.setLowByte(set(7, BC.getLowByte()));
            break;

        case 0xFA:
            DE.setHighByte(set(7, DE.getHighByte()));
            break;

        case 0xFB:
            DE.setLowByte(set(7, DE.getLowByte()));
            break;

        case 0xFC:
            HL.setHighByte(set(7, HL.getHighByte()));
            break;

        case 0xFD:
            HL.setLowByte(set(7, HL.getLowByte()));
            break;

        case 0xFE:
            mmu.writeByte(HL, set(7, mmu.readByte(HL)));
            break;

        case 0xFF:
            AF.setHighByte(set(7, AF.getHighByte()));
            break;

        default:
            System.out.println("Unrecognized CB OpCode: "
                    + Integer.toHexString(op));
            break;
        }

    }

    private void bit(int n, int val) {

        int F = (AF.getLowByte() & C_FLAG) | H_FLAG;
        F &= ~Z_FLAG;
        if (((0x1 << n) & val) == 0)
            F |= Z_FLAG;
        AF.setLowByte(F);
    }

    private int set(int n, int val) {

        val |= (0x1 << n);

        return val & 0xFF;
    }

    private int res(int n, int val) {

        val &= ~(0x1 << n);

        return val & 0xFF;
    }

    private int rl(int val) {

        int carry = (AF.getLowByte() & C_FLAG) >> 4;
        int F = (val & 0x80) >> 3;
        val = (val << 1) | carry;
        if ((val & 0xFF) == 0)
            F |= Z_FLAG;
        AF.setLowByte(F);

        return val & 0xFF;
    }

    private int rr(int val) {

        int carry = (AF.getLowByte() & C_FLAG) >> 4;
        int F = (val & 0x01) << 4;
        val = (val >> 1) | (carry << 7);
        if ((val & 0xFF) == 0)
            F |= Z_FLAG;
        AF.setLowByte(F);

        return val & 0xFF;
    }

    private int rlc(int val) {

        val = (val << 1) | (val >> 7);
        int F = 0;
        F = (val & 0x1) << 4;
        if ((val & 0xFF) == 0)
            F |= Z_FLAG;
        AF.setLowByte(F);

        return val & 0xFF;
    }

    private int rrc(int val) {

        int F = 0;
        F = (val & 0x1) << 4;

        val = (val >> 1) | (val << 7);

        if ((val & 0xFF) == 0)
            F |= Z_FLAG;
        AF.setLowByte(F);

        return val & 0xFF;
    }

    private int sla(int val) {

        int F = (val & 0x80) >> 3;
        val = val << 1;
        if ((val & 0xFF) == 0)
            F |= Z_FLAG;
        AF.setLowByte(F);

        return val & 0xFF;
    }

    private int sra(int val) {

        int F = (val & 0x01) << 4;
        val = (val & 0x80) | (val >> 1);
        if ((val & 0xFF) == 0)
            F |= Z_FLAG;
        AF.setLowByte(F);

        return val & 0xFF;
    }

    private int srl(int val) {

        int F = (val & 0x01) << 4;
        val = val >> 1;
        val &= ~(0x80);
        if ((val & 0xFF) == 0)
            F |= Z_FLAG;
        AF.setLowByte(F);

        return val & 0xFF;
    }

    private int swap(int val) {

        val = ((val >> 4) | (val << 4)) & 0xFF;
        int F = 0;
        if (val == 0)
            F = Z_FLAG;
        AF.setLowByte(F);

        return val;
    }

    private int addSP(int val) {

        int tempSP = SP.getWord() + val;
        int F = 0;
        if ((((SP.getWord() & 0xFF) + (val & 0xFF)) & 0x100) == 0x100)
            F |= C_FLAG;

        if ((((SP.getWord() & 0x0F) + (val & 0x0F)) & 0x10) == 0x10)
            F |= H_FLAG;
        AF.setLowByte(F);

        return (tempSP & 0xFFFF);
    }

    private void adcA(int val) {

        int carry = ((AF.getLowByte() & C_FLAG) >> 4);
        int temp = AF.getHighByte() + val + carry;

        int F = 0;
        if ((((AF.getHighByte() & 0x0F) + (val & 0x0F) + carry) & 0x10) == 0x10)
            F |= H_FLAG;
        if ((temp & 0x100) == 0x100)
            F |= C_FLAG;
        if ((temp & 0xFF) == 0)
            F |= Z_FLAG;
        AF.setLowByte(F);

        AF.setHighByte(temp);
    }

    private void addA(int val) {

        int temp = AF.getHighByte() + val;

        int F = 0;
        if ((((AF.getHighByte() & 0x0F) + (val & 0x0F)) & 0x10) == 0x10)
            F |= H_FLAG;
        if ((temp & 0x100) == 0x100)
            F |= C_FLAG;
        if ((temp & 0xFF) == 0)
            F |= Z_FLAG;
        AF.setLowByte(F);

        AF.setHighByte(temp);
    }

    private void andA(int val) {

        int temp = AF.getHighByte() & val;

        int F = H_FLAG;
        if ((temp & 0xFF) == 0)
            F |= Z_FLAG;
        AF.setLowByte(F);

        AF.setHighByte(temp);
    }

    private void addHL16bit(Word val) {

        int temp = HL.getWord() + val.getWord();
        int F = AF.getLowByte() & Z_FLAG;
        if ((temp & 0x10000) == 0x10000)
            F |= C_FLAG;
        F |= (H_FLAG & ((HL.getWord() ^ val.getWord() ^ (temp & 0xFFFF)) >> 7));
        AF.setLowByte(F);
        HL.setWord(temp);
    }

    private int dec8bit(int val) {

        val--;

        int F = AF.getLowByte() & C_FLAG;
        F |= N_FLAG;
        if ((val & 0x0F) == 0xF)
            F |= H_FLAG;
        if ((val & 0xFF) == 0)
            F |= Z_FLAG;
        AF.setLowByte(F);

        return val;
    }

    private void cpA(int val) {

        int temp = AF.getHighByte() - val;

        int F = N_FLAG;
        F |= (H_FLAG & ((AF.getHighByte() ^ val ^ (temp & 0xFF)) << 1));
        if (AF.getHighByte() < val)
            F |= C_FLAG;
        if (AF.getHighByte() == val)
            F |= Z_FLAG;
        AF.setLowByte(F);
    }

    private int inc8bit(int val) {

        val++;

        int F = AF.getLowByte() & C_FLAG;
        if ((val & 0xF) == 0x0)
            F |= H_FLAG;
        if ((val & 0xFF) == 0)
            F |= Z_FLAG;
        AF.setLowByte(F);

        return val;
    }

    private void orA(int val) {

        int temp = AF.getHighByte() | val;

        int F = 0;
        if ((temp & 0xFF) == 0)
            F |= Z_FLAG;
        AF.setLowByte(F);

        AF.setHighByte(temp);
    }

    private void subA(int val) {

        int temp = AF.getHighByte() - val;

        int F = N_FLAG;
        F |= (H_FLAG & ((AF.getHighByte() ^ val ^ (temp & 0xFF)) << 1));
        if (temp < 0)
            F |= C_FLAG;
        if ((temp & 0xFF) == 0)
            F |= Z_FLAG;
        AF.setLowByte(F);

        AF.setHighByte(temp);
    }

    private void sbcA(int val) {

        int carry = ((AF.getLowByte() & C_FLAG) >> 4);
        int temp = AF.getHighByte() - val - carry;

        int F = N_FLAG;
        F |= (H_FLAG & ((AF.getHighByte() ^ val ^ (temp & 0xFF)) << 1));
        if (temp < 0)
            F |= C_FLAG;
        if ((temp & 0xFF) == 0)
            F |= Z_FLAG;
        AF.setLowByte(F);

        AF.setHighByte(temp);
    }

    private void xorA(int val) {

        int temp = AF.getHighByte() ^ val;

        int F = 0;
        if ((temp & 0xFF) == 0)
            F |= Z_FLAG;
        AF.setLowByte(F);

        AF.setHighByte(temp);
    }

    private final static int[] cycles = {
            4, 12, 8, 8, 4, 4, 8, 4, 20, 8, 8, 8, 4, 4, 8, 4,
            4, 12, 8, 8, 4, 4, 8, 4, 12, 8, 8, 8, 4, 4, 8, 4,
            12, 12, 8, 8, 4, 4, 8, 4, 12, 8, 8, 8, 4, 4, 8, 4,
            12, 12, 8, 8, 12, 12, 12, 4, 12, 8, 8, 8, 4, 4, 8, 4,

            4, 4, 4, 4, 4, 4, 8, 4, 4, 4, 4, 4, 4, 4, 8, 4,
            4, 4, 4, 4, 4, 4, 8, 4, 4, 4, 4, 4, 4, 4, 8, 4,
            4, 4, 4, 4, 4, 4, 8, 4, 4, 4, 4, 4, 4, 4, 8, 4,
            8, 8, 8, 8, 8, 8, 4, 8, 4, 4, 4, 4, 4, 4, 8, 4,

            4, 4, 4, 4, 4, 4, 8, 4, 4, 4, 4, 4, 4, 4, 8, 4,
            4, 4, 4, 4, 4, 4, 8, 4, 4, 4, 4, 4, 4, 4, 8, 4,
            4, 4, 4, 4, 4, 4, 8, 4, 4, 4, 4, 4, 4, 4, 8, 4,
            4, 4, 4, 4, 4, 4, 8, 4, 4, 4, 4, 4, 4, 4, 8, 4,

            20, 12, 16, 16, 24, 16, 8, 16, 20, 16, 16, 0, 24, 24, 8, 16,
            20, 12, 16, 0, 24, 16, 8, 16, 20, 16, 16, 0, 24, 0, 8, 16,
            12, 12, 8, 0, 0, 16, 8, 16, 16, 4, 16, 0, 0, 0, 8, 16,
            12, 12, 8, 4, 0, 16, 8, 16, 12, 8, 16, 4, 0, 0, 8, 16
    };

    private final static int[] cyclesCB = {
            8, 8, 8, 8, 8, 8, 16, 8, 8, 8, 8, 8, 8, 8, 16, 8,
            8, 8, 8, 8, 8, 8, 16, 8, 8, 8, 8, 8, 8, 8, 16, 8,
            8, 8, 8, 8, 8, 8, 16, 8, 8, 8, 8, 8, 8, 8, 16, 8,
            8, 8, 8, 8, 8, 8, 16, 8, 8, 8, 8, 8, 8, 8, 16, 8,

            8, 8, 8, 8, 8, 8, 12, 8, 8, 8, 8, 8, 8, 8, 12, 8,
            8, 8, 8, 8, 8, 8, 12, 8, 8, 8, 8, 8, 8, 8, 12, 8,
            8, 8, 8, 8, 8, 8, 12, 8, 8, 8, 8, 8, 8, 8, 12, 8,
            8, 8, 8, 8, 8, 8, 12, 8, 8, 8, 8, 8, 8, 8, 12, 8,

            8, 8, 8, 8, 8, 8, 16, 8, 8, 8, 8, 8, 8, 8, 16, 8,
            8, 8, 8, 8, 8, 8, 16, 8, 8, 8, 8, 8, 8, 8, 16, 8,
            8, 8, 8, 8, 8, 8, 16, 8, 8, 8, 8, 8, 8, 8, 16, 8,
            8, 8, 8, 8, 8, 8, 16, 8, 8, 8, 8, 8, 8, 8, 16, 8,

            8, 8, 8, 8, 8, 8, 16, 8, 8, 8, 8, 8, 8, 8, 16, 8,
            8, 8, 8, 8, 8, 8, 16, 8, 8, 8, 8, 8, 8, 8, 16, 8,
            8, 8, 8, 8, 8, 8, 16, 8, 8, 8, 8, 8, 8, 8, 16, 8,
            8, 8, 8, 8, 8, 8, 16, 8, 8, 8, 8, 8, 8, 8, 16, 8
    };
}
