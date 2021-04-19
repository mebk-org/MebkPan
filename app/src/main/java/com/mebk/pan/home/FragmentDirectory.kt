package com.mebk.pan.home

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.work.WorkInfo
import com.bumptech.glide.Glide
import com.mebk.pan.R
import com.mebk.pan.aa.DirectoryRvAdapter
import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.utils.RetrofitClient
import com.mebk.pan.utils.SharePreferenceUtils
import com.mebk.pan.utils.ToolUtils
import com.mebk.pan.vm.DirectoryViewModel
import com.mebk.pan.vm.MainViewModel
import de.hdodenhof.circleimageview.CircleImageView

class FragmentDirectory : Fragment(), Toolbar.OnMenuItemClickListener {
    private lateinit var rv: RecyclerView
    private lateinit var sr: SwipeRefreshLayout
    private lateinit var toolbar: Toolbar
    private lateinit var circleIv: CircleImageView
    private val viewModel by viewModels<DirectoryViewModel>()
    private val mainViewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_directory, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list: MutableList<DirectoryDto.Object> = mutableListOf()

        rv = view.findViewById(R.id.fragment_directory_rv)
        sr = view.findViewById(R.id.fragment_directory_sr)
        toolbar = view.findViewById(R.id.fragment_directory_toolbar)

        toolbar.setOnMenuItemClickListener(this)
        circleIv = toolbar.findViewById(R.id.fragment_directory_toolbar_user_image)
        val uid = SharePreferenceUtils.getSharePreference(requireActivity()).getString(SharePreferenceUtils.SP_KEY_UID, "")
        if (!TextUtils.isEmpty(uid)) {
            Glide.with(requireActivity())
                    .load(ToolUtils.splitUrl("https://pan.mebk.org/api/v3/user/avatar/", uid!!, "/s"))
                    .placeholder(R.drawable.mine)
                    .dontAnimate()
                    .into(circleIv)
            LogUtil.err(this.javaClass, "url=${ToolUtils.splitUrl("https://pan.mebk.org/api/v3/user/avatar/", uid!!, "/s")}")
        }
        circleIv.setOnClickListener {
            findNavController().navigate(R.id.action_fragment_directory_to_fragmentUserInfo)
        }
        sr.setProgressViewEndTarget(true, 300)
        viewModel.directory()

        sr.isRefreshing = true

        val adapter = context?.let { DirectoryRvAdapter(it, list) }
        rv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rv.adapter = adapter
        (rv.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        viewModel.directoryInfo.observe(viewLifecycleOwner, Observer {
            LogUtil.err(this::class.java, "更新列表")
            list.clear()
            list.addAll(it)
            adapter?.notifyDataSetChanged()
            sr.isRefreshing = false
        })

        viewModel.requestInfo.observe(viewLifecycleOwner, Observer {
            if (it != RetrofitClient.REQUEST_SUCCESS) {
                Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
                sr.isRefreshing = false
                list.removeAt(0)
                adapter?.notifyItemRemoved(0)
            }
        })


        sr.setOnRefreshListener {
            val refreshDto = DirectoryDto.Object(viewModel.lastRefreshTimeInfo.value!!, "0", "正在刷新...", "", "", 0, "refresh")
            list.add(0, refreshDto)
            rv.scrollToPosition(0)
            adapter?.notifyItemInserted(0)
            sr.isRefreshing = true
            viewModel.directory(true)
        }

        adapter?.let { directoryRvAdapter ->
            directoryRvAdapter.setOnClickListener {
                if (sr.isRefreshing) {
                    Toast.makeText(context, "正在刷新，请稍后", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!adapter.isFileOperator) {
                    val bundle = Bundle()
                    bundle.putString("path", viewModel.directoryInfo.value!![it].path)
                    when (viewModel.directoryInfo.value!![it].type) {
                        "dir" -> {
                            bundle.putString("name", viewModel.directoryInfo.value!![it].name)
                            findNavController().navigate(R.id.action_fragment_directory_to_fragment_internal_file, bundle)
                        }
                        else -> {
                            bundle.putString("id", viewModel.directoryInfo.value!![it].id)
                            bundle.putString("type", viewModel.directoryInfo.value!![it].type)
                            bundle.putString("name", viewModel.directoryInfo.value!![it].name)
                            findNavController().navigate(R.id.action_fragment_directory_to_fragmentFileInfo, bundle)
                        }
                    }
                }
            }
            directoryRvAdapter.setOnLongClickListener {
                if (sr.isRefreshing) {
                    Toast.makeText(context, "正在刷新，请稍后", Toast.LENGTH_SHORT).show()
                } else {
                    mainViewModel.changeFileOperator()
                }
            }

            directoryRvAdapter.setOnClickMoreImageViewListener {
                if (sr.isRefreshing) Toast.makeText(context, "正在刷新，请稍后", Toast.LENGTH_SHORT).show()
                else mainViewModel.changeFileOperator()
            }

            directoryRvAdapter.setOnClickCheckBoxListener { position, isCheck ->
                if (sr.isRefreshing) {
                    Toast.makeText(context, "正在刷新，请稍后", Toast.LENGTH_SHORT).show()
                } else {
                    if (isCheck) viewModel.directoryInfo.value?.get(position)?.let { mainViewModel.addCheck(it) }
                    else viewModel.directoryInfo.value?.get(position)?.let { mainViewModel.removeCheck(it) }
                }
            }

        }

        //拦截返回事件
        val callBack = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            mainViewModel.changeFileOperator()
        }

        mainViewModel.isFileOperator.observe(viewLifecycleOwner, Observer {
            callBack.isEnabled = it

            adapter?.isFileOperator = it
            adapter?.notifyItemRangeChanged(0, list.size)

        })

    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        LogUtil.err(this.javaClass, item!!.itemId.toString())
        when (item!!.itemId) {
            R.id.menu_directory_transmit -> {
                findNavController().navigate(R.id.action_fragment_directory_to_fragmentTransmit)
            }
        }
        return false
    }


}