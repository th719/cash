package th.cash.env;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import java.io.IOException;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.Vector;

import jpos.JposException;
import jpos.Scanner;

import org.apache.log4j.Logger;

import th.cash.cd.PosDisp;
import th.cash.dev.*;
import th.cash.fr.FrKPrinter;
import th.cash.model.SprController;
import th.cash.model.SprModel;
import th.cash.model.SprUpdater;
import th.cash.model.User;
import th.cash.ui.sale.ProcessShow;
import th.cash.ui.sale.UsersListDisplayable;
import th.cash.ui.util.UserDlg;

import th.kkm.sa.server.Server;
import th.kkm.sa.server.checkLoadUnload;


public class KKMEnv 
{

  // конфигурация jdbc
  private DbCfg loginCfg;  

  // модель, контроллер 
  private SprModel sprModel;
  private SprController sprController;

  // связано с логином текущий пользователь 
  private User currentUser = null;

  //     ********      Устройства        *******
  // фискальник
  private FiscalPrinter fiscal_printer = null;
  // сканнер (Jpos)
  private Scanner bs = null;
  // дисплей покупателя
  private CustDisplay cd = null;

  // поток для обмена данными с сервером
  private Server exch_thread;

  // коннект для интерфейса, открыт постоянно для записи транзакций
  private Connection tr_conn;

  // поток для фонового обновления модели и Runnable для него
  private Thread update_thread = null;   
  private SprUpdater update_run = null;

  // для отображения процесса инициализации
  private ProcessShow init_vis;          

  // cash module version & build (from manifest)
  private String ver = null, build = null;

  // logger
  protected final static String LOG_PREF = "MODEL"; 
  private Logger log = Logger.getLogger(LOG_PREF + '.' + this.getClass().getName());
    


  /**
   * инициализация справочников
   */
  public void init() throws Exception
  {

    log.info("*** DB Login Params ***");
    initLoginCfg();
    log.info("URL = " + getLoginCfg().getLocalURL());
    log.info("*** Ok");

    log.info("*** Connect to DB ***");
    Connection conn = getLoginCfg().connectLocal();
    log.info("*** Ok");

    log.info("*** Spr model & controller ***");
    initSpr(conn);
    log.info("*** Ok");
    


    conn.close();
    log.info("*** Connection closed ****");


    log.info("*** Connecntion for UI ***");
    tr_conn = getLoginCfg().connectLocal();    
    log.info("*** Ok");
    
  }


  /**
   * параметры подключения к локальной базе
   */ 
  private void initLoginCfg() throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException, SQLException
  {
    loginCfg = new DbCfg();
    loginCfg.init();
  }

  public DbCfg getLoginCfg()
  {
    return loginCfg;
  }

  /**
   * инициализация справочников и контроллера обновления
   */
  private void initSpr(Connection c) throws SQLException
  {
    sprModel = new SprModel(20000, 20000);
    sprController = new SprController(sprModel);
    sprController.refreshAllModel(c);
  }

  public SprModel getSprModel()
  {
    return sprModel;
  }

  public SprController getSprController()
  {
    return sprController;
  }

  public Connection getTrConnection()
  {
    return tr_conn;
  }

  /**
   * сервер обмена локальных справочников
   */
  protected void initSprExcServer() 
  {
    log.info("*** Init Exchange Thread ***");

    exch_thread = new Server();

    Vector exch_par = new Vector();
    exch_par.add(new Integer(5));    //delay in seconds
    exch_par.add(new Integer(100));  //sleep time in microseconds
    exch_par.add(Boolean.TRUE);     //commit_enabled

  // set checkLoadUnload params
    Vector p_clu = new Vector();
    p_clu.add(new Integer(checkLoadUnload.FILE_TYPE_FLAG));


    Vector p_fl=new Vector();
      
    Vector p_dbl=new Vector();

    Vector p_dbu=new Vector();

    Vector p_fu=new Vector();

    Vector params=new Vector();
      
    params.add(exch_par);
    params.add(p_clu);
    params.add(p_fl);
    params.add(p_dbl);
    params.add(p_dbu);
    params.add(p_fu);

    
    int init_res=exch_thread.init(params, this);

    if (init_res != 1 )
      exch_thread = null;

    if (exch_thread == null) 
      log.warn("*** Init error, exchange disabled !!!"); 
    else 
      log.info("*** Ok");
    
  }


  // подстройка клавиатуры, переключения фокуса и событий кнопок
  public void initKeyboard()
  {
    log.info("*** Init keyboard: NumLock On ***");

    // NumLock лучше включать при каждом открытии открытии  окна регистрации
    try {
      Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_NUM_LOCK, true);
    } catch (Exception ex)
    {
      log.warn(ex.getMessage());
    }
    UserDlg.setArrowFocusManager();
    UserDlg.setButtonInputMap();

    log.info("*** Ok");
  }


  // init barcode scanner
  public void initScanner(String name)
  {
    log.info("*** Barcode scanner, device name ="+ name);
  
    try
    {
      bs = new Scanner();
      bs.open(name);
      bs.claim(2000);
      bs.setDecodeData(true);
      bs.setDeviceEnabled(false);
      bs.setDataEventEnabled(true);

      log.info("***" + bs.getCheckHealthText()); 
      log.info("*** Ok");
    } catch (JposException ex)
    {
      bs = null;
      log.warn("*** Scanner init failure:" + ex.getMessage());
      log.error(ex.getMessage(), ex);
    }
  }

  /**
   * init FR
   */

  public void initFiscalPrinterPort(String port, int timeout) throws Exception
  {
    fiscal_printer = new FrKPrinter(port, timeout);
  }

//  public void initFiscalPrinterDevice()throws Exception
//  {
//    fiscal_printer.init();
//
//    // инициализируем объект состояния  
//    fr_state = new FullStateFr();
//    fr_state.decodeParams(fiscal_printer.getReply());
//    fr_state.printAll();
//  }

//  public void initFiscalPrinterParams() throws Exception
//  {
//    FrKPrinter pr = (FrKPrinter)fiscal_printer;
//
//    Properties pfr_modes = getSprModel().getSettings().getFrModes();
//    Properties pfr_check_text = getSprModel().getSettings().getFrCheckText();
//
//    if (log.isDebugEnabled())
//      log.debug("*** Program fr tables ...");
//    _copyPropToFp(pr, pfr_modes, Integer.class, 1);
//
//    _copyPropToFp(pr, pfr_check_text, String.class, 40);
//  }

//  public StateA getFrState()
//  {
//    return fr_state;
//  }
   
//  private void initFiscalPrinter(String port) throws Exception
//  {
//    log.info("*** Open fiscal printer on " + port);
//    FrKPrinter pr = new FrKPrinter(port, 100);
//    fr_state = new FullStateFr();
//    fr_state.decodeParams(pr.getReply());
//
//    fr_state.printAll();
//
//    fiscal_printer = pr;
//
//    ftb_modes = new FrTable((byte)1);
////    ftb_modes.initFromFr(pr.getFrDrv());
//
//
//    Properties pfr_modes = getSprModel().getSettings().getFrModes();
//    Properties pfr_check_text = getSprModel().getSettings().getFrCheckText();
//
//    if (log.isDebugEnabled())
//      log.debug("*** Program fr tables ...");
//    _copyPropToFp(pr, pfr_modes, Integer.class, 1);
//
//    _copyPropToFp(pr, pfr_check_text, String.class, 40);
//
//    log.info("*** Fiscal printer Ok ");
//    
//  }

  // временная примочка - копирование настроек в таблицу фискальника
  // номер таблицы, строки и колонки - из имени свойства
//  private void _copyPropToFp(FrKPrinter fp, Properties pr, Class data_class, int sz) throws Exception
//  {
//    Enumeration keys;
//
//    keys = pr.keys();
//
//    String key, prop;
//    Object val;
//    int[] tidx;
//
////    StrFormat sfmt = null;
////    if (data_class.equals(String.class)) sfmt = new StrFormat();
//    
//    while (keys.hasMoreElements())
//    {
//      key = keys.nextElement().toString();
//      prop = pr.getProperty(key);
//
//      if (data_class.equals(Integer.class))
//        val = new Integer(prop);
//      else
//        val = prop;
//
////        val = sfmt.getCenterString(prop, ' ', sz); // текст в чеке(по центру)
//        
//      tidx = getSprModel().getSettings().getTabRowCol(key);
//
//      if (log.isDebugEnabled()) 
//        log.debug("*** Set tab[" + tidx[0] + "]." + tidx[1] + "." + tidx[2] + " = " + val);
//
//      fp.setTabCell(tidx[0], tidx[1], tidx[2], val, sz);
//    }
//  }

  // дисплей покупателя
  public void initCustomerDisplay(String port) 
  {
    log.info("*** Customer display on " + port);
    try
    {
      PosDisp disp = new PosDisp(port, 9600, 2000);

      disp.start();
      cd = disp;
      log.info("*** Customer display Ok");
      
    } catch (Exception ex)
    {
      // делаем заглушку, чтобы не проверять null !!!!!!!!!!!
      /*
      cd = new CustDisplay() {
        public void setText(boolean b, String s1, String s2) {
          log.debug("s1=" + s1); log.debug("s2=" + s2);}
        public void close() {}
        public int getStrLen() { return 20; }
      };
      */
      cd = null;
      log.error("*** Customer display error:", ex);
    }
  }

  public void initUpdateThread(UsersListDisplayable o)
  {
    log.info("*** Spr Update thread");
    update_run = new SprUpdater(sprController, sprModel, o ,true, null, false);
    update_thread = new Thread(update_run, "ModelSync");
    log.info("*** Ok");
  }


  /**
   * Закрытие устройств, соединений, завершение процессов
   */
  public void close()
  {
    try
    {
      // added 12.02.2008
      stopExchThread();

      // added 12.02.2008
      stopUpdThread();

      if (tr_conn != null) 
      {
        log.info("*** Close connecntion for UI");
        tr_conn.close();
        log.info("*** Ok");
      }

      log.info("*** Devices ---");
      if (fiscal_printer != null)
      {
        fiscal_printer.close();
        fiscal_printer = null;
        log.debug("*** Fiscal printer closed");
      }
      if (bs != null)
      {
        bs.close(); 
        bs = null;
        log.debug("*** Barcode scanner closed");
      }

      if (cd != null)
      {
        cd.close();
        cd = null;
        log.debug("*** Customer display closed");
      }

      log.info("*** KKMEnv.close() Ok");
            
    } catch (Exception ex)
    {
      log.error(ex.getMessage(), ex);
    }
  }
  

  // DEVICES !!!
  public FiscalPrinter getFiscalPrinter()
  {
    return fiscal_printer;
  }

  public Scanner getScanner()
  {
    return bs;
  }

  public CustDisplay getCustomerDisplay()
  {
    return cd;
  }


  // for login
  public User getCurrentUser()
  {
    return currentUser;
  }

  public void setCurrentUser(User newCurrentUser) 
  {
    currentUser = newCurrentUser;
  }


  public int getCashNumber()
  {
    return getSprModel().getSettings().getCashNumber().intValue();
  }


  //************************************************
  // поток для обмена через файлы 
  public Server getExchThread()
  {
    return exch_thread;
  }

  public void startExchThread()
  {
    if (exch_thread != null)
    {
      exch_thread.start();
      log.info("*** Excange thread started");
    }
      
  }

  public void stopExchThread()
  {
    if (exch_thread != null)
    {
      log.info("*** Stop exchange thread");
      exch_thread.stopServer();
      exch_thread.interrupt();
      exch_thread = null;
      log.info("*** Ok");
    }
  }

  //************************************************
  // поток обновления справочников в модели
  public void startUpdThread()
  {
    if (update_thread != null && update_run != null)
    {
      update_run.setStopped(false);
      update_thread.start();
      log.info("*** Spr update thread started");
    }
  }

  public void stopUpdThread()
  {
    if (update_thread != null && update_run != null )
    {
      if (update_thread.isAlive())
      {
        update_run.setStopped(true);
        update_thread.interrupt();
        log.info("*** Spr update thread stopped");
      }
    }
  }

  public SprUpdater getSprUpdater()
  {
    return update_run;
  }

  //************************************************
  // версия и номер сборки основного пакета
  protected void setVerAndBuild(String vn, String bn)
  {
    ver = vn; build = bn;
  }

  public String getVersion() { return ver; }

  public String getBuild() { return build; }
  
   
}