package edu.gw.csci.simulator.isa.instructions;

import edu.gw.csci.simulator.cpu.CPU;
import edu.gw.csci.simulator.exceptions.IllegalOpcode;
import edu.gw.csci.simulator.isa.Instruction;
import edu.gw.csci.simulator.isa.InstructionType;
import edu.gw.csci.simulator.isa.SetCC;
import edu.gw.csci.simulator.memory.AllMemory;
import edu.gw.csci.simulator.registers.AllRegisters;
import edu.gw.csci.simulator.registers.Register;
import edu.gw.csci.simulator.registers.RegisterDecorator;
import edu.gw.csci.simulator.registers.RegisterType;
import edu.gw.csci.simulator.utils.BinaryCalculate;
import edu.gw.csci.simulator.utils.BitConversion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.BitSet;
import java.util.Optional;

import static edu.gw.csci.simulator.utils.BitConversion.toBinaryString;

public class ArithmeticLogic {

    private static final Logger LOGGER = LogManager.getLogger(ArithmeticLogic.class);

    public static class AMR implements Instruction {

        private InstructionType instructionType = InstructionType.AMR;

        private String data;

        @Override
        public void execute(AllMemory memory, AllRegisters registers,CPU cpu) {
            String Rs = data.substring(0, 2);
            Register R = registers.getRegister(RegisterType.getGeneralPurpose(Rs));
            RegisterDecorator Rd = new RegisterDecorator(R);

            int EA = memory.EA();
            int RValue = Rd.toInt();
            BitSet MemoryBits = memory.fetch(EA);
            int MemoryValue = BitConversion.convert(MemoryBits);

            registers.checkOverUnderFlow(RValue + MemoryValue);

            R.setData(BinaryCalculate.BitAdd(R.getData(),MemoryBits));


            String mess = String.format("AMR R:%s EA:%d, %s = %d + %d",
                    R.getName(), EA, R.getName(), RValue, MemoryValue);
            LOGGER.info(mess);

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

    public static class SMR implements Instruction {

        private InstructionType instructionType = InstructionType.SMR;

        private String data;

        @Override
        public void execute(AllMemory memory, AllRegisters registers,CPU cpu) {
            String Rs = data.substring(0,2);
            Register R =registers.getRegister(RegisterType.getGeneralPurpose(Rs));
            RegisterDecorator Rd = new RegisterDecorator(R);

            int EA = memory.EA();
            int RValue = Rd.toInt();
            BitSet MemoryBits = memory.fetch(EA);
            int MemoryValue = BitConversion.convert(MemoryBits);

            registers.checkOverUnderFlow(RValue-MemoryValue);

            R.setData(BinaryCalculate.BitMinus(R.getData(),MemoryBits));

            String mess = String.format("SMR R:%s EA:%d, %s = %d - %d",
                    R.getName(),EA,R.getName(),RValue,MemoryValue);
            LOGGER.info(mess);
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

    public static class AIR implements Instruction {

        private InstructionType instructionType = InstructionType.AIR;

        private String data;

        @Override
        public void execute(AllMemory memory, AllRegisters registers,CPU cpu) {
            String Rs = data.substring(0,2);
            Register R =registers.getRegister(RegisterType.getGeneralPurpose(Rs));
            RegisterDecorator Rd = new RegisterDecorator(R);

            int EA = memory.EA();
            int RValue = Rd.toInt();

            if(EA==0)
            {
                R.setData(R.getData());
            }
            else if(RValue ==0)
            {
                Rd.setValue(EA);
            }
            else{
               registers.checkOverUnderFlow(RValue+EA);
               R.setData(BinaryCalculate.BitAdd(R.getData(),EA));
            }

            String mess = String.format("AIR R:%s immed:%d, %s = %d + %d",
                    R.getName(),EA,R.getName(),RValue,EA);
            LOGGER.info(mess);
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

    public static class SIR implements Instruction {

        private InstructionType instructionType = InstructionType.SIR;

        private String data;

        @Override
        public void execute(AllMemory memory, AllRegisters registers,CPU cpu) {
            String Rs = data.substring(0,2);
            Register R =registers.getRegister(RegisterType.getGeneralPurpose(Rs));
            RegisterDecorator Rd = new RegisterDecorator(R);

            int EA = memory.EA();
            int RValue = Rd.toInt();

            if(EA==0) {
                R.setData(R.getData());
            }
            else if(RValue ==0)
            {
                Rd.setValue(-EA);
            }
            else{
                registers.checkOverUnderFlow(RValue-EA);
                R.setData(BinaryCalculate.BitMinus(R.getData(),EA));
            }

            String mess = String.format("SIR R:%s immed:%d, %s = %d - %d",
                    R.getName(),EA,R.getName(),RValue,EA);
            LOGGER.info(mess);
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

    public static class MLT implements Instruction {

        private InstructionType instructionType = InstructionType.MLT;

        private String data;

        @Override
        public void execute(AllMemory memory, AllRegisters registers,CPU cpu) {
            String Rxs = data.substring(0,2);
            String Rys = data.substring(2,4);
            Register Rx =registers.getRegister(RegisterType.getGeneralPurpose(Rxs));
            Register Ry =registers.getRegister(RegisterType.getGeneralPurpose(Rys));
            Register Rx_1;

            if((Rx.getName().equals("R0") ||Rx.getName().equals("R2"))&&(Ry.getName().equals("R0") ||Ry.getName().equals("R2"))) {
                //rx must be 0 or 2 ; ry must be 0 or 2
                if (Rx.getName().equals("R0")) {
                    Rx_1 = registers.getRegister(RegisterType.R1);
                } else{
                    Rx_1 = registers.getRegister(RegisterType.R3);
                }
                int RxValue=BitConversion.convert(Rx.getData());
                int RyValue=BitConversion.convert(Ry.getData());

                registers.checkExtendOverUnderFlow(RxValue*RyValue);

                String MulValue = BitConversion.toBinaryString(BitConversion.ExtendConvert(RxValue*RyValue),32);

                Rx.setData(BitConversion.fromBinaryStringToBitSet(MulValue.substring(0,16)));
                Rx_1.setData(BitConversion.fromBinaryStringToBitSet(MulValue.substring(16,31)));

                String mess = String.format("MLT Rx:%s(%d) Ry:%s(%d)",
                        Rx.getName(),RxValue,Ry.getName(),RyValue);
                LOGGER.info(mess);
            }
            else{
                String mess = "R registers must be 0 or 2.";
                throw  new IllegalOpcode(mess);
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

    public static class DVD implements Instruction {

        private InstructionType instructionType = InstructionType.DVD;

        private String data;

        @Override
        public void execute(AllMemory memory, AllRegisters registers,CPU cpu) {
            String Rxs = data.substring(0,2);
            String Rys = data.substring(2,4);
            Register Rx =registers.getRegister(RegisterType.getGeneralPurpose(Rxs));
            Register Ry =registers.getRegister(RegisterType.getGeneralPurpose(Rys));
            Register Rx_1;
            RegisterDecorator Rx_1d;
            RegisterDecorator Rxd = new RegisterDecorator(Rx);


            if(Ry.getData().isEmpty()) {
                //If c(ry) = 0, set cc(3) to 1 (set DIVZERO flag)
                registers.DIVZERO();
            }
            else
            {
                if((Rx.getName().equals("R0") ||Rx.getName().equals("R2"))&&(Ry.getName().equals("R0") ||Ry.getName().equals("R2")))
                {
                    //rx must be 0 or 2 ; ry must be 0 or 2
                    if(Rx.getName().equals("R0")) {
                        Rx_1 = registers.getRegister(RegisterType.R1);
                    }
                    else{
                        Rx_1 = registers.getRegister(RegisterType.R3);
                    }
                    Rx_1d = new RegisterDecorator(Rx_1);
                    int RxValue=BitConversion.convert(Rx.getData());
                    int RyValue=BitConversion.convert(Ry.getData());
                    Rxd.setValue(RxValue/RyValue);
                    Rx_1d.setValue(RxValue%RyValue);

                    String mess = String.format("DVD Rx:%s Ry:%s",
                            Rx.getName(),Ry.getName());
                    LOGGER.info(mess);
                }
                else{
                    String mess = "R registers must be 0 or 2.";
                    throw  new IllegalOpcode(mess);
                }
            }
            //registers.PCadder();

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

    public static class TRR implements Instruction {

        private InstructionType instructionType = InstructionType.TRR;

        private String data;

        @Override
        public void execute(AllMemory memory, AllRegisters registers,CPU cpu) {
            String Rxs = data.substring(0,2);
            String Rys = data.substring(2,4);
            Register Rx =registers.getRegister(RegisterType.getGeneralPurpose(Rxs));
            Register Ry =registers.getRegister(RegisterType.getGeneralPurpose(Rys));

            //If c(rx) = c(ry), set cc(4) <- 1; else, cc(4) <- 0
            if(Rx.getData().equals(Ry.getData())){
                registers.EQUALORNOT(true);
            }
            else{
                registers.EQUALORNOT(false);
            }
            //registers.PCadder();

            String mess = String.format("TRR Rx:%s Ry:%s If c(rx) = c(ry), set cc(4) <- 1; else, cc(4) <- 0",
                    Rx.getName(),Ry.getName());
            LOGGER.info(mess);
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

    public static class AND implements Instruction {

        private InstructionType instructionType = InstructionType.AND;

        private String data;

        @Override
        public void execute(AllMemory memory, AllRegisters registers,CPU cpu) {
            String Rxs = data.substring(0,2);
            String Rys = data.substring(2,4);
            Register Rx =registers.getRegister(RegisterType.getGeneralPurpose(Rxs));
            Register Ry =registers.getRegister(RegisterType.getGeneralPurpose(Rys));

            BitSet bits = Rx.getData();
            bits.and(Ry.getData());
            Rx.setData(bits);

            String mess = String.format("AND Rx:%s Ry:%s",
                    Rx.getName(),Ry.getName());
            LOGGER.info(mess);
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

    public static class ORR implements Instruction {

        private InstructionType instructionType = InstructionType.ORR;

        private String data;

        @Override
        public void execute(AllMemory memory, AllRegisters registers,CPU cpu) {
            String Rxs = data.substring(0,2);
            String Rys = data.substring(2,4);
            Register Rx =registers.getRegister(RegisterType.getGeneralPurpose(Rxs));
            Register Ry =registers.getRegister(RegisterType.getGeneralPurpose(Rys));

            BitSet bits = Rx.getData();
            bits.or(Ry.getData());
            Rx.setData(bits);

            String mess = String.format("ORR Rx:%s Ry:%s",
                    Rx.getName(),Ry.getName());
            LOGGER.info(mess);
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

    public static class NOT implements Instruction {

        private InstructionType instructionType = InstructionType.NOT;

        private String data;

        @Override
        public void execute(AllMemory memory, AllRegisters registers,CPU cpu) {
            String Rs = data.substring(0,2);
            Register R =registers.getRegister(RegisterType.getGeneralPurpose(Rs));


            BitSet bits = R.getData();
            bits.flip(0,16);
            R.setData(bits);

            String mess = String.format("NOT R:%s",
                    R.getName());
            LOGGER.info(mess);
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
