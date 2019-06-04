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
public class IntDiv extends PipelineStageBase {
    int stall_counter = 0;
    
    public IntDiv(IModule parent) {
        super(parent, "IntDiv");
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
        
        int src1 = ins.getSrc1().getValue();
        int src2 = ins.getSrc2().getValue();

        int result = 0;
        switch (ins.getOpcode()) {
            case DIV:
                result = src1 / src2;
                break;
            case MOD:
                result = src1 % src2;
                break;
        }
        
        output.setInstruction(ins);
        output.setResultValue(result);
    }
    
}
