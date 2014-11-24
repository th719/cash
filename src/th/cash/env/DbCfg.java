package th.cash.env;

import java.util.Properties;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.io.IOException;

public class DbCfg 
{

  private final static String LOGIN_CONF_RES = "/conn.properties";
  // config file parameters
  public final static String PASSWORD_PR = "password";
  public final static String URL_PR      = "url";
  public final static String DRIVER_PR   = "driver";
  public final static String USER_PR     = "user";

  private Properties loginCfg;  
  private String localURL;
  private String user, password;

  // использовать шифрование пароля
//  protected static PaswCipher paswCipher = null;
  private boolean use_cr_pass = true;

  public DbCfg()
  {
    this(false);
  }

  public DbCfg(boolean cr_pass)
  {
    use_cr_pass = cr_pass;
  }

  public void init() throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException, SQLException
  {
    initFromRes(LOGIN_CONF_RES);
    if (use_cr_pass) decryptPassword(loginCfg);
  }

  public void initFromRes(String res_name) throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException, SQLException
  {
    loginCfg = new Properties();
    loginCfg.load(getClass().getResourceAsStream(res_name));
    String driver_name = loginCfg.getProperty(DRIVER_PR,"org.postgresql.Driver");
    Driver dri = (Driver)Class.forName(driver_name).newInstance();
    DriverManager.registerDriver(dri);

    localURL = loginCfg.getProperty(URL_PR);
    user = loginCfg.getProperty(USER_PR);
    password = loginCfg.getProperty(PASSWORD_PR);
  }
/*
  public synchronized void decryptPassword(Properties cfg) throws Exception
  {
    // password decrypting
    if (USE_CR_PASS)
    {
      String pass = cfg.getProperty(ShopParams.PASSWORD_PR);
      if (pass == null) throw new RuntimeException("Connect password is absent");
      try
      {
        if (paswCipher == null)
          paswCipher = (PaswCipher)Class.forName("th.shop.local.cryp.DESPaswCipher").newInstance();
        pass = paswCipher.decrypt(pass);
        cfg.setProperty(ShopParams.PASSWORD_PR, pass);
      } catch (Exception ex)
      {
        //paswCipher = (PaswCipher)Class.forName("th.shop.local.cryp.EmptyPaswCipher").newInstance();
      }
    }
  }
  */

   public synchronized void decryptPassword(Properties cfg) 
   {
     password = loginCfg.getProperty(PASSWORD_PR);
   }

   public synchronized Connection connectLocal() throws SQLException
   {
     Connection conn; 
     conn = DriverManager.getConnection(localURL, loginCfg);
     conn.setAutoCommit(false);
     return conn;
   }

  public String getLocalURL()
  {
    return localURL;
  }
   
}