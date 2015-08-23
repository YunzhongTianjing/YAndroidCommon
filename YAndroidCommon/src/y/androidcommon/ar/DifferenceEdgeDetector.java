package y.androidcommon.ar;

public class DifferenceEdgeDetector {
	private DifferenceEdgeDetector() {
	}

	public static short[] detect(short[] data, int width, int height) {
		// processing start and stop X,Y positions
		final int startX = 1;
		final int startY = 1;
		final int stopX =  width - 1;
		final int stopY = height - 1;

		// data pointers
		final short[] result = new short[data.length];

		// for each line
		for (int y = startY; y < stopY; y++) {
			// for each pixel
			for (int x = startX; x < stopX; x++) {
				int max = Integer.MIN_VALUE, tmp = 0;
				final int curPos = x + y * width;

				// left diagonal
				tmp = data[curPos - width - 1] - data[curPos + width + 1];
				tmp = tmp > 0 ? tmp : -tmp;
				max = tmp > max ? tmp : max;

				// right diagonal
				tmp = (int) data[curPos - width + 1] - data[curPos + width - 1];
				tmp = tmp > 0 ? tmp : -tmp;
				max = tmp > max ? tmp : max;

				// vertical
				tmp = (int) data[curPos - width] - data[curPos + width];
				tmp = tmp > 0 ? tmp : -tmp;
				max = tmp > max ? tmp : max;

				// horizontal
				tmp = (int) data[curPos - 1] - data[curPos + 1];
				tmp = tmp > 0 ? tmp : -tmp;
				max = tmp > max ? tmp : max;

				result[curPos] = (short) max;
			}
		}
		return result;
	}

}
