package app.grapheneos.pdfviewer

import android.webkit.JavascriptInterface
import app.grapheneos.pdfviewer.viewModel.PdfViewModel

class PdfJsChannel(private val viewModel: PdfViewModel) {

    companion object {
        internal const val MIN_ZOOM_RATIO = 0.2f
        internal const val MAX_ZOOM_RATIO = 10f
        private const val MAX_RENDER_PIXELS = 8_388_608
    }

    @JavascriptInterface
    fun setHasDocumentOutline(hasOutline: Boolean) {
        viewModel.setHasOutline(hasOutline)
    }

    @JavascriptInterface
    fun setDocumentOutline(outline: String) {
        viewModel.parseOutlineString(outline)
    }

    @JavascriptInterface
    fun getPage(): Int = viewModel.page.value

    @JavascriptInterface
    fun setCurrentPage(page: Int) {
        // The WebView is treated as untrusted, so reject impossible pages.
        if (page !in 1..viewModel.numPages.value) return
        viewModel.setPage(page)
        viewModel.showPageIndicator()
    }

    @JavascriptInterface
    fun getZoomRatio(): Float = viewModel.zoomRatio.value

    @JavascriptInterface
    fun setZoomRatio(ratio: Float) {
        viewModel.setZoomRatio(ratio.coerceIn(MIN_ZOOM_RATIO, MAX_ZOOM_RATIO))
    }

    @JavascriptInterface
    fun getMaxRenderPixels(): Int = MAX_RENDER_PIXELS

    @JavascriptInterface
    fun getZoomFocusX(): Float = viewModel.zoomFocusX

    @JavascriptInterface
    fun getZoomFocusY(): Float = viewModel.zoomFocusY

    @JavascriptInterface
    fun getMinZoomRatio(): Float = MIN_ZOOM_RATIO

    @JavascriptInterface
    fun getMaxZoomRatio(): Float = MAX_ZOOM_RATIO

    @JavascriptInterface
    fun getInsetLeft(): Float = viewModel.insetLeft

    @JavascriptInterface
    fun getInsetTop(): Float = viewModel.insetTop

    @JavascriptInterface
    fun getInsetRight(): Float = viewModel.insetRight

    @JavascriptInterface
    fun getInsetBottom(): Float = viewModel.insetBottom

    @JavascriptInterface
    fun getInsetIme(): Float = viewModel.insetIme

    @JavascriptInterface
    fun getDocumentOrientationDegrees(): Int = viewModel.documentOrientationDegrees.value

    @JavascriptInterface
    fun getPageFitMode(): Int = viewModel.pageFitMode.value

    @JavascriptInterface
    fun getContinuousMode(): Boolean = viewModel.continuousMode.value

    @JavascriptInterface
    fun getDarkDocument(): Boolean = viewModel.darkDocument.value

    @JavascriptInterface
    fun setNumPages(numPages: Int) {
        viewModel.setNumPages(numPages)
    }

    @JavascriptInterface
    fun setDocumentProperties(properties: String) {
        if (!viewModel.documentPropertiesLoaded.compareAndSet(false, true)) {
            throw SecurityException("setDocumentProperties already called")
        }
        val numPages = viewModel.numPages.value
        val uri = viewModel.uri.value ?: return
        viewModel.retrieveDocumentProperties(properties, numPages, uri)
    }

    @JavascriptInterface
    fun showPasswordPrompt() {
        viewModel.requestPasswordPrompt()
    }

    @JavascriptInterface
    fun invalidPassword() {
        viewModel.invalidPassword()
    }

    @JavascriptInterface
    fun onLoaded() {
        viewModel.validated()
    }

    @JavascriptInterface
    fun onLoadError() {
        viewModel.handleLoadError()
    }

    @JavascriptInterface
    fun getPassword(): String = viewModel.encryptedDocumentPassword

    /**
     * One page of extracted text, as a JSON array of per-item strings. Called on a WebView
     * binder thread. Returns false once the index is full, which stops the extraction loop.
     */
    @JavascriptInterface
    fun setPageText(page: Int, itemsJson: String, generation: Int): Boolean =
        viewModel.setPageText(page, itemsJson, generation)

    /**
     * Match positions on one page, for highlighting, as nested arrays of
     * [itemIndex, offsetInItem, length]. With continuous scrolling several pages are
     * on screen, so each page asks for its own when its text layer is drawn. Only
     * numbers cross: the query itself never reaches the WebView.
     */
    @JavascriptInterface
    fun getSearchMatches(page: Int): String = viewModel.searchMatchesFor(page)
}
