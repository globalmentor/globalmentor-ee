package com.garretwilson.faces.component;

import java.io.*;

import javax.faces.component.*;
import javax.faces.context.*;
import javax.faces.convert.*;
import javax.faces.el.*;
import javax.mail.internet.*;

import com.garretwilson.faces.component.ComponentUtilities;
import com.garretwilson.faces.component.renderkit.xhtml.InputFileRenderer;
import com.garretwilson.lang.ClassUtilities;
import static com.garretwilson.text.CharacterEncoding.*;
import com.garretwilson.util.Debug;

import org.apache.commons.fileupload.*;

import static com.garretwilson.io.Files.*;
import static com.garretwilson.text.TextUtilities.*;

/**Input component for uploading files.
@author Garret Wilson
*/
public class UIInputFile extends UIInput
{

	public static final String COMPONENT_TYPE=ClassUtilities.getFullName(UIInputFile.class, "InputFile");

	/**The directory value binding variable name.*/
	public static String DIRECTORY_VAR="directory";

	/**The file value binding variable name.*/
	public static String FILENAME_VAR="filename";

	/**The directory in which to store files, which overrides any value binding.*/
	private File directory=null;

		/**@return The directory in which to store files, if there is one.*/
		public File getDirectory()
		{
			return ComponentUtilities.getValue(this, getFacesContext(), directory, DIRECTORY_VAR);	//get the local value or value binding
		}		

		/**Sets the directory.
		@param directory The directory in which to store files.
		*/
		public void setDirectory(final File directory)
		{
			this.directory=directory;	//set the directory
		}

	/**The filename to use when storing files, which overrides any value binding.*/
	private String filename=null;

		/**@return The filename to use when storing files, or <code>null</code> if
			the default name of the submitted file should be used.
		*/
		public String getFilename()
		{
			return ComponentUtilities.getValue(this, getFacesContext(), filename, FILENAME_VAR);	//get the local value or value binding
		}		

		/**Sets the filename to use when storing the file.
		@param filename The filename to use when storing the file, or
			<code>null</code> if the submitted default filename should be used; this
			value will be ignored if file storing is not enabled.
		*/
		public void setFilename(final String filename)
		{
			this.filename=filename;	//set the filename
		}

	/**Default constructor.*/
	public UIInputFile()
	{
		super();	//construct the parent class
		setRendererType(InputFileRenderer.RENDERER_TYPE);	//default to a file renderer
	}

	/**Convert the submitted value into a "local value" of the
		appropriate data type, if necessary.
	This version stores the data in the given directory is one is specified.
		Otherwise, the literal bytes of the file are returned. If the file can
		be determined to be a text file, a string is returned.
	@param context JSF context for the request being processed.
	@param newSubmittedValue A value stored on the component during
		<code>decode</code>.
	@exception ConverterException if the submitted value
		cannot be converted successfully.
	@exception NullPointerException if <code>context</code>
		or <code>component</code> is <code>null</code>
	*/
	protected Object getConvertedValue(final FacesContext context, final Object newSubmittedValue) throws ConverterException
	{
//G***del Debug.setDebug(true);	//G***del
//G***del Debug.trace("getting converted value of input field");
			//TODO make sure the original FileItem gets removed elsewhere in the application, in case there's an error, so it won't be sitting around taking up space
		final Object convertedValue;	//we'll determined the converted value
		final Object defaultConvertedValue=super.getConvertedValue(context, newSubmittedValue);	//do the default conversion
		if(newSubmittedValue instanceof FileItem)	//if the submitted value is a file item
		{
Debug.trace("the submitted item is a fileitem");
			final FileItem fileItem=(FileItem)newSubmittedValue;	//get the submitted value as a file item
			assert !fileItem.isFormField() : "File item isn't expected to be a form field for file input.";
			final File directory=getDirectory();	//get the directory in which to store files
			if(directory!=null)	//if a directory is specified
			{
Debug.trace("we have a directory:", directory);
				final String filename;	//we'll determine the filename to use
				if(getFilename()!=null)	//if a filename is explicitly specified
				{
					filename=getFilename();	//use the specified filename
				}
				else	//if no filename is specified
				{
					filename=fileItem.getName();	//get the filename suggested to us by the client
				}
				if(filename!=null && filename.length()>0)	//if we have a filename
				{
Debug.trace("we have a filename:", filename);
						//if there is a file separator character in the filename, throw an exception
						//---this could be a security breach from a rogue client!
					if(filename.indexOf(File.separatorChar)>=0)		//if the filename isn't a simple one
					{
						throw new ConverterException(filename+" is not a simple filename.");
					}
					try
					{
Debug.trace("making sure directory exists");
						if(!directory.isDirectory())	//if the directory doesn't exist as a directory
						{
							mkdirs(directory);	//try to create the the directory
						}
						final File file=new File(directory, filename);	//we now know which file to use
Debug.trace("file to write is:", file);
						fileItem.write(file);	//write the file item to a file
					}
					catch(final Exception exception)	//if there was a problem writing the file to a directory (we can't just check for an IOException, because FileItem.write() can throw a general exception)
					{
Debug.error(exception);
						throw new ConverterException(exception);
					}
				}				
			}
			else	//if there is no directory specified
			{
//G***del Debug.trace("no directory");
				try
				{
					final byte[] bytes=fileItem.get();	//get the bytes of the file
					final ContentType contentType=new ContentType(fileItem.getContentType());	//get the content type of the file
//G***del Debug.trace("uploaded file content type", contentType);
					if(isText(contentType))	//if this is a text file
					{
//G***del Debug.trace("is text type");
						final String encoding=UTF_8;	//TODO get the encoding from the file if we can; look at the content type, test a text file, and look into an XML file
						final String string=new String(bytes, encoding);	//convert the bytes to a string using the correct encoding
						return string;	//return the string representation of the file contents
					}
					else	//if this is not a text file
					{
						return bytes;	//return the binary contents of the file
					}
				}
				catch(final ParseException parseException)	//if there is a problem parsing the content type
				{
					throw new ConverterException(parseException);
				}
				catch(final UnsupportedEncodingException unsupportedEncodingException)	//if we don't recognize the file encoding
				{
					throw new ConverterException(unsupportedEncodingException);
				}
			}
			convertedValue=null;	//if we process the file item, we'll always return nothing by default unless we decided to return the contents of the file
		}
		else	//if the value is not a file item
		{
			convertedValue=defaultConvertedValue;	//keep the default converted value
		}
		return convertedValue;	//return our converted value
	}
}
