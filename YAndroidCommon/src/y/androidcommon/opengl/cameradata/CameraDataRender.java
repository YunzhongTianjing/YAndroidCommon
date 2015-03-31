package y.androidcommon.opengl.cameradata;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import y.androidcommon.R;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;

@SuppressWarnings({ "deprecation" })
public class CameraDataRender implements Renderer {
	private final Context mContext;

	private int mPreviewWidth;
	private int mPreviewHeight;

	private int mProgramHandle;
	private int mYTextureHandle;
	private int mUVTextureHandle;
	private int uYTextureSampler;
	private int uUVTextureSampler;
	private int aPosition;
	private int aTexCoord;

	private ByteBuffer mYBuffer;
	private ByteBuffer mUVBuffer;

	private final float[] POSITION = { -1.0f, 1.0f, 0, 1,// Position 0
			-1.0f, -1.0f, 0, 1,// Position 1
			1.0f, -1.0f, 0, 1,// Position 2
			1.0f, -1.0f, 0, 1,// Position 2
			1.0f, 1.0f, 0, 1,// Position 3
			-1.0f, 1.0f, 0, 1,// Position 0
	};

	private final float[] TEXTURE = { 0.0f, 0.0f, // TexCoord 0
			0.0f, 1.0f, // TexCoord 1
			1.0f, 1.0f, // TexCoord 2
			1.0f, 1.0f, // TexCoord 2
			1.0f, 0.0f,// TexCoord 3
			0.0f, 0.0f, // TexCoord 0
	};

	private Buffer mPosBuffer;
	private Buffer mTexBuffer;
	private Camera mCamera;

	public CameraDataRender(Context context) {
		this.mContext = context;
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		final int vsh = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
		GLES20.glShaderSource(vsh,
				Utitls.readStringFromResRaw(mContext, R.raw.camera_vertex));
		GLES20.glCompileShader(vsh);
		checkResult(GLES20.glGetShaderInfoLog(vsh));

		final int fsh = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
		GLES20.glShaderSource(fsh,
				Utitls.readStringFromResRaw(mContext, R.raw.camera_fragment));
		GLES20.glCompileShader(fsh);
		checkResult(GLES20.glGetShaderInfoLog(fsh));

		mProgramHandle = GLES20.glCreateProgram();
		GLES20.glAttachShader(mProgramHandle, vsh);
		GLES20.glAttachShader(mProgramHandle, fsh);
		GLES20.glLinkProgram(mProgramHandle);
		checkResult(GLES20.glGetProgramInfoLog(mProgramHandle));

		uYTextureSampler = GLES20.glGetUniformLocation(mProgramHandle,
				"uYTextureSampler");
		uUVTextureSampler = GLES20.glGetUniformLocation(mProgramHandle,
				"uUVTextureSampler");
		checkLocation(uYTextureSampler, uUVTextureSampler);

		aPosition = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
		GLES20.glEnableVertexAttribArray(aPosition);
		aTexCoord = GLES20.glGetAttribLocation(mProgramHandle, "aTexCoord");
		GLES20.glEnableVertexAttribArray(aTexCoord);
		checkLocation(aPosition, aTexCoord);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		Size size = mCamera.getParameters().getPreviewSize();
		height = size.height;
		width = size.width;
		GLES20.glViewport(0, 0, width, height);

		this.mPreviewWidth = width;
		this.mPreviewHeight = height;
		mYBuffer = (ByteBuffer) ByteBuffer.allocateDirect(width * height)
				.order(ByteOrder.nativeOrder()).position(0);
		mUVBuffer = (ByteBuffer) ByteBuffer.allocateDirect(width * height / 2)
				.order(ByteOrder.nativeOrder()).position(0);

		mPosBuffer = ByteBuffer.allocateDirect(POSITION.length * 4)
				.order(ByteOrder.nativeOrder()).asFloatBuffer().put(POSITION)
				.position(0);
		mTexBuffer = ByteBuffer.allocateDirect(TEXTURE.length * 4)
				.order(ByteOrder.nativeOrder()).asFloatBuffer().put(TEXTURE)
				.position(0);

		final int[] textures = new int[2];
		GLES20.glGenTextures(2, textures, 0);
		mYTextureHandle = textures[0];
		mUVTextureHandle = textures[1];
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mYTextureHandle);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
				GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
				GLES20.GL_CLAMP_TO_EDGE);

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mUVTextureHandle);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
				GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
				GLES20.GL_CLAMP_TO_EDGE);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		GLES20.glUseProgram(mProgramHandle);

		GLES20.glVertexAttribPointer(aPosition, 4, GLES20.GL_FLOAT, false, 0,
				mPosBuffer);
		GLES20.glVertexAttribPointer(aTexCoord, 2, GLES20.GL_FLOAT, false, 0,
				mTexBuffer);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glUniform1i(uYTextureSampler, 0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mYTextureHandle);
		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE,
				mPreviewWidth, mPreviewHeight, 0, GLES20.GL_LUMINANCE,
				GLES20.GL_UNSIGNED_BYTE, mYBuffer);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		GLES20.glUniform1i(uUVTextureSampler, 1);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mUVTextureHandle);
		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE_ALPHA,
				mPreviewWidth / 2, mPreviewHeight / 2, 0,
				GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, mUVBuffer);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, POSITION.length / 4);
		GLES20.glFinish();
	}

	public void onPreviewFrame(byte[] data) {
		if (null == mYBuffer || null == mUVBuffer)
			return;
		mYBuffer.put(data, 0, mPreviewWidth * mPreviewHeight).position(0);
		mUVBuffer.put(data, mPreviewWidth * mPreviewHeight,
				mPreviewWidth * mPreviewHeight / 2).position(0);
	}

	public void setCamera(Camera camera) {
		this.mCamera = camera;
	}

	private void checkLocation(int... location) {
		for (int l : location) {
			if (-1 == l)
				throw new RuntimeException("can't find location");
		}
	}

	private void checkResult(String log) {
		if (!"".equals(log))
			throw new RuntimeException(log);
	}
}
