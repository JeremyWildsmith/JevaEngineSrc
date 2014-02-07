package io.github.jevaengine.graphics.pipeline;

import io.github.jevaengine.math.Matrix4X4;

import java.awt.geom.AffineTransform;

import javax.media.opengl.GL4;

import org.jogamp.glg2d.GLGraphics2D;
import org.jogamp.glg2d.impl.AbstractMatrixHelper;

public class TransformHelper extends AbstractMatrixHelper {
	protected GL4 gl;

	private float[] matrixBuf = new float[16];
	private PrimitiveShader m_shader;
	private DrawBatcher m_drawBatcher;
	
	public TransformHelper(PrimitiveShader shader, DrawBatcher drawBatcher)
	{
		m_drawBatcher = drawBatcher;
		m_shader = shader;
	}
	
	@Override
	public void setG2D(GLGraphics2D g2d)
	{
		super.setG2D(g2d);
		gl = g2d.getGLContext().getGL().getGL4();

		setupGLView();
		flushTransformToOpenGL();
	}

	protected void setupGLView()
	{
		int[] viewportDimensions = new int[4];
		gl.glGetIntegerv(GL4.GL_VIEWPORT, viewportDimensions, 0);
		int width = viewportDimensions[2];
		int height = viewportDimensions[3];

		Matrix4X4 ortho = new Matrix4X4(
				2.0F/(float)width, 0, 0, -1,
				0, 2.0F/(float)height, 0, -1,
				0, 0, -1, 0	,
				0, 0, 0, 1
				);
		
		m_shader.setProjection(ortho);
	}

	/**
	 * Sends the {@code AffineTransform} that's on top of the stack to the video
	 * card.
	 */
	protected void flushTransformToOpenGL()
	{
		float[] matrix = getGLMatrix(stack.peek());

		m_drawBatcher.flush();
		m_shader.setModelView(Matrix4X4.fromColumnMajor(matrix));
	}

	/**
	 * Gets the GL matrix for the {@code AffineTransform} with the change of
	 * coordinates inlined. Since Java2D uses the upper-left as 0,0 and OpenGL
	 * uses the lower-left as 0,0, we have to pre-multiply the matrix before
	 * loading it onto the video card.
	 */
	protected float[] getGLMatrix(AffineTransform transform)
	{
		matrixBuf[0] = (float) transform.getScaleX();
		matrixBuf[1] = -(float) transform.getShearY();
		matrixBuf[4] = (float) transform.getShearX();
		matrixBuf[5] = -(float) transform.getScaleY();
		matrixBuf[10] = 1;
		matrixBuf[12] = (float) transform.getTranslateX();
		matrixBuf[13] = g2d.getCanvasHeight()
				- (float) transform.getTranslateY();
		matrixBuf[15] = 1;

		return matrixBuf;
	}
}
