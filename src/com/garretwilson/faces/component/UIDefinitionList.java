package com.garretwilson.faces.component;

import java.io.File;
import java.net.URI;

import javax.faces.component.UICommand;
import javax.faces.component.UIData;
import javax.faces.component.UIOutput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import com.garretwilson.faces.component.renderkit.xhtml.DLRenderer;
import com.garretwilson.lang.ClassUtilities;
import com.globalmentor.marmot.burrow.Burrow;
import com.globalmentor.marmot.burrow.file.FileBurrow;
import com.globalmentor.marmot.faces.UIBurrowTable;
import com.globalmentor.marmot.resource.ResourceKitManager;
import com.globalmentor.webapps.globalmentor.groups.MentorGroups;

/**A component containing a list of definitions.
@author Garret Wilson
*/
public class UIDefinitionList extends UIData
{
	public static final String COMPONENT_TYPE=ClassUtilities.getFullName(UIDefinitionList.class, "DefinitionList");

	public UIDefinitionList()
	{
		super();
		setRendererType(DLRenderer.RENDERER_TYPE);	//default to an XHTML &lt;dl&/gt; renderer
		setVar("var");	//G***testing
	}

}
