package com.example.seat;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PointF;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * 
 * @author samuel
 * @date 2013-7-8
 * @Description:影院座位表类
 * 
 */
@SuppressLint("DrawAllocation")
public class SeatView extends ImageView {

	public static int ROW = 12;// 设置最大列数
	public static int COL = 15;// 设置最大行数
	// 座位paint
	Paint paint = new Paint();
	// 正常位置图片
	Bitmap normalSeatSelected, normalSeatOptionSelect, normalSeatLock, normalSeatSell, normalSeatNull;

	// 原始盒子大小
	int originalBoxSize = 50;
	// 原始位置大小
	int originalSeatSize = 40;

	// 行
	int row = 0;
	// 列
	int column = 0;
	// 保存选中座位
	private List<Integer> selectedSeat = new ArrayList<Integer>();
	// 不可选位置
	private List<Integer> unavaliableSeat = new ArrayList<Integer>();

	static final int NONE = 0;// 初始状态
	static final int DRAG = 1;// 拖动
	static final int ZOOM = 2;// 缩放
	// 点击状态
	private int mode = NONE;

	static final float MIN_SCALE = 0.5f;// 最小缩放比例
	static final float MAX_SCALE = 1.5f;// 最大缩放比例
	// 缩放大小
	float currScale = 0.7f;
	// 保留上一次缩放比例
	float prevScale = currScale;
	// 手势down时 两点的距离
	float oldDistance;
	// 两点最新距离（比如缩放）
	float newDistance;
	// 手势down时的座位位置
	int downClickPosition;
	// 手势up时的座位位置
	int upClickPosition;
	// 两点中间位置(暂时没用到，打算用在两点之间缩放时焦点在两点中点进行)
	PointF middlePoint = new PointF();
	// 记录按下的位置(用于判断移动是否超过限制如果是则认定为移动)
	PointF downPoint = new PointF();
	// 记录上一个移动坐标的位置
	PointF prevPoint = new PointF();
	// 记录padding的位置（主要是top和left）
	RectF paddingRect = new RectF();
	// 缓存影院座位表的画图
	Bitmap seatBitmap;
	// 第一次加载
	boolean isFirst = true;

	// 缩放监听
	private ArrayList<SeatZoomListener> mSeatZoomListeners = new ArrayList<SeatZoomListener>();

	public SeatView(Context context) {
		super(context);
		init();
	}

	public SeatView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		row = ROW;
		column = COL;

		normalSeatSelected = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.seat_ok);
		normalSeatSell = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.seat_selled);
		normalSeatOptionSelect = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.seat_select);
		// normalSeatLock =
		// BitmapFactory.decodeResource(getContext().getResources(),
		// R.drawable.seat_null);
		// 过道
		normalSeatNull = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.seat_null);

		// screenWidth =
		// getContext().getResources().getDisplayMetrics().widthPixels;
		// screenHeight =
		// getContext().getResources().getDisplayMetrics().heightPixels;
	}

	public void addSeatZoomListener(SeatZoomListener listener) {
		mSeatZoomListeners.add(listener);
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// setMeasuredDimension(newBoxSize * column, newBoxSize * row);
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
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mode == ZOOM || mode == NONE) {
			Bitmap tempBitmap = drawSeat();
			canvas.drawBitmap(tempBitmap, paddingRect.left, paddingRect.top, paint);
			tempBitmap.recycle();
			seatBitmap = null;
		} else {
			if (seatBitmap == null) {
				seatBitmap = drawSeat();
			}
			canvas.drawBitmap(seatBitmap, paddingRect.left, paddingRect.top, paint);
		}

		if (isFirst) {
			for (int i = 0; i < mSeatZoomListeners.size(); i++) {
				// 缩放变化
				mSeatZoomListeners.get(i).zoomChange(row, column, Math.round(originalBoxSize * currScale),
						Math.round(originalSeatSize * currScale), paddingRect.left, paddingRect.top);
			}
			isFirst = false;
		}

	}

	private Bitmap drawSeat() {
		int newBoxSize = Math.round(originalBoxSize * currScale);
		int newSeatSize = Math.round(originalSeatSize * currScale);
		int seatBitmapWidth = (int) (newBoxSize * column);
		Bitmap seatBitmap = Bitmap.createBitmap(seatBitmapWidth, (int) (newBoxSize * row), Config.ARGB_8888);
		Canvas canvas = new Canvas(seatBitmap);
		// 已选
		Bitmap seatSelected = Bitmap.createScaledBitmap(normalSeatSelected, newSeatSize, newSeatSize, true);
		// 已售
		Bitmap seatSell = Bitmap.createScaledBitmap(normalSeatSell, newSeatSize, newSeatSize, true);
		// 可选
		Bitmap seatOptionSelect = Bitmap.createScaledBitmap(normalSeatOptionSelect, newSeatSize, newSeatSize, true);
		// 过道
		Bitmap seatNull = Bitmap.createScaledBitmap(normalSeatNull, newSeatSize, newSeatSize, true);

		// 画座位
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < column; j++) {
				canvas.drawBitmap(seatOptionSelect, j * (newBoxSize), i * (newBoxSize), null);

				// if (i == 7) {
				// canvas.drawBitmap(seatSell, j * (newBoxSize), i *
				// (newBoxSize), null);
				// unavaliableSeat.add(i * column + j);
				// }
				// if (j == 3) {
				// canvas.drawBitmap(seatSell, j * (newBoxSize), i *
				// (newBoxSize), null);
				// unavaliableSeat.add(i * column + j);
				// }
				// if (j == 14) {
				// canvas.drawBitmap(seatSell, j * (newBoxSize), i *
				// (newBoxSize), null);
				// unavaliableSeat.add(i * column + j);
				// }
				// // 过道
				// if (i == 5) {
				// canvas.drawBitmap(seatNull, j * (newBoxSize), i *
				// (newBoxSize), null);
				// unavaliableSeat.add(i * column + j);
				// }
				// // 过道
				// if (i == 9) {
				// canvas.drawBitmap(seatNull, j * (newBoxSize), i *
				// (newBoxSize), null);
				// unavaliableSeat.add(i * column + j);
				// }
			}
		}

		// 我的座位 变成绿色
		for (int i = 0; i < selectedSeat.size(); i++) {
			canvas.drawBitmap(seatSelected, (selectedSeat.get(i) % column) * newBoxSize, (selectedSeat.get(i) / column)
					* newBoxSize, null);
		}

		seatSelected.recycle();
		seatSell.recycle();
		seatOptionSelect.recycle();
		seatNull.recycle();

		return seatBitmap;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			System.out.println("ACTION_DOWN");
			downPoint.set(event.getX(), event.getY());
			prevPoint.set(event.getX(), event.getY());
			downClickPosition = getSeatPosition(event);
			break;
		case MotionEvent.ACTION_UP:
			System.out.println("ACTION_UP");
			if (mode == NONE) {
				// 点击
				upClickPosition = getSeatPosition(event);
				if (upClickPosition == downClickPosition) {
					if (selectedSeat.contains(upClickPosition)) {
						selectedSeat.remove(selectedSeat.indexOf(upClickPosition));
						invalidate();
					} else if (!unavaliableSeat.contains(upClickPosition)) {
						selectedSeat.add(upClickPosition);
						invalidate();
					}
				}
			}
			mode = NONE;

			break;
		case MotionEvent.ACTION_POINTER_UP:
			System.out.println("ACTION_POINTER_UP");
			mode = NONE;
			// 保存上一次缩放大小
			prevScale = currScale;

			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			System.out.println("ACTION_POINTER_DOWN");
			oldDistance = getDistance(event);
			// 如果连续两点距离大于10，则判定为多点模式
			if (oldDistance > 10f) {
				getMiddlePoint(middlePoint, event);
				// 标志缩放
				mode = ZOOM;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == ZOOM) {
				float newDist = getDistance(event);
				if (newDist > 10f) {
					currScale = newDist / oldDistance * prevScale;
					// 检查scale范围
					currScale = currScale < MIN_SCALE ? MIN_SCALE : (currScale > MAX_SCALE ? MAX_SCALE : currScale);

					invalidate();
				}
			} else {
				float distanceX = event.getX() - downPoint.x;
				float distanceY = event.getY() - downPoint.y;
				// 判断downPoint跟点击的位置是否超过20，如果超过就判定为移动，之后都用prevPoint小位移计算
				if (Math.sqrt(distanceX * distanceX + distanceY * distanceY) > 20) {
					// 标志拖动
					mode = DRAG;
					paddingRect.left += (event.getX() - prevPoint.x);
					paddingRect.top += (event.getY() - prevPoint.y);
					// System.out.println(paddingRect);
					// 判断边界
					if (paddingRect.left > 1) {
						paddingRect.left = 1;
					}
					if (paddingRect.top > 1) {
						paddingRect.top = 1;
					}

					// left加屏幕宽度大于影院宽度就说明已经到右侧的边框，left不能再减少(向左移)
					if (Math.abs(paddingRect.left) + getMeasuredWidth() > getRealWidth()) {
						paddingRect.left = -(getRealWidth() - getMeasuredWidth() + 5);
					}
					// top加屏幕高度大于影院高度就说明已经到底部的边框，top不能再减少(向上移)
					if (Math.abs(paddingRect.top) + getMeasuredHeight() > getRealHeight()) {
						paddingRect.top = -(getRealHeight() - getMeasuredHeight() + 5);
					}
					// 当座位表的真实高度或者宽度都小于measure的就直接固定位置
					if (getMeasuredWidth() > getRealWidth()) {
						paddingRect.left = 1;
					}
					if (getMeasuredHeight() > getRealHeight()) {
						paddingRect.top = 1;
					}

					prevPoint.set(event.getX(), event.getY());
					invalidate();
				}
			}
			for (int i = 0; i < mSeatZoomListeners.size(); i++) {
				// 缩放变化
				mSeatZoomListeners.get(i).zoomChange(row, column, Math.round(originalBoxSize * currScale),
						Math.round(originalSeatSize * currScale), paddingRect.left, paddingRect.top);
			}
			break;
		}
		return true;
	}

	/**
	 * 获取影院座位表实际宽度(该宽度会随着放大缩小变化)
	 * 
	 * @return
	 */
	private int getRealWidth() {
		return Math.round(originalBoxSize * currScale) * column;
	}

	/**
	 * 获取影院座位表实际高度(该高度会随着放大缩小变化)
	 * 
	 * @return
	 */
	private int getRealHeight() {
		return Math.round(originalBoxSize * currScale) * row;
	}

	/**
	 * 两点距离
	 * 
	 * @param event
	 * @return
	 */
	private float getDistance(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/**
	 * 两点的中点
	 * */
	private void getMiddlePoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	/**
	 * 根据坐标获取座位位置
	 * 
	 * @param event
	 * @return
	 */
	private int getSeatPosition(MotionEvent event) {
		int newBoxSize = Math.round(originalBoxSize * currScale);
		float currentXPosition = event.getX() + Math.abs(paddingRect.left);
		float currentYPosition = event.getY() + Math.abs(paddingRect.top);
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < column; j++) {
				if ((j * newBoxSize) < currentXPosition && currentXPosition < j * newBoxSize + newBoxSize
						&& (i * newBoxSize) < currentYPosition && currentYPosition < i * newBoxSize + newBoxSize) {
					return i * column + j;
				}
			}
		}
		return 0;
	}

}
