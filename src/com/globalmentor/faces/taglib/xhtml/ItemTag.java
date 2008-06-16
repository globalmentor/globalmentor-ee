package com.globalmentor.faces.taglib.xhtml;

import javax.faces.webapp.UIComponentTag;

import com.globalmentor.faces.component.UIItem;

//TODO comment
public class ItemTag extends AbstractXHTMLTag
{

    public String getRendererType() {
        return null;
    }

	/**@return The type of the component to use for this tag.*/
	public String getComponentType() {return UIItem.COMPONENT_TYPE;}

}

