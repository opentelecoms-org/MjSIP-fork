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


import org.zoolu.sip.message.Message;


/** A TransactionClientListener listens for TransactionClient events.
  * It collects all TransactionClient callback functions.
  */
public interface TransactionClientListener
{  
   /** When the TransactionClient starts and goes into the "Trying" state */
   //public void onCltTrying(TransactionClient transaction);
   
   /** When the TransactionClient is (or goes) in "Proceeding" state and receives a new 1xx provisional response */
   public void onCltProvisionalResponse(TransactionClient transaction, Message resp);
   
   /** When the TransactionClient goes into the "Completed" state receiving a 200-699 response */
   //public void onCltFinalResponse(TransactionClient transaction, Message resp);  

   /** When the TransactionClient goes into the "Completed" state receiving a 2xx response */
   public void onCltSuccessResponse(TransactionClient transaction, Message resp);

   /** When the TransactionClient goes into the "Completed" state receiving a 300-699 response */
   public void onCltFailureResponse(TransactionClient transaction, Message resp);
   
   /** When the TransactionClient goes into the "Terminated" state, caused by transaction timeout */
   public void onCltTimeout(TransactionClient transaction); 

   /** When the TransactionClient goes into the "Terminated" state, caused by clearing timeout after a final response */
   //public void onCltClearingTimeout(TransactionClient transaction); 

   /** When the TransactionClient goes into the "Terminated" state, forced by the transaction user */
   //public void onCltTerminated(TransactionClient transaction);
   
   /************************ InviteTransactionClient listeners ************************

   /** When the InviteTransactionClient goes into the "Trying" state */
   //public void onInvCltTrying(TransactionClient transaction);
   
   /** When the InviteTransactionClient is in "Proceeding" state and receives a new 1xx response */
   //public void onInvCltProvisionalResponse(TransactionClient transaction, Message resp);
   
   /** When the InviteTransactionClient goes into the "Completed" state, receiving a failure response */
   //public void onInvCltFailureResponse(TransactionClient transaction, Message resp);
           
   /** When an InviteTransactionClient goes into the "Terminated" state, receiving a 2xx response */
   //public void onInvCltSuccessResponse(TransactionClient transaction, Message resp);   

   /** When the InviteTransactionClient goes into the "Terminated" state, caused by transaction timeout */
   //public void onInvCltTimeout(TransactionClient transaction); 

   /** When the InviteTransactionClient goes into the "Terminated" state, caused by end timeout after a failure response */
   //public void onInvCltEndTimeout(TransactionClient transaction); 

   /** When the InviteTransactionClient goes into the "Terminated" state, forced by the transaction user */
   //public void onInvCltTerminated(TransactionClient transaction); 

   /************************* AckTransactionClient listeners *************************

   /** When the AckTransactionClient goes into the "Terminated" state */
   //public void onAckCltTerminated(AckTransactionClient transaction);

}
