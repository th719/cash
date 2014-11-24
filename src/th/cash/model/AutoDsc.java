package th.cash.model;

import java.sql.Timestamp;

// подкласс, дескриптор отдльного условия скидки в схеме
public class AutoDsc 
{
  private Integer s_id;

  private Integer a_id;
  private String name;

  private Boolean is_dsc;
  private Boolean is_sum;
  private Double  dsc_value;
  private String  check_text;

  private Timestamp bd, ed;  // вместе со временем

  private String bt, et;

  private Integer ws, we;

  private Double sa, ea;

  private Double ss, es;

  private Integer apc = null;

  private Boolean is_sc;
 
  
  public AutoDsc(Integer s_id, Integer a_id, String name, int type, int kind,
                 Double val, String ct, Timestamp bd, Timestamp ed,
                 String bt, String et, Integer ws, Integer we,
                 Double sa, Double ea, Double ss, Double es, Boolean is_sc
                 )
  {
    this.s_id = s_id; 
    this.a_id = a_id;
    this.name = name;
    is_dsc = new Boolean(type == 1);
    is_sum = new Boolean(kind == 2);
    this.dsc_value = val;
    this.check_text = ct;
    this.bd = bd;
    this.ed = ed;
    this.bt = bt;
    this.et = et;
    this.ws = ws;
    this.we = we;
    this.sa = sa;
    this.ea = ea;
    this.ss = ss;
    this.es = es;
    this.is_sc = is_sc;
  }

  public Integer getSchemId() { return s_id; }

  public Integer getAutoId() { return a_id; }

  public String getName() { return name; }

  public Boolean getIsDsc() { return is_dsc; }
  public Boolean getIsSum() { return is_sum; }

  public Double getDscValue() { return dsc_value; }

  public String getCheckText() { return check_text; }

  public Timestamp getBeginDate() { return bd; }

  public Timestamp getEndDate() { return ed; }

  public String getBeginTime() { return bt; }

  public String getEndTime() { return et; }

  public Integer getWeekStart() { return ws; }

  public Integer getWeekEnd() { return we; } // день недели

  public Double getStartAmount() { return sa; }

  public Double getEndAmount() { return ea; }

  public Double getStartSum() { return ss; }

  public Double getEndSum() { return es; }

  public Boolean getIsSumCheck() { return is_sc; }
}