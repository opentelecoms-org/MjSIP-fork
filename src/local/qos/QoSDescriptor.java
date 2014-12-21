package local.qos;


import org.zoolu.sdp.*;
import org.zoolu.tools.Parser;
import java.util.Vector;
import java.util.Enumeration;


/** QoSDescriptor allows the characterization of QoS sessions. */
public class QoSDescriptor
{
   /** Vector of flows (?) */
   public Vector flows=new Vector();

   /** Sets a void QoSDescriptor */
   public QoSDescriptor()
   { 
   }
   
   /** Creates a new QoSDescriptor from a SDP */
   public QoSDescriptor(SessionDescriptor sdp)
   {
      String media_addr=sdp.getConnection().getAddress();
      for (Enumeration i=sdp.getMediaDescriptors().elements(); i.hasMoreElements(); )
      {  MediaField media=((MediaDescriptor)i.nextElement()).getMedia();          
         String media_type=media.getMedia();
         int media_port=media.getPort();
         String media_proto=media.getTransport();
         if (media_proto.indexOf("udp")>=0) media_proto="udp";
         else
         if (media_proto.indexOf("tcp")>=0) media_proto="tcp";
         else
         if (media_proto.indexOf("rtp")>=0) media_proto="udp";
         else
         if (media_proto.indexOf("http")>=0) media_proto="tcp";
         else
         if (media_proto.indexOf("tcp")>=0) media_proto="tcp";
         else
            media_proto="udp"; // default protocol
                                                           
         EndPoint dest=new EndPoint(media_addr,media_proto,media_port,null/*,null*/);
         TSpec tspec;
         if (media_type.equals("audio")) tspec=new TSpec(8000,100000,6000,48,1500); // r=64kbps p=800kbps           
         else
         if (media_type.equals("video")) tspec=new TSpec(150000,1000000,60000,100,1500); // r=1.4Mbps p=8Mbps
         else
         tspec=new TSpec(13000,200000,6000,100,1500); // r=104kbps p=1600kbps  
                   
         FlowSpec flow=new FlowSpec(null,dest,tspec,null);
         addFlow(flow);
      }
   }

   public void addFlow(FlowSpec f)
   {  flows.addElement(f);
   }
   
   public Vector getFlows()
   {  return flows;
   }
   
   public String toString()
   {  String str="";
      for (int i=0; i<flows.size(); i++)
         str+=((FlowSpec)flows.elementAt(i)).toString()+"\r\n";
      return str;
   }
}
