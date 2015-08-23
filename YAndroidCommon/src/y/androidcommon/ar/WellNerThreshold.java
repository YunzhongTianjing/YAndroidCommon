package y.androidcommon.ar;

import android.graphics.Color;

@Deprecated
public class WellNerThreshold {
	public static int[] adaptiveThreshold(int[] inputData, int width, int height) {
		int S = width >> 3;
		int T = 15;

		int i, j;
		int sum = 0;
		int count = 0;
		int index;
		int x1, y1, x2, y2;
		int s2 = S / 2;

		// create the integral image
		int[] integralImg = new int[width * height];
		for (i = 0; i < width; i++) {
			// reset this column sum
			sum = 0;
			for (j = 0; j < height; j++) {
				index = j * width + i;
				sum += Color.red(inputData[index]);
				if (i == 0)
					integralImg[index] = sum;
				else
					integralImg[index] = integralImg[index - 1] + sum;
			}
		}
		int[] binData = new int[width * height];
		// perform thresholding
		for (i = 0; i < width; i++) {
			for (j = 0; j < height; j++) {
				index = j * width + i;
				// set the SxS region
				x1 = i - s2;
				x2 = i + s2;
				y1 = j - s2;
				y2 = j + s2;
				// check the border
				if (x1 < 0)
					x1 = 0;
				if (x2 >= width)
					x2 = width - 1;
				if (y1 < 0)
					y1 = 0;
				if (y2 >= height)
					y2 = height - 1;
				count = (x2 - x1) * (y2 - y1);
				// I(x,y)=s(x2,y2)-s(x1,y2)-s(x2,y1)+s(x1,x1)
				sum = integralImg[y2 * width + x2]
						- integralImg[y1 * width + x2]
						- integralImg[y2 * width + x1]
						+ integralImg[y1 * width + x1];
				if (Color.red(inputData[index]) * count < (long) (sum
						* (100 - T) / 100))
					binData[index] = 0xff000000;
				else
					binData[index] = 0xffffffff;
			}
		}
		return binData;
	}

}
