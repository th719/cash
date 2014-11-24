package th.cash.ui.paym;

import java.awt.Frame;

import javax.swing.JDialog;


import org.apache.log4j.Logger;
import th.cash.card.PaymentSelector;


public class PaymentSelectDialog extends JDialog implements PaymentSelector
{

  private PaymentSelectPanel mp;
  
  public PaymentSelectDialog(Frame owner, String pay_ks, Logger log)
  {
    super(owner, "Оплата", true);

    mp = new PaymentSelectPanel(this, pay_ks, log);

    setContentPane(mp);

    setSize(520, 360);
  }

  public boolean selPaymentTypes(double check_sum, double nal_sum, double bn_sum)
  {
    mp.refreshData(check_sum, nal_sum, bn_sum);
    setLocationRelativeTo(getOwner());
    show();
    return mp.isSelected();
  }

  public double getNalSum()
  {
    return mp.getNalSum();
  }

  public double getBnSum()
  {
    return mp.getBnSum();
  }
}