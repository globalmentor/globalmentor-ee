package com.garretwilson.faces;

import javax.faces.component.*;

/**Utilities for working with JavaServer Faces components.
@author Garret Wilson
*/
public class ComponentUtilities
{

	/**Searches up the component hierarchy and returns the first found instance
	 	of a parent component of the given class.
	@param component The component the parent of which is to be found.
	@param parentClass The class of parent to find (e.g. <code>UIForm.class</code>.
	@return The first parent of the given component that is an instance of the
		given class, or <code>null</code> if no such parent could be found.
	*/
	public static UIComponent getParent(UIComponent component, final Class parentClass)
	{
		do
		{
			component=component.getParent();	//look at the component's parent
			if(parentClass.isInstance(component))	//if the component is an instance of the requested parent class
			{
				break;	//stop looking for parents
			}	
		}
		while(component!=null);	//keep looking until we run out of parents
		return component;	//return the component we found, or null
	}
}
