/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
 * 
 * This file is part of MjSip (http://www.mjsip.org)
 * 
 * MjSip is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * MjSip is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MjSip; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.zoolu.sip.transaction;


import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.provider.*;
import org.zoolu.sip.message.*;
import org.zoolu.tools.Timer;
import org.zoolu.tools.TimerListener;
import org.zoolu.tools.LogLevel;


/** INVITE server transaction as defined in RFC 3261 (Section 17.2.1).
  * <BR> An InviteTransactionServer is responsable to create a new SIP invite
  * transaction that starts with a INVITE message received by the SipProvider
  * and ends sending a final response.
  * <BR> The changes of the internal status and the received messages are fired
  * to the TransactionListener passed to the InviteTransactionServer object.
  * <BR> This implementation of InviteTransactionServer automatically generates
  * a "100 Trying" response when the INVITE message is received
  * (as suggested by RFC3261) 
  */
public class InviteTransactionServer extends TransactionServer
{     
   /** the TransactionServerListener that captures the events fired by the InviteTransactionServer */
   InviteTransactionServerListener transaction_listener=null;

   /** last response message */
   //Message response=null;
   
   /** retransmission timeout ("Timer G" in RFC 3261) */
   Timer retransmission_to;
   /** end timeout ("Timer H" in RFC 3261) */
   Timer end_to;
   /** clearing timeout ("Timer I" in RFC 3261) */
   //Timer clearing_to; 

   /** Whether sending automatically 100 trying on INVITE */
   boolean auto_trying=true;
   
   /** Whether sending automatically 100 trying on INVITE.
     * The default value is <b>true</b> */
   public void setAuto100Trying(boolean auto_trying)
   {  this.auto_trying=auto_trying;
   }
   
   /** Creates a new InviteTransactionServer */
   public InviteTransactionServer(SipProvider provider, InviteTransactionServerListener tr_listener)
   {  super(provider);
      transaction_id=new TransactionIdentifier(SipMethods.INVITE);
      init(tr_listener);
      printLog("created",LogLevel.HIGH);
   }  
      
   /** Creates a new InviteTransactionServer with an already incomed INVITE message. */
   public InviteTransactionServer(SipProvider provider, Message invite, boolean auto_trying, InviteTransactionServerListener tr_listener)
   {  super(provider);
      method=new Message(invite);
      connection_id=invite.getConnectionId();
      transaction_id=method.getTransactionId();
      init(tr_listener);
      printLog("created",LogLevel.HIGH);

      // CHANGE-050619: moved starting stuff here
      changeStatus(STATE_TRYING);
      sip_provider.addSipProviderListener(transaction_id,this);
      //if (transaction_listener!=null) transaction_listener.onSrvRequest(this,method);
      // automatically send "100 Tryng" response and go to STATE_PROCEEDING
      if (auto_trying)
      {  Message trying100=MessageFactory.createResponse(method,100,"Trying",null,"");
         respondWith(trying100); // this method makes it going automatically to STATE_PROCEEDING
      }
   }  

   /** Initializes timeouts and listener. */
   void init(InviteTransactionServerListener tr_listener)
   {  transaction_listener=tr_listener;
      retransmission_to=new Timer(SipStack.retransmission_timeout,"Retransmission",this);
      end_to=new Timer(SipStack.transaction_timeout,"End",this);
      clearing_to=new Timer(SipStack.clearing_timeout,"Clearing",this);
   }   

   /** Starts the InviteTransactionServer. */
   public void listen()
   {  printLog("start",LogLevel.LOW);
      // CHANGE-050619: moved starting stuff to the TransactionServer costructor..
      //if (method==null)
      if (statusIs(STATE_IDLE))
      {  changeStatus(STATE_WAITING);  
         sip_provider.addSipProviderListener(new TransactionIdentifier(SipMethods.INVITE),this); 
      }
      // CHANGE-050619: moved starting stuff to the TransactionServer costructor..
      //else
      //{  changeStatus(STATE_TRYING);
      //   sip_provider.addSipProviderListener(transaction_id,this);
      //   if (transaction_listener!=null) transaction_listener.onSrvRequest(this,method);
      //   // automatically send "100 Tryng" response and go to STATE_PROCEEDING
      //   if (auto_trying)
      //   {  Message trying100=MessageFactory.createResponse(method,100,"Trying",null,"");
      //      respondWith(trying100); // this method makes it going automatically to STATE_PROCEEDING
      //   }
      //}
   }  

   /** Sends a response message */
   public void respondWith(Message resp)
   {  response=resp;
      int code=response.getStatusLine().getCode();
      if (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING)) sip_provider.sendMessage(response,connection_id);         
      if (code>=100 && code<200 && statusIs(STATE_TRYING))
      {  changeStatus(STATE_PROCEEDING);
         //if (transaction_listener!=null) transaction_listener.onInvSrvProceeding(this);     
         return;
      }
      if (code>=200 && code<300 && (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING)))
      {  sip_provider.removeSipProviderListener(transaction_id);
         changeStatus(STATE_TERMINATED);
         //if (transaction_listener!=null) transaction_listener.onInvSrvSuccessTerminated(this);
         // (CHANGE-040421) now it can free links to transaction_listener and timers
         transaction_listener=null;
         //retransmission_to=null;
         //end_to=null;
         //clearing_to=null;
         return;
      }
      if (code>=300 && code<700 && (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING)))
      {  changeStatus(STATE_COMPLETED);
         //if (transaction_listener!=null) transaction_listener.onInvSrvFailureCompleted(this);
         // (CHANGE-040905) no retransmission for reliable transport 
         if (connection_id==null)
         {  retransmission_to.start();
            end_to.start();
         }
         else
         {  printLog("No retransmissions for reliable transport ("+connection_id+")",LogLevel.LOW);
            onTimeout(end_to);
         }
      }
   }  

   /** Method derived from interface SipListener.
     * It's fired from the SipProvider when a new message is catch for to the present ServerTransaction. */
   public void onReceivedMessage(SipProvider provider, Message msg)
   {  if (msg.isRequest())
      {  String req_method=msg.getRequestLine().getMethod();
         
         // invite received
         if (req_method.equals(SipMethods.INVITE))
         {
            if (statusIs(STATE_WAITING))
            {  method=new Message(msg);
               connection_id=msg.getConnectionId();
               transaction_id=method.getTransactionId();
               sip_provider.addSipProviderListener(transaction_id,this); 
               sip_provider.removeSipProviderListener(new TransactionIdentifier(SipMethods.INVITE));
               changeStatus(STATE_TRYING);
               // automatically send "100 Tryng" response and go to STATE_PROCEEDING
               if (auto_trying)
               {  Message trying100=MessageFactory.createResponse(method,100,"Trying",null,"");
                  respondWith(trying100); // this method makes it going automatically to STATE_PROCEEDING
               }
               if (transaction_listener!=null) transaction_listener.onSrvRequest(this,msg);
               return;            
            }
            if (statusIs(STATE_PROCEEDING) || statusIs(STATE_COMPLETED))
            {  // retransmission of the last response
               sip_provider.sendMessage(response,connection_id);
               return;
            }
         }
         // ack received
         if (req_method.equals(SipMethods.ACK) && statusIs(STATE_COMPLETED))
         {  retransmission_to.halt();
            end_to.halt();
            changeStatus(STATE_CONFIRMED);
            if (transaction_listener!=null) transaction_listener.onSrvFailureAck(this,msg);
            clearing_to.start();
            return;
         }
         // cancel received
         /*
         if (req_method.equals(SipMethods.CANCEL) && (statusIs(STATE_PROCEEDING) || statusIs(STATE_TRYING)))
         {  // create a CANCEL TransactionServer and send a 200 OK (CANCEL)
           (new TransactionServer(msg,null)).respondWith(MessageFactory.createResponse(msg,200,"OK",null,""));
           // automatically sends a 487 Cancelled
           respondWith(MessageFactory.createResponse(method,487,"Cancelled",null,""));
         }
         */
      }    
   }

   /** Method derived from interface TimerListener.
     * It's fired from an active Timer. */
   public void onTimeout(Timer to)
   {  if (to.equals(retransmission_to) && statusIs(STATE_COMPLETED))
      {  printLog("Retransmission timeout expired",LogLevel.HIGH);
         long timeout=2*retransmission_to.getTime();
         if (timeout>SipStack.max_retransmission_timeout) timeout=SipStack.max_retransmission_timeout;
         retransmission_to=new Timer(timeout,retransmission_to.getLabel(),this);
         retransmission_to.start();
         sip_provider.sendMessage(response,connection_id);
      }
      if (to.equals(end_to) && statusIs(STATE_COMPLETED))
      {  printLog("End timeout expired",LogLevel.HIGH);
         retransmission_to.halt();
         sip_provider.removeSipProviderListener(transaction_id);
         changeStatus(STATE_TERMINATED);
         //if (transaction_listener!=null) transaction_listener.onInvSrvEndTimeout(this);
         // (CHANGE-040421) now it can free links to transaction_listener and timers
         transaction_listener=null;
         //retransmission_to=null;
         //end_to=null;
         //clearing_to=null;
      }  
      if (to.equals(clearing_to) && statusIs(STATE_CONFIRMED))
      {  printLog("Clearing timeout expired",LogLevel.HIGH);
         sip_provider.removeSipProviderListener(transaction_id);
         changeStatus(STATE_TERMINATED);
         //if (transaction_listener!=null) transaction_listener.onInvSrvClearingTimeout(this);
         // (CHANGE-040421) now it can free links to transaction_listener and timers
         transaction_listener=null;
         //retransmission_to=null;
         //end_to=null;
         //clearing_to=null;
      }  
   }   

   /** Method used to drop an active transaction */
   public void terminate()
   {  retransmission_to.halt();
      clearing_to.halt();     
      end_to.halt();
      if (statusIs(STATE_TRYING)) sip_provider.removeSipProviderListener(new TransactionIdentifier(SipMethods.INVITE));
         else sip_provider.removeSipProviderListener(transaction_id);
      changeStatus(STATE_TERMINATED);
      //if (transaction_listener!=null) transaction_listener.onInvSrvTerminated(this);
      // (CHANGE-040421) now it can free links to transaction_listener and timers
      transaction_listener=null;
      //retransmission_to=null;
      //end_to=null;
      //clearing_to=null;
   }
   
   /** Cancels the transaction.
     * This methods should be called by the local CancelServer when a CANCEL request is received for this Transaction. */
   /*public void cancelTransaction()
   {  if (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING))
         respondWith(MessageFactory.createResponse(method,487,"Request Terminated",null,""));
   }*/


   //**************************** Logs ****************************/

   /** Adds a new string to the default Log */
   void printLog(String str, int level)
   {  super.printLog("Invite: "+str,level);
   }
}