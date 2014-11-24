package th.cash.abg;

import th.cash.ui.paym.*;
import java.io.*;
import java.util.Date;

import java.text.SimpleDateFormat;

public class ACEmulator 
{
  public ACEmulator()
  {
  }
  private final static String TRM_NUM = "011";
  
  private static File searchTrFile(String dir, String pref) throws IOException
  {
    File tr_dir = new File(dir);
    File last_file = null;


    File[] bfs = tr_dir.listFiles(new SPFilter(pref, "." + TRM_NUM));

    if (bfs != null && bfs.length > 0)
    {
      String s, cur_fn = "";
      
      for (int i = 0; i < bfs.length; i++)
      {
        s = bfs[i].getName();
        if (s.compareTo(cur_fn) > 0) { cur_fn = s; last_file = bfs[i]; }
      }

    }
    return last_file;
  }


  private static String _fill(String s, char fc, int len)
  {
    if (s == null) s = "";
    int slen = s.length();
    if (slen == len) return s; else
      if (slen > len) return s.substring( slen - len, slen); else
        {
          StringBuffer sb = new StringBuffer();
          for (int i = slen; i<len; i++) sb.append(fc);
          return sb + s; // выровнено по правому краю
        }
   
  }

  
  private static void waitForReport() 
  {
    try
    {

       while (true)
       {
         Thread.currentThread().sleep(1000);
         File in_f = searchTrFile("/home/lazarev/ke/bank/remote", "R");
         

         if (in_f != null && in_f.exists())
         {
           System.out.println("Processing file '" + in_f.getAbsolutePath() + "'");

           File rep_file = new File(in_f.getParent() + File.separator + "T" + in_f.getName().substring(1));

           System.out.println("Write to " + rep_file); 

           FileInputStream fis = new FileInputStream(in_f);
           BufferedReader br = new BufferedReader(new InputStreamReader(fis));

           FileOutputStream fos = new FileOutputStream(rep_file, false); 
           BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(fos));

//           BufferedWriter wr = new BufferedWriter( new FileWriter(rep_file, false));


           String trmn, cardno, expdate, samount, authcode, msgn, stransdate, stt;

           String line;
           SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd,HHmmss");

           SimpleDateFormat df0 = new SimpleDateFormat("yyyyMMdd");
           SimpleDateFormat df1 = new SimpleDateFormat("yyMM");


           SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
           SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");

           double amount;
           Date d;

           String rline;

           int lcnt = 0;

           while ((line = br.readLine()) != null )
           {
              
              lcnt++;
              
              trmn = line.substring(0, 3).trim();
              cardno = line.substring(4, 23).trim();
              expdate = line.substring(24, 32).trim();
              samount = line.substring(33, 45).trim();
              amount = Double.parseDouble(samount);
              authcode = line.substring(46, 52).trim();
              msgn = line.substring(53, 57).trim();
              stransdate = line.substring(58, 73); // вместе дата и время
              stt = line.substring(74, 75);

              d = sdf.parse(stransdate);
              


              rline = "*" + ' ' + 
                _fill(String.valueOf(lcnt) , '0', 4) + ' ' + 
                _fill(cardno, ' ' , 19) + ' ' + 
                df1.format(df0.parse(expdate)) + ' ' + 
                _fill(samount, ' ', 12) + ' ' + 
                _fill(authcode, ' ' , 9) + ' ' + 
                df.format(d) + ' ' + 
                tf.format(d) + ' ' + 
                _fill("11", ' ', 9) + ' ' + 
                _fill("194", ' ', 15);

              System.out.println(line + " -> " + rline);


              wr.write(rline); wr.newLine();
           }

           fis.close();

           wr.flush();
           wr.close();

           in_f.delete();
         }

       }
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
    
  }

  private static void waitForAuth()
  {
     int tr_cnt = 1, acode_cnt = 1000;

     try
     {

     while (true)
     {
       Thread.currentThread().sleep(1000);
       File in_f = new File("/home/lazarev/ke/bank/remote/$int_i$.011");
       if (in_f.exists())
       {
         System.out.println("Processing file '" + in_f.getAbsolutePath() + "'");
         AuthData in_data = AuthData.readFromFile(in_f.getAbsolutePath());

         Date cur_date = new Date();
         SimpleDateFormat sdfd = new SimpleDateFormat("ddMMyy");
         SimpleDateFormat sdft = new SimpleDateFormat("HHmmss");

          String s = in_data.getValue(6);
          String cnum = null, expdate = null;   

          int ieq = s.indexOf('=');
          if (ieq > 0) cnum = s.substring(1, ieq);
          int iqw = s.indexOf('?');
          if (iqw > ieq) expdate = s.substring(ieq + 1, ieq + 5);

         double amount = Double.parseDouble( in_data.getValue(7) );

         long rres = Math.round(Math.random());
         long rf20 = 0;
         long if26 = 0;
         String sf20 = null;
         if (rres == 1)
         {
           rf20 = Math.round(Math.random() * 10 - 0.5);
           sf20 = new Long(rf20).toString();
           
           if26 = Math.round(Math.random() * FConst.F26_DESCR.length - 0.5);
         }
         

         AuthData out_data = AuthData.createAuthData(
           rres == 0 ? FConst.SF_OK : FConst.SF_DENY, 
           null,
           sf20 == null ? FConst.FILL_CHAR : sf20.charAt(sf20.length() - 1), 
           FConst.F26_DESCR[(int)if26][0],  
           String.valueOf( ++tr_cnt ), 
           in_data.getCharValue(3) == FConst.EM_KEY, 
           cnum, 
           expdate, 
           sdfd.format( cur_date ), 
           sdft.format( cur_date),
           String.valueOf( ++acode_cnt ), 
           in_data.getValue(6), 
           in_data.getCharValue(10), 
           amount, 
           in_data.getValue(15) );

        AuthData.writeToFile(out_data, "/home/lazarev/ke/bank/remote/$int_o$.011");
        
        in_f.delete();

        out_data.print();
       }
     }
    
     } catch (Exception ex)
     {
       ex.printStackTrace();
     }
    
  }

  public static void main(String[] args)
  {

    waitForReport();
//    waitForAuth();
  }
}