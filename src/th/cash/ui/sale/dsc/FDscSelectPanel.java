package th.cash.ui.sale.dsc;

import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JTable;

import java.awt.Dialog;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;

import java.awt.BorderLayout;

import javax.swing.Action;
import javax.swing.AbstractAction;

import th.common.db.VectorDataModel;
import th.common.db.VRow;
import th.common.ui.table.TableScroller;
import th.common.ui.table.TableSearch;
import th.common.ui.table.DoubleRenderer;

import th.cash.model.FixedDsc;

public class FDscSelectPanel extends JPanel
{

  private final static Class[] COL_CLASSES = new Class[] { String.class, Double.class, String.class };
  private final static int[] COL_WIDTH = new int[] { 100, 70, 20 };
  private JTable jt_fdsc_list;
  private VectorDataModel model;
  private TFDscCashe view;
  private Dialog owner;
  private int selIndex;
  
  private Action ok, cancel;

  public FDscSelectPanel()
  {
    this(null);
  }

  public FDscSelectPanel(Dialog o)
  {
    owner = o;
    init();
  }

  public void setOwner(Dialog o)
  {
    owner = o;
  }

  public void init()
  {
    setLayout(new BorderLayout());

    ok = new AbstractAction("Ok")
    {
      public void actionPerformed(ActionEvent e)
      {
        selIndex = jt_fdsc_list.getSelectedRow();
        if (owner != null) owner.hide();
      }
    };

    cancel = new AbstractAction("Отмена")
    {
      public void actionPerformed(ActionEvent e)
      {
        selIndex = -1;
        if (owner != null) owner.hide();
      }
    };


    view = new TFDscCashe();
    model = new VectorDataModel(view, view, new String[]{"Название", "Значение", "Ед"}, COL_CLASSES);
    model.setEditable(false);

    jt_fdsc_list = new JTable(model);

    TableScroller scroller = new TableScroller(jt_fdsc_list);

    scroller.setRelatedColWidth(COL_WIDTH);

    jt_fdsc_list.getColumnModel().getColumn(1).setCellRenderer(new DoubleRenderer(2));

    JPanel ctrlPanel;

    ctrlPanel = new JPanel();

    ctrlPanel.add(new JButton(ok));
    ctrlPanel.add(new JButton(cancel));

    jt_fdsc_list.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
      KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "none");
      
    getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(
      KeyEvent.VK_ENTER, 0),"select");
    getActionMap().put("select", ok);


    jt_fdsc_list.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
      KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "none");
    
    getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(
      KeyEvent.VK_ESCAPE, 0),"cancel");
    getActionMap().put("cancel", cancel);
    


    setLayout(new BorderLayout());
    add(scroller, BorderLayout.CENTER);
    add(ctrlPanel, BorderLayout.SOUTH);
    
    
  }


  public void refreshData(Vector fixed_dsc)
  {
    view.refresh(fixed_dsc);
    model.fireTableDataChanged();
    TableSearch.selectRow(jt_fdsc_list, 0);

 
    selIndex = -1;

  }


  public int getSelectedIndex()
  {
    return selIndex;
  }


  public FixedDsc getSelectedDsc()
  {
    if (selIndex < 0)
      return null;
    else
      return (FixedDsc)((VRow)view.get(selIndex)).get(TFDscCashe.FDSC);
  }
  

  
}