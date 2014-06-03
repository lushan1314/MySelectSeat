package com.example.seat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.view.View;

/**
 * 
 * @author simon
 * @date 2013-7-9
 * @Description:影院座位表左侧的数字（座位行数）
 * 
 */
public class SeatNumberView extends View implements SeatZoomListener {

	// 数字paint
	Paint fontPaint = new Paint();
	int row = 0;
	int newBoxSize = 0;
	int newSeatSize = 0;
	float paddingTop = 0;

	public SeatNumberView(Context context) {
		super(context);
		init();
	}

	public SeatNumberView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		fontPaint.setColor(Color.BLACK);
		fontPaint.setAntiAlias(true);
		fontPaint.setTextSize(18);
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(measure(widthMeasureSpec), measure(heightMeasureSpec));
	}

	private int measure(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		if (specMode == MeasureSpec.EXACTLY) {
			// We were told how big to be
			result = specSize;
		} else {
			// Measure the text
			if (specMode == MeasureSpec.AT_MOST) {
				// Respect AT_MOST value if that was what is called for by
				// measureSpec
				// result = Math.min(result, specSize);
				result = specSize;
			}
		}
		return result;
	}

	@Override
	public void zoomChange(int row, int column, int newBoxSize, int newSeatSize, float paddingLeft, float paddingTop) {
		this.row = row;
		this.newBoxSize = newBoxSize;
		this.paddingTop = paddingTop;
		this.newSeatSize = newSeatSize;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		for (int i = 0; i < row; i++) {
			// 字体宽度
			float fontWidth = fontPaint.measureText("" + i);
			// 字体高度
			FontMetrics fm = fontPaint.getFontMetrics();
			float fontHeight = fm.descent - fm.ascent;
			
			float fontStartY = newSeatSize / 2 - fontHeight / 2;
			float fontStartX = getMeasuredWidth() / 2 - fontWidth / 2;
			canvas.drawText("" + i, fontStartX, newBoxSize * i + paddingTop + (newSeatSize - newBoxSize) / 2 + fontHeight
					+ fontStartY, fontPaint);
		}
	}
}
