package us.msu.cse.repair.ec.problems;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.JavaFileObject;

import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;

import jmetal.core.Solution;
import jmetal.encodings.variable.ArrayInt;
import jmetal.encodings.variable.Binary;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import us.msu.cse.repair.core.parser.BooleanExpressionDetector;
import us.msu.cse.repair.core.parser.ExtendedModificationPoint;
import us.msu.cse.repair.core.parser.ModificationPoint;
import us.msu.cse.repair.core.parser.NumberLiteralDetector;
import us.msu.cse.repair.core.parser.StaticMFDetector;
import us.msu.cse.repair.core.templates.TemplateExecutor;
import us.msu.cse.repair.core.util.Helper;
import us.msu.cse.repair.core.util.IO;
import us.msu.cse.repair.core.util.Patch;
import us.msu.cse.repair.ec.representation.ArrayIntAndBinarySolutionType2;

public class PareProblem extends ArjaProblem {
	private static final long serialVersionUID = 1L;
	
	protected List<ExtendedModificationPoint> modificationPoints;
	
	public PareProblem(Map<String, Object> parameters) throws Exception {
		super(parameters);

		weight = (Double) parameters.get("weight");
		if (weight == null)
			weight = 0.5;

		mu = (Double) parameters.get("mu");
		if (mu == null)
			mu = 0.06;


		initializationStrategy = (String) parameters.get("initializationStrategy");
		if (initializationStrategy == null)
			initializationStrategy = "Prior";
		
		setProblemParams();
	}
	
	@Override
	protected void setProblemParams() throws JMException {
		numberOfVariables_ = 2;
		numberOfObjectives_ = numberOfObjectives;
		numberOfConstraints_ = 0;
		problemName_ = "PareProblem";

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
		
		modificationPoints = new ArrayList<ExtendedModificationPoint>();
		for (ModificationPoint mp : super.modificationPoints) {
			ExtendedModificationPoint emp = new ExtendedModificationPoint(mp);
			modificationPoints.add(emp);
		}
		
		invokeStaticMFDetector();
		invokeNumberLiteralDetector();
		invokeBooleanExpressionDetector();
		invokeTemplateExecutor();
		
		invokeManipulationInitializer();
		invokeModificationPointsTrimmer();
		
		invokeTestFilter();
		invokeCompilerOptionsInitializer();
		invokeProgURLsInitializer();
		
		invokeRedundantIngredientsRemover();
	}

	
	protected void invokeRedundantIngredientsRemover() {
		for (ExtendedModificationPoint mp : modificationPoints) {
			List<Statement> replaceIngredients = mp.getReplaceIngredients();
			List<Statement> insertIngredients = mp.getInsertIngredients();
			
			replaceIngredients = Helper.getStatementListWithoutDuplicates(replaceIngredients);
			insertIngredients = Helper.getStatementListWithoutDuplicates(insertIngredients);
			mp.setReplaceIngredients(replaceIngredients);
			mp.setInsertIngredients(insertIngredients);
		}
	}
	
	
	
	@Override
	protected void invokeManipulationInitializer() {
		System.out.println("Initialization of manipulations starts...");
		availableManipulations = new ArrayList<List<String>>(modificationPoints.size());

		for (int i = 0; i < modificationPoints.size(); i++) {
			ExtendedModificationPoint mp = modificationPoints.get(i);
			boolean noReplace = mp.getReplaceIngredients().isEmpty();
			boolean noInsert = mp.getInsertIngredients().isEmpty();
			
			List<String> list = new ArrayList<String>();
			for (int j = 0; j < manipulationNames.length; j++) {
				String manipulationName = manipulationNames[j];
				
				if (noReplace && manipulationName.equalsIgnoreCase("Replace"))
					continue;
				
				if (noInsert && manipulationName.equalsIgnoreCase("InsertBefore"))
					continue;
				
				list.add(manipulationName);
				
			}
			availableManipulations.add(list);
		}
		System.out.println("Initialization of manipulations is finished!");
	}
	
	@Override
	protected void invokeModificationPointsTrimmer() {
		int i = 0;
		while (i < modificationPoints.size()) {
			List<String> manips = availableManipulations.get(i);

			if (manips.isEmpty()) {
				modificationPoints.remove(i);
				availableManipulations.remove(i);
			} else
				i++;
		}
	}

	protected void invokeTemplateExecutor() throws MalformedTreeException, BadLocationException {
		System.out.println("Execution of repair templates starts...");
		TemplateExecutor executor = new TemplateExecutor(modificationPoints, sourceContents, declaredClasses,
				methodDeclarations);
		executor.execute();
		System.out.println("Execution of repair templates is finished!");
	}
	
	protected void invokeNumberLiteralDetector() {
		System.out.println("Detection of number literal starts...");
		NumberLiteralDetector numberLiteralDetector = new NumberLiteralDetector(modificationPoints);
		numberLiteralDetector.detect();
		System.out.println("Detection of number literal is finished!");
	}
	
	protected void invokeStaticMFDetector() {
		System.out.println("Detection of methods and variables starts...");
		StaticMFDetector detector = new StaticMFDetector(modificationPoints, declaredClasses);
		detector.detect();
		System.out.println("Detection of methods and variables is finished!");
	}
	
	protected void invokeBooleanExpressionDetector() {
		System.out.println("Detection of boolean expressions starts...");
		BooleanExpressionDetector detector = new BooleanExpressionDetector(modificationPoints, sourceASTs);
		detector.detect();
		System.out.println("Detection of boolean expressions is finished!");
	}
	
	@Override
	public void evaluate(Solution solution) throws JMException {
		// TODO Auto-generated method stub
		System.out.println("One fitness evaluation starts...");
		
		int[] array = ((ArrayInt) solution.getDecisionVariables()[0]).array_;
		BitSet bits = ((Binary) solution.getDecisionVariables()[1]).bits_;

		int size = modificationPoints.size();
		Map<String, ASTRewrite> astRewriters = new HashMap<String, ASTRewrite>();

		Map<Integer, Double> selectedMP = new HashMap<Integer, Double>();

		for (int i = 0; i < size; i++) {
			if (bits.get(i)) {
				double suspValue = modificationPoints.get(i).getSuspValue();
				selectedMP.put(i, suspValue);
			}
		}

		if (selectedMP.isEmpty()) {
			assignMaxObjectiveValues(solution);
			return;
		}

		int numberOfEdits = selectedMP.size();
		List<Map.Entry<Integer, Double>> list = new ArrayList<Map.Entry<Integer, Double>>(selectedMP.entrySet());

		for (int i = 0; i < numberOfEdits; i++)
			manipulateOneModificationPoint(list.get(i).getKey(), size, array, astRewriters);


		Map<String, String> modifiedJavaSources = getModifiedJavaSources(astRewriters);
		Map<String, JavaFileObject> compiledClasses = getCompiledClassesForTestExecution(modifiedJavaSources);

		boolean status = false;
		if (compiledClasses != null) {
			if (numberOfObjectives == 2 || numberOfObjectives == 3)
				solution.setObjective(0, numberOfEdits);
			try {
				status = invokeTestExecutor(compiledClasses, solution);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			assignMaxObjectiveValues(solution);
			System.out.println("Compilation fails!");
		}

		if (status) {
			save(solution, modifiedJavaSources, compiledClasses, list, numberOfEdits);
		}
		
		evaluations++;
		System.out.println("One fitness evaluation is finished...");
	}
	
	
	@Override
	protected void save(Solution solution, Map<String, String> modifiedJavaSources, Map<String, JavaFileObject> compiledClasses,
			List<Map.Entry<Integer, Double>> list, int numberOfEdits) {
		List<Integer> opList = new ArrayList<Integer>();
		List<Integer> locList = new ArrayList<Integer>();
		List<Integer> ingredList = new ArrayList<Integer>();

		int[] var0 = ((ArrayInt) solution.getDecisionVariables()[0]).array_;
		int size = var0.length / 3;

		for (int i = 0; i < numberOfEdits; i++) {
			int loc = list.get(i).getKey();
			int op = var0[loc];
			
			String manipName = availableManipulations.get(loc).get(op);
			
			int ingred;
			if (manipName.equalsIgnoreCase("Replace"))
				ingred = var0[loc + size];
			else
				ingred = var0[loc +  2 * size];
			
			opList.add(op);
			locList.add(loc);
			ingredList.add(ingred);
		}

		try {
			saveTestAdequatePatch(opList, locList, ingredList, modifiedJavaSources);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean addTestAdequatePatch(List<Integer> opList, List<Integer> locList, List<Integer> ingredList) {
		Patch patch = new Patch(opList, locList, ingredList, modificationPoints, availableManipulations, true);
		return patches.add(patch);
	}
	
	@Override
	public void saveTestAdequatePatch(List<Integer> opList, List<Integer> locList, List<Integer> ingredList)
			throws IOException {
		long estimatedTime = System.currentTimeMillis() - launchTime;
		if (patchOutputRoot != null)
			IO.savePatch(opList, locList, ingredList, modificationPoints, availableManipulations, patchOutputRoot,
					globalID, evaluations, estimatedTime, true);
	}

	
	@Override
	protected boolean manipulateOneModificationPoint(int i, int size, int array[], Map<String, ASTRewrite> astRewriters)
			throws JMException {
		ExtendedModificationPoint mp = modificationPoints.get(i);
		String manipName = availableManipulations.get(i).get(array[i]);

		Statement ingredStatement = null;
		if (manipName.equalsIgnoreCase("Replace"))
			ingredStatement = mp.getReplaceIngredients().get(array[i + size]);
		else if (manipName.equalsIgnoreCase("InsertBefore"))
			ingredStatement = mp.getInsertIngredients().get(array[i + 2 * size]);
		
		return manipulateOneModificationPoint(mp, manipName, ingredStatement, astRewriters);
	}
	
	protected List<ModificationPoint> getOrgModificationPoints() {
		return super.modificationPoints;
	}
	
	public List<ExtendedModificationPoint> getExtendedModificationPoints() {
		return this.modificationPoints;
	}
}
