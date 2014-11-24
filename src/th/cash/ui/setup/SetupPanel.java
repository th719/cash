package th.cash.ui.setup;

import java.util.Properties;
import java.util.Set;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;



import th.cash.env.KKMEnv;
import th.cash.model.Settings;

import th.cash.ui.util.UserDlg;

import java.sql.Connection;
import java.sql.SQLException;

import th.kkm.sa.server.Server;

public class SetupPanel extends JPanel
{
//  private final static String UI_SET = "Основные";

  private TabSetPanel[] tabPanels;
  private FrTablesPanel fr_panel;
  private FrParamsPanel fr_panel2;
  
  private JTabbedPane mtab;

  private boolean hasChanges = false;

  private KKMEnv env = null;
  
  public SetupPanel()
  {
    init();
  }

  private void init()
  {
    tabPanels = new TabSetPanel[4];

    mtab = new JTabbedPane();

    int pcnt = 0;
    mtab.add("Общие", tabPanels[pcnt++] = new UISetPanel() );
    mtab.add("Обмен", tabPanels[pcnt++] = new ExchSetPanel() );
//    mtab.add("Fr",   tabPanels[pcnt++] = new FrSetPanel() );
    mtab.add("Таблицы ФР", tabPanels[pcnt++] = fr_panel = new FrTablesPanel() );
    mtab.add("Параметры ФР", tabPanels[pcnt++] = fr_panel2 = new FrParamsPanel());

    setLayout(new BorderLayout());
    add(mtab, BorderLayout.CENTER);
    
  }

  public void refreshData(KKMEnv env)
  {
    refreshData(env.getSprModel().getSettings().getAllSet());
    fr_panel.setFiscalPrinter(env.getFiscalPrinter());
    fr_panel2.setFiscalPrinter(env.getFiscalPrinter());
    this.env = env;
  }

  // обновляем каждую закладку через список совойств
  private void refreshData(Properties p)
  {
    for (int i = 0; i < tabPanels.length; i++) tabPanels[i].refreshData(p);
  }

  public boolean saveAll()
  {
    boolean res = true;
    boolean has_changes = false;
    boolean exch_params_changed = false;

    String pres = null;

    Settings settings = env.getSprModel().getSettings();

    Properties p0 = settings.getAllSet();
    
    Properties p1 = new Properties();

    int i = 0; 
    // опрас всех панелек на сохранение данных
    while (i < tabPanels.length && pres == null) 
    {
      TabSetPanel tsp = tabPanels[i];
      if (tsp.isDataChanged())
      {
        if (tsp instanceof ExchSetPanel)
          exch_params_changed = true;
          
        has_changes = true;
//        tsp.saveData(p1);
      }
      pres = tsp.saveData(p1);
      i++;
    }

    if (pres != null)
    {
      UserDlg.showError(this, pres);
      return false;
    }

//    System.out.println("From controls:");
//    p1.list(System.out);

    if (has_changes)
    {
      Connection con = null;
      try
      {
        con = env.getLoginCfg().connectLocal();
        
        Properties ns = SetDbUtil.saveSetProperties(p0, p1, con);

//        System.out.println("Saved:");
//        ns.list(System.out);

        settings.clear();

        Set keys = ns.keySet();
        Object[] arr_keys = keys.toArray();
        int len = arr_keys.length;
        String key;

        for (i = 0; i < len; i++)
        {
          key = (String)arr_keys[i];
          settings.putProperty(key, ns.getProperty(key));
        }
        

      } catch (SQLException ex)
      {
        res = false;
        UserDlg.showError(this, ex.getMessage());
      } finally
      {
        try { con.close(); } catch (Exception ex) {}
      }
    }

    if (res && has_changes)
    {

      if (exch_params_changed)
      {
        // оповещение потока обмена данными
        synchronized (env.getExchThread().getExchSettings())
        {
          env.getExchThread().getExchSettings().init(env.getSprModel().getSettings());
        }
      }

    } 


    return res;
  }

  public final static String APPLY_S = "Применить";
  public final static String SAVE_S = "Сохранить";
  public final static String CANCEL_S = "Отмена";
  
  
}