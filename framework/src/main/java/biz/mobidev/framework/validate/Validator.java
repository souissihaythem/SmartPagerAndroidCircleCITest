package biz.mobidev.framework.validate;

public class Validator {

	public static boolean notNull(Object object) {
		boolean result = false;
		if (object != null) {
			result = true;
		}
		return result;
	}

	// TODO USe TextUtils.isEmpty()
	public static boolean notEmtyOrNull(String value) {
		boolean result = false;
		if (notNull(value)) {
			if (!value.trim().equalsIgnoreCase("")) {
				result = true;
			}
		}
		return result;
	}

	/**
	 * This function use to validate email
	 * 
	 * @param emailAdress
	 *            email for validate
	 * @return result of validation
	 */
	public static boolean validateEmailAdress(String emailAdress) {
		boolean result = false;
		if (emailAdress.matches("^[a-zA-z0-9]([a-zA-Z0-9\\.\\-_]*)@([a-zA-Z]+\\.){1,2}[a-z]{2,4}$")) {
			result = true;
		}
		return result;
	}

	/**
	 * Check confirmPassword
	 * 
	 * @param password
	 * @param confirmPassword
	 * @return result of validation
	 */
	public static boolean validateConfirmPassword(String password, String confirmPassword) {
		return password.equals(confirmPassword);
	}

	/**
	 * Validate password
	 * 
	 * @param password
	 * @return result of validation
	 */
	public static boolean validatePassword(String password) {
		return password.trim().matches("^[a-zA-z0-9]([a-zA-Z0-9_]*)");
	}

}
