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
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.header.*;
import org.zoolu.sip.transaction.TransactionClient;
import org.zoolu.sip.transaction.TransactionClientListener;
import org.zoolu.sip.message.*;
import org.zoolu.tools.Log;
import org.zoolu.tools.LogLevel;
import local.auth.DigestAuthentication;


/** Register User Agent.
  * It registers (one time or periodically) a contact address with a registrar server.
  */
class RegisterAgent implements Runnable, TransactionClientListener
{
   /** RegisterAgent listener */
   RegisterAgentListener listener;
   
   /** SipProvider */
   SipProvider sip_provider;

   /** User's URI with the fully qualified domain name of the registrar server. */
   NameAddress target;

   /** User name. */
   String username;

   /** User name. */
   String realm;

   /** User's passwd. */
   String passwd;

   /** Nonce for the next authentication. */
   String next_nonce=null;

   /** Qop for the next authentication. */
   String qop=null;

   /** User's contact address. */
   NameAddress contact; 

   /** Expiration time. */
   int expire_time;

   /** Renew time. */
   int renew_time;

   /** Whether keep on registering. */
   boolean loop;

   /** Whether the thread is running. */
   boolean is_running;

   /** Event logger. */
   Log log;

      
   /** Creates a new RegisterAgent. */
   public RegisterAgent(SipProvider sip_provider, String target_url, String contact_url, RegisterAgentListener listener)
   {  init(sip_provider,target_url,contact_url,listener);
   }
   
   
   /** Creates a new RegisterAgent with authentication credentials (i.e. username, realm, and passwd). */
   public RegisterAgent(SipProvider sip_provider, String target_url, String contact_url, String username, String realm, String passwd, RegisterAgentListener listener)
   {  init(sip_provider,target_url,contact_url,listener);
      this.username=username;
      this.realm=realm;
      this.passwd=passwd;
   }

   /** Inits the RegisterAgent. */
   private void init(SipProvider sip_provider, String target_url, String contact_url, RegisterAgentListener listener)
   {  this.sip_provider=sip_provider;
      this.log=sip_provider.getLog();
      this.target=new NameAddress(target_url);
      this.contact=new NameAddress(contact_url);
      this.expire_time=SipStack.expires;
      this.renew_time=0;
      this.is_running=false;
      this.listener=listener;
   }


   /** Whether it is periodically registering. */
   public boolean isRegistering()
   {  return is_running;
   }


   /** Registers with the registrar server. */
   public void register()
   {  register(expire_time);
   }


   /** Registers with the registrar server for <i>expire_time</i> seconds. */
   public void register(int expire_time)
   {  if (expire_time>0) this.expire_time=expire_time;
      Message req=MessageFactory.createRegisterRequest(sip_provider,target,target,contact);
      req.setExpiresHeader(new ExpiresHeader(String.valueOf(expire_time)));
      if (next_nonce!=null)
      {  AuthorizationHeader ah=new AuthorizationHeader("Digest");
         SipURL target_url=target.getAddress();
         ah.addUsernameParam(username);
         ah.addRealmParam(realm);
         ah.addNonceParam(next_nonce);
         ah.addUriParam(req.getRequestLine().getAddress().toString());
         ah.addQopParam(qop);
         String response=(new DigestAuthentication(SipMethods.REGISTER,ah,null,passwd)).calcResponse();
         ah.addResponseParam(response);
         req.setAuthorizationHeader(ah);
      }
      if (expire_time>0) printLog("Registering contact "+contact+" (it expires in "+expire_time+" secs)",LogLevel.HIGH);
      else printLog("Unregistering contact "+contact,LogLevel.HIGH);
      TransactionClient t=new TransactionClient(sip_provider,req,this);
      t.request(); 
   }


   /** Unregister with the registrar server */
   public void unregister()
   {  register(0);
   } 


   /** Unregister all contacts with the registrar server */
   public void unregisterall()
   {  NameAddress user=new NameAddress(target);
      Message req=MessageFactory.createRegisterRequest(sip_provider,target,target,null);
      //ContactHeader contact_star=new ContactHeader(); // contact is *
      //req.setContactHeader(contact_star);
      req.setExpiresHeader(new ExpiresHeader(String.valueOf(0)));
      printLog("Unregistering all contacts",LogLevel.HIGH);
      TransactionClient t=new TransactionClient(sip_provider,req,this); 
      t.request(); 
   }


   /** Periodically registers with the registrar server. */
   public void loopRegister(int expire_time, int renew_time)
   {  this.expire_time=expire_time;
      this.renew_time=renew_time;
      loop=true;
      if (!is_running) (new Thread(this)).start();
   }

   /** Halts the periodic registration. */
   public void halt()
   {  if (is_running) loop=false;
   }

   
   // ***************************** run() *****************************

   /** Run method */
   public void run()
   {  
      is_running=true;
      try
      {  while (loop)
         {  register();
            Thread.sleep(renew_time*1000);
         }
      }
      catch (Exception e) {  printException(e,LogLevel.HIGH);  }
      is_running=false;
   }

   
   // **************** Transaction callback functions *****************

   /** Callback function called when client sends back a failure response. */

   /** Callback function called when client sends back a provisional response. */
   public void onCltProvisionalResponse(TransactionClient transaction, Message resp)
   {  // do nothing..
   }

   /** Callback function called when client sends back a success response. */
   public void onCltSuccessResponse(TransactionClient transaction, Message resp)
   {  if (transaction.getTransactionMethod().equals(SipMethods.REGISTER))
      {  if (resp.hasAuthenticationInfoHeader())
         {  next_nonce=resp.getAuthenticationInfoHeader().getNextnonceParam();
         }
         StatusLine status=resp.getStatusLine();
         String result=status.getCode()+" "+status.getReason();
         printLog("Registration success: "+result,LogLevel.HIGH);
         if (listener!=null) listener.onUaRegistrationSuccess(this,target,contact,result);
      }
   }

   /** Callback function called when client sends back a failure response. */
   public void onCltFailureResponse(TransactionClient transaction, Message resp)
   {  if (transaction.getTransactionMethod().equals(SipMethods.REGISTER))
      {  StatusLine status=resp.getStatusLine();
         int code=status.getCode();
         if (code==401 && resp.hasWwwAuthenticateHeader() && resp.getWwwAuthenticateHeader().getRealmParam().equalsIgnoreCase(realm))
         {  Message req=MessageFactory.createRegisterRequest(sip_provider,target,target,contact);
            WwwAuthenticateHeader wah=resp.getWwwAuthenticateHeader();
            String qop_options=wah.getQopOptionsParam();
            //printLog("DEBUG: qop-options: "+qop_options,LogLevel.MEDIUM);
            qop=(qop_options!=null)? "auth" : null;
            AuthorizationHeader ah=(new DigestAuthentication(SipMethods.REGISTER,req.getRequestLine().getAddress().toString(),wah,qop,null,username,passwd)).calcAuthorizationHeader();
            req.setAuthorizationHeader(ah);
            req.setExpiresHeader(new ExpiresHeader(String.valueOf(expire_time)));            
            TransactionClient t=new TransactionClient(sip_provider,req,this);
            t.request();        
         }
         else
         {  String result=code+" "+status.getReason();
            printLog("Registration failure: "+result,LogLevel.HIGH);
            if (listener!=null) listener.onUaRegistrationFailure(this,target,contact,result);
         }
      }
   }

   /** Callback function called when client expires timeout. */
   public void onCltTimeout(TransactionClient transaction)
   {  if (transaction.getTransactionMethod().equals(SipMethods.REGISTER))
      {  printLog("Registration failure: No response from server.",LogLevel.HIGH);
         if (listener!=null) listener.onUaRegistrationFailure(this,target,contact,"Timeout");
      }
   }

   
   // ****************************** Logs *****************************

   /** Adds a new string to the default Log */
   void printLog(String str, int level)
   {  if (log!=null) log.println("RegisterAgent: "+str,level+SipStack.LOG_LEVEL_UA);  
   }

   /** Adds the Exception message to the default Log */
   void printException(Exception e,int level)
   {  if (log!=null) log.printException(e,level+SipStack.LOG_LEVEL_UA);
   }
}