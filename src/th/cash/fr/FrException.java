package th.cash.fr;

public class FrException extends Exception
{
  private int code;
  private int crval = 0;
  private Exception nested = null;

  public FrException(String msg, int code)
  {
    this(msg, code, 0);
  }
  
  public FrException(String msg, int code, int lev)
  {
    super(msg);
    this.code = code;
    this.crval = lev;
  }
  
  public int getErrorCode()
  {
    return code;
  }

  public boolean isCritical()
  {
    return crval > 0;
  }

  public Exception getNestedException()
  {
    return nested;
  }

  public void setNestedException(Exception ex)
  {
    nested = ex;
  }

  
}

