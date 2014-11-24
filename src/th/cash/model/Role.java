package th.cash.model;

import java.util.StringTokenizer;

/**
 * Набор прав (роль)
 */
public class Role implements Comparable
{
  private Integer id;
  private String name;
  private String descr;

  private CheckPerm sale_perm;
  private CheckPerm ret_perm;

  private boolean f_fixed_dsc;
  private boolean f_in_pay, f_out_pay, f_zreport, f_xreport, f_close_day, f_moneybox, f_bn, f_setup;

  private final static int NUM_FLAGS = 25;

  // пустой шаблон, если роль для пользователя отсутствует
  public Role()
  {
    this(null, null, null);
  }

  // код , имя пользователя и строковое представление набора флагов 
  public Role(Integer id, String name, String descr)
  {
    this.id = id;
    this.name = name;
    this.descr = descr;

    boolean[] flags = emptyFlags(); 

    if (descr != null)
    {
      StringTokenizer st = new StringTokenizer(descr, ";,");

      int i = 0; 
      while (i < NUM_FLAGS && st.hasMoreTokens())
        flags[i++] = "1".equals(st.nextToken());
    }

    init(flags);
  }

  private boolean[] emptyFlags()
  {
    boolean[] flags = new boolean[NUM_FLAGS];
    for (int i = 0; i < NUM_FLAGS; i++) flags[i] = false;
    return flags;
  }

// флаги для чека:
// <open>,<type_code>,<scan_bc>,<type_bc>,<storno>,<repeat>,<cancel>,<close>

// Шаблон
// ------------------+---------------+---------------+-------+-------+
// 1      Полный      1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1 
// 3      Кассир      1,1,1,1,0,0,0,1,0,0,0,0,0,0,0,0,1,1,1,0,1,1,1,0,1 
// 4      Контроллер  0,0,0,0,1,0,0,1,1,1,1,1,0,0,0,1,0,0,0,0,0,1,0,0,0 


                      
  private void init(boolean[] flags)
  {
    int fcnt = 0;
    // 7 флажков на вид чека 
    // 0 - 6
    sale_perm = new CheckPerm(
      getFlag(flags, fcnt++), getFlag(flags, fcnt++), getFlag(flags, fcnt++), getFlag(flags, fcnt++), 
      getFlag(flags, fcnt++), getFlag(flags, fcnt++), getFlag(flags, fcnt++), getFlag(flags, fcnt++));
    // 7 - 13
    ret_perm  = new CheckPerm(
      getFlag(flags, fcnt++), getFlag(flags, fcnt++), getFlag(flags, fcnt++), getFlag(flags, fcnt++), 
      getFlag(flags, fcnt++), getFlag(flags, fcnt++), getFlag(flags, fcnt++), getFlag(flags, fcnt++));

    // 14
    f_in_pay    = getFlag(flags, fcnt++);
    // 15
    f_out_pay   = getFlag(flags, fcnt++);

    // 16
    f_zreport   = getFlag(flags, fcnt++);
    // 17
    f_xreport   = getFlag(flags, fcnt++);

    // 18
    f_close_day = getFlag(flags, fcnt++);
    // 19
    f_moneybox  = getFlag(flags, fcnt++);
    // 20
    f_bn        = getFlag(flags, fcnt++);

    // 21
    f_setup     = getFlag(flags, fcnt++);

    f_fixed_dsc = getFlag(flags, fcnt++);
  }

  private boolean getFlag(boolean[] flags, int ind)
  {
    return ind < NUM_FLAGS ? flags[ind] : false;
  }


  // **********************************************
  public Integer getId() { return id; }

  public String getName() { return name; }

  // внесение / выплата
  public boolean isInPay() { return f_in_pay; }

  public boolean isOutPay() { return f_out_pay; }

  // отчеты
  public boolean isZReport() { return f_zreport; }

  public boolean isXReport() { return f_xreport; }

  // закрытие дня по картам
  public boolean isCloseDay() { return f_close_day; }

  // открытие денежного ящика
  public boolean isMoneybox() { return f_moneybox; }

  // безналичная оплата 
  public boolean isBn() { return f_bn; }

  // настройки
  public boolean isSetup() { return f_setup; }

  // фиксированные скидки
  public boolean isFixedDsc() { return f_fixed_dsc; }

  // права на чек продажи
  public CheckPerm getSaleCheckPerm() { return sale_perm; }

  // права на чек возврата
  public CheckPerm getReturnCheckPerm() { return ret_perm; }



  public int compareTo(Object o) { return getId().compareTo(((Role)o).getId()); }
  
}
