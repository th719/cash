package th.cash.fr.table;

public class CellIndex 
{
  private int rowIndex;
  private int colIndex;
  
  public CellIndex(int rowIndex, int colIndex)
  {
    this.rowIndex = rowIndex;
    this.colIndex = colIndex;
  }

  public int getRowIndex()
  {
    return rowIndex;
  }

  public int getColIndex()
  {
    return colIndex;
  }
}