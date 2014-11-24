package th.cash.ui.paym;

import java.awt.Font;
import java.awt.Dialog;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.KeyStroke;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JLabel;

import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.apache.log4j.Logger;


import th.common.ui.table.TableScroller;
import th.common.ui.table.DoubleRenderer;

import th.cash.ui.util.UserDlg;

import th.common.db.*;

import th.cash.ui.sale.InputLabel;

/**
 * Панелька для выбора способа оплаты суммы чека ...
 * открывается при явном запросе безналичной оплаты или
 * при закрытии чека суммой меньше итога чека ...
 * (разумеется, если безнал. оплата разрешена настройками )
 */
public class PaymentSelectPanel extends JPanel
{

  private InputLabel label;
  private JTable jt_pay_types;
  private final static String[] COL_NAMES = {"Вид оплаты", "Сумма"};

  private double check_sum, nal_sum, bn_sum; 

  private JLabel jl_text_check_sum, jl_check_sum;
  private JLabel jl_text_rest, jl_rest;

  private Dialog owner;
  private String pay_ks; // обрабытываются кнопки <Enter>, <Esc> и бн. оплата

  private Action ok, cancel, set_bn;

  private NumberFormat money_fmt;

  private boolean selected;
  private Logger log;
  
  
  public PaymentSelectPanel(Dialog owner, String pay_ks, Logger log)
  {
    this.owner = owner;
    this.pay_ks = pay_ks;
    this.log = log;

    init();
  }

  private void init()
  {
    setLayout(new BorderLayout());

    label  = new InputLabel();
    JPanel tp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    tp.add(label);

    VectorDataCashe view = new VectorDataCashe(2, 1);
    VectorDataModel model = new VectorDataModel(view, view, new String[]{"Вид оплаты", "Сумма"}, 
                                         new Class[] { String.class, Double.class });

    model.setEditable(false);                                    

    jt_pay_types = new JTable(model);

    VRow row;

    row = view.createNewRow(3);
    row.set(0, "Наличными");
    view.add(row);
    
    row = view.createNewRow(3);
    row.set(0, "Кредитом");
    view.add(row);

    model.fireTableDataChanged();

    jt_pay_types.getColumnModel().getColumn(1).setCellRenderer(new DoubleRenderer(2));

    TableScroller scroller = new TableScroller(jt_pay_types);
    
    JPanel p;
    JPanel bp = new JPanel(new GridLayout(0, 2));

    bp.add(jl_text_check_sum = new JLabel("Сумма чека:"));
    p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    p.add(jl_check_sum = new JLabel());
    bp.add(p);

    bp.add(jl_text_rest = new JLabel("Остаток:"));
    p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    p.add(jl_rest = new JLabel());
    bp.add(p);

   


    add(tp, BorderLayout.NORTH);
    add(scroller, BorderLayout.CENTER);
    add(bp, BorderLayout.SOUTH);


    DecimalFormatSymbols ds = new DecimalFormatSymbols(Locale.ENGLISH);
    ds.setDecimalSeparator('.');
    ds.setGroupingSeparator('\'');
    ds.setGroupingSeparator(' ');

    money_fmt = new DecimalFormat();
    ((DecimalFormat)money_fmt).setDecimalFormatSymbols(ds);
    money_fmt.setMinimumFractionDigits(2);
    money_fmt.setGroupingUsed(true);

    if (owner != null)
    {
      owner.addWindowListener(new WindowAdapter()
      {
        public void windowClosing(WindowEvent e) 
        {
          do_cancel();
        }
      });

    }
    initActions();
    jt_pay_types.addKeyListener(label);

  }

  private void initActions()
  {
    ok = new AbstractAction("Оплата")
    {
      public void actionPerformed(ActionEvent e) 
      {

        do_pay();
      }
    };

    cancel = new AbstractAction("Отмена")
    { 
      public void actionPerformed(ActionEvent e) 
      {
        do_cancel();
      }
    };

    set_bn = new AbstractAction("Сумма кредита")
    {
      public void actionPerformed(ActionEvent e) 
      {
        try
        {
          Double d = parseInput(label.getText());
        
          bn_sum = d == null ? 0 : d.doubleValue();
          /// 
          jl_rest.setText(money_fmt.format(check_sum - nal_sum - bn_sum));
          jt_pay_types.setValueAt(new Double(bn_sum), 1, 1);
          
        } catch (ParseException ex)
        {
          log.error(ex);
        }
      }
    };

      // блокируем некоторые сочетания для таблицы
    jt_pay_types.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "none");
    jt_pay_types.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "none");

    setFont();


    getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(pay_ks), "set_bn");
    getActionMap().put("set_bn", set_bn);

    getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ok");
    getActionMap().put("ok", ok);

    getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
    getActionMap().put("cancel", cancel);
    
  }

  private void do_pay()
  {
    if (bn_sum > 0 && bn_sum > check_sum)
    {
      String msg = "Сумма оплаты больше итога чека";
      //log.error(msg + ":" + money_fmt.format(nal_sum) + "+" + money_fmt.format(bn_sum) + " > " + money_fmt.format(check_sum));
      UserDlg.showError(owner, msg);
      
    } else
    {
      selected = true;
      owner.hide();
    }
  }

  private void do_cancel()
  {
    selected = false;
    owner.hide();
  }

  private void setFont()
  {
    JLabel tmp = new JLabel();
    Font def_font = tmp.getFont();

    label.setFont(def_font.deriveFont((float)80));

    Font lbl_font = def_font.deriveFont((float)22);

    jl_text_check_sum.setFont(lbl_font);
    jl_check_sum.setFont(lbl_font);
    jl_text_rest.setFont(lbl_font);
    jl_rest.setFont(lbl_font);


    jt_pay_types.setFont(lbl_font);
    jt_pay_types.getTableHeader().setFont(lbl_font);
//    jt_pay_types.set

    jt_pay_types.setRowHeight(jt_pay_types.getRowHeight() + 12);

  }

    private Double parseInput(String s) throws ParseException
    {
      Double d = null;
      Number res = money_fmt.parse(s);
      if (res instanceof Double) 
        d = (Double)res;
      else
        if (res instanceof Long)
          d = new Double(((Long)res).doubleValue());
      return d;
    }


  public void refreshData(double check_sum, double nal_sum, double bn_sum)
  {
    this.check_sum = check_sum;
    this.nal_sum = nal_sum;
    this.bn_sum = bn_sum;

    jl_check_sum.setText(money_fmt.format(check_sum));
    jl_rest.setText(money_fmt.format(check_sum - nal_sum - bn_sum));

    jt_pay_types.setValueAt(new Double(nal_sum), 0, 1);
    
    jt_pay_types.setValueAt(new Double(bn_sum), 1, 1);

    selected = false;
  }

  public boolean isSelected() { return selected; }

  public double getNalSum() { return nal_sum; }

  public double getBnSum() { return bn_sum; }
  
}