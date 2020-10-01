[ ![Download](https://api.bintray.com/packages/ahmerafzal/maven/AhmerPDFViewer/images/download.svg) ](https://bintray.com/ahmerafzal/maven/AhmerPDFViewer/_latestVersion)

# AhmerPdfViewer
Android view for displaying PDFs rendered with PdfiumAndroid from API 19

## Installation

Add to _build.gradle_:

`implementation 'com.github.ahmerafzal1:AhmerPDFViewer:2.7.3'`

or if you want to use more stable version:
 
`implementation 'com.github.ahmerafzal1:AhmerPdfium:7.6.4''`

## Include PDFView in your layout

``` xml
<com.ahmer.afzal.pdfviewer.PDFView
        android:id="@+id/pdfView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```

## Load a PDF file

All available options with default values:
``` java
pdfView.fromUri(Uri)
or
pdfView.fromFile(File)
or
pdfView.fromBytes(byte[])
or
pdfView.fromStream(InputStream) // stream is written to bytearray - native code cannot use Java Streams
or
pdfView.fromSource(DocumentSource)
or
pdfView.fromAsset(String)
    .pages(0, 2, 1, 3, 3, 3) // all pages are displayed by default
    .enableSwipe(true) // allows to block changing pages using swipe
    .swipeHorizontal(false)
    .enableDoubletap(true)
    .defaultPage(0)     
    .onDraw(onDrawListener) // allows to draw something on the current page, usually visible in the middle of the screen    
    .onDrawAll(onDrawListener) // allows to draw something on all pages, separately for every page. Called only for visible pages
    .onLoad(onLoadCompleteListener) // called after document is loaded and starts to be rendered
    .onPageChange(onPageChangeListener)
    .onPageScroll(onPageScrollListener)
    .onError(onErrorListener)
    .onPageError(onPageErrorListener)
    .onRender(onRenderListener) // called after document is rendered for the first time    
    .onTap(onTapListener) // called on single tap, return true if handled, false to toggle scroll handle visibility
    .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
    .password(null)
    .scrollHandle(null)
    .enableAntialiasing(true) // improve rendering a little bit on low-res screens    
    .spacing(0) // spacing between pages in dp. To define spacing color, set view background
    .autoSpacing(false) // add dynamic spacing to fit each page on its own on the screen
    .linkHandler(DefaultLinkHandler)
    .pageFitPolicy(FitPolicy.WIDTH) // mode to fit pages in the view
    .fitEachPage(true) // fit each page to the view, else smaller pages are scaled relative to largest page.
    .nightMode(false) // toggle night mode
    .pageSnap(true) // snap pages to screen boundaries
    .pageFling(false) // make a fling change only a single page like ViewPager
    .load();
```

* `pages` is optional, it allows you to filter and order the pages of the PDF as you need

## Scroll handle

**PDFView** in **RelativeLayout** to use **ScrollHandle** is not required, you can use any layout.

To use scroll handle just register it using method `Configurator#scrollHandle()`.
This method accepts implementations of **ScrollHandle** interface.

There is default implementation shipped with AndroidPdfViewer, and you can use it with
`.scrollHandle(new DefaultScrollHandle(this))`.
**DefaultScrollHandle** is placed on the right (when scrolling vertically) or on the bottom (when scrolling horizontally).
By using constructor with second argument (`new DefaultScrollHandle(this, true)`), handle can be placed left or top.

You can also create custom scroll handles, just implement **ScrollHandle** interface.
All methods are documented as Javadoc comments on interface [source](https://github.com/AhmerAfzal1/AhmerPDF/blob/master/AhmerPDFViewer/src/main/java/com/ahmer/afzal/pdfviewer/scroll/ScrollHandle.java).

## Document sources
_Document sources_, which are just providers for PDF documents.
Every provider implements **DocumentSource** interface.
Predefined providers are available in **com.ahmer.afzal.pdfviewer.source.DocumentSource** package and can be used as
samples for creating custom ones.

Predefined providers can be used with shorthand methods:
```
pdfView.fromUri(Uri)
pdfView.fromFile(File)
pdfView.fromBytes(byte[])
pdfView.fromStream(InputStream)
pdfView.fromAsset(String)
```
Custom providers may be used with `pdfView.fromSource(DocumentSource)` method.

## Links
By default, **DefaultLinkHandler** is used and clicking on link that references page in same document causes jump to destination page
and clicking on link that targets some URI causes opening it in default application.

You can also create custom link handlers, just implement **LinkHandler** interface and set it using
`Configurator#linkHandler(LinkHandler)` method. Take a look at [DefaultLinkHandler](https://github.com/AhmerAfzal1/AhmerPDF/blob/master/AhmerPDFViewer/src/main/java/com/ahmer/afzal/pdfviewer/scroll/DefaultScrollHandle.java)
source to implement custom behavior.

## Pages fit policy
Library supports fitting pages into the screen in 3 modes:
* WIDTH - width of widest page is equal to screen width
* HEIGHT - height of highest page is equal to screen height
* BOTH - based on widest and highest pages, every page is scaled to be fully visible on screen

Apart from selected policy, every page is scaled to have size relative to other pages.

Fit policy can be set using `Configurator#pageFitPolicy(FitPolicy)`. Default policy is **WIDTH**.

## Additional options

### Bitmap quality
By default, generated bitmaps are _compressed_ with `RGB_565` format to reduce memory consumption.
Rendering with `ARGB_8888` can be forced by using `pdfView.useBestQuality(true)` method.

### Double tap zooming
There are three zoom levels: min (default 1), mid (default 1.75) and max (default 3). On first double tap,
view is zoomed to mid level, on second to max level, and on third returns to min level.
If you are between mid and max levels, double tapping causes zooming to max and so on.

Zoom levels can be changed using following methods:

``` java
void setMinZoom(float zoom);
void setMidZoom(float zoom);
void setMaxZoom(float zoom);
```

### Why I cannot open PDF from URL?
Downloading files is long running process which must be aware of Activity lifecycle, must support some configuration, 
data cleanup and caching, so creating such module will probably end up as new library.

### How can I show last opened page after configuration change?
You have to store current page number and then set it with `pdfView.defaultPage(page)`, refer to sample app

### How can I fit document to screen width (eg. on orientation change)?
Use `FitPolicy.WIDTH` policy or add following snippet when you want to fit desired page in document with different page sizes:
``` java
Configurator.onRender(new OnRenderListener() {
    @Override
    public void onInitiallyRendered(int pages, float pageWidth, float pageHeight) {
        pdfView.fitToWidth(pageIndex);
    }
});
```

### How can I scroll through single pages like a ViewPager?
You can use a combination of the following settings to get scroll and fling behaviour similar to a ViewPager:
``` java
.swipeHorizontal(true)
.pageSnap(true)
.autoSpacing(true)
.pageFling(true)
```

## License

Created with the help of [AndroidPdfViewer](https://github.com/barteksc/AndroidPdfViewer/) by Bartosz Schiller
```
Copyright 2018 Ahmer Afzal

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

