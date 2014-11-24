package th.cash.ui.paym;

import java.awt.Frame;

import java.awt.Font;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import java.awt.event.KeyListener;

import javax.swing.KeyStroke;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BoxLayout;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.event.KeyEvent;

import th.common.ui.dialog.UserDlg;

/**
 * Для считывания карты
 * 
 * проблема с повторным использованием инстанса этого диалога
 */
public class CardInputDialog extends JDialog implements KeyListener
{

  private ForDigField fdate, f1, f2, f3, f4, fc;

  private boolean bykeyb = false;
  private boolean selected = false;
  private StringBuffer tbuf;
  private String track_data, expdate, cnum;
  private Action ok, cancel;

  private int conf_fails = 0; // счетчик количества ошибок подтверждения номера карты
  
  public CardInputDialog(Frame owner)
  {
    super(owner, "Введите карту", true);

    init();

    pack();
  }

  private void init()
  {

    FormLayout fl;
    CellConstraints cc;

    cc = new CellConstraints();

    fl = new FormLayout("4dlu, l:p, 2dlu, l:p, 2dlu, l:p, 2dlu, l:p, 2dlu, l:p, 4dlu", 
                        "4dlu, p, 2dlu, p, 2dlu, p, 4dlu");

    JPanel p1 = new JPanel();
    p1.setLayout(fl);


    fdate = new ForDigField();
    f1 = new ForDigField();
    f2 = new ForDigField();
    f3 = new ForDigField();
    f4 = new ForDigField();
    fc = new ForDigField();

    Font lbf;

    JLabel jl_date, jl_f1, jl_fc;


    jl_date = new JLabel("Дата:");
    jl_f1 = new JLabel("Номер:");
    jl_fc = new JLabel("Последние 4 цифры номера");
    
    lbf = jl_date.getFont().deriveFont((float)22);

    jl_date.setFont(lbf);
    jl_f1.setFont(lbf);
    jl_fc.setFont(lbf);

    p1.add(jl_date, cc.xy(2, 2));
    p1.add(fdate, cc.xy(4, 2));

    p1.add(jl_f1, cc.xy(2, 4));
    p1.add(f1, cc.xy(4, 4));
    p1.add(f2, cc.xy(6, 4));
    p1.add(f3, cc.xy(8, 4));
    p1.add(f4, cc.xy(10, 4));

    p1.add(jl_fc, cc.xywh(2, 6, 7, 1));
    p1.add(fc, cc.xy(10, 6));

    ok = new AbstractAction("Ок") 
    {
      public void actionPerformed(ActionEvent e) { do_ok(); }
    };

    cancel = new AbstractAction("Отмена") 
    {
      public void actionPerformed(ActionEvent e) { do_cancel(); }
    };

    fc.setEditable(true);
    

    JPanel p2 = new JPanel();
    p2.add(new JButton(ok));
    p2.add(new JButton(cancel));

    JPanel p = new JPanel();
    
    
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); 

    p.setLayout(new BorderLayout());
    p.add(p1, BorderLayout.CENTER);
    p.add(p2, BorderLayout.SOUTH);

    setContentPane(p);


    p.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "but_ok");
        p.getActionMap().put("but_ok", ok);

    p.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "but_cancel");
        p.getActionMap().put("but_cancel", cancel);

    
//    p.addKeyListener(this);
//    addKeyListener(this);
    fdate.addKeyListener(this);
  }

  public void clear()
  {
    bykeyb = false;
    selected = false;
    tbuf = null;
    expdate = null;  cnum = null; track_data = null;
    conf_fails = 0;
  }

  private void refrControls()
  {
    fdate.setText(expdate);
    f1.setText(cnum == null ? null : cnum.substring(0, 4));
    f2.setText(cnum == null ? null : cnum.substring(4, 8));
    f3.setText(cnum == null ? null : cnum.substring(8, 12));
//    f4.setText(cnum == null ? null : cnum.substring(12, 16));
    f4.setText(cnum == null ? null : "****");
    fc.setText("");
    fc.setEditable(cnum != null);
    
  }

  public void show()
  {
    clear();
    refrControls();
    fdate.requestFocus();
    super.show();
  }

  private void do_ok()
  {
   // System.out.println("do_ok()");

    if (System.currentTimeMillis() > lct + 500 && track_data != null)
    {
      String last4dig = cnum.substring(12, 16);
      String conf = fc.getText();
      boolean confirmed = last4dig.equals(conf);

      if (confirmed) 
      {
        selected = true; hide(); 
      }
      else 
      {
        if (conf_fails++ < 3)
        {
          UserDlg.showError(this, "Подтвердите последние 4 цифры карты");
          fc.setText(""); 
          fc.requestFocus();
        } else
        {
          UserDlg.showError(this, "Авторизация карты отменена");
          do_cancel();
        }
      }
    }
  }

  private void do_cancel()
  {
    selected = false;
    clear();
    hide();
  }

  public boolean isSelected() { return selected; }

  public boolean isReadFromKb() { return bykeyb; }

  public String getTrackData() { return track_data; }

// *    ;4188730003844303=07121211003839300000?

  private long lct = 0;
  public void keyTyped(KeyEvent e)
  {
    char c = e.getKeyChar();
    lct = System.currentTimeMillis();
//    System.out.println("keyTyped(" + c +")");
    if (Character.isDigit(c) || c == ';' || c == '=' || c == '?')
    {
      if (tbuf == null && expdate == null && cnum == null)
        bykeyb = c != ';';

      if (!bykeyb && tbuf == null) tbuf = new StringBuffer(40);

      if (!bykeyb) tbuf.append(c);

      if (c == '?') trackCompleted();
    }    
    
  }

  private void trackCompleted()
  {
    String s = tbuf.toString();

    int ieq = s.indexOf('=');
    if (ieq > 0) cnum = s.substring(1, ieq);
    int iqw = s.indexOf('?');
    if (iqw > ieq) expdate = s.substring(ieq + 1, ieq + 5);
    if (iqw > 0) track_data = s.substring(0, iqw + 1);

    refrControls();

    fc.requestFocus();
  }

  public void keyPressed(KeyEvent e)
  {
  }

  public void keyReleased(KeyEvent e)
  {
  }


}