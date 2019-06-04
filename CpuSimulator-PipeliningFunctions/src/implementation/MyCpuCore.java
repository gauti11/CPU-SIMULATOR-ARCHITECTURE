/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation;

import baseclasses.PipelineRegister;
import baseclasses.PipelineStageBase;
import baseclasses.CpuCore;
//import examples.MultiStageFunctionalUnit;
import tools.InstructionSequence;
import utilitytypes.IPipeReg;
import utilitytypes.IPipeStage;
import static utilitytypes.IProperties.*;

import ParallelFunctionalUnits.FloatAddSub;
import ParallelFunctionalUnits.Memory;
import utilitytypes.Logger;
import voidtypes.VoidRegister;

/**
 * This is an example of a class that builds a specific CPU simulator out of
 * pipeline stages and pipeline registers.
 * 
 * @author 
 */
public class MyCpuCore extends CpuCore {
    static final String[] producer_props = {RESULT_VALUE};
        
    public void initProperties() {
        properties = new GlobalData();
    }
    
    public void loadProgram(InstructionSequence program) {
        getGlobals().loadProgram(program);
    }
    
    public void runProgram() {
        properties.setProperty("running", true);
        while (properties.getPropertyBoolean("running")) {
            Logger.out.println("## Cycle number: " + cycle_number);
            advanceClock();
        }
    }

    @Override
    public void createPipelineRegisters() {
        createPipeReg("FetchToDecode");
        createPipeReg("DecodeToExecute");
        createPipeReg("DecodeToMemory");
        //createPipeReg("DecodeToMSFU");
        //createPipeReg("MemUnit.AddrToLSQ");
        createPipeReg("DecodeToIntDiv");
        createPipeReg("DecodeToFloatDiv");
      //  createPipeReg("DecodeToIntMul");
        createPipeReg("DecodeToFloatAddSub");
        createPipeReg("DecodeToFloatMul");
        createPipeReg("ExecuteToWriteback");
       // createPipeReg("MemoryToWriteback");
        createPipeReg("IDivToWriteback");
        createPipeReg("FDivToWriteback");
    }

    @Override
    public void createPipelineStages() {
        addPipeStage(new AllMyStages.Fetch(this));
        addPipeStage(new AllMyStages.Decode(this));
        addPipeStage(new AllMyStages.Execute(this));
        //addPipeStage(new AllMyStages.Memory(this));
        addPipeStage(new AllMyStages.Writeback(this));
        addPipeStage(new AllMyStages.IntDiv(this));
        addPipeStage(new AllMyStages.FloatDiv(this));
        
    }

    @Override
    public void createChildModules() {
        // MSFU is an example multistage functional unit.  Use this as a
        // basis for FMul, IMul, and FAddSub functional units.
        //addChildUnit(new MultiStageFunctionalUnit(this, "MSFU",4));
        //cHILD units for the rest of the Functional Units
        addChildUnit(new FloatMul(this, "FloatMul",5));
       // addChildUnit(new IntMul(this, "IntMul",3));
        addChildUnit(new FloatAddSub(this, "FloatAddSub",5));
        addChildUnit(new Memory(this, "Memory",2));
        
 
    }

    @Override
    public void createConnections() {
        // Connect pipeline elements by name.  Notice that 
        // Decode has multiple outputs, able to send to Memory, Execute,
        // or any other compute stages or functional units.
        // Writeback also has multiple inputs, able to receive from 
        // any of the compute units.
        // NOTE: Memory no longer connects to Execute.  It is now a fully 
        // independent functional unit, parallel to Execute.
        
        // Connect two stages through a pipelin register
        connect("Fetch", "FetchToDecode", "Decode");
        
        // Decode has multiple output registers, connecting to different
        // execute units.  
        // "MSFU" is an example multistage functional unit.  Those that
        // follow the convention of having a single input stage and single
        // output register can be connected simply my naming the functional
        // unit.  The input to MSFU is really called "MSFU.in".
        connect("Decode", "DecodeToExecute", "Execute");
        connect("Decode", "DecodeToMemory", "Memory");
       // connect("Decode", "DecodeToMSFU", "MSFU");
        //Newly added connections
        connect("Decode", "DecodeToIntDiv", "IntDiv");
        connect("Decode", "DecodeToFloatDiv", "FloatDiv");
      //  connect("Decode", "DecodeToIntMul", "IntMul");
        connect("Decode", "DecodeToFloatAddSub", "FloatAddSub");
        connect("Decode", "DecodeToFloatMul", "FloatMul");
        
        // Writeback has multiple input connections from different execute
        // units.  The output from MSFU is really called "MSFU.Delay.out",
        // which was aliased to "MSFU.out" so that it would be automatically
        // identified as an output from MSFU.
        connect("Execute","ExecuteToWriteback", "Writeback");
      //  connect("Memory", "MemoryToWriteback", "Writeback");
       // connect("MSFU", "Writeback");
        //Connections added to the Writeback for both pipelined and non Pipelined.
        connect("IntDiv","IDivToWriteback", "Writeback");
        connect("FloatDiv","FDivToWriteback", "Writeback");
        connect("Memory", "Writeback");
     //   connect("IntMul", "Writeback");
        connect("FloatAddSub", "Writeback");
        connect("FloatMul", "Writeback");
        
    }

    @Override
    public void specifyForwardingSources() {
        addForwardingSource("ExecuteToWriteback");
        //addForwardingSource("MemoryToWriteback");
        addForwardingSource("Memory.out");
        addForwardingSource("FDivToWriteback");
        addForwardingSource("FloatMul.out");
        addForwardingSource("FloatAddSub.out");
        
        // MSFU.specifyForwardingSources is where this forwarding source is added
        // addForwardingSource("MSFU.out");
    }

    @Override
    public void specifyForwardingTargets() {
        // Not really used for anything yet
    }

    @Override
    public IPipeStage getFirstStage() {
        // CpuCore will sort stages into an optimal ordering.  This provides
        // the starting point.
        return getPipeStage("Fetch");
    }
    
    public MyCpuCore() {
        super(null, "core");
        initModule();
        printHierarchy();
        Logger.out.println("");
    }
}
