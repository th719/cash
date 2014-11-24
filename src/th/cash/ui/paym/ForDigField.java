package th.cash.ui.paym;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import javax.swing.JTextField;

public class ForDigField extends JTextField
{
  public ForDigField()
  {
    super(4);
    setEditable(false);

    setFont(getFont().deriveFont((float)22));

     getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "none");
    getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "none");
  }
}