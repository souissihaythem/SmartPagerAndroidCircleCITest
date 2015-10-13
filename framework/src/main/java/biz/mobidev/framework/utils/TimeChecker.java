package biz.mobidev.framework.utils;

import biz.mobidev.framework.utils.Log.LogType;


public class TimeChecker {

	public static final int NANO = 1;
	public static final int MILI = 10000000;
	public static final int MIKRO = 1000;
	private static long mStartTime;
	private static long mEndTime;

	public static void SetStartPoint() {
		mStartTime = System.nanoTime();
	}

	public static void WriteTimeSpend(TimeType type) {
	//	if (BuildConfig.DEBUG) {
			String prefix = "";
			double del = 1;
			switch (type) {
			case Nano: {
				prefix = "ns";
				del = NANO;
				break;
			}
			case Mili: {
				prefix = "mls";
				del = MILI;
				break;
			}
			case Micro: {
				prefix = "mks";
				del = MIKRO;
			}
			}

			mEndTime = System.nanoTime();
			Log.write(LogType.Debug, 3, String.valueOf(((double) (mEndTime - mStartTime)) / del) + " " + prefix);
		//}
	}
	
	public static void WriteTimeSpendInMicroSeconds() {
		WriteTimeSpend(TimeType.Micro);
	}

	public static void WriteTimeSpendInMiliSeconds() {
		WriteTimeSpend(TimeType.Mili);
	}
	
	public static void WriteTimeSpendInNanoSeconds() {
		WriteTimeSpend(TimeType.Nano);
	}
	
	enum TimeType {
		Nano, Mili, Micro;
	}

}
