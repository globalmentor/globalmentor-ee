package com.garretwilson.faces.taglib.xhtml;

import javax.faces.webapp.UIComponentTag;
import com.garretwilson.faces.component.UIItem;

//TODO comment
public class ItemTag extends XHTMLTag
{

    public String getRendererType() {
        return null;
    }

	/**@return The type of the component to use for this tag.*/
	public String getComponentType() {return UIItem.COMPONENT_TYPE;}

}

