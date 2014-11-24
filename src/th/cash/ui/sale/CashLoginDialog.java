package th.cash.ui.sale;


import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Vector;

import javax.swing.*;

import jpos.JposException;

import jpos.events.DataEvent;
import jpos.events.DataListener;

import org.apache.log4j.Logger;

import th.cash.env.KKMEnv;
import th.cash.model.User;
import th.cash.ui.util.UserDlg;


public class CashLoginDialog extends JDialog implements DataListener, UsersListDisplayable
{

  private final static String SALE_CMD  = "gotoSale";
  private final static String SETUP_CMD = "showSetup";
  private final static String EXIT_CMD  = "exit";
  private final static String EXIT_SYSTEM_CMD = "exit_system";

  private JComboBox jcb_users;
  private JPasswordField jp_pwd;

  private Vector users;
  private KKMEnv cashEnv;


  private Action ok, cancel, setup, cancel_system;  


  private String pwd;
  private User authUser = null;
  private String sel_cmd = null;

  private AScannerSwitcher scan_switcher = null;
  private boolean bc_accept;

  protected final static String UI_LOG_PREF = "UI"; 
  private Logger log = Logger.getLogger(UI_LOG_PREF + '.' + CashLoginDialog.class.getName());


  public CashLoginDialog()
  {
    super();
    setTitle("Авторизация");
    setModal(true);


    // ui interface 
    JPanel p;

    p = new JPanel();

    // панелька авторизации
    jcb_users = new JComboBox();
    
    jp_pwd = new JPasswordField(15);

    FormLayout fl = new FormLayout(
      "3dlu, l:p, 2dlu, l:p:g, 3dlu",
      "3dlu ,p, 2dlu, p, 5dlu"
    );

    p.setLayout(fl);

    CellConstraints cc = new CellConstraints();
    
    p.add(new JLabel("Пользователь:"), cc.xy(2, 2));
    p.add(jcb_users, cc.xy(4, 2));
    p.add(new JLabel("Пароль:"), cc.xy(2, 4));
    p.add(jp_pwd, cc.xy(4, 4));


    // actions

    ok = new AbstractAction("Продажи") 
    {
      public void actionPerformed(ActionEvent e) { do_select();}
    };


    setup = new AbstractAction("Настройка")
    {
      public void actionPerformed(ActionEvent e) { do_setup(); }
    };


    cancel = new AbstractAction("Выключить") 
    {
      public void actionPerformed(ActionEvent e) { do_cancel(); }
    };


    cancel_system = new AbstractAction("Закрыть")
    {
      public void actionPerformed(ActionEvent e) { do_cancel2(); }
    };
    

    // install actions
    installAction(p, ok, "ENTER", SALE_CMD);
    installAction(p, ok, "F6", SALE_CMD);
    installAction(p, setup, "control S", SETUP_CMD);
    installAction(p, cancel, "ESCAPE", EXIT_CMD);
    installAction(p, cancel_system, "control E", EXIT_SYSTEM_CMD);

    setContentPane(p);

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

    addWindowListener(new WindowAdapter(){

      public void windowActivated(WindowEvent e) { jp_pwd.requestFocus(); }
      public void windowOpened(WindowEvent e) { jp_pwd.requestFocus(); }
    
      public void windowClosing(WindowEvent e) { do_cancel(); }
    });

    pack();

    setLocationRelativeTo(getOwner());
    
  }

  private void installAction(JComponent c, Action a, String key, String cmd) 
  {
    a.putValue(Action.ACTION_COMMAND_KEY, cmd);
    c.getInputMap(c.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), cmd);
    c.getActionMap().put(cmd, a);
  }


  // найти пользователя с указанным паролем
  private User checkPwd(String pwd)
  {
    User res = null;

    if (pwd == null) return null;
    
    int i = 0;
    while (i < users.size() && !pwd.equals(((User)users.get(i)).getPassword())) i++;

    res = i < users.size() ? (User)users.get(i) : null;
    return res;
  }


  /**
   * методы для обработки событий
   */
  // по кнопке <Enter>
  private void do_select()
  {
    sel_cmd = SALE_CMD;
    pwd = new String(jp_pwd.getPassword());
    jp_pwd.setText(null);
    hide();
  }

  // по событию ... setup
  private void do_setup()
  {
    // проверить пользователя и открыть окно настроек
    sel_cmd = SETUP_CMD;
    pwd = new String(jp_pwd.getPassword());
    jp_pwd.setText(null);
    hide();
  }

  // выход с кодом 0 (для выключения системы)
  private void do_cancel()
  {
    if (UserDlg.showQuestion(this, "Выключить компьютер?")) 
    {
      sel_cmd = EXIT_CMD;
      hide();
    } 
  }

  // для выхода по другому коду...
  private void do_cancel2()
  {
    if (UserDlg.showQuestion(this, "Выйти из программы?")) 
    {
      sel_cmd = EXIT_SYSTEM_CMD;
      hide();
    } 
  }


  /**
   * main login method
   */
  public void do_login(KKMEnv env)
  {

    cashEnv = env;
    setUsers(cashEnv.getSprModel().getUsersVector());    
    jcb_users.setSelectedItem(authUser);
    boolean lflag;

    // включаем сканнер, обновлялки
    if (scan_switcher == null)
      scan_switcher = new AScannerSwitcher(cashEnv.getScanner(), this);    

    scan_switcher.addDataListener();
    scan_switcher.setScannerEnabled(true);
    cashEnv.getSprUpdater().setBoxNotify(true);
    // changed 12.02.2008
    //cashEnv.startUpdThread();

    
    do
    {
      pwd = null;
      sel_cmd = null;

      jp_pwd.setText(null);
      //jp_pwd.requestFocus();
      bc_accept = true;
      show(); // здесь определяется sel_cmd (обязательно) и пассворд
      bc_accept = false;

      lflag = true;
      
      if (isSale() || isSetup())
      {
        authUser = checkPwd(pwd);
        if (authUser == null)
        {
          log.warn("Autorisation fails");
          UserDlg.showError(null, "Ошибка авторизации");
        } else 
        {
          
          lflag = false;
          log.info("Login as " + authUser.getName() + "  cmd = " + sel_cmd);
        }
      }

      if (isCancel() || isSysExit())
      {
        authUser = null;
        lflag = false;
      }

      
    } while (lflag);


    // выключаем
    scan_switcher.setScannerEnabled(false);
    scan_switcher.removeDataListener();
    // changed 12.02.2008
    //cashEnv.stopUpdThread();
    cashEnv.getSprUpdater().setBoxNotify(false);
    
  }

  // ***********************************************************************
  public void dataOccurred(DataEvent p0)
  {
    try
    {
      if (log.isDebugEnabled()) 
        log.debug("CashLoginDialog.dataOccurred()");
        
      byte[] scan_data;
      synchronized (cashEnv.getScanner())
      {
        scan_data = cashEnv.getScanner().getScanDataLabel();
      }

      if (bc_accept) 
      {
        pwd = new String(scan_data);
        sel_cmd = SALE_CMD;
        hide();
      }
      else pwd = null;

    } catch (JposException ex)
    {
      log.error(ex.getMessage(), ex);
    }
  }

  // ***********************************************************************
  // синхронизация !!! TODO
  public void setUsers(Vector u)
  {
    users = (Vector)u.clone();
    jcb_users.setModel(new DefaultComboBoxModel(users));
  }

  public boolean isCancel() { return EXIT_CMD.equals(sel_cmd); }

  public boolean isSysExit() { return EXIT_SYSTEM_CMD.equals(sel_cmd); }

  public boolean isSetup() { return SETUP_CMD.equals(sel_cmd); }

  public boolean isSale() { return SALE_CMD.equals(sel_cmd); }

  public User getUser() { return authUser; }

  public JComboBox getComboBox() { return jcb_users; }
  
}