package th.cash.ui.paym;


import java.util.Date;

// класс - банковская операция
public class BankOper implements Comparable 

{

  // атрибуты, по которым транзакции сравниваются с отчетом
  private String card_no;
  private Date exp_date;

  private double amount;

  private String auth_code;
  private Date tr_date;
  // /////////////////////////////////////////////////////////////


  private char t_type;


  // from transactions
  private String trm_n;
  private String msg_n;
  
  

  // from report 
  private String trm_id;
  private String merch_n;
  private int check_num = -1;
  
  
  
  
  
  public BankOper(String card_no, Date exp_date, double amount, String auth_code, Date tr_date, char ttype)
  {
    this.card_no = card_no;
    this.exp_date = exp_date;
    this.amount = amount;
    this.auth_code = auth_code;
    this.tr_date = tr_date;
    this.t_type = ttype;
  }

  // это только для транзакций отчета
  public void setCheckNum(int val)
  {
    check_num = val;
  }

  public int getCgeckNum()
  {
    return check_num;
  }

  public boolean equals(Object o)
  {
    if (o == null) return false;

    BankOper bo = (BankOper)o;

    boolean res = true;

    res = res && tr_date.equals(bo.tr_date);
    res = res && auth_code.equals(bo.auth_code);
    res = res && card_no.equals(bo.card_no);
    res = res && exp_date.equals(bo.exp_date);
    res = res && amount == bo.amount;
    res = res && t_type == bo.t_type;

    return res;
  }

  public int compareTo(Object o)
  {
    if (o == null) return -1;

    BankOper bo = (BankOper)o;

    int res = 0;
    if (res == 0) res = tr_date.compareTo(bo.tr_date);
    if (res == 0) res = auth_code.compareTo(bo.auth_code);
    if (res == 0) res = card_no.compareTo(bo.card_no);
    if (res == 0) res = exp_date.compareTo(bo.exp_date);
    if (res == 0) res = Double.compare(amount, bo.amount);
    if (res == 0) res = t_type - bo.t_type;
    return res;
  }

  public double getAmount()
  {
    return amount;
  }

  public String getAuthCode()
  {
    return auth_code;
  }

  public String getCardNo()
  {
    return card_no;
  }

  public Date getExpDate()
  {
    return exp_date;
  }

  public Date getTrDate()
  {
    return tr_date;
  }

  public char getTType()
  {
    return t_type;
  }
  


  

  
}