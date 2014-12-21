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


import local.media.AudioClipPlayer;
import org.zoolu.sip.call.*;
import org.zoolu.sip.address.*;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.header.ExpiresHeader;
import org.zoolu.sip.header.ContactHeader;
import org.zoolu.sip.header.CallIdHeader;
import org.zoolu.sip.header.StatusLine;
import org.zoolu.sip.transaction.TransactionClient;
import org.zoolu.sip.transaction.TransactionClientListener;
import org.zoolu.sip.call.*;
import org.zoolu.sip.message.*;
import org.zoolu.sdp.*;
import org.zoolu.tools.Log;
import org.zoolu.tools.LogLevel;
import org.zoolu.tools.Parser;
import org.zoolu.tools.Archive;

//import java.util.Iterator;
import java.util.Enumeration;
import java.util.Vector;
import java.io.*;


/** Simple command-line-based SIP user agent (UA).
  * It includes audio/video applications.
  * <p>It can use external audio/video tools as media applications.
  * Currently only RAT (Robust Audio Tool) and VIC are supported as external applications.
  */
public class CommandLineUA implements UserAgentListener, RegisterAgentListener
{           

   /** Event logger. */
   Log log;
   
   /** User Agent */
   UserAgent ua;

   /** Register Agent */
   RegisterAgent ra;
   
   /** UserAgentProfile */
   UserAgentProfile user_profile;

   
   /** Costructs a UA with a default media port */
   public CommandLineUA(SipProvider sip_provider, UserAgentProfile user_profile)
   {  log=sip_provider.getLog();
      this.user_profile=user_profile;

      ua=new UserAgent(sip_provider,user_profile,this);      

      SipURL AOR=(new NameAddress(user_profile.from_url)).getAddress();
      String username=AOR.getUserName();
      String realm=AOR.getHost();
      ra=new RegisterAgent(sip_provider,user_profile.from_url,user_profile.contact_url,username,realm,user_profile.passwd,this);

      run();
   }


   /** Register with the registrar server */
   void register(int expire_time)
   {  if (ra.isRegistering()) ra.halt();
      ra.register(expire_time);
   }


   /** Periodically registers the contact address with the registrar server. */
   void loopRegister(int expire_time, int renew_time)
   {  if (ra.isRegistering()) ra.halt();
      ra.loopRegister(expire_time,renew_time);
   }


   /** Unregister with the registrar server */
   void unregister()
   {  if (ra.isRegistering()) ra.halt();
      ra.unregister();
   }


   /** Unregister all contacts with the registrar server */
   void unregisterall()
   {  if (ra.isRegistering()) ra.halt();
      ra.unregisterall();
   }


   /** Makes a new call */
   void call(String target_url)
   {  ua.hangup();
      ua.printLog("UAC: CALLING "+target_url);
      if (!ua.user_profile.audio && !ua.user_profile.video) ua.printLog("ONLY SIGNALING, NO MEDIA");       
      ua.call(target_url);       
   } 
         
         
   /** Receives incoming calls (auto accept) */
   void listen()
   {  ua.printLog("UAS: WAITING FOR INCOMING CALL");
      if (!ua.user_profile.audio && !ua.user_profile.video) ua.printLog("ONLY SIGNALING, NO MEDIA");       
      ua.listen(); 
      System.out.println("digit the callee's URL to make a call or press 'enter' to exit");
   } 


   /** Starts the UA */
   void run()
   {
      try
      {  BufferedReader in=new BufferedReader(new InputStreamReader(System.in)); 
         
         // Set the re-invite
         if (user_profile.re_invite_time>0)
         {  ua.reInvite(user_profile.contact_url,user_profile.re_invite_time);
         }

         // Set the transfer (REFER)
         if (user_profile.transfer_to!=null && user_profile.transfer_time>0)
         {  ua.callTransfer(user_profile.transfer_to,user_profile.transfer_time);
         }

         if (user_profile.do_unregister_all)
         // ########## unregisters ALL contact URLs
         {  ua.printLog("UNREGISTER ALL contact URLs");
            unregisterall();
         } 

         if (user_profile.do_unregister)
         // unregisters the contact URL
         {  ua.printLog("UNREGISTER the contact URL");
            unregister();
         } 

         if (user_profile.do_register)
         // ########## registers the contact URL with the registrar server
         {  ua.printLog("REGISTRATION");
            loopRegister(user_profile.expires,user_profile.expires/2);
         }         
         
         if (ua.user_profile.hangup_time>0)   
         {  Thread.sleep(ua.user_profile.hangup_time*1000);
            ua.hangup();
         }

         if (user_profile.call_to!=null)
         {  // UAC
            call(user_profile.call_to); 
            System.out.println("press 'enter' to hangup");
            in.readLine();
            ua.hangup();
            exit();
         }
         else
         {  // UAS
            if (user_profile.auto_accept) ua.printLog("UAS: AUTO ACCEPT MODE");
            listen();
            while (true)
            {  String line=in.readLine();
               if (ua.statusIs(UserAgent.UA_INCOMING_CALL))
               {  if (line.toLowerCase().startsWith("n")) ua.hangup();
                  else
                  {  ua.accept();             
                  }
               }
               else
               if (ua.statusIs(UserAgent.UA_IDLE))
               {  if (line!=null && line.length()>0)
                  {  call(line);
                  }
                  else
                  {  exit();
                  }
               }
               else
               if (ua.statusIs(UserAgent.UA_ONCALL))
               {  ua.hangup();
                  listen();
               }
            }
         }
      }
      catch (Exception e)  {  e.printStackTrace(); System.exit(0);  }
   }


   /** Exits */
   void exit()
   {  try {  Thread.sleep(1000);  } catch (Exception e) {}
      System.exit(0);
   }


   // ******************* UserAgent callback functions ******************

   /** When a new call is incoming */
   public void onUaCallIncoming(UserAgent ua, NameAddress caller)
   {  if (ua.user_profile.redirect_to!=null) // redirect the call
      {  ua.redirect(ua.user_profile.redirect_to);
         System.out.println("call redirected to "+ua.user_profile.redirect_to);
      }         
      else
      if (ua.user_profile.auto_accept) // automatically accept the call
      {  ua.accept();
         System.out.println("press 'enter' to hangup"); 
      }
      else         
      {  System.out.println("incoming call from "+caller.toString());
         System.out.println("accept? [yes/no]");
      }
   }
   
   /** When an ougoing call is remotly ringing */
   public void onUaCallRinging(UserAgent ua)
   {  
   }

   /** When an ougoing call has been accepted */
   public void onUaCallAccepted(UserAgent ua)
   {
   }
   
   /** When a call has been trasferred */
   public void onUaCallTrasferred(UserAgent ua)
   {  
   }

   /** When an incoming call has been cancelled */
   public void onUaCallCancelled(UserAgent ua)
   {  
   }

   /** When an ougoing call has been refused or timeout */
   public void onUaCallFailed(UserAgent ua)
   {  if (ua.user_profile.call_to!=null) exit();
      else listen();
   }

   /** When a call is beeing remotly closed */
   public void onUaCallClosing(UserAgent ua)
   {  if (ua.user_profile.call_to!=null) exit();
      else listen();     
   }


   // **************** RegisterAgent callback functions *****************

   /** When a UA has been successfully (un)registered. */
   public void onUaRegistrationSuccess(RegisterAgent ra, NameAddress target, NameAddress contact, String result)
   {  ua.printLog("Registration success: "+result,LogLevel.HIGH);
   }

   /** When a UA failed on (un)registering. */
   public void onUaRegistrationFailure(RegisterAgent ra, NameAddress target, NameAddress contact, String result)
   {  ua.printLog("Registration failure: "+result,LogLevel.HIGH);
   }
   

   // ***************************** MAIN *****************************


   /** The main method. */
   public static void main(String[] args)
   {         
      String file=null;
      boolean opt_regist=false;
      boolean opt_unregist=false;
      boolean opt_unregist_all=false;
      int     opt_expires=0;
      String  opt_call_to=null;      
      boolean opt_auto_accept=false;      
      int     opt_hangup_time=0;
      boolean opt_no_offer=false;
      String  opt_redirect_to=null;
      boolean opt_audio=false;
      boolean opt_video=false;
      boolean opt_recv_only=false;
      boolean opt_send_only=false;
      boolean opt_send_tone=false;
      String  opt_send_file=null;
      String  opt_recv_file=null;
      int     opt_media_port=21068;
      String  opt_transfer_to=null;
      int     opt_transfer_time=0;
      int     opt_re_invite_time=0;
 
      try
      {  
         for (int i=0; i<args.length; i++)
         {
            if (args[i].equals("-f") && args.length>(i+1))
            {  file=args[++i];
               continue;
            }
            if (args[i].equals("-c") && args.length>(i+1)) // make a call with a remote user (url)
            {  opt_call_to=args[++i];
               continue;
            }
            if (args[i].equals("-s")) // automatic accept incoming call
            {  opt_auto_accept=true;
               continue;
            }
            if (args[i].equals("-p") && args.length>(i+1)) // set the local port
            {  opt_media_port=Integer.parseInt(args[++i]);
               continue;
            }
            if (args[i].equals("-g") && args.length>(i+1)) // registrate the contact url
            {  opt_regist=true;
               String time=args[++i];
               if (time.charAt(time.length()-1)=='h') opt_expires=Integer.parseInt(time.substring(0,time.length()-1))*3600;
               else opt_expires=Integer.parseInt(time);
               continue;
            }
            if (args[i].equals("-u")) // unregistrate the contact url
            {  opt_unregist=true;
               continue;
            }
            if (args[i].equals("-z")) // unregistrate all contact urls
            {  opt_unregist_all=true;
               continue;
            }
            if (args[i].equals("-t") && args.length>(i+1)) // set the call duration
            {  opt_hangup_time=Integer.parseInt(args[++i]);
               continue;
            }
            if (args[i].equals("-i") && args.length>(i+1)) // set the re-invite time
            {  opt_re_invite_time=Integer.parseInt(args[++i]);
               continue;
            }
            if (args[i].equals("-o")) // no offer in the invite
            {  opt_no_offer=true;
               continue;
            }
            if (args[i].equals("-r") && args.length>(i+1)) // redirect the call to a new url
            {  opt_auto_accept=true;
               opt_redirect_to=args[++i];
               continue;
            }
            if (args[i].equals("-q") && args.length>(i+1)) // transfers the call to a new user (REFER)
            {  opt_transfer_to=args[++i];
               opt_transfer_time=Integer.parseInt(args[++i]);
               continue;
            }
            if (args[i].equals("-a")) // use audio
            {  opt_audio=true;
               continue;
            }
            if (args[i].equals("-v")) // use video
            {  opt_video=true;
               continue;
            }
            if (args[i].equals("--recv-only")) // receive only mode
            {  opt_recv_only=true;
               continue;
            }
            if (args[i].equals("--send-only")) // send only mode
            {  opt_send_only=true;
               continue;
            }
            if (args[i].equals("--send-tone")) // send only mode
            {  opt_send_only=true;
               opt_send_tone=true;
               continue;
            }
            if (args[i].equals("--send-file")) // send audio file
            {  opt_send_file=args[++i];
               continue;
            }
            if (args[i].equals("--recv-file")) // receive audio file
            {  opt_recv_file=args[++i];
               continue;
            }
            
            // else, do:
            if (!args[i].equals("-h"))
               System.out.println("unrecognized param '"+args[i]+"'\n");
            
            System.out.println("usage:\n   java UserAgent [options]");
            System.out.println("   options:");
            System.out.println("   -h                 this help");
            System.out.println("   -f <config_file>   specifies a configuration file");
            System.out.println("   -c <call_to>       calls a remote user");
            System.out.println("   -s                 auto accept incoming calls");
            System.out.println("   -t <secs>          specipies the call duration (0 means manual hangup)");
            System.out.println("   -p <port>          local media port");
            System.out.println("   -i <secs>          re-invite after <secs> seconds");
            System.out.println("   -g <time>          registers the contact URL with the registrar server");
            System.out.println("                      where time is the duration of the registration, and can be");
            System.out.println("                      in seconds (default) or hours ( -r 7200 is the same as -r 2h )");
            System.out.println("   -u                 unregisters the contact URL with the registrar server");
            System.out.println("                      (is the same as -r 0)");
            System.out.println("   -z                 unregisters ALL the contact URLs");
            System.out.println("   -r <url>           redirects the call to a new user");
            System.out.println("   -q <url> <secs>    transfers the call to a new user (REFER) after <secs> seconds");
            System.out.println("   -o                 no offer in invite (offer/answer in 2xx/ack)");
            System.out.println("   -a                 audio");
            System.out.println("   -v                 video");
            System.out.println("   --recv-only        receive only mode, no media is sent");
            System.out.println("   --send-only        send only mode, no media is received");
            System.out.println("   --send-tone        send only mode, an audio test tone is generated");
            System.out.println("   --send-file <file> audio is played from the specified file");
            System.out.println("   --recv-file <file> audio is recorded to the specified file");
            System.exit(0);
         }
                     
         SipStack.init(file);
         SipProvider sip_provider=new SipProvider(file);
         UserAgentProfile user_profile=new UserAgentProfile(file);
         
         if (opt_regist) user_profile.do_register=true;
         if (opt_unregist) user_profile.do_unregister=true;
         if (opt_unregist_all) user_profile.do_unregister_all=true;
         if (opt_expires>0) user_profile.expires=opt_expires;
         if (opt_call_to!=null) user_profile.call_to=opt_call_to;
         if (opt_auto_accept) user_profile.auto_accept=true;
         if (opt_hangup_time>0) user_profile.hangup_time=opt_hangup_time;
         if (opt_redirect_to!=null) user_profile.redirect_to=opt_redirect_to;
         if (opt_re_invite_time>0) user_profile.re_invite_time=opt_re_invite_time;
         if (opt_no_offer) user_profile.no_offer=true;
         if (opt_media_port!=21068) user_profile.video_port=(user_profile.audio_port=opt_media_port)+2;
         if (opt_audio) user_profile.audio=true;
         if (opt_video) user_profile.video=true;
         if (opt_recv_only) user_profile.recv_only=true;
         if (opt_send_only) user_profile.send_only=true;             
         if (opt_send_tone) user_profile.send_tone=true;
         if (opt_send_file!=null) user_profile.send_file=opt_send_file;
         if (opt_recv_file!=null) user_profile.recv_file=opt_recv_file;

         // ################# patch to make audio working with javax.sound.. #################
         // # currently AudioSender must be started before any AudioClipPlayer is initialized,
         // # since there is a problem with the definition of the audio format
         // ##################################################################################
         if (!user_profile.use_rat && !user_profile.use_jmf)
         //{  if (user_profile.audio && !user_profile.recv_only) local.media.AudioInput.initAudioLine();
         {  if (user_profile.audio && !user_profile.recv_only && user_profile.send_file==null && !user_profile.send_tone) local.media.AudioInput.initAudioLine();
            if (user_profile.audio && !user_profile.send_only && user_profile.recv_file==null) local.media.AudioOutput.initAudioLine();
         }
     
         new CommandLineUA(sip_provider,user_profile);
      }
      catch (Exception e)  {  e.printStackTrace(); System.exit(0);  }
   }    
   

   // ****************************** Logs *****************************

   /** Adds a new string to the default Log */
   void printLog(String str)
   {  printLog(str,LogLevel.HIGH);
   }

   /** Adds a new string to the default Log */
   void printLog(String str, int level)
   {  if (log!=null) log.println("CommandLineUA: "+str,level+SipStack.LOG_LEVEL_UA);  
   }

   /** Adds the Exception message to the default Log */
   void printException(Exception e,int level)
   {  if (log!=null) log.printException(e,level+SipStack.LOG_LEVEL_UA);
   }

}
