package com.natcom.fragment

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.natcom.R
import com.natcom.activity.LeadController
import com.natcom.activity.ListAdapter
import com.natcom.model.Lead
import kotterknife.bindView
import java.util.*


class PictureListFragment : BoundFragment(), View.OnClickListener {
    val list by bindView<RecyclerView>(R.id.list)

    lateinit var leadController: LeadController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initFragment(inflater.inflate(R.layout.shift_fragment, container, false), "123")

        leadController = activity as LeadController

        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
        list.layoutManager = llm
        val dividerItemDecoration = DividerItemDecoration(list.getContext(),
                llm.orientation)
        list.addItemDecoration(dividerItemDecoration)

        update()

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    fun update() {
        //this.list.swapAdapter(ListAdapter(it, this), false)
    }

    override fun onClick(v: View?) {
        val itemPosition = list.getChildLayoutPosition(v)
        //openLead((list.adapter as ListAdapter).list[itemPosition])
    }
}

class ListAdapter(list: List<Lead>, private val pictureListFragment: PictureListFragment) : RecyclerView.Adapter<ListAdapter.MyViewHolder>() {
    val list: List<Lead> = Collections.unmodifiableList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item, parent, false)
        itemView.setOnClickListener(pictureListFragment)
        return ListAdapter.MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ListAdapter.MyViewHolder, position: Int) {
        val lead = list[position]
        holder.picture.text = lead.company.subSequence(0, 1)
        holder.picture.setTextColor(ContextCompat.getColor(pictureListFragment.context, when (lead.company[0]) {
            'Б' -> R.color.b
            'М' -> R.color.m
            'Н' -> R.color.n
            else -> R.color.black
        }))
        holder.company.text = lead.company
        holder.address.text = lead.address
        holder.status.text = lead.status
        holder.responsible.text = lead.responsible
        holder.responsible.setTextColor(ContextCompat.getColor(pictureListFragment.context, when (lead.color) {
            0 -> R.color.gray
            1 -> R.color.green
            2 -> R.color.red
            else -> R.color.black
        }))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val picture = view.findViewById(R.id.picture) as TextView
        val company = view.findViewById(R.id.company) as TextView
        val address = view.findViewById(R.id.address) as TextView
        val status = view.findViewById(R.id.status) as TextView
        val responsible = view.findViewById(R.id.responsible) as TextView
    }
}