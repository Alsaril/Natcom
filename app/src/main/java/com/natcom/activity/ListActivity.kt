package com.natcom.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.natcom.*
import com.natcom.model.Lead
import com.natcom.network.AssignResult
import com.natcom.network.ListResult
import com.natcom.network.NetworkController
import kotterknife.bindView
import java.util.*

class ListActivity : AppCompatActivity(), ListResult, AssignResult, View.OnClickListener, View.OnLongClickListener {

    val navigation by bindView<BottomNavigationView>(R.id.navigation)
    val list by bindView<RecyclerView>(R.id.list)
    val progress by bindView<ProgressBar>(R.id.progress)
    val error by bindView<TextView>(R.id.error)
    var type: ListType? = null
        set(value) {
            field = value
            supportActionBar?.title = type.toString()
        }
    var param: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!auth(this)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.list_activity)

        val mActionBarToolbar = findViewById(R.id.toolbar_actionbar) as Toolbar
        setSupportActionBar(mActionBarToolbar)

        navigation.setOnNavigationItemSelectedListener {
            val type = when (it.itemId) {
                R.id.today -> ListType.TODAY
                R.id.tomorrow -> ListType.TOMORROW
                else -> ListType.DATE
            }
            if (type == ListType.DATE) {
                val date = Calendar.getInstance()
                DatePickerDialog(this, { view, year, month, day ->
                    run {
                        if (this.type != type) {
                            this.type = type
                            this.param = "$year.$month.$day"
                        }
                        update()
                    }
                }, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)).show()
            } else {
                if (this.type != type) {
                    this.type = type
                }
                update()
            }
            true
        }

        savedInstanceState?.let {
            type = it.getSerializable(LIST_TYPE_KEY) as ListType? ?: ListType.TODAY
            param = it.getString(PARAM_KEY)
        }

        if (type == null) type = ListType.TODAY

        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL
        list.layoutManager = llm

        NetworkController.listCallback = this
        NetworkController.assignCallback = this

        update()
    }

    override fun onDestroy() {
        super.onDestroy()
        NetworkController.listCallback = null
        NetworkController.assignCallback = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putSerializable(LIST_TYPE_KEY, type)
        param?.let { outState.putString(PARAM_KEY, it) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        val searchMenuItem = menu.findItem(R.id.action_search)
        val mSearchView = searchMenuItem.actionView as SearchView
        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                param = query
                update()
                return true
            }

            override fun onQueryTextChange(newText: String?) = true
        })
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, object : MenuItemCompat.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                type = ListType.SEARCH
                Toast.makeText(this@ListActivity, "SEARCH", Toast.LENGTH_SHORT).show()
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                Toast.makeText(this@ListActivity, "UNSEARCH", Toast.LENGTH_SHORT).show()
                return true
            }
        })
        return true
    }

    fun openLead(lead: Lead) {
        val intent = Intent(this, LeadActivity::class.java)
        intent.putExtra(LEAD_KEY, lead)
        startActivity(intent)
    }

    fun update() {
        if (type == ListType.TODAY || type == ListType.TOMORROW) {
            NetworkController.list(type!!)
        } else if (param != null) {
            NetworkController.list(type!!, param!!)
        } else {
            return
        }
        progress.visibility = View.VISIBLE
        list.visibility = View.GONE
        error.visibility = View.GONE
    }

    override fun onListResult(success: Boolean, list: List<Lead>?) {
        progress.visibility = View.GONE
        if (success && list != null) {
            this.list.visibility = View.VISIBLE
            this.list.swapAdapter(ListAdapter(list, this), false)
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
            error.visibility = View.VISIBLE
        }
    }

    override fun onClick(v: View?) {
        val itemPosition = list.getChildLayoutPosition(v)
        val item = (list.adapter as ListAdapter?)?.list?.get(itemPosition)
        item?.let { openLead(it) }
    }

    override fun onLongClick(v: View?): Boolean {
        val itemPosition = list.getChildLayoutPosition(v)
        val item = (list.adapter as ListAdapter?)?.list?.get(itemPosition) ?: return false

        AlertDialog.Builder(this).setMessage("Assign?")
                .setPositiveButton("Yes", { dialog, id ->
                    NetworkController.assign(item.id)
                })
                .setNegativeButton("No", { dialog, id ->
                }).show()
        return true
    }

    override fun onAssignResult(success: Boolean) {
        if (!success) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Assign successful!", Toast.LENGTH_SHORT).show()
            update()
        }
    }
}

enum class ListType {
    TODAY, TOMORROW, DATE, SEARCH
}

class ListAdapter(list: List<Lead>, private val listActivity: ListActivity) : RecyclerView.Adapter<ListAdapter.MyViewHolder>() {

    val list: List<Lead> = Collections.unmodifiableList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item, parent, false)
        itemView.setOnClickListener(listActivity)
        itemView.setOnLongClickListener(listActivity)
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