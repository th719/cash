package th.cash.ui.sale;

import java.awt.Toolkit;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ProgressFrame extends JFrame implements ProcessShow 
{

  private JLabel jl_info;

  public ProgressFrame()
  {
    this("ProgressFrame");
  }
  
  
  public ProgressFrame(String title)
  {
    super(title);
    setResizable(false);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

    jl_info = new JLabel("122435");

    JPanel mp = new JPanel(new FlowLayout());
    mp.add(jl_info);
    
    
    setContentPane(mp);


    //pack();

    setSize(400, 65);
    Dimension d = Toolkit.getDefaultToolkit().getDefaultToolkit().getScreenSize();
    setLocation(d.width / 2 - getSize().width / 2, d.height / 2 - getSize().height / 2);
  }


  public void setText(String text)
  {
    jl_info.setText(text); 
  }

//  public void show()
//  {
//    SwingUtilities.invokeAndWait(new Runnable() { public void run(){ ProgressFrame.this.setVisible(true); } });
//  }

  public void addText(String s)
  {
    jl_info.setText( jl_info.getText() + ' ' + s );
  }
}