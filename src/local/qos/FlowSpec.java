package local.qos;


/** FlowSpec is a data structure that specifies
  * the source and destination end points, the TSpec and Rspec.
  */
public class FlowSpec
{
   /** Source EndPoint */
   public EndPoint src=null;
   /** Destination EndPoint */
   public EndPoint dest=null;

   /** TSpec */
   public TSpec tspec=null;
   /** RSpec */
   public RSpec rspec=null;
   
   /** Creates a new FlowSpec */
   public FlowSpec(EndPoint e_src, EndPoint e_dest, TSpec t_spec, RSpec r_spec)
   {  src=e_src; dest=e_dest; tspec=t_spec; rspec=r_spec;
   }

   /** Creates a new FlowSpec */
   public FlowSpec(FlowSpec f)
   {  src=f.src; dest=f.dest; tspec=f.tspec; rspec=f.rspec;
   }
   
   /** Gets a String value for the RSpec */
   public String toString()
   {  String str="";
      if (src!=null) str="src=\""+src+"\"";
      if (dest!=null) {  if (str.length()>0) str+="; "; str+="dest=\""+dest+"\"";  }
      if (tspec!=null) {  if (str.length()>0) str+="; "; str+="tspec=\""+tspec+"\"";  }
      if (rspec!=null) {  if (str.length()>0) str+="; "; str+="rspec=\""+rspec+"\"";  }
      return str;
   }
}
