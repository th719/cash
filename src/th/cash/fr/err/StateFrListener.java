package th.cash.fr.err;
import java.util.EventListener;

public interface StateFrListener extends EventListener 
{
  public void stateReceived(StateFrEvent e);
}