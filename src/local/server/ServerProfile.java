package local.server;


import org.zoolu.sip.address.*;
import org.zoolu.sip.provider.*;
import org.zoolu.tools.Configure;
import org.zoolu.tools.Parser;

import java.io.*;
import java.net.InetAddress;
import java.util.Vector;


/** ServerProfile maintains the server configuration
  */
public class ServerProfile extends Configure
{
   /** The default configuration file */
   private static String config_file="mjsip.cfg";

       
   // ********************* server configurations ********************

   /** The domain names that the server administers.
     * <p>It lists the domain names for which the Location Service maintains user bindings.
     * <br>Use 'auto-configuration' for auto domanin name configuration. */
   public String[] domain_names={};
   /** Whether consider any port as valid local local domain port
     * (regardless which sip port is used). */
   public boolean domain_port_any=false;
   /** The LocationService DB name. */
   public String location_db="users.db";
   /** Whether LocationService DB has to be cleaned at startup. */
   public boolean clean_location_db=false;
   /** The Registrar DB Class. */
   //public static String registrar_db_class="sipx.db.RegistrarDBImplementation";
   /** Whether the Server should act as Registrar (i.e. respond to REGISTER requests). */   
   public boolean is_registrar=true;
   /** Whether the Registrar can register new users (i.e. REGISTER requests from unregistered users). */   
   public boolean register_new_users=true;
   /** Whether the Server authenticates local users. */   
   public boolean do_authentication=false;
   /** The authentication realm.
     * If not defined or equal to 'NONE' (default), the used via address is used instead. */
   public String authentication_realm=null;
   /** The AuthenticationService DB name. */
   public String authentication_db="aaa.db";
   /** Whether the Server relays requests for non-local users. */   
   //public boolean do_proxy=true;
   /** Whether maintaining a complete call log. */   
   public boolean call_log=false;
   /** Whether the server should stay in the signaling path (uses Record-Route/Route) */
   public boolean on_route=false;
   /** Whether refer to the RFC3261 Loose Route (or RFC2543 Strict Route) rule */   
   public boolean loose_route=true;


   // ************************** costructors *************************
   
   /** Costructs a new ServerProfile */
   public ServerProfile(String file)
   {  // load SipStack first
      if (!SipStack.isInit()) SipStack.init();
      // load configuration
      loadFile(file);
      // post-load manipulation
      if (authentication_realm!=null && authentication_realm.equals(Configure.NONE)) authentication_realm=null;
   }


   /** Parses a single line of the file */
   protected void parseLine(String line)
   {  String attribute;
      Parser par;
      int index=line.indexOf("=");
      if (index>0) {  attribute=line.substring(0,index).trim(); par=new Parser(line,index+1);  }
      else {  attribute=line; par=new Parser("");  }
            
      if (attribute.equals("location_db")) { location_db=par.getString(); return; }
      if (attribute.equals("clean_location_db")) { clean_location_db=(par.getString().toLowerCase().startsWith("y")); return; }
      //if (attribute.equals("registrar_db_class")) { registrar_db_class=par.getString(); return; }
      if (attribute.equals("is_registrar")) { is_registrar=(par.getString().toLowerCase().startsWith("y")); return; }
      if (attribute.equals("register_new_users")) { register_new_users=(par.getString().toLowerCase().startsWith("y")); return; }
      if (attribute.equals("do_authentication")) { do_authentication=(par.getString().toLowerCase().startsWith("y")); return; }
      if (attribute.equals("authentication_realm")) { authentication_realm=par.getString(); return; }
      if (attribute.equals("authentication_db")) { authentication_db=par.getString(); return; }
      //if (attribute.equals("do_proxy")) { do_proxy=(par.getString().toLowerCase().startsWith("y")); return; }
      if (attribute.equals("call_log")) { call_log=(par.getString().toLowerCase().startsWith("y")); return; }
      if (attribute.equals("on_route")) { on_route=(par.getString().toLowerCase().startsWith("y")); return; }
      if (attribute.equals("loose_route")) { loose_route=(par.getString().toLowerCase().startsWith("y")); return; }

      if (attribute.equals("domain_port_any")) { domain_port_any=(par.getString().toLowerCase().startsWith("y")); return; }
      if (attribute.equals("domain_names"))
      {  Vector aux=new Vector();
         char[] delim={' ',','};
         do { aux.add(par.getWord(delim)); } while (par.hasMore());
         int len=aux.size();
         if (len==0 || aux.elementAt(0).equals(SipProvider.AUTO_CONFIGURATION))
         {  // auto configuration
            String host_addr=null;
            String host_name=null;
            try
            {  InetAddress address=java.net.InetAddress.getLocalHost();
               host_addr=address.getHostAddress();
               host_name=address.getHostName();
            }
            catch (java.net.UnknownHostException e)
            {  if (host_addr==null) host_addr="127.0.0.1";
               if (host_name==null) host_name="localhost";
            }
            domain_names=new String[2];
            domain_names[0]=host_addr;
            domain_names[1]=host_name;
         }
         else
         {  // manual configuration
            domain_names=new String[len];
            for (int i=0; i<len; i++) 
               domain_names[i]=(String)aux.elementAt(i);
         }
         return;
      }
   }  


   /** Converts the entire object into lines (to be saved into the config file) */
   protected String toLines()
   {  // currently not implemented..
      return toString();
   }


   // **************************** others ****************************     
 
   /** Gets a String representation */
   public String toString()
   {  return domain_names.toString();
   }

}
