package com.artifex.mupdfdemo;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;

import java.util.ArrayList;

public class MuPDFCore
{
	/* load our native library */
	static {
		System.loadLibrary("mupdf");
	}

	/* Readable members */
	private int numPages = -1;
	private float pageWidth;
	private float pageHeight;
	private long globals;
	private byte fileBuffer[];
	private String file_format;



    /* The native functions */
	private native long openFile(String filename);
	private native long openBuffer();
	private native String fileFormatInternal();
	private native int countPagesInternal();
	private native void gotoPageInternal(int localActionPageNum);
	private native float getPageWidth();
	private native float getPageHeight();
	private native void drawPage(Bitmap bitmap,
			int pageW, int pageH,
			int patchX, int patchY,
			int patchW, int patchH);
	private native void updatePageInternal(Bitmap bitmap,
			int page,
			int pageW, int pageH,
			int patchX, int patchY,
			int patchW, int patchH);
	private native RectF[] searchPage(String text);
	private native TextChar[][][][] text();
	private native byte[] textAsHtml();
	private native void addMarkupAnnotationInternal(PointF[] quadPoints, int type);
	private native void addInkAnnotationInternal(PointF[][] arcs);
	private native void deleteAnnotationInternal(int annot_index);
	private native int passClickEventInternal(int page, float x, float y);
	private native void setFocusedWidgetChoiceSelectedInternal(String [] selected);
	private native String [] getFocusedWidgetChoiceSelected();
	private native String [] getFocusedWidgetChoiceOptions();
	private native int getFocusedWidgetSignatureState();
	private native String checkFocusedSignatureInternal();
	private native boolean signFocusedSignatureInternal(String keyFile, String password);
	private native int setFocusedWidgetTextInternal(String text);
	private native String getFocusedWidgetTextInternal();
	private native int getFocusedWidgetTypeInternal();
	private native LinkInfo [] getPageLinksInternal(int page);
	private native RectF[] getWidgetAreasInternal(int page);
	private native Annotation[] getAnnotationsInternal(int page);
	private native OutlineItem [] getOutlineInternal();
	private native boolean hasOutlineInternal();
	private native boolean needsPasswordInternal();
	private native boolean authenticatePasswordInternal(String password);
	private native MuPDFAlertInternal waitForAlertInternal();
	private native void replyToAlertInternal(MuPDFAlertInternal alert);
	private native void startAlertsInternal();
	private native void stopAlertsInternal();
	private native void destroying();
	private native boolean hasChangesInternal();
	private native void saveInternal();

	public native boolean javascriptSupported();

	public MuPDFCore(Context context, String filename) throws Exception
	{
		globals = openFile(filename);
		if (globals == 0)
		{
			throw new Exception("ex");
		}
		file_format = fileFormatInternal();
	}

	public MuPDFCore(Context context, byte buffer[]) throws Exception
	{
		fileBuffer = buffer;
		globals = openBuffer();
		if (globals == 0)
		{
			throw new Exception("ex");
		}
		file_format = fileFormatInternal();
	}

	public  int countPages()
	{
		if (numPages < 0)
			numPages = countPagesSynchronized();

		return numPages;
	}

	public String fileFormat()
	{
		return file_format;
	}

	private synchronized int countPagesSynchronized() {
		return countPagesInternal();
	}

	/* Shim function */
	private void gotoPage(int page)
	{
		if (page > numPages-1)
			page = numPages-1;
		else if (page < 0)
			page = 0;
		gotoPageInternal(page);
		this.pageWidth = getPageWidth();
		this.pageHeight = getPageHeight();
	}

	public synchronized PointF getPageSize(int page) {
		gotoPage(page);
		return new PointF(pageWidth, pageHeight);
	}

	public MuPDFAlert waitForAlert() {
		MuPDFAlertInternal alert = waitForAlertInternal();
		return alert != null ? alert.toAlert() : null;
	}

	public void replyToAlert(MuPDFAlert alert) {
		replyToAlertInternal(new MuPDFAlertInternal(alert));
	}

	public void stopAlerts() {
		stopAlertsInternal();
	}

	public void startAlerts() {
		startAlertsInternal();
	}

	public synchronized void onDestroy() {
		destroying();
		globals = 0;
	}

	public synchronized void drawPage(Bitmap bm, int page,
			int pageW, int pageH,
			int patchX, int patchY,
			int patchW, int patchH) {
		gotoPage(page);
		drawPage(bm, pageW, pageH, patchX, patchY, patchW, patchH);
	}

	public synchronized void updatePage(Bitmap bm, int page,
			int pageW, int pageH,
			int patchX, int patchY,
			int patchW, int patchH) {
		updatePageInternal(bm, page, pageW, pageH, patchX, patchY, patchW, patchH);
	}



	public synchronized boolean setFocusedWidgetText(int page, String text) {
		boolean success;
		gotoPage(page);
		success = setFocusedWidgetTextInternal(text) != 0 ? true : false;

		return success;
	}

	public synchronized void setFocusedWidgetChoiceSelected(String [] selected) {
		setFocusedWidgetChoiceSelectedInternal(selected);
	}

	public synchronized String checkFocusedSignature() {
		return checkFocusedSignatureInternal();
	}

	public synchronized boolean signFocusedSignature(String keyFile, String password) {
		return signFocusedSignatureInternal(keyFile, password);
	}

	public synchronized LinkInfo [] getPageLinks(int page) {
		return getPageLinksInternal(page);
	}

	public synchronized RectF [] getWidgetAreas(int page) {
		return getWidgetAreasInternal(page);
	}

	public synchronized Annotation [] getAnnoations(int page) {
		return getAnnotationsInternal(page);
	}

	public synchronized RectF [] searchPage(int page, String text) {
		gotoPage(page);
		return searchPage(text);
	}

	public synchronized byte[] html(int page) {
		gotoPage(page);
		return textAsHtml();
	}

	public synchronized TextWord [][] textLines(int page) {
		gotoPage(page);
		TextChar[][][][] chars = text();

		// The text of the page held in a hierarchy (blocks, lines, spans).
		// Currently we don't need to distinguish the blocks level or
		// the spans, and we need to collect the text into words.
		ArrayList<TextWord[]> lns = new ArrayList<TextWord[]>();

		for (TextChar[][][] bl: chars) {
			if (bl == null)
				continue;
			for (TextChar[][] ln: bl) {
				ArrayList<TextWord> wds = new ArrayList<TextWord>();
				TextWord wd = new TextWord();

				for (TextChar[] sp: ln) {
					for (TextChar tc: sp) {
						if (tc.c != ' ') {
							wd.Add(tc);
						} else if (wd.w.length() > 0) {
							wds.add(wd);
							wd = new TextWord();
						}
					}
				}

				if (wd.w.length() > 0)
					wds.add(wd);

				if (wds.size() > 0)
					lns.add(wds.toArray(new TextWord[wds.size()]));
			}
		}

		return lns.toArray(new TextWord[lns.size()][]);
	}

	public synchronized void addMarkupAnnotation(int page, PointF[] quadPoints, Annotation.Type type) {
		gotoPage(page);
		addMarkupAnnotationInternal(quadPoints, type.ordinal());
	}

	public synchronized void addInkAnnotation(int page, PointF[][] arcs) {
		gotoPage(page);
		addInkAnnotationInternal(arcs);
	}

	public synchronized void deleteAnnotation(int page, int annot_index) {
		gotoPage(page);
		deleteAnnotationInternal(annot_index);
	}

	public synchronized boolean hasOutline() {
		return hasOutlineInternal();
	}

	public synchronized OutlineItem [] getOutline() {
		return getOutlineInternal();
	}

	public synchronized boolean needsPassword() {
		return needsPasswordInternal();
	}

	public synchronized boolean authenticatePassword(String password) {
		return authenticatePasswordInternal(password);
	}

	public synchronized boolean hasChanges() {
		return hasChangesInternal();
	}

	public synchronized void save() {
		saveInternal();
	}
}
