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

package local.qos;


import local.server.Proxy;
import local.server.ServerProfile;
import org.zoolu.sip.address.*;
import org.zoolu.sip.provider.*;
import org.zoolu.sip.header.*;
import org.zoolu.sip.message.MessageFactory;
import org.zoolu.sdp.*;
import org.zoolu.sip.call.SdpTools;
import org.zoolu.tools.LogLevel;

import java.util.Iterator;
import java.util.Hashtable;
import java.util.Vector;
import java.io.*;


/** QSipProxy is a Proxy Server with Q-SIP funciotnality.
  * See &lt;draft-veltri-sip-qsip&gt; for more details on QSIP extension.
  * <p> Class QSipProxy extends class Proxy (stateless proxy).
  */
public class QSipProxy extends Proxy
{   
   /** The default Q-SIP configuration file */
   static final String default_config_file="qsip.cfg";
   
   /** The qos-proxy parameter name */
   static final String rr_qos_proxy_param="qos-proxy";
   /** The qos-sdp parameter name */
   static final String rr_qos_sdp_param="qos-sdp";

   /** The local QoS-ER */
   String qos_er=null;

   /** The local QoS-Domain */
   String qos_domain=null;
   
   /** The provisional QoS-States list */
   Hashtable provisional_states;

   /** The QoS-States list */
   Hashtable qos_states;

   /** The QoS provider */
   QoSProvider qos_provider;


   /** Costructs a new QSipProxy that acts also as location server for registered users. */
   public QSipProxy(SipProvider provider, ServerProfile server_profile, String qsip_config_file)
   {  super(provider,server_profile);
      // Disable the RecordRoute option of the SipStack
      server_profile.on_route=false;
      if (qsip_config_file==null) qsip_config_file=default_config_file;
      load(qsip_config_file);
      provisional_states=new Hashtable();
      qos_states=new Hashtable();
      qos_provider=new QoSProviderAdapter();
   }
   
   
   /** When a new request message is received for a local user */
   public void processRequestToLocalUser(org.zoolu.sip.message.Message msg)
   {  printlog("inside processRequestToLocalUser(msg)",LogLevel.MEDIUM);
      if (location_service==null) return;
      msg=processQosRequest(new Message(msg));
      super.processRequestToLocalUser(msg);                           
   }

   
   /** When a new request message is received for a remote UA */
   public void processRequestToRemoteUA(org.zoolu.sip.message.Message msg)
   {  printlog("inside processRequestToRemoteUA(msg)",LogLevel.MEDIUM);
      msg=processQosRequest(new Message(msg));
      super.processRequestToRemoteUA(msg);                           
   }


   /** When a new response message is received */
   public void processResponse(org.zoolu.sip.message.Message resp)
   {  printlog("inside processResponse(msg)",LogLevel.MEDIUM);
      resp=processQosResponse(new Message(resp));
      super.processResponse(resp);                             
   }

   
   // **************************** private methods ****************************

   /** When a new request message is received */
   private Message processQosRequest(Message msg)
   {  printlog("inside processQosRequest(msg)",LogLevel.MEDIUM);
      if (msg.isInvite())
      {  
         SessionDescriptor sdp=new SessionDescriptor(msg.getBody());
         
         QoSProxyHeader qh;
         if (!msg.hasQoSProxyHeader())
         {  // the QoSProxyHeader is set by the first QSIP server, so this is the caller QSIP        

            // set the QoSProxyHeader
            qh=new QoSProxyHeader(new NameAddress(new SipURL(sip_provider.getViaAddress(),sip_provider.getPort())));
            qh.setER(qos_er);
            qh.setRealm(qos_domain);
            //qh.setDirection("unidirectional");                           
            //qh.setMode("enabled");
            msg.setQoSProxyHeader(qh);              
                                   
            // PRE-RESERVATION (caller-side) flow(s):callee-->caller

            // any-->callee QoSDescriptor
            QoSDescriptor qos_descriptor=createQoSDescriptor(null,null,sdp,qos_er);                                 
            printlog("QoS PRE-SETUP: "+msg.getCallIdHeader().getCallId()+"\r\n"+qos_descriptor.toString(),LogLevel.HIGH);
            qos_provider.qosPreSetup(qos_descriptor,msg.getCallIdHeader().getCallId());
         }
         else
         {  qh=msg.getQoSProxyHeader();
         }
         
         // add RecordRouteHeader
         RecordRouteHeader rrh=createRecordRouteHeader(qh,sdp);
         msg.addRecordRouteHeader(rrh);
      }
      else
      if (msg.isBye())
      {  // TEARDOWN
         printlog("QoS TEARDOWN: "+msg.getCallIdHeader().getCallId()+"\r\n",LogLevel.HIGH);
         qos_provider.qosTeardown(msg.getCallIdHeader().getCallId());
      }
      return msg;      
   }


   /** When a new response message is received */
   private Message processQosResponse(Message resp)
   {  printlog("inside processQosResponse(msg)",LogLevel.MEDIUM);
      int code=resp.getStatusLine().getCode();

      if (code>=200 && code<300 && resp.hasRecordRouteHeader())
      {  printlog("processQosResponse(2xx): Record-Route header found",LogLevel.LOW);
         
         /*
         Vector rvector=resp.getRecordRoutes().getHeaders();
         RecordRouteHeader rr=null;
         for (int i=0; i<rvector.size(); i++)
         {  RecordRouteHeader rr_i=new RecordRouteHeader((Header)rvector.elementAt(i));
            if (rr_i.getNameAddress().getAddress().getHost().equals(sip_provider.getAddress()))
            {  rr=rr_i;
               break;
            }
         }*/
         RecordRouteHeader rr=null;
         Vector rvector=resp.getRecordRoutes().getHeaders();
         if (rvector!=null && rvector.size()>0) rr=new RecordRouteHeader((Header)rvector.elementAt(0));    

        if( rr!=null && rr.hasParameter(rr_qos_proxy_param))
         {
            printlog("processQosResponse(2xx): Record-Route 'qos-proxy' parameter found",LogLevel.LOW);
         
            if (!resp.hasQoSProxyHeader())
            {  printlog("processQosResponse(2xx): this is the callee QSIP",LogLevel.MEDIUM);
               // this is the callee QSIP

               // set the QoSProxyHeader
               String callee_er=qos_er;
               QoSProxyHeader qh=new QoSProxyHeader(new NameAddress(new SipURL(sip_provider.getViaAddress(),sip_provider.getPort())));
               qh.setER(callee_er);
               qh.setRealm(qos_domain);
               //qh.setDirection("unidirectional");                           
               //qh.setMode("enabled");
               resp.setQoSProxyHeader(qh);              

               // PRE-RESERVATION (callee-side) flow(s):caller-->callee

               SessionDescriptor callee_sdp=new SessionDescriptor(resp.getBody());
               // any-->callee QoSDescriptor
               QoSDescriptor qos_descriptor=createQoSDescriptor(null,null,callee_sdp,callee_er);       
               printlog("QoS PRE-SETUP: "+resp.getCallIdHeader().getCallId()+"\r\n"+qos_descriptor.toString(),LogLevel.HIGH);
               qos_provider.qosPreSetup(qos_descriptor,resp.getCallIdHeader().getCallId());

               // ADMISSION CONTROL & RESERVATION (callee-side) flow(s):callee-->caller              

               QoSProxyHeader caller_qh=new QoSProxyHeader(undoStuffing(rr.getParameter(rr_qos_proxy_param)));
               SessionDescriptor caller_sdp=new SessionDescriptor(undoStuffing(rr.getParameter(rr_qos_sdp_param)));               
               caller_sdp=SdpTools.sdpMediaProduct(caller_sdp,callee_sdp.getMediaDescriptors());
               String caller_er=caller_qh.getER();
               // callee-->caller QoSDescriptor
               qos_descriptor=createQoSDescriptor(callee_sdp.getConnection().getAddress(),callee_er,caller_sdp,caller_er);                         
               //printlog("DEBUG: qos\r\n"+qos_descriptor.toString(),LogLevel.HIGH);
                  
               // admission control
               qos_descriptor=qos_provider.qosAdmissionControl(qos_descriptor);
               
               // reservation
               if(qos_descriptor!=null)
               {  printlog("QoS SETUP: "+resp.getCallIdHeader().getCallId()+"\r\n"+qos_descriptor.toString(),LogLevel.HIGH);
                  qos_provider.qosSetup(qos_descriptor,resp.getCallIdHeader().getCallId());
               }
               else printlog("QoS Reservation: Admission Control failed.",LogLevel.HIGH);
            }
            else
            {  // this MAY be the caller QSIP
               QoSProxyHeader caller_qh=new QoSProxyHeader(new Header(SipHeaders.QoSProxy,undoStuffing(rr.getParameter(rr_qos_proxy_param))));
               SipURL proxy_url=caller_qh.getNameAddress().getAddress();
               if (!sip_provider.getViaAddress().equals(proxy_url.getHost()) || sip_provider.getPort()!=proxy_url.getPort())
               {  // this is not the callee QSIP neither the caller QSIP
                  printlog("processQosResponse(2xx): this is NOT the caller or callee QSIP",LogLevel.MEDIUM);
                  return resp;
               }
               // else          
               // this is the caller QSIP
               printlog("processQosResponse(2xx): this is the caller QSIP",LogLevel.MEDIUM);
               
               // ADMISSION CONTROL & RESERVATION (caller-side) flow(s):caller-->callee

               // caller-->callee QoSDescriptor
               SessionDescriptor caller_sdp=new SessionDescriptor(undoStuffing(rr.getParameter(rr_qos_sdp_param)));
               String caller_er=caller_qh.getER();
               SessionDescriptor callee_sdp=new SessionDescriptor(resp.getBody());
               QoSProxyHeader callee_qh=resp.getQoSProxyHeader();
               String callee_er=callee_qh.getER();
               
               QoSDescriptor qos_descriptor=createQoSDescriptor(caller_sdp.getConnection().getAddress(),caller_er,callee_sdp,callee_er)               ;                         

               // admission control
               qos_descriptor=qos_provider.qosAdmissionControl(qos_descriptor);

               // reservation
               if(qos_descriptor!=null)
               {  printlog("QoS SETUP:  "+resp.getCallIdHeader().getCallId()+"\r\n"+qos_descriptor.toString(),LogLevel.HIGH);
                  qos_provider.qosSetup(qos_descriptor,resp.getCallIdHeader().getCallId());
               }
               else printlog("QoS Reservation: Admission Control failed.",LogLevel.HIGH);
            }
         }
      }
      return resp;
   }


   /** Creates the QoSDescriptor from the sdp */
   private QoSDescriptor createQoSDescriptor(String src_addr, String src_er, SessionDescriptor dest_sdp, String dest_er)
   {  // create a QoSDescriptor
      QoSDescriptor qos_descriptor=new QoSDescriptor(dest_sdp);    
      //printlog("DEBUG: SDP: "+sdp.toString(),LogLevel.HIGH);
      //printlog("DEBUG: QOS_DESCR: "+qos_descriptor.toString(),LogLevel.HIGH);
      // add the edge roter and domain name 
      Vector qflows=qos_descriptor.getFlows();
      for (int i=0; i<qflows.size(); i++)
      {  FlowSpec flow=(FlowSpec)qflows.elementAt(i);
         if (src_addr!=null || src_er!=null) flow.src=new EndPoint(src_addr,null,0,src_er);
         flow.dest.er=dest_er;
      }
      return qos_descriptor;
   }

   
   /** Creates the RecordRouteHeader based on QoSProxyHeader and SDP */
   private RecordRouteHeader createRecordRouteHeader(QoSProxyHeader qh, SessionDescriptor sdp)
   {  String proxy_param=null;
      String sdp_param=null;
      if (qh!=null) proxy_param=doStuffing(qh.getValue());
      if (sdp!=null) sdp_param=doStuffing(compactSdp(sdp));
      SipURL rr_url;
      if (sip_provider.getPort()==SipStack.default_port) rr_url=new SipURL(sip_provider.getViaAddress());
      else rr_url=new SipURL(sip_provider.getViaAddress(),sip_provider.getPort());
      if (server_profile.loose_route) rr_url.addParameter("lr",null);
      RecordRouteHeader rrh=new RecordRouteHeader(new NameAddress(rr_url));
      rrh.setParameter(rr_qos_proxy_param,proxy_param);
      rrh.setParameter(rr_qos_sdp_param,sdp_param);
      return rrh;
   }
   
   
   /** Extracts qos-related data from SDP */
   static private String compactSdp(SessionDescriptor sdp)
   {  Vector media=sdp.getMediaDescriptors();
      //Vector new_medias=new Vector();
      String compact_sdp=sdp.getConnection().toString();
      for (int i=0; i<media.size(); i++)
      {  MediaDescriptor m=(MediaDescriptor)media.elementAt(i);
         compact_sdp+=(new MediaDescriptor(m.getMedia(),null)).toString();
      }
      //printlog("DEBUG: media:\r\n"+compact_sdp,LogLevel.HIGH);
      return compact_sdp;
   }
   

   /** Stuffs a SIP/SDP field or a generic string. */
   static String doStuffing(String str)
   {  for (int i=0; i<str.length(); i++)
      {  if (str.charAt(i)=='=')
         {  str=str.substring(0,i)+"%3D"+str.substring(i+1);
            i+=2;
         }
         else
         if (str.charAt(i)==';')
         {  str=str.substring(0,i)+"%3B"+str.substring(i+1);
            i+=2;
         }
         else
         if (str.charAt(i)==' ')
         {  str=str.substring(0,i)+"%20"+str.substring(i+1);
            i+=2;
         }
         else
         if (str.charAt(i)=='"')
         {  str=str.substring(0,i)+"%22"+str.substring(i+1);
            i+=2;
         }
         else
         if (str.charAt(i)==',')
         {  str=str.substring(0,i)+"%2C"+str.substring(i+1);
            i+=2;
         }
         else
         if (str.charAt(i)=='\r')
         {  str=str.substring(0,i)+"%0D"+str.substring(i+1);
            i--;
         }
         else
         if (str.charAt(i)=='\n')
         {  str=str.substring(0,i)+"%0A"+str.substring(i+1);
            i--;
         }
      }
      return str;
   }     

   /** Undo stuffing of a SIP/SDP field or a generic string. */
   static String undoStuffing(String str)
   {  for (int i=0; i<str.length(); i++)
      {  if (str.startsWith("%3D",i))
         {  str=str.substring(0,i)+"="+str.substring(i+3);
            i-=2;
         }
         else
         if (str.startsWith("%3B",i))
         {  str=str.substring(0,i)+";"+str.substring(i+3);
            i-=2;
         }
         else
         if (str.startsWith("%20",i))
         {  str=str.substring(0,i)+" "+str.substring(i+3);
            i-=2;
         }
         else
         if (str.startsWith("%22",i))
         {  str=str.substring(0,i)+"\""+str.substring(i+3);
            i-=2;
         }
         if (str.startsWith("%2C",i))
         {  str=str.substring(0,i)+","+str.substring(i+3);
            i-=2;
         }
         if (str.startsWith("%0D",i))
         {  str=str.substring(0,i)+"\r"+str.substring(i+3);
            i-=2;
         }
         if (str.startsWith("%0A",i))
         {  str=str.substring(0,i)+"\n"+str.substring(i+3);
            i-=2;
         }
      }
      return str;
   }     

   /** loads the Q-SIP settings */
   private void load(String file)
   {  
      BufferedReader in=null;
      try { in = new BufferedReader(new FileReader(file)); }
         catch (FileNotFoundException e)
         {  System.err.println("ERROR: configuration file \""+file+"\" not found.");
            System.exit(0);
         }         
      while (true)
      {  String line=null;
         try { line=in.readLine(); } catch (Exception e) { e.printStackTrace(); System.exit(0); }
         if (line==null) break;    
         SipParser par=new SipParser(line);     
         if (line.startsWith("#")) continue;
         if (line.startsWith("qos_er"))         { qos_er=par.goTo('=').skipChar().getString(); continue; }
         if (line.startsWith("qos_domain"))     { qos_domain=par.goTo('=').skipChar().getString(); continue; }
         //if (line.startsWith("retransmission_timeout")) { retransmission_timeout=par.goTo('=').skipChar().getInt(); continue; }
      }      
      printlog("Q-SIP settings loaded",LogLevel.MEDIUM);
   }

   
   // ****************************** Logs *****************************

   /** Adds a new string to the default Log */
   private void printlog(String str, int level)
   {  if (log!=null) log.println("QSipProxy: "+str,level+SipStack.LOG_LEVEL_UA);  
   }
 
 
   // ********************************** MAIN *********************************

   /** The main method. */
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
         {  System.out.println("usage:\n   java QSipProxy [-f <config_file>] [-q <qsip_config_file>]\n");
            System.exit(0);
         }
      }
                  
      SipStack.init(file);
      SipProvider sip_provider=new SipProvider(file);
      ServerProfile server_profile=new ServerProfile(file);
      
      if (qfile==null) qfile=file;
      new QSipProxy(sip_provider,server_profile,qfile);      
   }
  
}

