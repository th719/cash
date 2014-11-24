package th.cash.fr.table;

/**
 * описание структуры поля
 */
public class TabFieldDescr 
{
  public final static int BIN_TYPE = 0;
  public final static int CHAR_TYPE = 1;

  private int num;
  private String name;
  private int size;
  private byte type;

  private int minVal = 0;
  private int maxVal = 0;

  private int def_val = 0;


  public TabFieldDescr(int num, String name, int size, byte type, int minv, int maxv)
  {
    this.num = num;
    this.name = name;
    this.size = size;
    this.type = type;
    this.minVal = minv;
    this.maxVal = maxv;
  }

  public int getNum()
  {
    return num;
  }

  public String getName()
  {
    return name;
  }

  public int getSize()
  {
    return size;
  }


  public byte getType()
  {
    return type;
  }

  public boolean isBinType()
  {
    return type == BIN_TYPE;
  }

  public boolean isCharType()
  {
    return type == CHAR_TYPE;
  }


  public int getMinVal()
  {
    return minVal;
  }

  public int getMaxVal()
  {
    return maxVal;
  }

  
}