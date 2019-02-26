/*
 * Copyright © 1996-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globalmentor.faces.component.renderkit.xhtml;

import javax.faces.component.*;

import com.globalmentor.faces.component.*;

import static com.globalmentor.w3c.spec.HTML.*;

/**
 * Renders a <code>UIInput</code> as an XHTML input element of type <code>file</code>.
 * @author Garret Wilson
 */
public class InputFileRenderer extends AbstractInputRenderer {

	/** The type of renderer. */
	public static final String RENDERER_TYPE = UIInputFile.COMPONENT_TYPE;

	/** Default constructor. */
	public InputFileRenderer() {
		super(INPUT_TYPE_FILE); //create an input file element
	}

	/**
	 * Determines the current value to be rendered. This implementation always returns <code>null</code>, because the sumitted and/or current value should never
	 * be rendered.
	 * @return <code>null</code> indicating the current value should not be displayed.
	 */
	protected Object getRenderValue(final UIInput input) {
		return null;
	}
}
