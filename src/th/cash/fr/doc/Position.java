package th.cash.fr.doc;


import th.common.util.RMath;

import th.cash.model.GoodMain;
import th.cash.model.Barcode;
import th.cash.model.Tax;

/**
 * Класс, определяющий позицию в чеке продажи / возврата
 * данные отображаются в модель таблицы
 * User: lazarev
 * Date: 14.11.2007
 * Time: 13:31:22
 *
 */
public class Position {

  
  public final static Integer SALE_POS = new Integer(1);
  public final static Integer CANCEL_POS = new Integer(2);
  public final static Integer SALE_RET_POS = new Integer(3);

//  пока без продажи
//  private final static int BUY_POS = 4;
//  private final static int BUY_RET_POS = 5;
  
  private Integer num;  // номер в чеке или null
  private Integer type; // тип - определяется константами см. выше
  private String gname; // 40 сиволов
  private Double quantity;
  private Double price;
  private Double sum;


  private Integer goodId;
  private String  barcode;
  private Double  koef;

  private Integer gtaxId;   // группа налога
  private Double  nalPc;    // процент 
  private String  tname;    // наименование налога 
  private Double  taxSum;   // сумма налога

  // TODO еще скидки ... добавить


  public Position(Position src)
  {
    this(null, src.type, src.gname, src.quantity, src.price, src.goodId, src.barcode, src.koef, src.gtaxId, src.nalPc, src.tname);
  }

  public Position(Integer type, Double q, GoodMain g, Barcode b, Tax t)
  {
    this(null, type, g.getName(), q, g.getPrice(), 
      g.getGoodId(), b == null ? null : b.getBarcode(), b == null ? null : b.getKoef(),
      t == null ? null : t.getGtaxId(), t == null ? null : t.getTaxPc(), t == null ? null : t.getTaxName());
  }
  
  public Position(Integer num, Integer type, 
        String gname, Double quan, Double price,
        Integer goodId, String barcode, Double koef,
        Integer gtaxId, Double nalPc, String tname)  
  {
    this.num = num;
    this.type = type;
    this.gname = gname;
    this.quantity = quan;
    this.price = price;
    
    this.goodId = goodId;
    this.barcode = barcode;
    this.koef = koef;

    this.gtaxId = gtaxId;
    this.nalPc = nalPc;
    this.tname = tname;

    if (/*sum == null && */quan != null && price != null)
      this.sum = new Double(RMath.round(quan.doubleValue() * price.doubleValue(), 2));

    taxSum = nalInSum(sum, nalPc);

  }

  private Double nalInSum(Double sum, Double pc)
  {
    if (sum == null || pc == null) return null;
    return new Double(RMath.round( sum.doubleValue() * pc.doubleValue() / (100 + pc.doubleValue()), 2));
  }

  // get/ set
  public Integer getNum()
  {
    return num;
  }

  public void setNum(Integer newNum)
  {
    num = newNum;
  }

  public Integer getType()
  {
    return type;
  }

  public void setType(Integer newType)
  {
    type = newType;
  }

  public String getBarcode()
  {
    return barcode;
  }

  public void setBarcode(String b)
  {
    barcode = b;
  }

  public String getGname()
  {
    return gname;
  }

  public Integer getGoodId()
  {
    return goodId;
  }

  public Double getKoef()
  {
    return koef;
  }

  public void setKoef(Double k)
  {
    koef = k;
  }

  public Double getNalPc()
  {
    return nalPc;
  }

  public Double getPrice()
  {
    return price;
  }

  public Double getQuantity()
  {
    return quantity;
  }

  public Double getSum()
  {
    return sum;
  }

  public Integer getGtaxId()
  {
    return gtaxId;
  }

  public String getTname()
  {
    return tname;
  }

 
}
