/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.adapters.viewholders

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat.setTransitionName
import androidx.recyclerview.widget.RecyclerView
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.MediaAdapterListener
import com.mainstreetcode.teammate.model.Media
import com.mainstreetcode.teammate.util.THUMBNAIL_SIZE
import com.mainstreetcode.teammate.util.extractPalette
import com.mainstreetcode.teammate.util.getTransitionName
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.tunjid.androidx.view.util.marginLayoutParams


abstract class MediaViewHolder<T : View> internal constructor(
        itemView: View,
        val delegate: MediaAdapterListener
) : RecyclerView.ViewHolder(itemView),
        ThumbnailHolder,
        Callback {

    lateinit var media: Media

    internal var fullResView: T
    @get:ColorInt

    var backgroundColor: Int = 0
        private set

    private val border: View
    private val container: ConstraintLayout
    final override val thumbnail: ImageView

    @get:IdRes
    abstract val thumbnailId: Int

    @get:IdRes
    abstract val fullViewId: Int

    init {
        backgroundColor = Color.BLACK
        border = itemView.findViewById(R.id.border)
        container = itemView.findViewById(R.id.container)

        @Suppress("LeakingThis")
        fullResView = itemView.findViewById(fullViewId)

        @Suppress("LeakingThis")
        thumbnail = itemView.findViewById(thumbnailId)

        val clickListener = View.OnClickListener { delegate.onMediaClicked(media) }

        itemView.setOnClickListener(clickListener)
        fullResView.setOnClickListener(clickListener)

        if (!delegate.isFullScreen) {
            itemView.setOnLongClickListener { performLongClick() }
            thumbnail.scaleType = ImageView.ScaleType.CENTER_CROP
            setUnityAspectRatio()
        } else {
            container.layoutParams.height = MATCH_PARENT
            val params = itemView.marginLayoutParams
            params.height = MATCH_PARENT
            params.bottomMargin = 0
            params.rightMargin = params.bottomMargin
            params.topMargin = params.rightMargin
            params.leftMargin = params.topMargin
        }
    }

    fun bind(media: Media) {
        this.media = media
        val delegate = this.delegate

        setTransitionName(itemView, media.getTransitionName(R.id.fragment_media_background))
        setTransitionName(thumbnail, media.getTransitionName(R.id.fragment_media_thumbnail))

        loadImage(media.thumbnail, false, thumbnail)
        if (!delegate.isFullScreen) highlightViewHolder(delegate::isSelected)
    }

    open fun fullBind(media: Media) = bind(media)

    open fun unBind() {}

    fun performLongClick(): Boolean {
        val delegate = this.delegate
        highlightViewHolder(delegate::onMediaLongClicked)
        return true
    }

    internal fun loadImage(url: String, fitToSize: Boolean, destination: ImageView) {
        val isFullScreen = delegate.isFullScreen
        if (url.isBlank()) return

        var creator = Picasso.get().load(url)

        if (!isFullScreen) creator.placeholder(R.drawable.bg_image_placeholder)

        creator = if (fitToSize) creator.fit() else creator.resize(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
        creator = creator.centerInside()

        val callBack = if (isFullScreen && fitToSize) this
        else PaletteCallBack(destination) { color -> onFillExtracted(color, if (isFullScreen) itemView else thumbnail) }

        creator.into(destination, callBack)
    }

    private fun setUnityAspectRatio() {
        val set = ConstraintSet()

        set.clone(container)
        set.setDimensionRatio(thumbnail.id, UNITY_ASPECT_RATIO)
        set.setDimensionRatio(fullResView.id, UNITY_ASPECT_RATIO)
        set.applyTo(container)
    }

    private fun highlightViewHolder(selectionFunction: (Media) -> Boolean) {
        val isSelected = selectionFunction.invoke(media)
        border.visibility = if (isSelected) View.VISIBLE else View.GONE
        scale(isSelected)
    }

    private fun scale(isSelected: Boolean) {
        val end = if (isSelected) FOUR_FIFTH_SCALE else FULL_SCALE

        val set = AnimatorSet()
        val scaleDownX = animateProperty(SCALE_X_PROPERTY, container.scaleX, end)
        val scaleDownY = animateProperty(SCALE_Y_PROPERTY, container.scaleY, end)

        set.playTogether(scaleDownX, scaleDownY)
        set.start()
    }

    private fun onFillExtracted(@ColorInt colorFill: Int, destination: View) {
        destination.setBackgroundColor(colorFill)
        delegate.onFillLoaded()
    }

    private fun animateProperty(property: String, start: Float, end: Float): ObjectAnimator =
            ObjectAnimator.ofFloat(container, property, start, end).setDuration(DURATION.toLong())

    override fun onSuccess() {}

    override fun onError(e: Exception) {}

    private class PaletteCallBack internal constructor(private val destination: ImageView, private val integerConsumer: (Int) -> Unit) : Callback {

        @SuppressLint("CheckResult")
        override fun onSuccess() {

            extractPalette(destination).map { palette -> palette.getDarkMutedColor(Color.BLACK) }
                    .subscribe(integerConsumer::invoke, Throwable::printStackTrace)
        }

        override fun onError(e: Exception) {}
    }

    companion object {

        private const val UNITY_ASPECT_RATIO = "1"
        private const val SCALE_X_PROPERTY = "scaleX"
        private const val SCALE_Y_PROPERTY = "scaleY"
        private const val FULL_SCALE = 1f
        private const val FOUR_FIFTH_SCALE = 0.8f
        private const val DURATION = 200
    }
}
