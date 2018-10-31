package edu.gw.csci.simulator.isa.instructions;

import edu.gw.csci.simulator.cpu.CPU;
import edu.gw.csci.simulator.isa.IOInstruction;
import edu.gw.csci.simulator.isa.Instruction;
import edu.gw.csci.simulator.isa.InstructionType;
import edu.gw.csci.simulator.memory.AllMemory;
import edu.gw.csci.simulator.registers.AllRegisters;
import edu.gw.csci.simulator.registers.Register;
import edu.gw.csci.simulator.registers.RegisterDecorator;
import edu.gw.csci.simulator.registers.RegisterType;
import edu.gw.csci.simulator.utils.BitConversion;
import javafx.scene.control.TextArea;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class IO {

    private static final Logger LOGGER = LogManager.getLogger(IO.class);


    public static class IN implements IOInstruction {

        private InstructionType instructionType = InstructionType.IN;

        private String data;

        @Override
        public void execute(AllMemory memory, AllRegisters registers,CPU cpu) {
            String Rs = data.substring(0,2);
            Register R =registers.getRegister(RegisterType.getGeneralPurpose(Rs));
            RegisterDecorator Rd = new RegisterDecorator(R);
            String DevID = data.substring(5, 10);
            Register PC = registers.getRegister(RegisterType.PC);
            RegisterDecorator PCd = new RegisterDecorator(PC);

            if(DevID.equals("00000")) {
                //Console Keyboard input
                Optional<String> NextInput = cpu.getNextInput();
                if(NextInput.isPresent()) {
                    int data = Integer.parseInt(NextInput.get());
                    Rd.setValue(data);
                    String mess = String.format("IN input Int:%d,Binary:%s to Register %s from Console Keyboard",
                            Rd.toInt(), Rd.toBinaryString(), R.getName());
                    LOGGER.info(mess);
                    //registers.PCadder();
                }
                else{
                    int PCindex = BitConversion.convert(PC.getData());
                    PCd.setValue(PCindex-1);
                    //registers.PCadder();
                    LOGGER.info("Please input first.");
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

        @Override
        public void setConsole(TextArea textArea) {

        }
    }

    public static class OUT implements IOInstruction {

        private InstructionType instructionType = InstructionType.OUT;
        private TextArea console;
        private String data;

        @Override
        public void execute(AllMemory memory, AllRegisters registers,CPU cpu) {
            String Rs = data.substring(0,2);
            Register R =registers.getRegister(RegisterType.getGeneralPurpose(Rs));
            RegisterDecorator Rd = new RegisterDecorator(R);
            String DevID = data.substring(5, 10);
            if (DevID.equals("00001")) {
                //Console Printer output
                String mess = String.format("OUT output Int :%d,Binary: %s from Register %s to Console Printer",
                       Rd.toInt(), Rd.toBinaryString(), R.getName());
                LOGGER.info(mess);
                cpu.consoleOutput.add(String.valueOf(Rd.toInt()));
                //registers.PCadder();
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

        @Override
        public void setConsole(TextArea textArea) {
            this.console = textArea;
        }
    }
}
