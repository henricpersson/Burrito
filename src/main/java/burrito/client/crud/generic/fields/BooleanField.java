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

package burrito.client.crud.generic.fields;

import burrito.client.crud.generic.CrudField;


public class BooleanField extends CrudField {

	private static final long serialVersionUID = 1L;
	private Boolean bool;

	public BooleanField() {
		// default constructor
	}

	public BooleanField(Boolean bool) {
		super();
		this.bool = bool;
	}

	@Override
	public Class<?> getType() {
		return Boolean.class;
	}

	@Override
	public Object getValue() {
		return bool;
	}

	@Override
	public void setValue(Object value) {
		this.bool = (Boolean) value;
	}
}
