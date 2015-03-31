package y.androidcommon.opengl.cameradata;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

public final class Utitls {
	private Utitls() {
	}

	public static String readStringFromResRaw(Context context, int resId) {
		try {
			return internalReadString(context, resId);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String internalReadString(Context context, int resId)
			throws IOException {
		InputStream is = null;
		try {
			is = context.getResources().openRawResource(resId);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			final byte[] buffer = new byte[1024];
			int len = -1;
			while (-1 != (len = is.read(buffer)))
				os.write(buffer, 0, len);
			return new String(os.toByteArray());
		} finally {
			if (null != is)
				is.close();
		}
	}
}
