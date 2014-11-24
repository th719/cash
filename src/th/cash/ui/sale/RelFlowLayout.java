package th.cash.ui.sale;

import java.awt.LayoutManager;
import java.io.Serializable;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.Insets;

public class RelFlowLayout implements LayoutManager 
{
  private int[] rel_sizes;
  private int sum_sizes;
  private int col_cnt;
  
  public RelFlowLayout(int[] rs)
  {
    if (rs == null || rs.length == 0) 
      throw new IllegalArgumentException("Empty relative sizes array");
    rel_sizes = rs;
    col_cnt = rel_sizes.length;
    sum_sizes = 0;
    for (int i = 0; i < col_cnt; i++) sum_sizes += rel_sizes[i];
  }

  public void addLayoutComponent(String name, Component comp)
  {
  }

  public void removeLayoutComponent(Component comp)
  {
  }

  public Dimension preferredLayoutSize(Container parent)
  {
    synchronized (parent.getTreeLock()) 
    {
    	Insets insets = parent.getInsets();

      int com_num = parent.getComponentCount();
      int i = 0;
      int max_height = 0;
      int lw = 0;
      Dimension d;  
      while (i < col_cnt && i < com_num)
      {
	      d = parent.getComponent(i).getPreferredSize();
        if (d.height > max_height) max_height = d.height;
        lw += d.width;
        i++;
      }
      return new Dimension(insets.left + insets.right + lw/*parent.getPreferredSize().width*/,
                           insets.top + insets.bottom + max_height);
    }  
  }

  public Dimension minimumLayoutSize(Container parent)
  {
    synchronized (parent.getTreeLock()) 
    {
    	Insets insets = parent.getInsets();

      int com_num = parent.getComponentCount();
      int i = 0;
      int max_height = 0;
      int lw = 0;
      Dimension d;  
      while (i < col_cnt && i < com_num)
      {
	      d = parent.getComponent(i).getMinimumSize();
        if (d.height > max_height) max_height = d.height;
        lw += d.width;
        i++;
      }
      return new Dimension(insets.left + insets.right + lw /*parent.getMinimumSize().width*/,
                           insets.top + insets.bottom + max_height);
    }  
  }

  public void layoutContainer(Container parent)
  {
    synchronized (parent.getTreeLock()) 
    {
    	Insets insets = parent.getInsets();
    	int com_num = parent.getComponentCount();

      if (com_num > 0)
      {
        // comon width & height
        int w = parent.getWidth() - (insets.left + insets.right);
        int h = parent.getHeight() - (insets.top + insets.bottom);

        int i = 0;
        int x = insets.left, y = insets.top, cw, ch;
        Component c;
        while (i < col_cnt && i < com_num)
        {
          c = parent.getComponent(i);
          cw = w * rel_sizes[i] / sum_sizes;
          c.setBounds(x, y, cw, h);
          x += cw;
          i++;
        }
      }
    }
  }
}