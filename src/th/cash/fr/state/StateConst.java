package th.cash.fr.state;

public interface StateConst 
{
  // коды состояний именованы константами, чтобы не было неясностей и путаницы
  public final static int M_DATA_TRANSMIT        = 1;
  public final static int M_OPEN_SHIFT_BEF_24    = 2;
  public final static int M_OPEN_SHIFT_AF_24     = 3;
  public final static int M_CLOSED_SHIFT         = 4;
  public final static int M_LOCKED_PASS          = 5;
  public final static int M_WAIT_DATE_CONF       = 6;
  public final static int M_ENABLE_DEC_POINT     = 7;
  public final static int M_OPENED_DOC           = 8;

    // статусы режимов
    public final static int MS_SALE              = 0;
    public final static int MS_PURCHASE          = 1;
    public final static int MS_RET_SALE          = 2;
    public final static int MS_RET_PURCHASE      = 3;

  public final static int M_ENABLE_TECH_ZEROING  = 9;
  
  public final static int M_TEST_PRINTING        = 10;
  public final static int M_FULL_REPORT          = 11;
  public final static int M_EKLZ_REPORT          = 12;

  public final static int SM_PAPER_OK            = 0;
  public final static int SM_PASS_NO_PAPER       = 1;
  public final static int SM_ACTIV_NO_PAPER      = 2;
  public final static int SM_WAIT_REPEAT         = 3;
  public final static int SM_FISCAL_PRINT        = 4;
  public final static int SM_PRINT               = 5;
  
}