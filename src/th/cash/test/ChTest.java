package th.cash.test;

import th.cash.fr.table.FrTable;
import th.cash.fr.*;
import th.cash.fr.state.*;
import th.cash.fr.doc.*;

public class ChTest 
{

  public final static byte CHECK_TYPE = 0;
  public final static byte ZERO_BYTE = 0;

  public static void main(String[] args)
  {
    
    try
    {
      FrKPrinter pr = new FrKPrinter("/dev/ttyS0", 100);
      pr.init();

//      pr.makeClrZReport();
//      pr.getFrDrv().calcelCheck();
      
      pr.getFrDrv().salePosition(1000, 1, 0, ZERO_BYTE, ZERO_BYTE, ZERO_BYTE, ZERO_BYTE, "тестовая позиция");
      pr.getFrDrv().closeCheck(1, 0, 0, 0, 0, ZERO_BYTE,ZERO_BYTE,ZERO_BYTE,ZERO_BYTE, "закрытие");
      

      pr.close();    

    }
    catch (FrException fex)
    {
      System.out.println("code=" + fex.getErrorCode());
      fex.printStackTrace();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    System.exit(0);
  }
}