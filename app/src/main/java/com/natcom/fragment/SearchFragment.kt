package com.natcom.fragment

import android.os.Bundle
import com.natcom.LIST_TYPE_KEY

class SearchFragment : ListFragment() {
    fun update(query: String) {
        param = query
        update()
    }

    init {
        val args = Bundle()
        args.putSerializable(LIST_TYPE_KEY, ListType.SEARCH)
        arguments = args
    }
}