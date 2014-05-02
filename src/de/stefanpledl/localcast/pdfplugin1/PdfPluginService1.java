package de.stefanpledl.localcast.pdfplugin1;

import com.artifex.mupdfdemo.MuPDFCore;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import de.stefanpledl.localcast.pdfplugin.IPdfPlugin;

public class PdfPluginService1 extends Service {
	static final String LOG_TAG = "ResPluginService1";

	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	public void onDestroy() {
		super.onDestroy();
	}

	public IBinder onBind(Intent intent) {
		return binder;
	}

	MuPDFCore core;
	private final IPdfPlugin.Stub binder = new IPdfPlugin.Stub() {
		public Bitmap getPage(String path, int i) {

			try {
				PointF size = core.getPageSize(i);
				Bitmap bitmap = Bitmap.createBitmap(
						(int) size.x,
						(int) size.y, Bitmap.Config.ARGB_8888);
				core.drawPage(bitmap, i, (int) size.x,
						(int) size.y, 0, 0,
						(int) size.x,
						(int) size.y);
				return bitmap;
			} catch (Throwable ignored) {
			}
			return null;
		}

		String oldPath = "";

		@Override
		public int getPageCount(String path) throws RemoteException {
			try {
				if (core == null || !oldPath.equals(path))
					core = new MuPDFCore(getBaseContext(), path);
				oldPath = path;
				int pageCount = core.countPages();
				return pageCount;
			} catch (Throwable ignored) {
			}
			return 0;
		}
	};
}
