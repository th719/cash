package th.cash.abg;

import java.text.NumberFormat;
import java.util.Locale;

public class ATrUtil 
{

  private NumberFormat num_fmt = NumberFormat.getInstance(Locale.ENGLISH);
  
  public ATrUtil()
  {
    num_fmt.setGroupingUsed(false);
    num_fmt.setMaximumFractionDigits(2);
  }

  // здесь длина полей !
  /*
  public String makeAuthReqStr(AuthReq r)
  {
    String mmes = FConst.FILL_CHAR +  ff(r.getMsgN(), 4) + r.getEntryMode() +
      ff(r.getCardNo(), 19) + ff(r.getExpDate(), 4) + ff(r.track2Data, 40) +
      ff( num_fmt.format(r.amount), 12) + ff(r.getTransDate(), 6) + ff(r.getTransTime(), 6) +
      r.transType + ff(r.getAuthCode(), 9) + ff(r.trmN, 3) +
      r.type + r.signOk;
    char ctrl_char = countControlChar(mmes);

    return mmes + ctrl_char + FConst.EOF_MARK;
  }

 */
  // исключительно разбор строки ответа без анализа флагов и параметров
 /*
  public AuthAns parseAuthReply(String s) throws Exception
  {
    int len = s.length();

    if (len != FConst.FIXED_AUTH_LEN)
      throw new Exception("Некорректна длина ответа");
  
    String ms = s.substring(0, len - 3);

    char cur_cc = countControlChar(ms);
    char scc = s.charAt(len - 3);

    if (cur_cc != scc)
      throw new Exception("Некорректна контрольная сумма");

    char sf, em, error_code;

    int sp = 0;
    
    String msg_n, card_no, exp_date, trans_date, trans_time, trans_id, 
      auth_code, msg, card_type, trm_id, trm_t, merch_n, resp_code;

    sf = ms.charAt(sp++);
    msg_n = ms.substring(sp, sp + 4); sp += 4;
    em = ms.charAt(sp++);

    card_no = ms.substring(sp, sp + 19); sp += 19;
    exp_date = ms.substring(sp, sp + 4); sp += 4;
    trans_date = ms.substring(sp, sp + 6); sp += 6;
    trans_time = ms.substring(sp, sp + 6); sp += 6;
    trans_id = ms.substring(sp, sp + 2); sp += 2;
    auth_code = ms.substring(sp, sp + 9); sp += 9;
    msg = ms.substring(sp, sp + 16); sp += 16;
    card_type = ms.substring(sp, sp + 2); sp += 2;
    trm_id = ms.substring(sp, sp + 8); sp += 8;
    trm_t = ms.substring(sp, sp + 4); sp += 4;

    merch_n = ms.substring(sp, sp + 15); sp += 15;

    error_code = ms.charAt(sp++);
    resp_code = ms.substring(sp, sp + 2); sp += 2;
    
    return new AuthAns(sf, msg_n, card_no, exp_date, trans_date, trans_time, 
      auth_code, trans_id, msg, card_type, trm_id, merch_n, error_code, resp_code);
  }
*/
  // что может из этого получиться - совсем непонятно
  private char countControlChar(String s)
  {
    char cc = (char)0;
    for (int i = 0; i < s.length(); i++) cc ^= s.charAt(i) | (i % 7);
    return (char) ((cc & 0x3F) | 0x0030);
  }

  // 2 вариант
  private byte countControlChar(byte[] sb)
  {
    byte cb = (byte)0;
    for (int i = 0; i < sb.length; i++) cb ^= sb[i] | (i % 7);
    return (byte)((cb & (byte)0x3F) | (byte)0x30);
  }

  private String ff(String s, int len)
  {
    return _fill(s, FConst.FILL_CHAR, len);
  }
  // заполнить нужными символами до указанной длины
  private String _fill(String s, char fc, int len)
  {
    if (s == null) s = "";
    int slen = s.length();
    if (slen == len) return s; else
      if (slen > len) return s.substring( slen - len, slen); else
        {
          StringBuffer sb = new StringBuffer();
          for (int i = slen; i<len; i++) sb.append(fc);
          return s + sb; // выровнено по правому краю
        }
   
  }
  
  
} 