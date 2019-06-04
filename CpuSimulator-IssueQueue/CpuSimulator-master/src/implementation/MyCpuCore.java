/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation;

import baseclasses.PipelineRegister;
import baseclasses.PipelineStageBase;
import baseclasses.CpuCore;
import examples.MultiStageFunctionalUnit;
import tools.InstructionSequence;
import utilitytypes.IGlobals;
import utilitytypes.IPipeReg;
import utilitytypes.IPipeStage;
import static utilitytypes.IProperties.*;
import utilitytypes.IRegFile;
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
        IRegFile rf = ((IGlobals)properties).getRegisterFile();
        int[] rat = ((IGlobals)properties).getPropertyIntArray("rat");
        for (int r=0; r<32; r++) {
            rf.changeFlags(r, IRegFile.SET_USED, 0);
            rat[r] = r;
        }
    }
    
    public void loadProgram(InstructionSequence program) {
        getGlobals().loadProgram(program);
    }
    
    private void freePhysRegs() {
        IGlobals globals = getGlobals();
        IRegFile regfile = globals.getRegisterFile();
        boolean printed = false;
        for (int i=0; i<256; i++) {
            if (regfile.isUsed(i)) {
                if (!regfile.isInvalid(i) && regfile.isRenamed(i)) {
                    regfile.markUsed(i, false);
                    if (!printed) {
                        Logger.out.print("# Freeing:");
                        printed = true;
                    }
                    Logger.out.print(" P" + i);
                }
            }
        }
        if (printed) Logger.out.println();
    }
    
    public void runProgram() {
        properties.setProperty("running", true);
        while (properties.getPropertyBoolean("running")) {
            Logger.out.println("## Cycle number: " + cycle_number);
            freePhysRegs();
            advanceClock();
        }
    }

    @Override
    public void createPipelineRegisters() {
        // To individual stages
        createPipeReg("FetchToDecode");
        createPipeReg("DecodeToIssueQ");
        createPipeReg("IssueQToExecute");
        createPipeReg("IssueQToMemory");
        createPipeReg("IssueQToIntDiv");
        createPipeReg("IssueQToFloatDiv");
        // To functional units
        createPipeReg("IssueQToIntMul");
        createPipeReg("IssueQToFloatAddSub");
        createPipeReg("IssueQToFloatMul");
        // From individual stages to writeback
        createPipeReg("IDivToWriteback");
        createPipeReg("FDivToWriteback");
        createPipeReg("ExecuteToWriteback");
    }

    @Override
    public void createPipelineStages() {
        addPipeStage(new AllMyStages.Fetch(this));
        addPipeStage(new AllMyStages.Decode(this));
        addPipeStage(new AllMyStages.IssueQ(this));
        addPipeStage(new AllMyStages.Execute(this));
        addPipeStage(new IntDiv(this));
        addPipeStage(new FloatDiv(this));
        addPipeStage(new AllMyStages.Writeback(this));
    }

    @Override
    public void createChildModules() {
        addChildUnit(new MemUnit(this));
        addChildUnit(new IntMul(this));
        addChildUnit(new FloatMul(this));
        addChildUnit(new FloatAddSub(this));
    }

    @Override
    public void createConnections() {
        // Connect pipeline elements by name.  Notice that 
        // Decode has two outputs, anle to send to either Memory OR Execute 
        // and that Writeback has two inputs, able to receive from both
        // Execute and Memory.  
        // Memory no longer connects to Execute.  It is now a fully 
        // independent functional unit, parallel to Execute.
        connect("Fetch", "FetchToDecode", "Decode");
        connect("Decode", "DecodeToIssueQ", "IssueQ");        
        // To individual stages
        connect("IssueQ", "IssueQToExecute", "Execute");          // Stage
        connect("IssueQ", "IssueQToMemory", "MemUnit");           // Stage
        connect("IssueQ", "IssueQToIntDiv", "IntDiv");            // Stage
        connect("IssueQ", "IssueQToFloatDiv", "FloatDiv");        // Stage
        // To functional units
        connect("IssueQ", "IssueQToIntMul", "IntMul");            // Unit
        connect("IssueQ", "IssueQToFloatAddSub", "FloatAddSub");  // Unit
        connect("IssueQ", "IssueQToFloatMul", "FloatMul");        // Unit
        
        // From stages
        connect("Execute", "ExecuteToWriteback", "Writeback");
        connect("IntDiv", "IDivToWriteback", "Writeback");
        connect("FloatDiv", "FDivToWriteback", "Writeback");
        // From functional units
        connect("IntMul", "Writeback");
        connect("FloatMul", "Writeback");
        connect("FloatAddSub", "Writeback");
        connect("MemUnit", "Writeback");
    }

    @Override
    public void specifyForwardingSources() {
        addForwardingSource("ExecuteToWriteback");
        addForwardingSource("FDivToWriteback");
        addForwardingSource("IDivToWriteback");
        // Forwarding sources for submodules are specified in the
        // specifyForwardingSources method of each module.
//        addForwardingSource("IntMul.out");    // TODO:  Find the output automatically
//        addForwardingSource("FloatMul.out");
//        addForwardingSource("FloatAddSub.out");
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
