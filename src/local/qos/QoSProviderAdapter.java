package local.qos;


/** QoSProviderAdapter is a void implementation of QoSProvider   
  */
public class QoSProviderAdapter implements QoSProvider
{

   /** Create a dummy QoSProviderAdapter */ 
   public QoSProviderAdapter()
   {
   }

   /** Admission control. Acccepts all flows.
     * Returns the entire QoSDescriptor. */
   public QoSDescriptor qosAdmissionControl(QoSDescriptor qd)
   {  return qd;
   }

   /** Void implementation of qosPreSetup()
     * Returns <b>true</b> */
   public boolean qosPreSetup(QoSDescriptor qd, String id)
   {  return true;
   }
   
   /** Void implementation of qosSetup()
     * Returns <b>true</b> */
   public boolean qosSetup(QoSDescriptor qd, String id)
   {  return true;
   }
   
   /** Void implementation of qosTeardown()
     * Returns <b>true</b> */
   public boolean qosTeardown(String id)
   {  return true;
   }
}
