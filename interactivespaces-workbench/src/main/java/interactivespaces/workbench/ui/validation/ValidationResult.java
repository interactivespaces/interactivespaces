/**
 * 
 */
package interactivespaces.workbench.ui.validation;

/**
 * The result of a validation.
 * 
 * @author Keith M. Hughes
 */
public enum ValidationResult {

	/**
	 * Everything validated properly.
	 */
	OK,

	/**
	 * There were warnings, but no errors.
	 */
	WARNINGS,

	/**
	 * There are errors.
	 */
	ERRORS
}
