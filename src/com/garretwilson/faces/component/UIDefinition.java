package com.garretwilson.faces.component;

import java.io.File;
import java.net.URI;

import javax.faces.component.UIColumn;
import javax.faces.component.UICommand;
import javax.faces.component.UIData;
import javax.faces.component.UIOutput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import com.garretwilson.lang.ClassUtilities;
import com.globalmentor.marmot.burrow.Burrow;
import com.globalmentor.marmot.burrow.file.FileBurrow;
import com.globalmentor.marmot.resource.ResourceKitManager;
import com.globalmentor.webapps.globalmentor.groups.MentorGroups;

/**A component representing a definition in a list of definitions.
@author Garret Wilson
@see UIDefinitionList
@see UITerm
*/
public class UIDefinition extends UIColumn
{
	public static final String COMPONENT_TYPE=ClassUtilities.getFullName(UIDefinition.class, "Definition");

	public UIDefinition()
	{
		super();
	}

}
