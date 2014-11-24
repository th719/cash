package th.cash.ui.sale.dsc;


import java.awt.Frame;

import java.util.Vector;
import javax.swing.JDialog;

import th.cash.model.FixedDsc;

import org.apache.log4j.Logger;

public class FDscSelDialog extends JDialog 
{

  private FDscSelectPanel mp;
  
  public FDscSelDialog(Frame owner)
  {
    super(owner, "Выбор скидки", true);

    mp = new FDscSelectPanel(this);

    setContentPane(mp);

    setSize(520, 360);
  }

  public FixedDsc refresh(Vector fdsc_list)
  {
    mp.refreshData(fdsc_list);
    setLocationRelativeTo(getOwner());
    show();
    return mp.getSelectedDsc(); // mp.isSelected();
  }

}