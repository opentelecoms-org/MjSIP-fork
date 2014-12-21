package local.qos;


/** QoSProvider is a standard interface to access to QoS service
  * provided by different implementation-dependant mechanisms   
  */
public interface QoSProvider
{
   /** Admission control.
     * Returns a QoSDescriptor with the admitted flows. */
   public QoSDescriptor qosAdmissionControl(QoSDescriptor qd);

   /** Sets up a pre-QoS state.
     * This can be useful for setting an automatic accept mode (allerting)
     * for incoming qos requests (e.g. in case of a callee RSVP daemon) */
   public boolean qosPreSetup(QoSDescriptor qd, String id);
   
   /** Sets up a QoS reservation.
     */
   public boolean qosSetup(QoSDescriptor qd, String id);
   
   /** Tears down QoS reservation.
     */
   public boolean qosTeardown(String id);
}
