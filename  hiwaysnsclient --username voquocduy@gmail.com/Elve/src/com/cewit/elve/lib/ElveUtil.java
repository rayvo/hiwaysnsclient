package com.cewit.elve.lib;

import java.util.Random;

public class ElveUtil {

	public static final int byteArrayToInt(byte[] b) {
		int length = b.length;
		switch (length){
		case 1:
			return (b[0] & 0xFF);
		case 2:
			return ((b[0] & 0xFF) << 8)	+ (b[1] & 0xFF);
		case 3:
			return ((b[0] & 0xFF) << 16) + ((b[1] & 0xFF) << 8)	+ (b[2] & 0xFF);
		case 4:
			return (b[0] << 24) + ((b[1] & 0xFF) << 16) + ((b[2] & 0xFF) << 8)	+ (b[3] & 0xFF);			
		default:
			return 0;
		}		
		
	}

	public static byte[] getBytes(byte[] source, int start, int length) {

		byte[] result = new byte[length];

		for (int i = 0; i < length; i++) {
			result[i] = source[start];
			start = start + 1;
		}

		return result;
	}
	
	public static int getLevel(int max, int noLevel, int value){		
		int level = (value * noLevel) / max;
		level = level + 1;
		return level;
	}

	public static int proLevelRandom(int i) {
		Random generator = new Random();
		int result = generator.nextInt(i);
		return result;
	}
}
