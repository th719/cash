package th.cash.test;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class KeyTestFrame extends JFrame implements KeyListener
{
  private JLabel lbl = new JLabel("Test");
  
  public KeyTestFrame()
  {
    super("KeyTestFrame");

    JPanel p = new JPanel(new FlowLayout());
    setContentPane(p);
//    InputLabel lbl = new InputLabel();

    Font fnt = new Font("Dialog", Font.BOLD, 14);
    lbl.setFont(fnt);
    lbl.setBorder(BorderFactory.createEtchedBorder());

    
    //addKeyListener(lbl); 
//    addKeyListener(lbl);
    addKeyListener(this);
    getRootPane().addKeyListener(this);
    
    p.add(lbl);

//    JLabel lb2 = new JLabel("1435LQqPp");
//    lb2.setFont(fnt);
//    lb2.setBorder(BorderFactory.createEtchedBorder());
//
//    p.add(lb2);

    setSize(400, 200);

    setDefaultCloseOperation(EXIT_ON_CLOSE);

  }

  public void keyPressed(KeyEvent p0)
  {
  }

  public void keyReleased(KeyEvent p0)
  {
  }

  public void keyTyped(KeyEvent p0)
  {
    String s = p0.getKeyModifiersText(p0.getModifiers()) + '+' + p0.getKeyChar();
//    String s = p0.getKeyModifiersText(p0.getModifiers()) + '+' + p0.getKeyText(p0.getKeyCode());
    lbl.setText(s);
    System.out.println(p0.paramString());
    
  }

  public static void main(String[] args)
  {
    
  
    new KeyTestFrame().show();

    
  }
  
}