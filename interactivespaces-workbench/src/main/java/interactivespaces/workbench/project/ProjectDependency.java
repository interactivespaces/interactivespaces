/**
 * 
 */
package interactivespaces.workbench.project;

/**
 * A dependency for the project.
 *
 * @author Keith M. Hughes
 */
public class ProjectDependency {

	/**
	 * The name of the dependency.
	 */
	private String name;

	/**
	 * The minimum version necessary for the activity.
	 */
	private String minimumVersion;

	/**
	 * The maximum version necessary for the activity.
	 */
	private String maximumVersion;

	/**
	 * Is the dependency required?
	 * 
	 * <p>
	 * {@code true} if the dependency is required
	 */
	private boolean required;
	
	/**
	 * Get the name of the dependency.
	 * 
	 * @return The name of the dependency.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of the dependency.
	 * 
	 * @param name
	 *            the name of the dependency
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the minimum version necessary for the activity.
	 * 
	 * @return
	 */
	public String getMinimumVersion() {
		return minimumVersion;
	}

	/**
	 * Set the minimum version necessary for the activity.
	 * 
	 * @param minimumVersion
	 * 			the minimum version 
	 */
	public void setMinimumVersion(String minimumVersion) {
		this.minimumVersion = minimumVersion;
	}

	/**
	 * Get the maximum version necessary for the activity.
	 * 
	 * @return
	 */
	public String getMaximumVersion() {
		return maximumVersion;
	}

	/**
	 * Set the maximum version necessary for the activity.
	 * 
	 * @param versionMaximum
	 * 			the maximum version 
	 */
	public void setMaximumVersion(String versionMaximum) {
		this.maximumVersion = maximumVersion;

	}

	/**
	 * Is the dependency required?
	 * 
	 * @return {@code true} if the dependency is required
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * Set if the dependency is required.
	 * 
	 * @param required
	 *            {@code true} if the dependency is required
	 */
	public void setRequired(boolean required) {
		this.required = required;
	}
}
