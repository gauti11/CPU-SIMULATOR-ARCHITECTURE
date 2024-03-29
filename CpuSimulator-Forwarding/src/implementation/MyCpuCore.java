/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation;

import baseclasses.PipelineRegister;
import baseclasses.PipelineStageBase;
import baseclasses.CpuCore;
import tools.InstructionSequence;
import utilitytypes.EnumComparison;
import utilitytypes.EnumOpcode;
import voidtypes.VoidRegister;

/**
 * This is an example of a class that builds a specific CPU simulator out of
 * pipeline stages and pipeline registers.
 * 
 * @author 
 */
public class MyCpuCore extends CpuCore<GlobalData>{
    PipelineRegister FetchToDecode;
    PipelineRegister DecodeToExecute;
    PipelineRegister ExecuteToMemory;
    PipelineRegister MemoryToWriteback;
    int i=1;
    AllMyStages.Fetch       Fetch;
    AllMyStages.Decode      Decode;
    AllMyStages.Execute     Execute;
    AllMyStages.Memory      Memory;
    AllMyStages.Writeback   Writeback;
    
    private void setup() throws Exception {
        // Instantiate pipeline registers
        FetchToDecode     = new PipelineRegister(AllMyLatches.FetchToDecode.class);
        DecodeToExecute   = new PipelineRegister(AllMyLatches.DecodeToExecute.class);
        ExecuteToMemory   = new PipelineRegister(AllMyLatches.ExecuteToMemory.class);
        MemoryToWriteback = new PipelineRegister(AllMyLatches.MemoryToWriteback.class);
        
        // Add the registers to the list or registers that get automatically clocked
        registers.add(FetchToDecode);
        registers.add(DecodeToExecute);
        registers.add(ExecuteToMemory);
        registers.add(MemoryToWriteback);
        
        // Instantiate pipeline stages
        Fetch       = new AllMyStages.Fetch(this, VoidRegister.getVoidRegister(), FetchToDecode);
        Decode      = new AllMyStages.Decode(this, FetchToDecode, DecodeToExecute);
        Execute     = new AllMyStages.Execute(this, DecodeToExecute, ExecuteToMemory);
        Memory      = new AllMyStages.Memory(this, ExecuteToMemory, MemoryToWriteback);
        Writeback   = new AllMyStages.Writeback(this, MemoryToWriteback, VoidRegister.getVoidRegister());

        // Add pipeline stages to list of pipeline stages that are automatically told to compute
        stages.add(Fetch);
        stages.add(Decode);
        stages.add(Execute);
        stages.add(Memory);
        stages.add(Writeback);
        
        globals = new GlobalData();
    }
    
    public MyCpuCore() throws Exception {
        setup();
    }
    
    public void loadProgram(InstructionSequence program) {
        globals.program = program;
    }
    
	public void runProgram() {
    	//AllMyStages.Writeback wb = new AllMyStages.Writeback(this, MemoryToWriteback, VoidRegister.getVoidRegister());
		
//Enum updated with HALT with the writeback		
		//GlobalData globals = (GlobalData)core.getGlobalResources();
    	while(globals.completedExecution==0)
    	{
    		
    		advanceClock();
    		globals.globalCounter++;
    	}
    		
    	
    	/*for(int i=1;i<1000;i++)
		{
    		//System.out.println("Code Cycle" + i);
    	//	i++;
    		advanceClock();
    			
    		 
    	}*/
    	
    	
    }
}
