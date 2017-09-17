package com.natcom.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.BottomNavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import com.google.gson.reflect.TypeToken
import com.natcom.*
import com.natcom.model.Lead
import com.natcom.network.ListResult
import com.natcom.network.NetworkController
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotterknife.bindView
import java.lang.reflect.Type
import java.util.*


class ListActivity : AppCompatActivity(), ListResult, View.OnClickListener, View.OnLongClickListener {

    val navigation by bindView<BottomNavigationView>(R.id.navigation)
    val list by bindView<RecyclerView>(R.id.list)
    val progress by bindView<ProgressBar>(R.id.progress)
    val empty_list by bindView<TextView>(R.id.empty_list)
    var type: ListType? = null
        set(value) {
            field = value
            supportActionBar?.title = MyApp.instance.getString(field!!.iname)
        }
    var param: String? = null
    var searchMenuItem: MenuItem? = null

    private val jobHolder = JobHolder()

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

            searchMenuItem?.collapseActionView()

            if (type == ListType.DATE) {
                val date = Calendar.getInstance()
                DatePickerDialog(this, { _, year, month, day ->
                    this.type = ListType.DATE
                    this.param = prepareDate(year, month, day)
                    update(true)
                }, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)).show()
            } else {
                if (this.type != type) {
                    this.type = type
                    update(true)
                } else {
                    update()
                }
            }
            true
        }

        savedInstanceState?.let {
            type = it.getSerializable(LIST_TYPE_KEY) as ListType? ?: ListType.TODAY
            param = it.getString(PARAM_KEY)
        }

        type = type ?: ListType.TODAY

        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL
        list.layoutManager = llm
        val dividerItemDecoration = DividerItemDecoration(list.context, llm.orientation)
        list.addItemDecoration(dividerItemDecoration)

        NetworkController.listCallback = this

        update()
    }

    override fun onDestroy() {
        super.onDestroy()

        jobHolder.dispose()
        NetworkController.listCallback = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putSerializable(LIST_TYPE_KEY, type)
        param?.let { outState.putString(PARAM_KEY, it) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        searchMenuItem = menu.findItem(R.id.action_search)
        val searchView = searchMenuItem?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?) = true
        })
        return true
    }

    private fun openLead(lead: Lead) {
        val intent = Intent(this, LeadActivity::class.java)
        intent.putExtra(LEAD_KEY, lead)
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && data?.action.equals(UPDATE_LIST)) {
            update(force = true)
        }
    }

    fun update(force: Boolean = false) {
        type?.let {
            if (it == ListType.TODAY || it == ListType.TOMORROW) {
                NetworkController.list(it, reset = force)
            } else {
                param?.let { p -> NetworkController.list(it, p, force) } ?: return
            }
        }
        progress.visibility = View.VISIBLE
        empty_list.visibility = View.GONE
        list.visibility = View.GONE
    }

    override fun onListResult(type: ListType, success: Boolean, list: List<Lead>?) {
        progress.visibility = View.GONE
        val newList: List<Lead>?
        if (!success) {
            toast(R.string.error)
            newList = loadList(type)
        } else {
            saveList(type, list)
            newList = list
        }

        newList?.let {
            if (it.isEmpty()) {
                empty_list.visibility = View.VISIBLE
                this.list.visibility = View.GONE
            } else {
                empty_list.visibility = View.GONE
                this.list.visibility = View.VISIBLE
                this.list.swapAdapter(ListAdapter(it, this), false)
            }
        }
    }

    private fun saveList(type: ListType, list: List<Lead>?) {
        if (list == null || type == ListType.DATE || type == ListType.SEARCH) return
        val value = gson.toJson(list)
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(LIST_KEY + type.toString(), value)
                .apply()
    }

    private fun loadList(type: ListType): List<Lead>? {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        if (!sp.contains(LIST_KEY + type.toString())) return null
        val t: Type = object : TypeToken<List<Lead>>() {}.type
        return gson.fromJson(sp.getString(LIST_KEY + type.toString(), ""), t)
    }

    override fun onClick(v: View?) {
        val itemPosition = list.getChildLayoutPosition(v)
        openLead((list.adapter as ListAdapter).list[itemPosition])
    }

    override fun onLongClick(v: View?): Boolean {
        val itemPosition = list.getChildLayoutPosition(v)
        val item = (list.adapter as ListAdapter).list[itemPosition]

        AlertDialog.Builder(this)
                .setMessage(R.string.assign)
                .setPositiveButton(R.string.ok, { _, _ -> assign(item.id) })
                .setNegativeButton(R.string.cancel, null)
                .show()
        return true
    }

    private fun assign(id: Int) {
        val job = launch(UI) {
            val result = NetworkController.api.assign(id).awaitResponse()
            if (!result.isSuccessful()) {
                toast(R.string.error)
                return@launch
            }
            toast(R.string.assign_success)
            update()
        }
        jobHolder.add(job)
    }

}

enum class ListType(val iname: Int) {
    TODAY(R.string.today),
    TOMORROW(R.string.tomorrow),
    DATE(R.string.date),
    SEARCH(R.string.search)
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
        holder.picture.text = lead.company.subSequence(0, 1)
        holder.picture.setTextColor(ContextCompat.getColor(listActivity, when (lead.company[0]) {
            'Б' -> R.color.b
            'М' -> R.color.m
            'Н' -> R.color.n
            else -> R.color.black
        }))
        holder.address.text = lead.address
        holder.status.text = lead.status
        holder.responsible.text = lead.responsible
        holder.responsible.setTextColor(ContextCompat.getColor(listActivity, when (lead.color) {
            0 -> R.color.gray
            1 -> R.color.green
            2 -> R.color.red
            else -> R.color.black
        }))
    }

    override fun getItemCount() = list.size

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val picture = view.findViewById(R.id.picture) as TextView
        val address = view.findViewById(R.id.address) as TextView
        val status = view.findViewById(R.id.status) as TextView
        val responsible = view.findViewById(R.id.responsible) as TextView
    }
}