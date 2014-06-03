package com.example.seat;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MovieSelectSeatActivity1 extends Activity {

	private TextView yingmuTextView;

	private SeatView seatView;
	private SeatNumberView seatNumberView;
	private ScreenView screenView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_movie_select_seat1);
		initView();
	}

	private void initView() {
		yingmuTextView = (TextView) findViewById(R.id.yingmu);
		yingmuTextView.setText("测试影院" + " 测试厅" + " 荧幕");

		seatView = (SeatView) findViewById(R.id.seat_view);
		seatNumberView = (SeatNumberView) findViewById(R.id.seat_number_view);
		screenView = (ScreenView) findViewById(R.id.screen_view);

		seatView.addSeatZoomListener(seatNumberView);
		seatView.addSeatZoomListener(screenView);
	}
	
}
