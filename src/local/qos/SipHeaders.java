package local.qos;


/** SipHeaders extends class sip.header.SipHeaders */
public class SipHeaders extends org.zoolu.sip.header.SipHeaders
{

   //****************************** Extensions *******************************/

   /** String "QoS-Info" */
   //public static final String QoSInfo="QoS-Info";
   
   /** Whether <i>str</i> is "QoS-Info" */
   //public static boolean isQoSInfo(String str) { return same(str,QoSInfo); }

   /** String "QoS-Proxy" */
   public static final String QoSProxy="QoS-Proxy";
   
   /** Whether <i>str</i> is "QoS-Proxy" */
   public static boolean isQoSProxy(String str) { return same(str,QoSProxy); }

   /** String "QoS-Description" */
   //public static final String QoSDescription="QoS-Description";
   
   /** Whether <i>str</i> is "QoS-Description" */
   //public static boolean isQoSDescription(String str) { return same(str,QoSDescription); }

}
