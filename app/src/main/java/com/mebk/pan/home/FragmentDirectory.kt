package com.mebk.pan.home

import android.content.Intent
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
import com.bumptech.glide.Glide
import com.mebk.pan.R
import com.mebk.pan.UserInfoActivity
import com.mebk.pan.aa.DirectoryRvAdapter
import com.mebk.pan.database.entity.File
import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.utils.*
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
        val list: MutableList<File> = mutableListOf()

        rv = view.findViewById(R.id.fragment_directory_rv)
        sr = view.findViewById(R.id.fragment_directory_sr)
        toolbar = view.findViewById(R.id.fragment_directory_toolbar)

        toolbar.setOnMenuItemClickListener(this)
        circleIv = toolbar.findViewById(R.id.fragment_directory_toolbar_user_image)
        val uid = SharePreferenceUtils.getSharePreference(requireActivity()).getString(SharePreferenceUtils.SP_KEY_UID, "")
        if (!TextUtils.isEmpty(uid)) {
            Glide.with(requireActivity())
                    .load(splitUrl("https://pan.mebk.org/api/v3/user/avatar/", uid!!, "/s"))
                    .placeholder(R.drawable.mine)
                    .dontAnimate()
                    .into(circleIv)
            LogUtil.err(this.javaClass, "url=${splitUrl("https://pan.mebk.org/api/v3/user/avatar/", uid!!, "/s")}")
        }
        circleIv.setOnClickListener {
            startActivity(Intent(requireContext(), UserInfoActivity::class.java))
        }
        sr.setProgressViewEndTarget(true, 300)
        viewModel.directory()

        sr.isRefreshing = true

        val adapter = context?.let { DirectoryRvAdapter(it, list) }
        rv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rv.adapter = adapter
        (rv.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        //拦截返回事件
        val callBack = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (mainViewModel.isFileOperator.value == true) {
                mainViewModel.changeFileOperator()
            } else {
                LogUtil.err(this.javaClass, "pop")
                viewModel.back()
            }
        }

        viewModel.directoryInfo.observe(viewLifecycleOwner, {
            LogUtil.err(this::class.java, "更新列表")
            list.clear()
            list.addAll(it)
            adapter?.notifyDataSetChanged()
            sr.isRefreshing = false
        })

        viewModel.requestInfo.observe(viewLifecycleOwner, {
            if (it != REQUEST_SUCCESS) {
                Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
                sr.isRefreshing = false
                list.removeAt(0)
                adapter?.notifyItemRemoved(0)
            }
        })

        viewModel.stackSize.observe(viewLifecycleOwner, {
            LogUtil.err(this::class.java, "栈内有${it}个列表")
            callBack.isEnabled = (it != 1 || mainViewModel.isFileOperator.value == true)
            LogUtil.err(this.javaClass, "stackSize 拦截callback=${callBack.isEnabled}")

        })

        sr.setOnRefreshListener {
            val refreshDto = File("0", "正在刷新...", "", "", 0, "refresh", string2timeStamp(viewModel.lastRefreshTimeInfo.value!!), "")
            list.add(0, refreshDto)
            rv.scrollToPosition(0)
            adapter?.notifyItemInserted(0)
            sr.isRefreshing = true
            if (viewModel.stackSize.value != 1) viewModel.directory(viewModel.getStackFirst().first, viewModel.getStackFirst().second, true)
            else viewModel.directory(true)

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
                            viewModel.directory(viewModel.directoryInfo.value!![it].name, viewModel.directoryInfo.value!![it].path)
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



        mainViewModel.isFileOperator.observe(viewLifecycleOwner, {
            LogUtil.err(this.javaClass, "it=${it},size=${viewModel.stackSize.value}")
            callBack.isEnabled = (it || viewModel.stackSize.value != 1)

            adapter?.isFileOperator = it
            adapter?.notifyItemRangeChanged(0, list.size)
            LogUtil.err(this.javaClass, "isFileOperator 拦截callback=${callBack.isEnabled}")
        })

        mainViewModel.deleteInfo.observe(viewLifecycleOwner, {
            if (it == MainViewModel.DELETE_DONE) {
                viewModel.directory()
                mainViewModel.changeFileOperator()
            }
        })

    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        LogUtil.err(this.javaClass, item!!.itemId.toString())
        when (item.itemId) {
            R.id.menu_directory_transmit -> {
                findNavController().navigate(R.id.action_fragment_directory_to_fragmentTransmit)
            }
        }
        return false
    }
}