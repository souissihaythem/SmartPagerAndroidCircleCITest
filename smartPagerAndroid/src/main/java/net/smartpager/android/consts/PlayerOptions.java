package net.smartpager.android.consts;

public class PlayerOptions {
	
	public enum Action {
		start,
		stop,
		moveTo
	}
	
	public enum IntentFilter {
		init,
		startPlay,
		updateProgress,
		stopPlay,
		completePlaying,
		error
	}
	
	public enum Params{
		fileName,
		url,
		progress,
		message,
		duration
	}
	
}
