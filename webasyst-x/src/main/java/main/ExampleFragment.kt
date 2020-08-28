package com.webasyst.x.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.webasyst.x.R
import kotlinx.android.synthetic.main.frag_example.textView

class ExampleFragment : Fragment(R.layout.frag_example) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textView.text = arguments?.getString(TEXT)

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
