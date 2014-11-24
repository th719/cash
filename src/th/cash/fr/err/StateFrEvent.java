package th.cash.fr.err;

import java.util.EventObject;

import th.cash.fr.state.StateA;

public class StateFrEvent extends EventObject 
{
  public StateFrEvent(StateA source)
  {
    super(source);
  }

  public StateA getState()
  {
    return (StateA)getSource();
  }
}