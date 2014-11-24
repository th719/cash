package th.cash.model;

/**
 * Created by IntelliJ IDEA.
 * User: lazarev
 * Date: 15.11.2007
 * Time: 17:04:57
 * To change this template use File | Settings | File Templates.
 */
public class Barcode implements Comparable{

    private String barcode;
    private Integer goodId;
    private Double price;
    private Double koef;

    public Barcode(String barcode)
    {
        this(barcode, null, null, null);
    }
    public Barcode(String barcode, Integer goodId, Double price, Double koef)
    {
        this.barcode = barcode;
        this.goodId = goodId;
        this.price = price;
        this.koef = koef;
    }

    public String getBarcode() {
        return barcode;
    }

    public Integer getGoodId() {
        return goodId;
    }

    public Double getPrice() {
        return price;
    }

    public Double getKoef() {
        return koef;
    }

    public int compareTo(Object o) {
//        return barcode.compareTo(((Barcode)o).getBarcode());

        // new search condition 15/01/09
        // сравниваем по ш/к, если равны - то по коду товара
        Barcode b = (Barcode)o;
        int res = barcode.compareTo(b.getBarcode());
        if (res == 0 && goodId != null && b.getGoodId() != null) 
          res = goodId.compareTo(b.getGoodId());

        return res;    
    }
}
