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
import org.zoolu.sip.provider.*;
import org.zoolu.sip.header.RequestLine;
import org.zoolu.sip.header.Header;
import org.zoolu.sip.header.ViaHeader;
import org.zoolu.sip.header.MaxForwardsHeader;
import org.zoolu.sip.header.MultipleHeader;
import org.zoolu.sip.header.RouteHeader;
import org.zoolu.sip.header.RecordRouteHeader;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.message.MessageFactory;
import org.zoolu.tools.LogLevel;

//import java.util.Enumeration;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.InputStreamReader;


/** Class Proxy implement a Proxy SIP Server.
  * It extends class Registrar. A Proxy can work as simply SIP Proxy,
  * or it can handle calls for registered users. 
  */
public class Proxy extends Registrar
{   
   /** Log of processed calls */
   CallLogger call_logger;

   /** Costructs a void Proxy */
   protected Proxy() {}

   /** Costructs a new Proxy that acts also as location server for registered users. */
   public Proxy(SipProvider provider, ServerProfile server_profile)
   {  super(provider,server_profile);
      if (server_profile.call_log) call_logger=new CallLoggerImpl(SipStack.log_path+"//"+provider.getViaAddress()+"."+provider.getPort()+"_calls.log");
   }


   /** When a new request message is received for a local user */
   public void processRequestToLocalUser(Message msg)
   {  printLog("inside processRequestToLocalUser(msg)",LogLevel.MEDIUM);

      if (server_profile.call_log) call_logger.update(msg);

      // message targets
      Vector targets=getTargets(msg);
      
      if (targets.isEmpty())
      {  printLog("No target found, message discarded",LogLevel.HIGH);
         if (!msg.isAck()) sip_provider.sendMessage(MessageFactory.createResponse(msg,404,"Not found",null,null));
         return;
      }           
      
      printLog("message will be forwarded to all user's contacts",LogLevel.MEDIUM); 
      for (int i=0; i<targets.size(); i++) 
      {  SipURL url=new SipURL((String)(targets.elementAt(i)));
         Message request=new Message(msg);
         request.removeRequestLine();
         request.setRequestLine(new RequestLine(msg.getRequestLine().getMethod(),url));
         
         updateProxingRequest(request);
         sip_provider.sendMessage(request);
      }
   }

   
   /** When a new request message is received for a remote UA */
   public void processRequestToRemoteUA(Message msg)
   {  printLog("inside processRequestToRemoteUA(msg)",LogLevel.MEDIUM);
   
      if (call_logger!=null) call_logger.update(msg);

      if (!server_profile.is_open_proxy)
      {  SipURL from_url=msg.getFromHeader().getNameAddress().getAddress();
         String username=from_url.getUserName();
         String hostaddr=from_url.getHost();
         String user;
         if (username==null) user=hostaddr; else user=username+"@"+hostaddr;
         if (!location_service.hasUser(user))
         {  printLog("user "+user+" not found: proxy denied.",LogLevel.HIGH);
            sip_provider.sendMessage(MessageFactory.createResponse(msg,503,"Service Unavailable",null,null));
            return;
         }
      }

      updateProxingRequest(msg);      
      sip_provider.sendMessage(msg);
   }

   
   /** Processes the Proxy headers of the request.
     * Such headers are: Via, Record-Route, Route, Max-Forwards, etc. */
   protected Message updateProxingRequest(Message msg)
   {  printLog("inside updateProxingRequest(msg)",LogLevel.LOW);

      // remove Route if present
      boolean is_on_route=false;  
      if (msg.hasRouteHeader())
      {  MultipleHeader mr=msg.getRoutes();
         SipURL route=(new RouteHeader(mr.getTop())).getNameAddress().getAddress();
         if (isLocalDomain(route.getHost(),route.getPort()))
         {  mr.removeTop();
            if (mr.size()>0) msg.setRoutes(mr);
            else msg.removeRoutes();
            is_on_route=true;
         }
      }
      // add Record-Route?
      if (server_profile.on_route && msg.isInvite() && !is_on_route)
      {  SipURL rr_url;
         if (sip_provider.getPort()==SipStack.default_port) rr_url=new SipURL(sip_provider.getViaAddress());
         else rr_url=new SipURL(sip_provider.getViaAddress(),sip_provider.getPort());
         if (server_profile.loose_route) rr_url.addParameter("lr");
         RecordRouteHeader rrh=new RecordRouteHeader(new NameAddress(rr_url));
         msg.addRecordRouteHeader(rrh);
      }
      // which protocol?
      String proto=null;
      if (msg.hasRouteHeader())
      {  SipURL route=msg.getRouteHeader().getNameAddress().getAddress();
         if (route.hasTransport()) proto=route.getTransport();
      }
      else proto=msg.getRequestLine().getAddress().getTransport();
      if (proto==null) proto=sip_provider.getDefaultTransport();
      
      // add Via
      ViaHeader via=new ViaHeader(proto,sip_provider.getViaAddress(),sip_provider.getPort());
      if (sip_provider.isRportSet()) via.setRport();
      via.setBranch(sip_provider.pickBranch(msg));
      msg.addViaHeader(via);

      // decrement Max-Forwards
      MaxForwardsHeader maxfwd=msg.getMaxForwardsHeader();
      if (maxfwd!=null) maxfwd.decrement();
      else maxfwd=new MaxForwardsHeader(SipStack.max_forwards);
      msg.setMaxForwardsHeader(maxfwd);
      
      // check whether the next Route is formed according to RFC2543
      msg.rfc2543RouteAdapt();
              
      return msg;                             
   }
   

   /** When a new response message is received */
   public void processResponse(Message resp)
   {  printLog("inside processResponse(msg)",LogLevel.MEDIUM);
   
      if(call_logger!=null) call_logger.update(resp);

      updateProxingResponse(resp);
      
      if (resp.hasViaHeader()) sip_provider.sendMessage(resp);
      else
         printLog("no VIA header found: message discarded",LogLevel.HIGH);            
   }
   
   
   /** Processes the Proxy headers of the response.
     * Such headers are: Via, .. */
   protected Message updateProxingResponse(Message resp)
   {  printLog("inside updateProxingResponse(resp)",LogLevel.MEDIUM);
      ViaHeader vh=new ViaHeader((Header)resp.getVias().getHeaders().elementAt(0));
      if (vh.getHost().equals(sip_provider.getViaAddress())) resp.removeViaHeader();
      return resp;
   }
   


   // ****************************** Logs *****************************

   /** Adds a new string to the default Log */
   private void printLog(String str, int level)
   {  if (log!=null) log.println("Proxy: "+str,level+SipStack.LOG_LEVEL_UA);  
   }


   // ****************************** MAIN *****************************

   /** The main method. */
   public static void main(String[] args)
   {  
         
      String file=null;
      boolean prompt_exit=false;
      
      for (int i=0; i<args.length; i++)
      {  if (args[i].equals("-f") && args.length>(i+1))
         {  file=args[++i];
            continue;
         }
         if (args[i].equals("--prompt"))
         {  prompt_exit=true;
            continue;
         }
         if (args[i].equals("-h"))
         {  System.out.println("usage:\n   java Proxy [options] \n");
            System.out.println("   options:");
            System.out.println("   -h               this help");
            System.out.println("   -f <config_file> specifies a configuration file");
            System.out.println("   --prompt         prompt for exit");
            System.exit(0);
         }
      }
                  
      SipStack.init(file);
      SipProvider sip_provider=new SipProvider(file);
      ServerProfile server_profile=new ServerProfile(file);

      new Proxy(sip_provider,server_profile);
      
      // promt before exit
      if (prompt_exit) 
      try
      {  System.out.println("press 'enter' to exit");
         BufferedReader in=new BufferedReader(new InputStreamReader(System.in)); 
         in.readLine();
         System.exit(0);
      }
      catch (Exception e) {}
   }
  
}