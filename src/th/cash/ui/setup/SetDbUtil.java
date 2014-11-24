package th.cash.ui.setup;

import java.util.Properties;
import java.util.Set;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Connection;

// утилита для сохранения настроек в базу
public class SetDbUtil 
{

  // p0 - старый (исходный) набор свойств
  // p1 - измененные значения
  // с - сеанс 
  public static Properties saveSetProperties(Properties p0, Properties p1, Connection c) throws SQLException
  {
    Properties res = new Properties();
 
    Set keys0 = p0.keySet();

    Object[] arr_keys;
    int len;

    arr_keys = keys0.toArray();
    len = arr_keys.length;
    String key, val0, nval; // ключ, старое значение, новое значение

    PreparedStatement dst, ist;
    dst = c.prepareStatement("delete from t_set where set_id = ?");
    ist = c.prepareStatement("insert into t_set(set_id, set_val) values(?, ?)");

    // убираем старые значения и ключи и добавляем измененные
    for (int i = 0; i < len; i++)
    {
      key = (String)arr_keys[i];
      val0 = p0.getProperty(key);

      nval = p1.getProperty(key);

      if (val0 != null)
      {
        dst.setString(1, key);
        dst.executeUpdate();
      }

      if (nval != null)
      {
        ist.setString(1, key);
        ist.setString(2, nval);
        ist.executeUpdate();

        res.setProperty(key, nval);
      }
    }

    // находим добавленные ключи (которых не было в исходных настройках)
    Set keys1 = p1.keySet();
    keys1.removeAll(keys0);

    arr_keys = keys1.toArray();
    len = arr_keys.length;

    for (int i = 0; i < len; i++)
    {
       key = (String)arr_keys[i];
       nval = p1.getProperty(key);
       if (nval != null)
       {
         ist.setString(1, key);
         ist.setString(2, nval);
         ist.executeUpdate();

         res.setProperty(key, nval);
       }
    }

    dst.close();
    ist.close();

    c.commit();

    return res;
  }



}