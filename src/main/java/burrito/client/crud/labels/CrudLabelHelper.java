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

package burrito.client.crud.labels;

import java.util.MissingResourceException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Dictionary;

public class CrudLabelHelper {

	private static Dictionary labels = Dictionary.getDictionary("BurritoMessages");

	/**
	 * This method will get a string from CrudLabels and if none is found, a
	 * {@link MissingResourceException} is thrown
	 * 
	 * @param methodName
	 * @return
	 */
	public static String getRequiredString(String methodName,
			String... fallbackMethodName) throws MissingResourceException {
		MissingResourceException mre = null;
		try {
			return labels.get(methodName);
		} catch (MissingResourceException e) {
			// expected, try fallbacks
			mre = e;
		}
		for (String m : fallbackMethodName) {
			try {
				return labels.get(m);
			} catch (MissingResourceException e) {
				// expected, try next fallback
				mre = e;
			}
		}
		throw mre;
	}

	/**
	 * This method is the failsafe equivalent of getRequiredString. If neither
	 * methodName nor any of the fallbacks are found, the methodName is
	 * returned, surrounded by {}, e.g. " {startDate}". An info log is logged,
	 * informing about how to correct the missing resource.
	 * 
	 * @param methodName
	 * @param fallbackMethodNames
	 */
	public static String getString(String methodName, String... fallbackMethodNames) {
		try {
			return getRequiredString(methodName, fallbackMethodNames);
		} catch (MissingResourceException e) {
			// warn about missing resource and return field name as fallback
			// default.
			StringBuilder sbMethods = new StringBuilder();
			for (String m : fallbackMethodNames) {
				sbMethods.append("\n...or...\nString ");
				sbMethods.append(m + "();");
			}
			StringBuilder sbFields = new StringBuilder();
			for (String m : fallbackMethodNames) {
				sbFields.append("\n...or...\n");
				sbFields.append(m + "=...");
			}

			GWT
					.log(
							"Warning: label is missing for field \""
									+ methodName
									+ "\". You should add the following lines of code to CrudLabels.java and CrudLabels.properties:"
									+ "\nString " + methodName + "();"
									+ sbMethods.toString()
									+ "\n" + methodName + "=..."
									+ sbFields.toString(), null);
			return "{" + methodName + "}";
		}
	}

	/**
	 * This method is similar to getString() but instead of returning a default
	 * value, <code>null</code> is returned and nothing is logged.
	 * 
	 * @param fullFieldName
	 * @param fallback
	 * @return
	 */
	public static String getNullableString(String methodName, String... fallbackMethodNames) {
		try {
			return getRequiredString(methodName, fallbackMethodNames);
		} catch (MissingResourceException e) {
			return null;
		}
	}

	/**
	 * 
	 * 
	 * @param relatedEntityName "se.company.Entity"
	 * @return
	 */
	public static String getStringEntityNameSingular(String relatedEntityName) {
		relatedEntityName = relatedEntityName.replace('.', '_') + "_singular";
		return getString(relatedEntityName);
	}
	

}
