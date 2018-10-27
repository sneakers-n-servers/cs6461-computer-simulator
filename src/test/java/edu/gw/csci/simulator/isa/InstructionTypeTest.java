package edu.gw.csci.simulator.isa;

import org.junit.Assert;
import org.junit.Test;

public class InstructionTypeTest {

    @Test
    public void testOctal(){
        InstructionType it = InstructionType.LDX;
        Assert.assertEquals(33, it.getOpCode());
    }

    @Test
    public void testDecodeLDR(){
        String binaryOpCode = "100001";
        InstructionType it = InstructionType.getInstructionType(binaryOpCode);
        Assert.assertEquals(it, InstructionType.LDX);
    }

    @Test
    public void testNull(){
        String binaryOpCode = "";
        InstructionType it = InstructionType.getInstructionType(binaryOpCode);
        Assert.assertNull(it);
    }
}