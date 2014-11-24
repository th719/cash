package th.cash.abg;

import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.Locale;


import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;

// общие данные авторизации 

public class AuthData 
{

/*
  protected char   statusFlag = FConst.FILL_CHAR;   // флаг 
  protected String msgN       = null;               // уникальный номер транзакции [4]
  protected char   entryMode  = FConst.FILL_CHAR;   // тип ввода [1], K/R
  protected String cardNo     = null;               // номер карты [..19]
  protected String expDate    = null;               // срок действия карты [4], YYMM
  protected String transDate  = null;                 // дата DDMMYY
  protected String transTime  = null;                 // и время транзакции  HHMMSS
  protected String authCode   = null;                 // Код авторизации (оффлайн транзакция) [9]
  

  public char   getStatusFlag() { return statusFlag; }

  public String getMsgN()       { return msgN; }

  public char   getEntryMode()  { return entryMode; }

  public String getCardNo()     { return cardNo; }

  public String getExpDate()    { return expDate; }

  public String getTransDate()  { return transDate; }

  public String getTransTime()  { return transTime; }

  public String getAuthCode()   { return authCode; }

*/
  // *********************************************************************
  // новый вариант 
  public AuthData()
  {
    this(null);
  }

  public AuthData(String s)
  {
    rdata  = s;
  }

  private String rdata; // 198 символов ()

  // индексы начинаются с 1, как в мануале
  private static int[] _getFParams(int f_ind)
  {
    return FConst.FLD_IND[f_ind - 1];
  }



/////////////////////////////////////////////////////////

  private static byte countControlChar(byte[] sb)
  {
    return countControlChar(sb, 0, sb.length);
  }

  private static byte countControlChar(byte[] sb, int s_ind, int len)
  {
    byte cb = (byte)0;
    for (int i = s_ind; i < len; i++) cb ^= sb[i] | (i % 7);
    return (byte)((cb & (byte)0x3F) | (byte)0x30);
  }

  private static String _fill(String s, int len)
  {
    return _fill(s, FConst.FILL_CHAR, len);
  }
  // заполнить нужными символами до указанной длины
  private static String _fill(String s, char fc, int len)
  {
    if (s == null) s = "";
    int slen = s.length();
    if (slen == len) return s; else
      if (slen > len) return s.substring( slen - len, slen); else
        {
          StringBuffer sb = new StringBuffer();
          for (int i = slen; i<len; i++) sb.append(fc);
          return s + sb.toString(); // выровнено по правому краю
        }
  }
////////////////////////////////////////////////////////////////

  public String getValue(int f_ind)
  {
    int[] fp = _getFParams(f_ind);
    return rdata.substring(fp[0], fp[0] + fp[1]);
  }

  public char getCharValue(int f_ind)
  {
    int[] fp = _getFParams(f_ind);
    return rdata.charAt(fp[0]);
  }


  public boolean checkCtrlChar() throws UnsupportedEncodingException
  {
    char file_cc = getCharValue(27);
    char calk_cc = (char)countControlChar(rdata.getBytes(FConst.FILE_ENC), 0, 197);

    System.out.println("file_cc=" + file_cc + " calk_cc=" + calk_cc);
    return file_cc == calk_cc;
  }


  public static AuthData createAuthData(

    String msg_n, boolean akeyb,  String card_no, String exp_date, 
    String trans_date, String trans_time, String auth_code,
    String track2_data, char trans_type, double amount,  String trm_n)
      throws UnsupportedEncodingException
  {
    return createAuthData(FConst.FILL_CHAR, null, '0', null, msg_n, akeyb, card_no, exp_date, 
      trans_date, trans_time, auth_code, track2_data, trans_type, amount, trm_n);
  }

  // создение строки по атрибутам запроса
  // основной метод
  public static AuthData createAuthData(
    char sf, String f13, char f20, String f26,
    String msg_n, boolean akeyb,  String card_no, String exp_date, 
    String trans_date, String trans_time, String auth_code,
    String track2_data, char trans_type, double amount,  String trm_n)
      throws UnsupportedEncodingException
  {
    StringBuffer sb = new StringBuffer(197);

    // добавляем поля фиксированной длины по описанию
    sb.append(sf/*FConst.FILL_CHAR*/);  // 1
    sb.append(_fill(msg_n, _getFParams(2)[1]));
    sb.append(akeyb ? FConst.EM_KEY : FConst.EM_READER);
    sb.append(_fill(card_no, _getFParams(4)[1]));
    sb.append(_fill(exp_date, _getFParams(5)[1]));
    sb.append(_fill(track2_data, _getFParams(6)[1]));

      // formatting double ... 
      NumberFormat num_fmt = NumberFormat.getInstance(Locale.ENGLISH);
      num_fmt.setGroupingUsed(false);
      num_fmt.setMaximumFractionDigits(2);  // ... ///

    sb.append(_fill(num_fmt.format(amount), _getFParams(7)[1]));
    sb.append(_fill(trans_date, _getFParams(8)[1]));
    sb.append(_fill(trans_time, _getFParams(9)[1]));

    sb.append(trans_type); // f10

    sb.append(_fill(null, _getFParams(11)[1]));
    sb.append(_fill(auth_code, _getFParams(12)[1]));
    sb.append(_fill(f13, _getFParams(13)[1]));
    sb.append(_fill(null, _getFParams(14)[1]));

    int i;

    sb.append(_fill(trm_n, _getFParams(15)[1]));

    sb.append(FConst.EXCH_CODE); // 16

    for (i = 17; i <= 19; i++) sb.append(_fill(null, _getFParams(i)[1]));
    
    sb.append(f20) ;
    sb.append(FConst.SIGN_OK); // 21

    for (i = 22; i <= 25; i++) sb.append(_fill(null, _getFParams(i)[1]));

    sb.append(_fill(f26, _getFParams(26)[1]));

    String md = sb.toString();

    char cc = (char)countControlChar(md.getBytes(FConst.FILE_ENC));

    return new AuthData( md + cc );
  }

  // factory
  // только online - авторизация
  // msg_n нужно только для отмены транзакции
  public static AuthData createAuthData(
    String msg_n, boolean akeyb, String card_no, String exp_date, 
    String track2_data, char trans_type, double amount, String trm_n)
      throws UnsupportedEncodingException
  {
    return createAuthData(msg_n, akeyb, card_no, exp_date, null, null, null, track2_data, trans_type, amount, trm_n);
  }

  // общий метод для продажи по безналу
  public static AuthData createSaleAuthData( boolean akeyb, String card_no, 
    String exp_date, String track2_data, double amount, String trm_n)
      throws UnsupportedEncodingException
  {
    return createAuthData(null, akeyb, card_no, exp_date, null, null, null, track2_data, FConst.TT_SALE, amount, trm_n);
  }

  // при использовании ридера
  public static AuthData createAuthData(
    String msg_n, String track2_data, char trans_type, double amount, String trm_n)
      throws UnsupportedEncodingException
  {
    return createAuthData(msg_n, false, null, null, null, null, null, track2_data, trans_type, amount, trm_n);
  }

  public static void writeToFile(AuthData d, String ffn) throws IOException
  {
    //"$int_i$.NNN"
    //String fn = "$int_i$." + _fill(trm_n, '0', 3);
    FileOutputStream fos = new FileOutputStream(ffn/*path + File.separator + fn*/);
    fos.write(d.rdata.getBytes(FConst.FILE_ENC));
    fos.write( (char)13 );
    fos.write( (char)10 );
    fos.flush();
    fos.close();
  }


  public static AuthData readFromFile(String ffn) throws IOException
  {
    File f = new File(ffn);
//    if (!f.exists()) throw new IOException
    FileInputStream fis = new FileInputStream(f);
    byte[] rbuf = new byte[FConst.FIXED_AUTH_LEN];
    int ln = fis.read(rbuf);
    fis.close();

    if (ln < FConst.FIXED_AUTH_LEN) throw new IOException("Incorrect server response file");

    AuthData data = new AuthData(new String(rbuf, 0, 198, FConst.FILE_ENC));

    data.print();

    if (!data.checkCtrlChar()) throw new IOException("Incorrect server response file");

    return data;
  }

  public static String checkAnswer(AuthData ans)
  {
    char akey = ans.getCharValue(1);
    if (akey == FConst.SF_OK)
    {
      return "ok";
    } else
    if (akey == FConst.SF_DENY)
    {
      return "Ошибка авторизации (" + ans.getCharValue(20) + ":" + ans.getValue(26) + ")";
    } else
      return null; // без атрибута ответа
  }

  public static String getErrComents(AuthData ans)
  {
    char c = ans.getCharValue(20);
    int i = Integer.parseInt(String.valueOf(c));
    
    return i < 10 ?  FConst.RU_F20_COMENTS[i] : null;
  }

  public static String getErrHiddenComents(AuthData ans)
  {
    char c = ans.getCharValue(20);

    

    String f26 = ans.getValue(26);

    int i = 0; 
    int len = FConst.F26_DESCR.length;
    while (i < len  && !f26.equals(FConst.F26_DESCR[i][0])) i++;

    String f26d = i < len ? ":" + FConst.F26_DESCR[i][1] + "->" + FConst.F26_DESCR[i][2] : "";
    return c + ":" + ans.getValue(13) + " & " + ans.getValue(26) + f26d;
  }

  /// транзакция для закрытия смены
  public String _makeTrStr()
  {
    String s_exp_date = getValue(5);
    String s_tr_date = getValue(8) + getValue(9);

    
    return getValue(15) + ',' + getValue(4) + ',' + getValue(5) + ',' + getValue(7) + ',' +
      getValue(12) + ',' + getValue(2) + ',' + getValue(8) + ',' + getValue(9) + ',' +
      getValue(10);
  }

  public String toString()
  {
    // TODO:  Override this java.lang.Object method
    return getValue(12);
  }

  public void print()
  {
    System.out.println(rdata);
  }

  // строка запроса / ответа
  public String getData() { return rdata; }

}