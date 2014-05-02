package de.stefanpledl.localcast.pdfplugin;

interface IPdfPlugin {
  Bitmap getPage( in String path, in int i );
  int getPageCount( in String path);
}
