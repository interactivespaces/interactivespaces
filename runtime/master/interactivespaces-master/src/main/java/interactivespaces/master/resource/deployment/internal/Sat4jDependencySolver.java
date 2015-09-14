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

package interactivespaces.master.resource.deployment.internal;

import org.apache.felix.bundlerepository.Repository;
//import org.sat4j.pb.IPBSolver;
//import org.sat4j.pb.SolverFactory;
//import org.sat4j.pb.tools.DependencyHelper;
//import org.sat4j.pb.tools.WeightedObject;
//import org.sat4j.specs.ContradictionException;
//import org.sat4j.specs.IVec;


/**
 *
 *
 * @author Keith M. Hughes
 */
public class Sat4jDependencySolver {

  private final Repository repository;

  /**
   * The subset of the repository that we are interested in
   */
  // private Set<Resource> slice;
  // private SolutionImpl solution;
  // private DependencyHelper<Resource, String> helper;

  public Sat4jDependencySolver(Repository repository) {
    this.repository = repository;
  }

  /**
   * Find a solution to the dependency problem.
   *
   * @return
   */
//  public Solution call(Collection<ResourceDependency> dependencies) {
//    SolutionImpl solution = new SolutionImpl(repository, dependencies);
//    solution.setState(SolutionState.Resolving);
//    IPBSolver solver = SolverFactory.newMiniLearningOPBClauseCardConstrMaxSpecificOrderIncrementalReductionToClause();
//    DependencyHelper<NamedVersionedResource, String> helper = new DependencyHelper<NamedVersionedResource, String>(solver, true);
//    Set<Resource> slice = null ; //Slicer.slice(repository, resource);
//    /*
//     * create the question
//     */
//    try {
//      //helper.setTrue(resource, "Build");
//      weighVersions(helper, slice);
//      addRequires(helper, slice);
//    } catch (ContradictionException e) {
//      e.printStackTrace();
//    }
//    /*
//     * ask the question
//     */
//    try {
//      if (helper.hasASolution()) {
//        IVec<NamedVersionedResource> solutionSet = helper.getSolution();
//        System.out.println("Size " + solutionSet.size());
//        Iterator<NamedVersionedResource> i = solutionSet.iterator();
//        while (i.hasNext()) {
//          solution.addDependency(i.next());
//        }
//        solution.setState(SolutionState.Satisfied);
//      } else {
//        solution.setState(SolutionState.UnSatisfied);
//      }
//    } catch (Exception e) {
//      solution.setState(SolutionState.UnSatisfied);
//    }
//    return solution;
//  }
//
//  @SuppressWarnings("unchecked")
//  private void weighVersions(DependencyHelper<Resource, String> helper, Set<Resource> slice) {
//    List<Resource> list = Lists.newArrayList(slice);
//    //Collections.sort(list, new ResourceSorter());
//    List<WeightedObject<Resource>> wos = Lists.newArrayList();
//    int i = 1;
//    for (Resource r : list) {
//      wos.add(WeightedObject.newWO(r, 10 * i));
//      i++;
//    }
//    helper.setObjectiveFunction(wos.toArray(new WeightedObject[] {}));
//  }
//
//  private void addRequires(DependencyHelper<NamedVersionedResource, String> helper, Set<Resource> slice)
//      throws ContradictionException {
//    Set<Resource> set = Sets.newHashSet();
//    for (Requirement req : resource.getRequirements()) {
//      set.clear();
//      for (Resource res : slice) {
////        if (Slicer.match(req, res)) {
////          set.add(res);
////        }
//      }
//      if (!set.isEmpty()) {
//        helper.or(req.getFilter().toString(), resource, set.toArray(new Resource[0]));
//      }
//    }
//  }
}
