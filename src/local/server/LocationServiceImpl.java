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
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.provider.SipParser;
import org.zoolu.sip.header.SipHeaders;
import org.zoolu.tools.Parser;
import org.zoolu.tools.LogLevel;
import java.io.*;
import java.util.*;


/** LocationServiceImpl is a simple implementation of a LocationService.
  * LocationServiceImpl allows creation and maintainance of a
  * Location Service for registered users.
  */
public class LocationServiceImpl implements LocationService
{
   /** LocationService file extension. */
   final String default_fext=".db";
 
   
   /** LocationService name. */
   String fname=null;
   
   /** Whether the Location DB has been changed without saving. */
   boolean changed=false;
   
   /** Users bindings. Set of pairs of { (String)user , (UserBinding)binding }. */
   Hashtable users;
   
   /** Creates a new LocationServiceImpl */
   public LocationServiceImpl(String name)
   {  fname=name;
      users=new Hashtable();
      load();
   }

   /** Returns the name of the database */
   public String getName() { return fname; }


   // **************** Methods of interface LocationService ****************

   /** Syncronizes the database.
     * <p> Can be used, for example, to save the current memory image of the DB. */
   public void sync()
   {  if (changed) save();
   }

   /** Returns the numbers of users in the database.
     * @return the numbers of user entries */
   public int size()
   {  return users.size();
   }
   
   /** Returns an enumeration of the users in this database.
     * @return the list of user names as an Enumeration of String */
   public Enumeration getUsers()
   {  return users.keys();
   }
      
   /** Whether a user is present in the database and can be used as key.
     * @param user the user name
     * @return true if the user name is present as key */
   public boolean hasUser(String user)
   {  return (users.containsKey(user));
   }
   
   /** Adds a new user at the database.
     * @param user the user name
     * @param data the user data
     * @return this object */
   public LocationService addUser(String user, String data)
   {  if (hasUser(user)) return this;
      UserBinding ur=new UserBinding(user,data);
      users.put(user,ur);
      changed=true;
      return this;
   }
      
   /** Removes the user from the database.
     * @param user the user name
     * @return this object */
   public LocationService removeUser(String user)
   {  if (!hasUser(user)) return this;
      //else
      users.remove(user);
      changed=true;
      return this;
   }
   
   /** Gets the user data.
     * @param user the user name
     * @return the user data */
   public String getUserData(String user)
   {  if (!hasUser(user)) return null;
      //else
      return getUserBinding(user).getData();
   }   

   /** Sets the user data.
     * @param user the user name
     * @param data the user data
     * @return this object */
   public LocationService setUserData(String user, String data)
   {  if (!hasUser(user)) addUser(user,null);
      UserBinding ur=getUserBinding(user);
      ur.setData(data);
      changed=true;
      return this;
   }   
      
   /** Adds a contact.
     * @param user the user name
     * @param contact the contact NameAddress
     * @param expire the contact expire Date
     * @return this object */
   public LocationService addUserContact(String user, NameAddress name_addresss, Date expire)
   {  if (!hasUser(user)) addUser(user,null);
      UserBinding ur=getUserBinding(user);
      ur.addContact(name_addresss,expire);
      changed=true;
      return this;
   }

   /** Whether the user has contact <i>url</i>.
     * @param user the user name
     * @param url the contact URL
     * @return true if is the contact present */
   public boolean hasUserContact(String user, String url)
   {  if (!hasUser(user)) return false;
      //else
      return getUserBinding(user).hasContact(url);
   }

   /** Gets the user contacts that are not expired.
     * @param user the user name
     * @return the list of contacts as Enumeration of String */
   public Enumeration getUserContacts(String user)
   {  if (!hasUser(user)) return null;
      //else
      changed=true;
      return getUserBinding(user).getContacts();
   }

   /** Removes a contact.
     * @param user the user name
     * @param url the contact URL
     * @return this object */
   public LocationService removeUserContact(String user, String url)
   {  if (!hasUser(user)) return this;
      //else
      UserBinding ur=getUserBinding(user);
      ur.removeContact(url);
      changed=true;
      return this;
   }   
   
   /** Gets NameAddress value of the user contact.
     * @param user the user name
     * @param url the contact URL
     * @return the contact NameAddress */
   public NameAddress getUserContactNameAddress(String user, String url)
   {  if (!hasUser(user)) return null;
      //else
      return getUserBinding(user).getNameAddress(url);
   }

   /** Gets expiration date of the user contact.
     * @param user the user name
     * @param url the contact URL
     * @return the contact expire Date */
   public Date getUserContactExpirationDate(String user, String url)
   {  if (!hasUser(user)) return null;
      //else
      return getUserBinding(user).getExpirationDate(url);
   }
   
   /** Whether the contact is expired.
     * @param user the user name
     * @param url the contact URL
     * @return true if it has expired */
   public boolean isUserContactExpired(String user, String url)
   {  if (!hasUser(user)) return true;
      //else
      return getUserBinding(user).isExpired(url);
   }
   
   /** Gets the String value of user information.
     * @return the String value for that user */
   /*public String userToString(String user)
   {  return getUserBinding(user).toString();
   }*/

   /** Removes all users from the database.
     * @return this object */
   public LocationService removeAllUsers()
   {  users.clear();
      changed=true;
      return this;
   }

   /** Removes all contacts from the database.
     * @return this object */
   public LocationService removeAllContacts()
   {  for (Enumeration i=getUserBindings(); i.hasMoreElements(); )
      {  ((UserBinding)i.nextElement()).removeContacts();
      }
      changed=true;
      return this;
   }

   /** Gets the String value of this Object.
     * @return the String value */
   public String toString()
   {  String str="";
      for (Enumeration i=getUserBindings(); i.hasMoreElements(); )
      {  UserBinding u=(UserBinding)i.nextElement();
         str+=u.toString();
      }
      return str;
   }


   // ******************************* New methods *******************************

   /** Gets boolean value to indicate if the database has changed */
   public boolean isChanged() { return changed; }

   /** Returns an enumeration of the values in this database */
   public Enumeration getUserBindings()
   {  return users.elements();
   }
   
   /** Adds a user record in the database */
   public void addUserBinding(UserBinding ur)
   {  if (hasUser(ur.getName())) removeUser(ur.getName());
      users.put(ur.getName(),ur);
   }
   
   /** Gets the user record of the user */
   public UserBinding getUserBinding(String user)
   {  return (UserBinding)users.get(user);  
   }
   
   /** Loads the database */
   public void load()
   {  BufferedReader in=null;
      changed=false;
      String filename=fname;
      if (filename.indexOf(".")<0) filename+=default_fext;
      try { in = new BufferedReader(new FileReader(filename)); }
      catch (FileNotFoundException e)
      {  System.err.println("WARNING: file \""+filename+"\" not found: created new empty DB");
         return;
      }   
      String user=null;
      NameAddress name_address=null;
      Date expire=null;
      while (true)
      {  String line=null;
         try { line=in.readLine(); }
            catch (Exception e) { e.printStackTrace(); System.exit(0); }   
         if (line==null)
            break;
         if (line.startsWith("#"))
            continue;
         if (line.startsWith("To"))
         {  Parser par=new Parser(line);
            user=par.skipString().getString();
            //System.out.println("add user: "+user);
            addUser(user,null);
            continue;
         }
         if (line.startsWith("User-Data:"))
         {  Parser par=new Parser(line);
            String data=par.skipString().getRemainingString().trim();
            getUserBinding(user).setData(data);
            continue;
         }
         if (line.startsWith(SipHeaders.Contact))
         {  SipParser par=new SipParser(line);
            name_address=((SipParser)par.skipString()).getNameAddress();
            //System.out.println("DEBUG: "+name_address);
            expire=(new SipParser(par.goTo("expires=").skipN(8).getStringUnquoted())).getDate(); 
            //System.out.println("DEBUG: "+expire);
            getUserBinding(user).addContact(name_address,expire);
            continue;
         }  
      }
      try { in.close(); } catch (Exception e) { e.printStackTrace(); } 
   }
 
 
   /** Saves the database */
   public void save()
   {  BufferedWriter out=null;
      changed=false;
      String filename=fname;
      if (filename.indexOf(".")<0) filename+=default_fext;
     //System.out.println(this.toString());
      try
      {  out=new BufferedWriter(new FileWriter(filename));
         out.write(this.toString());
         out.close();
      }
      catch (IOException e)
      {  System.err.println("WARNING: error trying to write on file \""+filename+"\"");
         return;
      }
   }
   
}
