package th.cash.fr.doc;

import java.util.Locale;
import java.text.NumberFormat;

/**
 * Форматирование строк текста для вывода на POS устройства
 */
public class StrFormat 
{
  private NumberFormat mfmt, qfmt;

  public StrFormat()
  {
    mfmt = NumberFormat.getInstance(Locale.ENGLISH);
    mfmt.setGroupingUsed(false);
    mfmt.setMinimumFractionDigits(2);
    mfmt.setMaximumFractionDigits(2);

    qfmt = NumberFormat.getInstance(Locale.ENGLISH);
    qfmt.setGroupingUsed(false);
    qfmt.setMaximumFractionDigits(3);
    
  }

  public NumberFormat getMoneyFmt() { return mfmt; }

  public NumberFormat getQuanFmt() { return qfmt; }

  public String getPosString(Position p, int max_len)
  {
    return getSEString(p.getGname(), ' ', getQPSString(p), max_len);
  }

  // расшифровка: количество X цена = сумма
  public String getQPSString(Position p)
  {
    String res;

    if (p.getQuantity().doubleValue() == (double)1)
    {
      res = "=" + mfmt.format(p.getSum());
    } else
    {
      res = qfmt.format(p.getQuantity()) + "X" + mfmt.format(p.getPrice()) + "=" + mfmt.format(p.getSum());
    }

    return res;
  }

  // строка с заполнением, размешенная по центру
  public String getCenterString(String s, char fc, int max_len)
  {
    if (s == null) s = "";
    if (s.length() > max_len) s = s.substring(0, max_len);
    int len = s.length();

    int i = 0;
    StringBuffer sb = new StringBuffer();

    while (i < (max_len - len) / 2) { sb.append(fc); i++; }

    sb.append(s); 
    i += len;

    while (i < max_len) { sb.append(fc); i++; }    

    return sb.toString();
  }

  // сборка строки из 2-х, 1-я наименование , далее заполнитель, 2-я значение 
  // причем первая строке может быть укорочена
  public String getSEString(String s1, char fc, String s2, int max_len)
  {
    int s1_len = s1 == null ? 0 : s1.length();
    int s2_len = s2 == null ? 0 : s2.length();
    int s1_cnt = Math.max(0, Math.min(s1_len, max_len - 1 - s2_len));

    StringBuffer sb = new StringBuffer();

    if (s1_cnt > 0) 
      sb.append(s1.substring(0, s1_cnt) );

    for (int i = 0; i < max_len - s1_cnt - s2_len; i++) 
      sb.append(fc);

    if (s2_len > 0) 
      sb.append(s2);
    return sb.toString();
  }

  
}