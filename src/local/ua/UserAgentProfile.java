package local.ua;


import org.zoolu.sip.address.*;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.tools.Configure;
import org.zoolu.tools.Parser;


/** UserProfile maintains the user configuration
  */
public class UserAgentProfile extends Configure
{
   /** The default configuration file */
   private static String config_file="mjsip.cfg";

       
   // ********************** user configurations *********************

   /** User's URL (From URL).
     * If not defined (default), it equals the <i>contact_url</i> */
   public String from_url=null;
   /** User's passwd. */
   public String passwd=null;
   /** Contact URL.
     * If not defined (default), it is formed by sip:local_user@host_address:host_port */
   public String contact_url=null;
   /** Local user name (used to build the contact url if not explitely defined) */
   public String contact_user="user";
   /** Path for the 'ua.jar' lib, used to retrive various UA media (gif, wav, etc.)
     * By default, it is used the "lib/ua.jar" folder */
   public static String ua_jar="lib/ua.jar";
   /** Path for the 'contacts.lst' file where save and load the VisualUA contacts
     * By default, it is used the "config/contacts.lst" folder */
   public static String contacts_file="contacts.lst";

   /** Whether registering with the registrar server */
   public boolean do_register=false;
   /** Whether unregistering the contact address */
   public boolean do_unregister=false;
   /** Whether unregistering all contacts beafore registering the contact address */
   public boolean do_unregister_all=false;
   /** Expires value. */
   public int expires=1800;

   /** Automatic call a remote user secified by the 'call_to' value.
     * Use value 'NONE' for manual calls (or let it undefined).  */
   public String call_to=null;      
   /** Whether it is in automatic respond mode */
   public boolean auto_accept=false;        
   /** Automatic hangup time (call duartion) in seconds.
     * time=0 corresponds to manual hangup mode */
   public int hangup_time=0;
   /** Redirect incoming call to the secified url.
     * Use value 'NONE' for not redirecting incoming calls (or let it undefined). */
   public String redirect_to=null;
   /** No offer in the invite */
   public boolean no_offer=false;
   
   /** Transfer calls to the secified url.
     * Use value 'NONE' for not transferring calls (or let it undefined). */
   public String transfer_to=null;
   /** Time waited beafore tranferring the call. Value '0' means no transfer. */
   public int transfer_time=0;
   /** Time waited beafore reinviting the remote party. Value '0' means no re-invite.  */
   public int re_invite_time=0;

   /** Whether using audio */
   public boolean audio=false;
   /** Whether using video */
   public boolean video=false;

   /** Whether playing in receive only mode */
   public boolean recv_only=false;
   /** Whether playing in send only mode */
   public boolean send_only=false;
   /** Whether playing a test tone in send only mode */
   public boolean send_tone=false;
   /** Audio file to be played */
   public String send_file=null;
   /** Audio file to be recorded */
   public String recv_file=null;

   /** Audio port */
   public int audio_port=21068;
   /** Audio avp */
   public int audio_avp=0;
   /** Audio codec */
   public String audio_codec="PCMU";
   /** Audio rate */
   public int audio_rate=8000;
   
   /** Video port */
   public int video_port=21070;
   /** Video avp */
   public int video_avp=17;

   /** Whether using JMF for audio/video streaming */
   public boolean use_jmf=false;
   /** Whether using RAT (Robust Audio Tool) as audio sender/receiver */
   public boolean use_rat=false;
   /** Whether using VIC (Video Conferencing Tool) as video sender/receiver */
   public boolean use_vic=false;
   /** RAT command-line executable */
   public String bin_rat="rat";
   /** VIC command-line executable */
   public String bin_vic="vic";
   
   // ************************** costructors *************************
   
   /** Costructs a void UserProfile */
   /*protected UserAgentProfile()
   {  // load SipStack first
      if (!SipStack.isInit()) SipStack.init();
   }*/

   /** Costructs a new UserProfile */
   public UserAgentProfile(String file)
   {  // load SipStack first
      if (!SipStack.isInit()) SipStack.init();
      // load configuration
      loadFile(file);    
      // post-load manipulation     
      if (call_to!=null && call_to.equalsIgnoreCase(Configure.NONE)) call_to=null;
      if (redirect_to!=null && redirect_to.equalsIgnoreCase(Configure.NONE)) redirect_to=null;
      if (transfer_to!=null && transfer_to.equalsIgnoreCase(Configure.NONE)) transfer_to=null;
      if (send_file!=null && send_file.equalsIgnoreCase(Configure.NONE)) send_file=null;
      if (recv_file!=null && recv_file.equalsIgnoreCase(Configure.NONE)) recv_file=null;
   }  

   // **************************** methods ***************************

   /** Parses a single line (loaded from the config file) */
   protected void parseLine(String line)
   {  String attribute;
      Parser par;
      int index=line.indexOf("=");
      if (index>0) {  attribute=line.substring(0,index).trim(); par=new Parser(line,index+1);  }
      else {  attribute=line; par=new Parser("");  }
              
      if (attribute.equals("from_url"))       { from_url=par.getRemainingString().trim(); return; }
      if (attribute.equals("passwd"))         { passwd=par.getRemainingString().trim(); return; }
      if (attribute.equals("contact_url"))    { contact_url=par.getRemainingString().trim(); return; }
      if (attribute.equals("contact_user"))   { contact_user=par.getString(); return; } 
      if (attribute.equals("ua_jar"))         { ua_jar=par.getStringUnquoted(); return; }      
      if (attribute.equals("contacts_file"))  { contacts_file=par.getStringUnquoted(); return; }      

      if (attribute.equals("do_register"))    { do_register=(par.getString().toLowerCase().startsWith("y")); return; }
      if (attribute.equals("do_unregister"))  { do_unregister=(par.getString().toLowerCase().startsWith("y")); return; }
      if (attribute.equals("do_unregister_all")) { do_unregister_all=(par.getString().toLowerCase().startsWith("y")); return; }
      //if (attribute.equals("expires"))        { expires=par.getInt(); return; } 

      if (attribute.equals("call_to"))     { call_to=par.getRemainingString().trim(); return; }
      if (attribute.equals("auto_accept"))    { auto_accept=(par.getString().toLowerCase().startsWith("y")); return; }
      if (attribute.equals("hangup_time"))    { hangup_time=par.getInt(); return; } 
      if (attribute.equals("redirect_to"))   { redirect_to=par.getRemainingString().trim(); return; }
      if (attribute.equals("no_offer"))       { no_offer=(par.getString().toLowerCase().startsWith("y")); return; }
      if (attribute.equals("transfer_to"))    { transfer_to=par.getRemainingString().trim(); return; }
      if (attribute.equals("transfer_time"))  { transfer_time=par.getInt(); return; } 
      if (attribute.equals("re_invite_time")) { re_invite_time=par.getInt(); return; } 

      if (attribute.equals("audio"))          { audio=(par.getString().toLowerCase().startsWith("y")); return; }
      if (attribute.equals("video"))          { video=(par.getString().toLowerCase().startsWith("y")); return; }
      if (attribute.equals("recv_only"))      { recv_only=(par.getString().toLowerCase().startsWith("y")); return; }
      if (attribute.equals("send_only"))      { send_only=(par.getString().toLowerCase().startsWith("y")); return; }
      if (attribute.equals("send_tone"))      { send_tone=(par.getString().toLowerCase().startsWith("y")); return; }
      if (attribute.equals("send_file"))      { send_file=par.getRemainingString().trim(); return; }
      if (attribute.equals("recv_file"))      { recv_file=par.getRemainingString().trim(); return; }

      if (attribute.equals("audio_port"))     { audio_port=par.getInt(); return; } 
      if (attribute.equals("audio_avp"))      { audio_avp=par.getInt(); return; } 
      if (attribute.equals("audio_codec"))    { audio_codec=par.getString(); return; } 
      if (attribute.equals("audio_rate"))     { audio_rate=par.getInt(); return; } 
      if (attribute.equals("video_port"))     { video_port=par.getInt(); return; } 
      if (attribute.equals("video_avp"))      { video_avp=par.getInt(); return; } 

      if (attribute.equals("use_jmf"))        { use_jmf=(par.getString().toLowerCase().startsWith("y")); return; }
      if (attribute.equals("use_rat"))        { use_rat=(par.getString().toLowerCase().startsWith("y")); return; }
      if (attribute.equals("bin_rat"))        { bin_rat=par.getStringUnquoted(); return; }
      if (attribute.equals("use_vic"))        { use_vic=(par.getString().toLowerCase().startsWith("y")); return; }
      if (attribute.equals("bin_vic"))        { bin_vic=par.getStringUnquoted(); return; }      
   }


   /** Converts the entire object into lines (to be saved into the config file) */
   protected String toLines()
   {  // currently not implemented..
      return contact_url;
   }
  
}
