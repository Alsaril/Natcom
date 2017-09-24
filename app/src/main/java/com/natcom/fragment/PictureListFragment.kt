package com.natcom.fragment

import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.natcom.R
import com.natcom.activity.LeadController
import com.natcom.model.Picture
import java.util.*


class PictureListFragment : Fragment(), View.OnClickListener {
    private lateinit var leadController: LeadController
    lateinit var list: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        leadController = activity as LeadController
        return inflater.inflate(R.layout.picture_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.pictures)
        list = view.findViewById(R.id.list)

        list.setHasFixedSize(true)
        list.layoutManager = GridLayoutManager(context, resources.getInteger(R.integer.list_columns))
        list.addItemDecoration(ItemDecorationAlbumColumns(resources.getDimensionPixelSize(R.dimen.list_spacing), resources.getInteger(R.integer.list_columns)))

        list.swapAdapter(ListAdapter(leadController.lead().images, this), false)

    }

    override fun onClick(v: View?) = leadController.fullscreen(list.getChildLayoutPosition(v))
}

class ListAdapter(list: List<Picture>, private val pictureListFragment: PictureListFragment) : RecyclerView.Adapter<com.natcom.fragment.ListAdapter.MyViewHolder>() {
    val list: List<Picture> = Collections.unmodifiableList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val image = ImageView(pictureListFragment.context)
        image.setOnClickListener(pictureListFragment)
        return MyViewHolder(image)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(pictureListFragment).load(list[position].url).into(holder.image)
    }

    override fun getItemCount() = list.size

    class MyViewHolder(val image: ImageView) : RecyclerView.ViewHolder(image)
}

class ItemDecorationAlbumColumns(private val mSizeGridSpacingPx: Int, private val mGridSize: Int) : RecyclerView.ItemDecoration() {

    private var mNeedLeftSpacing = false

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val frameWidth = ((parent.width - mSizeGridSpacingPx.toFloat() * (mGridSize - 1)) / mGridSize).toInt()
        val padding = parent.width / mGridSize - frameWidth
        val itemPosition = (view.layoutParams as RecyclerView.LayoutParams).viewAdapterPosition
        if (itemPosition < mGridSize) {
            outRect.top = 0
        } else {
            outRect.top = mSizeGridSpacingPx
        }
        if (itemPosition % mGridSize == 0) {
            outRect.left = 0
            outRect.right = padding
            mNeedLeftSpacing = true
        } else if ((itemPosition + 1) % mGridSize == 0) {
            mNeedLeftSpacing = false
            outRect.right = 0
            outRect.left = padding
        } else if (mNeedLeftSpacing) {
            mNeedLeftSpacing = false
            outRect.left = mSizeGridSpacingPx - padding
            if ((itemPosition + 2) % mGridSize == 0) {
                outRect.right = mSizeGridSpacingPx - padding
            } else {
                outRect.right = mSizeGridSpacingPx / 2
            }
        } else if ((itemPosition + 2) % mGridSize == 0) {
            mNeedLeftSpacing = false
            outRect.left = mSizeGridSpacingPx / 2
            outRect.right = mSizeGridSpacingPx - padding
        } else {
            mNeedLeftSpacing = false
            outRect.left = mSizeGridSpacingPx / 2
            outRect.right = mSizeGridSpacingPx / 2
        }
        outRect.bottom = 0
    }
}