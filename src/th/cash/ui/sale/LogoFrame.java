package th.cash.ui.sale;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class LogoFrame extends JFrame implements ProcessShow
{
  private JLabel jl_title;
  private JLabel jl_info;

  public LogoFrame()
  {
    super("Инициализация");
    setResizable(false);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

    jl_title = new JLabel();

    jl_info = new JLabel("Запуск");

    JPanel mp = new JPanel();

    mp.setLayout(new BorderLayout());

    ImageIcon logo = new ImageIcon(getClass().getResource("/logo.png"));
    jl_title.setIcon(logo);
    

    mp.add(jl_title, BorderLayout.CENTER);
    mp.add(jl_info, BorderLayout.SOUTH);

    mp.setBorder(BorderFactory.createRaisedBevelBorder());
    setContentPane(mp);

    pack();
    Dimension d = Toolkit.getDefaultToolkit().getDefaultToolkit().getScreenSize();
    setLocation(d.width / 2 - getSize().width / 2, d.height / 2 - getSize().height / 2);
  }

  public void setText(String text)
  {
    jl_info.setText(text);
  }

  public void addText(String s)
  {
    jl_info.setText( jl_info.getText() + ' ' + s );
  }
  
}