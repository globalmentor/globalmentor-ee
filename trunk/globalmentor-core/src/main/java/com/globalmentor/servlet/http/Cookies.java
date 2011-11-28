/*
 * Copyright © 1996-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globalmentor.servlet.http;

import javax.servlet.http.*;

/**Cookie manipulator class.
@author Garret Wilson
@see Cookie
@see HttpServletRequest
*/
public class Cookies
{

	/**Returns the cookie with the specified name. Created because
		javax.servlet.http.HttpServletRequest does not have a method to retrieve a
		specific cookie.
	@param cookieArray An array of cookies.
	@param cookieName The name of the cookie to find in the array.
	@return The cookie with the specified name, or <code>null</code> if no cookie
		with the specified name exists.
	*/
	public static Cookie getCookie(final Cookie[] cookieArray, final String cookieName)
	{
		if(cookieArray!=null)	//if they passed us a valid array
		{
			for(int i=0; i<cookieArray.length; ++i)	//look at each cookie
			{
				final Cookie cookie=cookieArray[i];	//get a reference to this cookie
				if(cookie.getName().equals(cookieName))	//if this cookie's name matches
					return cookie;	//return the cookie
			}
		}
		return null;	//return null since we couldn't find a cookie with a matching name
	}

}
