package th.cash.ui.paym;

import th.cash.card.PaymentSelector;
import th.cash.abg.AuthData;
import th.cash.abg.FConst;

public class TestCard 
{
  public static void main(String[] args)
  {

    PaymentSelector ps = new PaymentSelectDialog(null, "control ENTER", null);

    if (ps.selPaymentTypes(1010.50, 135.40, 0 ))
    {

      System.out.println("ns = " + ps.getNalSum());
      System.out.println("bn = " + ps.getBnSum());

    }

    CardInputDialog cid = new CardInputDialog(null);
    cid.show();
    System.out.println(cid.getTrackData()); 

//    double sum = 1.55;
    String trm_num = "011";

    try
    {
     AuthData req = AuthData.createAuthData(null, cid.getTrackData(), FConst.TT_SALE, ps.getBnSum(), trm_num);

      req.print();
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }


    System.exit(0);
  }

}