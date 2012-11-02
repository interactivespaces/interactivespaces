/*
 * Copyright (C) 2012 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.osgi.deployment.master.internal;


/**
 * 
 * 
 * @author Keith M. Hughes
 */
public class Sat4jDependencySolver {

//	private Repository repository;
//	private Resource resource;
//
//	/**
//	 * The subset of the repository that we are interested in
//	 */
//	// private Set<Resource> slice;
//	// private SolutionImpl solution;
//	// private DependencyHelper<Resource, String> helper;
//
//	public Sat4jDependencySolver(Repository repository, Resource resource) {
//		this.repository = repository;
//		this.resource = resource;
//	}
//
//	@Override
//	public Solution call() throws Exception {
//		SolutionImpl solution = new SolutionImpl(repository, resource);
//		solution.setState(SolutionState.Resolving);
//		IPBSolver solver = SolverFactory
//				.newMiniLearningOPBClauseCardConstrMaxSpecificOrderIncrementalReductionToClause();
//		DependencyHelper<Resource, String> helper = new DependencyHelper<Resource, String>(
//				solver, true);
//		Set<Resource> slice = Slicer.slice(repository, resource);
//		/*
//		 * create the question
//		 */
//		try {
//			helper.setTrue(resource, "Build");
//			weighVersions(helper, slice);
//			addRequires(helper, slice);
//		} catch (ContradictionException e) {
//			e.printStackTrace();
//		}
//		/*
//		 * ask the question
//		 */
//		try {
//			if (helper.hasASolution()) {
//				IVec<Resource> solutionSet = helper.getSolution();
//				System.out.println("Size " + solutionSet.size());
//				Iterator<Resource> i = solutionSet.iterator();
//				while (i.hasNext()) {
//					solution.addDependency(i.next());
//				}
//				solution.setState(SolutionState.Satisfied);
//			} else {
//				solution.setState(SolutionState.Unsatisfied);
//			}
//		} catch (Exception e) {
//			solution.setState(SolutionState.Unsatisfied);
//		}
//		return solution;
//	}
//
//	@SuppressWarnings("unchecked")
//	private void weighVersions(DependencyHelper<Resource, String> helper,
//			Set<Resource> slice) {
//		List<Resource> list = new ArrayList<Resource>(slice);
//		Collections.sort(list, new ResourceSorter());
//		List<WeightedObject<Resource>> wos = new ArrayList<WeightedObject<Resource>>();
//		int i = 1;
//		for (Resource r : list) {
//			wos.add(WeightedObject.newWO(r, 10 * i));
//			i++;
//		}
//		helper.setObjectiveFunction(wos.toArray(new WeightedObject[] {}));
//	}
//
//	private void addRequires(DependencyHelper<Resource, String> helper,
//			Set<Resource> slice) throws ContradictionException {
//		Set<Resource> set = new HashSet<Resource>();
//		for (Requirement req : resource.getRequirements()) {
//			set.clear();
//			for (Resource res : slice) {
//				if (Slicer.match(req, res)) {
//					set.add(res);
//				}
//			}
//			if (set.size() > 0) {
//				helper.or(req.getFilter().toString(), resource,
//						set.toArray(new Resource[0]));
//			}
//		}
//	}
}
