package us.msu.cse.repair.core.faultlocalizer;

import java.util.Map;
import java.util.Set;

import us.msu.cse.repair.core.parser.LCNode;

public interface IFaultLocalizer {
	public Map<LCNode, Double> searchSuspicious(double thr);

	public Set<String> getPositiveTests();

	public Set<String> getNegativeTests();
}
