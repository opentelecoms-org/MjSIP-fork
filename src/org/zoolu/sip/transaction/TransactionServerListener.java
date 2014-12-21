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


/** A TransactionServerListener listens for TransactionServer events.
  * It collects all TransactionServer callback functions.
  */
public interface TransactionServerListener
{  
   /** When the TransactionServer goes into the "Trying" state receiving a request */
   public void onSrvRequest(TransactionServer transaction, Message req);  
   
   /** When the TransactionServer goes into the "Proceeding" state, sending a provisional response  */
   //public void onSrvProceeding(TransactionServer transaction);
   
   /** When the TransactionServer goes into the "Completed" state, sending a final response */
   //public void onSrvCompleted(TransactionServer transaction);
   
   /** When the TransactionServer goes into the "Terminated" state, caused by clearing timeout after sent final response */
   //public void onSrvClearingTimeout(TransactionServer transaction);

   /** When the TransactionServer goes into the "Terminated" state, forced by the transaction user */
   //public void onSrvTerminated(TransactionServer transaction);


   /************************ InviteTransactionServer listeners ************************

   /** When the InviteTransactionServer goes into the "Trying" state receiving a request */
   //public void onInvSrvInvite(TransactionServer transaction, Message req);  
   
   /** When the InviteTransactionServer goes into the "Proceeding" state, sending a provisional response  */
   //public void onInvSrvProceeding(TransactionServer transaction);
   
   /** When the InviteTransactionServer goes into the "Completed" state, sending a failure response */
   //public void onInvSrvFailureCompleted(TransactionServer transaction);
   
   /** When an InviteTransactionServer goes into the "Confirmed" state receining an ACK for NON-2xx response */
   //public void onInvSrvFailureAck(TransactionServer transaction, Message ack);

   /** When the InviteTransactionServer goes into the "Terminated" state, sending a success response */
   //public void onInvSrvSuccessTerminated(TransactionServer transaction);

   /** When the InviteTransactionServer goes into the "Terminated" state, caused by end timeout after sent final response */
   //public void onInvSrvEndTimeout(TransactionServer transaction);

   /** When the InviteTransactionServer goes into the "Terminated" state, caused by clearing timeout after sent final response */
   //public void onInvSrvClearingTimeout(TransactionServer transaction);

   /** When the InviteTransactionServer goes into the "Terminated" state, forced by the transaction user */
   //public void onInvSrvTerminated(TransactionServer transaction);


   /************************* AckTransactionServer listeners *************************

   /** When the AckTransactionServer goes into the "Terminated" state, caused by transaction timeout */
   //public void onAckSrvTimeout(AckTransactionServer transaction);

   /** When the AckTransactionServer goes into the "Terminated" state, forced by the transaction user */
   //public void onAckSrvTerminated(AckTransactionServer transaction);

}
