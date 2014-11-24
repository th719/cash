package th.cash.model;

import java.util.Vector;

public class AutoSchem 
{

  private Integer s_id;
  private String name;
  private Boolean for_card;

  private Vector dsc_list;

  public AutoSchem(Integer s_id, String name, Boolean fc, Vector list)
  {
    this.s_id = s_id;
    this.name = name;
    this.for_card = fc;
    this.dsc_list = list;
  }

  public Integer getSchemId() { return s_id; }

  public String getName() { return name; }

  public Boolean getForCard() { return for_card; }

  public Vector getDiscounts() { return dsc_list; }

  public int compareTo(Object o) {
        return getSchemId().compareTo(((AmassDsc)o).getDscId());
    }
  
}