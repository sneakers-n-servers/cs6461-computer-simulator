package edu.gw.csci.simulator.cpu;

import edu.gw.csci.simulator.Bits;
import edu.gw.csci.simulator.exceptions.*;
import edu.gw.csci.simulator.memory.AllMemory;
import edu.gw.csci.simulator.memory.Memory;
import edu.gw.csci.simulator.memory.MemoryChunk;
import edu.gw.csci.simulator.registers.AllRegisters;
import edu.gw.csci.simulator.registers.Register;
import edu.gw.csci.simulator.registers.RegisterDecorator;
import edu.gw.csci.simulator.registers.RegisterType;
import edu.gw.csci.simulator.utils.BitConversion;
import javafx.scene.control.TableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.BitSet;

public class TrapController {

    private CPU cpu;
    private AllMemory allMemory;
    private AllRegisters allRegisters;

    private TableView<? extends Bits> registerTable, memoryTable;

    private static final Logger LOGGER = LogManager.getLogger(SimulatorException.class);

    public static final int HALT_LOCATION = 6,
            TRAP_PC_LOCATION = 2,
            HALT_POINTER_LOCATION = 0;

    private static final int EXCEPTION_TABLE_POINTER = 2000,
            EXCEPTION_TABLE_SIZE = 16;

    public TrapController(CPU cpu) {
        this.cpu = cpu;
        this.allMemory = cpu.getAllMemory();
        this.allRegisters = allMemory.getAllRegisters();
    }

    /**
     * In the case of a cell edit event, we want to ensure that the
     * event is closed in case of a fault. This setter provides the
     * means to do this.
     *
     * @param registerTable The register table when running the GUI
     * @param memoryTable   The memory table when running the GUI
     */
    public void setTables(TableView<Register> registerTable, TableView<MemoryChunk> memoryTable) {
        this.registerTable = registerTable;
        this.memoryTable = memoryTable;
    }


    /**
     * Performs all of the logic associated with a trap, meaning that
     * we save the contents of PC to memory location {@link TrapController#TRAP_PC_LOCATION}, and load the
     * PC with the contents of memory location {@link TrapController#HALT_POINTER_LOCATION}. This value is defaulted
     * to 6, which if unmodified, instructs the simulator to halt.
     */
    public void setFault(int opCode) {
        Register machineFaultRegister = allRegisters.getRegister(RegisterType.MFR);
        new RegisterDecorator(machineFaultRegister).setValue(opCode);

        //Save off the current PC, plus 1
        Register pcRegister = allRegisters.getRegister(RegisterType.PC);
        RegisterDecorator pcDecorator = new RegisterDecorator(pcRegister);
        int nextValue = pcDecorator.toInt() + 1;
        allMemory.store(TRAP_PC_LOCATION, BitConversion.convert(nextValue), false);

        //Set the next instruction to the trap routine
        BitSet trapMemory = allMemory.fetch(HALT_POINTER_LOCATION, false);
        int pointer = BitConversion.convert(trapMemory);
        BitSet nextInstruction = allMemory.fetch(pointer + opCode);
        pcDecorator.setValue(nextInstruction);

        //If the fault occurs while editing, refresh
        if (registerTable != null) {
            registerTable.edit(-1, null);
            registerTable.refresh();
        }
        if (memoryTable != null) {
            memoryTable.edit(-1, null);
            memoryTable.refresh();
        }

        //LOGGER.info("Excecuting trap routine {} ", opCode);
        LOGGER.info("Excecuting trap routine {} ", opCode);
        cpu.execute();
    }


    /**
     * This routine is used by the {@link edu.gw.csci.simulator.isa.instructions.Miscellaneous.TRAP TRAP} instruction
     * in order to determine what type of exception to raise. This method returns the proper exception, with a
     * default case of {@link IllegalTrapCode}, in order for another routine to throw.
     *
     * @param value The error code
     * @return The desired exception
     */
    public static SimulatorException getException(int value) {
        SimulatorException ex;
        switch (value) {
            case IllegalMemoryAccess.OP_CODE:
                ex = new IllegalMemoryAccess("Received trap code: illegal memory access");
                break;
            case IllegalOpcode.OP_CODE:
                ex = new IllegalOpcode("Received trap code: illegal op code");
                break;
            case IllegalRegisterAccess.OP_CODE:
                ex = new IllegalRegisterAccess("Received trap code: illegal register access");
                break;
            case IllegalValue.OP_CODE:
                ex = new IllegalValue("Received trap code: illegal values");
                break;
            case MemoryOutOfBounds.OP_CODE:
                ex = new MemoryOutOfBounds("Received trap code: memory out of bounds");
                break;
            default:
                ex = new IllegalTrapCode("Received trap code: illegal trap code");
        }
        return ex;
    }

    /**
     * Creates a table in memory consisting of nothing but pointers to the halt location {@link TrapController#HALT_LOCATION}.
     * The number of entries in the table is dictated by the variable {@link TrapController#EXCEPTION_TABLE_SIZE}.
     */
    public void setDefaultExceptionTable() {
        LOGGER.info(
                "Setting default exception table at memory location {} for {} values -> {}",
                EXCEPTION_TABLE_POINTER,
                EXCEPTION_TABLE_SIZE,
                HALT_LOCATION
        );
        Memory memory = allMemory.getMemory();
        memory.set(HALT_POINTER_LOCATION, BitConversion.convert(EXCEPTION_TABLE_POINTER));
        BitSet defaultPointer = BitConversion.convert(HALT_LOCATION);
        for (int i = 0; i < EXCEPTION_TABLE_SIZE; i++) {
            memory.set(EXCEPTION_TABLE_POINTER + i, defaultPointer);
        }
    }
}
