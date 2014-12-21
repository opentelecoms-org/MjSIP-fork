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


import org.zoolu.sip.provider.*;
import org.zoolu.sip.address.*;
import org.zoolu.tools.Archive;
import org.zoolu.tools.Log;
import org.zoolu.tools.LogLevel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.util.Vector;


/** Simple GUI-based SIP user agent (UA). */
public class GraphicalUA extends JFrame implements UserAgentListener, RegisterAgentListener
{
   /** This application */
   final String app_name="MjSip mini UA";

   /** Event logger. */
   Log log;
   
   /** User Agent */
   UserAgent ua;

   /** Register Agent */
   RegisterAgent ra;

   /** UserAgentProfile */
   UserAgentProfile user_profile;

   /** Title */
   String user_name=app_name;

   /** Recent contacts */
   protected static final int NMAX_CONTACTS=10;

   /** Recent contacts */
   StringList contact_list;

   private static final int W_Width=320; // window width
   private static final int W_Height=90; // window height
   private static final int C_Height=30; // buttons and combobox height (total)

   /** Media file path */
   final String MEDIA_PATH="media/local/ua/";
   
   final String CALL_GIF=MEDIA_PATH+"call.gif";
   final String HANGUP_GIF=MEDIA_PATH+"hangup.gif";
   
   Icon icon_call;
   Icon icon_hangup;
   //Icon icon_call=new ImageIcon("media/ua/call.gif");
   //Icon icon_hangup=new ImageIcon("media/ua/hangup.gif");

   JPanel jPanel1 = new JPanel();
   JPanel jPanel2 = new JPanel();
   JPanel jPanel3 = new JPanel();
   JPanel jPanel4 = new JPanel();
   JComboBox jComboBox1 = new JComboBox();
   BorderLayout borderLayout1 = new BorderLayout();
   BorderLayout borderLayout2 = new BorderLayout();
   JPanel jPanel5 = new JPanel();
   GridLayout gridLayout2 = new GridLayout();
   GridLayout gridLayout3 = new GridLayout();
   JButton jButton1 = new JButton();
   JButton jButton2 = new JButton();
   ComboBoxEditor comboBoxEditor1=new BasicComboBoxEditor();
   BorderLayout borderLayout3 = new BorderLayout();

   JTextField display=new JTextField();



   /** Creates a new GraphicalUA */
   public GraphicalUA(SipProvider sip_provider, UserAgentProfile user_profile)
   {
      log=sip_provider.getLog();
      this.user_profile=user_profile;
      
      ua=new UserAgent(sip_provider,user_profile,this);
      ua.listen();

      SipURL AOR=(new NameAddress(user_profile.from_url)).getAddress();
      String username=AOR.getUserName();
      String realm=AOR.getHost();
      ra=new RegisterAgent(sip_provider,user_profile.from_url,user_profile.contact_url,username,realm,user_profile.passwd,this);
      
      String jar_file=user_profile.ua_jar;
      icon_call=Archive.getImageIcon(Archive.getJarURL(jar_file,CALL_GIF));
      icon_hangup=Archive.getImageIcon(Archive.getJarURL(jar_file,HANGUP_GIF));

      user_name=user_profile.contact_url;
      contact_list=new StringList(user_profile.contacts_file);
      jComboBox1=new JComboBox(contact_list.getElements());

      try
      {
         jbInit();
      }
      catch(Exception e) { e.printStackTrace(); }
            
      //Image image=Archive.getImage(Archive.getJarURL(jar_file,"media/local/ua/zoolu.gif"));
      //PopupFrame about=new PopupFrame("About",image,this);
      //try  {  Thread.sleep(3000);  } catch(Exception e) {  }
      //about.closeWindow();
      
      run();   
   }


   private void jbInit() throws Exception
   {
      // set frame dimensions
      this.setSize(W_Width,W_Height);
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = this.getSize();
      if (frameSize.height > screenSize.height) frameSize.height = screenSize.height;
      if (frameSize.width > screenSize.width) frameSize.width = screenSize.width;
      this.setLocation((screenSize.width - frameSize.width)/2 - 40, (screenSize.height - frameSize.height)/2 - 40);
      this.setResizable(false);

      this.setTitle(user_name);
      this.addWindowListener(new java.awt.event.WindowAdapter()
      {  public void windowClosing(WindowEvent e) { this_windowClosing(e); }
      });
      jPanel1.setLayout(borderLayout3);
      jPanel2.setLayout(borderLayout2);
      display.setBackground(Color.black);
      display.setForeground(Color.green);
      display.setText(app_name);
      jPanel4.setLayout(borderLayout1);
      jPanel5.setLayout(gridLayout2);
      jPanel3.setLayout(gridLayout3);
      gridLayout3.setRows(2);
      gridLayout3.setColumns(1);
      if (icon_call!=null && icon_call.getIconWidth()>0) jButton1.setIcon(icon_call);
      else jButton1.setText("Call");
      jButton1.addActionListener(new java.awt.event.ActionListener()
      {  public void actionPerformed(ActionEvent e) { jButton1_actionPerformed(); }
      });
      jButton1.addKeyListener(new java.awt.event.KeyAdapter()
      {  public void keyTyped(KeyEvent e) { jButton1_actionPerformed(); }
      });
      if (icon_hangup!=null && icon_hangup.getIconWidth()>0) jButton2.setIcon(icon_hangup);
      else jButton2.setText("Hungup");
      jButton2.addActionListener(new java.awt.event.ActionListener()
      {  public void actionPerformed(ActionEvent e) { jButton2_actionPerformed(); }
      });
      jButton2.addKeyListener(new java.awt.event.KeyAdapter()
      {  public void keyTyped(KeyEvent e) { jButton2_actionPerformed(); }
      });
      jComboBox1.addActionListener(new java.awt.event.ActionListener()
      {  public void actionPerformed(ActionEvent e) { jComboBox1_actionPerformed(e); }
      });
      comboBoxEditor1.addActionListener(new java.awt.event.ActionListener()
      {  public void actionPerformed(ActionEvent e) { comboBoxEditor1_actionPerformed(e); }
      });
      jButton2.setFont(new java.awt.Font("Dialog", 0, 10));
      jButton1.setFont(new java.awt.Font("Dialog", 0, 10));
      comboBoxEditor1.getEditorComponent().setBackground(Color.yellow);
      jComboBox1.setEditable(true);
      jComboBox1.setEditor(comboBoxEditor1);
      jComboBox1.setSelectedItem(null);
      jPanel3.setPreferredSize(new Dimension(0,C_Height));
      this.getContentPane().add(jPanel1, BorderLayout.CENTER);
      jPanel1.add(jPanel2, BorderLayout.CENTER);
      jPanel1.add(jPanel3, BorderLayout.SOUTH);
      jPanel2.add(display, BorderLayout.CENTER);
      jPanel3.add(jPanel4, null);
      jPanel3.add(jPanel5, null);
      jPanel4.add(jComboBox1, BorderLayout.CENTER);
      jPanel5.add(jButton1, null);
      jPanel5.add(jButton2, null);

      // show it
      this.setVisible(true);
   }



   /** Starts the UA */
   void run()
   {
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

      if (user_profile.call_to!=null)
      // ########## make a call with the remote URL
      {  ua.printLog("UAC: CALLING "+user_profile.call_to);
         jComboBox1.setSelectedItem(null);
         comboBoxEditor1.setItem(user_profile.call_to);
         ua.call(user_profile.call_to);       
      } 

      if (!user_profile.audio && !user_profile.video) ua.printLog("ONLY SIGNALING, NO MEDIA");   
   }


   void this_windowClosing(WindowEvent e)
   {
      System.exit(0);
   }


   void jButton1_actionPerformed()
   {
      if (ua.statusIs(UserAgent.UA_IDLE))
      {  String url=(String)comboBoxEditor1.getItem();
         if (url!=null && url.length()>0)
         {  ua.hangup();
            ua.call(url);
            display.setText("CALLING "+url);
         }
      }
      else
      if (ua.statusIs(UserAgent.UA_INCOMING_CALL))
      {  ua.accept();
         display.setText("ON CALL");
      }
   }


   void jButton2_actionPerformed()
   {
      if (!ua.statusIs(UserAgent.UA_IDLE))
      {  ua.hangup();
         ua.listen();
      
         display.setText("HANGUP");
      }
   }


   void jComboBox1_actionPerformed(ActionEvent e)
   {  // if the edited URL is different from the selected item, copy the selected item in the editor
      /*
      String edit_name=(String)comboBoxEditor1.getItem();
      int index=jComboBox1.getSelectedIndex();
      if (index>=0)
      {  String selected_name=contact_list.elementAt(index);
         if (!selected_name.equals(edit_name)) comboBoxEditor1.setItem(selected_name);
      }*/
   }


   void comboBoxEditor1_actionPerformed(ActionEvent e)
   {  // if a new URL has been typed, insert it in the contact_list and make it selected item
      // else, simply make the URL the selected item
      String name=(String)comboBoxEditor1.getItem();
      // parse separatly NameAddrresses or SipURLs
      if (name.indexOf("\"")>=0 || name.indexOf("<")>=0)
      {  // try to parse a NameAddrress
         NameAddress nameaddr=(new SipParser(name)).getNameAddress();
         if (nameaddr!=null) name=nameaddr.toString();
         else name=null;
      }
      else
      {  // try to parse a SipURL
         SipURL url=new SipURL(name);
         if (url!=null) name=url.toString();
         else name=null;
      }

      if (name==null)
      {  System.out.println("DEBUG: No sip url recognized in: "+(String)comboBoxEditor1.getItem());
         return;
      }

      // checks if the the URL is already present in the contact_list
      if (!contact_list.contains(name))
      {  jComboBox1.insertItemAt(name,0);
         jComboBox1.setSelectedIndex(0);
         // limit the list size
         while (contact_list.getElements().size()>NMAX_CONTACTS) jComboBox1.removeItemAt(NMAX_CONTACTS);
         // save new contact list
         contact_list.save();         
      }
      else
      {  int index=contact_list.indexOf(name);
         jComboBox1.setSelectedIndex(index);
      }
 
   }


   /** Gets the UserAgent */
   /*protected UserAgent getUA()
   {  return ua;
   }*/


   /** Register with the registrar server */
   public void register(int expire_time)
   {  if (ra.isRegistering()) ra.halt();
      ra.register(expire_time);
   }


   /** Periodically registers the contact address with the registrar server. */
   public void loopRegister(int expire_time, int renew_time)
   {  if (ra.isRegistering()) ra.halt();
      ra.loopRegister(expire_time,renew_time);
   }


   /** Unregister with the registrar server */
   public void unregister()
   {  if (ra.isRegistering()) ra.halt();
      ra.unregister();
   }


   /** Unregister all contacts with the registrar server */
   public void unregisterall()
   {  if (ra.isRegistering()) ra.halt();
      ra.unregisterall();
   }


   // ********************** UA callback functions **********************


   /** When a new call is incoming */
   public void onUaCallIncoming(UserAgent ua, NameAddress caller)
   {      
      if (ua.user_profile.redirect_to!=null) // redirect the call
      {  display.setText("CALL redirected to "+ua.user_profile.redirect_to);
         ua.redirect(ua.user_profile.redirect_to);
      }         
      else
      if (ua.user_profile.auto_accept) // automatically accept the call
      {  display.setText("ON CALL");
         jComboBox1.setSelectedItem(null);
         comboBoxEditor1.setItem(caller.toString());
         ua.accept();
      }
      else
      {  display.setText("INCOMING CALL");
         jComboBox1.setSelectedItem(null);
         comboBoxEditor1.setItem(caller.toString());
      }
   }

   /** When an ougoing call is remotly ringing */
   public void onUaCallRinging(UserAgent ua)
   {
      display.setText("RINGING");
   }

   /** When an ougoing call has been accepted */
   public void onUaCallAccepted(UserAgent ua)
   {
      display.setText("ON CALL");
   }


   /** When an incoming call has been cancelled */
   public void onUaCallCancelled(UserAgent ua)
   {
      display.setText("CANCELLED");
      ua.listen();
   }


   /** When a call has been trasferred */
   public void onUaCallTrasferred(UserAgent ua)
   {  
      display.setText("TRASFERRED");
      ua.listen();
   }

   /** When an ougoing call has been refused or timeout */
   public void onUaCallFailed(UserAgent ua)
   {
      display.setText("FAILED");
      ua.listen();
   }

   /** When a call is beeing remotly closed */
   public void onUaCallClosing(UserAgent ua)
   {
      display.setText("BYE");
      ua.listen();
   }


   // ********************** RA callback functions **********************

   /** When a UA has been successfully (un)registered. */
   public void onUaRegistrationSuccess(RegisterAgent ra, NameAddress target, NameAddress contact, String result)
   {  ua.printLog("Registration success: "+result,LogLevel.HIGH);
   }

   /** When a UA failed on (un)registering. */
   public void onUaRegistrationFailure(RegisterAgent ra, NameAddress target, NameAddress contact, String result)
   {  ua.printLog("Registration failure: "+result,LogLevel.HIGH);
   }


   // ******************************** MAIN *******************************

   /** The main method. */
   public static void main(String[] args)
   {
      System.out.println("Graphical MJSIP UA 1.0");

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

            System.out.println("usage:\n   java GraphicalUA [options]");
            System.out.println("   options:");
            System.out.println("   -h                 this help");
            System.out.println("   -f <config_file>   specifies a configuration file");
            System.out.println("   -s                 auto accept incoming calls");
            System.out.println("   -t <secs>          specipies the call duration (0 means manual hangup)");
            System.out.println("   -p <port>          local media port");
            System.out.println("   -g <time>          registers the contact URL with the registrar server");
            System.out.println("                      where time is the duration of the registration, and can be");
            System.out.println("                      in seconds (default) or hours ( -r 7200 is the same as -r 2h )");
            System.out.println("   -u                 unregisters the contact URL with the registrar server");
            System.out.println("                      (is the same as -r 0)");
            System.out.println("   -z                 unregisters ALL the contact URLs");
            System.out.println("   -r <url>           redirects the call to a new user");
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

         GraphicalUA visual_ua=new GraphicalUA(sip_provider,user_profile);
      }
      catch (Exception e) { e.printStackTrace(); }
   }


   // ****************************** Logs *****************************

   /** Adds a new string to the default Log */
   private void printLog(String str)
   {  printLog(str,LogLevel.HIGH);
   }

   /** Adds a new string to the default Log */
   private void printLog(String str, int level)
   {  if (log!=null) log.println("GraphicalUA: "+str,level+SipStack.LOG_LEVEL_UA);  
   }

   /** Adds the Exception message to the default Log */
   private void printException(Exception e,int level)
   {  if (log!=null) log.printException(e,level+SipStack.LOG_LEVEL_UA);
   }
}