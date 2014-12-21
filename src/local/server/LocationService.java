/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
 * 
 * This source code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package local.server;

import org.zoolu.sip.address.*;
import java.util.Enumeration;
import java.util.Date;


/** LocationService is the interface used by SipRegistrar to access to the
  * local Location Service.
  * <p> A LocationService allows the maintinance of bindings between users and contacts.
  * <br> For each user the LocationService should maintain information regarding:
  * <br> - username, that is a fully qualified name for this service (e.g. alice@wonderland.net)
  * <br> - data, that is an opaque block of data (a string),
  *        that can be set and fetched for any service-depending use,
  * <br> - contacts/expires, that is the list of user contacts with the time when it expires,
  * <p> LocationService has a set of methods for query and modifing such data.
  * <p> Some of these methods include an optional parameter <i>app</i> that could be used 
  * to implement application-dependent mobility, i.e. lists of contacts that are specific
  * for particular applications. This feature might be used by guessing the application
  * by the SIP body (e.g. SDP) or by a new non-standard Application header (ref. [draft-XX.txt])  
  */
public interface LocationService
{
   /** Syncronizes the database.
     * <p> Can be used, for example, to save the current memory image of the DB. */
   public void sync();

   /** Returns the numbers of users in the database.
     * @return the numbers of user entries */
   public int size();
   
   /** Returns an enumeration of the users in this database.
     * @return the list of user names as an Enumeration of String */
   public Enumeration getUsers();
      
   /** Whether a user is present in the database and can be used as key.
     * @param user the user name
     * @return true if the user name is present as key */
   public boolean hasUser(String user);
   
   /** Adds a new user at the database.
     * @param user the user name
     * @param data the user data
     * @return this object */
   public LocationService addUser(String user, String data);
   
   /** Removes the user from the database.
     * @param user the user name
     * @return this object */
   public LocationService removeUser(String user);

   /** Gets the user data.
     * @param user the user name
     * @return the user data */
   public String getUserData(String user);   

   /** Sets the user data.
     * @param user the user name
     * @param data the user data
     * @return this object */
   public LocationService setUserData(String user, String data);   
      
   /** Adds a contact.
     * @param user the user name
     * @param contact the contact NameAddress
     * @param expire the contact expire Date
     * @return this object */
   public LocationService addUserContact(String user, NameAddress contact, Date expire);

   /** Whether the user has contact <i>url</i>.
     * @param user the user name
     * @param url the contact URL
     * @return true if is the contact present */
   public boolean hasUserContact(String user, String url);

   /** Gets the user contacts that are not expired.
     * @param user the user name
     * @return the list of contacts as Enumeration of String */
   public Enumeration getUserContacts(String user);

   /** Removes a contact.
     * @param user the user name
     * @param url the contact URL
     * @return this object */
   public LocationService removeUserContact(String user, String url);
   
   /** Gets NameAddress value of the user contact.
     * @param user the user name
     * @param url the contact URL
     * @return the contact NameAddress */
   public NameAddress getUserContactNameAddress(String user, String url);

   /** Gets expiration date of the user contact.
     * @param user the user name
     * @param url the contact URL
     * @return the contact expire Date */
   public Date getUserContactExpirationDate(String user, String url);
   
   /** Whether the contact is expired.
     * @param user the user name
     * @param url the contact URL
     * @return true if it has expired */
   public boolean isUserContactExpired(String user, String url);
   
   /** Removes all users from the database.
     * @return this object */
   public LocationService removeAllUsers();

   /** Removes all contacts from the database.
     * @return this object */
   public LocationService removeAllContacts();

   /** Gets the String value of this Object.
     * @return the String value */
   public String toString();

}
