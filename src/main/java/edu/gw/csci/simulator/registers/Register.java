package edu.gw.csci.simulator.registers;

import edu.gw.csci.simulator.Bits;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.BitSet;

/**
 * Leverages the defined register type to extend properties to other registers.
 *
 * @version 20180918
 */
public class Register extends Bits {

    private final RegisterType registerType;
    private ObjectProperty<BitSet> data;

    public Register(RegisterType registerType) {
        if (registerType.getSize() > 64) {
            throw new IllegalArgumentException("Can't instantiate register size larger than 64 bits");
        }
        this.registerType = registerType;
        this.data = new SimpleObjectProperty<>();
    }

    @Override
    public void initialize() {
        BitSet bitSet = new BitSet(registerType.getSize());
        data.set(bitSet);
    }

    @Override
    public BitSet getData() {
        return data.get();
    }

    @Override
    public int getSize() {
        return registerType.getSize();
    }

    public String getName() {
        return registerType.toString();
    }

    public void setData(BitSet data) {
        this.data.setValue(data);
    }

    public ObjectProperty<BitSet> getBitSetProperty() {
        return data;
    }
}
