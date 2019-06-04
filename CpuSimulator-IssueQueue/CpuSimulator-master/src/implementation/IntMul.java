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
public class IntMul extends FunctionalUnitBase {

    public IntMul(IModule parent) {
        super(parent, "IntMul");
    }

    
    private static class Mul extends PipelineStageBase {
        public Mul(IModule parent) {
            super(parent, "in");
        }
        
        @Override
        public void compute(Latch input, Latch output) {
            if (input.isNull()) return;
            doPostedForwarding(input);
            InstructionBase ins = input.getInstruction();

            int source1 = ins.getSrc1().getValue();
            int source2 = ins.getSrc2().getValue();

            int result = source1 * source2;
                        
            output.setResultValue(result);
            output.setInstruction(ins);
        }
    }
    
    @Override
    public void createPipelineRegisters() {
        createPipeReg("MulToDelay");
    }

    @Override
    public void createPipelineStages() {
        addPipeStage(new Mul(this));
    }

    @Override
    public void createChildModules() {
        IFunctionalUnit child = new MultiStageDelayUnit(this, "Delay", 3);
        addChildUnit(child);
        this.addRegAlias("Delay.out", "out");
    }

    @Override
    public void createConnections() {
        connect("in", "MulToDelay", "Delay");
    }

    @Override
    public void specifyForwardingSources() {
        addForwardingSource("out");
    }    
}
