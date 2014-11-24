package th.cash.model;

// накопительная карта, некоторые атрибуты опущены
public class DscCard implements Comparable
{
  private String id_num; 
  private String name = null; // зачем ?
  private String ct;
  private Double amass_sum;
  private Integer auto_id;
  private Integer amass_id;
  private Boolean forb; 
  private Boolean no_amass;
  private Boolean for_pos;
  private Boolean for_all_pos;
  private Integer ch_num;
  

  public DscCard(String num, String name, String ct, Double asum, Integer auto_id, Integer amass_id,
     Boolean is_forb, Boolean no_amass, Boolean for_pos, Boolean for_all_pos, Integer ch_num)
  {
    this.id_num = num;
    this.name = name;
    this.ct = ct;
    this.amass_sum = asum;
    this.auto_id = auto_id;
    this.amass_id = amass_id;
    this.forb = is_forb;
    this.no_amass = no_amass;
    this.for_pos = for_pos;
    this.for_all_pos = for_all_pos;
    this.ch_num = ch_num;
  }

  public String getNum() { return id_num; }

  public String getName() { return name; }

  public String getCheckText() { return ct; }

  public Double getAmassSum() { return amass_sum; }

  public Integer getAutoId() { return auto_id; }

  public Integer getAmassId() { return amass_id; }

  public boolean isForbidden() { return forb != null && forb.booleanValue(); }

  public boolean idNoAmass() { return no_amass != null && no_amass.booleanValue(); }

  public boolean isForPos() { return for_pos != null && for_pos.booleanValue(); }

  public boolean isForAllPos() { return for_all_pos != null && for_all_pos.booleanValue(); }

  public Integer getCheckNumber() { return ch_num; }

  public int compareTo(Object o) {
        return getNum().compareTo(((DscCard)o).getNum());
    }
  
}