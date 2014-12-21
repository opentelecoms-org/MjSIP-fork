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

package org.zoolu.sip.dialog;


import org.zoolu.sip.address.*;
import org.zoolu.sip.message.*;
import org.zoolu.sip.header.*;
import org.zoolu.sip.provider.*;
import org.zoolu.tools.Log;
import org.zoolu.tools.LogLevel;
import org.zoolu.tools.AssertException;


import java.util.Vector;


/** Class Dialog maintains a complete information status of a generic SIP dialog.
  * It has the following attributes:
  * <ul>
  * <li>call-id</li>
  * <li>local and remote URLs</li>
  * <li>local and remote contact URLs</li>
  * <li>local and remote CSeqs</li>
  * <li>local and remote tags</li> 
  * <li>dialog-id</li>
  * <li>route set</li>
  * </ul>
  */
public abstract class Dialog
{  
   
   // ************************ Static attributes *************************

   /** Identifier for the transaction client side of a dialog (CLIENT). */
   public final static int T_CLIENT=0;
   /** Identifier for the transaction server side of a dialog (SERVER). */
   public final static int T_SERVER=1;
   

   // *********************** Protected attributes ***********************

   /** Event logger. */
   protected Log log=null;
 
    /** Dialogs sequence number */
   protected static int dialog_counter=0;

   /** This dialog number */
   protected int d_number;

  /** The SipProvider */
   protected SipProvider sip_provider=null;


   // ************************ Private attributes ************************

   /** Internal dialog state. */
   int status;

   /** Local name */
   NameAddress local_name=null;

   /** Remote name */
   NameAddress remote_name=null;

   /** Local contact url */
   NameAddress local_contact=null;

   /** Remote contact url */
   NameAddress remote_contact=null;

   /** call-id */
   String call_id=null;

   /** Local tag */
   String local_tag=null;

   /** Remote tag */
   String remote_tag=null;
   /** Sets the remote tag */

   /** Local CSeq number */
   long local_cseq=-1;

   /** Remote CSeq number */
   long remote_cseq=-1;

   /** Route set (Vector of NameAddresses) */
   Vector route=null; 




   // ************************* Abstract methods *************************

   /** Gets the dialog state */
   abstract public String getStatus();


   // **************************** Costructors *************************** 

   /** Creates a new empty Dialog */
   protected Dialog(SipProvider provider)
   {  sip_provider=provider;
      log=sip_provider.getLog();
      status=0;
      d_number=dialog_counter++;
   }

   /** Creates a new empty Dialog */
   /*public Dialog(NameAddress contact)
   {  local_contact=contact;
   }*/

   /** Creates a new Dialog based on Message <i>msg</i>;
     * Parameter <i>side</i> indicates whether is the client Dialog (use value Dialog.CLIENT) or server Dialog (use flag Dialog.SERVER). */
   /*public Dialog(int side, Message msg)
   { init(side,msg);
   }*/
 

   // ************************** Public methods **************************

  
   /** Changes the internal dialog state */
   protected void changeStatus(int newstatus)
   {  status=newstatus;
      //if (statusIs(D_CALL)) in_call=true;
      //if (statusIs(D_INIT) || statusIs(D_CLOSE)) in_call=false;
      printLog("changed dialog state: "+getStatus(),LogLevel.MEDIUM);
   }
   
   /** Whether the dialog state is equal to <i>st</i> */
   public boolean statusIs(int st)
   {  return status==st;
   }

   /** Gets the SipProvider of this Dialog. */
   public SipProvider getSipProvider()
   {  return sip_provider;
   }

   /** Updates empty attributes (tag) and mutable attributes (cseq, contact, route), based on a new message.
     * @param side indicates whether the Dialog is acting as transaction client or server (use constant values Dialog.T_CLIENT or Dialog.T_SERVER)
     * @param msg the message that is used to update the Dialog state */
   public void update(int side, Message msg)
   {  // call_id
      if (call_id==null)
      {  call_id=msg.getCallIdHeader().getCallId();
      }
      // names and tags
      if (side==T_CLIENT)
      {  if (remote_name==null || remote_tag==null)
         {  ToHeader to=msg.getToHeader();
           if (remote_name==null) remote_name=to.getNameAddress();
           if (remote_tag==null) remote_tag=to.getTag();
         }
         if (local_name==null || local_tag==null)
         {  FromHeader from=msg.getFromHeader();
            if (local_name==null) local_name=from.getNameAddress();
            if (local_tag==null) local_tag=from.getTag();
         }
         local_cseq=msg.getCSeqHeader().getSequenceNumber();
         //if (remote_cseq==-1) remote_cseq=SipProvider.pickInitialCSeq()-1;
      }
      else
      {  if (local_name==null || local_tag==null)
         {  ToHeader to=msg.getToHeader();
           if (local_name==null) local_name=to.getNameAddress();
           if (local_tag==null) local_tag=to.getTag();
         }
         if (remote_name==null || remote_tag==null)
         {  FromHeader from=msg.getFromHeader();
            if (remote_name==null) remote_name=from.getNameAddress();
            if (remote_tag==null) remote_tag=from.getTag();
         }
         remote_cseq=msg.getCSeqHeader().getSequenceNumber();
         if (local_cseq==-1) local_cseq=SipProvider.pickInitialCSeq()-1;
      }
      // contact
      if (msg.hasContactHeader())
      {  if ((side==T_CLIENT && msg.isRequest()) || (side==T_SERVER && msg.isResponse()))
            local_contact=msg.getContactHeader().getNameAddress();
         else
            remote_contact=msg.getContactHeader().getNameAddress();
      }
      // route or record-route
      if (side==T_CLIENT)
      {  if (msg.isRequest() && msg.hasRouteHeader() && route==null)
         {  route=msg.getRoutes().getValues();
         }
         if (side==T_CLIENT && msg.isResponse() && msg.hasRecordRouteHeader())
         {  Vector rr=msg.getRecordRoutes().getHeaders();
            int size=rr.size();
            route=new Vector(size);
            for (int i=0; i<size; i++)
               route.insertElementAt((new RecordRouteHeader((Header)rr.elementAt(size-1-i))).getNameAddress(),i);
         }
      }
      else
      {  if (msg.isRequest() && msg.hasRouteHeader() && route==null)
         {  Vector reverse_route=msg.getRoutes().getValues();
            int size=reverse_route.size();
            route=new Vector(size);
            for (int i=0; i<size; i++)
               route.insertElementAt(reverse_route.elementAt(size-1-i),i); 
         }
         if (msg.isRequest() && msg.hasRecordRouteHeader())
         {  Vector rr=msg.getRecordRoutes().getHeaders();
            int size=rr.size();
            route=new Vector(size);
            for (int i=0; i<size; i++)
               route.insertElementAt((new RecordRouteHeader((Header)rr.elementAt(i))).getNameAddress(),i);
         }
      }
   }

        
   /** Gets the inique Dialog-ID </i> */
   public DialogIdentifier getDialogID()
   {  return new DialogIdentifier(call_id,local_tag,remote_tag);
   } 


   /** Sets the local name */
   public void setLocalName(NameAddress url) { local_name=url; }
   /** Gets the local name */
   public NameAddress getLocalName() { return local_name; }


   /** Sets the remote name */
   public void setRemoteName(NameAddress url) { remote_name=url; }
   /** Gets the remote name */
   public NameAddress getRemoteName() { return remote_name; }


   /** Sets the local contact url */
   public void setLocalContact(NameAddress name_address) { local_contact=name_address; }
   /** Gets the local contact url */
   public NameAddress getLocalContact() { return local_contact; }


   /** Sets the remote contact url */
   public void setRemoteContact(NameAddress name_address) { remote_contact=name_address; }
   /** Gets the remote contact url */
   public NameAddress getRemoteContact() { return remote_contact; }

  
   /** Sets the call-id */
   public void setCallID(String id) { call_id=id; }
   /** Gets the call-id */
   public String getCallID() { return call_id; }

   
   /** Sets the local tag */
   public void setLocalTag(String tag) { local_tag=tag; }
   /** Gets the local tag */
   public String getLocalTag() { return local_tag; }


   public void setRemoteTag(String tag) { remote_tag=tag; }
   /** Gets the remote tag */
   public String getRemoteTag() { return remote_tag; }

   
   /** Sets the local CSeq number */
   public void setLocalCSeq(long cseq) { local_cseq=cseq; }
   /** Increments the local CSeq number */
   public void incLocalCSeq() { local_cseq++; }
   /** Gets the local CSeq number */
   public long getLocalCSeq() { return local_cseq; }


   /** Sets the remote CSeq number */
   public void setRemoteCSeq(long cseq) { remote_cseq=cseq; }
   /** Increments the remote CSeq number */
   public void incRemoteCSeq() { remote_cseq++; }
   /** Gets the remote CSeq number */
   public long getRemoteCSeq() { return remote_cseq; }

   
   /** Sets the route set */
   public void setRoute(Vector r) { route=r; }
   /** Gets the route set */
   public Vector getRoute() { return route; }


 
   //**************************** Logs ****************************/

   /** Adds a new string to the default Log */
   protected void printLog(String str, int level)
   {  if (log!=null) log.println("Dialog#"+d_number+": "+str,level+SipStack.LOG_LEVEL_DIALOG);  
   }

   /** Adds a Warning message to the default Log */
   protected final void printWarning(String str, int level)
   {  printLog("WARNING: "+str,level); 
   }

   /** Adds the Exception message to the default Log */
   protected final void printException(Exception e, int level)
   {  if (log!=null) log.printException(e,level+SipStack.LOG_LEVEL_DIALOG);
   }

   /** Verifies the correct status; if not logs the event. */
   protected final boolean verifyStatus(boolean expression)
   {  return verifyThat(expression,"dialog state mismatching");
   }

   /** Verifies an event; if not logs it. */
   protected final boolean verifyThat(boolean expression, String str)
   {  if (!expression)
      {  if (str==null || str.length()==0) printWarning("expression check failed. ",1);
         else printWarning(str,1);
      }
      return expression;
   }
     
}