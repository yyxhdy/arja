package us.msu.cse.repair.ec.problems;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import jmetal.util.Configuration;
import jmetal.util.JMException;
import us.msu.cse.repair.core.filterrules.MIFilterRule;
import us.msu.cse.repair.core.filterrules.ManipulationFilterRule;
import us.msu.cse.repair.core.parser.ExtendedModificationPoint;
import us.msu.cse.repair.core.parser.ModificationPoint;
import us.msu.cse.repair.core.parser.context.ContextMetric;
import us.msu.cse.repair.ec.representation.ArrayIntAndBinarySolutionType2;


public class ArjaEProblem extends PareProblem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ArjaEProblem(Map<String, Object> parameters) throws Exception {
		super(parameters);
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	protected void setProblemParams() throws JMException {
		numberOfVariables_ = 2;
		numberOfObjectives_ = numberOfObjectives;
		numberOfConstraints_ = 0;
		
		problemName_ = "ArjaEProblem";

		int size = modificationPoints.size();

		double[] prob = new double[size];
		if (initializationStrategy.equalsIgnoreCase("Prior")) {
			for (int i = 0; i < size; i++)
				prob[i] = modificationPoints.get(i).getSuspValue() * mu;
		} else if (initializationStrategy.equalsIgnoreCase("Random")) {
			for (int i = 0; i < size; i++)
				prob[i] = 0.5;
		} else {
			Configuration.logger_.severe("Initialization strategy " + initializationStrategy + " not found");
			throw new JMException("Exception in initialization strategy: " + initializationStrategy);
		}

		solutionType_ = new ArrayIntAndBinarySolutionType2(this, size, prob);

		upperLimit_ = new double[3 * size];
		lowerLimit_ = new double[3 * size];
		for (int i = 0; i < size; i++) {
			lowerLimit_[i] = 0;
			upperLimit_[i] = availableManipulations.get(i).size() - 1;
		}

		for (int i = size; i < 2 * size; i++) {
			lowerLimit_[i] = 0;
			upperLimit_[i] = modificationPoints.get(i - size).getReplaceIngredients().size() - 1;
		}
		
		for (int i = 2 * size; i < 3 * size; i++) {
			lowerLimit_[i] = 0;
			upperLimit_[i] = modificationPoints.get(i - 2 * size).getInsertIngredients().size() - 1;
		}
	}
	
	@Override
	protected void invokeModules() throws Exception {
		invokeClassFinder();
		invokeFaultLocalizer();
		invokeASTRequestor();
		
		invokeLocalVarDetector();
		invokeFieldVarDetector();
		invokeMethodDetector();
		invokeIngredientScreener();
		
		modificationPoints = new ArrayList<ExtendedModificationPoint>();

		for (ModificationPoint mp : super.getOrgModificationPoints()) {
			ExtendedModificationPoint emp = new ExtendedModificationPoint(mp);
			boolean isVarDecl = emp.getStatement() instanceof VariableDeclarationStatement;
	
			for (Statement statement : mp.getIngredients()) {
				int notReplace = MIFilterRule.canFiltered("Replace", statement,emp);
				int notInsert = MIFilterRule.canFiltered("InsertBefore", statement,emp);
			
				ContextMetric cs = new ContextMetric(statement, mp, methodDeclarations);
				double rs = cs.getReplacementSimilarity();
				double is = cs.getInsertionRelevance();

				if (notReplace == -1 && (rs > repSim || isVarDecl)) {
					Statement rep = (Statement) ASTNode.copySubtree(mp.getStatement().getAST(), statement);
					emp.addReplaceIngredient(rep);
				}
				
				if (notInsert == -1 && is > insRel) {
					Statement ins = (Statement) ASTNode.copySubtree(mp.getStatement().getAST(), statement);
					emp.addInsertIngredient(ins);
				}
				
			}
			
			modificationPoints.add(emp);
		}

	    invokeStaticMFDetector();
		invokeNumberLiteralDetector();
		invokeBooleanExpressionDetector();
		invokeTemplateExecutor();
	  
		
		invokeManipulationInitializer();
		invokeModificationPointsTrimmer();
		
		invokeTestFilter();
		
		invokeTestInstrumenter();
		
		invokeCompilerOptionsInitializer();
		invokeProgURLsInitializer();
		
		invokeRedundantIngredientsRemover();		
	}
	
	
	
	@Override
	protected void invokeManipulationInitializer() {
		System.out.println("Initialization of manipulations starts...");
		availableManipulations = new ArrayList<List<String>>(modificationPoints.size());

		for (int i = 0; i < modificationPoints.size(); i++) {
			ExtendedModificationPoint mp = modificationPoints.get(i);
			boolean noReplace = mp.getReplaceIngredients().isEmpty();
			boolean noInsert = mp.getInsertIngredients().isEmpty();
			boolean noDelete = ManipulationFilterRule.canFiltered("Delete", mp);
			
			List<String> list = new ArrayList<String>();
			for (int j = 0; j < manipulationNames.length; j++) {
				String manipulationName = manipulationNames[j];
				
				if (noReplace && manipulationName.equalsIgnoreCase("Replace"))
					continue;
				
				if (noInsert && manipulationName.equalsIgnoreCase("InsertBefore"))
					continue;
				
				if (noDelete && manipulationName.equalsIgnoreCase("Delete"))
					continue;
				
				list.add(manipulationName);
				
			}
			availableManipulations.add(list);
		}
		System.out.println("Initialization of manipulations is finished!");
	}
	
	
	public List<ExtendedModificationPoint> getExtendedModificationPoints() {
		return this.modificationPoints;
	}
	
}
