package th.cash.fr.doc;

import java.util.Date;
/**
 * для журнала транзакций
 */
public class Transaction 
{
  // transaction types
  public final static int REG_POS        = 11;
  public final static int STORNO_POS     = 12;
  public final static int RET_POS        = 13;
  public final static int RET_IN_SALE    = 14;
  public final static int REG_VALUE      = 24;

  public final static int ITOG_DSC_SUM   = 35;
  public final static int ITOG_DSC_PC    = 37;

  public final static int PAYMENT        = 40;
  public final static int PAY_IN         = 50;
  public final static int PAY_OUT        = 51;
  public final static int CLOSE_CHECK    = 55;
  public final static int CANCEL_CHECK   = 56;

   public final static int XREP           = 60;
  public final static int ZREP           = 61;
//  public final static int EXT_REP        = 62;
  public final static int DAY_CARD_REP   = 63;

  public final static int POS_DSC_DET    = 70;
  public final static int CHECK_DSC_DET  = 71;

  public final static int NALOG          = 75;
  public final static int MONEY_BOX      = 65;
  // added 25.03.09
  public final static int LOGIN          = 101;
  public final static int LOGOFF         = 102;
  public final static int ERRLOGIN       = 103;

  public final static int START          = 104;
  public final static int STOP           = 105;

  public final static int DIAGNOSTIC     = 110;

  public final static int BN_TR          = 120; 

  // values
  private int       num;            // -1 - no_data
  private Date      trDate;
  private int       type;
  private int       kkmNum;
  private int       checkNum;
  private int       cashierNum;
  private String    str;
  private int       ival;           // 0 - no data
  private double    d10, d11, d12;
  private String    gname;
  private Integer   gtaxId;
  private boolean   printed;

  public Transaction(int num, Date date, int type, int k_n, int check_n, int cashier_n, 
                     String str, int ival, double d10, double d11, double d12, 
                     String gname, Integer gt_id)
  {
     this.num = num;
     this.trDate = date == null ? new Date() : date;
     this.type = type;
     this.kkmNum = k_n;
     this.checkNum = check_n;
     this.cashierNum = cashier_n;
     this.str = str;
     this.ival = ival;
     this.d10 = d10;
     this.d11 = d11;
     this.d12 = d12;
     this.gname = gname;
     this.gtaxId = gt_id;
     this.printed = false;
  }

  // static factory
  public static Transaction createRegPos(int k_n, int check_n, int cashier_n, 
                  int good_id, int sct, double price, double quan, double sum, 
                  String gname, Integer gt_id)
  {
    return new Transaction(-1, null, REG_POS, k_n, check_n, cashier_n, String.valueOf(good_id), sct, price, quan, sum, gname, gt_id);
  }

  public static Transaction createStornoPos(int k_n, int check_n, int cashier_n, 
                  int good_id, int sct, double price, double quan, double sum, 
                  String gname, Integer gt_id)
  {
    return new Transaction(-1, null, STORNO_POS, k_n, check_n, cashier_n, String.valueOf(good_id), sct, price, quan, sum, gname, gt_id);
  }

  public static Transaction createRetPos(int k_n, int check_n, int cashier_n, 
                  int good_id, int sct, double price, double quan, double sum, 
                  String gname, Integer gt_id)
  {
    return new Transaction(-1, null, RET_POS, k_n, check_n, cashier_n, String.valueOf(good_id), sct, price, quan, sum, gname, gt_id);
  }

  public static Transaction createRegValue(int k_n, int check_n, int cashier_n, 
                  String barcode, int sct, double koef)
  {
    return new Transaction(-1, null, REG_VALUE, k_n, check_n, cashier_n, barcode, sct, 0, koef, 0, null, null);
  }

  public static Transaction createPayment(int k_n, int check_n, int cashier_n, 
                  String card, double change, double pay_type_num, double sum)
  {
    return new Transaction(-1, null, PAYMENT, k_n, check_n, cashier_n, card, 0, change, pay_type_num, sum, null, null);
  }

  public static Transaction createPayIn(int k_n, int check_n, int cashier_n, double sum)
  {
    return new Transaction(-1, null, PAY_IN, k_n, check_n, cashier_n, "0", 0, 0, 0, sum, null, null);
  }

  public static Transaction createPayOut(int k_n, int check_n, int cashier_n, double sum)
  {
    return new Transaction(-1, null, PAY_OUT, k_n, check_n, cashier_n, "0", 0, 0, 0, sum, null, null);
  }
  
  public static Transaction createCloseCheck(int k_n, int check_n, int cashier_n, double sum)
  {
    return new Transaction(-1, null, CLOSE_CHECK, k_n, check_n, cashier_n, "0", 0, 0, 0, sum, null, null);
  }

  public static Transaction createCancelCheck(int k_n, int check_n, int cashier_n, double sum)
  {
    return new Transaction(-1, null, CANCEL_CHECK, k_n, check_n, cashier_n, "0", 0, 0, 0, sum, null, null);
  }

  public static Transaction createZRep(int k_n, int check_n, int cashier_n, 
                             int rep_num, double cash_nal, double sum)
  {
    return new Transaction(-1, null, ZREP, k_n, check_n, cashier_n, String.valueOf(rep_num), 0, cash_nal, 0, sum, null, null);
  
  }
  
  public static Transaction createNalog(int k_n, int check_n, int cashier_n, 
                              int ncode, double obnal, double sum)
  {
    return new Transaction(-1, null, NALOG, k_n, check_n, cashier_n, String.valueOf(ncode), 0, obnal, 0, sum, null, null);
  }

  public static Transaction createOpenMoneyBox(int k_n, int check_n, int cashier_n)
  {
    return new Transaction(-1, null, MONEY_BOX, k_n, check_n, cashier_n, "0", 0, 0, 0, 0, null, null);
  }

  public static Transaction createLoginMark(int k_n, int cashier_n, String win)
  {
    return new Transaction(-1, null, LOGIN, k_n, 0, cashier_n, win == null ? "0" : win, 0, 0, 0, 0, null, null);
  }

  public static Transaction createLogoffMark(int k_n, int cashier_n)
  {
    return new Transaction(-1, null, LOGOFF, k_n, 0, cashier_n, "0", 0, 0, 0, 0, null, null);
  }

  public static Transaction createStartMark(int k_n, String ver)
  {
    return new Transaction(-1, null, START, k_n, 0, 0, ver == null ? "?" : ver, 0, 0, 0, 0, null, null);
  }

  public static Transaction createStopMark(int k_n)
  {
    return new Transaction(-1, null, STOP, k_n, 0, 0, "0" , 0, 0, 0, 0, null, null);
  }

  public static Transaction createEKLZFull(int k_n)
  {
    return new Transaction(-1, null, DIAGNOSTIC, k_n, 0, 0, "0" , 1, 0, 0, 0, null, null);
  }

//  public static Transaction createCheckDscDet(int k_n, String card_n, int dsc_kind, double p2, double dpc, double dsum)
//  {
//    return new Transaction(-1, null, CHECK_DSC_DET, k_n, 0, 0, card_n, dsc_kind, p2, dpc, dsum, null, null);
//  }

  // getters
  public Date getTrDate() { return trDate; }

  public int getType() { return type; }

  public String getStr() { return str; }

  public int getKkmNum() { return kkmNum; }

  public int getIval() { return ival; }

  public int getCheckNum() { return checkNum; }

  public void setCheckNum(int num) { checkNum = num; }

  public int getCashierNum() { return cashierNum; }

  public double getD10() { return d10; }

  public double getD11() { return d11; }

  public double getD12() { return d12; }

  public String getGname() { return gname; }

  public Integer getGtaxId() { return gtaxId; }

  public boolean isPrinted() { return printed; }

  public void setPrinted(boolean v) { printed = v; }
  
}