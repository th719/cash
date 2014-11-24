package th.cash.model;

/**
 * Created by IntelliJ IDEA.
 * User: lazarev
 * Date: 15.11.2007
 * Time: 16:49:13
 * To change this template use File | Settings | File Templates.
 */
public class GoodMain implements Comparable{


    private Integer goodId;
    private String  name;
    private Double  price;
    private Integer adscSchem;
    private Boolean weightControl;
    private Integer section;
    private Double  maxDsc;
    private Integer nalGroup;

    public GoodMain(Integer good_id)
    {
        this(good_id, null, null, null, null, null, null, null);
    }

    public GoodMain(Integer good_id, String name, Double price, Integer adsc_schem,
                    Boolean wc, Integer section, Double max_dsc, Integer nal_gr)
    {
        this.goodId = good_id;
        this.name = name;
        this.price = price;
        this.adscSchem = adsc_schem;
        this.weightControl = wc;
        this.section = section;
        this.maxDsc = max_dsc;
        this.nalGroup = nal_gr;
    }

    public Integer getGoodId() {
        return goodId;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }

    public Integer getAdscSchem() {
        return adscSchem;
    }

    public Boolean getWeightControl() {
        return weightControl;
    }

    public Integer getSection() {
        return section;
    }

    public Double getMaxDsc() {
        return maxDsc;
    }

    public Integer getNalGroup() {
        return nalGroup;
    }

    public int compareTo(Object o) {
        return goodId.compareTo(((GoodMain)o).getGoodId());
    }
}
