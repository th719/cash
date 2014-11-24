package th.cash.fr.doc;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.ResultSet;


import th.cash.model.SprModel;
import th.cash.model.Tax;
import th.cash.model.User;

import java.text.SimpleDateFormat;
import java.text.NumberFormat;

import java.util.Date;
import java.util.Vector;
import java.util.Locale;
import org.apache.log4j.Logger;

// текстовая запись транзакции
public class LogConverter 
{

  private SimpleDateFormat date_sdf = null;
  private SimpleDateFormat time_sdf = null;

  private NumberFormat m_fmt = null;
  private NumberFormat q_fmt = null;

  private Logger log;  

  
  public LogConverter()
  {
    log = Logger.getLogger("TR" + '.' + this.getClass().getName());
  }

  
  public void logTransaction(int id, Transaction t)
  {
    if (log.isDebugEnabled()) 
    {
      if (date_sdf == null) date_sdf = new SimpleDateFormat("dd.MM.yy");
      if (time_sdf == null) time_sdf = new SimpleDateFormat("HH:mm:ss");
      if (m_fmt == null) { m_fmt = NumberFormat.getInstance(Locale.ENGLISH); m_fmt.setMinimumFractionDigits(2); m_fmt.setGroupingUsed(true); }
      if (q_fmt == null) { q_fmt = NumberFormat.getInstance(Locale.ENGLISH); q_fmt.setMinimumFractionDigits(3); q_fmt.setGroupingUsed(true); }

      String s = String.valueOf(id) + ';' + date_sdf.format(t.getTrDate()) + ';' +
        time_sdf.format(t.getTrDate()) + ';' + String.valueOf(t.getType()) + ';' +
        String.valueOf(t.getKkmNum()) + ';' + String.valueOf(t.getCheckNum()) + ';' +
        String.valueOf(t.getCashierNum()) + ';' + t.getStr() + ';' +
        String.valueOf(t.getIval()) + ';' + m_fmt.format(t.getD10()) + ';' +
        q_fmt.format(t.getD11()) + ';' + m_fmt.format(t.getD12()) + ';';
      
      log.debug(s);  
    }
  }

//  public Logger getLog()
//  {
//    return log;
//  }
//  
}