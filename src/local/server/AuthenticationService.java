package local.server;


import org.zoolu.sip.provider.SipStack;
import org.zoolu.tools.Parser;
import org.zoolu.tools.Base64;

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.*;


/** Class AuthenticationService implements an information repository for AAA
  */
public class AuthenticationService
{
   /** AuthenticationService file extension. */
   final String default_fext=".db";

   /** AuthenticationService name */
   String fname=null;
   /** Whether the AuthenticationService DB has been changed without saving */
   boolean changed=false;
   /** AuthenticationServer DB */
   Hashtable users;

   /** Costructs a new AuthenticationService */
   public AuthenticationService(String file_name)
   {  fname=file_name;
      users=new Hashtable();
      load();
   }
   
   private static final byte[] NULL_ARRAY=new byte[0];


   /** Returns the name of the database */
   public String getName() { return fname; }

   /** Returns the numbers of users in the database */
   public int size()
   {  return users.size();
   }
   
   /** Syncronizes the database. Save onto the hard-disk */
   public void sync()
   {  if (changed) save();
   }

   /** Returns an enumeration of the keys in this database */
   public Enumeration getUsers()
   {  return users.keys();
   }
   
   /** Tests if the specified user is a key in this database */
   public boolean hasUser(String user)
   {  return (users.containsKey(user));
   }
   
   /** Adds a user in the database */
   public void addUser(String user, byte[] key)
   {  addUser(user,key,NULL_ARRAY,0);
   }

   /** Adds a user in the database */
   public void addUser(String user, byte[] key, byte[] rand, int seqn)
   {  if (hasUser(user)) return;
      UserAuthInfo ur=new UserAuthInfo(user,key,rand,seqn);
      users.put(user,ur);
      changed=true;
   }
   
   /** Removes the user from the database */
   public void removeUser(String user)
   {  users.remove(user);
      changed=true;
   }



   /** Gets boolean value to indicate if the database has changed */
   public boolean isChanged() { return changed; }




   /** Adds a user record in the database */
   private void addUserAuthInfo(UserAuthInfo ur)
   {  if (hasUser(ur.getName())) removeUser(ur.getName());
      users.put(ur.getName(),ur);
   }
   
   /** Gets the record of the user */
   private UserAuthInfo getUserAuthInfo(String user)
   {  return (UserAuthInfo)users.get(user);  
   }
 
   /** Returns an enumeration of the values in this database */
   private Enumeration getUserAuthInfos()
   {  return users.elements();
   }
   
 
   
   /** Gets the user key */
   public byte[] getUserKey(String user)
   {  if (hasUser(user)) return getUserAuthInfo(user).getKey();
      else return null;
   }   
   /** Sets the user key */
   public void setUserKey(String user, byte[] key)
   {  UserAuthInfo ur=getUserAuthInfo(user);
      if (ur!=null)
      {  ur.setKey(key);
         changed=true;
      }
   }   
      
      
   /** Gets the user rand */
   public byte[] getUserRand(String user)
   {  if (hasUser(user)) return getUserAuthInfo(user).getRand();
      else return null;
   }   
   /** Sets the user rand */
   public void setUserRand(String user, byte[] rand)
   {  UserAuthInfo ur=getUserAuthInfo(user);
      if (ur!=null)
      {  ur.setRand(rand);
         changed=true;
      }
   }   

         
   /** Gets the user sequence number */
   public int getUserSeqnum(String user)
   {  if (hasUser(user)) return getUserAuthInfo(user).getSeqnum();
      else return 0;
   }   
   /** Gets the user sequence number */
   public int incUserSeqnum(String user)
   {  if (hasUser(user)) return getUserAuthInfo(user).incSeqnum();
      else return 0;
   }   
   /** Sets the user sequence number */
   public void setUserSeqnum(String user, int sqn)
   {  UserAuthInfo ur=getUserAuthInfo(user);
      if (ur!=null)
      {  ur.setSeqnum(sqn);
         changed=true;
      }
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
      String user=null; byte[] key=NULL_ARRAY; byte[] rand=NULL_ARRAY; int seqn=0;
      while (true)
      {  String line=null;
         try { line=in.readLine(); } catch (Exception e) { e.printStackTrace(); System.exit(0); }   

         if (line==null)
            break;

         Parser par=new Parser(line);

         if (line.startsWith("#"))
            continue;         
         if (line.startsWith("user"))
         {  if (user!=null) addUser(user,key,rand,seqn);
            user=par.goTo('=').skipChar().getString();  
            key=NULL_ARRAY;
            rand=NULL_ARRAY;
            seqn=0;      
            continue;
         }
         if (line.startsWith("key"))
         {  key=Base64.decode(par.goTo('=').skipChar().getString());         
            continue;
         }
         if (line.startsWith("rand"))
         {  rand=Base64.decode(par.goTo('=').skipChar().getString());         
            continue;
         }
         if (line.startsWith("seqn"))
         {  seqn=par.goTo('=').skipChar().getInt();         
            continue;
         }
      }
      if (user!=null) addUser(user,key,rand,seqn);

      try
      {  in.close();
      }
      catch (Exception e) { e.printStackTrace(); } 
   }
 
 
   /** Saves the database */
   public void save()
   {  BufferedWriter out=null;
      changed=false;
      String filename=fname;
      if (filename.indexOf(".")<0) filename+=default_fext;
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
 
   /** Gets string representation */
   public String toString()
   {  String str="";
      for (Enumeration e=getUserAuthInfos(); e.hasMoreElements(); )
      {  UserAuthInfo ur=(UserAuthInfo)e.nextElement();
         String name=ur.getName();
         byte[] key=ur.getKey();
         byte[] rand=ur.getRand();
         int seqn=ur.getSeqnum();
         
         str+="user= "+name+"\r\n";
         str+="key= "+Base64.encode(key)+"\r\n";
         if (rand.length>0) str+="rand= "+Base64.encode(rand)+"\r\n";
         if (seqn>0) str+="seqn= "+String.valueOf(seqn)+"\r\n";
         //str+="\r\n";
      }
      return str;
   }
   
}



/** User's authentication info */
class UserAuthInfo
{
   String name;
   String getName() { return name; }   
   void setName(String user_name) { name=user_name; }
   
   byte[] key;
   byte[] getKey() { return key; }  
   void setKey(byte[] user_key) { key=user_key; }
   
   byte[] rand;
   byte[] getRand() { return rand; }
   void setRand(byte[] user_rand) { rand=user_rand; }

   int seqnum;
   int getSeqnum() { return seqnum; }
   int incSeqnum() { return ++seqnum; }
   void setSeqnum(int user_seqnum) { seqnum=user_seqnum; }

   
   UserAuthInfo(String user_name, byte[] user_key, byte[] user_rand, int user_seqnum)
   {  name=user_name;
      key=user_key;
      rand=user_rand;
      seqnum=user_seqnum;
   } 
}

