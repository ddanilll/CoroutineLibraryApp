package com.example.coroutineslibraryapp.activity.recycler

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coroutineslibraryapp.actitvity.MainActivity
import com.example.coroutineslibraryapp.activity.recycler.adapters.vh.HeaderViewHolder
import com.example.coroutineslibraryapp.activity.recycler.adapters.LibraryAdapter
import com.example.coroutineslibraryapp.databinding.FragmentLibraryBinding
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.launch

class LibraryFragment : Fragment() {
    private var _binding: FragmentLibraryBinding? = null
    private val binding: FragmentLibraryBinding
        get() = requireNotNull(_binding) { "Binding is null. Fragment view is destroyed or not created yet." }

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: LibraryAdapter
    private var itemClickListener: LibraryAdapter.OnItemClickListener? = null

    private var shimmerContainer: ShimmerFrameLayout? = null
    private var isShimmering: Boolean = false

    fun setOnItemClickListener(listener: LibraryAdapter.OnItemClickListener) {
        this.itemClickListener = listener
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is LibraryAdapter.OnItemClickListener) {
            itemClickListener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shimmerContainer = binding.shimmerView

        setupRecyclerView()
        setupObservers()
        setupItemTouchHelper()

        binding.createButton.setOnClickListener {
            showCreateDialog()
        }

        viewModel.scrollToPosition.observe(viewLifecycleOwner) { position ->
            binding.recyclerView.post {
                if (position != null) {
                    binding.recyclerView.smoothScrollToPosition(position)
                }
            }
        }

        if (savedInstanceState != null) {
            isShimmering = savedInstanceState.getBoolean(SHIMMER_STATE, false)
            if (isShimmering) {
                showShimmer()
            } else {
                hideShimmer()
            }
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SHIMMER_STATE, isShimmering)
    }

    private fun setupRecyclerView() {
        adapter = LibraryAdapter(mutableListOf(), itemClickListener)
        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@LibraryFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.items.collect { items ->
                    adapter.updateList(items)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { isLoading ->
                    if (isLoading) {
                        showShimmer()
                    } else {
                        hideShimmer()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { error ->
                    error?.let { showError(it) }
                }
            }
        }
    }

    private fun showShimmer() {
        with(binding) {
            shimmerContainer?.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            shimmerContainer?.startShimmer()
            isShimmering = true
        }
    }

    private fun hideShimmer() {
        with(binding) {
            shimmerContainer?.stopShimmer()
            shimmerContainer?.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            isShimmering = false
        }
    }

    private fun showError(message: String) {
        AlertDialog.Builder(requireContext()).setTitle("Ошибка").setMessage(message)
            .setPositiveButton("Повторить") { _, _ ->
                viewModel.loadInitialData()
            }.setNegativeButton("Отмена", null).show()
    }

    private fun showCreateDialog() {
        val items = listOf(BOOK, NEWSPAPER, DISK)
        AlertDialog.Builder(requireContext()).setTitle("Создать новый элемент")
            .setItems(items.toTypedArray()) { _, which ->
                (activity as? MainActivity)?.createNewItem(items[which])
            }.show()
    }

    private fun setupItemTouchHelper() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    viewModel.removeItem(position)
                }
            }

            override fun getSwipeDirs(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder
            ): Int {
                return if (viewHolder is HeaderViewHolder) 0 else super.getSwipeDirs(
                    recyclerView,
                    viewHolder
                )
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.recyclerView)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    companion object {
        const val BOOK = "BOOK"
        const val NEWSPAPER = "NEWSPAPER"
        const val DISK = "DISK"
        const val SHIMMER_STATE = "SHIMMER_STATE"
    }
}
