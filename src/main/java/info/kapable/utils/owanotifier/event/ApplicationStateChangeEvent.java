package info.kapable.utils.owanotifier.event;

public class ApplicationStateChangeEvent implements Event
{
	public enum StateChange
	{
		STARTED
	};

	private StateChange stateChange;

	public StateChange getStateChange()
	{
		return stateChange;
	}

	public void setStateChange(StateChange stateChange)
	{
		this.stateChange = stateChange;
	}
}
