package th.cash.card;

public interface PaymentSelector 
{

  public boolean selPaymentTypes(double cs, double nal, double bn);

  public double getNalSum();

  public double getBnSum();
}