package com.metech.fireflyprintingkiosk.api10;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.metech.fireflyprintingkiosk.api10.UpdateTravelDoc.PassengerAdapter.PassengerItem;

public class UpdateTravelDoc extends Activity {

	public static final String KEY_UPDATE_COMPLETE = "UpdateTravelDoc:KEY_UPDATE_COMPLETE";
	public static final String DEPARTURE_CODE = "UpdateTravelDoc:DEPARTURE_CODE";
	public static final String ARRIVAL_CODE = "UpdateTravelDoc:ARRIVAL_CODE";
	public static final String PNR = "UpdateTravelDoc:PNR";
	public static final String PASSENGER = "UpdateTravelDoc:PASSENGER";
	public static final SimpleDateFormat SDF_DD_MMM_YYY = new SimpleDateFormat("dd MMM yyyy");

	public static Intent newInstance(Context context, String pnr, JSONArray passenger) {
		Intent intent = new Intent(context, UpdateTravelDoc.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(PNR, pnr);
		intent.putExtra(PASSENGER, passenger.toString());
		return intent;
	}

	public static List<DropDownItem> COUNTRY_LIST;
	public static List<DropDownItem> DOC_TYPE_LIST;
	// public static JSONObject countryListKey;
	public AQuery aq;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		getWindow().setWindowAnimations(0);
		setContentView(R.layout.update_travel_doc);
		// MyToast.makeText(this, "Testing a long long long Toast message to display", Toast.LENGTH_LONG).show();

		aq = new AQuery(this);
		if (icicle == null) {
			aq.progress(setProgressDialog(this)).ajax(MainActivity.URL_COUNTRY_LIST, JSONObject.class, callbackCountryList);
		}
	}

	public static ProgressDialog setProgressDialog(Activity activity) {
		ProgressDialog dialog = new ProgressDialog(activity);
		dialog.setIndeterminate(true);
		dialog.setCancelable(false);
		dialog.setMessage("Please wait...");
		return dialog;
	}

	AjaxCallback<JSONObject> callbackCountryList = new AjaxCallback<JSONObject>() {
		@Override
		public void callback(String url, JSONObject json, AjaxStatus status) {
			if (json != null) {
				// XXX 2014-06-03
				// successful ajax call
				JSONArray countries = json.optJSONArray("country");

				COUNTRY_LIST = new ArrayList<DropDownItem>();
				for (int i = 0; i < countries.length(); i++) {
					JSONObject c = countries.optJSONObject(i);
					COUNTRY_LIST.add(new DropDownItem(c.optString("country_code"), c.optString("country_name")));
				}
				// countryListKey = json.optJSONObject("country_key");

				// XXX 2014-06-03
				DOC_TYPE_LIST = new ArrayList<DropDownItem>();
				DOC_TYPE_LIST.add(new DropDownItem("NRIC", "NRIC"));
				DOC_TYPE_LIST.add(new DropDownItem("P", "Passport"));
				DOC_TYPE_LIST.add(new DropDownItem("V", "Visa"));

				PlaceholderFragment frag = new PlaceholderFragment();
				frag.setArguments(getIntent().getExtras());
				getFragmentManager().beginTransaction().add(R.id.container, frag).commit();
			}
		}
	};

	public static class PlaceholderFragment extends ListFragment {

		private AQuery aq;
		Calendar cal = Calendar.getInstance();

		public PlaceholderFragment() {
			aq = new AQuery(getActivity());
		}

		@Override
		public void onActivityCreated(Bundle icicle) {
			super.onActivityCreated(icicle);
			ListView listView = getListView();
			listView.setDividerHeight(0);
			listView.setDivider(null);
			listView.setScrollBarSize(0);
			listView.setCacheColorHint(getResources().getColor(android.R.color.transparent));
			listView.setHorizontalScrollBarEnabled(false);
			listView.setVerticalScrollBarEnabled(false);
			listView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

			String passenger = getArguments().getString(PASSENGER);
			final String pnr = getArguments().getString(PNR);
			String departureCode = getArguments().getString(DEPARTURE_CODE);
			String arrivalCode = getArguments().getString(ARRIVAL_CODE);

			ArrayList<PassengerItem> items = new ArrayList<PassengerItem>();
			// Log.e("passenger", passenger.toString());
			try {
				JSONArray passengers = new JSONArray(passenger.toString());
				for (int i = 0; i < passengers.length(); i++) {
					JSONObject p = passengers.optJSONObject(i);
					int passengerNumber = p.optInt("PassengerNumber");
					String passengerName = p.optString("FirstName") + " " + p.optString("LastName");
					String docTypeCode = p.optString("DocTypeCode");
					String docNumber = p.optString("DocNumber");
					String countryCode = p.optString("IssuedByCode");
					String birthDate = p.optString("DOB");
					String expiryDate = p.optString("ExpirationDate");
					docTypeCode = (docTypeCode.isEmpty()) ? "NRIC" : docTypeCode;
					countryCode = (countryCode.isEmpty() && docTypeCode.equals("NRIC")) ? "MY" : countryCode;
					if (departureCode.equals("SIN")) {
						docTypeCode = "P";
					}
					PassengerItem passengerItem = new PassengerItem(passengerNumber, passengerName, docTypeCode, docNumber, countryCode, birthDate, expiryDate);
					passengerItem.setStations(departureCode, arrivalCode);
					items.add(passengerItem);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			final PassengerAdapter adapter = new PassengerAdapter(getActivity());
			adapter.updateItems(items);

			View submitView = getActivity().getLayoutInflater().inflate(R.layout.submit_button, null);
			aq.recycle(submitView);
			aq.id(R.id.btn_cancel).clicked(new OnClickListener() {
				@Override
				public void onClick(View v) {
					getActivity().finish();
				}
			});
			aq.id(R.id.btn_submit).clicked(new OnClickListener() {
				@Override
				public void onClick(View v) {
					JSONArray jsonArr = new JSONArray();

					try {
						// XXX 2014-06-03
						for (int i = 0; i < adapter.getCount(); i++) {
							PassengerItem p = adapter.getItem(i);

							for (DropDownItem iter : DOC_TYPE_LIST) {
								if (iter.text.equals(p.docType)) {
									p.docType = iter.key;
								}
							}
							for (DropDownItem iter : COUNTRY_LIST) {
								if (iter.text.equals(p.country)) {
									p.country = iter.key;
								}
							}
							if (p.docType.isEmpty()) {
								MyToast.makeText(getActivity(), "Please select document type", Toast.LENGTH_SHORT).show();
								return;
							} else {
								if (p.country.isEmpty() || p.docNumber.isEmpty() || p.birthDate.isEmpty()) {
									MyToast.makeText(getActivity(), "Please fill in the blank text box", Toast.LENGTH_SHORT).show();
									return;
								}
								if (p.expiryDate.isEmpty() && p.docType.equals("NRIC") == false) {
									MyToast.makeText(getActivity(), "Please specify passport expiry date", Toast.LENGTH_SHORT).show();
									return;
								}
							}

							JSONObject jsonObj = new JSONObject();
							jsonObj.put("PassengerNumber", p.passengerNumber);
							jsonObj.put("TravelDocument", p.docType);
							jsonObj.put("IssuingCountry", p.country);
							jsonObj.put("DocumentNumber", p.docNumber);
							jsonObj.put("ExpirationDate", p.expiryDate);
							jsonObj.put("DOB", p.birthDate);
							jsonArr.put(jsonObj);
						}

						Map<String, Object> params = new HashMap<String, Object>();
						params.put("pnr", pnr);
						params.put("data", jsonArr);
						Log.e("params", params.toString());
						aq.progress(setProgressDialog(getActivity())).ajax(MainActivity.URL_DOCUMENT_UPDATE, params, JSONObject.class, callbackDocUpdate);

					} catch (Exception e) {
						e.printStackTrace();
						Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
					}
				}
			});

			getListView().addFooterView(submitView);
			setListAdapter(adapter);
		}

		AjaxCallback<JSONObject> callbackDocUpdate = new AjaxCallback<JSONObject>() {
			@Override
			public void callback(String url, JSONObject json, AjaxStatus status) {
				// Log.e("callbackDocUpdate", " " + json.toString());
				if (json != null) {
					String s = json.optString("status");
					if (s.equals("ok")) {
						Intent i = MainActivity.newInstance(getActivity());
						i.putExtra(UpdateTravelDoc.KEY_UPDATE_COMPLETE, true);
						startActivity(i);
					} else {
						// ((UpdateTravelDoc) getActivity()).showErrorScreen(json.optString("message"));
						// return;
					}
					MyToast.makeText(getActivity(), json.optString("message"), Toast.LENGTH_LONG).show();
				}
			}
		};
	}

	public static class PassengerAdapter extends BaseAdapter {
		private class ViewHolder {
			public TextView passengerName;
			public EditText docNumber;
			public TextView docType;
			public TextView country;
			public TextView birthDate;
			public TextView expiryDate;
			public TextWatcher txtWatcherDocNumber;
			public TextWatcher txtWatcherDocType;
			public TextWatcher txtWatcherCountry;
			public TextWatcher txtWatcherBirthDate;
			public TextWatcher txtWatcherExpiryDate;
			public TextView labelExpiry;

			public ViewHolder() {
			}
		}

		public static class PassengerItem {
			public int passengerNumber;
			public String passengerName;
			public String docNumber;
			public String docType;
			public String country;
			public String birthDate;
			public String expiryDate;
			public String departureCode;
			public String arrivalCode;

			public PassengerItem(int passengerNumber, String passengerName, String docTypeCode, String docNumber, String countryCode, String birthDate, String expiryDate) {
				this.passengerNumber = passengerNumber;
				this.passengerName = passengerName;
				this.docType = docTypeCode;
				this.docNumber = docNumber;
				this.country = countryCode;// countryListKey.optString(countryCode);
				this.birthDate = birthDate;
				this.expiryDate = expiryDate;
			}

			public void setStations(String departureCode, String arrivalCode) {
				this.departureCode = departureCode;
				this.arrivalCode = arrivalCode;
			}
		}

		protected static final String DATEPICKER_BIRTH = "datePickerBirth";
		protected static final String DATEPICKER_EXPIRY = "datePickerExpiry";

		// public static List<DropDownItem> docTypeDropList = new ArrayList<DropDownItem>();
		// public static LinkedHashMap<String, String> countries = new LinkedHashMap<String, String>();

		private List<PassengerItem> items = Collections.emptyList();
		private Activity act;
		private AQuery aq;

		public static final int TYPE_ITEM_DEFAULT = 0;
		public static final int TYPE_ITEM_SUBMIT = 1;
		private int itemViewType = TYPE_ITEM_DEFAULT;

		public PassengerAdapter(Activity activity) {
			this.act = activity;
			aq = new AQuery(act);
		}

		public void updateItems(List<PassengerItem> items) {
			this.items = items;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public PassengerItem getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public int getItemViewType() {
			return itemViewType;
		}

		public int setItemViewType(int itemViewType) {
			return this.itemViewType = itemViewType;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;

			// object item based on the position
			final PassengerItem objItem = getItem(position);

			if (convertView == null) {
				holder = new ViewHolder();

				// inflate the layout
				// well set up the ViewHolder
				convertView = LayoutInflater.from(act).inflate(R.layout.update_travel_doc_item, parent, false);
				aq.recycle(convertView);
				holder.passengerName = aq.id(R.id.passenger_name).getTextView();
				holder.docType = aq.id(R.id.doc_type).getTextView();
				holder.docNumber = aq.id(R.id.ic_no).getEditText();
				holder.country = aq.id(R.id.country).getTextView();
				holder.birthDate = aq.id(R.id.birth_date).getTextView();
				holder.expiryDate = aq.id(R.id.expiry_date).getTextView();
				holder.labelExpiry = aq.id(R.id.label_expiry).getTextView();

				holder.docNumber.setFilters(new InputFilter[] { alphaNumericFilter });

				// doc type menu adapter
				// //////////////////////////////////////////////////
				DropMenuAdapter dropDocTypeAdapter = new DropMenuAdapter(act);
				dropDocTypeAdapter.updateItems(DOC_TYPE_LIST);

				final Builder alertDocType = new AlertDialog.Builder(act);
				alertDocType.setSingleChoiceItems(dropDocTypeAdapter, -1, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						aq.id(holder.docType).text(DOC_TYPE_LIST.get(which).text);
						dialog.dismiss();
					}
				});
				aq.id(holder.docType).clicked(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						alertDocType.show();
					}
				});
				// //////////////////////////////////////////////////

				// country name adapter
				// //////////////////////////////////////////////////
				DropMenuAdapter dropCountryAdapter = new DropMenuAdapter(act);
				dropCountryAdapter.updateItems(COUNTRY_LIST);

				final Builder alertCountry = new AlertDialog.Builder(act);
				alertCountry.setSingleChoiceItems(dropCountryAdapter, -1, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						aq.id(holder.country).text(COUNTRY_LIST.get(which).text);
						dialog.dismiss();
					}
				});
				aq.id(holder.country).clicked(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						alertCountry.show();
					}
				});
				// //////////////////////////////////////////////////

				// date adapter
				// //////////////////////////////////////////////////
				aq.id(holder.birthDate).clicked(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						DatePickerFragment picker = new DatePickerFragment(new MyOnDateSetListener() {
							@Override
							public void returnDate(Date date) {
								Calendar cal = Calendar.getInstance();
								if (date.after(cal.getTime())) {
									MyToast.makeText(act, "Invalid date of birth", Toast.LENGTH_SHORT).show();
									return;
								}
								String d = SDF_DD_MMM_YYY.format(date);
								holder.birthDate.setText(d);
							}
						});
						picker.show(act.getFragmentManager(), DATEPICKER_BIRTH);
					}
				});
				aq.id(holder.expiryDate).clicked(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						DatePickerFragment picker = new DatePickerFragment(new MyOnDateSetListener() {
							@Override
							public void returnDate(Date date) {
								Calendar cal = Calendar.getInstance();
								if (date.before(cal.getTime())) {
									MyToast.makeText(act, "Invalid date of expiry", Toast.LENGTH_SHORT).show();
									return;
								}
								String d = SDF_DD_MMM_YYY.format(date);
								holder.expiryDate.setText(d);
							}
						});
						picker.show(act.getFragmentManager(), DATEPICKER_EXPIRY);
					}
				});
				// //////////////////////////////////////////////////

				// store the holder with the view.
				convertView.setTag(holder);

			} else {
				// we've just avoided calling findViewById() on resource
				// everytime just use the viewHolder
				holder = (ViewHolder) convertView.getTag();
			}

			if (holder != null) {
				setTextWatcher(holder, objItem);

				holder.passengerName.setText(objItem.passengerName);

				// XXX 2014-06-03
				// holder.docType.setText(objItem.docType);
				for (DropDownItem iter : DOC_TYPE_LIST) {
					if (iter.key.equals(objItem.docType)) {
						aq.id(holder.docType).text(iter.text);
					}
				}
				holder.docNumber.setText(objItem.docNumber);

				// XXX 2014-06-03
				// holder.country.setText(objItem.country);
				for (DropDownItem iter : COUNTRY_LIST) {
					if (iter.key.equals(objItem.country)) {
						aq.id(holder.country).text(iter.text);
					}
				}

				if (objItem.birthDate.equals("01 Jan 1970")) {
					objItem.birthDate = "";
				}
				if (objItem.expiryDate.equals("01 Jan 1970")) {
					objItem.expiryDate = "";
				}
				holder.birthDate.setText(objItem.birthDate);
				holder.expiryDate.setText(objItem.expiryDate);
			}

			return convertView;
		}

		private void setTextWatcher(final ViewHolder holder, final PassengerItem objectItem) {
			// Remove previous TextWatcher if any
			if (holder.txtWatcherDocType != null) {
				holder.docType.removeTextChangedListener(holder.txtWatcherDocType);
			}
			if (holder.txtWatcherDocNumber != null) {
				holder.docNumber.removeTextChangedListener(holder.txtWatcherDocNumber);
			}
			if (holder.txtWatcherCountry != null) {
				holder.country.removeTextChangedListener(holder.txtWatcherCountry);
			}
			if (holder.txtWatcherBirthDate != null) {
				holder.birthDate.removeTextChangedListener(holder.txtWatcherBirthDate);
			}
			if (holder.txtWatcherExpiryDate != null) {
				holder.expiryDate.removeTextChangedListener(holder.txtWatcherExpiryDate);
			}

			holder.txtWatcherDocType = new TextWatcher() {
				@Override
				public void afterTextChanged(Editable editable) {
					objectItem.docType = editable.toString();
					if (objectItem.docType.equals("NRIC")) {
						holder.expiryDate.setVisibility(View.INVISIBLE);
						holder.labelExpiry.setVisibility(View.INVISIBLE);
					} else {
						holder.expiryDate.setVisibility(View.VISIBLE);
						holder.labelExpiry.setVisibility(View.VISIBLE);
					}
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}
			};

			holder.txtWatcherDocNumber = new TextWatcher() {
				@Override
				public void afterTextChanged(Editable editable) {
					objectItem.docNumber = editable.toString();
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}
			};

			holder.txtWatcherCountry = new TextWatcher() {
				@Override
				public synchronized void afterTextChanged(Editable editable) {
					objectItem.country = editable.toString();
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}
			};

			holder.txtWatcherBirthDate = new TextWatcher() {
				@Override
				public void afterTextChanged(Editable editable) {
					objectItem.birthDate = editable.toString();
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}
			};

			holder.txtWatcherExpiryDate = new TextWatcher() {
				@Override
				public void afterTextChanged(Editable editable) {
					objectItem.expiryDate = editable.toString();
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}
			};

			holder.docType.addTextChangedListener(holder.txtWatcherDocType);
			holder.docNumber.addTextChangedListener(holder.txtWatcherDocNumber);
			holder.country.addTextChangedListener(holder.txtWatcherCountry);
			holder.birthDate.addTextChangedListener(holder.txtWatcherBirthDate);
			holder.expiryDate.addTextChangedListener(holder.txtWatcherExpiryDate);
		}

		InputFilter alphaNumericFilter = new InputFilter() {
			@Override
			public CharSequence filter(CharSequence arg0, int arg1, int arg2, Spanned arg3, int arg4, int arg5) {
				for (int k = arg1; k < arg2; k++) {
					if (!Character.isLetterOrDigit(arg0.charAt(k))) {
						return "";
					}
				}
				return null;
			}
		};

		public interface MyOnDateSetListener {
			void returnDate(Date date);
		}

		public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
			private MyOnDateSetListener mListener;

			public DatePickerFragment(MyOnDateSetListener onDateSetListener) {
				this.mListener = onDateSetListener;
			}

			@Override
			public Dialog onCreateDialog(Bundle savedInstanceState) {
				// Use the current date as the default date in the picker
				final Calendar c = Calendar.getInstance();
				int year = c.get(Calendar.YEAR);
				int month = c.get(Calendar.MONTH);
				int day = c.get(Calendar.DAY_OF_MONTH);

				if (getTag().equals(DATEPICKER_BIRTH)) {
					year = 1980;
					month = 6 - 1;
					day = 1;
				}

				// Create a new instance of DatePickerDialog and return it
				return new DatePickerDialog(getActivity(), this, year, month, day);
			}

			@Override
			public void onDateSet(DatePicker view, int year, int month, int day) {
				Calendar c = Calendar.getInstance();
				c.set(year, month, day);
				this.mListener.returnDate(c.getTime());
			}
		}
	}

	private static class DropDownItem {
		public String text;
		public String key;

		public DropDownItem(String key, String text) {
			this.key = key;
			this.text = text;
		}
	}

	public static class DropMenuAdapter extends BaseAdapter {

		private class ViewHolder {
			public TextView txtText;

			public ViewHolder() {
			}
		}

		private List<DropDownItem> items = Collections.emptyList();
		private AQuery aq;
		private Activity act;

		public DropMenuAdapter(Activity activity) {
			this.act = activity;
			aq = new AQuery(act);
		}

		public void updateItems(List<DropDownItem> items) {
			this.items = items;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public DropDownItem getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			// object item based on the position
			DropDownItem objectItem = getItem(position);

			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(act).inflate(android.R.layout.simple_spinner_item, parent, false);
				aq.recycle(convertView);
				holder.txtText = aq.id(android.R.id.text1).getTextView();
				holder.txtText.setTextSize(50);
				holder.txtText.setPadding(10, 10, 10, 10);
				holder.txtText.setGravity(Gravity.CENTER_HORIZONTAL);

				// store the holder with the view.
				convertView.setTag(holder);

			} else {
				// we've just avoided calling findViewById() on resource
				// everytime just use the viewHolder
				holder = (ViewHolder) convertView.getTag();
			}

			// assign values if the object is not null
			if (objectItem != null) {
				aq.id(holder.txtText).text(objectItem.text);
			}

			return convertView;
		}
	}

	// public static void hideSoftKeyboard(Activity activity) {
	// InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
	// inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
	// }
	//
	// public void hideKeyboardOnOutsideTouch(View view) {
	// // Set up touch listener for non-text box views to hide keyboard.
	// if (!(view instanceof EditText)) {
	// view.setOnTouchListener(new OnTouchListener() {
	// public boolean onTouch(View v, MotionEvent event) {
	// hideSoftKeyboard(UpdateTravelDoc.this);
	// return false;
	// }
	// });
	// }
	//
	// // If a layout container, iterate over children and seed recursion.
	// if (view instanceof ViewGroup) {
	// for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
	// View innerView = ((ViewGroup) view).getChildAt(i);
	// hideKeyboardOnOutsideTouch(innerView);
	// }
	// }
	// }
	//
	// private void showErrorScreen(String message) {
	// setContentView(R.layout.error);
	// TextView textView = (TextView) findViewById(R.id.errorlabel);
	// textView.setText(message);
	//
	// backToMain();
	// }
	//
	// private void backToMain() {
	// Handler handler = new Handler();
	// handler.postDelayed(new Runnable() {
	// @Override
	// public void run() {
	// // ((MainActivity) getParent()).showMainMenu();
	// finish();
	// }
	// }, 3000);
	// }

}
