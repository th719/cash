package th.cash.ui.setup;

import java.util.Properties;
import java.util.Date;

import java.awt.event.ActionEvent;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;

import th.cash.fr.FrException;
import th.cash.dev.FiscalPrinter;
import th.cash.fr.FrKPrinter;
import th.cash.fr.state.StateA;
import th.cash.fr.state.FullStateFr;

import th.cash.ui.util.UserDlg;


import java.text.SimpleDateFormat;

// Управление фискальником ...
// дата/время прочитать, установить
// подтвердить ввод даты 
// отмена тех. теста 
// чтение и вывод состояния ФР

public class FrParamsPanel extends TabSetPanel
{

  private JLabel jl_time_1, jl_time_2;
  private JTextField jtf_tm_1, jtf_tm_2;
  private JTextArea jta_state;

  private SimpleDateFormat sdf_full_date;
  private SimpleDateFormat sdf_date;
  


  private FiscalPrinter printer;


  private Action time_from_fr, time_from_pk, conf_data, cancel_test, refr_state;

  public FrParamsPanel()
  {
    init();
  }

  private void init()
  {

    jl_time_2 = new JLabel("ПК");

    jtf_tm_1 = new JTextField(20);
    jtf_tm_2 = new JTextField(20);


    jta_state = new JTextArea(18, 0);
    jta_state.setEditable(false);
    jta_state.setLineWrap(true);
    jta_state.setWrapStyleWord(true);
    JScrollPane sp = new JScrollPane(jta_state);

    sdf_full_date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    sdf_date = new SimpleDateFormat("dd.MM.yyyy");


    initActions();

    FormLayout fl = new FormLayout("p, 2dlu, p, 2dlu, p", "p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p");
    CellConstraints cc = new CellConstraints();

    setLayout(fl);
    
    add(new JLabel("Дата/время ФР"), cc.xy(1, 1));
    add(jtf_tm_1, cc.xy(3, 1));
    add(new JButton(time_from_fr), cc.xy(5,1));

    add(new JLabel("Дата/время ПК"), cc.xy(1, 3));
    add(jtf_tm_2, cc.xy(3, 3));
    add(new JButton(time_from_pk), cc.xy(5,3));

    add(new JButton(conf_data), cc.xy(1, 5));
    
    
    add(new JButton(refr_state), cc.xy(1, 7));

    add(new JButton(cancel_test), cc.xywh(3, 7, 3, 1));

    add(sp, cc.xywh(1, 9, 5, 1));
    
  }

  private void initActions()
  {
    time_from_fr = new AbstractAction("Обновить")
    {
      public void actionPerformed(ActionEvent e)
      {
        try
        {
          printer.init();
          StateA st = ((FrKPrinter)printer).stateRequest();
          Date fd = ((FullStateFr)st).getCurDate();
          jtf_tm_1.setText(sdf_full_date.format(fd));

        } catch (Exception ex)
        {
          UserDlg.showError(FrParamsPanel.this, ex.getMessage());
        }
      }
    };

    time_from_pk = new AbstractAction("Обновить")
    {
      public void actionPerformed(ActionEvent e)
      {
        jtf_tm_2.setText(sdf_full_date.format(new java.util.Date()));
      }
    };

    conf_data = new AbstractAction("Подтвердить дату")
    {
      public void actionPerformed(ActionEvent e)
      {
        try
        {   
          Date ud = sdf_date.parse(jtf_tm_2.getText());

          int dd = Integer.parseInt(new SimpleDateFormat("dd").format(ud));
          int mm = Integer.parseInt(new SimpleDateFormat("MM").format(ud));
          int yy = Integer.parseInt(new SimpleDateFormat("yy").format(ud));

          printer.init();  
          ((FrKPrinter)printer).getFrDrv().confirmDate((byte)dd, (byte)mm, (byte)yy);
          
        } catch (Exception ex)
        {
          UserDlg.showError(FrParamsPanel.this, ex.getMessage());
        }
      }
    };

    cancel_test = new AbstractAction("Отменить технологический тест")
    {
      public void actionPerformed(ActionEvent e)
      {
        try
        {   
          printer.init();
          printer.interruptTest();
          
        } catch (Exception ex)
        {
          UserDlg.showError(FrParamsPanel.this, ex.getMessage());
        }
      }
    };

    refr_state = new AbstractAction("Запрос состояния")
    {
      public void actionPerformed(ActionEvent e)
      {
        try
        {   
          printer.init();
          StateA st = ((FrKPrinter)printer).stateRequest();
          StringBuffer sb = new StringBuffer();

          st.printAll(sb);

          jta_state.setText(sb.toString());
        } catch (Exception ex)
        {
          UserDlg.showError(FrParamsPanel.this, ex.getMessage());
        }
      }

    };
    

  }

  public void setFiscalPrinter(FiscalPrinter fp)
  {
    printer = fp;
  }


  public void refreshData(Properties p)
  {
    
  }

  public boolean isDataChanged()
  {
    return false;
  }

  public String saveData(Properties p)
  {
    return null;
  }

}