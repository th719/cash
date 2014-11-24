package th.cash.fr;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class FrUtil 
{
  // ***************************************************************************
  
  private static String _strFromBytes(byte[] buf, int offs, int num) throws UnsupportedEncodingException
  {
    int num2 = 0;
    // фактическая длина строки
    while (num2 < num && buf[offs + num2] != (byte)0x00) num2++;
    return new String(buf, offs, num2, FrConst.FR_STR_ENC);
  }

  public static String strFromBytes(byte[] buf, int offs, int num) throws FrException
  {
    try
    {
      return _strFromBytes(buf, offs, num);
    } catch (UnsupportedEncodingException ex)
    {
      FrException fex = new FrException("Неподдерживаемая кодировка символов", -11, 1);
      fex.setNestedException(ex);
      throw fex;
    }
  }




  public static long longFromBytes(byte[] buf, int offs, int nb)
  {
    long res = 0;
    int i = nb - 1;
    while (i >= 0)
    {
      res = res << 8;
      res = res | (0x00000000000000FF & buf[offs + i]);
      i--;
    }
    return res;
  }

  public static int intFromBytes(byte[] buf, int offs, int nb)
  {
    int res = 0;
    int i = nb - 1;
    while (i >= 0)
    {
      res = res << 8;
      res = res | (0x000000FF & buf[offs + i]);  
      i--;
    }
    return res;
  }


  public static byte byteFromBytes(byte[] buf, int offs)
  {
    return buf[offs];
  }

  // ***************************************************************************

  public static void byteToBytes(byte[] buf, byte val, int offs)
  {
    buf[offs] = val;
  }

  public static void intToBytes(byte[] buf, int val, int nb, int offs)
  {
    for (int i = 0; i < nb; i++)
    {
      buf[offs + i] = (byte) (val & 0xFF);
      val = val >>> 8;
    }
  }

  public static void longToBytes(byte[] buf, long val, int nb, int offs)
  {
    for (int i = 0; i < nb; i++)
    {
      buf[offs + i] = (byte) (val & 0xFF);
      val = val >>> 8;
    }
  }

  private static void _strToBytes(byte[] buf, String val, int max_len, int offs) throws UnsupportedEncodingException
  {
    if (val == null) return;
    byte[] bstr = val.getBytes(FrConst.FR_STR_ENC);
    int num_bytes = Math.min(bstr.length, max_len);
    int i = 0;
    while (i < num_bytes)
    {
      buf[offs + i] = bstr[i];
      i++;
    }
    // остаток до max_len заполняем нулями
    while (i < max_len)
    {
      buf[offs + i] = (byte)0;
      i++;
    }
//    for (int i = 0; i < num_bytes; i++)
//      buf[offs + i] = bstr[i];
  }

  public static void strToBytes(byte[] buf, String val, int max_len, int offs) throws FrException
  {
    try {
      _strToBytes(buf, val, max_len, offs);      
    } catch (UnsupportedEncodingException ex)
    {
      FrException fex = new FrException("Неподдерживаемая кодировка символов", -11, 1);
      fex.setNestedException(ex);
      throw fex;
    }
  }

  public static byte[] getSelBytes(byte[] buf, int nb, int offs)
  {
    byte[] res = new byte[nb];
    for (int i = 0; i < nb; i++)
      res[i] = buf[offs + i];
    return res;
  }

  /**
   ********************* 
   * Date
   */

   // date - 3 байта  ДД-ММ-ГГ
   // time - 3 байта  Время (3 байта) ЧЧ-ММ-СС
  public static void dateToBytes(Date dt, byte[] date, byte[] time)
  {
    if (dt == null || date == null || time == null) return;
    SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyHHmmss");
    String s = sdf.format(dt);
//    System.out.println("date - " + s);
    int i;
    for(i = 0; i < 3; i++)
      date[i] = Byte.parseByte(s.substring(2 * i, 2 * (i + 1)));
    for (i = 3; i < 6; i++)
      time[i - 3] = Byte.parseByte(s.substring(2 * i, 2 * (i + 1)));
  }

  public static String get2SigStr(byte b)
  {
    return (b < 10 ? "0" : "") + String.valueOf(b);
  }
  private static Date _dateFromBytes(byte[] date, byte[] time) throws ParseException
  {
    StringBuffer sb = new StringBuffer();
    int i;
    for (i = 0; i < 3; i++) sb.append(get2SigStr(date[i]));
    for (i = 0; i < 3; i++) sb.append(get2SigStr(time[i]));

    SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyHHmmss");
    Date res = sdf.parse(sb.toString());
    return res;
  }

  public static Date dateFromBytes(byte[] date, byte[] time) throws FrException
  {
    try{
      return _dateFromBytes(date, time);
    } catch (ParseException ex)
    {
      FrException fex = new FrException("Ошибка при разборе даты", -12, 1);
      fex.setNestedException(ex);
      throw fex;
    }
  }


}