/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation;

import implementation.AllMyLatches.*;
import utilitytypes.EnumOpcode;
import baseclasses.InstructionBase;
import baseclasses.PipelineRegister;
import baseclasses.PipelineStageBase;
import voidtypes.VoidLatch;
import baseclasses.CpuCore;

/**
 * The AllMyStages class merely collects together all of the pipeline stage 
 * classes into one place.  You are free to split them out into top-level
 * classes.
 * 
 * Each inner class here implements the logic for a pipeline stage.
 * 
 * It is recommended that the compute methods be idempotent.  This means
 * that if compute is called multiple times in a clock cycle, it should
 * compute the same output for the same input.
 * 
 * How might we make updating the program counter idempotent?
 * 
 * @author
 */
public class AllMyStages {
	/*** Fetch Stage ***/
	static class Fetch extends PipelineStageBase<VoidLatch,FetchToDecode> {
		public Fetch(CpuCore core, PipelineRegister input, PipelineRegister output) {
			super(core, input, output);
		}

		@Override
		public String getStatus() {
			// Generate a string that helps you debug.
			return null;
		}

		@Override
		public void compute(VoidLatch input, FetchToDecode output) {
			GlobalData globals = (GlobalData)core.getGlobalResources();
			int pc = globals.program_counter;
			// Fetch the instruction
			InstructionBase ins = globals.program.getInstructionAt(pc);
			if (ins.isNull()) return;

			// Do something idempotent to compute the next program counter.

			// Don't forget branches, which MUST be resolved in the Decode
			// stage.  You will make use of global resources to commmunicate
			// between stages.

			// Your code goes here...
			/*System.out.println("Decode isStalled: " + globals.isStalled);
			if(!globals.isStalled)
				globals.program_counter++;
				*/
			if(globals.branchTaken)
				ins.setOpcode(EnumOpcode.INVALID);
			output.setInstruction(ins);
		}

		@Override
		public boolean stageWaitingOnResource() {
			// Hint:  You will need to implement this for when branches
			// are being resolved.
			return false;
		}


		/**
		 * This function is to advance state to the next clock cycle and
		 * can be applied to any data that must be updated but which is
		 * not stored in a pipeline register.
		 */
		@Override
		public void advanceClock() {
			// Hint:  You will need to implement this help with waiting
			// for branch resolution and updating the program counter.
			// Don't forget to check for stall conditions, such as when
			// nextStageCanAcceptWork() returns false.
			GlobalData globals = (GlobalData)core.getGlobalResources();
			System.out.println("Cycle: " + (++globals.cycle));
            if(!globals.isStalled)
                globals.program_counter++;
		}
	}


	/*** Decode Stage ***/
	static class Decode extends PipelineStageBase<FetchToDecode,DecodeToExecute> {
		public Decode(CpuCore core, PipelineRegister input, PipelineRegister output) {
			super(core, input, output);
		}

		@Override
		public boolean stageWaitingOnResource() {
			// Hint:  You will need to implement this to deal with 
			// dependencies.
			GlobalData globals = (GlobalData)core.getGlobalResources();
            if(globals.isStalled)
                return true;

            return false;
		}


		@Override
		public void compute(FetchToDecode input, DecodeToExecute output) {
			InstructionBase ins = input.getInstruction();

			// You're going to want to do something like this:

			// VVVVV LOOK AT THIS VVVVV
			ins = ins.duplicate();
			// ^^^^^ LOOK AT THIS ^^^^^

			// The above will allow you to do things like look up register 
			// values for operands in the instruction and set them but avoid 
			// altering the input latch if you're in a stall condition.
			// The point is that every time you enter this method, you want
			// the instruction and other contents of the input latch to be
			// in their original state, unaffected by whatever you did 
			// in this method when there was a stall condition.
			// By cloning the instruction, you can alter it however you
			// want, and if this stage is stalled, the duplicate gets thrown
			// away without affecting the original.  This helps with 
			// idempotency.



			// These null instruction checks are mostly just to speed up
			// the simulation.  The Void types were created so that null
			// checks can be almost completely avoided.
			if (ins.isNull()) return;
			
			if(ins.getOpcode() == EnumOpcode.STORE)
			{
				System.out.println("");
			}

			GlobalData globals = (GlobalData)core.getGlobalResources();
			int[] regfile = globals.register_file;
			System.out.println("Value of R1:" + globals.register_file[1]);
			boolean isStalled = false;
			// Do what the decode stage does:
			// - Look up source operands
			// - Decode instruction
			// - Resolve branches
			
		
			//If sources are invalid then stall
			if(ins.getOpcode() == EnumOpcode.ADD || ins.getOpcode() == EnumOpcode.LOAD || ins.getOpcode() == EnumOpcode.STORE || ins.getOpcode() == EnumOpcode.CMP)
			{
				if(ins.getSrc1().getRegisterNumber() != -1 && globals.register_invalid[ins.getSrc1().getRegisterNumber()])
				{
					isStalled = true;
				}
				if(ins.getSrc2().getRegisterNumber() != -1 && globals.register_invalid[ins.getSrc2().getRegisterNumber()])
				{
					isStalled = true;
				}
			}
			if(ins.getOpcode() == EnumOpcode.STORE || ins.getOpcode() == EnumOpcode.BRA)
			{
				if(ins.getOper0().getRegisterNumber() != -1 && globals.register_invalid[ins.getOper0().getRegisterNumber()])
				{
					isStalled = true;
				}
			}
			
			/*switch (ins.getOpcode()) {
			case MOVC:
				
				break;
			case ADD:
				if(ins.getSrc1().getRegisterNumber() != -1 && globals.register_invalid[ins.getSrc1().getRegisterNumber()])
				{
					isStalled = true;
				}
				if(ins.getSrc2().getRegisterNumber() != -1 && globals.register_invalid[ins.getSrc2().getRegisterNumber()])
				{
					isStalled = true;
				}
				break;
			case LOAD:
				if(ins.getSrc1().getRegisterNumber() != -1 && globals.register_invalid[ins.getSrc1().getRegisterNumber()])
				{
					isStalled = true;
				}
				if(ins.getSrc2().getRegisterNumber() != -1 && globals.register_invalid[ins.getSrc2().getRegisterNumber()])
				{
					isStalled = true;
				}
				break;
			case STORE:
				if(ins.getOper0().getRegisterNumber() != -1 && globals.register_invalid[ins.getOper0().getRegisterNumber()])
				{
					isStalled = true;
				}
				if(ins.getSrc2().getRegisterNumber() != -1 && globals.register_invalid[ins.getSrc2().getRegisterNumber()])
				{
					isStalled = true;
				}
				if(ins.getSrc1().getRegisterNumber() != -1 && globals.register_invalid[ins.getSrc1().getRegisterNumber()])
				{
					isStalled = true;
				}
				if(ins.getSrc2().getRegisterNumber() != -1 && globals.register_invalid[ins.getSrc2().getRegisterNumber()])
				{
					isStalled = true;
				}
				break;

			default:
				break;
			}*/
			globals.isStalled = isStalled;
			System.out.println("Decode isStalled: " + isStalled);
			if(ins.getOpcode() != EnumOpcode.STORE && ins.getOpcode() != EnumOpcode.JMP)
				globals.register_invalid[ins.getOper0().getRegisterNumber()] = true;
			
			if(ins.getOpcode() == EnumOpcode.BRA) {

                if ("init_list".equals(ins.getLabelTarget().getName())) {// ins.getLabelTarget().getName().equals("")
                	if(globals.branchTaken)
                		globals.program_counter = ins.getLabelTarget().getAddress();
                	globals.branchTaken = false;
                    /*if(regfile[ins.getOper0().getRegisterNumber()]  == 4) {
                        globals.program_counter = ins.getLabelTarget().getAddress() - 1;
                        //globals.branchInDecode = false;
                    }
                    else{
                        globals.program_counter = 5;
                        //globals.branchInDecode = false;
                    }*/
                }
			}
			
			
			output.setInstruction(ins);
			// Set other data that's passed to the next stage.
		}


	}


	/*** Execute Stage ***/
	static class Execute extends PipelineStageBase<DecodeToExecute,ExecuteToMemory> {
		public Execute(CpuCore core, PipelineRegister input, PipelineRegister output) {
			super(core, input, output);
		}

		@Override
		public void compute(DecodeToExecute input, ExecuteToMemory output) {
			InstructionBase ins = input.getInstruction();
			if (ins.isNull()) return;
			GlobalData globals = (GlobalData)core.getGlobalResources();
			/*int source1 = ins.getSrc1().getValue();
			int source2 = ins.getSrc2().getValue();
			int oper0 =   ins.getOper0().getValue();
			*/
			int source1 = ins.getSrc1().getRegisterNumber() == -1 ? ins.getSrc1().getValue() : globals.register_file[ins.getSrc1().getRegisterNumber()];
            int source2 = ins.getSrc2().getRegisterNumber() == -1 ? ins.getSrc2().getValue() : globals.register_file[ins.getSrc2().getRegisterNumber()];
            int oper0 =   ins.getOper0().getRegisterNumber() == -1 ? ins.getOper0().getValue() : globals.register_file[ins.getOper0().getRegisterNumber()];
			//ins.getOpcode(2)
			System.out.println(ins.getOpcode().toString() + ", source1 value:" + ins.getSrc1().getRegisterNumber() + ":" + source1 + ", Source2 value:" +  ins.getSrc2().getRegisterNumber() + ":" + source2 + ", oper0:" +  ins.getOper0().getRegisterNumber() + ":" + oper0);

            if(ins.getOpcode() == EnumOpcode.STORE || ins.getOpcode() == EnumOpcode.LOAD)
            {
            	System.out.println("");
            	//source1 = ins.getSrc1().getRegisterNumber();
            	//oper0 = ins.getOper0().getRegisterNumber();
            }
			
			int result = MyALU.execute(ins.getOpcode(), source1, source2, oper0);
			
			if(ins.getOpcode()== EnumOpcode.CMP)
			{
				if(result == 0)
					globals.branchTaken = true;
				else
					globals.branchTaken = false;
				//if ("init_list".equals(ins.getLabelTarget().getName()) && result == 0) // ins.getLabelTarget().getName().equals("")
                	//globals.program_counter = ins.getLabelTarget().getAddress()-1;
			}
			
			if(ins.getOpcode() == EnumOpcode.STORE)
				globals.memAddress= result;
			else
				ins.getOper0().setValue(result);
			// Fill output with what passes to Memory stage...
			output.setInstruction(ins);
			// Set other data that's passed to the next stage.
		}
	}


	/*** Memory Stage ***/
	static class Memory extends PipelineStageBase<ExecuteToMemory,MemoryToWriteback> {
		public Memory(CpuCore core, PipelineRegister input, PipelineRegister output) {
			super(core, input, output);
		}

		@Override
		public void compute(ExecuteToMemory input, MemoryToWriteback output) {
			InstructionBase ins = input.getInstruction();
			if (ins.isNull()) return;

			// Access memory...
			GlobalData globals = (GlobalData)core.getGlobalResources();
			int mem[] = globals.memory;
			if(ins.getOpcode() == EnumOpcode.LOAD)
            {
				ins.getOper0().setValue(mem[ins.getOper0().getValue()]);
            }
			if(ins.getOpcode() == EnumOpcode.STORE)
            {
            	mem[globals.memAddress] = ins.getOper0().getValue();
            }

			output.setInstruction(ins);
			// Set other data that's passed to the next stage.
		}
	}


	/*** Writeback Stage ***/
	static class Writeback extends PipelineStageBase<MemoryToWriteback,VoidLatch> {
		public Writeback(CpuCore core, PipelineRegister input, PipelineRegister output) {
			super(core, input, output);
		}

		@Override
		public void compute(MemoryToWriteback input, VoidLatch output) {


			GlobalData globals = (GlobalData)core.getGlobalResources();
			//int[] regfile = globals.register_file;


			InstructionBase ins = input.getInstruction();
			if (ins.isNull()) return;
			// Write back result to register file
			if(ins.getOpcode() == EnumOpcode.MOVC)
			{
				globals.register_file[ins.getOper0().getRegisterNumber()] = ins.getOper0().getValue();
				globals.register_invalid[ins.getOper0().getRegisterNumber()] = false;
				//regfile[ins.getOper0().getRegisterNumber()] = ins.getSrc1().getValue();
			}
			if(ins.getOpcode() == EnumOpcode.ADD || ins.getOpcode() == EnumOpcode.CMP)
			{
				globals.register_file[ins.getOper0().getRegisterNumber()] = ins.getOper0().getValue();
				globals.register_invalid[ins.getOper0().getRegisterNumber()] = false;
				/*int srcreg1 = regfile[ins.getSrc1().getRegisterNumber()];
				if(ins.getSrc2().isRegister())
				{
					int srcreg2 = regfile[ins.getSrc2().getRegisterNumber()];
					regfile[ins.getOper0().getRegisterNumber()] = srcreg1 + srcreg2;
				}
				else
				{
					regfile[ins.getOper0().getRegisterNumber()] = srcreg1 + ins.getSrc2().getValue();
				}*/
			}
			if(ins.getOpcode() == EnumOpcode.LOAD)
			{
				globals.register_file[ins.getOper0().getRegisterNumber()] = ins.getOper0().getValue();
				globals.register_invalid[ins.getOper0().getRegisterNumber()] = false;
				//regfile[ins.getOper0().getRegisterNumber()] = ins.getSrc1().getValue();
			}
			

				if (input.getInstruction().getOpcode() == EnumOpcode.HALT) {
					// Stop the simulation
				}
			}
		}
	}