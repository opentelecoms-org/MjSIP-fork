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
import java.applet.*;

/*
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
*/


/** Simple Applet-based SIP user agent (UA). */
public class AppletUA extends Applet
{

   //Initialize the applet (when running as applet)
   public void init()
   {  
      SipStack.init();
      SipStack.debug_level=0;
      SipProvider sip_provider=new SipProvider("7.7.7.7",5077);
      UserAgentProfile user_profile=new UserAgentProfile();
      user_profile.ua_jar="mjapplet.jar";
      new GraphicalUA(sip_provider,user_profile);
   }

}