package th.cash.model;

import java.util.Date;
import java.util.Calendar;

import java.text.SimpleDateFormat;
import java.text.*;

public class SecSaleRule 
{
  private int section;
  private int h1, m1, h2, m2;
  private String com;
  private boolean res;
  
  
  public SecSaleRule(String descr) throws ParseException
  {
    String s;

    s = getTagValue(descr, "section");
    section = s == null ? -1 : Integer.parseInt(s);

    s = getTagValue(descr, "time");
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    
    Date d = sdf.parse(s.substring(0, 5));

    Calendar cl = Calendar.getInstance();
    cl.setTime(d);
    h1 = cl.get(Calendar.HOUR_OF_DAY); m1 = cl.get(Calendar.MINUTE);

    d = sdf.parse(s.substring(6, 11));
    cl.setTime(d);
    h2 = cl.get(Calendar.HOUR_OF_DAY); m2 = cl.get(Calendar.MINUTE);
    

    s = getTagValue(descr, "result");
    res = s == null ? true : Boolean.valueOf(s).booleanValue();

    com = getTagValue(descr, "coment");
  }

  private String getTagValue(String text, String tagn)
  {
    String res = null;
    int tindex = text.indexOf(tagn);
    if (tindex >= 0)
    {
      tindex += tagn.length();
      tindex = text.indexOf('=', tindex);
      if (tindex >= 0)
      {
        tindex = text.indexOf('"', tindex);
        if (tindex >= 0)
        {
          int endindex = text.indexOf('"', tindex + 1);
          res = text.substring(tindex + 1, endindex);
        }
      }
    }
    return res;
  }

  public boolean isSaleEnabled(GoodMain g)
  {
    Date cur_date = new Date();
    Calendar cl = Calendar.getInstance();
    cl.setTime(cur_date);

    Calendar cdr1 = Calendar.getInstance();
    cdr1.setTime(cur_date);
    cdr1.set(Calendar.HOUR_OF_DAY, h1);
    cdr1.set(Calendar.MINUTE, m1);

    Calendar cdr2 = Calendar.getInstance();
    cdr2.setTime(cur_date);
    cdr2.set(Calendar.HOUR_OF_DAY, h2);
    cdr2.set(Calendar.MINUTE, m2);

    boolean cdr_w = cl.after(cdr1) && cl.before(cdr2);
    boolean eq_section = g.getSection() != null && g.getSection().intValue() == section;
    return eq_section ? (cdr_w ? res : !res) : true;
  }

  public String getComent()
  {
    return com;
  }
}