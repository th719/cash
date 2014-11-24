package th.cash.fr;


import java.util.*;

// The String/Hex conversion methods
public class Hex 
{

  private static char[] hexDigits = 
    {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

  public static String byteToHexString(byte b)
  {
    int n = b;
    if (n<0) n = 256 + n;
    int d1 = n / 16;
    int d2 = n % 16;
    String res = ""+hexDigits[d1] + hexDigits[d2];
    return res;
  }

  public static String byteArrayToHexString(byte[] b)
  {
    return byteArrayToHexString(b, 0, b.length);
//    String result = "";
//    for (int i=0; i<b.length; i++)
//      result += byteToHexString(b[i]);
//    return result;
  }

  public static String byteArrayToHexString(byte[] b, int offs, int len)
  {
    String result = "";
    for (int i=offs; i<offs + len; i++)
      result += byteToHexString(b[i]);
    return result;
  }

  private static byte hexCharToByte(char ch) throws Exception
  {
    byte res = 0;  
    boolean d1,d2;
    d1 = ch >= '0' && ch <= '9';
    d2 = ch >= 'A' && ch <= 'F';
    if (d1 || d2) 
    {
      res = (byte)((d1)? ch - '0' : 10 + ch - 'A');
    }
      else throw new Exception("Incorrect HEX format");
    return res;
  }

  public static byte[] hexStringToByteArray(String s) throws Exception
  {
    int len = s.length() / 2;
    byte[] res = new byte[len];

    char lhd,hhd;

    boolean corr; 

    for (int i=0; i < len; i++)
    {
      hhd = Character.toUpperCase( s.charAt(2*i) );
      lhd = Character.toUpperCase( s.charAt(2*i+1));
      res[i] = (byte)(hexCharToByte(hhd)*16+hexCharToByte(lhd));
    }

    return res;
  }

}