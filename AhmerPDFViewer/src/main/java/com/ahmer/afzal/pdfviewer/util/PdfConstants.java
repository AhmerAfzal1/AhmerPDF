package com.ahmer.afzal.pdfviewer.util;

public class PdfConstants {

    public static final boolean DEBUG_MODE = false;

    /**
     * Between 0 and 1, the thumbnails quality (default 0.3). Increasing this value may cause performance decrease
     */
    public static final float THUMBNAIL_RATIO = 0.6f; // Default 0.3f

    /**
     * The size of the rendered parts (default 256)
     * Tinier : a little bit slower to have the whole page rendered but more reactive.
     * Bigger : user will have to wait longer to have the first visual results
     */
    public static final float PART_SIZE = 384; // Default 256

    /**
     * Part of document above and below screen that should be preloaded, in dp
     */
    public static final int PRELOAD_OFFSET = 30; // Default 20

    /**
     * Max pages to load at the time, others are in queue
     */
    public static final int MAX_PAGES = 15;

    public static class Cache {

        /**
         * The size of the cache (number of bitmaps kept)
         */
        public static final int CACHE_SIZE = 150; // Default 150
        public static final int THUMBNAILS_CACHE_SIZE = 10; // Default 8
    }

    public static class Pinch {

        public static final float MAXIMUM_ZOOM = 10;
        public static final float MINIMUM_ZOOM = 1;
    }
}
