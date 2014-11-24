package th.cash.fr.doc;

/**
 * Итог по группе налога
 */
public class TaxSum 
{

  private Integer gtaxId;
  private String tname;
  private double tpc;
  private double sum = 0;

  public TaxSum(Integer gr_id, String name, double pc)
  {
    gtaxId = gr_id;
    tname = name;
    tpc = pc;
    sum = 0;
  }

  public Integer getGtaxId() { return gtaxId; }
  public String getTname() { return tname; }
  public double getTaxPc() { return tpc; }
  public double getSum() { return sum; }
  public void setSum(double s) { sum = s; }
}