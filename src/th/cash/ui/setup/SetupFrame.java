package th.cash.ui.setup;

import javax.swing.*;

import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.*;

import th.cash.env.KKMEnv;
import th.cash.ui.util.UserDlg;

import java.sql.SQLException;

public class SetupFrame extends JFrame implements WindowListener
{
  private SetupPanel main_panel;

  private KKMEnv cashEnv;
  
  public SetupFrame()
  {
    super("Настройки");

    main_panel = new SetupPanel();

    JPanel ctrlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    Action apply = new AbstractAction( SetupPanel.APPLY_S )
    {
      public void actionPerformed(ActionEvent e){ do_apply();}
    };

    Action save = new AbstractAction( SetupPanel.SAVE_S )
    {
      public void actionPerformed(ActionEvent e){ do_save();}
    };

    Action cancel = new AbstractAction( SetupPanel.CANCEL_S )
    {
      public void actionPerformed(ActionEvent e){ do_cancel();}
    };

    ctrlPanel.add(new JButton(apply));
    ctrlPanel.add(new JButton(save));
    ctrlPanel.add(new JButton(cancel));

    getContentPane().add(main_panel, BorderLayout.CENTER);
    getContentPane().add(ctrlPanel, BorderLayout.SOUTH);
    
    setSize(640,480);
    addWindowListener(this);

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

  }

  private void do_apply()
  {
    main_panel.saveAll();
  }

  private void do_save()
  {
    if (main_panel.saveAll()) do_cancel();
  }

  private void do_cancel()
  {
    if (isVisible()) hide();
  }

  public void hide()
  {
    super.hide(); 
    processWindowEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSED));
  }


  public void setCashEnv(KKMEnv env)
  {
    cashEnv = env;
  }

  public void refreshData()
  {
    main_panel.refreshData(cashEnv);
  }

  public boolean showFrame()
  {
    boolean res = cashEnv.getCurrentUser().getRole().isSetup();
    if (res)
    {
      refreshData();
      show();
    } else
    {
      UserDlg.showError(this, "Недостаточно прав для настройки программы!");
    }
    return res;
  }


  // *****************************************************
  public void windowOpened(WindowEvent e)
  {
  }

  public void windowClosing(WindowEvent e)
  {
    do_cancel();
  }

  public void windowClosed(WindowEvent e)
  {
  }

  public void windowIconified(WindowEvent e)
  {
  }

  public void windowDeiconified(WindowEvent e)
  {
  }

  public void windowActivated(WindowEvent e)
  {
  }

  public void windowDeactivated(WindowEvent e)
  {
  }
  
}