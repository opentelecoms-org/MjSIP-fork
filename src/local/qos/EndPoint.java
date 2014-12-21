package local.qos;


import org.zoolu.tools.Parser;


/** EndPoint caracterizes an application end-point.
  * <p> It collects the followith data:
  * <br> o ip address,
  * <br> o transport protocol,
  * <br> o port, and
  * <br> o default edge router. */
public class EndPoint
{
   public String addr=null;
   public int port=0;
   public String proto=null;
   public String er=null;
   //public String domain=null;
   
   public EndPoint()
   {
   }

   public EndPoint(String end_addr, String end_proto, int end_port, String edge_router/*, String domain_name*/)
   {  addr=end_addr; proto=end_proto; port=end_port; er=edge_router; /*domain=domain_name;*/
   }

   public EndPoint(EndPoint ep)
   {  addr=ep.addr; proto=ep.proto; port=ep.port; er=ep.er; /*domain=ep.domain;*/
   }

   public String toString()
   {  String str="ep=";
      if (addr!=null) str+=addr; else str+="0.0.0.0";
      if (port>0) str+=":"+port;
      if (proto!=null) str+="/"+proto;
      if (er!=null) str+=",er="+er;
      //if (domain!=null) str+=",domain="+domain;
      return str;
   }

   public static EndPoint parseEndPoint(String str)
   {  EndPoint e=new EndPoint();
      Parser par=new Parser(str);
      int equal=par.indexOf('=');
      while (equal>=0)
      {  String param=par.getString(equal-par.getPos()).trim();
         String value;
         int comma=par.skipChar().indexOf(',');
         if (comma>0) {  value=par.getString(comma-par.getPos()).trim(); par.skipChar();  }
         else value=par.getString().trim();
         //if (param.equals("domain")) e.domain=value;
         if (param.equals("er")) e.er=value;
         if (param.equals("ep"))
         {  Parser url_par=new Parser(value);
            // address[:port][/proto]
            int colon=url_par.skipChar().indexOf(':');
            int slash=url_par.skipChar().indexOf('/');
            if (colon>0)
            {  e.addr=url_par.getString(colon-url_par.getPos());
               if (slash>0)
               {  e.port=Integer.parseInt((url_par.skipChar().getString(slash-url_par.getPos())));
                  e.proto=url_par.skipChar().getString();
               }
               else e.port=url_par.skipChar().getInt();
            }
            else
            {  if (slash>0)
               {  e.addr=url_par.getString(slash-url_par.getPos());
                  e.proto=url_par.skipChar().getString();
               }
               else
               {  e.addr=url_par.getString();
               }
            }
         }
         equal=par.indexOf('=');
      }
      return e;
   }
}