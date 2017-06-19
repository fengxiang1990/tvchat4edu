package com.bizcom.vo.enums;


public enum MixVideoLayoutType {

	// 1
	FILL_ONE,
	// 1|2
	LEFT_RIGHT_2,

	// 1|2
	// 3|4
	SPLIT_4,

	// 1 2
	// 3
	// 4 5 6
	MAIN_6,

	/*
	 * 1 2 3 5 6 7 8
	 */
	MAIN_8,

	/*
	 * 1 2 3 4 5 6 7 8 9
	 */
	SPLIT_9,

	/**
	 * 
	 * 1 2 3 4 5 6 7 8 9 10
	 */
	MAIN_2_LINEAR_10,

	/**
	 * 1 2 3 4 5 6 7 8 9 10
	 */
	MAIN_MIDDLE_2_LINEAR_10,

	/**
	 * 2 3 4 1 5 6 7 8 9 10 11
	 */
	MAIN_1_AROUND_11,

	/**
	 * 
	 * 1 2 3 4 5 8 6 7 9 10 11 12 13
	 */
	MAIN_1_AROUND_13,

	/**
	 * 
	 * 1 2 3 4 5 6 7 8 9 10 11 12 13
	 */
	MAIN_1_LINEAR_13,

	/**
	 * 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16
	 */
	SPLIT_16, UNKOWN;

	public int toIntValue() {
		switch (this) {
		case FILL_ONE:
			return 1;
		case LEFT_RIGHT_2:
			break;
		case MAIN_1_AROUND_11:
			return 11;
		case MAIN_1_AROUND_13:
			return 131;
		case MAIN_1_LINEAR_13:
			break;
		case MAIN_2_LINEAR_10:
			return 101;
		case MAIN_6:
			return 6;
		case MAIN_8:
			return 8;
		case MAIN_MIDDLE_2_LINEAR_10:
			break;
		case SPLIT_16:
			return 16;
		case SPLIT_4:
			return 4;
		case SPLIT_9:
			return 9;
		case UNKOWN:
		default:
			return -1;

		}
		return -1;
	}

	public static MixVideoLayoutType fromInt(int val) {
		switch (val) {
		case 1:
			return FILL_ONE;
		case 4:
			return SPLIT_4;
		case 6:
			return MAIN_6;
		case 8:
			return MAIN_8;
		case 9:
			return SPLIT_9;
		case 101:
			return MAIN_2_LINEAR_10;
		case 11:
			return MAIN_1_AROUND_11;
		case 131:
			return MAIN_1_AROUND_13;
		case 16:
			return SPLIT_16;

		}
		return LEFT_RIGHT_2;
	}
}