package com.garretwilson.faces.component;

import java.io.*;

import javax.faces.component.*;
import javax.faces.context.*;
import javax.faces.convert.*;
import javax.faces.el.*;

import org.apache.commons.fileupload.*;

import static com.garretwilson.io.FileUtilities.*;

/**Input component for uploading files.
@author Garret Wilson
*/
public class UIFileInput extends UIInput
{

	/**The directory value binding variable name.*/
	public static String DIRECTORY_VAR="directory";

	/**The file value binding variable name.*/
	public static String FILENAME_VAR="filename";

	/**The directory in which to store files, which overrides any value binding.*/
	private File directory=null;

		/**@return The directory in which to store files, if there is one.*/
		protected File getDirectory()	//TODO refactor this into a generic routine, and replace the code in UIBurrow
		{
			if(directory!=null)	//if we have a directory
			{
				return directory;	//return our directory
			}
			else	//if we don't have a directory
			{
				final ValueBinding valueBinding=getValueBinding(DIRECTORY_VAR);	//get a value binding to the directory
				if(valueBinding!=null)//if there is a value binding
				{
					return (File)valueBinding.getValue(getFacesContext());	//get the value binding expression's value
				}
			}
			return null;	//show that we couldn't find a value binding
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
		protected String getFilename()
		{
			if(filename!=null)	//if we have a filename
			{
				return filename;	//return our filename
			}
			else	//if we don't have a filename
			{
				final ValueBinding valueBinding=getValueBinding(FILENAME_VAR);	//get a value binding to the filename
				if(valueBinding!=null)//if there is a value binding
				{
					return (String)valueBinding.getValue(getFacesContext());	//get the value binding expression's value
				}
			}
			return null;	//show that we couldn't find a value binding
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
	public UIFileInput()
	{
		super();	//construct the parent class
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
			//TODO make sure the original FileItem gets removed elsewhere in the application, in case there's an error, so it won't be sitting around taking up space
		final Object convertedValue;	//we'll determined the converted value
		final Object defaultConvertedValue=super.getConvertedValue(context, newSubmittedValue);	//do the default conversion
		if(newSubmittedValue instanceof FileItem)	//if the submitted value is a file item
		{
			final FileItem fileItem=(FileItem)newSubmittedValue;	//get the submitted value as a file item
			assert !fileItem.isFormField() : "File item isn't expected to be a form field for file input.";
			final File directory=getDirectory();	//get the directory in which to store files
			if(directory!=null)	//if a directory is specified
			{
				final String filename;	//we'll determine the filename to use
				if(getFilename()!=null)	//if a filename is explicitly specified
				{
					filename=getFilename();	//use the specified filename
				}
				else	//if no filename is specified
				{
					filename=fileItem.getName();	//get the filename suggested to us by the client
				}
				if(filename!=null)	//if we have a filename
				{
						//if there is a file separator character in the filename, throw an exception
						//---this could be a security breach from a rogue client!
					if(filename.indexOf(File.separatorChar)>=0)		//if the filename isn't a simple one
					{
						throw new ConverterException(filename+" is not a simple filename.");
					}
					try
					{
						mkdirs(directory);	//make sure the directory exists
						final File file=new File(directory, filename);	//we now know which file to use
						fileItem.write(file);	//write the file item to a file
					}
					catch(final Exception exception)	//if there was a problem writing the file to a directory (we can't just check for an IOException, because FileItem.write() can throw a general exception)
					{
						throw new ConverterException(exception);
					}
				}				
			}
			convertedValue=null;	//if we process the file item, we'll always return nothing
		}
		else	//if the value is not a file item
		{
			convertedValue=defaultConvertedValue;	//keep the default converted value
		}
		return convertedValue;	//return our converted value
	}

}
