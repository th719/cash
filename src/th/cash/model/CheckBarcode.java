/*
 * CheckBarcode.java
 *
 * Created on 16 Ноябрь 2007 г., 13:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package th.cash.model;
import th.cash.model.SearchData;

/**
 * Класс для разбора строки поиска товара
 * Условия: Товар, Штрих-код, Весовой штрих-код
 * Инициализируются трибуты ш/к: префикс,маска. Пока только как константы.
 * По идее все нужно делать через базу или параметры.
 * @author lazarev
 */
public class CheckBarcode {
    
    private final static String DEF_WEIGHT_MASK = "22CCCCCWWWWWS";
    private final static char CODE_CHAR = 'C';
    private final static char WEIGHT_CHAR = 'W';
    private final static int WDIV = 1000;
    
    private String weightMask = null;
    private int code_start, code_end, weight_start, weight_end;
    private String weight_pref;
    
    /** Creates a new instance of CheckBarcode */
    public CheckBarcode() {
        setWeightMask(DEF_WEIGHT_MASK);
    }
    
 
    private void setWeightMask(String mask)
    {
        weightMask = mask;
        code_start = weightMask.indexOf(CODE_CHAR);
        code_end = weightMask.lastIndexOf(CODE_CHAR);
        weight_pref = weightMask.substring(0, code_start);
        weight_start = weightMask.indexOf(WEIGHT_CHAR);
        weight_end = weightMask.lastIndexOf(WEIGHT_CHAR);
    }
    
    
    /**
     * В SearchData проверяется что это такое, просто код товара, штрихкод или
     * весовой штрихкод
     * Сей метод можно совершенствовать ;-)
     */
    public void analyzeData(SearchData data)
    {
        String bc;
        bc = data.getDataForSearch();
        // проверка что в строке одни цифры (EAN7, EAN13, UPC)
        // если нет - code128 и т.п. ....
        if (bc.length() == weightMask.length())
        {
            if (bc.startsWith(weight_pref))
            {
                Integer good_id = new Integer(bc.substring(code_start, code_end + 1));
                Double quan = new Double(Double.parseDouble(bc.substring(weight_start, weight_end +1)) / WDIV);
                data.setGoodId(good_id);
                data.setQuantity(quan);
                data.setDataType(data.TYPE_WEIGHT);
            } else
            {
                data.setDataType(data.TYPE_BARCODE);
            }
        } else
        {
            data.setDataType( bc.length() < 7 ? data.TYPE_GOOD_ID : data.TYPE_BARCODE);
        }
    }
    
}
