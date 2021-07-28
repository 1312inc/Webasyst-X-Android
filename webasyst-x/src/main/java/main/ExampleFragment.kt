package com.webasyst.x.main

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.webasyst.x.R

class ExampleFragment : Fragment(R.layout.frag_example) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.textView).text = arguments?.getString(TEXT)
    }

    companion object {
        private const val TEXT = "text"

        fun newInstance(text: String): ExampleFragment {
            val bundle = Bundle()
            bundle.putString(TEXT, text)
            val fragment = ExampleFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}
