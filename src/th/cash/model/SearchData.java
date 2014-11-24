package th.cash.model;
/*
 * SearchData.java
 *
 * Created on 16 Ноябрь 2007 г., 13:05
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

import java.util.Vector;

/**
 * Объект для поиска товара в модели
 * @author lazarev
 */
public class SearchData {
    
    public final static int TYPE_GOOD_ID = 0;
    public final static int TYPE_BARCODE = 1;
    public final static int TYPE_WEIGHT  = 2;
    
    
    private String in_str;
    
    private Integer good_id = null;
    private Vector goods = null;
    private Vector barcodes = null;
    private Double quan = null; // для весовых товаров
    
    private int data_type = TYPE_GOOD_ID;
    /** Creates a new instance of SearchData */
    public SearchData(String v) {
        setDataForSearch(v);
    }

    
    protected void setGoods(Vector v)
    {
        goods = v;
    }
    
    protected void setBarcodes(Vector v)
    {
        barcodes = v;
    }
        
    
    protected void setGoodId(Integer goods_id)
    {
        this.good_id = goods_id;
    }
    
    protected void setQuantity(Double q)
    {
        this.quan = q;
    }
        
    protected void setDataType(int type)
    {
        data_type = type;
    }
        
        
            
    public void setDataForSearch(String v)
    {
        in_str = v;
        good_id = null;
        quan = null;
    }
    
    public String getDataForSearch()
    {
        return in_str;
    }
        
        
  
    // результат поиска
    public Vector getGoods()
    {
        return goods;
    }
    
    public Vector getBarcodes()
    {
        return barcodes;
    }
        
    
    // разобранная строка, код товара
    public Integer getGoodId()
    {
        return good_id;
    }
    
    public Double getQuantity()
    {
        return quan;
    }
    
    public boolean isGoodId()
    {
        return data_type == TYPE_GOOD_ID;
    }
    
    public boolean isBarcode()
    {
        return data_type == TYPE_BARCODE;
    }
    
    public boolean isWeight()
    {
        return data_type == TYPE_WEIGHT;
    }
    
}
