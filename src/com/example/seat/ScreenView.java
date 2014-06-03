package com.example.seat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.FontMetrics;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

/**
 * 
 * @author samuel
 * @date 2013-7-9
 * @Description:荧屏界面
 * 
 */
@SuppressLint("DrawAllocation")
public class ScreenView extends View implements SeatZoomListener {

	// 字体paint
	Paint fontPaint = new Paint();
	int row = 0;
	int column = 0;
	int newBoxSize = 0;
	int newSeatSize = 0;
	float paddingTop = 0;
	float paddingLeft = 0;
	// 荧屏名称
	String screenName = "荧屏";
	// 荧屏默认高度和宽度
	final int screenWidth = 200;
	final int screenHeight = 50;
	//
	int screenPadding = 10;

	public ScreenView(Context context) {
		super(context);
		init();
	}

	public ScreenView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		fontPaint.setAntiAlias(true);
		fontPaint.setColor(Color.WHITE);
		fontPaint.setTextSize(20);
	}

	/**
	 * 设置荧屏名称
	 * 
	 * @param srceenName
	 */
	public void setScreenName(String screenName) {
		if (TextUtils.isEmpty(screenName)) {
			screenName = "荧屏";
		}
		this.screenName = screenName;
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
		this.column = column;
		this.newBoxSize = newBoxSize;
		this.paddingTop = paddingTop;
		this.paddingLeft = paddingLeft;
		this.newSeatSize = newSeatSize;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {

		if (newBoxSize == 0) {
			return;
		}
		// 字体宽度
		int screenNameWidth = Math.round(fontPaint.measureText(screenName));
		// 字体高度
		FontMetrics fm = fontPaint.getFontMetrics();
		float screenNameHeight = fm.descent - fm.ascent;
		// 座位表真实宽度
		int seatBitmapWidth = newBoxSize * column;
		int startScreenX = seatBitmapWidth / 2 - screenWidth / 2;
		fontPaint.setColor(Color.RED);
		// 画荧屏
		canvas.drawRect(
				new RectF(startScreenX + paddingLeft, 0, startScreenX + paddingLeft + screenWidth, screenHeight),
				fontPaint);
		fontPaint.setColor(Color.WHITE);
		// 画荧屏名称
		canvas.drawText(screenName, paddingLeft + startScreenX + (screenWidth / 2 - screenNameWidth / 2), screenHeight
				/ 2 + screenNameHeight / 2, fontPaint);
	}
}
