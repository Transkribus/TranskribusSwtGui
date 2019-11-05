package eu.transkribus.swt.util;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.widgets.Text;

public class MetadataTextFieldValidator<T> {
	
	List<MetadataTextFieldWrapper> textFields;
	
	public MetadataTextFieldValidator() {
		textFields = new LinkedList<>();
	}
	
	/**
	 * Attach the validator to a text input field.
	 * 
	 * @param name the name of the text field as shown in the label
	 * @param textField the text field to validate
	 * @param sizeLowerBound min size of input value. Pass non-positive value to allow empty value.
	 * @param sizeUpperBound max size of input value. Pass non-positive value to not enforce a limit.
	 * @param getter the function for retrieving the original value from the object, e.g. a getter
	 */
	public void attach(String name, Text textField, final int sizeLowerBound, final int sizeUpperBound, Function<T, String> getter) {
		textFields.add(new MetadataTextFieldWrapper(name, textField, sizeLowerBound, sizeUpperBound, getter));
	}
	
	/**
	 * @param object to be edited and for which the original field values will be taken into account
	 */
	public void setOriginalObject(T object) {
		textFields.forEach(o -> o.setObject(object));
	}
	
	/**
	 * @return true if the input in all attached fields is valid
	 */
	public boolean isInputValid() {
		return getValidationErrorMessages().isEmpty();
	}
	
	/**
	 * @return true if any of the text field values is unequal to the original value in object
	 */
	public boolean hasInputChanged() {
		boolean hasChanged = false;
		for(MetadataTextFieldWrapper w : textFields) {
			hasChanged |= w.hasChanged();
		}
		return hasChanged;
	}
	
	/**
	 * @return a descriptive error message for each field where the validation failed
	 */
	public List<String> getValidationErrorMessages() {
		List<String> messages = new LinkedList<>();
		for(MetadataTextFieldWrapper w : textFields) {
			final String msg = w.validate();
			if(!StringUtils.isEmpty(msg)) {
				messages.add(msg);
			}
		}
		return messages;
	}
	
	/**
	 * Validate textField input. The saveBtnListener is en-/disabled when validation (not empty and below size limit) succeeds 
	 * and a change compared to getOrigValue() is detected.
	 */
	class MetadataTextFieldWrapper {
		final int sizeUpperBound, sizeLowerBound;
		final Function<T, String> getter;
		final String name;
		final Text textField;
		T object;
		
		MetadataTextFieldWrapper(String name, Text textField, final int sizeLowerBound, final int sizeUpperBound, Function<T, String> getter) {
			this.name = name;
			this.textField = textField;
			this.sizeUpperBound = sizeUpperBound;
			this.sizeLowerBound = sizeLowerBound;
			this.getter = getter;
			this.object = null;
		}
		
		void setObject(T object) {
			this.object = object;
		}
		
		/**
		 * @return the original value from the object to be modified using getter
		 */
		String getOrigValue() {
			if(object == null) {
				return "";
			} else {
				 return getter.apply(object);
			}
		}
		
		/**
		 * @return the current value entered in the text field
		 */
		String getValue() {
			return textField.getText();
		}
		
		/**
		 * @return true if the values from getOrigValue() and getValue() differ.
		 */
		boolean hasChanged() {
			return !getOrigValue().equals(getValue());
		}
		
		/**
		 * @return true if getValue() returns non-empty value with size below sizeLimit.
		 */
		boolean isValid() {
			return validate() == null;
		}
		
		/**
		 * @return null if getValue() returns non-empty value with size below sizeLimit., error message otherwise
		 */
		String validate() {
			if(sizeLowerBound > 0 && getValue().length() < sizeLowerBound) {
				return name + " is too short. Minimum length: " + sizeLowerBound;
			}
			if(sizeUpperBound > 1 && getValue().length() > sizeUpperBound) {
				return name + " is too long. Maximum length: " + sizeUpperBound;
			}
			return null;
		}
	}
}
