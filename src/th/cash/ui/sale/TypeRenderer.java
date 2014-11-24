package th.cash.ui.sale;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import java.awt.Component;
import javax.swing.JTable;
import java.awt.Color;

public class TypeRenderer extends DefaultTableCellRenderer implements ColoredRenderer
{
  private ImageIcon ok_img, cancel_img;
  private Integer cancel_value;

  private Color n_fg, n_bg, s_fg, s_bg;


  public TypeRenderer(Integer val)
  {
    super();
    cancel_value = val;
    setHorizontalAlignment(JLabel.CENTER);
    ok_img = new ImageIcon(getClass().getResource("/th/common/ui/res/stock_ok-16.png"));
    cancel_img = new ImageIcon(getClass().getResource("/th/common/ui/res/stock_cancel-16.png"));
  }

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
  {
    Component c = super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);

    if (cancel_value.equals(value)) setIcon(cancel_img); else setIcon(ok_img);

    if (isSelected)
    {
      c.setForeground(s_fg); c.setBackground(s_bg);
      
    } else
    {
      c.setForeground(n_fg); c.setBackground(n_bg);
    }

    return c;
  }

//  public void setNormSelColors(Color norm_fg, Color norm_bg, Color sel_fg, Color sel_bg)
//  {
//    n_fg = norm_fg; n_bg = norm_bg; s_fg = sel_fg; s_bg = sel_bg;
//  }

  public void setColors(TbColorSettins cset)
  {
    n_fg = cset.getNormalFg();
    n_bg = cset.getNormalBg();
    s_fg = cset.getSelectedFg();
    s_bg = cset.getSelectedBg();
  }

  
}