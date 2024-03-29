/*
 * Copyright © 1996-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globalmentor.faces.el;

import javax.faces.context.FacesContext;
import javax.faces.el.*;

/**
 * A <code>MethodBinding</code> subclass that allows a general <code>Expression</code> to be used wherever a <code>MethodBinding</code> instance is called for.
 * @author Garret Wilson
 * @see Expression
 */
public class ExpressionMethodBinding extends MethodBinding {

	/** The represented expression. */
	private final Expression<?> expression;

	/** @return The represented expression. */
	protected Expression<?> getExpression() {
		return expression;
	}

	/**
	 * Expression constructor.
	 * @param expression The represented expression.
	 * @throws NullPointerException Thrown if the expression is <code>null</code>.
	 */
	public ExpressionMethodBinding(final Expression<?> expression) {
		this.expression = expression; //save the expression
		if(expression == null) { //if there is no expression
			throw new NullPointerException("No expression provided.");
		}
	}

	/**
	 * Simulates a call to a method by retrieving the value of the expression.
	 * @param context The JSF context for the current request.
	 * @param params The array of parameters to be passed to the called method, or <code>null</code> for no parameters.
	 * @throws EvaluationException if an exception is thrown by the called method (the thrown exception must be included as the <code>cause</code> property of
	 *           this exception).
	 * @throws MethodNotFoundException if no suitable method can be found.
	 * @throws NullPointerException if <code>context</code> is <code>null</code>
	 * @see Expression#getValue(FacesContext)
	 */
	public Object invoke(final FacesContext context, final Object params[]) throws EvaluationException, MethodNotFoundException {
		return getExpression().getValue(context); //return the expression value
	}

	/**
	 * Return the Java class representing the return type from the method identified by this method binding expression.
	 * @param context The JSF context for the current request.
	 * @throws MethodNotFoundException if no suitable method can be found
	 * @throws NullPointerException if <code>context</code> is <code>null</code>.
	 * @see Expression#getType(FacesContext)
	 */
	public Class getType(final FacesContext context) throws MethodNotFoundException {
		return getExpression().getType(context); //return the expression's type
	}

	/**
	 * Return the (possibly <code>null</code>) expression string, with leading and trailing delimiters, from which this <code>MethodBinding</code> was built.
	 * @see Expression#getExpressionString()
	 */
	public String getExpressionString() {
		return getExpression().getExpressionString(); //return the expression's expresion string
	}
}
