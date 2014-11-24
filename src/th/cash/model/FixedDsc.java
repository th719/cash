package th.cash.model;

// фиксированная скидка
public class FixedDsc implements Comparable
{
  private Integer dscId;
  private String name;
  private Boolean isDsc;
  private Boolean isSum;
  private Double  dscValue;
  private String  checkText;
  private String  cardPref;
  private Boolean forCheck, forPos;

  public FixedDsc(Integer id, String name, int type, int kind, Double value, String ct, String pref, Boolean forCheck, Boolean forPos)
  {
    this.dscId = id;
    this.name = name;
    isDsc = new Boolean(type == 1);
    isSum = new Boolean(kind == 2);
    this.dscValue = value;
    this.checkText = ct;
    this.cardPref = pref;
    this.forCheck = forCheck;
    this.forPos = forPos;
  }

  public Integer getDscId() { return dscId; }

  public String getName() { return name; }

  public Boolean getIsDsc() { return isDsc; }
  public Boolean getIsSum() { return isSum; }


  public Double getDscValue() { return dscValue; }

  public String getCheckText() { return checkText; }

  public String getCardPref() { return cardPref; }

  public Boolean getForCheck() { return forCheck; }

  public boolean isForCheck() { return forCheck != null && forCheck.booleanValue(); }

  public Boolean getForPos() { return forPos; }

  public int compareTo(Object o) {
    return dscId.compareTo(((FixedDsc)o).getDscId());
  }
}