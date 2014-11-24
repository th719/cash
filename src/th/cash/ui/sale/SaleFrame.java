package th.cash.ui.sale;

import javax.swing.JWindow;
import javax.swing.JFrame;
import java.awt.event.KeyEvent;
import java.awt.Toolkit;
import java.awt.Dimension;

import java.awt.event.WindowEvent;

import th.cash.env.KKMEnv;
// обертка для SalePanel
public class SaleFrame extends JFrame
{
  private SalePanel mainPanel;
  
  public SaleFrame()
  {
    super("Регистрация продаж");
    init();
  }


  private void init()
  {
    mainPanel = new SalePanel();
    setContentPane(mainPanel);
    mainPanel.setOwner(this);
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    d = new Dimension(640, 480);
//    d = new Dimension(800, 600);
    mainPanel.setPreferredSize(d);
    setSize(d);
    setLocation(0,0);
    mainPanel.relativeFontsSize(d);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
  }

  public void setCashEnv(KKMEnv env)
  {
    mainPanel.setCashEnv(env);
  }
  
  public boolean showFrame()
  {
    return mainPanel.firstLogin();
  }

  public void hide()
  {
    super.hide();
    processWindowEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSED));    
  }

}