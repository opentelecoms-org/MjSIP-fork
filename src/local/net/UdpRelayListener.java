package local.net;


/** Listener for UdpRelay.
  */
public interface UdpRelayListener
{
   /** When source address changes. */
   public void onUdpRelaySourceChanged(UdpRelay udp_relay, String src_addr, int src_port);

   /** When UdpRelay stops relaying UDP datagrams. */
   public void onUdpRelayTerminated(UdpRelay udp_relay);   
}
