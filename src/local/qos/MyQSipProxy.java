package local.qos;


import local.server.ServerProfile;
import org.zoolu.sip.provider.*;

//import de.tud.kom.rsvp.*;


/** MyQSipProxy is an implementation of a Q-SIP Proxy Server.
  * Class MyQSipProxy extends class QSipProxy.
  * <p />
  * Currently it does not use any
  * QoSProvider and it can be used just as skeleton for further implementations.
  * <p />
  * Modify this class or rename it if you want to implement a new Q-SIP Proxy
  * with your own QoSProvider implementation.
  */
public class MyQSipProxy extends QSipProxy
{   

   /** Costructs a new MyQSipProxy. */
   public MyQSipProxy(SipProvider provider, ServerProfile server_profile, String qsip_config_file)
   {  super(provider,server_profile,qsip_config_file);
      //qos_provider=new KomRsvpProvider();
   }


   // ********************************** MAIN *********************************

   public static void main(String[] args)
   {         
      String file=null, qfile=null;
      
      for (int i=0; i<args.length; i++)
      {  if (args[i].equals("-f") && args.length>(i+1))
         {  file=args[++i];
            continue;
         }
         if (args[i].equals("-q") && args.length>(i+1))
         {  qfile=args[++i];
            continue;
         }
         if (args[i].equals("-h"))
         {  System.out.println("usage:\n   java KomQSipProxy [-f <config_file>] [-q <qsip_config_file>]\n");
            System.exit(0);
         }
      }
                  
      SipStack.init(file);
      SipProvider sip_provider=new SipProvider(file);
      ServerProfile server_profile=new ServerProfile(file);

      if (qfile==null) qfile=file;
      new MyQSipProxy(sip_provider,server_profile,qfile);      
   }

}