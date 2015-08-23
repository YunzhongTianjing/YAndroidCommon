package y.androidcommon.ar;

public class Binarization {
	private Binarization() {
	}

	public static class Threshold {
		private Threshold() {
		}

		// http://blog.csdn.net/akunainiannian/article/details/27565899
		public static short otsu(short[] data, int width, int height) {
			final int GRAY_LEVEL = 256;

			// 统计灰度级中每个像素在整幅图像中的个数
			final int[] pixelCount = new int[GRAY_LEVEL];
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					final int curPos = j + i * width;
					pixelCount[data[curPos]]++; // 将像素值作为计数数组的下标
				}
			}

			// 计算每个像素在整幅图像中的比例
			final float[] pixelPro = new float[GRAY_LEVEL];
			final int size = width * height;
			float maxPro = 0.0f;
			for (int i = 0; i < GRAY_LEVEL; i++) {
				pixelPro[i] = (float) pixelCount[i] / size;
				maxPro = maxPro > pixelPro[i] ? maxPro : pixelPro[i];
			}

			// 遍历灰度级[0,255]
			short result = 0;
			float deltaMax = 0;
			for (short threshold = 0; threshold < GRAY_LEVEL; threshold++) {
				float w0 = 0, w1 = 0, u0tmp = 0, u1tmp = 0, u0 = 0, u1 = 0, u = 0, deltaTmp = 0;
				for (int i = 0; i < GRAY_LEVEL; i++) {
					if (i <= threshold) // 背景部分
					{
						w0 += pixelPro[i];
						u0tmp += i * pixelPro[i];
					} else // 前景部分
					{
						w1 += pixelPro[i];
						u1tmp += i * pixelPro[i];
					}
				}
				u0 = u0tmp / w0;
				u1 = u1tmp / w1;
				u = u0tmp + u1tmp;
				deltaTmp = (float) (w0 * Math.pow((u0 - u), 2) + w1
						* Math.pow((u1 - u), 2));
				if (deltaTmp > deltaMax) {
					deltaMax = deltaTmp;
					result = threshold;
				}
			}

			return result;
		}
	}

	public static void binarizeInPlace(short[] data, short threshold) {
		for (int i = 0; i < data.length; i++)
			data[i] = (short) (data[i] > threshold ? 255 : 0);
	}

}
