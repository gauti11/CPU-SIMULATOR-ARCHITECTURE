/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation;

import baseclasses.FunctionalUnitBase;
import baseclasses.InstructionBase;
import baseclasses.Latch;
import baseclasses.PipelineStageBase;
import tools.MultiStageDelayUnit;
import utilitytypes.IFunctionalUnit;
import utilitytypes.IModule;

/**
 *
 * @author millerti
 */
public class FloatAddSub extends FunctionalUnitBase {

    public FloatAddSub(IModule parent) {
        super(parent, "FloatAddSub");
    }
    
    private static class FAdd extends PipelineStageBase {
        public FAdd(IModule parent) {
            super(parent, "in");
        }
        
        @Override
        public void compute(Latch input, Latch output) {
            if (input.isNull()) return;
            doPostedForwarding(input);
            InstructionBase ins = input.getInstruction();

            
            float source1 = ins.getSrc1().getFloatValue();
            float source2 = ins.getSrc2().getFloatValue();
            
            float result = 0;
            
            switch (ins.getOpcode()) {
                case FADD:
                    result = source1 + source2;
                    break;
                case FSUB:
                case FCMP:
                    result = source1 - source2;
                    break;                    
            }
            
            output.setResultFloatValue(result);
            output.setInstruction(ins);
        }
    }
    

    @Override
    public void createPipelineRegisters() {
        createPipeReg("FAddToDelay");
    }

    @Override
    public void createPipelineStages() {
        addPipeStage(new FAdd(this));
    }

    @Override
    public void createChildModules() {
        IFunctionalUnit child = new MultiStageDelayUnit(this, "Delay", 5);
        addChildUnit(child);
        addRegAlias("Delay.out", "out");
    }

    @Override
    public void createConnections() {
        connect("in", "FAddToDelay", "Delay");
    }

    @Override
    public void specifyForwardingSources() {
        addForwardingSource("out");
    }
    
}
