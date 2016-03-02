package com.metech.fireflyprintingkiosk.api10;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.epson.eposprint.Builder;
import com.epson.eposprint.EposException;
import com.epson.eposprint.Print;

public class MainActivity extends Activity implements OnClickListener {

	int page = 1;
	int action = 1;
	long idle = 0;
	long lastUsed = 0;
	int width = 1280;
	int height = 800;
	String pnr = "";
	String departureStationCode = "";
	String arrivalStationCode = "";
	String STD = "";
	String checkIn = "";
	String jsonCheckPNR = "";
	String jsonCheckInPassenger = "";
	String jsonPrintBoardingPass = "";
	String device_notes = "";

	static final int SEND_TIMEOUT = 180 * 1000;

	static final int MAIN_MENU_BUTTON_ENTER_FIREFLY_PNR = 1001;
	static final int MAIN_MENU_BUTTON_SCAN_QR_CODE = 1002;
	static final int MAIN_MENU_BUTTON_ENTER_CODESHARE_PNR = 1028;

	static final int ENTER_PNR_BUTTON_ALPHANUMERIC = 1003;
	static final int ENTER_PNR_BUTTON_BACKSPACE = 1004;
	static final int ENTER_PNR_BUTTON_CANCEL = 1005;
	static final int ENTER_PNR_BUTTON_NEXT = 1006;

	static final int SECTOR_BUTTON_FLIGHT = 1007;
	static final int SECTOR_BUTTON_CANCEL = 1008;

	static final int PASSENGER_LIST_CHECKBOX = 1009;
	static final int PASSENGER_LIST_BUTTON_CANCEL = 1010;
	static final int PASSENGER_LIST_BUTTON_NEXT = 1011;
	static final int PASSENGER_LIST_SCROLLVIEW = 1026;
	static final int PASSENGER_LIST_BUTTON_NAME = 1027;

	static final int CHECKED_IN_BUTTON_PRINT_BOARDING_PASS = 1012;
	static final int CHECKED_IN_BUTTON_MAIN_MENU = 1013;

	static final int TASK_MENU_BUTTON_CHECK_IN = 1014;
	static final int TASK_MENU_BUTTON_REPRINT_BOARDING_PASS = 1015;
	static final int TASK_MENU_BUTTON_CANCEL = 1016;

	static final int AGREEMENT_CHECKBOX = 1017;
	static final int AGREEMENT_BUTTON_NEXT = 1018;
	static final int AGREEMENT_BUTTON_CANCEL = 1019;

	static final int DANGEROUS_GOODS_CHECKBOX = 1023;
	static final int DANGEROUS_GOODS_BUTTON_NEXT = 1024;
	static final int DANGEROUS_GOODS_BUTTON_CANCEL = 1025;

	static final int SPECIAL_MENU_EXIT = 1020;
	static final int SPECIAL_MENU_INFO = 1021;
	static final int SPECIAL_MENU_TEST = 1022;

	static final int BIG_BUTTON_WIDTH = 1280 - 200;
	static final int BIG_BUTTON_HEIGHT = 200;

	static final int MEDIUM_BUTTON_WIDTH = 1280 - 200;
	static final int MEDIUM_BUTTON_HEIGHT = 150;

	static final int SMALL_BUTTON_WIDTH = 300;
	static final int SMALL_BUTTON_HEIGHT = 70;
	static final int SMALL_BUTTON_BOTTOM_Y = 650;

	static final float TEXT_SIZE_50 = 44f;
	static final float TEXT_SIZE_40 = 32f;
	static final float TEXT_SIZE_36 = 28f;

	static final String URL_KIOSK = "http://fymobilekiosk.me-tech.com.my/kiosk2/";
	static final String URL_DEVICE_LOG = URL_KIOSK + "kiosk2_device_log.php";
	static final String URL_DEVICE_STATUS = URL_KIOSK
			+ "kiosk2_device_status.php";
	static final String URL_PING_LOG = URL_KIOSK + "kiosk2_ping_log.php";
	static final String URL_BANNER = URL_KIOSK + "kiosk2_banner.php";
	static final String URL_PRINT_BOARDING_PASS = URL_KIOSK
			+ "kiosk2_print_boarding_pass.php";
	static final String URL_CHECK_PNR = URL_KIOSK + "kiosk2_check_pnr.php";
	static final String URL_CHECKIN_PASSENGER = URL_KIOSK
			+ "kiosk2_checkin_passenger.php";
	static final String URL_COUNTRY_LIST = URL_KIOSK
			+ "kiosk2_country_list.php";
	static final String URL_DOCUMENT_UPDATE = URL_KIOSK
			+ "kiosk2_document_update.php";

	String ip_address = "";
	String macaddr = "";
	String ssid = "";
	String printer_ip = "192.168.1.200";
	String regId = "";
	boolean online = true;
	boolean run = true;
	static int[] PNR_LENGTH = { 6, 5 };
	public static String countryListKey;

	String device_name = "N/A";
	String location_name = "N/A";
	String server_url = "N/A";
	String printer_status = "N/A";
	String pin = "123456";
	String device_status = "Y";
	String device_inactive_message = "";

	public boolean flag = false;

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	int errorScreen = 0;
	int errorScreen2 = 0;

	PendingIntent intent;

	@Override
	protected void onStop() {
		super.onStop(); // Always call the superclass method first
		if (flag == false) {
			AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			mgr.set(AlarmManager.RTC, System.currentTimeMillis() + (5 * 60000), intent);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Crashlytics.start(this);
		// Calendar cal = Calendar.getInstance();
		// Calendar dateToday = cal;
		// try {
		// cal.setTime(UpdateTravelDoc.SDF_DD_MMM_YYY.parse("17 Jun 2014"));
		// } catch (ParseException e) {
		// e.printStackTrace();
		// }
		// cal.add(Calendar.MONTH, -6);
		// Log.e("cal", cal.compareTo(dateToday) + "");
		// if (cal.compareTo(dateToday) == 1) {
		// }

		intent = PendingIntent.getActivity(this.getApplicationContext(), 0,
				new Intent(getIntent()), getIntent().getFlags());

		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread paramThread,
					Throwable paramThrowable) {
				AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
				mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000,
						intent);
				System.exit(2);
			}
		});

		WifiManager wifiMan = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInf = wifiMan.getConnectionInfo();
		macaddr = wifiInf.getMacAddress();
		ssid = wifiInf.getSSID();

		run = true;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		if (isOnline()) {
			online = true;
			showConnecting();
		} else {
			online = false;
			showOffline();
		}

		lastUsed = System.currentTimeMillis();

		pd = ProgressDialog.show(this, "", "Please wait...", true, false);
		pd.hide();
	}

	public void showConnecting() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpParams httpParams = new BasicHttpParams();
					HttpConnectionParams
							.setConnectionTimeout(httpParams, 60000);
					HttpConnectionParams.setSoTimeout(httpParams, 60000);
					DefaultHttpClient client = new DefaultHttpClient(httpParams);
					HttpPost httppost = new HttpPost(URL_DEVICE_LOG);
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair("macaddr",
							macaddr));
					nameValuePairs.add(new BasicNameValuePair("action", "01"));
					nameValuePairs.add(new BasicNameValuePair("pnr", ""));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse httpResponse = client.execute(httppost);
				} catch (Exception e) {

				}
			}
		}).start();

		page = 2;
		setContentView(R.layout.error);
		TextView textView = (TextView) findViewById(R.id.errorlabel);
		textView.setText("Connecting to the server ...");
		new Thread(new Runnable() {
			@Override
			public void run() {

				String returnString = null;
				String show_printer_offline = "";
				try {
					returnString = kiosk2_device_status();
					if (returnString == null || returnString.isEmpty()) {
						returnString = kiosk2_device_status();
					}
					if (returnString == null || returnString.isEmpty()) {
						returnString = kiosk2_device_status();
					}
					JSONObject object = (JSONObject) new JSONTokener(
							returnString).nextValue();
					String trigger_show_banner = object
							.getString("trigger_show_banner");
					String json_check_in = object.getString("check_in");
					String json_print_boarding_pass = object
							.getString("print_boarding_pass");
					String json_printer_ip = object.getString("printer_ip");
					String json_show_printer_offline = object
							.getString("show_printer_offline");
					String json_device_name = object.getString("device_name");
					String json_location_name = object
							.getString("location_name");
					String json_server_url = object.getString("server_url");
					String json_printer_status = object
							.getString("printer_status");
					String json_pin = object.getString("pin");
					String json_device_status = object
							.getString("device_status");
					String json_device_inactive_message = object
							.getString("device_inactive_message");
					if (json_printer_ip != null) {
						printer_ip = json_printer_ip;
					}
					if (json_show_printer_offline != null) {
						show_printer_offline = json_show_printer_offline;
					}
					if (json_device_name != null) {
						device_name = json_device_name;
					}
					if (json_location_name != null) {
						location_name = json_location_name;
					}
					if (json_server_url != null) {
						server_url = json_server_url;
					}
					if (json_printer_status != null) {
						printer_status = json_printer_status;
					}
					if (json_pin != null) {
						pin = json_pin;
					}
					if (json_device_status != null) {
						device_status = json_device_status;
					}
					if (json_device_inactive_message != null) {
						device_inactive_message = json_device_inactive_message;
					}

					epsonPrinterTask = 0;
					epsonPrinterTask();

				} catch (JSONException e) {

				}

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showMainMenu();
					}
				});

			}
		}).start();
	}

	@Override
	protected void onPause() {

		run = false;

		super.onPause();
	}

	public String kiosk2_device_status() {
		String returnString = null;

		WifiManager wifiMan = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInf = wifiMan.getConnectionInfo();
		macaddr = wifiInf.getMacAddress();
		ssid = wifiInf.getSSID();
		int ipAddress = wifiInf.getIpAddress();

		ip_address = String.format("%d.%d.%d.%d", (ipAddress & 0xff),
				(ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
				(ipAddress >> 24 & 0xff));

		try {
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 60000);
			HttpConnectionParams.setSoTimeout(httpParams, 60000);
			DefaultHttpClient client = new DefaultHttpClient(httpParams);
			HttpPost httppost = new HttpPost(URL_DEVICE_STATUS);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("macaddr", macaddr));
			nameValuePairs.add(new BasicNameValuePair("ssid", ssid));
			nameValuePairs.add(new BasicNameValuePair("printer_status", ""
					+ epsonPrinterStatus));
			nameValuePairs.add(new BasicNameValuePair("idle", "" + idle));
			nameValuePairs.add(new BasicNameValuePair("local_ip", ""
					+ ip_address));
			nameValuePairs.add(new BasicNameValuePair("version", "3.4"));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse httpResponse = client.execute(httppost);
			HttpEntity entity = httpResponse.getEntity();
			InputStream is = entity.getContent();
			returnString = convertStreamToString(is);
		} catch (Exception exception) {

		}
		return returnString;
	}

	int flagShowPrinterOffline = 0;

	boolean kiosk_online = false;
	boolean printer_online = false;

	String action_log = "";

	boolean screen = true;
	boolean qrcode_flag = false;

	@Override
	protected void onResume() {

		run = true;

		// ping
		/*
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (run == true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {

					}
					String ping_result = "";
					try {
						Runtime r = Runtime.getRuntime();
						Process p = r.exec(new String[] { "ping", "-c 60",
								"fymobilekiosk.me-tech.com.my" });
						BufferedReader in = new BufferedReader(
								new InputStreamReader(p.getInputStream()));
						String inputLine;
						while ((inputLine = in.readLine()) != null) {
							ping_result += inputLine + "\n";
						}
						in.close();
					} catch (Exception e) {

					}
					try {
						HttpParams httpParams = new BasicHttpParams();
						HttpConnectionParams.setConnectionTimeout(httpParams,
								180000);
						HttpConnectionParams.setSoTimeout(httpParams, 60000);
						DefaultHttpClient client = new DefaultHttpClient(
								httpParams);
						HttpPost httppost = new HttpPost(URL_PING_LOG);
						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
								2);
						nameValuePairs.add(new BasicNameValuePair("macaddr",
								macaddr));
						nameValuePairs.add(new BasicNameValuePair(
								"ping_result", ping_result));

						httppost.setEntity(new UrlEncodedFormEntity(
								nameValuePairs));
						HttpResponse httpResponse = client.execute(httppost);
						HttpEntity entity = httpResponse.getEntity();
						InputStream is = entity.getContent();
					} catch (Exception e) {

					}
				}
			}
		}).start();


		new Thread(new Runnable() {
			@Override
			public void run() {
				while (run == true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {

					}
					String ping_result = "";
					try {
						Runtime r = Runtime.getRuntime();
						Process p = r.exec(new String[] { "ping", "-c 60",
								printer_ip });
						BufferedReader in = new BufferedReader(
								new InputStreamReader(p.getInputStream()));
						String inputLine;
						while ((inputLine = in.readLine()) != null) {
							ping_result += inputLine + "\n";
						}
						in.close();
					} catch (Exception e) {

					}
					try {
						HttpParams httpParams = new BasicHttpParams();
						HttpConnectionParams.setConnectionTimeout(httpParams,
								180000);
						HttpConnectionParams.setSoTimeout(httpParams, 60000);
						DefaultHttpClient client = new DefaultHttpClient(
								httpParams);
						HttpPost httppost = new HttpPost(URL_PING_LOG);
						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
								2);
						nameValuePairs.add(new BasicNameValuePair("macaddr",
								macaddr));
						nameValuePairs.add(new BasicNameValuePair(
								"ping_result", ping_result));

						httppost.setEntity(new UrlEncodedFormEntity(
								nameValuePairs));
						HttpResponse httpResponse = client.execute(httppost);
						HttpEntity entity = httpResponse.getEntity();
						InputStream is = entity.getContent();
					} catch (Exception e) {

					}
				}
			}
		}).start();
		*/

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (run == true) {
					try {
						Thread.sleep(1000);

					} catch (InterruptedException e) {

					}
					idle = System.currentTimeMillis() - lastUsed;
					// if idle more than 3 minutes
					if (page != 2) {
						if (idle > 60000 * 3) {
							lastUsed = System.currentTimeMillis();
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									showMainMenu();
								}
							});
						}
					}
					// if idle more than 10 minutes, dim the screen
					if (idle > 60000 * 10) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {

							}
						});
					}
				}
			}
		}).start();

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (run == true) {
					epsonPrinterTask = 0;
					epsonPrinterTask();
					Log.w("eps", "" + epsonPrinterStatus + " " + printer_ip);
					boolean flag2 = true;
					if (epsonPrinterStatus == 16777220
							|| epsonPrinterStatus == 16908292) {
						flag2 = true;
					} else {
						flag2 = false;
					}
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {

					}
					if (flag2 == false) {
						epsonPrinterTask = 0;
						epsonPrinterTask();
						Log.w("eps", "" + epsonPrinterStatus + " " + printer_ip);
						if (epsonPrinterStatus == 16777220
								|| epsonPrinterStatus == 16908292) {
							flag2 = true;
						} else {
							flag2 = false;
						}
					}
					if (flag2 == false) {
						printer_online = false;
					} else {
						printer_online = true;
					}
					// printer_online = true;
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (page != 100) {
								if (kiosk_online == false) {
									showOffline();
								} else if (device_status.equals("N")) {
									showDeviceInactive();
								} else if (printer_online == false) {
									showPrinterOffline();
								} else if (page == 200) {
									showMainMenu();
								}
							}
						}
					});
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
					}
				}
			}
		}).start();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (run == true) {
					try {
						String returnString = null;
						String show_printer_offline = "";
						
						try {
							returnString = kiosk2_device_status();
							for (int i = 1; i <= 3; i++) {
								if (returnString == null
										|| returnString.isEmpty()) {
									returnString = kiosk2_device_status();
								}
							}
							/*
							if (returnString == null) {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										reconnect();
									}
								});

							}
							 */
							Log.w("tag", returnString);
							

							
							/*
							 * if (returnString == null ||
							 * returnString.isEmpty()) { try { Process proc =
							 * Runtime.getRuntime() .exec(new String[] { "su",
							 * "-c", "reboot" }); proc.waitFor(); } catch
							 * (Exception ex) { ex.printStackTrace(); } }
							 */
							try {
								JSONObject object = (JSONObject) new JSONTokener(
										returnString).nextValue();
								String trigger_show_banner = object
										.getString("trigger_show_banner");
								String json_printer_ip = object
										.getString("printer_ip");
								String json_show_printer_offline = object
										.getString("show_printer_offline");
								String json_device_name = object
										.getString("device_name");
								String json_location_name = object
										.getString("location_name");
								String json_server_url = object
										.getString("server_url");
								String json_printer_status = object
										.getString("printer_status");
								String json_pin = object.getString("pin");
								String json_device_status = object
										.getString("device_status");
								String json_device_inactive_message = object
										.getString("device_inactive_message");
								String json_qrcode = object.getString("qrcode");
								String json_update = object.getString("update");

								String json_apk = object.getString("apk");
								String json_shutdown = object
										.getString("shutdown");
								String json_device_notes = object
										.getString("device_notes");

								if (json_shutdown != null) {
									if (json_shutdown.equals("Y")) {
										try {
											Process proc = Runtime
													.getRuntime()
													.exec(new String[] { "su",
															"-c", "reboot -p" });
											proc.waitFor();
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}

								if (json_update != null) {
									if (json_update.equals("Y")) {
Log.w("TAG", "test1");
										try {
											File dir = new File(
													Environment
															.getExternalStorageDirectory(),
													"Firefly");
											if (!dir.exists()) {
												dir.mkdirs();
											}
											URL url = new URL(json_apk);
											String path = Environment
													.getExternalStorageDirectory()
													+ "/Firefly/kiosk.apk";
											File file = new File(path);
											URLConnection ucon = url
													.openConnection();
											InputStream is = ucon
													.getInputStream();
											BufferedInputStream bis = new BufferedInputStream(
													is);
											ByteArrayBuffer baf = new ByteArrayBuffer(
													50);
											int current = 0;
											while ((current = bis.read()) != -1) {
												baf.append((byte) current);
											}
											FileOutputStream fos = new FileOutputStream(
													file);
											fos.write(baf.toByteArray());
											fos.close();

											// REQUIRES ROOT
											Process proc = Runtime
													.getRuntime()
													.exec(new String[] {
															"su",
															"-c",
															"pm uninstall com.metech.fireflyprintingkiosk.api10; pm install -r "
																	+ path
																	+ "; am start -n com.metech.fireflyprintingkiosk.api10/.MainActivity" }); // WAS
																																				// 79
											proc.waitFor();

										} catch (Exception ex) {
											Log.w("TAG", "" + ex.getMessage());
										}
									}
								}

								if (trigger_show_banner != null) {
									if (trigger_show_banner.equals("Y")
											&& page == 2) {
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												showMainMenu();
											}
										});
									}
								}
								if (json_device_notes != null) {
									device_notes = json_device_notes;
								}
								if (json_printer_ip != null) {
									printer_ip = json_printer_ip;
								}
								if (json_show_printer_offline != null) {
									show_printer_offline = json_show_printer_offline;
								}
								if (json_device_name != null) {
									device_name = json_device_name;
								}
								if (json_location_name != null) {
									location_name = json_location_name;
								}
								if (json_server_url != null) {
									server_url = json_server_url;
								}
								if (json_printer_status != null) {
									printer_status = json_printer_status;
								}
								if (json_pin != null) {
									pin = json_pin;
								}
								if (json_device_status != null) {
									device_status = json_device_status;
								}
								if (json_device_inactive_message != null) {
									device_inactive_message = json_device_inactive_message;
								}
								if (json_qrcode != null) {
									if (json_qrcode.equals("off")) {
										if (qrcode_flag == true) {
											qrcode_flag = false;
											if (page == 2) {
												runOnUiThread(new Runnable() {
													@Override
													public void run() {
														showMainMenu();
													}
												});
											}
										}
									}
									if (json_qrcode.equals("on")) {
										if (qrcode_flag == false) {
											qrcode_flag = true;
											if (page == 2) {
												runOnUiThread(new Runnable() {
													@Override
													public void run() {
														showMainMenu();
													}
												});
											}
										}
									}
								}

							} catch (JSONException e) {

							}

						} catch (Exception e) {

						}
						if (returnString == null || returnString.isEmpty()) {
							kiosk_online = false;
						}
						if (returnString != null && !returnString.isEmpty()) {
							kiosk_online = true;
						}
						Thread.sleep(30000);
					} catch (InterruptedException e) {
					}
				}
			}
		}).start();

		super.onResume();
	}

	public void reconnect() {
		WifiManager wifiManager = (WifiManager) this.getApplicationContext()
				.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager.isWifiEnabled() == false) {
			wifiManager.setWifiEnabled(true);
		}
		List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
		if (list != null) {
			int total = list.size();
			int wifiCount = 0;
			for (WifiConfiguration i : list) {
				if (wifiCount == wifiCounter) {
					/*
					 * Toast.makeText(getApplicationContext(), i.SSID,
					 * Toast.LENGTH_LONG).show();
					 */
					wifiManager.disconnect();
					wifiManager.enableNetwork(i.networkId, true);
					wifiManager.reconnect();
				}
				wifiCount++;
			}
			wifiCounter++;
			if (wifiCounter >= total)
				wifiCounter = 0;
		}
	}

	@Override
	public void onUserInteraction() {
		super.onUserInteraction();
		if (page == 1) {
			showMainMenu();
		}
		lastUsed = System.currentTimeMillis();
	}

	public void showOffline() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpParams httpParams = new BasicHttpParams();
					HttpConnectionParams
							.setConnectionTimeout(httpParams, 60000);
					HttpConnectionParams.setSoTimeout(httpParams, 60000);
					DefaultHttpClient client = new DefaultHttpClient(httpParams);
					HttpPost httppost = new HttpPost(URL_DEVICE_LOG);
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair("macaddr",
							macaddr));
					nameValuePairs.add(new BasicNameValuePair("action", "02"));
					nameValuePairs.add(new BasicNameValuePair("pnr", ""));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse httpResponse = client.execute(httppost);
				} catch (Exception e) {

				}
			}
		}).start();

		page = 200;
		setContentView(R.layout.error);
		TextView textView = (TextView) findViewById(R.id.errorlabel);
		textView.setText("Kiosk is currently off-line.\nPlease proceed to Firefly's check-in counter.");

	}

	int wifiCounter = 0;

	public void showDeviceInactive() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpParams httpParams = new BasicHttpParams();
					HttpConnectionParams
							.setConnectionTimeout(httpParams, 60000);
					HttpConnectionParams.setSoTimeout(httpParams, 60000);
					DefaultHttpClient client = new DefaultHttpClient(httpParams);
					HttpPost httppost = new HttpPost(URL_DEVICE_LOG);
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair("macaddr",
							macaddr));
					nameValuePairs.add(new BasicNameValuePair("action", "16"));
					nameValuePairs.add(new BasicNameValuePair("pnr", ""));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse httpResponse = client.execute(httppost);
				} catch (Exception e) {

				}
			}
		}).start();

		page = 200;
		setContentView(R.layout.error);
		TextView textView = (TextView) findViewById(R.id.errorlabel);
		textView.setText(device_inactive_message);
	}

	public void showPrinterOffline() {
		page = 200;
		setContentView(R.layout.error);
		TextView textView = (TextView) findViewById(R.id.errorlabel);
		if (epsonPrinterStatus == 17432588) {

			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						HttpParams httpParams = new BasicHttpParams();
						HttpConnectionParams.setConnectionTimeout(httpParams,
								180000);
						HttpConnectionParams.setSoTimeout(httpParams, 60000);
						DefaultHttpClient client = new DefaultHttpClient(
								httpParams);
						HttpPost httppost = new HttpPost(URL_DEVICE_LOG);
						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
								2);
						nameValuePairs.add(new BasicNameValuePair("macaddr",
								macaddr));
						nameValuePairs.add(new BasicNameValuePair("action",
								"03"));
						nameValuePairs.add(new BasicNameValuePair("pnr", ""));
						httppost.setEntity(new UrlEncodedFormEntity(
								nameValuePairs));
						HttpResponse httpResponse = client.execute(httppost);
					} catch (Exception e) {

					}
				}
			}).start();
			textView.setText("Printer is currently out of paper.\nPlease proceed to Firefly's check-in counter.");
		} else {

			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						HttpParams httpParams = new BasicHttpParams();
						HttpConnectionParams.setConnectionTimeout(httpParams,
								180000);
						HttpConnectionParams.setSoTimeout(httpParams, 60000);
						DefaultHttpClient client = new DefaultHttpClient(
								httpParams);
						HttpPost httppost = new HttpPost(URL_DEVICE_LOG);
						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
								2);
						nameValuePairs.add(new BasicNameValuePair("macaddr",
								macaddr));
						nameValuePairs.add(new BasicNameValuePair("action",
								"04"));
						nameValuePairs.add(new BasicNameValuePair("pnr", ""));
						httppost.setEntity(new UrlEncodedFormEntity(
								nameValuePairs));
						HttpResponse httpResponse = client.execute(httppost);
					} catch (Exception e) {

					}
				}
			}).start();
			textView.setText("Printer is currently off-line.\nPlease proceed to Firefly's check-in counter.");
		}
	}

	public void showBanner() {
		page = 1;
		setContentView(R.layout.banner);
		WebView mWebView = (WebView) findViewById(R.id.webview);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		mWebView.loadUrl(URL_BANNER + "?macaddr=" + macaddr);
	}

	Button buttonScanQRCode;
	Button buttonFireflyPnr;
	Button buttonCodesharePnr;

	public void showMainMenu() {
		isCodeshare = 0;

		if (kiosk_online == false) {
			showOffline();
			return;
		}
		if (device_status.equals("N")) {
			showDeviceInactive();
			return;
		}
		if (printer_online == false) {
			showPrinterOffline();
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpParams httpParams = new BasicHttpParams();
					HttpConnectionParams
							.setConnectionTimeout(httpParams, 60000);
					HttpConnectionParams.setSoTimeout(httpParams, 60000);
					DefaultHttpClient client = new DefaultHttpClient(httpParams);
					HttpPost httppost = new HttpPost(URL_DEVICE_LOG);
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair("macaddr",
							macaddr));
					nameValuePairs.add(new BasicNameValuePair("action", "05"));
					nameValuePairs.add(new BasicNameValuePair("pnr", ""));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse httpResponse = client.execute(httppost);
				} catch (Exception e) {

				}
			}
		}).start();

		page = 2;
		RelativeLayout layout = new RelativeLayout(this);
		layout.setBackgroundColor(Color.parseColor("#f26f21"));
		layout.setBackgroundResource(R.drawable.main);

		buttonFireflyPnr = new Button(this);
		// button1.setEnabled(false);
		buttonFireflyPnr.setText("Firefly Confirmation");
		buttonFireflyPnr.setTextSize(TEXT_SIZE_50);
		buttonFireflyPnr.setTextColor(Color.parseColor("#666666"));
		buttonFireflyPnr.setBackgroundResource(R.drawable.custom_button);
		buttonFireflyPnr.setOnClickListener(this);
		buttonFireflyPnr.setId(MAIN_MENU_BUTTON_ENTER_FIREFLY_PNR);
		RelativeLayout.LayoutParams button1LP = new RelativeLayout.LayoutParams(
				MEDIUM_BUTTON_WIDTH, MEDIUM_BUTTON_HEIGHT);
		button1LP.leftMargin = 100;
		button1LP.topMargin = 180;
		layout.addView(buttonFireflyPnr, button1LP);

		buttonCodesharePnr = new Button(this);
		buttonCodesharePnr
				.setText("Malaysia Airlines Confirmation (Codeshare)");
		buttonCodesharePnr.setTextSize(TEXT_SIZE_50);
		buttonCodesharePnr.setTextColor(Color.parseColor("#666666"));
		buttonCodesharePnr.setBackgroundResource(R.drawable.custom_button);
		buttonCodesharePnr.setOnClickListener(this);
		buttonCodesharePnr.setId(MAIN_MENU_BUTTON_ENTER_CODESHARE_PNR);
		RelativeLayout.LayoutParams button3LP = new RelativeLayout.LayoutParams(
				MEDIUM_BUTTON_WIDTH, MEDIUM_BUTTON_HEIGHT);
		button3LP.leftMargin = 100;
		button3LP.topMargin = 360;
		layout.addView(buttonCodesharePnr, button3LP);

		buttonScanQRCode = new Button(this);
		buttonScanQRCode.setText("Scan QR Code");
		buttonScanQRCode.setTextSize(TEXT_SIZE_50);
		buttonScanQRCode.setTextColor(Color.parseColor("#666666"));
		buttonScanQRCode.setBackgroundResource(R.drawable.custom_button);
		buttonScanQRCode.setOnClickListener(this);
		buttonScanQRCode.setId(MAIN_MENU_BUTTON_SCAN_QR_CODE);
		RelativeLayout.LayoutParams button2LP = new RelativeLayout.LayoutParams(
				MEDIUM_BUTTON_WIDTH, MEDIUM_BUTTON_HEIGHT);
		button2LP.leftMargin = 100;
		button2LP.topMargin = 540;
		layout.addView(buttonScanQRCode, button2LP);

		if (URL_KIOSK.contains("kiosk_dev")) {
			device_notes = "TEST ENVIRONMENT";
		}

		TextView textViewNotice = new TextView(this);
		textViewNotice.setText(device_notes);
		textViewNotice.setTextSize(TEXT_SIZE_40);
		textViewNotice.setTextColor(Color.parseColor("#000000"));
		textViewNotice.setGravity(Gravity.CENTER);
		RelativeLayout.LayoutParams textViewNoticeLP = new RelativeLayout.LayoutParams(
				1280, 100);
		textViewNoticeLP.leftMargin = 0;
		textViewNoticeLP.topMargin = 700;
		layout.addView(textViewNotice, textViewNoticeLP);

		if (qrcode_flag == false) {
			buttonScanQRCode.setVisibility(View.INVISIBLE);
		}

		setContentView(layout);

	}

	public void showTaskMenu() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpParams httpParams = new BasicHttpParams();
					HttpConnectionParams
							.setConnectionTimeout(httpParams, 60000);
					HttpConnectionParams.setSoTimeout(httpParams, 60000);
					DefaultHttpClient client = new DefaultHttpClient(httpParams);
					HttpPost httppost = new HttpPost(URL_DEVICE_LOG);
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair("macaddr",
							macaddr));
					nameValuePairs.add(new BasicNameValuePair("action", "08"));
					nameValuePairs.add(new BasicNameValuePair("pnr", pnr));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse httpResponse = client.execute(httppost);
				} catch (Exception e) {

				}
			}
		}).start();

		boolean b1 = false;
		boolean b2 = false;

		if (jsonCheckPNR != null) {
			try {
				passengerNumber.clear();
				JSONObject object = (JSONObject) new JSONTokener(jsonCheckPNR)
						.nextValue();
				String status = object.getString("status");
				String message = object.getString("message");
				if (status.equals("ok")) {
					page = 6;
					JSONArray jsonArray = object.getJSONArray("journey");
					JSONArray jsonArray2 = object.getJSONArray("passenger");
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject object2 = jsonArray.getJSONObject(i);
						if (object2.getString("DepartureStation").equals(
								departureStationCode)
								&& object2.getString("ArrivalStation").equals(
										arrivalStationCode)
								&& object2.getString("STD").equals(STD)) {
							String CheckedIn = object2.getString("CheckedIn");
							if (CheckedIn.contains("0")) {
								b1 = true;
							}
							if (CheckedIn.contains("1")) {
								b2 = true;
							}
						}
					}
				}
			} catch (Exception e) {

			}
		}

		page = 2;
		RelativeLayout layout = new RelativeLayout(this);
		layout.setBackgroundColor(Color.parseColor("#f26f21"));
		layout.setBackgroundResource(R.drawable.main);
		Button button1 = new Button(this);
		button1.setText("Check In");
		button1.setTextSize(TEXT_SIZE_50);
		button1.setTextColor(Color.parseColor("#666666"));
		button1.setBackgroundResource(R.drawable.custom_button);
		button1.setOnClickListener(this);
		button1.setId(TASK_MENU_BUTTON_CHECK_IN);
		RelativeLayout.LayoutParams button1LP = new RelativeLayout.LayoutParams(
				BIG_BUTTON_WIDTH, BIG_BUTTON_HEIGHT);
		button1LP.leftMargin = 100;
		button1LP.topMargin = 200;
		if (b1 == false) {
			button1.setEnabled(false);
			button1.setBackgroundColor(Color.GRAY);
		}
		layout.addView(button1, button1LP);
		Button button2 = new Button(this);
		button2.setText("Reprint Boarding Pass");
		button2.setTextSize(TEXT_SIZE_50);
		button2.setTextColor(Color.parseColor("#666666"));
		button2.setBackgroundResource(R.drawable.custom_button);
		button2.setOnClickListener(this);
		button2.setId(TASK_MENU_BUTTON_REPRINT_BOARDING_PASS);
		RelativeLayout.LayoutParams button2LP = new RelativeLayout.LayoutParams(
				BIG_BUTTON_WIDTH, BIG_BUTTON_HEIGHT);
		button2LP.leftMargin = 100;
		button2LP.topMargin = 420;
		if (b2 == false) {
			button2.setEnabled(false);
			button2.setBackgroundColor(Color.GRAY);
		}
		layout.addView(button2, button2LP);
		Button button3 = new Button(this);
		button3.setText("Back");
		button3.setTextSize(TEXT_SIZE_40);
		button3.setTextColor(Color.parseColor("#666666"));
		button3.setBackgroundResource(R.drawable.custom_button);
		button3.setOnClickListener(this);
		button3.setId(TASK_MENU_BUTTON_CANCEL);
		RelativeLayout.LayoutParams button3LP = new RelativeLayout.LayoutParams(
				300, 70);
		button3LP.leftMargin = 640 - 150;
		button3LP.topMargin = SMALL_BUTTON_BOTTOM_Y;
		layout.addView(button3, button3LP);
		setContentView(layout);
	}

	TextView textViewPNR;
	TextView textViewInstruction;

	public void showEnterPNR() {
		// XXX 2014-06-20
		// XXX alert terms here
		// Alert dialog on non-check in category to be prompted after selecting
		// method of check in. eg 6PNR > alert dialog > keyboard
		MainActivity.alert(this, readAssets("txtconfirm.txt"), true);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpParams httpParams = new BasicHttpParams();
					HttpConnectionParams
							.setConnectionTimeout(httpParams, 60000);
					HttpConnectionParams.setSoTimeout(httpParams, 60000);
					DefaultHttpClient client = new DefaultHttpClient(httpParams);
					HttpPost httppost = new HttpPost(URL_DEVICE_LOG);
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair("macaddr",
							macaddr));
					nameValuePairs.add(new BasicNameValuePair("action", "06"));
					nameValuePairs.add(new BasicNameValuePair("pnr", ""));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse httpResponse = client.execute(httppost);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

		page = 3;
		RelativeLayout layout = new RelativeLayout(this);
		layout.setBackgroundColor(Color.parseColor("#f26f21"));
		layout.setBackgroundResource(R.drawable.main);
		textViewInstruction = new TextView(this);
		textViewInstruction.setText("Please enter your confirmation number:");
		textViewInstruction.setTextSize(TEXT_SIZE_40);
		textViewInstruction.setTextColor(Color.parseColor("#000000"));
		textViewInstruction.setGravity(Gravity.CENTER);
		RelativeLayout.LayoutParams textViewInstructionLP = new RelativeLayout.LayoutParams(
				width, 70);
		textViewInstructionLP.leftMargin = 0;
		textViewInstructionLP.topMargin = 130;
		layout.addView(textViewInstruction, textViewInstructionLP);
		textViewPNR = new TextView(this);
		textViewPNR.setText("");
		textViewPNR.setTextSize(TEXT_SIZE_40);
		textViewPNR.setTextColor(Color.parseColor("#000000"));
		textViewPNR.setBackgroundColor(Color.parseColor("#ffffff"));
		textViewPNR.setGravity(Gravity.CENTER);
		RelativeLayout.LayoutParams textViewPNRLP = new RelativeLayout.LayoutParams(
				width / 2, 70);
		textViewPNRLP.leftMargin = width / 4;
		textViewPNRLP.topMargin = 210;
		layout.addView(textViewPNR, textViewPNRLP);
		Button buttonBackspace = new Button(this);
		buttonBackspace.setBackgroundResource(R.drawable.backspace);
		buttonBackspace.setOnClickListener(this);
		buttonBackspace.setId(ENTER_PNR_BUTTON_BACKSPACE);
		RelativeLayout.LayoutParams buttonBackspaceLP = new RelativeLayout.LayoutParams(
				65, 52);
		buttonBackspaceLP.leftMargin = 1000;
		buttonBackspaceLP.topMargin = 220;
		layout.addView(buttonBackspace, buttonBackspaceLP);
		Button buttonCancel = new Button(this);
		buttonCancel.setText("Cancel");
		buttonCancel.setTextSize(TEXT_SIZE_40);
		buttonCancel.setTextColor(Color.parseColor("#666666"));
		buttonCancel.setBackgroundResource(R.drawable.custom_button);
		buttonCancel.setOnClickListener(this);
		buttonCancel.setId(ENTER_PNR_BUTTON_CANCEL);
		RelativeLayout.LayoutParams buttonCancelLP = new RelativeLayout.LayoutParams(
				SMALL_BUTTON_WIDTH, SMALL_BUTTON_HEIGHT);
		buttonCancelLP.leftMargin = (width / 2) - 300 - 10;
		buttonCancelLP.topMargin = 620 - 80 - 80 - 80 - 80;
		layout.addView(buttonCancel, buttonCancelLP);
		Button buttonNext = new Button(this);
		buttonNext.setText("Next");
		buttonNext.setTextSize(TEXT_SIZE_40);
		buttonNext.setTextColor(Color.parseColor("#666666"));
		buttonNext.setBackgroundResource(R.drawable.custom_button);
		buttonNext.setOnClickListener(this);
		buttonNext.setId(ENTER_PNR_BUTTON_NEXT);
		RelativeLayout.LayoutParams buttonNextlLP = new RelativeLayout.LayoutParams(
				SMALL_BUTTON_WIDTH, SMALL_BUTTON_HEIGHT);
		buttonNextlLP.leftMargin = (width / 2) + 10;
		buttonNextlLP.topMargin = 620 - 80 - 80 - 80 - 80;
		layout.addView(buttonNext, buttonNextlLP);
		for (int i = 0; i < 10; i++) {
			Button button = new Button(this);
			button.setText("" + i);
			button.setTextSize(TEXT_SIZE_40);
			button.setTextColor(Color.parseColor("#666666"));
			button.setBackgroundResource(R.drawable.custom_button);
			button.setOnClickListener(this);
			button.setTag("" + i);
			button.setId(ENTER_PNR_BUTTON_ALPHANUMERIC);
			RelativeLayout.LayoutParams buttonLP = new RelativeLayout.LayoutParams(
					((width - 100) / 10) - 4, 70);
			buttonLP.leftMargin = 50 + (0 + ((width - 100) / 10) * i) + 2;
			buttonLP.topMargin = 640 - 80 - 80 - 80;
			layout.addView(button, buttonLP);
		}
		String QWERTYUIOP = "QWERTYUIOP";
		for (int i = 0; i < 10; i++) {
			Button button = new Button(this);
			button.setText(QWERTYUIOP.substring(i, i + 1));
			button.setTextSize(TEXT_SIZE_40);
			button.setTextColor(Color.parseColor("#666666"));
			button.setBackgroundResource(R.drawable.custom_button);
			button.setOnClickListener(this);
			button.setTag(QWERTYUIOP.substring(i, i + 1));
			button.setId(ENTER_PNR_BUTTON_ALPHANUMERIC);
			RelativeLayout.LayoutParams buttonLP = new RelativeLayout.LayoutParams(
					((width - 100) / 10) - 4, 70);
			buttonLP.leftMargin = 50 + (0 + ((width - 100) / 10) * i) + 2;
			buttonLP.topMargin = 640 - 80 - 80;
			layout.addView(button, buttonLP);
		}
		String ASDFGHJKL = "ASDFGHJKL";
		for (int i = 0; i < 9; i++) {
			Button button = new Button(this);
			button.setText(ASDFGHJKL.substring(i, i + 1));
			button.setTextSize(TEXT_SIZE_40);
			button.setTextColor(Color.parseColor("#666666"));
			button.setBackgroundResource(R.drawable.custom_button);
			button.setOnClickListener(this);
			button.setTag(ASDFGHJKL.substring(i, i + 1));
			button.setId(ENTER_PNR_BUTTON_ALPHANUMERIC);
			RelativeLayout.LayoutParams buttonLP = new RelativeLayout.LayoutParams(
					((width - 100) / 10) - 4, 70);
			buttonLP.leftMargin = 35 + 50 + (0 + ((width - 100) / 10) * i) + 2;
			buttonLP.topMargin = 640 - 80;
			layout.addView(button, buttonLP);
		}
		String ZXCVBNM = "ZXCVBNM";
		for (int i = 0; i < 7; i++) {
			Button button = new Button(this);
			button.setText(ZXCVBNM.substring(i, i + 1));
			button.setTextSize(TEXT_SIZE_40);
			button.setTextColor(Color.parseColor("#666666"));
			button.setBackgroundResource(R.drawable.custom_button);
			button.setOnClickListener(this);
			button.setTag(ZXCVBNM.substring(i, i + 1));
			button.setId(ENTER_PNR_BUTTON_ALPHANUMERIC);
			RelativeLayout.LayoutParams buttonLP = new RelativeLayout.LayoutParams(
					((width - 100) / 10) - 4, 70);
			buttonLP.leftMargin = 35 + 35 + 35 + 50
					+ (0 + ((width - 100) / 10) * i) + 2;
			buttonLP.topMargin = 640;
			layout.addView(button, buttonLP);
		}
		setContentView(layout);
	}

	Button[] buttons = new Button[10];
	CheckBox[] passengerCheckBox = new CheckBox[99];
	Button passengerListbuttonNextDisable;
	Button passengerListbuttonNextEnable;

	public void showPassengerList() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpParams httpParams = new BasicHttpParams();
					HttpConnectionParams
							.setConnectionTimeout(httpParams, 60000);
					HttpConnectionParams.setSoTimeout(httpParams, 60000);
					DefaultHttpClient client = new DefaultHttpClient(httpParams);
					HttpPost httppost = new HttpPost(URL_DEVICE_LOG);
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair("macaddr",
							macaddr));
					nameValuePairs.add(new BasicNameValuePair("action", "09"));
					nameValuePairs.add(new BasicNameValuePair("pnr", pnr));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse httpResponse = client.execute(httppost);
				} catch (Exception e) {

				}
			}
		}).start();

		page = 6;
		if (jsonCheckPNR != null) {
			try {
				passengerNumber.clear();
				JSONObject object = (JSONObject) new JSONTokener(jsonCheckPNR)
						.nextValue();
				String status = object.getString("status");
				String message = object.getString("message");
				if (status.equals("ok")) {
					page = 6;
					JSONArray jsonArray = object.getJSONArray("journey");
					JSONArray jsonArray2 = object.getJSONArray("passenger");
					RelativeLayout layout = new RelativeLayout(this);
					layout.setBackgroundColor(Color.parseColor("#f26f21"));
					layout.setBackgroundResource(R.drawable.main);
					TextView textView = new TextView(this);
					if (action == 1) {
						textView.setText("Select passenger(s) to check in");
					} else {
						textView.setText("Select passenger(s) to print boarding pass");
					}
					textView.setTextSize(TEXT_SIZE_40);
					textView.setTextColor(Color.parseColor("#000000"));
					textView.setGravity(Gravity.CENTER);
					RelativeLayout.LayoutParams textViewLP = new RelativeLayout.LayoutParams(
							width, 70);
					textViewLP.leftMargin = 0;
					textViewLP.topMargin = 130;
					layout.addView(textView, textViewLP);

					ScrollView sv = new ScrollView(this);
					sv.setId(PASSENGER_LIST_SCROLLVIEW);
					RelativeLayout layout2 = new RelativeLayout(this);
					sv.addView(layout2);

					RelativeLayout.LayoutParams params10 = new RelativeLayout.LayoutParams(
							width, 420);
					params10.leftMargin = 0;
					params10.topMargin = 200;
					layout.addView(sv, params10);
					String CheckedIn = "";
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject object2 = jsonArray.getJSONObject(i);
						if (object2.getString("DepartureStation").equals(
								departureStationCode)
								&& object2.getString("ArrivalStation").equals(
										arrivalStationCode)
								&& object2.getString("STD").equals(STD)) {
							CheckedIn = object2.getString("CheckedIn");
						}
					}

					for (int i = 0; i < passengerCheckBox.length; i++) {
						passengerCheckBox[i] = null;
					}

					int row = 0;
					for (int i = 0; i < jsonArray2.length(); i++) {
						if (action == 1) {
							if (CheckedIn.charAt(i) == '0') {
								JSONObject object2 = jsonArray2
										.getJSONObject(i);
								String FirstName = object2
										.getString("FirstName");
								String LastName = object2.getString("LastName");

								Button btnPassengerName = new Button(this);
								btnPassengerName
										.setBackgroundResource(R.drawable.custom_button);
								btnPassengerName
										.setId(PASSENGER_LIST_BUTTON_NAME);
								btnPassengerName.setTag(i);
								btnPassengerName.setOnClickListener(this);
								btnPassengerName.setTextSize(TEXT_SIZE_50);
								btnPassengerName.setText(FirstName + " / "
										+ LastName);
								btnPassengerName.setGravity(Gravity.LEFT
										| Gravity.CENTER_VERTICAL);
								btnPassengerName.setPadding(50, 0, 0, 0);
								RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(
										1100, 120);
								params1.leftMargin = 40;
								params1.topMargin = 0 + row * 130;
								layout2.addView(btnPassengerName, params1);

								passengerCheckBox[i] = new CheckBox(this);
								passengerCheckBox[i].setTextSize(TEXT_SIZE_50);
								passengerCheckBox[i].setTextColor(Color
										.parseColor("#000000"));
								passengerCheckBox[i].setTag(i);
								passengerCheckBox[i].setText("");
								passengerCheckBox[i]
										.setId(PASSENGER_LIST_CHECKBOX);
								passengerCheckBox[i].setOnClickListener(this);
								passengerCheckBox[i]
										.setButtonDrawable(R.drawable.checkbox);
								RelativeLayout.LayoutParams params9 = new RelativeLayout.LayoutParams(
										width - 20, -2);
								params9.leftMargin = 20;
								params9.topMargin = 25 + row * 130;
								params9.addRule(RelativeLayout.RIGHT_OF,
										PASSENGER_LIST_BUTTON_NAME);
								layout2.addView(passengerCheckBox[i], params9);
								row++;
							}
						}
						if (action == 2) {
							if (CheckedIn.charAt(i) == '1') {
								JSONObject object2 = jsonArray2
										.getJSONObject(i);
								String FirstName = object2
										.getString("FirstName");
								String LastName = object2.getString("LastName");

								Button btnPassengerName = new Button(this);
								btnPassengerName
										.setBackgroundResource(R.drawable.custom_button);
								btnPassengerName
										.setId(PASSENGER_LIST_BUTTON_NAME);
								btnPassengerName.setTag(i);
								btnPassengerName.setOnClickListener(this);
								btnPassengerName.setTextSize(TEXT_SIZE_50);
								btnPassengerName.setText(FirstName + " / "
										+ LastName);
								btnPassengerName.setGravity(Gravity.LEFT
										| Gravity.CENTER_VERTICAL);
								btnPassengerName.setPadding(50, 0, 0, 0);
								RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(
										1100, 120);
								params1.leftMargin = 40;
								params1.topMargin = 0 + row * 130;
								layout2.addView(btnPassengerName, params1);

								passengerCheckBox[i] = new CheckBox(this);
								passengerCheckBox[i].setTextSize(TEXT_SIZE_50);
								passengerCheckBox[i].setTextColor(Color
										.parseColor("#000000"));
								passengerCheckBox[i].setTag(i);
								passengerCheckBox[i].setText("");
								passengerCheckBox[i]
										.setId(PASSENGER_LIST_CHECKBOX);
								passengerCheckBox[i].setOnClickListener(this);
								passengerCheckBox[i]
										.setButtonDrawable(R.drawable.checkbox);
								RelativeLayout.LayoutParams params9 = new RelativeLayout.LayoutParams(
										width - 20, -2);
								params9.leftMargin = 20;
								params9.topMargin = 25 + row * 130;
								params9.addRule(RelativeLayout.RIGHT_OF,
										PASSENGER_LIST_BUTTON_NAME);
								layout2.addView(passengerCheckBox[i], params9);
								row++;
							}
						}
					}

					Button buttonCancel = new Button(this);
					buttonCancel.setText("Back");
					buttonCancel.setTextSize(TEXT_SIZE_40);
					buttonCancel.setTextColor(Color.parseColor("#666666"));
					buttonCancel
							.setBackgroundResource(R.drawable.custom_button);
					buttonCancel.setId(PASSENGER_LIST_BUTTON_CANCEL);
					buttonCancel.setOnClickListener(this);
					RelativeLayout.LayoutParams buttonCancelLP = new RelativeLayout.LayoutParams(
							SMALL_BUTTON_WIDTH, SMALL_BUTTON_HEIGHT);
					buttonCancelLP.leftMargin = (width / 2) - 300 - 10;
					buttonCancelLP.topMargin = SMALL_BUTTON_BOTTOM_Y;
					layout.addView(buttonCancel, buttonCancelLP);

					passengerListbuttonNextDisable = new Button(this);
					passengerListbuttonNextDisable.setText("Next");
					passengerListbuttonNextDisable.setTextSize(TEXT_SIZE_40);
					passengerListbuttonNextDisable.setTextColor(Color
							.parseColor("#666666"));
					passengerListbuttonNextDisable
							.setBackgroundResource(R.drawable.custom_button);
					passengerListbuttonNextDisable
							.setBackgroundColor(Color.GRAY);
					RelativeLayout.LayoutParams passengerListbuttonNextDisableLP = new RelativeLayout.LayoutParams(
							SMALL_BUTTON_WIDTH, SMALL_BUTTON_HEIGHT);
					passengerListbuttonNextDisableLP.leftMargin = (width / 2) + 10;
					passengerListbuttonNextDisableLP.topMargin = SMALL_BUTTON_BOTTOM_Y;
					layout.addView(passengerListbuttonNextDisable,
							passengerListbuttonNextDisableLP);

					passengerListbuttonNextEnable = new Button(this);
					passengerListbuttonNextEnable.setText("Next");
					passengerListbuttonNextEnable.setTextSize(TEXT_SIZE_40);
					passengerListbuttonNextEnable.setTextColor(Color
							.parseColor("#666666"));
					passengerListbuttonNextEnable
							.setBackgroundResource(R.drawable.custom_button);
					passengerListbuttonNextEnable
							.setId(PASSENGER_LIST_BUTTON_NEXT);
					passengerListbuttonNextEnable.setOnClickListener(this);
					RelativeLayout.LayoutParams passengerListbuttonNextEnableLP = new RelativeLayout.LayoutParams(
							SMALL_BUTTON_WIDTH, SMALL_BUTTON_HEIGHT);
					passengerListbuttonNextEnableLP.leftMargin = (width / 2) + 10;
					passengerListbuttonNextEnableLP.topMargin = SMALL_BUTTON_BOTTOM_Y;
					layout.addView(passengerListbuttonNextEnable,
							passengerListbuttonNextEnableLP);

					passengerListbuttonNextDisable.setVisibility(View.VISIBLE);
					passengerListbuttonNextEnable.setVisibility(View.GONE);

					if (row == 0) {
						if (action == 1) {
							Toast.makeText(getApplicationContext(),
									"No passenger available to check in.",
									Toast.LENGTH_LONG).show();
						}
						if (action == 2) {
							Toast.makeText(
									getApplicationContext(),
									"No passenger available to print boarding pass.",
									Toast.LENGTH_LONG).show();
						}
					} else {
						setContentView(layout);
					}
				} else {
					setContentView(R.layout.error);
					TextView textView = (TextView) findViewById(R.id.errorlabel);
					textView.setText(message);

					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								Thread.sleep(5000);
							} catch (InterruptedException e) {
							}
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									pd.hide();
									showMainMenu();
								}
							});
						}
					}).start();
				}
			} catch (JSONException e) {
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), "Connection error.",
						Toast.LENGTH_LONG).show();
			}
		}

	}

	JSONArray boardingpass = null;

	public void showPrintBoardingPass() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpParams httpParams = new BasicHttpParams();
					HttpConnectionParams
							.setConnectionTimeout(httpParams, 60000);
					HttpConnectionParams.setSoTimeout(httpParams, 60000);
					DefaultHttpClient client = new DefaultHttpClient(httpParams);
					HttpPost httppost = new HttpPost(URL_DEVICE_LOG);
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair("macaddr",
							macaddr));
					nameValuePairs.add(new BasicNameValuePair("action", "11"));
					nameValuePairs.add(new BasicNameValuePair("pnr", pnr));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse httpResponse = client.execute(httppost);
				} catch (Exception e) {

				}
			}
		}).start();

		page = 8;
		if (jsonPrintBoardingPass != null) {
			try {
				JSONObject object = (JSONObject) new JSONTokener(
						jsonPrintBoardingPass).nextValue();
				String status = object.getString("status");
				String message = object.getString("message");
				if (status.equals("ok")) {
					boardingpass = object.getJSONArray("boardingpass");
					setContentView(R.layout.printing);

					new Thread(new Runnable() {
						@Override
						public void run() {
							epsonPrinterTask = 1;
							epsonPrinterData = boardingpass;
							epsonPrinterTask();
							try {
								Thread.sleep(5000);
							} catch (InterruptedException e) {
							}
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									pd.hide();
									showMainMenu();
								}
							});
						}
					}).start();
				} else if (status.equals("error")) {
					setContentView(R.layout.error);
					TextView textView = (TextView) findViewById(R.id.errorlabel);
					textView.setText(message);

					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								Thread.sleep(5000);
							} catch (InterruptedException e) {
							}
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									pd.hide();
									showMainMenu();
								}
							});
						}
					}).start();
				} else {

					setContentView(R.layout.invalid_code);
					new Thread(new Runnable() {
						@Override
						public void run() {

							try {
								Thread.sleep(5000);
							} catch (InterruptedException e) {

							}
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									pd.hide();
									showMainMenu();
								}
							});

						}
					}).start();
				}
			} catch (JSONException e) {
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), "Connection error.",
						Toast.LENGTH_LONG).show();
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
						}
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								pd.hide();
								showMainMenu();
							}
						});
					}
				}).start();
			}
		}
	}

	public void showSector() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpParams httpParams = new BasicHttpParams();
					HttpConnectionParams
							.setConnectionTimeout(httpParams, 60000);
					HttpConnectionParams.setSoTimeout(httpParams, 60000);
					DefaultHttpClient client = new DefaultHttpClient(httpParams);
					HttpPost httppost = new HttpPost(URL_DEVICE_LOG);
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair("macaddr",
							macaddr));
					nameValuePairs.add(new BasicNameValuePair("action", "07"));
					nameValuePairs.add(new BasicNameValuePair("pnr", pnr));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse httpResponse = client.execute(httppost);
				} catch (Exception e) {

				}
			}
		}).start();

		page = 4;
		if (jsonCheckPNR != null) {
			try {
				JSONObject object = (JSONObject) new JSONTokener(jsonCheckPNR)
						.nextValue();
				String status = object.getString("status");
				String message = object.getString("message");

				if (status.equals("ok")) {
					agreement = object.getString("agreement");
					dangerous_goods = object.getString("dangerous_goods");
					page = 6;
					JSONArray jsonArray = object.getJSONArray("journey");
					JSONArray jsonArray2 = object.getJSONArray("passenger");
					RelativeLayout layout = new RelativeLayout(this);
					layout.setBackgroundColor(Color.parseColor("#f26f21"));
					layout.setBackgroundResource(R.drawable.main);
					TextView textView = new TextView(this);
					textView.setText("Select your flight");
					textView.setTextSize(TEXT_SIZE_40);
					textView.setTextColor(Color.parseColor("#000000"));
					textView.setGravity(Gravity.CENTER);
					RelativeLayout.LayoutParams textViewLP = new RelativeLayout.LayoutParams(
							width, 70);
					textViewLP.leftMargin = 0;
					textViewLP.topMargin = 130;
					layout.addView(textView, textViewLP);

					TextView textViewFlight = new TextView(this);
					textViewFlight.setText("Flight");
					textViewFlight.setTextSize(TEXT_SIZE_36);
					textViewFlight.setTextColor(Color.parseColor("#000000"));
					RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
							200, 75);
					params.leftMargin = 40 + 20;
					params.topMargin = 200;
					layout.addView(textViewFlight, params);

					TextView textViewDepart = new TextView(this);
					textViewDepart.setText("Depart");
					textViewDepart.setTextSize(TEXT_SIZE_36);
					textViewDepart.setTextColor(Color.parseColor("#000000"));
					RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
							400, 75);
					params2.leftMargin = 40 + 220;
					params2.topMargin = 200;
					layout.addView(textViewDepart, params2);

					TextView textViewArrive = new TextView(this);
					textViewArrive.setText("Arrive");
					textViewArrive.setTextSize(TEXT_SIZE_36);
					textViewArrive.setTextColor(Color.parseColor("#000000"));
					RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(
							400, 75);
					params3.leftMargin = 40 + 600;
					params3.topMargin = 200;
					layout.addView(textViewArrive, params3);

					ScrollView sv = new ScrollView(this);
					RelativeLayout layout2 = new RelativeLayout(this);
					sv.addView(layout2);

					RelativeLayout.LayoutParams params10 = new RelativeLayout.LayoutParams(
							width, 400);
					params10.leftMargin = 0;
					params10.topMargin = 250;
					layout.addView(sv, params10);

					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject object2 = jsonArray.getJSONObject(i);
						String CarrierCode = object2.getString("CarrierCode");
						String FlightNumber = object2.getString("FlightNumber");
						String DepartureStation = object2
								.getString("DepartureStation");
						String ArrivalStation = object2
								.getString("ArrivalStation");
						String DepartureStationName = object2
								.getString("DepartureStationName");
						String ArrivalStationName = object2
								.getString("ArrivalStationName");
						String DepartureDate = object2
								.getString("DepartureDate");
						String ArrivalDate = object2.getString("ArrivalDate");
						String DepartureTime = object2
								.getString("DepartureTime");
						String ArrivalTime = object2.getString("ArrivalTime");
						String STD = object2.getString("STD");

						CheckBox checkBox = new CheckBox(this);
						checkBox.setTag(i);
						checkBox.setId(SECTOR_BUTTON_FLIGHT);
						checkBox.setOnClickListener(this);
						checkBox.setButtonDrawable(R.drawable.checkbox);
						checkBox.setTag(DepartureStation + ArrivalStation + STD);
						RelativeLayout.LayoutParams params91 = new RelativeLayout.LayoutParams(
								80, 80);
						params91.leftMargin = 1180;
						params91.topMargin = 20 + i * 130;
						layout2.addView(checkBox, params91);

						Button buttonFlights = new Button(this);
						buttonFlights
								.setBackgroundResource(R.drawable.custom_button);
						buttonFlights.setId(SECTOR_BUTTON_FLIGHT);
						buttonFlights.setOnClickListener(this);
						buttonFlights.setTag(DepartureStation + ArrivalStation
								+ STD);
						RelativeLayout.LayoutParams params9 = new RelativeLayout.LayoutParams(
								1100, 120);
						params9.leftMargin = 40;
						params9.topMargin = 0 + i * 130;
						layout2.addView(buttonFlights, params9);

						TextView textViewFlights = new TextView(this);
						textViewFlights.setText(CarrierCode + " "
								+ FlightNumber);
						textViewFlights.setTextSize(TEXT_SIZE_36);
						textViewFlights.setTextColor(Color
								.parseColor("#000000"));
						RelativeLayout.LayoutParams params4 = new RelativeLayout.LayoutParams(
								200, 75);
						params4.leftMargin = 40 + 20;
						params4.topMargin = 0 + i * 130 + 10;
						layout2.addView(textViewFlights, params4);

						TextView textViewDepartureStations = new TextView(this);
						textViewDepartureStations.setText(DepartureStationName);
						textViewDepartureStations.setTextSize(TEXT_SIZE_36);
						textViewDepartureStations.setTextColor(Color
								.parseColor("#000000"));
						RelativeLayout.LayoutParams params5 = new RelativeLayout.LayoutParams(
								400, 75);
						params5.leftMargin = 40 + 220;
						params5.topMargin = 0 + i * 130 + 10;
						layout2.addView(textViewDepartureStations, params5);

						TextView textViewDepartureDates = new TextView(this);
						textViewDepartureDates.setText(DepartureDate + " - "
								+ DepartureTime);
						textViewDepartureDates.setTextSize(TEXT_SIZE_36);
						textViewDepartureDates.setTextColor(Color
								.parseColor("#000000"));
						RelativeLayout.LayoutParams params6 = new RelativeLayout.LayoutParams(
								400, 75);
						params6.leftMargin = 40 + 220;
						params6.topMargin = 0 + i * 130 + 55 + 10;
						layout2.addView(textViewDepartureDates, params6);

						TextView textViewArrivalStations = new TextView(this);
						textViewArrivalStations.setText(ArrivalStationName);
						textViewArrivalStations.setTextSize(TEXT_SIZE_36);
						textViewArrivalStations.setTextColor(Color
								.parseColor("#000000"));
						RelativeLayout.LayoutParams params7 = new RelativeLayout.LayoutParams(
								400, 75);
						params7.leftMargin = 40 + 600;
						params7.topMargin = 0 + i * 130 + 10;
						layout2.addView(textViewArrivalStations, params7);

						TextView textViewArrivalDates = new TextView(this);
						textViewArrivalDates.setText(ArrivalDate + " - "
								+ ArrivalTime);
						textViewArrivalDates.setTextSize(TEXT_SIZE_36);
						textViewArrivalDates.setTextColor(Color
								.parseColor("#000000"));
						RelativeLayout.LayoutParams params8 = new RelativeLayout.LayoutParams(
								400, 75);
						params8.leftMargin = 40 + 600;
						params8.topMargin = 0 + i * 130 + 55 + 10;
						layout2.addView(textViewArrivalDates, params8);
					}
					Button buttonCancel = new Button(this);
					buttonCancel.setText("Cancel");
					buttonCancel.setTextSize(TEXT_SIZE_40);
					buttonCancel.setTextColor(Color.parseColor("#666666"));
					buttonCancel
							.setBackgroundResource(R.drawable.custom_button);
					buttonCancel.setId(SECTOR_BUTTON_CANCEL);
					buttonCancel.setOnClickListener(this);
					RelativeLayout.LayoutParams buttonCancelLP = new RelativeLayout.LayoutParams(
							SMALL_BUTTON_WIDTH, SMALL_BUTTON_HEIGHT);
					buttonCancelLP.leftMargin = 640 - 150;
					buttonCancelLP.topMargin = SMALL_BUTTON_BOTTOM_Y;
					layout.addView(buttonCancel, buttonCancelLP);
					setContentView(layout);
				} else {
					setContentView(R.layout.error);
					TextView textView = (TextView) findViewById(R.id.errorlabel);
					textView.setText(message);

					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								Thread.sleep(5000);
							} catch (InterruptedException e) {
							}
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									pd.hide();
									showMainMenu();
								}
							});
						}
					}).start();
				}
			} catch (JSONException e) {
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), "Connection error.",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	String agreement = "";
	String dangerous_goods = "";

	public void showAgreement() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpParams httpParams = new BasicHttpParams();
					HttpConnectionParams
							.setConnectionTimeout(httpParams, 60000);
					HttpConnectionParams.setSoTimeout(httpParams, 60000);
					DefaultHttpClient client = new DefaultHttpClient(httpParams);
					HttpPost httppost = new HttpPost(URL_DEVICE_LOG);
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair("macaddr",
							macaddr));
					nameValuePairs.add(new BasicNameValuePair("action", "10"));
					nameValuePairs.add(new BasicNameValuePair("pnr", pnr));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse httpResponse = client.execute(httppost);
				} catch (Exception e) {

				}
			}
		}).start();

		agreementChecked = false;
		RelativeLayout layout = new RelativeLayout(this);
		layout.setBackgroundColor(Color.parseColor("#f26f21"));
		layout.setBackgroundResource(R.drawable.main);

		WebView webView = new WebView(this);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		webView.loadData(agreement, "text/html", "UTF-8");

		webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		webView.setScrollbarFadingEnabled(false);

		RelativeLayout.LayoutParams webViewLP = new RelativeLayout.LayoutParams(
				width, 450);
		webViewLP.leftMargin = 0;
		webViewLP.topMargin = 120;
		layout.addView(webView, webViewLP);

		TextView textView2 = new TextView(this);
		textView2
				.setText("I have read and understand these important information.");
		textView2.setTextSize(TEXT_SIZE_36);
		textView2.setTextColor(Color.parseColor("#000000"));
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				width, 50);
		params.leftMargin = 20;
		params.topMargin = 580;
		layout.addView(textView2, params);

		// CheckBox checkBox = new CheckBox(this);
		// checkBox.setText("Yes, I have read and understand these important information.");
		// checkBox.setTextSize(TEXT_SIZE_36);
		// checkBox.setTextColor(Color.parseColor("#000000"));
		// checkBox.setOnClickListener(this);
		// checkBox.setId(AGREEMENT_CHECKBOX);
		// RelativeLayout.LayoutParams checkBoxLP = new
		// RelativeLayout.LayoutParams(
		// width, 50);
		// checkBoxLP.leftMargin = 20;
		// checkBoxLP.topMargin = 580;
		// layout.addView(checkBox, checkBoxLP);

		Button buttonCancel = new Button(this);
		buttonCancel.setText("Disagree");
		buttonCancel.setTextSize(TEXT_SIZE_40);
		buttonCancel.setTextColor(Color.parseColor("#666666"));
		buttonCancel.setBackgroundResource(R.drawable.custom_button);
		buttonCancel.setId(AGREEMENT_BUTTON_CANCEL);
		buttonCancel.setOnClickListener(this);
		RelativeLayout.LayoutParams buttonCancelLP = new RelativeLayout.LayoutParams(
				500, 80);
		buttonCancelLP.leftMargin = (width / 2) - 500 - 10;
		buttonCancelLP.topMargin = SMALL_BUTTON_BOTTOM_Y;
		layout.addView(buttonCancel, buttonCancelLP);

		Button buttonNext = new Button(this);
		buttonNext.setText("I Agree, Next");
		buttonNext.setTextSize(TEXT_SIZE_40);
		buttonNext.setTextColor(Color.parseColor("#666666"));
		buttonNext.setBackgroundResource(R.drawable.custom_button);
		buttonNext.setId(AGREEMENT_BUTTON_NEXT);
		buttonNext.setOnClickListener(this);
		RelativeLayout.LayoutParams buttonNextLP = new RelativeLayout.LayoutParams(
				500, 80);
		buttonNextLP.leftMargin = (width / 2) + 10;
		buttonNextLP.topMargin = SMALL_BUTTON_BOTTOM_Y;
		layout.addView(buttonNext, buttonNextLP);

		setContentView(layout);
	}

	public void showDangerousGoods() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpParams httpParams = new BasicHttpParams();
					HttpConnectionParams
							.setConnectionTimeout(httpParams, 60000);
					HttpConnectionParams.setSoTimeout(httpParams, 60000);
					DefaultHttpClient client = new DefaultHttpClient(httpParams);
					HttpPost httppost = new HttpPost(URL_DEVICE_LOG);
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair("macaddr",
							macaddr));
					nameValuePairs.add(new BasicNameValuePair("action", "10"));
					nameValuePairs.add(new BasicNameValuePair("pnr", pnr));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse httpResponse = client.execute(httppost);
				} catch (Exception e) {

				}
			}
		}).start();

		dangerousGoodsChecked = false;
		RelativeLayout layout = new RelativeLayout(this);
		layout.setBackgroundColor(Color.parseColor("#f26f21"));
		layout.setBackgroundResource(R.drawable.main);

		WebView webView = new WebView(this);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		webView.loadData(dangerous_goods, "text/html", "UTF-8");

		webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		webView.setScrollbarFadingEnabled(false);

		RelativeLayout.LayoutParams webViewLP = new RelativeLayout.LayoutParams(
				width, 420);
		webViewLP.leftMargin = 0;
		webViewLP.topMargin = 120;
		layout.addView(webView, webViewLP);

		TextView textView2 = new TextView(this);
		textView2
				.setText("I am aware that the dangerous good above are not permitted in my bag, the luggage has been in my possession at all times and I am aware of the content in the bag.");
		textView2.setTextSize(TEXT_SIZE_36);
		textView2.setTextColor(Color.parseColor("#000000"));
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				width, 100);
		params.leftMargin = 20;
		params.topMargin = 530;
		layout.addView(textView2, params);

		// CheckBox checkBox = new CheckBox(this);
		// checkBox.setText("Yes, I am aware that the dangerous good above are not permitted in my bag, the luggage has been in my possession at all times and I am aware of the content in the bag.");
		// checkBox.setTextSize(TEXT_SIZE_36);
		// checkBox.setTextColor(Color.parseColor("#000000"));
		// checkBox.setOnClickListener(this);
		// checkBox.setId(DANGEROUS_GOODS_CHECKBOX);
		// RelativeLayout.LayoutParams checkBoxLP = new
		// RelativeLayout.LayoutParams(
		// width, 100);
		// checkBoxLP.leftMargin = 20;
		// checkBoxLP.topMargin = 530;
		// layout.addView(checkBox, checkBoxLP);

		Button buttonCancel = new Button(this);
		buttonCancel.setText("Disagree");
		buttonCancel.setTextSize(TEXT_SIZE_40);
		buttonCancel.setTextColor(Color.parseColor("#666666"));
		buttonCancel.setBackgroundResource(R.drawable.custom_button);
		buttonCancel.setId(DANGEROUS_GOODS_BUTTON_CANCEL);
		buttonCancel.setOnClickListener(this);
		RelativeLayout.LayoutParams buttonCancelLP = new RelativeLayout.LayoutParams(
				300, 80);
		buttonCancelLP.leftMargin = 120;
		buttonCancelLP.topMargin = SMALL_BUTTON_BOTTOM_Y;
		layout.addView(buttonCancel, buttonCancelLP);

		Button buttonNext = new Button(this);
		buttonNext.setText("I Agree, Print Boarding Pass");
		buttonNext.setTextSize(TEXT_SIZE_40);
		buttonNext.setTextColor(Color.parseColor("#666666"));
		buttonNext.setBackgroundResource(R.drawable.custom_button);
		buttonNext.setId(DANGEROUS_GOODS_BUTTON_NEXT);
		buttonNext.setOnClickListener(this);
		RelativeLayout.LayoutParams buttonNextLP = new RelativeLayout.LayoutParams(
				600, 80);
		buttonNextLP.leftMargin = 500;
		buttonNextLP.topMargin = SMALL_BUTTON_BOTTOM_Y;
		layout.addView(buttonNext, buttonNextLP);

		setContentView(layout);
	}

	int epsonPrinterStatus = -1;
	int epsonPrinterTask = 0;
	JSONArray epsonPrinterData;
	String istatus = "";

	public synchronized void epsonPrinterTask() {
		if (epsonPrinterTask == 0) {
			Print printer = new Print();
			try {
				printer = new Print();
				printer.openPrinter(Print.DEVTYPE_TCP, printer_ip);
			} catch (Exception e) {
				printer = null;
				epsonPrinterStatus = -1;
			}
			com.epson.eposprint.Builder builder = null;
			try {
				builder = new com.epson.eposprint.Builder("TM-T88V", 0);
				int[] status = new int[1];
				try {
					printer.sendData(builder, SEND_TIMEOUT, status);
					epsonPrinterStatus = status[0];
				} catch (EposException e) {
					epsonPrinterStatus = -1;
				}
			} catch (Exception e) {
				epsonPrinterStatus = -1;
			}
			if (printer != null) {
				try {
					printer.closePrinter();
					printer = null;
				} catch (Exception e) {
					printer = null;
				}
			}
		}
		if (epsonPrinterTask == 1) {
			com.epson.eposprint.Builder builder = null;

			try {
				builder = new com.epson.eposprint.Builder("TM-T88V", 0);
				builder.addTextFont(com.epson.eposprint.Builder.FONT_A);
				builder.addTextAlign(com.epson.eposprint.Builder.ALIGN_LEFT);
				builder.addTextLineSpace(30);
				builder.addTextLang(com.epson.eposprint.Builder.LANG_EN);
				builder.addTextSize(2, 1);
				builder.addTextStyle(com.epson.eposprint.Builder.FALSE,
						com.epson.eposprint.Builder.FALSE,
						com.epson.eposprint.Builder.FALSE,
						com.epson.eposprint.Builder.COLOR_1);
				builder.addTextPosition(0);

				for (int i = 0; i < epsonPrinterData.length(); i++) {
					String str = epsonPrinterData.getString(i);
					if (str.startsWith("addTextSize")) {
						String[] elements = str.split(",", 3);
						if (elements.length == 3) {
							int width = Integer.parseInt(elements[1]);
							int height = Integer.parseInt(elements[2]);
							builder.addTextSize(width, height);
						}
					} else if (str.startsWith("addText")) {
						String[] elements = str.split(",", 2);
						if (elements.length == 2) {
							builder.addText(elements[1]);
						}
					} else if (str.startsWith("addFeedUnit")) {
						String[] elements = str.split(",", 2);
						if (elements.length == 2) {
							int unit = Integer.parseInt(elements[1]);
							builder.addFeedUnit(unit);
						}
					} else if (str.startsWith("addCut")) {
						builder.addCut(com.epson.eposprint.Builder.CUT_FEED);
					} else if (str.startsWith("addImage")) {
						String[] elements = str.split(",", 2);
						if (elements.length == 2) {
							byte[] decodedString = Base64.decode(elements[1],
									Base64.DEFAULT);
							Bitmap decodedByte = BitmapFactory.decodeByteArray(
									decodedString, 0, decodedString.length);
							builder.addImage(decodedByte, 0, 0,
									Math.min(512, decodedByte.getWidth()),
									decodedByte.getHeight(), Builder.COLOR_1);
						}
					} else if (str.startsWith("addBarcode")) {
						String[] elements = str.split(",", 2);
						if (elements.length == 2) {
							builder.addBarcode(elements[1],
									Builder.BARCODE_CODE39, Builder.HRI_NONE,
									Builder.FONT_A, 2, 100);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			Print printer = new Print();
			try {
				printer.openPrinter(Print.DEVTYPE_TCP, printer_ip);
			} catch (Exception e) {
				printer = null;
			}
			try {
				int[] status = new int[1];
				printer.sendData(builder, SEND_TIMEOUT, status);
			} catch (Exception e) {
				printer = null;
			}
			try {
				printer.closePrinter();
				printer = null;
			} catch (Exception e) {
				printer = null;
			}
		}
		if (epsonPrinterTask == 2) {
			com.epson.eposprint.Builder builder = null;

			try {
				builder = new com.epson.eposprint.Builder("TM-T88V", 0);
				builder.addTextFont(com.epson.eposprint.Builder.FONT_A);
				builder.addTextAlign(com.epson.eposprint.Builder.ALIGN_LEFT);
				builder.addTextLineSpace(30);
				builder.addTextLang(com.epson.eposprint.Builder.LANG_EN);
				builder.addTextSize(2, 1);
				builder.addTextStyle(com.epson.eposprint.Builder.FALSE,
						com.epson.eposprint.Builder.FALSE,
						com.epson.eposprint.Builder.FALSE,
						com.epson.eposprint.Builder.COLOR_1);
				builder.addTextPosition(0);
				builder.addTextSize(1, 1);
				builder.addText("Device Name: " + device_name);
				builder.addFeedUnit(30);
				builder.addText("Location Name: " + location_name);
				builder.addFeedUnit(30);
				builder.addText("MAC Address: " + macaddr);
				builder.addFeedUnit(30);
				builder.addText("Server URL: " + server_url);
				builder.addFeedUnit(30);
				builder.addText("Printer IP: " + printer_ip);
				builder.addFeedUnit(30);
				builder.addText("Local IP: " + ip_address);
				builder.addFeedUnit(30);
				builder.addText("Printer Status: " + printer_status);
				builder.addFeedUnit(30);
				builder.addFeedUnit(30);
				builder.addCut(com.epson.eposprint.Builder.CUT_FEED);
			} catch (Exception e) {

			}
			Print printer = new Print();
			try {
				printer.openPrinter(Print.DEVTYPE_TCP, printer_ip);
			} catch (Exception e) {
				printer = null;
			}
			try {
				int[] status = new int[1];
				printer.sendData(builder, SEND_TIMEOUT, status);
			} catch (Exception e) {
				printer = null;
			}
			try {
				printer.closePrinter();
				printer = null;
			} catch (Exception e) {
				printer = null;
			}
		}
	}

	ProgressDialog pd;
	String qrcode = "";
	int isCodeshare = 0;

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				qrcode = intent.getStringExtra("SCAN_RESULT");

				if (qrcode.startsWith("Firefly")) {
					lastUsed = System.currentTimeMillis();
					setContentView(R.layout.printing);
					pd.show();

					Thread worker = new Thread(new Runnable() {
						String returnString = "";

						@Override
						public void run() {
							try {
								HttpParams httpParams = new BasicHttpParams();
								HttpConnectionParams.setConnectionTimeout(
										httpParams, 60000);
								HttpConnectionParams.setSoTimeout(httpParams,
										60000);
								DefaultHttpClient client = new DefaultHttpClient(
										httpParams);
								HttpPost httppost = new HttpPost(
										URL_PRINT_BOARDING_PASS);
								List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
										2);
								nameValuePairs.add(new BasicNameValuePair(
										"macaddr", macaddr));
								nameValuePairs.add(new BasicNameValuePair(
										"qrcode", qrcode));
								httppost.setEntity(new UrlEncodedFormEntity(
										nameValuePairs));
								HttpResponse httpResponse = client
										.execute(httppost);
								HttpEntity entity = httpResponse.getEntity();
								InputStream is = entity.getContent();
								returnString = convertStreamToString(is);
							} catch (Exception e) {
							}
							jsonPrintBoardingPass = returnString;
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									pd.hide();
									showPrintBoardingPass();
								}
							});
						}
					});
					worker.start();

				} else {
					setContentView(R.layout.invalid_code);
					new Thread(new Runnable() {
						@Override
						public void run() {

							try {
								Thread.sleep(5000);

								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										pd.hide();
										showMainMenu();
									}
								});

							} catch (InterruptedException e) {

							}

						}
					}).start();
				}
			} else if (resultCode == RESULT_CANCELED) {
				lastUsed = System.currentTimeMillis();
				action = 1;
				showMainMenu();
			} else if (resultCode == 1) {
				lastUsed = System.currentTimeMillis();
				action = 2;
				showMainMenu();
			}

		}
	}

	public void serverPrintBoardingPass() {
		pd.show();
		Thread worker = new Thread(new Runnable() {
			String returnString = "";

			@Override
			public void run() {
				try {
					HttpParams httpParams = new BasicHttpParams();
					HttpConnectionParams
							.setConnectionTimeout(httpParams, 60000);
					HttpConnectionParams.setSoTimeout(httpParams, 60000);
					DefaultHttpClient client = new DefaultHttpClient(httpParams);
					HttpPost httppost = new HttpPost(URL_PRINT_BOARDING_PASS);
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair("macaddr",
							macaddr));
					nameValuePairs.add(new BasicNameValuePair("pnr", pnr));
					nameValuePairs.add(new BasicNameValuePair(
							"departure_station_code", departureStationCode));
					nameValuePairs.add(new BasicNameValuePair(
							"arrival_station_code", arrivalStationCode));
					nameValuePairs.add(new BasicNameValuePair("STD", STD));
					nameValuePairs.add(new BasicNameValuePair("checkin",
							checkIn));
					// Log.e("URL_PRINT_BOARDING_PASS",
					// nameValuePairs.toString());
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse httpResponse = client.execute(httppost);
					HttpEntity entity = httpResponse.getEntity();
					InputStream is = entity.getContent();
					returnString = convertStreamToString(is);
				} catch (Exception e) {
				}
				jsonPrintBoardingPass = returnString;
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						pd.hide();
						showPrintBoardingPass();
					}
				});

			}
		});
		worker.start();
	}

	public void refreshPassengerListButtonNext() {
		boolean flag = false;
		for (int j = 0; j < passengerCheckBox.length; j++) {
			if (passengerCheckBox[j] != null) {
				if (passengerCheckBox[j].isChecked() == true) {
					flag = true;
				}
			}
		}
		if (flag == true) {
			passengerListbuttonNextDisable.setVisibility(View.GONE);
			passengerListbuttonNextEnable.setVisibility(View.VISIBLE);
		} else {
			passengerListbuttonNextDisable.setVisibility(View.VISIBLE);
			passengerListbuttonNextEnable.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == PASSENGER_LIST_BUTTON_NAME) {
			int i = Integer.parseInt(view.getTag().toString());
			if (passengerCheckBox[i].isChecked() == true) {
				passengerCheckBox[i].setChecked(false);
			} else {
				passengerCheckBox[i].setChecked(true);
			}
			refreshPassengerListButtonNext();

		} else if (view.getId() == MAIN_MENU_BUTTON_ENTER_FIREFLY_PNR) {
			action = 1;
			isCodeshare = 0;
			showEnterPNR();
		} else if (view.getId() == MAIN_MENU_BUTTON_ENTER_CODESHARE_PNR) {
			action = 1;
			isCodeshare = 1;
			showEnterPNR();
		} else if (view.getId() == MAIN_MENU_BUTTON_SCAN_QR_CODE) {
			action = 1;
			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
			intent.putExtra("SAVE_HISTORY", false);
			startActivityForResult(intent, 0);
		} else if (view.getId() == TASK_MENU_BUTTON_CHECK_IN) {
			action = 1;
			showPassengerList();
		} else if (view.getId() == TASK_MENU_BUTTON_REPRINT_BOARDING_PASS) {
			action = 2;
			showPassengerList();
		} else if (view.getId() == TASK_MENU_BUTTON_CANCEL) {
			showSector();
		} else if (view.getId() == ENTER_PNR_BUTTON_BACKSPACE) {
			if (textViewPNR.getText().length() > 0) {
				String str = textViewPNR.getText().toString();
				textViewPNR.setText(str.substring(0, str.length() - 1));
			}
		} else if (view.getId() == ENTER_PNR_BUTTON_CANCEL) {
			showMainMenu();
		} else if (view.getId() == ENTER_PNR_BUTTON_NEXT) {
			pnr = textViewPNR.getText().toString();
			if (page == 100) {
				if (pnr.equals(pin.trim())) {
					showSpecialMenu();
				} else {
					showMainMenu();
				}
			} else {
				// TODO DEBUG
				if (pnr.length() != PNR_LENGTH[isCodeshare]) {
					Toast.makeText(getApplicationContext(),
							"Confirmation number is invalid.",
							Toast.LENGTH_LONG).show();

				} else {
					pd.show();
					Thread worker = new Thread(new Runnable() {
						String returnString = "";

						@Override
						public void run() {
							try {
								HttpParams httpParams = new BasicHttpParams();
								HttpConnectionParams.setConnectionTimeout(
										httpParams, 60000);
								HttpConnectionParams.setSoTimeout(httpParams,
										60000);
								DefaultHttpClient client = new DefaultHttpClient(
										httpParams);
								HttpPost httppost = new HttpPost(URL_CHECK_PNR);
								List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
										2);
								nameValuePairs.add(new BasicNameValuePair(
										"macaddr", macaddr));
								nameValuePairs.add(new BasicNameValuePair(
										"action", "" + action));
								nameValuePairs.add(new BasicNameValuePair(
										"pnr", pnr));
								httppost.setEntity(new UrlEncodedFormEntity(
										nameValuePairs));
								HttpResponse httpResponse = client
										.execute(httppost);
								HttpEntity entity = httpResponse.getEntity();
								InputStream is = entity.getContent();
								returnString = convertStreamToString(is);
							} catch (Exception e) {

							}
							jsonCheckPNR = returnString;
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									pd.hide();
									showSector();
								}
							});
						}
					});
					worker.start();

				}
			}
		} else if (view.getId() == ENTER_PNR_BUTTON_ALPHANUMERIC) {
			// XXX 2014-06-03
			if (isCodeshare == 1) {
				if (textViewPNR.getText().length() < 5) {
					textViewPNR.setText(textViewPNR.getText() + ""
							+ view.getTag());
				}
			} else {
				if (textViewPNR.getText().length() < 6) {
					textViewPNR.setText(textViewPNR.getText() + ""
							+ view.getTag());
				}
			}
		} else if (view.getId() == CHECKED_IN_BUTTON_PRINT_BOARDING_PASS) {
			action = 2;
			serverPrintBoardingPass();
		} else if (view.getId() == CHECKED_IN_BUTTON_MAIN_MENU) {
			showMainMenu();
		} else if (view.getId() == SECTOR_BUTTON_FLIGHT) {
			Button button = (Button) view;
			String tag = button.getTag().toString();
			departureStationCode = tag.substring(0, 3);
			arrivalStationCode = tag.substring(3, 6);
			STD = tag.substring(6);
			showTaskMenu();
		} else if (view.getId() == SECTOR_BUTTON_CANCEL) {
			showEnterPNR();
		} else if (view.getId() == PASSENGER_LIST_CHECKBOX) {
			refreshPassengerListButtonNext();
			/*
			 * CheckBox checkBox = (CheckBox) view; if (checkBox.isChecked() ==
			 * true) { passengerNumber.add(checkBox.getTag().toString()); } else
			 * { passengerNumber.remove(checkBox.getTag().toString()); }
			 */
		} else if (view.getId() == PASSENGER_LIST_BUTTON_CANCEL) {
			showTaskMenu();
		} else if (view.getId() == PASSENGER_LIST_BUTTON_NEXT) {
			checkIn = "";
			for (int i = 0; i < passengerCheckBox.length; i++) {
				if (passengerCheckBox[i] != null) {
					if (passengerCheckBox[i].isChecked() == true) {
						checkIn += passengerCheckBox[i].getTag().toString()
								+ ",";
					}
				}
			}
			if (checkIn.equals("")) {
				Toast.makeText(getApplicationContext(),
						"Passenger is required.", Toast.LENGTH_LONG).show();
			} else {
				// TODO DEBUG

				try {
					JSONArray passenger = new JSONObject(jsonCheckPNR)
							.optJSONArray("passenger");
					String[] s = checkIn.split(",");

					JSONArray passengerList = new JSONArray();
					for (String str : s) {
						passengerList.put(passenger.optJSONObject(Integer
								.parseInt(str)));
					}

					// boolean isCompletedDoc = true;
					// for (int i = 0; i < passengerList.length(); i++) {
					// JSONObject p = passengerList.optJSONObject(i);
					// if (p.optBoolean("DocCompleteness") == false) {
					// isCompletedDoc = false;
					// }
					// }

					if (action == 2) {
						showAgreement();
					} else {
						Intent i = UpdateTravelDoc.newInstance(this, pnr,
								passengerList);
						i.putExtra(UpdateTravelDoc.DEPARTURE_CODE,
								departureStationCode);
						i.putExtra(UpdateTravelDoc.ARRIVAL_CODE,
								arrivalStationCode);
						startActivity(i);
					}
					// startActivity(UpdateTravelDoc.newInstance(this, pnr,
					// passengerList));

					// Log.e("checkIn", checkIn);
					// Log.e("passengerList",passengerList.toString());

				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG)
							.show();
				}
			}
		} else if (view.getId() == AGREEMENT_CHECKBOX) {
			CheckBox checkBox = (CheckBox) view;
			if (checkBox.isChecked() == true) {
				agreementChecked = true;
			} else {
				agreementChecked = false;
			}
		} else if (view.getId() == AGREEMENT_BUTTON_CANCEL) {
			showMainMenu();
		} else if (view.getId() == AGREEMENT_BUTTON_NEXT) {
			showDangerousGoods();
		} else if (view.getId() == DANGEROUS_GOODS_CHECKBOX) {
			CheckBox checkBox = (CheckBox) view;
			if (checkBox.isChecked() == true) {
				dangerousGoodsChecked = true;
			} else {
				dangerousGoodsChecked = false;
			}
		} else if (view.getId() == DANGEROUS_GOODS_BUTTON_CANCEL) {
			showMainMenu();
		} else if (view.getId() == DANGEROUS_GOODS_BUTTON_NEXT) {
			setContentView(R.layout.printing);
			pd.show();
			if (action == 1) {
				Thread worker = new Thread(new Runnable() {
					String returnString = "";

					@Override
					public void run() {
						try {
							HttpParams httpParams = new BasicHttpParams();
							HttpConnectionParams.setConnectionTimeout(
									httpParams, 60000);
							HttpConnectionParams
									.setSoTimeout(httpParams, 60000);
							DefaultHttpClient client = new DefaultHttpClient(
									httpParams);
							HttpPost httppost = new HttpPost(
									URL_CHECKIN_PASSENGER);
							List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
									2);
							nameValuePairs.add(new BasicNameValuePair(
									"macaddr", macaddr));
							nameValuePairs.add(new BasicNameValuePair("pnr",
									pnr));
							nameValuePairs.add(new BasicNameValuePair(
									"departure_station_code",
									departureStationCode));
							nameValuePairs
									.add(new BasicNameValuePair(
											"arrival_station_code",
											arrivalStationCode));
							nameValuePairs.add(new BasicNameValuePair("STD",
									STD));
							nameValuePairs.add(new BasicNameValuePair(
									"checkin", checkIn));
							httppost.setEntity(new UrlEncodedFormEntity(
									nameValuePairs));
							HttpResponse httpResponse = client
									.execute(httppost);
							HttpEntity entity = httpResponse.getEntity();
							InputStream is = entity.getContent();
							returnString = convertStreamToString(is);
						} catch (Exception e) {
						}
						jsonPrintBoardingPass = returnString;
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								pd.hide();
								showPrintBoardingPass();
							}
						});

					}
				});
				worker.start();
			} else if (action == 2) {
				serverPrintBoardingPass();
			}
		} else if (view.getId() == SPECIAL_MENU_EXIT) {
			flag = true;
			finish();
		} else if (view.getId() == SPECIAL_MENU_INFO) {
			showSpecialInfo();
		} else if (view.getId() == SPECIAL_MENU_TEST) {
			showSpecialTest();
		}
	}

	boolean agreementChecked = false;
	boolean dangerousGoodsChecked = false;
	List<String> passengerNumber = new ArrayList<String>();

	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append((line + "\n"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	float pointer1startX = 0;
	float pointer1startY = 0;
	float pointer2startX = 0;
	float pointer2startY = 0;
	long startTime = 0;
	float pointer1endX = 0;
	float pointer1endY = 0;
	float pointer2endX = 0;
	float pointer2endY = 0;
	long endTime = 0;
	int pointerCount = 0;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN
				|| ev.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
			if (pointerCount < ev.getPointerCount()) {
				pointerCount = ev.getPointerCount();
			}
			if (ev.getPointerCount() == 1) {
				pointer1startX = ev.getX(0);
				pointer1startY = ev.getY(0);
				startTime = ev.getEventTime();
			}
			if (ev.getPointerCount() == 2) {
				pointer2startX = ev.getX(1);
				pointer2startY = ev.getY(1);
				startTime = ev.getEventTime();
			}
		}

		if (ev.getAction() == MotionEvent.ACTION_UP
				|| ev.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
			if (pointerCount == 1 && ev.getPointerCount() == 1) {
				pointer1endX = ev.getX(0);
				pointer1endY = ev.getY(0);
				endTime = ev.getEventTime();
			}
			if (pointerCount == 2 && ev.getPointerCount() == 2) {
				pointer1endX = ev.getX(0);
				pointer1endY = ev.getY(0);
				pointer2endX = ev.getX(1);
				pointer2endY = ev.getY(1);
				endTime = ev.getEventTime();
			}
			if (ev.getPointerCount() == 1) {
				float duration = endTime - startTime;
				if (pointerCount == 1) {
					float xVelocity1 = Math.abs((pointer1endX - pointer1startX)
							/ (duration / 1000));
					float yVelocity1 = Math.abs((pointer1endY - pointer1startY)
							/ (duration / 1000));
					if (pointer1startX < pointer1endX && xVelocity1 > 500) {
					} else if (pointer1startX > pointer1endX
							&& xVelocity1 > 500) {
					} else if (pointer1startY < pointer1endY
							&& yVelocity1 > 500) {
					} else if (pointer1startY > pointer1endY
							&& yVelocity1 > 500) {
					}
				} else if (pointerCount == 2) {
					float xVelocity1 = Math.abs((pointer1endX - pointer1startX)
							/ (duration / 1000));
					float yVelocity1 = Math.abs((pointer1endY - pointer1startY)
							/ (duration / 1000));
					float xVelocity2 = Math.abs((pointer2endX - pointer2startX)
							/ (duration / 1000));
					float yVelocity2 = Math.abs((pointer2endY - pointer2startY)
							/ (duration / 1000));
					if (pointer1startX < pointer1endX && xVelocity1 > 500
							&& pointer2startX < pointer2endX
							&& xVelocity2 > 500) {
					} else if (pointer1startX > pointer1endX
							&& xVelocity1 > 500
							&& pointer2startX > pointer2endX
							&& xVelocity2 > 500) {
					} else if (pointer1startY < pointer1endY
							&& yVelocity1 > 500
							&& pointer2startY < pointer2endY
							&& yVelocity2 > 500) {
						showMainMenu();
					} else if (pointer1startY > pointer1endY
							&& yVelocity1 > 500
							&& pointer2startY > pointer2endY
							&& yVelocity2 > 500) {
						showEnterPIN();
					}
				}
				pointerCount = 0;
				pointer1startX = 0f;
				pointer1startY = 0f;
				pointer2startX = 0f;
				pointer2startY = 0f;
				pointer1endX = 0f;
				pointer1endY = 0f;
				pointer2endX = 0f;
				pointer2endY = 0f;
			}
		}

		return super.dispatchTouchEvent(ev);
	}

	public void showEnterPIN() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpParams httpParams = new BasicHttpParams();
					HttpConnectionParams
							.setConnectionTimeout(httpParams, 60000);
					HttpConnectionParams.setSoTimeout(httpParams, 60000);
					DefaultHttpClient client = new DefaultHttpClient(httpParams);
					HttpPost httppost = new HttpPost(URL_DEVICE_LOG);
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair("macaddr",
							macaddr));
					nameValuePairs.add(new BasicNameValuePair("action", "12"));
					nameValuePairs.add(new BasicNameValuePair("pnr", ""));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse httpResponse = client.execute(httppost);
				} catch (Exception e) {

				}
			}
		}).start();

		flagShowPrinterOffline = 0;
		page = 100;
		RelativeLayout layout = new RelativeLayout(this);
		layout.setBackgroundColor(Color.parseColor("#f26f21"));
		layout.setBackgroundResource(R.drawable.main);
		textViewInstruction = new TextView(this);
		textViewInstruction.setText("Please enter your PIN number:");
		textViewInstruction.setTextSize(TEXT_SIZE_40);
		textViewInstruction.setTextColor(Color.parseColor("#000000"));
		textViewInstruction.setGravity(Gravity.CENTER);
		RelativeLayout.LayoutParams textViewInstructionLP = new RelativeLayout.LayoutParams(
				width, 70);
		textViewInstructionLP.leftMargin = 0;
		textViewInstructionLP.topMargin = 130;
		layout.addView(textViewInstruction, textViewInstructionLP);
		textViewPNR = new TextView(this);
		textViewPNR.setText("");
		textViewPNR.setTextSize(TEXT_SIZE_40);
		textViewPNR.setTextColor(Color.parseColor("#000000"));
		textViewPNR.setBackgroundColor(Color.parseColor("#ffffff"));
		textViewPNR.setGravity(Gravity.CENTER);
		RelativeLayout.LayoutParams textViewPNRLP = new RelativeLayout.LayoutParams(
				width / 2, 70);
		textViewPNRLP.leftMargin = width / 4;
		textViewPNRLP.topMargin = 210;
		layout.addView(textViewPNR, textViewPNRLP);
		Button buttonBackspace = new Button(this);
		buttonBackspace.setBackgroundResource(R.drawable.backspace);
		buttonBackspace.setOnClickListener(this);
		buttonBackspace.setId(ENTER_PNR_BUTTON_BACKSPACE);
		RelativeLayout.LayoutParams buttonBackspaceLP = new RelativeLayout.LayoutParams(
				65, 52);
		buttonBackspaceLP.leftMargin = 1000;
		buttonBackspaceLP.topMargin = 220;
		layout.addView(buttonBackspace, buttonBackspaceLP);
		Button buttonCancel = new Button(this);
		buttonCancel.setText("Cancel");
		buttonCancel.setTextSize(TEXT_SIZE_40);
		buttonCancel.setTextColor(Color.parseColor("#666666"));
		buttonCancel.setBackgroundResource(R.drawable.custom_button);
		buttonCancel.setOnClickListener(this);
		buttonCancel.setId(ENTER_PNR_BUTTON_CANCEL);
		RelativeLayout.LayoutParams buttonCancelLP = new RelativeLayout.LayoutParams(
				SMALL_BUTTON_WIDTH, SMALL_BUTTON_HEIGHT);
		buttonCancelLP.leftMargin = (width / 2) - 300 - 10;
		buttonCancelLP.topMargin = 620 - 80 - 80 - 80 - 80;
		layout.addView(buttonCancel, buttonCancelLP);
		Button buttonNext = new Button(this);
		buttonNext.setText("Next");
		buttonNext.setTextSize(TEXT_SIZE_40);
		buttonNext.setTextColor(Color.parseColor("#666666"));
		buttonNext.setBackgroundResource(R.drawable.custom_button);
		buttonNext.setOnClickListener(this);
		buttonNext.setId(ENTER_PNR_BUTTON_NEXT);
		RelativeLayout.LayoutParams buttonNextlLP = new RelativeLayout.LayoutParams(
				SMALL_BUTTON_WIDTH, SMALL_BUTTON_HEIGHT);
		buttonNextlLP.leftMargin = (width / 2) + 10;
		buttonNextlLP.topMargin = 620 - 80 - 80 - 80 - 80;
		layout.addView(buttonNext, buttonNextlLP);
		for (int i = 0; i < 10; i++) {
			Button button = new Button(this);
			button.setText("" + i);
			button.setTextSize(TEXT_SIZE_40);
			button.setTextColor(Color.parseColor("#666666"));
			button.setBackgroundResource(R.drawable.custom_button);
			button.setOnClickListener(this);
			button.setTag("" + i);
			button.setId(ENTER_PNR_BUTTON_ALPHANUMERIC);
			RelativeLayout.LayoutParams buttonLP = new RelativeLayout.LayoutParams(
					((width - 100) / 10) - 4, 70);
			buttonLP.leftMargin = 50 + (0 + ((width - 100) / 10) * i) + 2;
			buttonLP.topMargin = 640 - 80 - 80 - 80;
			layout.addView(button, buttonLP);
		}
		String QWERTYUIOP = "QWERTYUIOP";
		for (int i = 0; i < 10; i++) {
			Button button = new Button(this);
			button.setText(QWERTYUIOP.substring(i, i + 1));
			button.setTextSize(TEXT_SIZE_40);
			button.setTextColor(Color.parseColor("#666666"));
			button.setBackgroundResource(R.drawable.custom_button);
			button.setOnClickListener(this);
			button.setTag(QWERTYUIOP.substring(i, i + 1));
			button.setId(ENTER_PNR_BUTTON_ALPHANUMERIC);
			RelativeLayout.LayoutParams buttonLP = new RelativeLayout.LayoutParams(
					((width - 100) / 10) - 4, 70);
			buttonLP.leftMargin = 50 + (0 + ((width - 100) / 10) * i) + 2;
			buttonLP.topMargin = 640 - 80 - 80;
			layout.addView(button, buttonLP);
		}
		String ASDFGHJKL = "ASDFGHJKL";
		for (int i = 0; i < 9; i++) {
			Button button = new Button(this);
			button.setText(ASDFGHJKL.substring(i, i + 1));
			button.setTextSize(TEXT_SIZE_40);
			button.setTextColor(Color.parseColor("#666666"));
			button.setBackgroundResource(R.drawable.custom_button);
			button.setOnClickListener(this);
			button.setTag(ASDFGHJKL.substring(i, i + 1));
			button.setId(ENTER_PNR_BUTTON_ALPHANUMERIC);
			RelativeLayout.LayoutParams buttonLP = new RelativeLayout.LayoutParams(
					((width - 100) / 10) - 4, 70);
			buttonLP.leftMargin = 35 + 50 + (0 + ((width - 100) / 10) * i) + 2;
			buttonLP.topMargin = 640 - 80;
			layout.addView(button, buttonLP);
		}
		String ZXCVBNM = "ZXCVBNM";
		for (int i = 0; i < 7; i++) {
			Button button = new Button(this);
			button.setText(ZXCVBNM.substring(i, i + 1));
			button.setTextSize(TEXT_SIZE_40);
			button.setTextColor(Color.parseColor("#666666"));
			button.setBackgroundResource(R.drawable.custom_button);
			button.setOnClickListener(this);
			button.setTag(ZXCVBNM.substring(i, i + 1));
			button.setId(ENTER_PNR_BUTTON_ALPHANUMERIC);
			RelativeLayout.LayoutParams buttonLP = new RelativeLayout.LayoutParams(
					((width - 100) / 10) - 4, 70);
			buttonLP.leftMargin = 35 + 35 + 35 + 50
					+ (0 + ((width - 100) / 10) * i) + 2;
			buttonLP.topMargin = 640;
			layout.addView(button, buttonLP);
		}
		setContentView(layout);
	}

	public void showSpecialMenu() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpParams httpParams = new BasicHttpParams();
					HttpConnectionParams
							.setConnectionTimeout(httpParams, 60000);
					HttpConnectionParams.setSoTimeout(httpParams, 60000);
					DefaultHttpClient client = new DefaultHttpClient(httpParams);
					HttpPost httppost = new HttpPost(URL_DEVICE_LOG);
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair("macaddr",
							macaddr));
					nameValuePairs.add(new BasicNameValuePair("action", "13"));
					nameValuePairs.add(new BasicNameValuePair("pnr", ""));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse httpResponse = client.execute(httppost);
				} catch (Exception e) {

				}
			}
		}).start();

		page = 100;
		RelativeLayout layout = new RelativeLayout(this);
		layout.setBackgroundColor(Color.parseColor("#f26f21"));
		layout.setBackgroundResource(R.drawable.main);

		Button button1 = new Button(this);
		button1.setText("Exit");
		button1.setTextSize(TEXT_SIZE_50);
		button1.setTextColor(Color.parseColor("#666666"));
		button1.setBackgroundResource(R.drawable.custom_button);
		button1.setOnClickListener(this);
		button1.setId(SPECIAL_MENU_EXIT);
		RelativeLayout.LayoutParams button1LP = new RelativeLayout.LayoutParams(
				1280 - 200, 150);
		button1LP.leftMargin = 100;
		button1LP.topMargin = 200;
		layout.addView(button1, button1LP);

		Button button2 = new Button(this);
		button2.setText("Info");
		button2.setTextSize(TEXT_SIZE_50);
		button2.setTextColor(Color.parseColor("#666666"));
		button2.setBackgroundResource(R.drawable.custom_button);
		button2.setOnClickListener(this);
		button2.setId(SPECIAL_MENU_INFO);
		RelativeLayout.LayoutParams button2LP = new RelativeLayout.LayoutParams(
				1280 - 200, 150);
		button2LP.leftMargin = 100;
		button2LP.topMargin = 200 + 170;
		layout.addView(button2, button2LP);

		Button button3 = new Button(this);
		button3.setText("Test");
		button3.setTextSize(TEXT_SIZE_50);
		button3.setTextColor(Color.parseColor("#666666"));
		button3.setBackgroundResource(R.drawable.custom_button);
		button3.setOnClickListener(this);
		button3.setId(SPECIAL_MENU_TEST);
		RelativeLayout.LayoutParams button3LP = new RelativeLayout.LayoutParams(
				1280 - 200, 150);
		button3LP.leftMargin = 100;
		button3LP.topMargin = 200 + 170 + 170;
		layout.addView(button3, button3LP);

		setContentView(layout);

	}

	public void showSpecialInfo() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpParams httpParams = new BasicHttpParams();
					HttpConnectionParams
							.setConnectionTimeout(httpParams, 60000);
					HttpConnectionParams.setSoTimeout(httpParams, 60000);
					DefaultHttpClient client = new DefaultHttpClient(httpParams);
					HttpPost httppost = new HttpPost(URL_DEVICE_LOG);
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair("macaddr",
							macaddr));
					nameValuePairs.add(new BasicNameValuePair("action", "14"));
					nameValuePairs.add(new BasicNameValuePair("pnr", ""));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse httpResponse = client.execute(httppost);
				} catch (Exception e) {

				}
			}
		}).start();

		page = 100;
		RelativeLayout layout = new RelativeLayout(this);
		layout.setBackgroundColor(Color.parseColor("#f26f21"));
		layout.setBackgroundResource(R.drawable.main);

		WifiManager wifiMan = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInf = wifiMan.getConnectionInfo();
		macaddr = wifiInf.getMacAddress();
		int ipAddress = wifiInf.getIpAddress();

		ip_address = String.format("%d.%d.%d.%d", (ipAddress & 0xff),
				(ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
				(ipAddress >> 24 & 0xff));

		String info = "Device Name: " + device_name + "\n";
		info += "Location Name: " + location_name + "\n";
		info += "MAC Address: " + macaddr + "\n";
		info += "Server URL: " + server_url + "\n";
		info += "Printer IP: " + printer_ip + "\n";
		info += "Local IP: " + ip_address + "\n";
		info += "Printer Status: " + printer_status + "\n";

		TextView textViewInfo = new TextView(this);
		textViewInfo.setText(info);
		textViewInfo.setTextSize(28.0f);
		textViewInfo.setTextColor(Color.parseColor("#000000"));
		RelativeLayout.LayoutParams textViewInfoLP = new RelativeLayout.LayoutParams(
				1000, 400);
		textViewInfoLP.leftMargin = 100;
		textViewInfoLP.topMargin = 200;
		layout.addView(textViewInfo, textViewInfoLP);

		Button buttonCancel = new Button(this);
		buttonCancel.setText("Cancel");
		buttonCancel.setTextSize(TEXT_SIZE_40);
		buttonCancel.setTextColor(Color.parseColor("#666666"));
		buttonCancel.setBackgroundResource(R.drawable.custom_button);
		buttonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showSpecialMenu();
			}
		});
		RelativeLayout.LayoutParams buttonCancelLP = new RelativeLayout.LayoutParams(
				SMALL_BUTTON_WIDTH, SMALL_BUTTON_HEIGHT);
		buttonCancelLP.leftMargin = 640 - 150 - 300;
		buttonCancelLP.topMargin = SMALL_BUTTON_BOTTOM_Y;
		layout.addView(buttonCancel, buttonCancelLP);

		Button buttonPrint = new Button(this);
		buttonPrint.setText("Print");
		buttonPrint.setTextSize(TEXT_SIZE_40);
		buttonPrint.setTextColor(Color.parseColor("#666666"));
		buttonPrint.setBackgroundResource(R.drawable.custom_button);
		buttonPrint.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				epsonPrinterTask = 2;
				epsonPrinterTask();
			}
		});
		RelativeLayout.LayoutParams buttonPrintLP = new RelativeLayout.LayoutParams(
				SMALL_BUTTON_WIDTH, SMALL_BUTTON_HEIGHT);
		buttonPrintLP.leftMargin = 640 - 150 + 300;
		buttonPrintLP.topMargin = SMALL_BUTTON_BOTTOM_Y;
		layout.addView(buttonPrint, buttonPrintLP);

		setContentView(layout);
	}

	String test = "";
	TextView textViewInfo;
	String test1 = "Checking ...";
	String test2 = "Checking ...";
	String test3 = "Checking ...";

	public void showSpecialTest() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpParams httpParams = new BasicHttpParams();
					HttpConnectionParams
							.setConnectionTimeout(httpParams, 60000);
					HttpConnectionParams.setSoTimeout(httpParams, 60000);
					DefaultHttpClient client = new DefaultHttpClient(httpParams);
					HttpPost httppost = new HttpPost(URL_DEVICE_LOG);
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair("macaddr",
							macaddr));
					nameValuePairs.add(new BasicNameValuePair("action", "15"));
					nameValuePairs.add(new BasicNameValuePair("pnr", ""));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse httpResponse = client.execute(httppost);
				} catch (Exception e) {

				}
			}
		}).start();

		test1 = "Checking ...";
		test2 = "Checking ...";
		test3 = "Checking ...";

		page = 100;
		RelativeLayout layout = new RelativeLayout(this);
		layout.setBackgroundColor(Color.parseColor("#f26f21"));
		layout.setBackgroundResource(R.drawable.main);

		test = "Internet Connection: " + test1 + "\n";
		test += "Navitaire Connection: " + test2 + "\n";
		test += "Printer Connection: " + test3 + "\n";

		textViewInfo = new TextView(this);
		textViewInfo.setText(test);
		textViewInfo.setTextSize(28.0f);
		textViewInfo.setTextColor(Color.parseColor("#000000"));
		RelativeLayout.LayoutParams textViewInfoLP = new RelativeLayout.LayoutParams(
				1000, 400);
		textViewInfoLP.leftMargin = 100;
		textViewInfoLP.topMargin = 200;
		layout.addView(textViewInfo, textViewInfoLP);

		Button buttonCancel = new Button(this);
		buttonCancel.setText("Cancel");
		buttonCancel.setTextSize(TEXT_SIZE_40);
		buttonCancel.setTextColor(Color.parseColor("#666666"));
		buttonCancel.setBackgroundResource(R.drawable.custom_button);
		buttonCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showSpecialMenu();
			}
		});
		RelativeLayout.LayoutParams buttonCancelLP = new RelativeLayout.LayoutParams(
				SMALL_BUTTON_WIDTH, SMALL_BUTTON_HEIGHT);
		buttonCancelLP.leftMargin = 640 - 150;
		buttonCancelLP.topMargin = SMALL_BUTTON_BOTTOM_Y;
		layout.addView(buttonCancel, buttonCancelLP);

		setContentView(layout);

		kiosk2_test_internet();
	}

	public void kiosk2_test_internet() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String test_internet = kiosk2_test_internet_connection();
				if (test_internet != null && test_internet.trim().equals("OK")) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							kiosk2_test_internet_ok();
						}
					});
				} else {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							kiosk2_test_internet_failed();
						}
					});
				}
			}
		}).start();
	}

	public String kiosk2_test_internet_connection() {
		String returnString = null;
		try {
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 60000);
			HttpConnectionParams.setSoTimeout(httpParams, 60000);
			DefaultHttpClient client = new DefaultHttpClient(httpParams);
			HttpPost httppost = new HttpPost(
					"http://fymobilekiosk.me-tech.com.my/kiosk_dev2/kiosk2_test_internet.php");
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse httpResponse = client.execute(httppost);
			HttpEntity entity = httpResponse.getEntity();
			InputStream is = entity.getContent();
			returnString = convertStreamToString(is);
		} catch (Exception exception) {

		}
		return returnString;
	}

	public void kiosk2_test_internet_ok() {
		test1 = "OK";
		test = "Internet Connection: " + test1 + "\n";
		test += "Navitaire Connection: " + test2 + "\n";
		test += "Printer Connection: " + test3 + "\n";
		textViewInfo.setText(test);
		kiosk2_test_navitaire();
	}

	public void kiosk2_test_internet_failed() {
		test1 = "Failed";
		test = "Internet Connection: " + test1 + "\n";
		test += "Navitaire Connection: " + test2 + "\n";
		test += "Printer Connection: " + test3 + "\n";
		textViewInfo.setText(test);
		kiosk2_test_navitaire();
	}

	public void kiosk2_test_navitaire() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String test_internet = kiosk2_test_navitaire_connection();
				if (test_internet != null && test_internet.trim().equals("OK")) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							kiosk2_test_navitaire_ok();
						}
					});
				} else {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							kiosk2_test_navitaire_failed();
						}
					});
				}
			}
		}).start();
	}

	public String kiosk2_test_navitaire_connection() {
		String returnString = null;
		try {
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 60000);
			HttpConnectionParams.setSoTimeout(httpParams, 60000);
			DefaultHttpClient client = new DefaultHttpClient(httpParams);
			HttpPost httppost = new HttpPost(
					"http://fymobilekiosk.me-tech.com.my/kiosk_dev2/kiosk2_test_navitaire.php");
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse httpResponse = client.execute(httppost);
			HttpEntity entity = httpResponse.getEntity();
			InputStream is = entity.getContent();
			returnString = convertStreamToString(is);
		} catch (Exception exception) {

		}
		return returnString;
	}

	public void kiosk2_test_navitaire_ok() {
		test2 = "OK";
		test = "Internet Connection: " + test1 + "\n";
		test += "Navitaire Connection: " + test2 + "\n";
		test += "Printer Connection: " + test3 + "\n";
		textViewInfo.setText(test);
		kiosk2_test_printer();
	}

	public void kiosk2_test_navitaire_failed() {
		test2 = "Failed";
		test = "Internet Connection: " + test1 + "\n";
		test += "Navitaire Connection: " + test2 + "\n";
		test += "Printer Connection: " + test3 + "\n";
		textViewInfo.setText(test);
		kiosk2_test_printer();
	}

	public void kiosk2_test_printer() {
		epsonPrinterTask = 0;
		epsonPrinterTask();
		if (epsonPrinterStatus == -1) {
			kiosk2_test_printer_failed();
		} else {
			kiosk2_test_printer_ok();
		}
	}

	public void kiosk2_test_printer_ok() {
		test3 = "OK";
		test = "Internet Connection: " + test1 + "\n";
		test += "Navitaire Connection: " + test2 + "\n";
		test += "Printer Connection: " + test3 + "\n";
		textViewInfo.setText(test);
	}

	public void kiosk2_test_printer_failed() {
		test3 = "Failed";
		test = "Internet Connection: " + test1 + "\n";
		test += "Navitaire Connection: " + test2 + "\n";
		test += "Printer Connection: " + test3 + "\n";
		textViewInfo.setText(test);
	}

	// XXX 2014-06-10
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent.getBooleanExtra(UpdateTravelDoc.KEY_UPDATE_COMPLETE, false)) {
			this.showAgreement();
		}
	}

	// XXX 2014-06-10
	public static Intent newInstance(Activity activity) {
		Intent intent = new Intent(activity, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		return intent;
	}

	// XXX 2014-06-20
	public static void alert(Activity activity, String text, boolean isHtml) {
		AlertDialog.Builder alert = new AlertDialog.Builder(activity);
		alert.setCancelable(false);
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});
		if (isHtml) {
			WebView webView = new WebView(activity);
			webView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));
			webView.loadDataWithBaseURL(null, text, "text/html", "utf-8", null);
			alert.setView(webView);
		} else {
			alert.setMessage(text);
		}
		if (text != null) {
			AlertDialog alertDialog = alert.create();
			alertDialog.show();
		}
	}

	private String readAssets(String assetFilePath) {
		// XXX 2014-06-26
		InputStream inputStream = null;

		try {
			inputStream = getAssets().open(assetFilePath);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			StringBuilder strBuild = new StringBuilder();
			try {
				String line;
				while ((line = reader.readLine()) != null) {
					strBuild.append(line);
				}
			} finally {
				reader.close();
			}
			return strBuild.toString();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

}
