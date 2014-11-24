package th.cash.test;

import java.awt.*;
import javax.swing.*;

import th.cash.ui.sale.InputLabel;

import th.cash.ui.sale.ProgressFrame;

public class LabelTestFrame extends JFrame
{
  public LabelTestFrame()
  {
    super("LabelTestFrame");
    JPanel p = new JPanel(new FlowLayout());
    setContentPane(p);
    InputLabel lbl = new InputLabel();

    Font fnt = new Font("Dialog", Font.BOLD, 72);
    lbl.setFont(fnt);
    lbl.setBorder(BorderFactory.createEtchedBorder());

    
    //addKeyListener(lbl); 
//    p.addKeyListener(lbl);
    getRootPane().addKeyListener(lbl);
    
    p.add(lbl);

    JLabel lb2 = new JLabel("1435LQqPp");
    lb2.setFont(fnt);
    lb2.setBorder(BorderFactory.createEtchedBorder());

    p.add(lb2);

    setSize(400, 200);

    setDefaultCloseOperation(EXIT_ON_CLOSE);

    System.out.println( lbl.getPreferredSize() );
//    System.out.println( lbl.set);
  }

  public static void main(String[] args)
  {
//    Font[] fa = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
//
//    for (int i = 0; i < fa.length; i++)
//    {
//      System.out.println(fa[i]);
//    }
    
  
    new LabelTestFrame().show();

//    ProgressFrame pf = new ProgressFrame("ProgressFrame");
//    pf.setText("test test test");
//    pf.rep
//    pf.show();
    
  }
}