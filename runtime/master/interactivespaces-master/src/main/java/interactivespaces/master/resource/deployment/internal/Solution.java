/**
 *
 */
package interactivespaces.master.resource.deployment.internal;

import interactivespaces.resource.NamedVersionedResource;

/**
 * @author Keith M. Hughes
 */
public interface Solution {

  /**
   * Add in a resource which satisfies a dependency for the solution.
   *
   * @param resource
   *          the resource to add
   */
  void addDependency(NamedVersionedResource resource);

  /**
   * Set the state of the solution being created,
   *
   * @param state
   *          the new state
   */
  void setState(SolutionState state);
}
