package th.cash.model;

public class Tax implements Comparable
{
  private Integer taxId;
  private String taxName;
  private Integer gtaxId;
  private Double  taxPc;   // ставка налога
  private Integer kkmNum;  // номер в регистраторе

  public Tax(Integer taxId, String taxName, Integer gtaxId, Double  taxPc, Integer kkmNum)
  {
    this.taxId = taxId;
    this.taxName = taxName;
    this.gtaxId = gtaxId;
    this.taxPc = taxPc;
    this.kkmNum = kkmNum;
  }

  public Integer getGtaxId()
  {
    return gtaxId;
  }

  public Integer getKkmNum()
  {
    return kkmNum;
  }

  public Integer getTaxId()
  {
    return taxId;
  }

  public String getTaxName()
  {
    return taxName;
  }

  public Double getTaxPc()
  {
    return taxPc;
  }

  public int compareTo(Object o)
  {
    return getGtaxId().compareTo(((Tax)o).getGtaxId());    
  }
  
}