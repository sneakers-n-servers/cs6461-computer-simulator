/*
Project 1 - Memory

%%%%%    %%%%%%%%%%    %%%%%%%%%%    %%%%%
%%%%%    Revision:    20180921
%%%%%    %%%%%%%%%%    %%%%%%%%%%    %%%%%
*/

/*
%%%%%%%%%%
REVISION HISTORY

%%%     20180921
        Modified:   
    -Changed memory type to int - from short

%%%     20180920
        Modified:   
    -Added debug memory output to setWords()

%%%     20180916
        Author: Group 9    
        Project:    CSCI_6461_F18_Project1
    -Initial release

%%%%%%%%%%
*/

//IMPORT
import edu.gw.csci.simulator.exceptions.*;
import java.util.Arrays;

//BEGIN
public class Memory {
  //CONSTANTS
  private static final int DEFAULT_MEMORY_SIZE_INWORDS = 2048;
  private static final int DEFAULT_MEMORY_WORD_SIZE_INBITS = 16;
  private static final int DEFAULT_MEMORY_MASK = (int)Math.pow(2, DEFAULT_MEMORY_WORD_SIZE_INBITS) - 1;
  // debug constants
  private static final int MODULE_DEBUG_FLAG = 0;

  //VARIABLES
  private int[] memory;

  //CONSTRUCTORS
  public Memory() {
    memory = new int[DEFAULT_MEMORY_SIZE_INWORDS];
    if (MODULE_DEBUG_FLAG == 1) {
      System.out.println("No memory size specified; default used: " + DEFAULT_MEMORY_SIZE_INWORDS + ".");
    }
  }
  public Memory(int memory_size_inwords) {
    memory = new int[memory_size_inwords];
    if (MODULE_DEBUG_FLAG == 1) {
      System.out.println("Memory size specified: " + memory_size_inwords + " words.");
    }
  }

  //ACCESS
  // return size
  public int getSize() {
    return memory.length;
  }
  // get values from the memory
  public void getWords(int[] destination, int first_index, int number_of_words) 
    throws projectexceptions.MemoryOutOfBounds{
    //VARIABLES
    int error = 0;
    String exception_string = "No exception information.";
    // check for index errors
    if ((first_index < 0) || (number_of_words < 0) || (first_index + number_of_words > this.getSize())) {
      // create exception message
      exception_string = "Memory out of bounds!!!\n"
        + "(First index = " + first_index
        + ") + (Number of words = " + number_of_words + ") assumes " + (first_index + number_of_words)
        + " word(s) but max available would be " + (this.getSize() - first_index) 
        + " word(s) from that index.";
      // optionally display debug info
      if (MODULE_DEBUG_FLAG == 1) {
        System.out.println(exception_string);
      }
      // tag error flag
      error = 1;
    }
    // handle the error case in which at least the first index is valid
    if (error == 1) {
      if ((first_index >= 0) && (first_index < this.getSize())) {
        // modify the upper bound
        number_of_words = this.getSize() - first_index;
      } else {
        // even first index is bad so just quit
        throw new projectexceptions.MemoryOutOfBounds("\n" + exception_string);
      }
    }
    // ok... now actually get the data
    try {
      System.arraycopy(memory, first_index, destination, 0, number_of_words);
    } catch(ArrayIndexOutOfBoundsException bound_error) {
      // in case there was a weird out of bounds
      throw new projectexceptions.MemoryOutOfBounds("\n\nSort of MemoryOutOfBounds but we missed it\n\n.");
    }
    // don't forget to throw exception if only some data were copied
    if (error == 1) {
      throw new projectexceptions.MemoryOutOfBounds("\n" + exception_string 
        + "\nCopied " + number_of_words + " word(s) before throw.");
    }
  }

  // put values into the memory
  public void setWords(int[] source, int first_index) 
    throws projectexceptions.MemoryOutOfBounds{
    //VARIABLES
    int number_of_words = source.length;
    int error = 0;
    String exception_string = "No exception information.";
    // check for index errors
    if ((first_index < 0) || (number_of_words < 0) || (first_index + number_of_words > this.getSize())) {
      // create exception message
      exception_string = "Memory out of bounds!!!\n"
        + "(First index = " + first_index
        + ") + (Number of words = " + number_of_words + ") assumes " + (first_index + number_of_words)
        + " word(s) but max available would be " + (this.getSize() - first_index) 
        + " word(s) from that index.";
      // optionally display debug info
      if (MODULE_DEBUG_FLAG == 1) {
        System.out.println(exception_string);
      }
      // tag error flag
      error = 1;
    }
    // handle the error case in which at least the first index is valid
    if (error == 1) {
      if ((first_index >= 0) && (first_index < this.getSize())) {
        // modify the upper bound
        number_of_words = this.getSize() - first_index;
      } else {
        // even first index is bad so just quit
        throw new projectexceptions.MemoryOutOfBounds("\n" + exception_string);
      }
    }
    // cast the input data just in case
    int[] masked_source = new int[number_of_words];
    for (int i = 0; i < number_of_words; i++) {
      masked_source[i] = source[i] & DEFAULT_MEMORY_MASK;
    }
    // now put
    try {
      System.arraycopy(masked_source, 0, memory, first_index, number_of_words);
      if (MODULE_DEBUG_FLAG == 1) {
        System.out.println("Memory: " + Arrays.toString(memory) + ".");
      }
    } catch(ArrayIndexOutOfBoundsException bound_error) {
      // in case there was a weird out of bounds
      throw new projectexceptions.MemoryOutOfBounds("\n\nSort of MemoryOutOfBounds but we missed it\n\n.");
    }
    // don't forget to throw exception if only some data were copied
    if (error == 1) {
      throw new projectexceptions.MemoryOutOfBounds("\n" + exception_string 
        + "\nCopied " + number_of_words + " word(s) before throw.");
    }
  }
}

