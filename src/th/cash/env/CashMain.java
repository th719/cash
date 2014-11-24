package th.cash.env;

import java.io.InputStream;
import javax.swing.Action;
import javax.swing.AbstractAction;

import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

//import th.cash.ui.sale.LoginDialog;
import th.cash.ui.sale.CashLoginDialog;
import th.cash.ui.sale.SalePanel;
import th.cash.ui.sale.SaleFrame;
import th.cash.ui.sale.LogoFrame;
import th.cash.ui.sale.ProcessShow;
import th.cash.ui.sale.PermException;

import th.cash.dev.CustDisplay;

import th.cash.ui.setup.SetupFrame;

import th.cash.model.Settings;
import th.cash.fr.doc.StrFormat;

import th.cash.ui.util.UserDlg;
import th.cash.fr.doc.DbConverter;
import th.cash.fr.doc.Transaction;
//import th.cash.fr.doc.LogConverter;

// Main class
public class CashMain extends WindowAdapter implements Runnable
{
  // versioning
  private String ver = null;
  private String num_build = null;
  private String date_build = null;

  // data enviroment
  private KKMEnv       cashEnv = null;
  // ui
//  private LoginDialog  login = null;
  private CashLoginDialog login = null;
  private SaleFrame    saleFrame = null; 
  private SetupFrame   setupFrame = null;

  // logger
  private Logger log = Logger.getLogger("MAIN" + '.' + this.getClass().getName());

  // static link
  private static CashMain cash_main;


  // Init OS Signal Handler
  private void initSignalHandler()
  {
    SignalHandler exit_handler = new SignalHandler()
    {
      public void handle(Signal sig) 
      { 
        cash_main.do_exit(2, "Exiting by " + sig.getName() + " signal ...");
      } 
    };
    Signal.handle( new Signal("INT"), exit_handler); 
    Signal.handle( new Signal("TERM"), exit_handler); 
  }


  // Init UI containers
  private void initLogin()
  {
    login = new CashLoginDialog();
    cashEnv.initUpdateThread(login);
    // added 12.02.2008
    cashEnv.startUpdThread();
  }

  private void initSaleFrame()
  {
    saleFrame = new SaleFrame();
    saleFrame.setCashEnv(cash_main.cashEnv);
    saleFrame.addWindowListener(this);

    // added 12.02.2008
//    cashEnv.getSprUpdater().setLocker(saleFrame., true);
  }

  private void initSetup()
  {
    setupFrame = new SetupFrame();
    setupFrame.setCashEnv(cash_main.cashEnv);
    setupFrame.addWindowListener(this);
  }


  // вывод сообщения о нерабочем состоянии
  private void showLockedTextOnDisplay()
  {
    CustDisplay cd = cashEnv.getCustomerDisplay();
    if (cd != null)
    {
      Settings set = cashEnv.getSprModel().getSettings();
      StrFormat fmt = new StrFormat();
      cd.setText(true,  
         fmt.getCenterString(set.getLockedCdText1(), ' ', cd.getStrLen()), 
         fmt.getCenterString(set.getLockedCdText2(), ' ', cd.getStrLen()));
    }
  }

  private boolean wt;
  private boolean fld; // отображать окно авторизации
    
  public void run()
  {
    wt = true;
    fld = true;
    try
    {
      while(wt)
      {
        if (fld)
        {
          if (login == null) initLogin();

          // вывод сообщения о нерабочем состоянии
          showLockedTextOnDisplay();

          // отображение окна авторизации
          login.do_login(cashEnv);

          // возможно что то выводить на дисплей

          if (login.getUser() == null) 
          {
            wt = false; 
          } else
          {
            cashEnv.setCurrentUser(login.getUser());
    
            if (login.isSetup())
            {
              if (setupFrame == null) initSetup();
              fld = !setupFrame.showFrame();
              if (cashEnv.getSprModel().getSettings().isLoginMark())
                markTransaction(Transaction.createLoginMark(cashEnv.getCashNumber(), 
                            cashEnv.getCurrentUser().getCode().intValue(), "setup"));
            } 
            
            if (login.isSale())
            {
              if (saleFrame == null) initSaleFrame();
              fld = !saleFrame.showFrame();
              if (cashEnv.getSprModel().getSettings().isLoginMark())
                markTransaction(Transaction.createLoginMark(cashEnv.getCashNumber(), 
                               cashEnv.getCurrentUser().getCode().intValue(), null));
            }
          }
        }

        if (wt) Thread.currentThread().sleep(250); 
      }
    } catch (Exception ex)
    {
      log.error(ex.getMessage(), ex);
      UserDlg.showError(null, ex.getMessage());
    }

    int exit_code = login.isCancel() ? 0 : 3;  
    do_exit(exit_code, "Exiting by user UI request ...");    
  }

  public void windowClosed(WindowEvent e)
  {
    fld = true;
    if (cash_main.cashEnv.getSprModel().getSettings().isLoginMark())
      markTransaction(Transaction.createLogoffMark(cashEnv.getCashNumber(), cashEnv.getCurrentUser().getCode().intValue()));
    log.info("Logout");
  }


  private void do_exit(int code, String msg)
  {
    log.warn(msg);

    showLockedTextOnDisplay();
    // added 26.03.09
    if (cash_main.cashEnv.getSprModel().getSettings().isStartMark())
      markTransaction(Transaction.createStopMark(cash_main.cashEnv.getCashNumber()));
    //
    cashEnv.close();
    System.exit(code);
  }

  // added
  private void markTransaction(Transaction t)
  {
    try
    {
      boolean tr_log = cash_main.cashEnv.getSprModel().getSettings().isLogTransToFile();
      DbConverter.saveTransaction(cash_main.cashEnv.getTrConnection(), t, tr_log);
    } catch (Exception ex)
    {
      log.error(ex);
    }
  }



  private void printStartInfo()
  {
    log.info("*****************************************************************");
    log.info("*************************   Start   *****************************");

    try
    { 
      JarFile jf = new JarFile("cash.jar", false);
      Manifest mf = jf.getManifest();
      
      if (mf != null)
      {
        Attributes a;
        String ver_entry_name = "th/cash";
        String build_entry_name = "Build info";

        a = mf.getAttributes(ver_entry_name);

        if (a != null)
        {
          String impl_ver_attr = "Implementation-Version";
          ver = a.getValue(impl_ver_attr);
          log.info(impl_ver_attr + ": " + ver);
        }

        a = mf.getAttributes(build_entry_name);
        if (a != null)
        {
          num_build = a.getValue("Build");
          date_build = a.getValue("Date");
          log.info("Build: " + num_build + "   " + date_build);
        }
      }
    } catch (Exception ex){}
    

    log.info("*****************************************************************");
  }


  //**********************  MAIN  **************************  
  public static void main(String[] args)
  {

    cash_main = new CashMain();

    // parse command line arguments
    String scan_dev = null, fp_dev = null, cdisp_dev = null;

    if (args.length > 0)  fp_dev = args[0];
    if (args.length > 1)  scan_dev = args[1];
    if (args.length > 2)  cdisp_dev = args[2];


    // log start info
    cash_main.printStartInfo();

    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < args.length; i++) sb.append(args[i] + " ");
    
    cash_main.log.info("Arguments: " + sb.toString());


    ProcessShow logoWin = new LogoFrame();    
    cash_main.cashEnv = new KKMEnv();
    cash_main.cashEnv.setVerAndBuild(cash_main.ver, cash_main.num_build);

    try
    {
      cash_main.log.info("Init Cash Env ...");

      // показываем заставку      
      logoWin.show();

      // инициализация элементов cashEnv      
      logoWin.setText("Инициализация справочников");
      cash_main.cashEnv.init();
      logoWin.addText("Ok");


      if (scan_dev != null)
      {
        cash_main.log.info("Init Barcode Scanner ...");
        logoWin.setText("Инициализация сканера Ш/К");
        cash_main.cashEnv.initScanner(scan_dev);
        logoWin.addText("Ok");
      }
      
      if (cdisp_dev != null)
      {
        cash_main.log.info("Init Customer Display ...");
        logoWin.setText("Инициализация дисплея покупателя");
        cash_main.cashEnv.initCustomerDisplay(cdisp_dev);
        logoWin.addText("Ok");
      }
      
      cash_main.cashEnv.initKeyboard(); 

      cash_main.log.info("Start Exchange Thread ...");
      cash_main.cashEnv.initSprExcServer(); 
      cash_main.cashEnv.startExchThread();


      cash_main.log.info("Init Fiscal Printer ...");
      logoWin.setText("Инициализация фискального регистратора");
      cash_main.cashEnv.initFiscalPrinterPort(fp_dev, 100);
      logoWin.addText("Ok");

      logoWin.hide();
      ((JFrame)logoWin).dispose();
      

      // прицепляем обработчик системных событий
      cash_main.log.info("Init system signal handler ...");
      cash_main.initSignalHandler();

      
        
      // переключаемся на окно Login
      cash_main.log.info("Show UI login ...");
      //cash_main.a_login.actionPerformed(null);

      // fix start mark , added 26.03.09 
//      cash_main.log.debug("logTransToFile =" + cash_main.cashEnv.getSprModel().getSettings().isLogTransToFile());

//      LogConverter.
      
      if (cash_main.cashEnv.getSprModel().getSettings().isStartMark())
        cash_main.markTransaction(
          Transaction.createStartMark(cash_main.cashEnv.getCashNumber(), 
                         "V"+cash_main.ver + " B" +cash_main.num_build));

      new Thread(cash_main).start();
      
    } catch (Exception ex)
    {

      cash_main.log.error("Exception in main", ex);
      ex.printStackTrace();  // dublicate to System.err


      logoWin.hide();

      UserDlg.showError(null, ex.getMessage());
      
      cash_main.do_exit(1, "Exiting by exception ...");
    }
  }
}