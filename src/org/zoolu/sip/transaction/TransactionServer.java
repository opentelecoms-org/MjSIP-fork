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


import org.zoolu.tools.Timer;
import org.zoolu.tools.TimerListener;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.provider.*;
import org.zoolu.sip.message.*;
import org.zoolu.tools.LogLevel;


/** Generic server transaction as defined in RFC 3261 (Section 17.2.2).
  * A TransactionServer is responsable to create a new SIP transaction that starts with a request message received by the SipProvider and ends sending a final response.<BR>
  * The changes of the internal status and the received messages are fired to the TransactionListener passed to the TransactionServer object.<BR>
  * When costructing a new TransactionServer, the transaction type is passed as String parameter to the costructor (e.g. "CANCEL", "BYE", etc..)
  */
 
public class TransactionServer extends Transaction
{  
   /** the TransactionServerListener that captures the events fired by the TransactionServer */
   TransactionServerListener transaction_listener=null;

   /** last response message */
   Message response=null;
   
   /** clearing timeout ("Timer J" in RFC 3261) */
   Timer clearing_to;


   /** Costructs a new TransactionServer. */
   protected TransactionServer(SipProvider provider)
   {  super(provider);
   } 
   
   /** Creates a new TransactionServer of type <i>method_type</i>, and listens for a request */
   public TransactionServer(SipProvider provider, String method_type, TransactionServerListener tr_listener)
   {  super(provider);
      transaction_id=new TransactionIdentifier(method_type);
      init(tr_listener);
      printLog("created",LogLevel.HIGH);
   }  

   /** Creates a new TransactionServer for the request message <i>request</i> */
   public TransactionServer(SipProvider provider, Message request, TransactionServerListener tr_listener)
   {  super(provider);
      method=new Message(request);
      connection_id=request.getConnectionId();
      transaction_id=method.getTransactionId();
      init(tr_listener);
      printLog("created",LogLevel.HIGH);
      
      // CHANGE-050619: moved starting stuff here
      printLog("start",LogLevel.LOW);
      changeStatus(STATE_TRYING);
      sip_provider.addSipProviderListener(transaction_id,this); 
      //if (transaction_listener!=null) transaction_listener.onSrvRequest(this,method);  
   }  

   /** Initializes timeouts and listener. */
   void init(TransactionServerListener tr_listener)
   {  transaction_listener=tr_listener;
      clearing_to=new Timer(SipStack.transaction_timeout,"Clearing",this);
   }  

   /** Starts the TransactionServer. */
   public void listen()
   {  // CHANGE-050619: moved starting stuff to the TransactionServer costructor..
      //if (method==null)
      if (statusIs(STATE_IDLE))
      {  printLog("start",LogLevel.LOW);
         changeStatus(STATE_WAITING);  
         sip_provider.addSipProviderListener(transaction_id,this); 
      }
      // CHANGE-050619: moved starting stuff to the TransactionServer costructor..
      //else
      //{  changeStatus(STATE_TRYING);
      //   sip_provider.addSipProviderListener(transaction_id,this); 
      //   if (transaction_listener!=null) transaction_listener.onSrvRequest(this,method);  
      //}
   }  

   /** Sends a response message */
   public void respondWith(Message resp)
   {  response=resp;
      if (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING))
      {  sip_provider.sendMessage(response,connection_id);
         int code=response.getStatusLine().getCode();
         if (code>=100 && code<200 && statusIs(STATE_TRYING))
         {  changeStatus(STATE_PROCEEDING);
            //if (transaction_listener!=null) transaction_listener.onSrvProceeding(this);
         }
         if (code>=200 && code<700)
         {  changeStatus(STATE_COMPLETED);
            //if (transaction_listener!=null) transaction_listener.onSrvCompleted(this);
            
            // (CHANGE-040905) clearing_to=0 for reliable transport 
            if (connection_id==null) clearing_to.start();
            else
            {  printLog("clearing_to=0 for reliable transport",LogLevel.LOW);
               onTimeout(clearing_to);
            }
         }
      }
   }  

   /** Method derived from interface SipListener.
     * It's fired from the SipProvider when a new message is received for to the present TransactionServer. */
   public void onReceivedMessage(SipProvider provider, Message msg)
   {  if (msg.isRequest())
      {  if (statusIs(STATE_WAITING))
         {  method=new Message(msg);
            connection_id=msg.getConnectionId();
            transaction_id=method.getTransactionId();
            sip_provider.removeSipProviderListener(new TransactionIdentifier(method.getTransactionMethod()));
            sip_provider.addSipProviderListener(transaction_id,this); 
            changeStatus(STATE_TRYING);
            if (transaction_listener!=null) transaction_listener.onSrvRequest(this,msg);
            return;
         }
         if (statusIs(STATE_PROCEEDING) || statusIs(STATE_COMPLETED))
         {  // retransmission of the last response
            printLog("response retransmission",LogLevel.LOW);
            sip_provider.sendMessage(response,connection_id);
            return;
         }
      }
   }

   /** Method derived from interface TimerListener.
     * It's fired from an active Timer. */
   public void onTimeout(Timer to)
   {  if (to.equals(clearing_to))
      {  printLog("Clearing timeout expired",LogLevel.HIGH);
         if (statusIs(STATE_WAITING)) sip_provider.removeSipProviderListener(new TransactionIdentifier(method.getTransactionMethod()));
         else sip_provider.removeSipProviderListener(transaction_id);
         changeStatus(STATE_TERMINATED);
         //if (transaction_listener!=null) transaction_listener.onSrvClearingTimeout(this);
         // (CHANGE-040421) now it can free links to transaction_listener and timers
         transaction_listener=null;
         //clearing_to=null;
      }  
   }   

   /** Terminates the transaction. */
   public void terminate()
   {  if (!statusIs(STATE_TERMINATED))
      {  clearing_to.halt();
         if (statusIs(STATE_WAITING)) sip_provider.removeSipProviderListener(new TransactionIdentifier(method.getTransactionMethod()));
         else sip_provider.removeSipProviderListener(transaction_id);
         changeStatus(STATE_TERMINATED);
         //if (transaction_listener!=null) transaction_listener.onSrvTerminated(this);
         // (CHANGE-040421) now it can free links to transaction_listener and timers
         transaction_listener=null;
         //clearing_to=null;
      }
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
   {  super.printLog("Server: "+str,level);
   }
}