package edu.gw.csci.simulator.cpu;

import edu.gw.csci.simulator.Simulator;
import edu.gw.csci.simulator.exceptions.IllegalOpcode;
import edu.gw.csci.simulator.exceptions.SimulatorException;
import edu.gw.csci.simulator.gui.Program;
import edu.gw.csci.simulator.isa.*;
import edu.gw.csci.simulator.memory.AllMemory;
import edu.gw.csci.simulator.memory.Memory;
import edu.gw.csci.simulator.memory.MemoryCache;
import edu.gw.csci.simulator.registers.AllRegisters;
import edu.gw.csci.simulator.registers.Register;
import edu.gw.csci.simulator.registers.RegisterDecorator;
import edu.gw.csci.simulator.registers.RegisterType;
import edu.gw.csci.simulator.utils.BitConversion;
import javafx.scene.control.TextArea;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;

/**
 * This is the main driver of our simulator. The CPU has instances of {@link AllRegisters registers},
 * and {@link AllMemory memory} for basic operation. The {@link Program prgram} is injected from the GUI and the
 * CPU loads it, and set the PC to proper location. We selected to start at index 32, given that it is the first
 * unused memory location. The console also injects console for use with IO instructions.
 */
public class CPU {

    private static final Logger LOGGER = LogManager.getLogger(CPU.class);

    private final AllMemory memory;
    private final AllRegisters registers;

    private Program program;
//    public TextArea consoleInput;
    public ArrayList<String> consoleInput;
    public ArrayList<String> consoleOutput;
    private Decoder decoder;
    public boolean TrapFlag;

    public CPU(AllMemory allMemory){
        this.memory = allMemory;
        this.registers = allMemory.getAllRegisters();
        this.decoder = new Decoder();
        this.consoleInput = new ArrayList<>();
        this.consoleOutput = new ArrayList<>();
        this.TrapFlag = false;
    }
    public Optional<String> getNextInput(){

        if(!consoleInput.isEmpty()){
            String current = consoleInput.get(0);
            consoleInput.remove(0);
            return Optional.of(current);
        }
        return Optional.empty();
    }

    public void setProgram(Program program){
        this.program = program;
    }

//    public void setTextArea(TextArea textArea){
//        this.consoleInput = textArea;
//    }


    /**
     * This method executes instruction in IR register until a HLT instruction is received.
     * This is handy because unused memory will automatically indicate a halt, there is no need to explicitly
     * declare one in the program. The GUI restricts one ability to call this function
     * unless the machine has been initialized, and a program has been set.
     */
    public void execute(){
        Instruction instruction = getNextInstruction(registers);
        while (instruction.getInstructionType() != InstructionType.HLT){
            instruction.execute(memory, registers,this);
            registers.PCadder();
            instruction = getNextInstruction(registers);
        }
        instruction.execute(memory, registers,this);

    }

    /**
     * This function returns the next instruction to execute by the CPU. It gets
     * passed the {@link RegisterDecorator} of the PC so we don't have to continually
     * create a new one, given that the current instruction index must be known.
     *
     * @param allRegisters All Registers
     * @return The next instruction to execute
     */
    private Instruction getNextInstruction(AllRegisters allRegisters){
        int nextInstructionIndex = getPCDecorator().toInt();
        BitSet instructionData = memory.fetch(nextInstructionIndex);
        Register IR = allRegisters.getRegister(RegisterType.IR);
        IR.setData(instructionData);
        return decoder.getInstruction(instructionData);
    }

    /**
     * This function creates a {@link RegisterDecorator} for the PC register.
     *
     * @return The PC register decorator
     */
    private RegisterDecorator getPCDecorator(){
        Register pc = registers.getRegister(RegisterType.PC);
        return new RegisterDecorator(pc);
    }


    /**
     * This function receives a program from the GUI, and loads it into memory.
     * The GUI restricts one to load a program before the machine is initialized,
     * so we know that all memory addresses, and registers have been instantiated.
     */
    public void loadProgram(){
        int defaultLoadLocation = 32;
        BitSet programCounter = BitConversion.convert(defaultLoadLocation);
        List<String> lines = program.getLines();
        for (String line : lines) {
            LOGGER.debug("Setting Line: " + line);
            BitSet convert = BitConversion.convert(line);
            memory.store(defaultLoadLocation, convert);
            defaultLoadLocation++;
        }
        registers.setRegister(RegisterType.PC, programCounter);
    }
    /**
     * This function receives a program from the GUI, and loads it into memory.
     * The start index of program is set by users.
     * The GUI restricts one to load a program before the machine is initialized,
     * so we know that all memory addresses, and registers have been instantiated.
     */
    public void loadProgram(int start) throws SimulatorException{
        int defaultLoadLocation = start;
        BitSet programCounter = BitConversion.convert(defaultLoadLocation);
        List<String> lines = program.getLines();
        for (String line : lines) {
            if (line.length() == 16) {
                LOGGER.info("Setting Line: " + line);
                BitSet convert = BitConversion.convert(line);
                memory.store(defaultLoadLocation, convert);
                defaultLoadLocation++;
            } else {
                String mess = "An instruction should be 16 bits.";
                LOGGER.error(mess);
            }
        }
        registers.setRegister(RegisterType.PC, programCounter);
    }

    /**
     * This function grabs the next instruction, executes it, and
     * adjusts the program counter.
     */
    public void step(){
        Instruction instruction = getNextInstruction(registers);
        try{
            instruction.execute(memory, registers,this);
            if(instruction.getInstructionType()!= InstructionType.HLT)
                registers.PCadder();
        }catch (SimulatorException e){
        }
    }


    public void StoreValue(int index,int value){
        BitSet b = BitConversion.convert(value);
        memory.store(index, b);
    }

    public void FileReader(){
        SimulatorFileReader.readSentences("program2_paragraph.txt",this);
    }
}
