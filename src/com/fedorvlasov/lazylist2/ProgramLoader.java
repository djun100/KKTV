package com.fedorvlasov.lazylist2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.keke.player.R;
import org.stagex.danmaku.adapter.ProgramInfo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.DateFormat;
import android.widget.ImageView;
import android.widget.TextView;

public class ProgramLoader {

	private Context mContext;

	MemoryCache memoryCache = new MemoryCache();

	private Map<TextView, String> textViews = Collections
			.synchronizedMap(new WeakHashMap<TextView, String>());

	public ProgramLoader(Context context) {
		// Make the background thead low priority. This way it will not affect
		// the UI performance
		programLoaderThread.setPriority(Thread.NORM_PRIORITY - 2);
		mContext = context;
	}

	public void DisplayText(String url, Activity activity, TextView textView) {
		textViews.put(textView, url);
		String program = memoryCache.get(url);
		if (program != null)
			textView.setText(program);
		else {
			queueProgram(url, activity, textView);
			textView.setText("loading...");
		}
	}

	private void queueProgram(String url, Activity activity, TextView textView) {
		// This ImageView may be used for other images before. So there may be
		// some old tasks in the queue. We need to discard them.
		programsQueue.Clean(textView);
		ProgramToLoad p = new ProgramToLoad(url, textView);
		synchronized (programsQueue.programsToLoad) {
			programsQueue.programsToLoad.push(p);
			programsQueue.programsToLoad.notifyAll();
		}

		// start thread if it's not started yet
		if (programLoaderThread.getState() == Thread.State.NEW)
			programLoaderThread.start();
	}

	private String getProgram(String programPath) {

		/* ====================================================== */
		int listPosition = 0;

		/* TODO 以listView文本方式显示节目预告 */
		Document doc = null;
		try {
			doc = Jsoup.connect(
					"http://www.tvmao.com/ext/show_tv.jsp?p=" + programPath)
					.get();

			Elements links = doc.select("li"); // 带有href属性的a元素

			ArrayList<ProgramInfo> infos = new ArrayList<ProgramInfo>();

			Date fromDate = new Date();
			SimpleDateFormat simple1 = new SimpleDateFormat("kk:mm");

			// 当前时间
			String timeStr = DateFormat.format("kk:mm",
					System.currentTimeMillis()).toString();
			try {
				fromDate = simple1.parse(timeStr);
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return null;
			}

			long curTime = fromDate.getTime();
			Boolean findFlag = false;

			for (Element link : links) {
				String[] pair = link.text().split(" ");
				if (pair.length < 2)
					continue;
				String time = pair[0].trim();
				String program = pair[1].trim();

				if (!findFlag) {
					listPosition++;
					try {
						fromDate = simple1.parse(time);
						/* 找到第一个比当前时间大的节目，而正在播放的实际是前一个节目 */
						if (fromDate.getTime() >= curTime) {
							findFlag = true;
						}
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				ProgramInfo info = new ProgramInfo(time, program, false);
				infos.add(info);
			}

			// 在listView中突出显示当前的播放节目
			if (!findFlag) {
				// FIXME bug#0022 有些节目预告有内容，但是不是真正的节目单，此时的失败是因为没有节目单
				if (infos.size() == 0) {
					return null;
				} else {
					// FIXME bug#0022 此处的没找到是因为有节目预告，但是处于24：00分左右的临界情况
					/* 如果没有大于当前时间值的节目，说明当日的最后一个节目就是当前播放的节目 */
					return "正在播出：" + infos.get(infos.size() - 1).getProgram();
				}
			} else if (listPosition == 1) {
				/* 如果第一个节目的时间指就大于当前时间，实际是前一天的最后一个节目，在新的一天什么都不显示 */
			} else {
				/* 其他正常情况，如果找到一个大于当前时间值的节目，置前一个节目为正在播放节目 */
				return "正在播出：" + infos.get(listPosition - 2).getProgram();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/* ====================================================== */
		return null;
	}

	// Task for the queue
	private class ProgramToLoad {
		public String url;
		public TextView textView;

		public ProgramToLoad(String u, TextView i) {
			url = u;
			textView = i;
		}
	}

	ProgramsQueue programsQueue = new ProgramsQueue();

	public void stopThread() {
		programLoaderThread.interrupt();
	}

	// stores list of photos to download
	class ProgramsQueue {
		private Stack<ProgramToLoad> programsToLoad = new Stack<ProgramToLoad>();

		// removes all instances of this ImageView
		public void Clean(TextView textView) {
			for (int j = 0; j < programsToLoad.size();) {
				if (programsToLoad.get(j).textView == textView)
					programsToLoad.remove(j);
				else
					++j;
			}
		}
	}

	class ProgramsLoader extends Thread {
		public void run() {
			try {
				while (true) {
					// thread waits until there are any images to load in the
					// queue
					if (programsQueue.programsToLoad.size() == 0)
						synchronized (programsQueue.programsToLoad) {
							programsQueue.programsToLoad.wait();
						}
					if (programsQueue.programsToLoad.size() != 0) {
						ProgramToLoad programToLoad;
						synchronized (programsQueue.programsToLoad) {
							programToLoad = programsQueue.programsToLoad.pop();
						}
						String string = getProgram(programToLoad.url);
						memoryCache.put(programToLoad.url, string);
						String tag = textViews.get(programToLoad.textView);
						if (tag != null && tag.equals(programToLoad.url)) {
							ProgramDisplayer bd = new ProgramDisplayer(string,
									programToLoad.textView);
							Activity a = (Activity) programToLoad.textView
									.getContext();
							a.runOnUiThread(bd);
						}
					}
					if (Thread.interrupted())
						break;
				}
			} catch (InterruptedException e) {
				// allow thread to exit
			}
		}
	}

	ProgramsLoader programLoaderThread = new ProgramsLoader();

	// Used to display bitmap in the UI thread
	class ProgramDisplayer implements Runnable {
		String string;
		TextView textView;

		public ProgramDisplayer(String b, TextView i) {
			string = b;
			textView = i;
		}

		public void run() {
			if (string != null)
				textView.setText(string);
			else
				textView.setText("");
		}
	}

	public void clearCache() {
		memoryCache.clear();
	}

}
