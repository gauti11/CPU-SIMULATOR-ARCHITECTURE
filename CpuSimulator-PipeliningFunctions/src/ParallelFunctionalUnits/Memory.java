/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ParallelFunctionalUnits;

import baseclasses.FunctionalUnitBase;
import baseclasses.InstructionBase;
import baseclasses.Latch;
import baseclasses.ModuleBase;
import baseclasses.PipelineStageBase;
import baseclasses.PropertiesContainer;
import implementation.GlobalData;

import static utilitytypes.IProperties.MAIN_MEMORY;

import java.util.Map;
import tools.MultiStageDelayUnit;
import tools.MyALU;
import utilitytypes.IFunctionalUnit;
import utilitytypes.IGlobals;
import utilitytypes.IModule;
import utilitytypes.IPipeReg;
import utilitytypes.IPipeStage;
import utilitytypes.IProperties;
import utilitytypes.Operand;
import voidtypes.VoidProperties;

/**
 *
 * @author millerti
 */
public class Memory extends FunctionalUnitBase {
	int numberofPipeline;
    public Memory(IModule parent, String name, int numberofPipeline) {
        super(parent, name);
        this.numberofPipeline=numberofPipeline;
    }

    private static class MyMathUnit extends PipelineStageBase {
        public MyMathUnit(IModule parent) {
            // For simplicity, we just call this stage "in".
            super(parent, "in");
//            super(parent, "in:Math");  // this would be fine too
        }
        
        @Override
        public void compute(Latch input, Latch output) {
            if (input.isNull()) return;
            doPostedForwarding(input);
            InstructionBase ins = input.getInstruction();
            setActivity(ins.toString());

            Operand oper0 = ins.getOper0();
            int oper0val = ins.getOper0().getValue();
            int source1 = ins.getSrc1().getValue();
            int source2 = ins.getSrc2().getValue();
            
            // The Memory stage no longer follows Execute.  It is an independent
            // functional unit parallel to Execute.  Therefore we must perform
            // address calculation here.
            int addr = source1 + source2;
            
            int value = 0;
            IGlobals globals = (GlobalData)getCore().getGlobals();
            int[] memory = globals.getPropertyIntArray(MAIN_MEMORY);

            switch (ins.getOpcode()) {
                case LOAD:
                    // Fetch the value from main memory at the address
                    // retrieved above.
                    value = memory[addr];
                    output.setResultValue(value);
                    output.setInstruction(ins);
                    addStatusWord("Mem[" + addr + "]");
                    break;
                
                case STORE:
                    // For store, the value to be stored in main memory is
                    // in oper0, which was fetched in Decode.
                    memory[addr] = oper0val;
                    addStatusWord("Mem[" + addr + "]=" + ins.getOper0().getValueAsString());
                    output.setInstruction(ins);
                    return;
                    
                default:
                    throw new RuntimeException("Non-memory instruction got into Memory stage");
            }
        }
    }
    
    @Override
    public void createPipelineRegisters() {
        createPipeReg("MathToDelay");  
    }

    @Override
    public void createPipelineStages() {
        addPipeStage(new MyMathUnit(this));
    }

    @Override
    public void createChildModules() {
        IFunctionalUnit child = new MultiStageDelayUnit(this, "Delay", numberofPipeline);
        addChildUnit(child);
    }

    @Override
    public void createConnections() {
        addRegAlias("Delay.out", "out");
        connect("in", "MathToDelay", "Delay");
    }

    @Override
    public void specifyForwardingSources() {
        addForwardingSource("out");
    }
}
