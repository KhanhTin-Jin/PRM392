package com.example.moviecatalog.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpaceItemDecoration(private val spacePx: Int): RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, v: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.set(spacePx, spacePx, spacePx, spacePx)
    }
}
