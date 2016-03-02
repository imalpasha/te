package com.metech.fireflyprintingkiosk.api10;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MyToast {
	private static Toast toast;

	public static Toast makeText(Context context, String text, int length) {
		View layout = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, null);
		TextView tv = (TextView) layout.findViewById(android.R.id.text1);
		tv.setBackgroundColor(Color.parseColor("#99000000"));
		tv.setTextSize(40);
		tv.setPadding(25, 5, 25, 5);
		tv.setText(text);

		toast = new Toast(context);
		toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.FILL_HORIZONTAL, 0, 50);
		toast.setDuration(length);
		toast.setView(layout);
		return toast;
	}
}
