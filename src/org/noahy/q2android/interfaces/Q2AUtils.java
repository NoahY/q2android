package org.noahy.q2android.interfaces;

public class Q2AUtils {
	public final int QA_USER_LEVEL_BASIC = 0;
	public final int QA_USER_LEVEL_EXPERT = 20;
	public final int QA_USER_LEVEL_EDITOR = 50;
	public final int QA_USER_LEVEL_MODERATOR = 80;
	public final int QA_USER_LEVEL_ADMIN = 100;
	public final int QA_USER_LEVEL_SUPER = 120;
	
	public Boolean isLevel(int inLevel, int checkLevel) {
		return inLevel >= checkLevel;
	}
	
}
