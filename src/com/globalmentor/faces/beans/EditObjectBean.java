package com.globalmentor.faces.beans;

/**A generic JSF backing bean that allows editing of object information.
@author Garret Wilson
*/
public abstract class EditObjectBean<T> extends EditBean
{
	/**Constructor.
	@param navigationOutcome The outcome for navigating to the edit page.
	*/
	public EditObjectBean(final String navigationOutcome)
	{
		super(navigationOutcome);	//construct the base class
	}	
		
	/**@return A new object from the bean property settings.*/
	protected abstract T create();

	/**Accepts the information entered.
	@return <code>true</code> if successful.
	@see #accept(T)
	*/
	public boolean accept()
	{
		return accept(create());	//create a user object from the provided information
	}

	/**Accepts the information from the given object.
	@param object The object to accept.
	The method can use <code>isNew()</code> to determine whether the object is new or
		an existing object was being edited.
	@return <code>true</code> if successful.
	*/
	public abstract boolean accept(final T object);

}
