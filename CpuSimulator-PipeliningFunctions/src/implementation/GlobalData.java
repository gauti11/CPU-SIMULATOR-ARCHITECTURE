/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation;

import utilitytypes.IGlobals;

import java.util.ArrayList;

import tools.InstructionSequence;

/**
 * As a design choice, some data elements that are accessed by multiple
 * pipeline stages are stored in a common object.
 * 
 * TODO:  Add to this any additional global or shared state that is missing.
 * 
 * @author 
 */
public class GlobalData implements IGlobals {
    public InstructionSequence program;
	public int program_counter = 0;
    public int[] register_file = new int[32];
    public int register = 9999;
    public int value = 0;
    public int register_value = 0;
    public int[] memory = new int[105];
    public int memAddress = 0;
    public boolean[] register_invalid = new boolean[32];
    public boolean isStalled = false;
    public boolean branchTaken = false;
    public int cycle = 0;
    public boolean flushDecode = false;
    public int completedExecution = 0;
    public int globalCounter = 0;

    @Override
    public void reset() {
        program_counter = 0;
        register_file = new int[32];
    }
    
    
    // Other global and shared variables here....

}
