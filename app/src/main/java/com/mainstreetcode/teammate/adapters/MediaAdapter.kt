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

package com.mainstreetcode.teammate.adapters

import android.view.ViewGroup

import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.viewholders.AdViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.ContentAdViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.ImageMediaViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.InstallAdViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.MediaViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.VideoMediaViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.bind
import com.mainstreetcode.teammate.model.Ad
import com.mainstreetcode.teammate.model.Media
import com.tunjid.androidx.recyclerview.InteractiveAdapter
import com.tunjid.androidx.recyclerview.InteractiveViewHolder
import com.tunjid.androidx.recyclerview.diff.Differentiable

import com.mainstreetcode.teammate.util.CONTENT_AD
import com.mainstreetcode.teammate.util.INSTALL_AD
import com.mainstreetcode.teammate.util.MEDIA_IMAGE
import com.mainstreetcode.teammate.util.MEDIA_VIDEO
import com.tunjid.androidx.view.util.inflate

/**
 * Adapter for [Media]
 */

class MediaAdapter(
        private val media: () -> List<Differentiable>,
        listener: MediaAdapterListener
) : InteractiveAdapter<InteractiveViewHolder<*>, MediaAdapter.MediaAdapterListener>(listener) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): InteractiveViewHolder<*> = when (viewType) {
                CONTENT_AD -> ContentAdViewHolder(viewGroup.inflate(R.layout.viewholder_grid_content_ad), delegate)
                INSTALL_AD -> InstallAdViewHolder(viewGroup.inflate(R.layout.viewholder_grid_install_ad), delegate)
                MEDIA_IMAGE -> ImageMediaViewHolder(viewGroup.inflate(R.layout.viewholder_image), delegate)
                else -> VideoMediaViewHolder(viewGroup.inflate(R.layout.viewholder_video), delegate)
            }

    override fun onBindViewHolder(viewHolder: InteractiveViewHolder<*>, position: Int) {
        when (val item = media()[position]) {
            is Media -> (viewHolder as MediaViewHolder<*>).bind(item)
            is Ad<*> -> (viewHolder as AdViewHolder<*>).bind(item)
        }
    }

    override fun getItemCount(): Int = media().size

    override fun getItemViewType(position: Int): Int {
        val identifiable = media()[position]
        return if (identifiable is Media) if (identifiable.isImage) MEDIA_IMAGE else MEDIA_VIDEO else (identifiable as Ad<*>).type
    }

    override fun getItemId(position: Int): Long = media()[position].hashCode().toLong()

    interface MediaAdapterListener {

        val isFullScreen: Boolean

        fun onFillLoaded() {}

        fun onMediaClicked(item: Media)

        fun onMediaLongClicked(media: Media): Boolean

        fun isSelected(media: Media): Boolean
    }
}