/**
 * 
 */
package interactivespaces.domain.support;

/**
 *
 *
 * @author Keith M. Hughes
 */
public class DomainValidationResult {
	
	/**
	 * The result type of the validation.
	 */
	private DomainValidationResultType resultType;
	
	/**
	 * Description of the result.
	 */
	private String description;
	
	
	public DomainValidationResult(DomainValidationResultType resultType,
			String description) {
		this.resultType = resultType;
		this.description = description;
	}

	/**
	 * get the type of the result.
	 * 
	 * @return the type of the result
	 */
	public DomainValidationResultType getResultType() {
		return resultType;
	}

	/**
	 * Get the description of the validation result.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	public enum DomainValidationResultType {

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
}
