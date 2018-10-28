package edu.gw.csci.simulator.isa.instructions;

import edu.gw.csci.simulator.cpu.CPU;
import edu.gw.csci.simulator.isa.Instruction;
import edu.gw.csci.simulator.isa.InstructionType;
import edu.gw.csci.simulator.memory.AllMemory;
import edu.gw.csci.simulator.registers.AllRegisters;
import edu.gw.csci.simulator.registers.Register;
import edu.gw.csci.simulator.registers.RegisterDecorator;
import edu.gw.csci.simulator.registers.RegisterType;
import edu.gw.csci.simulator.utils.BitConversion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ShiftRotate {
    private static final Logger LOGGER = LogManager.getLogger(ShiftRotate.class);

    public static class SRC implements Instruction {

        private InstructionType instructionType = InstructionType.SRC;

        private String data;

        @Override
        public void execute(AllMemory memory, AllRegisters registers, CPU cpu) {
            String Rs = data.substring(0,2);
            Register R =registers.getRegister(RegisterType.getGeneralPurpose(Rs));
            RegisterDecorator Rd = new RegisterDecorator(R);
            String LR = data.substring(2,3);
            String AL = data.substring(3,4);
            String Counts = data.substring(6,10);

            boolean LorR = LR.equals("1");
            boolean AorL = AL.equals("1");
            int count = Integer.parseInt(Counts,2);
            String LRflag;
            String ALflag;

            if(LorR) LRflag ="left";
            else LRflag="right";

            if(AorL) ALflag = "logically";
            else ALflag = "arithmetically";

            String mess = String.format("SRC Shift Register:%s %s %s by %d",R.getName(),LRflag,ALflag,count);
            LOGGER.info(mess);
            if(count == 0){
                registers.PCadder();
            }
            else{
                registers.PCadder();
                String s1 = BitConversion.toBinaryString(R.getData(),R.getSize());
                if(LorR) {
                    //logically left shift equals to arithmetically left shift
                    String s2 = s1.substring(count);
                    StringBuilder s3 = new StringBuilder(count);
                    for (int i = 0; i < count; i++) {
                        s3.append("0");
                        //s2=s2+"0";
                    }
                    s2 = s2 + s3.toString();
                    R.setData(BitConversion.convert(s2));
                }
                else if(AorL){
                    //logically right shift
                    String s2 = s1.substring(0,s1.length()-count);
                    StringBuilder s3 = new StringBuilder(count);
                    for (int i = 0; i < count; i++) {
                        s3.append("0");
                        //s2="0"+s2;
                    }
                    s2 = s3.toString()+s2;
                    R.setData(BitConversion.convert(s2));
                }
                else{
                    //arithmetically right shift
                    String s2 = s1.substring(0,s1.length()-count);
                    String first = s1.substring(0,1);
                    StringBuilder s3 = new StringBuilder(count);
                    for (int i = 0; i < count; i++) {
                        s3.append(first);
                        //s2=s1.substring(0,1)+s2;
                    }
                    s2 = s3.toString()+s2;
                    R.setData(BitConversion.convert(s2));
                }
            }
        }

        @Override
        public void setData(String data) {
            this.data = data;
        }

        @Override
        public InstructionType getInstructionType() {
            return instructionType;
        }
    }

    public static class RRC implements Instruction {

        private InstructionType instructionType = InstructionType.RRC;

        private String data;

        @Override
        public void execute(AllMemory memory, AllRegisters registers,CPU cpu) {

            String Rs = data.substring(0,2);
            Register R =registers.getRegister(RegisterType.getGeneralPurpose(Rs));
            RegisterDecorator Rd = new RegisterDecorator(R);

            String LR = data.substring(2,3);
            boolean LorR = LR.equals("1");
            String Counts = data.substring(6,10);
            int count = Integer.parseInt(Counts,2);
            String LRflag;

            if(LorR) LRflag ="left";
            else LRflag="right";

            String mess = String.format("RRC Rotate Register:%s %s by %d",R.getName(),LRflag,count);
            LOGGER.info(mess);

            if(count ==0) {
                registers.PCadder();
            }
            else {
                registers.PCadder();
                String s1 = BitConversion.toBinaryString(R.getData(),R.getSize());
                if(LorR)
                //left rotation
                {
                    String s2 = s1.substring(count);
                    String s3 = s1.substring(0,count);
                    R.setData(BitConversion.convert(s2+s3));
                }
                else{
                    //right rotation
                    String s2 = s1.substring(0,s1.length()-count);
                    String s3 = s1.substring(s1.length()-count);
                    R.setData(BitConversion.convert(s3+s2));
                }

            }
        }

        @Override
        public void setData(String data) {
            this.data = data;
        }

        @Override
        public InstructionType getInstructionType() {
            return instructionType;
        }
    }
}