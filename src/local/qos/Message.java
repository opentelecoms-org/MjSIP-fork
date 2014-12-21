package local.qos;


import org.zoolu.sip.header.Header;
import org.zoolu.sip.header.MultipleHeader;
import org.zoolu.sip.message.*;


import java.net.DatagramSocket;

/** Message Extends class sipx.message.Message
  * handling:
  * <p> header OoS-Info (draft <veltri-sip-qsip>)
  */
public class Message extends org.zoolu.sip.message.Message
{
   /** Returrns a new empty Message */
   public Message() { super(); }

   /** Converts the String <i>str</i> into a new Message */
   public Message(String str)
   {  super(str);
   }

   /** Makes a clone of Message <i>msg</i> */
   public Message(org.zoolu.sip.message.Message msg)
   {  super(msg);
   }
   
   /** Receives the next UDP datagram and returns a new SIP Message */
   /*public Message(DatagramSocket socket)
   {  super(socket);
   }*/

   // ****************************** Extensions ******************************

   // QoSDescriptionHeader
   
   /** Whether the message has the QoSDescriptionHeader *   
   public boolean hasQoSDescriptionHeader()
   {  return hasHeader(SipHeaders.QoSDescription);
   }*/
   /** Gets QoSDescriptionHeader *
   public QoSDescriptionHeader getQoSDescriptionHeader()
   {  MultipleHeader mqh=new MultipleHeader(getHeaders(SipHeaders.QoSDescription));
      mqh.setCommaSeparated(true);
      return new QoSDescriptionHeader(mqh.toHeader());
   }*/
   /** Sets QoSDescriptionHeader *
   public void setQoSDescriptionHeader(QoSDescriptionHeader qh) 
   {  setHeader(qh);
   }*/
   /** Removes QoSDescriptionHeader from Message (if it exists) *
   public void removeQoSDescriptionHeader() 
   {  removeHeader(SipHeaders.QoSDescription);
   }*/


   // QoSProxyHeader

   /** Whether the message has the QoSProxyHeader */   
   public boolean hasQoSProxyHeader()
   {  return hasHeader(SipHeaders.QoSProxy);
   }
   /** Gets QoSProxyHeader */
   public QoSProxyHeader getQoSProxyHeader()
   {  Header h=getHeader(SipHeaders.QoSProxy);
      if (h==null) return null;
      return new QoSProxyHeader(h);
   }  

   /** Sets QoSProxyHeader */
   public void setQoSProxyHeader(QoSProxyHeader qh) 
   {  setHeader(qh);
   } 
   /** Removes QoSProxyHeader from Message (if it exists) */
   public void removeQoSProxyHeader() 
   {  removeHeader(SipHeaders.QoSProxy);
   }

}