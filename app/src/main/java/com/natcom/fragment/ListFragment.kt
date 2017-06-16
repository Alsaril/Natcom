package com.natcom.fragment

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.natcom.LIST_TYPE_KEY
import com.natcom.PARAM_KEY
import com.natcom.activity.CHF
import com.natcom.model.Lead
import com.natcom.network.AssignResult
import com.natcom.network.ListResult
import com.natcom.network.NetworkController
import com.rv150.musictransfer.fragment.BoundFragment
import kotterknife.bindView
import natcom.com.natcom.R
import java.util.*


class ListFragment : BoundFragment(), ListResult, View.OnClickListener, View.OnLongClickListener, AssignResult {
    val list by bindView<RecyclerView>(R.id.list)
    var adapter: ListAdapter? = null
    var type: ListType? = null
    var param: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initView(inflater.inflate(R.layout.list_fragment, container, false))
        type = arguments?.getSerializable(LIST_TYPE_KEY) as ListType? ?: ListType.TODAY

        savedInstanceState?.let {
            type = it.getSerializable(LIST_TYPE_KEY) as ListType? ?: ListType.TODAY
            param = it.getString(PARAM_KEY)
        }
        NetworkController.listCallback = this
        NetworkController.assignCallback = this

        update()

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        NetworkController.listCallback = null
        NetworkController.assignCallback = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putSerializable(LIST_TYPE_KEY, type)
        param?.let { outState.putString(PARAM_KEY, it) }
    }

    fun update() {
        if (type == ListType.TODAY || type == ListType.TOMORROW) {
            NetworkController.list(type!!)
        } else if (param != null) {
            NetworkController.list(type!!, param!!)
        }
    }

    override fun onListResult(success: Boolean, list: List<Lead>?) {
        if (!success) {
            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show()
            return
        }
        if (list != null) {
            this.list.swapAdapter(ListAdapter(list, this), false)
        }
    }

    override fun onClick(v: View?) {
        val itemPosition = list.getChildLayoutPosition(v)
        val item = adapter?.list?.get(itemPosition)
        if (item != null) {
            (activity as CHF).openLead(item)
        }
    }

    override fun onLongClick(v: View?): Boolean {
        val itemPosition = list.getChildLayoutPosition(v)
        val item = adapter?.list?.get(itemPosition) ?: return false

        AlertDialog.Builder(activity).setMessage("Assign?")
                .setPositiveButton("Yes", { dialog, id ->
                    NetworkController.assign(item.id)
                })
                .setNegativeButton("No", { dialog, id ->
                }).show()
        return true
    }

    override fun onAssignResult(success: Boolean) {
        if (!success) {
            Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, "Assign successful!", Toast.LENGTH_SHORT).show()
            update()
        }
    }
}

enum class ListType {
    TODAY, TOMORROW, DATE, SEARCH
}

class ListAdapter(list: List<Lead>, private val listFragment: ListFragment) : RecyclerView.Adapter<ListAdapter.MyViewHolder>() {

    val list: List<Lead> = Collections.unmodifiableList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item, parent, false)
        itemView.setOnClickListener(listFragment)
        itemView.setOnLongClickListener(listFragment)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val lead = list[position]
        holder.company.text = lead.company
        holder.address.text = lead.address
        holder.status.text = lead.status

        /*val photos = service.getPhotos()
        if (!photos.isEmpty()) {
            val mainPhotoURL = NetHelper.BASE_URL + photos.get(0)
            Glide.with(feedFragment).load(mainPhotoURL).centerCrop().into(holder.photo)
        } else {
            Glide.with(feedFragment).load(R.drawable.default_pic).centerCrop().into(holder.photo)
        }*/
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val picture: ImageView = view.findViewById(R.id.picture) as ImageView
        val company: TextView = view.findViewById(R.id.company) as TextView
        val address: TextView = view.findViewById(R.id.address) as TextView
        val status: TextView = view.findViewById(R.id.status) as TextView
    }
}
