package th.cash.model;

import th.cash.fr.Hex;

/**
 * Декодирование пароля кассира
 */
public class PwdUserDecode 
{
  public String decode(String cpwd) throws Exception
  {
    byte[] data = th.cash.fr.Hex.hexStringToByteArray(cpwd);
    for (int i = 0; i < data.length; i++)
      data[i] = (byte)(255 - data[i]);
    return new String(data, "ASCII");
  }
}