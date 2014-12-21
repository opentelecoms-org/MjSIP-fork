package local.qos;


import org.zoolu.tools.Parser;


/** RSpec is a data structure specifing the requested bandwidth R
  * and (optionally) a Slack term S.
  * <p>
  * It implements the RSpec structure specified by the
  * Resource ReSerVation Protocol (RSVP) protocol.
  */
public class RSpec
{
   /** Requested bandwidth [b/s] */
   public float R;
   /** RSVP Slack term */
   public int S;

   /** Creates a new void RSpec */
   public RSpec()
   {  R=0; S=0;
   }

   /** Creates a new RSpec */
   public RSpec(float R_)
   {  R=R_; S=0;
   }

   /** Creates a new RSpec */
   public RSpec(float R_, int S_)
   {  R=R_; S=S_;
   }

   /** Creates a new RSpec */
   public  RSpec(RSpec rspec)
   {  R=rspec.R; S=rspec.S;
   }
   
   /** Parses a String searching for a RSpec */
   public static RSpec parseRSpec(String str)
   {  if (str==null) return null;
      char[] separators={ '/' , ' ' };
      RSpec rspec=new RSpec();
      Parser par=new Parser(str);
      String aux;
      aux=par.getWord(separators);
      if (!aux.equals("*")) rspec.R=Float.parseFloat(aux);
      if (par.hasMore() && par.getChar()=='/')
      {  aux=par.getWord(separators);
         if (!aux.equals("*")) rspec.S=Integer.parseInt(aux);
      }
      return rspec;
   }

   /** Gets a String value for the RSpec */
   public String toString()
   {  String str="";
      if (R>0) str+=R; else str+="*";
      if (S>0) str+="/"+S; /* else str+=" *" */;
      return str;
   }
}
