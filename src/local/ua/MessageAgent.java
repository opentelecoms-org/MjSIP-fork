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

package local.ua;


import org.zoolu.sip.address.*;
import org.zoolu.sip.provider.*;
import org.zoolu.sip.transaction.*;
import org.zoolu.sip.header.StatusLine;
import org.zoolu.sip.message.*;
import org.zoolu.tools.Log;
import org.zoolu.tools.LogLevel;

import java.io.*;


/** Simple Message Agent.
  * It allows a user to send and receive short messages.
  */
public class MessageAgent implements SipProviderListener, TransactionClientListener
{     
   /** Event logger. */
   protected Log log=null;

   /** UserProfile */
   protected UserAgentProfile user_profile;

   /** SipProvider */
   protected SipProvider sip_provider;

   /** UserProfile */
   protected MessageAgentListener listener;

   
   /** Costructs a new MessageAgent. */
   public MessageAgent(SipProvider sip_provider, UserAgentProfile user_profile, MessageAgentListener listener)
   {  this.sip_provider=sip_provider;
      this.log=sip_provider.getLog();
      this.user_profile=user_profile;
      this.listener=listener;
      // if no contact_url and/or from_url has been set, create it now
      if (user_profile.contact_url==null)
      {  user_profile.contact_url="sip:"+user_profile.contact_user+"@"+sip_provider.getViaAddress();
         if (sip_provider.getPort()!=SipStack.default_port) user_profile.contact_url+=":"+sip_provider.getPort();
         if (!sip_provider.getDefaultTransport().equals(SipProvider.PROTO_UDP)) user_profile.contact_url+=";transport="+sip_provider.getDefaultTransport();
      }
      if (user_profile.from_url==null)
         user_profile.from_url=user_profile.contact_url;
   }   

   
   /** Sends a new text message. */
   public void send(String recipient, String subject, String content)
   {  send(recipient,subject,"application/text",content);
   }   


   /** Sends a new message. */
   public void send(String recipient, String subject, String content_type, String content)
   {  NameAddress to_url=new NameAddress(recipient);
      NameAddress from_url=new NameAddress(user_profile.from_url);
      Message req=MessageFactory.createMessageRequest(sip_provider,to_url,from_url,subject,content_type,content);
      TransactionClient t=new TransactionClient(sip_provider,req,this);
      t.request();
   }


   /** Waits for incoming message. */
   public void receive()
   {  sip_provider.addSipProviderListener(new MethodIdentifier(SipMethods.MESSAGE),this);  
   } 
   

   /** Stops receiving messages. */
   public void halt()
   {  sip_provider.removeSipProviderListener(new MethodIdentifier(SipMethods.MESSAGE));  
   } 


   // ******************* Callback functions implementation ********************

   /** Inherited from class SipProviderListener
     * Called when a new message is received (out of any ongoing transaction) */
   public void onReceivedMessage(SipProvider provider, Message msg)
   {  //printLog("Message received: "+msg.getFirstLine().substring(0,msg.toString().indexOf('\r')));
      if (msg.isRequest() && msg.isMessage())
      {  (new TransactionServer(sip_provider,msg,null)).respondWith(MessageFactory.createResponse(msg,200,"OK",null,""));
         NameAddress sender=msg.getFromHeader().getNameAddress();
         String subject=null;
         if (msg.hasSubjectHeader()) subject=msg.getSubjectHeader().getSubject();
         String content_type=msg.getContentTypeHeader().getContentType();
         String content=msg.getBody();
         if (listener!=null) listener.onMaReceivedMessage(this,sender,subject,content_type,content);
      }
   }
 
   /** When the TransactionClient goes into the "Completed" state receiving a 2xx response */
   public void onCltSuccessResponse(TransactionClient tc, Message resp) 
   {  onDeliverySuccess(tc,resp.getStatusLine().getReason());
   }

   /** When the TransactionClient goes into the "Completed" state receiving a 300-699 response */
   public void onCltFailureResponse(TransactionClient tc, Message resp) 
   {  onDeliveryFailure(tc,resp.getStatusLine().getReason());
   }
    
   /** When the TransactionClient is (or goes) in "Proceeding" state and receives a new 1xx provisional response */
   public void onCltProvisionalResponse(TransactionClient tc, Message resp)
   {  // do nothing.
   }
      
   /** When the TransactionClient goes into the "Terminated" state, caused by transaction timeout */
   public void onCltTimeout(TransactionClient tc)
   {  onDeliveryFailure(tc,"Timeout");
   }
 
   /** When the delivery successes. */
   private void onDeliverySuccess(TransactionClient tc, String result)
   {  printLog("Message successfully delivered ("+result+").");
      Message req=tc.getMethodMessage();
      NameAddress recipient=req.getToHeader().getNameAddress();
      String subject=null;
      if (req.hasSubjectHeader()) subject=req.getSubjectHeader().getSubject();
      if (listener!=null) listener.onMaDeliverySuccess(this,recipient,subject,result);
   }

   /** When the delivery fails. */
   private void onDeliveryFailure(TransactionClient tc, String result)
   {  printLog("Message delivery failed ("+result+").");
      Message req=tc.getMethodMessage();
      NameAddress recipient=req.getToHeader().getNameAddress();
      String subject=null;
      if (req.hasSubjectHeader()) subject=req.getSubjectHeader().getSubject();
      if (listener!=null) listener.onMaDeliveryFailure(this,recipient,subject,result);
   }

   //**************************** Logs ****************************/

   /** Starting log level for this class */
   //private static final int LOG_OFFSET=SipStack.LOG_LEVEL_UA;

   /** Adds a new string to the default Log */
   private void printLog(String str)
   {  printLog(str,LogLevel.HIGH);
   }

   /** Adds a new string to the default Log */
   private void printLog(String str, int level)
   {  if (log!=null) log.println("MessageAgent: "+str,level+SipStack.LOG_LEVEL_UA);
      //System.out.println("MA: "+str);  
   }
}