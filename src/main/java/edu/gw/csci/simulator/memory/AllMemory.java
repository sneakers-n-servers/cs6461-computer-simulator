package edu.gw.csci.simulator.memory;

import edu.gw.csci.simulator.exceptions.IllegalMemoryAccess;
import edu.gw.csci.simulator.exceptions.MemoryOutOfBounds;
import edu.gw.csci.simulator.isa.InstructionType;
import edu.gw.csci.simulator.registers.AllRegisters;
import edu.gw.csci.simulator.registers.Register;
import edu.gw.csci.simulator.registers.RegisterDecorator;
import edu.gw.csci.simulator.registers.RegisterType;
import edu.gw.csci.simulator.utils.BitConversion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.BitSet;
import java.util.Optional;

/**
 * Provides abstraction of support operations for interaction between the simulated memory and the GUI framework.
 *
 * @version 20180918
 */
public class AllMemory {

    private static final Logger LOGGER = LogManager.getLogger(AllMemory.class);

    private final Memory memory;
    private final AllRegisters allRegisters;
    private final MemoryCache memoryCache;

    private final static int highestReservedMemory = 5;
    private final int maxMemory;

    public AllMemory(Memory memory, AllRegisters allRegisters, MemoryCache memoryCache) {
        this.memory = memory;
        this.allRegisters = allRegisters;
        this.memoryCache = memoryCache;
        this.maxMemory = memory.getSize() - 1;
    }

    public void store(int value, int index) throws MemoryOutOfBounds, IllegalMemoryAccess {
        checkIndex(index, true);
        String mess = String.format("Stroing %d(%s) to memory index %d", value, BitConversion.toBinaryString(value,16), index);
        LOGGER.info(mess);
        String BinaryValue = Integer.toBinaryString(value);
        //word is over 16 bits
        if (BinaryValue.length() > 16) {
            mess = "Word is over 16 bits.";
            LOGGER.error(mess);
            return;
        }
        BitSet store = BitConversion.convert(value);
        memory.set(index, store);
        allRegisters.setRegister(RegisterType.MBR, store);

    }

    /**
     * Fetches an address from memory. If the memory location is cached, it will return
     * the cached value, otherwise it will get the data from memory. The MAR is updated to
     * the index being fetched, and the MBR is updated with the value.
     *
     * @param index The index to fetch
     * @return The contents of memory, or cache, as appropriate.
     * @throws MemoryOutOfBounds   When the memory index is out of bounds
     * @throws IllegalMemoryAccess When the memory index is reserved
     */
    public BitSet fetch(int index) throws MemoryOutOfBounds, IllegalMemoryAccess {
        //checkIndex(index, true);
        BitSet toFetch = BitConversion.convert(index);
        allRegisters.setRegister(RegisterType.MAR, toFetch);
        Optional<BitSet> bits = memoryCache.get(index);
        BitSet fetched = bits.orElseGet(() -> memory.get(index));
        allRegisters.setRegister(RegisterType.MBR, fetched);
        int value = BitConversion.convert(fetched);
        String mess = String.format("Fetching %d(%s) from memory index %d", value, BitConversion.toBinaryString(value,16), index);
        LOGGER.info(mess);
        return fetched;
    }

    /**
     * Checks to make sure that the memory address is valid, and if so, sets the bits in memory.
     * The memory buffer register is updated to value set, and the data is placed in the cache.
     *
     * @param index  The index of the data to store
     * @param bitSet The data to store
     * @throws MemoryOutOfBounds   When the memory index is out of bounds
     * @throws IllegalMemoryAccess When the memory index is reserved
     */
    public void store(int index, BitSet bitSet) throws MemoryOutOfBounds, IllegalMemoryAccess {
        checkIndex(index, true);
        int value = BitConversion.convert(bitSet);
        String mess = String.format("Stroing %d(%s) to memory index %d", value, BitConversion.toBinaryString(bitSet,16), index);
        LOGGER.info(mess);
        memory.set(index, bitSet);
        allRegisters.setRegister(RegisterType.MBR, bitSet);
        memoryCache.put(index, bitSet);
    }

    /**
     * We need to check to make sure that the memory being set, or fetched is a valid location.
     * Making sure that the memory is in bounds is something that should always occur, but during
     * the implementation of TRAP codes in part 3, there are circumstances where we need to access
     * the special memory locations. This is why the throwReserve flag exists
     *
     * @param index An index to check the validity of
     * @param throwReseve Whether or not to throw an exception if trying to access a reserved location
     * @throws MemoryOutOfBounds When the memory index is out of bounds
     * @throws IllegalMemoryAccess When the memory index is reserved
     */
    private void checkIndex(int index, boolean throwReseve) throws MemoryOutOfBounds, IllegalMemoryAccess {
        if(index > maxMemory){
            String mess = String.format("Will not store/fetch data in reserved location %d", index);
            LOGGER.error(mess);
            throw new MemoryOutOfBounds(mess);
        }
        if(index <= highestReservedMemory && throwReseve){
            String mess = String.format("Will not store/fetch index: %d higher than max: %d", index, maxMemory);
            LOGGER.error(mess);
            throw new IllegalMemoryAccess(mess);
        }
    }

    public int EA(){
        int EA;
        Register IR = allRegisters.getRegister(RegisterType.IR);
        RegisterDecorator IRd = new RegisterDecorator(IR);
        String instruction = IRd.toBinaryString();
        String Opcode = instruction.substring(0, 6);
        String IX_code = instruction.substring(8, 10);
        String I_code = instruction.substring(10, 11);
        String Address_code = instruction.substring(11, 16);

        if (InstructionType.getInstructionType(Opcode).equals(InstructionType.RFS)) {
                //I,IX is ignored in RFS
                EA = Integer.parseInt(Address_code, 2);
                return EA;
            }
            if (InstructionType.getInstructionType(Opcode).equals(InstructionType.AIR) || InstructionType.getInstructionType(Opcode).equals(InstructionType.SIR)) {
                //I,IX is ignored in AIR,SIR
                EA = Integer.parseInt(Address_code, 2);
                return EA;
            }
            if (InstructionType.getInstructionType(Opcode).equals(InstructionType.LDX) ||
                    InstructionType.getInstructionType(Opcode).equals(InstructionType.STX)) {
                //IX is not used in LDX,STX
                if (I_code.equals("0")) {
                    return Integer.parseInt(Address_code, 2);
                }
                else {
                    EA = BitConversion.convert(fetch(Integer.parseInt(Address_code, 2)));
                    return EA;
                }
            }
            if (I_code.equals("0")) {
                if (IX_code.equals("00")) {
                    EA = Integer.parseInt(Address_code, 2);
                    return EA;
                } else {
                    //int ixCode = BitConversion.fromBinaryString(IX_code);
                    RegisterType registerType = RegisterType.getIndex(IX_code);
                    Register register = allRegisters.getRegister(registerType);
                    RegisterDecorator Xd = new RegisterDecorator(register);
                    EA = Xd.toInt() + Integer.parseInt(Address_code, 2);
                    return EA;
                }
            }
            else {
                if (IX_code.equals("00")) {
                    EA = BitConversion.convert(fetch(Integer.parseInt(Address_code, 2)));
                    return EA;
                } else {
                    //int ixCode = BitConversion.fromBinaryString(IX_code);
                    RegisterType registerType = RegisterType.getIndex(IX_code);
                    Register register = allRegisters.getRegister(registerType);
                    RegisterDecorator Xd = new RegisterDecorator(register);
                    int index = Xd.toInt() + Integer.parseInt(Address_code, 2);
                    EA = BitConversion.convert(fetch(index));
                    return EA;
                }
            }
        }

}
