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
import utilitytypes.IModule;

/**
 *
 * @author millerti
 */
public class FloatDiv extends PipelineStageBase {
    int stall_counter = 0;
    
    public FloatDiv(IModule parent) {
        super(parent, "FloatDiv");
    }
    
    
    @Override
    public void compute(Latch input, Latch output) {
        if (input.isNull()) return;
        doPostedForwarding(input);
        
        if (stall_counter < 15) {
            stall_counter++;
            this.setResourceWait("Loop"+stall_counter);
            return;
        }
        stall_counter = 0;

        InstructionBase ins = input.getInstruction();
        
        float source1 = ins.getSrc1().getFloatValue();
        float source2 = ins.getSrc2().getFloatValue();

        float result = source1 / source2;
        
        output.setInstruction(ins);
        output.setResultFloatValue(result);
    }
    
}
