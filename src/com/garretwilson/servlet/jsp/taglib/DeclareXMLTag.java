package com.garretwilson.servlet.jsp.taglib;

import java.io.IOException;

import javax.mail.internet.ContentType;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import com.garretwilson.io.ContentTypeUtilities;
import com.garretwilson.text.xml.XMLUtilities;

import static com.garretwilson.text.xml.XMLUtilities.*;

/**A tag for declaring a JSP to be XML.
<p>For XHTML media types, this tag sets the content type to
	"application/xhtml+xml" if the user agent supports it; otherwise, the
	content type "text/html" is used.</p>
<p>This tag also generates an XML declaration and document type.</p>
<dl>
	<dt>publicID</dt>
	<dd>The doctype public ID. Required if a document type declaration is desired.</dd>

	<dt>systemID</dt>
	<dd>The doctype system ID. If not present, defaults to the default system
		ID for the given public ID. Required if the public ID is not recognized by
		this implementation.</dd>

	<dt>contentType</dt>
	<dd>The document content type. Defaults to the appropriate
		content type for the given document type, if recognized; otherwise, defaults
		to "text/xml".</dd>

	<dt>rootElement</dt>
	<dd>The name of the root element of the document. Defaults to the appropriate
		root element for the content type, if recognized; otherwise, required.</dd>
</dl>
@author Garret Wilson
*/
public class DeclareXMLTag extends TagSupport
{

	/**The document content type.*/
	private String contentType;

		/**@return The document content type.
		Defaults to the appropriate content type if a recognized document type is
			requested; otherwise "text/xml".
		@see #getMediaType()
		*/
		public String getContentType()
		{
			return getMediaType().toString();	//return the string version of the determined media type
		}

		/**@return The document content type object.
		Defaults to the appropriate content type if a recognized document type is
			requested; otherwise "text/xml".
		@exception IllegalArgumentException Thrown if the string is not a
			syntactically correct content type.
		*/
		protected ContentType getMediaType() throws IllegalArgumentException
		{
			ContentType mediaType=null;	//we'll determine the media type if we can
			if(contentType!=null)	//if there is a content type string
			{
				mediaType=ContentTypeUtilities.createContentType(contentType);	//create a new media type
			}
			else	//if there is no content type string
			{
				final String publicID=getPublicID();	//get the public ID
				if(publicID!=null)	//if there is a document type public ID
				{
					mediaType=XMLUtilities.getContentType(publicID);	//get the content type for this doctype public ID
				}
				if(mediaType==null)	//if we still couldn't find a content type
				{
					mediaType=XML_CONTENT_TYPE;	//use the generic "text/xml" content type
				}
			}
			return mediaType;	//return the media type we found
		}

		/**Sets the content type.
		@param contentType The content type.
		*/
		public void setContentType(final String contentType) {this.contentType=contentType;}

	/**The name of the root element of the document.*/
	private String rootElement=null;

		/**@return The name of the root element of the document.
		Defaults to the appropriate root element name if a recognized content type
			can be determined that has a default root element; otherwise <code>null</code>.
		*/
		public String getRootElement()
		{
			String element=rootElement;	//get the current root element
			if(element==null)	//if we don't have a root element
			{
				final ContentType mediaType=getMediaType();	//determine the media type
				element=getDefaultRootElementLocalName(mediaType);	//determine the root element name based upon the media type
			}
			return element;	//return our determined root element
		}

		/**Sets the name of the root element of the document.
		@param rootElement The name of the root element of the document
		*/
		public void setRootElement(final String rootElement) {this.rootElement=rootElement;}

	/**The doctype public ID.*/
	private String publicID;

		/**@return The doctype public ID.*/
		public String getPublicID() {return publicID;}

		/**Sets the doctype public ID.
		@param publicID The doctype public ID.
		*/
		public void setPublicID(final String publicID) {this.publicID=publicID;}

	/**The doctype system ID.*/
	private String systemID;

		/**@return The doctype system ID.
		Defaults to the appropriate system ID if a recognized document type is
			requested; otherwise <code>null</code>.
		*/
		public String getSystemID()
		{
			String id=systemID;	//get the current system ID
			if(id==null)	//if we don't have a system ID
			{
				final String publicID=getPublicID();	//get the public ID
				if(publicID!=null)	//if we have a public ID
				{
					id=getDefaultSystemID(publicID);	//try to determine the system ID from the public ID
				}
			}
			return id;	//return our determined system ID, if there is one
		}

		/**Sets the doctype system ID.
		@param systemID The doctype public ID, or <code>null</code> if the system
			ID should be determined automatically from the public ID.
		*/
		public void setSystemID(final String systemID) {this.systemID=systemID;}

	/**Sets the correct XHTML content type and outputs the correct declarations. 
	@throws JspException if a JSP error occurs.
	*/
	public int doStartTag() throws JspException
	{
		final JspWriter writer=pageContext.getOut();	//get the writer
		try
		{
				//set the content type
			final ContentType mediaType=getMediaType();	//get the media type
/*G***fix
			if(mediaType.get)
			
<jsp:directive.page import="java.util.Enumeration"/>
<jsp:scriptlet>
  String contentType="text/html";
  final Enumeration httpAcceptEnumeration=request.getHeaders("accept");
  while(httpAcceptEnumeration.hasMoreElements())
  {

if(((String)httpAcceptEnumeration.nextElement()).indexOf("application/xhtml+xml")>=0)
    {
      contentType="application/xhtml+xml";
      break;

    }
  }
 
  response.setContentType(contentType);    //set the content type to whatever we decided upon 			
*/
			//TODO set the content type
				//write the XML declaration
			writer.write(XML_DECL_START);	//<?xml
			writer.write(SPACE_CHAR);
			writer.write(VERSIONINFO_NAME);	//version
			writer.write(EQUAL_CHAR);	//=
			writer.write(DOUBLE_QUOTE_CHAR);	//"
			writer.write(XML_VERSION);	//1.0
			writer.write(DOUBLE_QUOTE_CHAR);	//"
			writer.write(SPACE_CHAR);	//TODO make the last space conditional
	//TODO implement the encoding decoration			ENCODINGDECL_NAME+EQUAL_CHAR+DOUBLE_QUOTE_CHAR+encoding+DOUBLE_QUOTE_CHAR+
				//G***fix standalone writing here
			writer.write(XML_DECL_END);	//?>
			writer.write('\n');
				//write the document type declaration
			//TODO write the doctype declaration
		}
		catch(IOException ioException)	//if there is an error
		{
			throw new JspException(ioException);	//pass the exception back
		}
		return SKIP_BODY;	//we have no body to render		
	}


	/**Releases references to any acquired resources.*/
	public void release()
	{
		contentType=null;
		rootElement=null;
		publicID=null;
		systemID=null;
	}
}

