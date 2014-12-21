package local.qos;


import org.zoolu.tools.Parser;


/** TSpec is a data structure specifing the token bucket parameters (r,p,b)
  * and the min and max MTU size (m,M)
  * <p>
  * It implements the TSpec structure specified by the
  * Resource ReSerVation Protocol (RSVP) protocol.
  * <p>
  * r and p rates are expressed in byte/sec 
  * <br> b, m, and M are expressed in byte 
  */
public class TSpec
{
	/** Token bucket mean rate [B/s] */
	public float r;
   /** Token bucket peak rate [B/s] */
   public float p;
   /** Bucket size [B] */
   public float b;
   /** Minimum packet size [B] */
   public int m;
   /** Maximum packet size [B] */
   public int M;
	
   /** Creates a new void TSpec */
   public TSpec()
   {
      r = p = b = (float)0.0;
      m = M = 0;
   }
	
   /** Creates a new TSpec */
   public TSpec(float r_, float p_, float b_, int m_, int M_)
   { 
      r=r_; p=p_; b=b_; m=m_; M=M_;
   }
   
   /** Creates a new TSpec */
   public TSpec(TSpec tspec)
   { r=tspec.r; p=tspec.p; b=tspec.b; m=tspec.m; M=tspec.M;
   }
   
   /** Parses a String searching for a TSpec */
   public static TSpec parseTSpec(String str)
   {  if (str==null) return null;
      char[] separators={ '/' , ' ' };
      TSpec tspec=new TSpec();
      Parser par=new Parser(str);
      String aux;
      aux=par.getWord(separators);
      if (!aux.equals("*")) tspec.r=Float.parseFloat(aux);
      aux=par.skipChar().getWord(separators);
      if (!aux.equals("*")) tspec.p=Float.parseFloat(aux);
      aux=par.skipChar().getWord(separators);
      if (!aux.equals("*")) tspec.b=Float.parseFloat(aux);
      if (par.hasMore() && par.getChar()=='/')
      {  aux=par.getWord(separators);
         if(!aux.equals("*")) tspec.m=Integer.parseInt(aux);
         aux=par.skipChar().getWord(separators);
         if(!aux.equals("*")) tspec.M=Integer.parseInt(aux);
      }
      return tspec;
   }

   /** Gets a String value for the TSpec */
   public String toString()
   {  String str="";
      if (r>0) str+=r+"/"; else str+="*/";
      if (p>0) str+=p+"/"; else str+="*/";
      if (b>0) str+=b; else str+="*";
      if (m>0 || M>0)
      {  if (m>0) str+="/"+m; else str+="/*";
         if (M>0) str+="/"+M; else str+="/*";
      }
      return str;
   }
}
