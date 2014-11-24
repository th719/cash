package th.cash.ui.sale;

import java.awt.Component;
import java.awt.Color;
import javax.swing.JTable;
import th.common.ui.table.DoubleRenderer;

public class DoubleColoredRenderer extends DoubleRenderer implements ColoredRenderer 
{
  private Color n_fg, n_bg, s_fg, s_bg;

  public DoubleColoredRenderer(int ndig)
  {
    super(ndig);
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

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
  {
    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    if (isSelected)
    {
      c.setForeground(s_fg); c.setBackground(s_bg);
    } else
    {
      c.setForeground(n_fg); c.setBackground(n_bg);
    }
    
    return c;
  }

  
}