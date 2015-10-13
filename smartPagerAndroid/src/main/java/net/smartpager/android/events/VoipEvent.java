package net.smartpager.android.events;

import net.smartpager.android.events.VoipEvent.VoipEventType;

public class VoipEvent {	

	public enum VoipEventType {
		CONNECTING,
		CONNECTED,
		DISCONNECTED
	}
	
	public VoipEventType eventType;
	
	public VoipEvent(VoipEventType eventType) {
		this.eventType = eventType;
	}


	
}
