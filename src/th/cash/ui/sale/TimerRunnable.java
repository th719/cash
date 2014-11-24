package th.cash.ui.sale;

import java.util.Date;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import java.text.SimpleDateFormat;

/**
 * Часики :-)
 */
public class TimerRunnable implements Runnable
{
  private JLabel label;
  private SimpleDateFormat sdf;
  private boolean cont;

  public TimerRunnable(JLabel dc)
  {
    this(dc, "dd.MM.yy HH:mm");
  }
  
  public TimerRunnable(JLabel dc, String fmt)
  {
    label = dc;
    sdf = new SimpleDateFormat(fmt);
    cont = true;
  }

  public void setStopped()
  {
    cont = false;
  }

  public void run()
  {
    try
    {
      while (cont) 
      {
        // display
        SwingUtilities.invokeLater(
          new Runnable() 
          {
            public void run()
            {
              label.setText(sdf.format(new Date()));
            }
          }
        );
        Thread.currentThread().sleep(1000);
      }
    } catch (InterruptedException ex) {}
  }
}