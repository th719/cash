package th.cash.model;

// Скидка/схема накопительная
public class AmassDsc implements Comparable
{
  private Integer d_id;
  private String d_name;
  private String ct;
  private Double dsc_value;

  private Boolean is_ss;
  private Double ss;

  private Boolean is_es;
  private Double es;

  private Boolean is_sr;
  private Integer sr;

  private Boolean is_er;
  private Integer er;

  private Integer s_id;
  private String s_name;
  
  public AmassDsc(Integer dsc_id, String d_name, String ct, Double val, 
                  Boolean is_ss, Double ss, Boolean is_es, Double es,
                  Boolean is_sr, Integer sr, Boolean is_er, Integer er,
                  Integer sch_id, String s_name)
  {
    this.d_id = dsc_id;
    this.d_name = d_name;
    this.ct = ct;
    this.dsc_value = val;
    this.is_ss = is_ss;
    this.ss = ss;
    this.is_es = is_es;
    this.es = es;
    this.is_sr = is_sr;
    this.sr = sr;
    this.is_er = is_er;
    this.er = er;
    this.s_id = sch_id;
    this.s_name = s_name;
  }

  public Integer getDscId() { return d_id; }

  public String getDscName() { return d_name; }

  public String getCheckText() { return ct; }

  public Double getDscValue() { return dsc_value; }

  public Double getStartSum() { return ss; }

  public Double getEndSum() { return es; }

  public Integer getStartCn() { return sr; }

  public Integer getEndCn() { return er; }

  public Integer getSchemId() { return s_id; }

  public String getSchemName() { return s_name; }

  public int compareTo(Object o) {
        return getDscId().compareTo(((AmassDsc)o).getDscId());
    }
  
}