/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ParallelFunctionalUnits;

import baseclasses.FunctionalUnitBase;
import baseclasses.InstructionBase;
import baseclasses.Latch;
import baseclasses.PipelineStageBase;
import tools.MultiStageDelayUnit;
import utilitytypes.EnumOpcode;
import utilitytypes.IFunctionalUnit;
import utilitytypes.IModule;

/**
 *
 * @author millerti
 */
public class FloatAddSub extends FunctionalUnitBase {
	int numberofPipeline;
	public FloatAddSub(IModule parent, String name, int numberofPipeline) {
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
			float result=0;
			float source1;
			float source2;
//			System.out.println("greaffffffffffffffffffffffffffffffffffter");
			if(ins.getSrc1().isFloat())
			{
				source1 = ins.getSrc1().getFloatValue();
			}
			else
			{
				source1 = ins.getSrc1().getValue();
			}
			if(ins.getSrc2().isFloat())
			{
				source2 = ins.getSrc2().getFloatValue();
			}
			else
			{
				source2 = ins.getSrc2().getValue();
			}
			
			if(ins.getOpcode()==EnumOpcode.FADD)
			{
				result = source1 + source2;
				System.out.println("greafffffffffffffffffffffff");
				
			}
			else if(ins.getOpcode()==EnumOpcode.FSUB)
			{
				result = source1 - source2;
			}
			else
			{
				if(source1 == source2) {
					result=0;
					System.out.println("equal");
				}
					
				else if(source1>source2) {
					result=1;
					System.out.println("greater");
				}
				else if(source1<source2) {
					result=-1;
					System.out.println("less");
				}
			}

			output.setResultFloatValue(result);
			output.setInstruction(ins);
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
