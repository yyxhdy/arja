package us.msu.cse.repair.ec.operators.mutation;

import java.util.HashMap;

import jmetal.operators.mutation.Mutation;
import jmetal.operators.mutation.MutationFactory;
import jmetal.util.JMException;

public class ExtendedMutationFactory {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Mutation getMutationOperator(String name, HashMap parameters) throws JMException {

		if (name.equalsIgnoreCase("BitFilpUniformMutation"))
			return new BitFilpUniformMutation(parameters);
		else if (name.equalsIgnoreCase("GenProgMutation"))
			return new GenProgMutation(parameters);
		else
			return MutationFactory.getMutationOperator(name, parameters);

	} // getMutationOperator
}
