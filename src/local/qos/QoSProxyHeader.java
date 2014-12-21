package local.qos;


import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.header.Header;
import org.zoolu.sip.header.NameAddressHeader;


/** QoS-Proxy header is a SIP extension for QoS support (Q-SIP).
  * It is current a proposed draft, and it is defined in <draft-veltri-sip-qsip-02>.
  */
public class QoSProxyHeader extends NameAddressHeader
{
   /** Creates a new QoSProxyHeader. */
   public QoSProxyHeader()
   {  super(new Header(SipHeaders.QoSProxy,null));
   }

   /** Creates a new QoSProxyHeader. */
   public QoSProxyHeader(NameAddress name_address)
   {  super(SipHeaders.QoSProxy,name_address);
   }

   /** Creates a new QoSProxyHeader. */
   public QoSProxyHeader(String value)
   {  super(new Header(SipHeaders.QoSProxy,value));
   }

   /** Creates a new QoSProxyHeader. */
   public QoSProxyHeader(Header hd)
   {  super(hd);
   }

   // ******************** already defined parameters ********************


   /** Whether there is the 'realm' parameter */
   public boolean hasRealm()
   {  return hasParameter("realm");
   }
   /** Gets the 'domain' parameter */
   public String getRealm()
   {  return getParameter("realm");
   }
   /** Sets the 'domain' parameter */
   public void setRealm(String value)
   {  setParameter("realm",value);
   }


   /** Whether there is the 'er' parameter */
   public boolean hasER()
   {  return hasParameter("er");
   }
   /** Gets the 'er' parameter */
   public String getER()
   {  return getParameter("er");
   }
   /** Sets the 'er' parameter */
   public void setER(String value)
   {  setParameter("er",value);
   }


   /** Whether there is the 'mode' parameter */
   public boolean hasMode()
   {  return hasParameter("mode");
   }
   /** Gets the 'mode' parameter */
   public String getMode()
   {  return getParameter("mode");
   }
   /** Sets the 'mode' parameter */
   public void setMode(String value)
   {  setParameter("mode",value);
   }


   /** Whether there is the 'dir' parameter */
   public boolean hasDirection()
   {  return hasParameter("dir");
   }
   /** Gets the 'dir' parameter */
   public String getDirection()
   {  return getParameter("dir");
   }
   /** Sets the 'dir' parameter */
   public void setDirection(String value)
   {  setParameter("dir",value);
   }
   
}
