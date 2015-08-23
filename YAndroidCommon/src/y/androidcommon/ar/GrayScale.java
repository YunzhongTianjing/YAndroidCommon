package y.androidcommon.ar;

import java.nio.ShortBuffer;

import android.graphics.Bitmap;
import android.graphics.Color;

public class GrayScale {
	private GrayScale() {
	}

	public static short[] BT709(int[] src, int width, int height) {
		// java byte range from -128 to 127,not from 0 to 255,so we choose short
		final short[] result = new short[width * height];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				final int curPixel = src[i * width + j];
				final int iGray = (int) (Color.red(curPixel) * 0.2126
						+ Color.green(curPixel) * 0.7152 + Color.blue(curPixel) * 0.0722);
				result[i * width + j] = (short) iGray;
			}
		}
		return result;
	}

	public static Bitmap createBitmapARGB_8888(short[] data, int width,
			int height) {
		return Bitmap.createBitmap(shortArrayToIntArray(data), width, height,
				Bitmap.Config.ARGB_8888);
	}

	private static int[] shortArrayToIntArray(short[] data) {
		final int[] result = new int[data.length];
		for (int i = 0; i < result.length; i++)
			result[i] = Color.argb(255, data[i], data[i], data[i]);
		return result;
	}

	@Deprecated
	static Bitmap createBitmapALPHA_8(short[] data, int width, int height) {
		final Bitmap result = Bitmap.createBitmap(width, height,
				Bitmap.Config.ALPHA_8);
		result.copyPixelsFromBuffer(ShortBuffer.wrap(data));
		return result;
	}

}