package th.cash.ui.sale;

import java.awt.Font;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.BorderLayout;

import java.util.Vector;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

import javax.swing.Action;
import javax.swing.AbstractAction;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JButton;
import java.awt.Dialog;

import th.common.db.VRow;
import th.common.db.VectorDataCashe;
import th.common.db.VectorDataModel;

import th.common.ui.table.DoubleRenderer;

import th.common.ui.table.TableScroller;

import th.cash.model.*;


public class GMSelectPanel extends JPanel 
{

  private final static String[] COL_NAMES = {"Код", "Наименование", "Цена"};
  private final static int[] COL_WIDTH = new int[]{50, 350, 80};
  private final static Class[] COL_CLASSES = new Class[]{Integer.class, String.class, Double.class};
  
  private JLabel jl_msg;
  private JTable table;
  private VectorDataModel model;
  private VectorDataCashe cashe;

  private Action ok, cancel;
  private Dialog owner;

  private int selIndex = -1;
  private Vector sgm;

  public GMSelectPanel()
  {
    this(null);
  }
  
  public GMSelectPanel(Dialog o)
  {
    owner = o;
    init();
  }

  public void setOwner(Dialog o)
  {
    owner = o;
  }
  

  private void init()
  {
    ok = new AbstractAction("Ok")
    {
      public void actionPerformed(ActionEvent e)
      {
        selIndex = table.getSelectedRow();
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
  
    cashe = new VectorDataCashe(3, 1);
    model = new VectorDataModel(cashe, cashe, COL_NAMES, COL_CLASSES);
    table = new JTable(model);

    TableScroller scroller = new TableScroller(table);

    scroller.setRelatedColWidth(COL_WIDTH);

    table.getColumnModel().getColumn(2).setCellRenderer(new DoubleRenderer(2));

    JPanel topPanel, ctrlPanel;

    topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    topPanel.add(jl_msg = new JLabel());
    jl_msg.setForeground(Color.BLACK);
    

    ctrlPanel = new JPanel();

    ctrlPanel.add(new JButton(ok));
    ctrlPanel.add(new JButton(cancel));

    table.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
      KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "none");
      
    getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(
      KeyEvent.VK_ENTER, 0),"select");
    getActionMap().put("select", ok);


    table.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
      KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "none");
    
    getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(
      KeyEvent.VK_ESCAPE, 0),"cancel");
    getActionMap().put("cancel", cancel);
    


    setLayout(new BorderLayout());
    add(topPanel, BorderLayout.NORTH);
    add(scroller, BorderLayout.CENTER);
    add(ctrlPanel, BorderLayout.SOUTH);
  }

  public void initTable(TbColorSettins cset, Font tb_font)
  {
    StringColoredRenderer rid  = new StringColoredRenderer();
    StringColoredRenderer rname  = new StringColoredRenderer();
    DoubleColoredRenderer rmoney = new DoubleColoredRenderer(2);

    rid.setColors(cset);
    rname.setColors(cset);
    rmoney.setColors(cset);

    table.getColumnModel().getColumn(0).setCellRenderer(rid);
    table.getColumnModel().getColumn(1).setCellRenderer(rname);
    table.getColumnModel().getColumn(2).setCellRenderer(rmoney);
    
    table.setRowHeight(table.getRowHeight() + 3);
    table.setFont(tb_font);
  }


  public void refreshData(String msg, Vector list)
  {
    GoodMain gm;
    VRow row;
    sgm = list;
    
    cashe.clear();
    
    for (int i = 0; i < list.size(); i++)
    {
      gm = (GoodMain)list.get(i);

      row = VectorDataCashe.createRow(4);
      row.set(0, gm.getGoodId());
      row.set(1, gm.getName());
      row.set(2, gm.getPrice());
      row.setRefreshed();

      cashe.add(row);
    }

    model.fireTableDataChanged();
    table.getSelectionModel().setSelectionInterval(0, 0);

    jl_msg.setText(msg);
 
    selIndex = -1;
  }

  public int getSelectedIndex()
  {
    return selIndex;
  }

  public GoodMain getSelectedGM()
  {
    if (selIndex < 0)
      return null;
    else
      return (GoodMain)sgm.get(selIndex);
  }
}