package th.cash.ui.setup;

import java.util.Properties;

import java.awt.FlowLayout;
import java.awt.BorderLayout;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.AbstractAction;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JTable;

import th.common.ui.table.TableScroller;
import th.common.ui.table.TableUtils;

import th.cash.fr.table.FrTable;
import th.cash.fr.FrException;
import th.cash.dev.FiscalPrinter;
import th.cash.fr.FrKPrinter;


import th.cash.ui.util.UserDlg;


// таблицы ФР
public class FrTablesPanel extends TabSetPanel
{
  private JComboBox jcb_tab_num;
  private JTable table;
  private TableScroller scroller;
  private FrTable fr_table = null;
  private FrSetTableModel model;

  private FiscalPrinter printer;
  
  private final static String[] FR_TAB_NAMES = {
    "1 Тип и режимы кассы", 
    "2 Пароли кассиров и администраторов", 
    "3 Таблица перевода времени", 
    "4 Текст в чеке", 
    "5 Наименование типов оплаты", 
    "6 Налоговые ставки", 
    "7 Наименование отделов", 
    "8 Настройки шрифтов"};
  
  public FrTablesPanel()
  {
    init();
  }

  private void init()
  {
    JPanel ctrlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

    jcb_tab_num = new JComboBox(FR_TAB_NAMES);
    
    Action load = new AbstractAction("Прочитать")
    {
      public void actionPerformed(ActionEvent e) {do_load();}
    };

    Action save = new AbstractAction("Записать")
    {
      public void actionPerformed(ActionEvent e) {do_save();}
    };

    Action db_save = new AbstractAction("В настройки")
    {
      public void actionPerformed(ActionEvent e) {do_db_save();}
    };
    db_save.setEnabled(false);

    ctrlPanel.add(new JLabel("Таб."));
    ctrlPanel.add(jcb_tab_num);
    ctrlPanel.add(new JButton(load));
    ctrlPanel.add(new JButton(save));
    ctrlPanel.add(new JButton(db_save));

    //fr_table = new FrTable((byte)0);
    model = new FrSetTableModel(fr_table); 
    table = new JTable(model);
    
    scroller = new TableScroller(table);


    setLayout(new BorderLayout());

    add(ctrlPanel, BorderLayout.NORTH);
    add(scroller, BorderLayout.CENTER);
  }

  // ширина стролбцов (зависит от таблицы)
  private int[] initColWidth()
  {
    int[] res;

    res = new int[model.getColumnCount()];
    if (res.length > 0)
    {
      res[0] = 40;
      for (int i = 1; i < res.length; i++)
        if (String.class.equals(model.getColumnClass(i))) res[i] = 400; else res[i] = 100;
    }
    return res;
  }

  // прочитать выбранную таблицу из фискальника
  public void do_load()
  {
    int tab_num = jcb_tab_num.getSelectedIndex() + 1;
    try
    {
      fr_table = new FrTable((byte)tab_num);
      printer.init();
      fr_table.initFromFr( ((FrKPrinter)printer).getFrDrv() );
      fr_table.printAll();
      model.setSrcTable(fr_table);
      scroller.setRelatedColWidth(initColWidth());
    } catch (Exception ex)
    {
      UserDlg.showError(this, ex.getMessage());
    }
  }

  // записать изменения в таблицу
  public void do_save()
  {
    try
    {
      if (fr_table != null)
      {
        TableUtils.stopTableEditing(table);
        fr_table.saveToFr( ((FrKPrinter)printer).getFrDrv() );
      }
    } catch (FrException ex)
    {
      UserDlg.showError(this, ex.getMessage());
    }
  }

  public void setFiscalPrinter(FiscalPrinter fp)
  {
    printer = fp;
    fr_table = null;
    model.setSrcTable(fr_table);
    jcb_tab_num.setSelectedIndex(0);
  }

  public void do_db_save()
  {
    
  }

  public boolean isDataChanged()
  {
    return false;
  }

  public void refreshData(Properties p)
  {
  }

  public String saveData(Properties p)
  {
    return null;
  }

}

