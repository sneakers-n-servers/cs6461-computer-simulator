package edu.gw.csci.simulator.gui;

import edu.gw.csci.simulator.cpu.CPU;
import edu.gw.csci.simulator.isa.Program;
import edu.gw.csci.simulator.memory.Memory;
import edu.gw.csci.simulator.memory.MemoryChunk;
import edu.gw.csci.simulator.memory.MemoryChunkDecorator;
import edu.gw.csci.simulator.registers.AllRegisters;
import edu.gw.csci.simulator.registers.Register;
import edu.gw.csci.simulator.registers.RegisterDecorator;
import edu.gw.csci.simulator.registers.RegisterType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class "connects" the GUI to the simulated computer.
 * It translates user events to machine operations and vice-versa.
 *
 * @version 20180918
 */
public class Controller {

    private static final Logger LOGGER = LogManager.getLogger(Controller.class);

    @FXML
    private TableView<Register> registerTable;

    @FXML
    private TableColumn<Register, String> registerNameColumn;

    @FXML
    private TableColumn<Register, String> registerBinaryColumn;

    @FXML
    private TableColumn<Register, String> registerDecimalColumn;

    @FXML
    private Pagination memoryPagination;

    @FXML
    private TableView<MemoryChunk> memoryTable;

    @FXML
    private TableColumn<MemoryChunk, String> memoryIndexColumn;

    @FXML
    private TableColumn<MemoryChunk, String> memoryBinaryColumn;

    @FXML
    private TableColumn<MemoryChunk, String> memoryDecimalColumn;

    @FXML
    private ComboBox<String> programNameSelector;

    @FXML
    private TextArea programContents;

    @FXML
    private TextArea consoleInput;

    private AllRegisters allRegisters;
    private Memory memory;
    private HashMap<String, Program> programs;
    private CPU cpu;

    @FXML
    protected void runIPL() {
        LOGGER.info("Initializing machine");
        allRegisters.initializeRegisters();
        memory.initialize();
    }

    public Controller() {
        this.allRegisters = new AllRegisters();
        this.memory = new Memory();
        this.programs = new HashMap<>();
        this.cpu = new CPU(memory, allRegisters);
    }

    @FXML
    private void initialize() {
        initializeRegisters();
        initializeMemory();
        initializePrograms();
    }

    /**
     * This function performs the proper binding of the registers to the GUI. Each register column is set with a value
     * factory that converts the {@link Register} to a desired format. The initial list of registers used to populate the
     * table is generated and set. Finally, each register value is bound to refresh the table when modified.
     * This provides convenience throughout the simulators operation.
     */
    private void initializeRegisters() {
        registerNameColumn.setCellValueFactory(cellData -> new RegisterDecorator(cellData.getValue()).getRegisterName());
        registerBinaryColumn.setCellValueFactory(cellData -> new BitDecorator<>(cellData.getValue()).toBinaryObservableString());
        registerDecimalColumn.setCellValueFactory(cellData -> new BitDecorator<>(cellData.getValue()).toLongObservableString());

        ObservableList<Register> registerList = FXCollections.observableArrayList();
        for (Map.Entry<RegisterType, Register> registerEntry : allRegisters.getRegisters()) {
            registerList.add(registerEntry.getValue());
        }
        registerTable.setItems(registerList);
        allRegisters.bindTableView(registerTable);
    }

    /**
     * This function performs the proper binding of the memory to the GUI. Each memory column is set with a value
     * factory that converts the {@link MemoryChunk} to a desired format. The initial list of memory locations used
     * to populate the table is generated and set. Finally each memmory location is bound to refresh the table when modified.
     * This provides convenience throughout the simulator's operation.
     */
    private void initializeMemory() {
        memoryIndexColumn.setCellValueFactory(cellData -> new MemoryChunkDecorator(cellData.getValue()).getIndex());
        memoryBinaryColumn.setCellValueFactory(cellData -> new BitDecorator<>(cellData.getValue()).toBinaryObservableString());
        memoryDecimalColumn.setCellValueFactory(cellData -> new BitDecorator<>(cellData.getValue()).toLongObservableString());
        ObservableList<MemoryChunk> memoryList = FXCollections.observableArrayList();
        memoryList.addAll(Arrays.asList(memory.getMemory()));
        memoryTable.setItems(memoryList);
        memory.bindTableView(memoryTable);
        memoryPagination.setPageFactory(pageIndex -> {
            int fromIndex = pageIndex * 32;
            int toIndex = Math.min(fromIndex + 32, memory.getSize());
            memoryTable.setItems(FXCollections.observableList(memoryList.subList(fromIndex, toIndex)));
            return new BorderPane(memoryTable);
        });
        int maxPages = (int) Math.ceil((double) memory.getSize() / 32);
        memoryPagination.setPageCount(maxPages);
    }

    /**
     * This function initializes {@link Program}s within the simulator. The default
     * program is named FreeRun, which initializes empty, and left to the console
     * user to define. Switching between programs clears the contents of the text area
     * and loads the contents of newly selected program.
     */
    private void initializePrograms(){
        //We need to pre-populate Program1, so this code will change
        Program freeRun = new Program("FreeRun");
        programs.put("FreeRun", freeRun);

        Program program1 = new Program("Program1");
        programs.put("Program1", program1);

        //Set the ComboBox to all of our programs
        Set<String> allPrograms = programs.keySet();
        String[] programNames = new String[allPrograms.size()];
        programNames = allPrograms.toArray(programNames);
        Arrays.sort(programNames);
        programNameSelector.getItems().addAll(programNames);

        //Set the default program
        programNameSelector.setValue(freeRun.getName());

        //Handle switching programs
        programNameSelector.setOnAction(event -> {
            programContents.clear();
            String programName = programNameSelector.getValue();
            LOGGER.info(String.format("Loading program %s", programName));
            Program selected = programs.get(programName);
            for(String line : selected.getLines()){
                programContents.appendText(line + "\n");
            }
        });
    }

    @FXML
    private void runProgram(){
        String programName = programNameSelector.getValue();
        Program program = programs.get(programName);
        cpu.setProgram(program);
        cpu.execute();
    }

    /**
     * We define a save program routine to overcome the fact that JavaFX wants
     * to execute a routine every time the program's contents are modified. Adding a timer
     * would resolve this issue, but doing so adds a complication with little
     * benefit. Having an explicit save function makes sense, and overcomes this hurdle.
     */
    @FXML
    private void saveProgram(){
        String name = programNameSelector.getValue();
        LOGGER.info(String.format("Saving the contents of program %s", name));
        String contents = programContents.textProperty().get();
        Program program = programs.get(name);
        program.clearContents();
        String[] lines = contents.split("\n");
        for(String line : lines){
            program.appendLine(line);
        }
    }

//    @FXML
//    void execute(ActionEvent event) {
//        if (IRinput.getText().length() == 16) {
//            String IRinputS = IRinput.getText();
//            Register IR = allRegisters.getRegister(RegisterType.IR);
//            if (isBinary(IRinputS)) {
//                int IRinputI = Integer.parseInt(IRinputS, 2);
//                BitSet bs = BitConversion.convert(IRinputI);
//                IR.setData(bs);
//                //Execute.execute_IR(allRegisters, memory);
//                registerTable.refresh();
//            }
//            LOGGER.info("Wrong instruction.");
//        } else {
//            LOGGER.info("Instruction should be 16 bits.");
//        }
//    }

//    private static boolean isBinary(String str) {
//        Pattern pattern = Pattern.compile("[0-1]*");
//        return pattern.matcher(str).matches();
//    }
//
//    private static boolean isNumeric(String str) {
//        Pattern pattern = Pattern.compile("[0-9]*");
//        return pattern.matcher(str).matches();
//    }
}
