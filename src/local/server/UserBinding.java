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

import java.util.*;
import java.text.*;
import org.zoolu.sip.address.*;
import org.zoolu.sip.header.ContactHeader;

/** This class represents a user record of the Location Service DB.
  * <p> A UserBinding contains the user name, an opaque data, and a set of
  * contact information (i.e. contact and expire-time).
  * <p> Method getContacts() returns an Enumeration of String values
  * rapresenting the various contact SipURLs.
  * Such values can be used as keys for getting for each contact
  * both the contact NameAddress and the expire Date. 
  */
public class UserBinding
{
   /** User name */
   String name;
   /** Opaque data */
   String data;
   /** Hashtable of ContactHeader with String as key. */
   Hashtable contact_list;
   
   /** Costructs a new UserBinding for user <i>name</i> with no opaque data.
     * @param name the user name */
   public UserBinding(String name)
   {  this.name=name;
      data=null;
      contact_list=new Hashtable();
   }
   
   /** Costructs a new UserBinding for user <i>name</i>
     * with opaque data <i>data</i>.
     * @param name the user name 
     * @param data the user data */
   public UserBinding(String name, String data)
   {  this.name=name;
      this.data=data;
      contact_list=new Hashtable();
   }

   /** Gets the user name.
     * @return the user name */
   public String getName()
   {  return name;
   }

   /** Gets the user data.
     * @return the user data */
   public String getData()
   {  return data;
   }

   /** Sets the user data.
     * @param data the new user data 
     * @return this object */
   public UserBinding setData(String data)
   {  this.data=data;
      return this;
   }
   
   /** Gets the user contacts.
     * @return the user contacts as an Enumeration of String */
   public Enumeration getContacts()
   {  return contact_list.keys();
   }

   /** Whether the user has any registered contact.
     * @param url the contact url (String) 
     * @return true if one or more contacts are present */
   public boolean hasContact(String url)
   {  return contact_list.containsKey(url);
   }
   
   /** Adds a new contact.
     * @param contact the contact address (NameAddress) 
     * @param expire the expire value (Date) 
     * @return this object */
   public UserBinding addContact(NameAddress contact, Date expire)
   {  contact_list.put(contact.getAddress().toString(),(new ContactHeader(contact)).setExpires(expire));
      return this;
   }
 
   /** Removes a contact.
     * @param url the contact url (String) 
     * @return this object */
   public UserBinding removeContact(String url)
   {  if (contact_list.containsKey(url)) contact_list.remove(url);
      return this;
   }  
   
   /** Removes all contacts.
     * @return this object */
   public UserBinding removeContacts()
   {  contact_list.clear();
      return this;
   }

   /** Gets NameAddress of a contact.
     * @param url the contact url (String) 
     * @return the contact NameAddress, or null if the contact is not present */
   public NameAddress getNameAddress(String url)
   {  if (contact_list.containsKey(url)) return ((ContactHeader)contact_list.get(url)).getNameAddress();
      else return null;
   }

   /** Whether the contact is expired.
     * @param url the contact url (String) 
     * @return true if the contact is expired or contact does not exist */
   public boolean isExpired(String url)
   {  if (contact_list.containsKey(url)) return ((ContactHeader)contact_list.get(url)).isExpired();
      else return true;
   }
   
   /** Gets expiration date.
     * @param url the contact url (String) 
     * @return the expire Date */
   public Date getExpirationDate(String url)
   {  ContactHeader contact=(ContactHeader)contact_list.get(url);
      //System.out.println("DEBUG: UserBinding: ContactHeader: "+contact.toString());
      //System.out.println("DEBUG: UserBinding: expires param: "+contact.getParameter("expires"));
      if (contact_list.containsKey(url)) return ((ContactHeader)contact_list.get(url)).getExpiresDate();
      else return null;
   }
   
   /** Gets the String value of this Object.
     * @return the String value */
   public String toString()
   {  String str="To: "+name+"\r\n";
      if (data!=null) str+="User-Data: "+data+"\r\n";
      for (Enumeration i=getContacts(); i.hasMoreElements(); )
      {  ContactHeader ch=(ContactHeader)contact_list.get(i.nextElement());
         str+=ch.toString();
      }
      return str;
   }
}

