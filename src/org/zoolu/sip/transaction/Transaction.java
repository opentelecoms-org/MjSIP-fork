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
import org.zoolu.sip.header.*;
import org.zoolu.sip.message.*;
import org.zoolu.tools.Timer;
import org.zoolu.tools.TimerListener;
import org.zoolu.tools.Log;
import org.zoolu.tools.LogLevel;


/** Abstract class Transaction is hinerited by classes
 *  ClientTransaction, ServerTransaction, InviteClientTransaction and InviteServerTransaction.
 *  An Object Transaction is responsable to handle a new SIP transaction.<BR>
 *  The changes of the internal status and the received messages are fired to the
 *  TransactionListener passed to the Transaction Objects.<BR>
 */
public abstract class Transaction implements SipProviderListener, TimerListener
{ 
   /** Event logger. */
   Log log=null;
 
   /** Transactions sequence number */
   protected static int transaction_counter=0;

   /** This transaction number */
   protected int t_number;

   // all transaction states:
   /** State Waiting, used only by server transactions */
   //static final String STATE_IDLE="T_Idle"; 
   /** State Waiting, used only by server transactions */
   //static final String STATE_WAITING="T_Waiting"; 
   /** State Trying */
   //static final String STATE_TRYING="T_Trying"; 
   /** State Proceeding */
   //static final String STATE_PROCEEDING="T_Proceeding";
   /** State Completed */
   //static final String STATE_COMPLETED="T_Completed";
   /** State Confirmed, used only by invite server transactions */
   //static final String STATE_CONFIRMED="T_Confirmed";
   /** State Waiting */
   //static final String STATE_TERMINATED="T_Terminated"; 
  
   // all transaction states:
   /** State Waiting, used only by server transactions */
   static final int STATE_IDLE=0; 
   /** State Waiting, used only by server transactions */
   static final int STATE_WAITING=1; 
   /** State Trying */
   static final int STATE_TRYING=2; 
   /** State Proceeding */
   static final int STATE_PROCEEDING=3;
   /** State Completed */
   static final int STATE_COMPLETED=4;
   /** State Confirmed, used only by invite server transactions */
   static final int STATE_CONFIRMED=5;
   /** State Waiting */
   static final int STATE_TERMINATED=7; 
  
   /** Gets the transaction state. */
   static String getStatus(int st)
   {  switch (st)
      {  case STATE_IDLE       : return "T_Idle";
         case STATE_WAITING    : return "T_Waiting"; 
         case STATE_TRYING     : return "T_Trying"; 
         case STATE_PROCEEDING : return "T_Proceeding";
         case STATE_COMPLETED  : return "T_Completed";
         case STATE_CONFIRMED  : return "T_Confirmed";
         case STATE_TERMINATED : return "T_Terminated";
         default : return null;
      }
   }

   /** Lower layer dispatcher that sends and receive messages.
     * The messages received by the SipProvider are fired to the Transaction
     * by means of the onReceivedMessage() method. */
   SipProvider sip_provider=null;
 
   /** internal state-machine status */
   int status=STATE_IDLE;

   /** transaction method */
   Message method=null;
   
   /** the Transaction ID */
   TransactionIdentifier transaction_id=null;

   /** Transaction connection id */
   ConnectionIdentifier connection_id=null;

   /** Costructs a new Transaction */
   protected Transaction(SipProvider provider)
   {  sip_provider=provider;
      log=sip_provider.getLog();
   }

   /** Changes the internal status */
   void changeStatus(int newstatus)
   {  status=newstatus;
      //transaction_listener.onChangedTransactionStatus(status);
      printLog("changed transaction state: "+getStatus(),LogLevel.MEDIUM);
   }
   
   /** Whether the internal status is equal to <i>st</i> */
   boolean statusIs(int st)
   {  return status==st;
   }      

   /** Gets the current transaction state. */
   String getStatus()
   {  return getStatus(status);
   }

   /** Gets the SipProvider of this Transaction. */
   public SipProvider getSipProvider()
   {  return sip_provider;
   }

   /** Gets the Transaction request message */
   public Message getMethodMessage()
   {  return method;
   }

   /** Gets the Transaction method */
   public String getTransactionMethod()
   {  return method.getTransactionMethod();
   }

   /** Gets the transaction-ID */
   public TransactionIdentifier getTransactionId()
   {  return transaction_id;
   }

   /** Gets the transaction connection id */
   public ConnectionIdentifier getConnectionId()
   {  return connection_id;
   }

   /** Method derived from interface SipListener.
     * It's fired from the SipProvider when a new message is catch for to the present ServerTransaction.
     */
   public void onReceivedMessage(SipProvider provider, Message msg)
   {  //do nothing
   }
   
   /** Method derived from interface TimerListener.
     * It's fired from an active Timer.
     */
   public void onTimeout(Timer to)
   {  //do nothing
   }

   /** Starts the transaction client and sends the transaction request. It it should be extended by TransactioClients. */
   /*public void request()
   {  //do nothing 
      System.err.println("DEBUG: request() method called for a TransactionServer.");
   }*/

   /** Starts the transaction server. It it should be extended by TransactioServerss. */
   /*public void listen()
   {  //do nothing 
      System.err.println("DEBUG: listen() method called for a TransactionClient.");
   }*/

   /** Terminates the transaction. */
   public abstract void terminate();
   

   //**************************** Logs ****************************/

   /** Adds a new event to the log file */
   void printLog(String str, int level)
   {  if (log!=null) log.println("Transaction#"+t_number+": id="+transaction_id+", "+str,level+SipStack.LOG_LEVEL_TRANSACTION);  
   }

   /** Adds a WARNING to the default Log */
   final void printWarning(String str, int level)
   {  printLog("WARNING: "+str,level); 
   }

   /** Adds the Exception to the log file */
   final void printException(Exception e, int level)
   {  if (log!=null) log.printException(e,level+SipStack.LOG_LEVEL_TRANSACTION);
   }
}