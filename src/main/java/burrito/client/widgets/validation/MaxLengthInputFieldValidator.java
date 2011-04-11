/**
 * Copyright 2011 Henric Persson (henric.persson@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package burrito.client.widgets.validation;

import burrito.client.widgets.validation.InputFieldValidator;
import burrito.client.widgets.validation.ValidationException;

import com.google.gwt.core.client.GWT;
import burrito.client.widgets.messages.CommonMessages;

public class MaxLengthInputFieldValidator implements InputFieldValidator {

	private CommonMessages messages = GWT.create(CommonMessages.class);

	private int maxChars;
	
	public MaxLengthInputFieldValidator(int maxChars) {
		this.maxChars = maxChars;
	}
	
	@Override
	public void validate(String value) throws ValidationException {
		if(value != null && value.length() > maxChars) {
			throw new ValidationException(messages.validationTooLong(maxChars));
		}
	}

}
