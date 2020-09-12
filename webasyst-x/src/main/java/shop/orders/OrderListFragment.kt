package com.webasyst.x.shop.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.webasyst.x.databinding.FragShopOrderListBinding
import com.webasyst.x.main.MainFragment
import kotlinx.android.synthetic.main.frag_shop_order_list.orderListView

class OrderListFragment : Fragment() {
    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(
            this,
            OrderListViewModel.Factory(
                requireActivity().application,
                arguments?.getString(MainFragment.INSTALLATION_ID),
                arguments?.getString(MainFragment.INSTALLATION_URL)
            )
        )
            .get(OrderListViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragShopOrderListBinding.inflate(
        inflater,
        container,
        false
    ).let { binding ->
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = OrderListAdapter()
        orderListView.layoutManager = LinearLayoutManager(
            orderListView.context,
            LinearLayoutManager.VERTICAL,
            false
        )
        orderListView.adapter = adapter

        viewModel.orderList.observe(viewLifecycleOwner) { adapter.submitList(it) }
    }
}
