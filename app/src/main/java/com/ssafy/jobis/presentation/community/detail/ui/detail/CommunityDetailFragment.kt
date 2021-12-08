package com.ssafy.jobis.presentation.community.detail.ui.detail

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ssafy.jobis.R
import com.ssafy.jobis.data.model.community.Comment
import com.ssafy.jobis.data.response.PostResponse
import com.ssafy.jobis.databinding.CommunityDetailFragmentBinding
import com.ssafy.jobis.presentation.community.detail.CommunityDetailActivity
import java.text.SimpleDateFormat
import java.util.*

class CommunityDetailFragment : Fragment() {
    private lateinit var communityDetailViewModel: CommunityDetailViewModel
    private var _binding: CommunityDetailFragmentBinding? = null
    private val binding get() = _binding!!
    private var uid: String? = null
    private var auth: FirebaseAuth? = null
    var id: String? = null
    companion object {
        fun newInstance() = CommunityDetailFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CommunityDetailFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        communityDetailViewModel = ViewModelProvider(this, CommunityDetailViewModelFactory())
            .get(CommunityDetailViewModel::class.java)
        communityDetailViewModel.post.observe(viewLifecycleOwner,
            Observer { post ->
                post ?: return@Observer
                updateUi(post)
            })
        communityDetailViewModel.isLiked.observe(viewLifecycleOwner,
            Observer { isLiked ->
                isLiked ?: return@Observer
                updateLikeUi(isLiked)
            })
        communityDetailViewModel.likeCount.observe(viewLifecycleOwner,
            Observer { likeCount ->
                likeCount ?: return@Observer
                binding.detailLikeCountTextView.text = likeCount.toString()
            })
        communityDetailViewModel.comments.observe(viewLifecycleOwner,
            Observer { comments ->
                comments ?: return@Observer
                updateCommentsUi(comments)
            })
        communityDetailViewModel.deleted.observe(viewLifecycleOwner,
            Observer { deleted ->
                deleted ?: return@Observer
                (activity as CommunityDetailActivity).loadingOff()
                if (deleted) {
                    Toast.makeText(context, "게시글이 삭제되었습니다.", Toast.LENGTH_LONG).show()
                    (activity as CommunityDetailActivity).onBackPressed()
                } else {
                    Toast.makeText(context, "게시글이 삭제 실패", Toast.LENGTH_LONG).show()
                }
            })
        communityDetailViewModel.reportResult.observe(viewLifecycleOwner,
            Observer { reportResult ->
                reportResult ?: return@Observer
                (activity as CommunityDetailActivity).loadingOff()
                if (reportResult) {
                    Toast.makeText(context, "신고되었습니다. 관리자 검토 후 조치됩니다.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "신고실패", Toast.LENGTH_LONG).show()
                }
            })
        id = arguments?.getString("id")
        auth = Firebase.auth
        uid = auth!!.currentUser?.uid
        binding.detailLikeImageView.setOnClickListener {
            communityDetailViewModel.updateLike(id!!, uid!!)
        }

        binding.commentButton.setOnClickListener {
            val text = binding.commentEditText.text.toString()
            if (text.length > 0) {
                (activity as CommunityDetailActivity).loadingOn()
                communityDetailViewModel.createComment(text, id!!, uid!!)
                binding.commentEditText.text.clear()
            }
        }

        binding.postDeleteButton.setOnClickListener {
            showPopup(0, null)
        }

        binding.postEditButton.setOnClickListener {
            val post = communityDetailViewModel.getPost()
            if (post != null) {
                (activity as CommunityDetailActivity).goPostEditFragment(id!!, uid!!, post)
            }
        }

        binding.postDeclarationButton.setOnClickListener {
            showPopup(2, null)
        }

        if (id != null && uid != null) {
            communityDetailViewModel.loadPost(id!!, uid!!)
        }
    }

    private fun updateUi(post: PostResponse) {
        binding.detailTitleTextView.text = post.title
        binding.detailCategoryTextView.text = post.category
        binding.detailContentTextView.text = post.content
        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.KOREAN)
        val cal = Calendar.getInstance()
        cal.time = post.created_at.toDate()
        cal.add(Calendar.HOUR, 9)
        binding.detailDateTextView.text = sdf.format(cal.time).toString()
        binding.detailLikeCountTextView.text = post.like.size.toString()
        binding.detailNickNameTextView.text = "by " + post.user_nickname + "  |  "
        binding.commentCountTextView.text = "댓글 " +post.comment_list.size.toString() + "개"
        if (post.user_id == uid || auth?.currentUser?.email == "ssafy@gmail.com") {
            binding.postEditButton.visibility = View.VISIBLE
            binding.postDeleteButton.visibility = View.VISIBLE
        }
    }

    private fun updateLikeUi(isLiked: Boolean) {
        if (isLiked) {
            binding.detailLikeImageView.circleBackgroundColor = resources.getColor(R.color.primary)
        } else {
            binding.detailLikeImageView.circleBackgroundColor = resources.getColor(R.color.light_gray)
        }
    }

    private fun updateCommentsUi(comments: MutableList<Comment>) {
        val adapter = CustomCommentAdapter()
        adapter.listData = comments
        adapter.setOnItemClickListener(object: CustomCommentAdapter.OnItemClickListener{
            override fun onItemClick(v: View, comment: Comment, pos: Int) {
                if ( comment.user_id == uid) {
                    showPopup(1, comment)
                }
            }
        })
        binding.detailCommentRecyclerView.adapter = adapter
        binding.detailCommentRecyclerView.layoutManager = StaggeredGridLayoutManager(
            1,
            StaggeredGridLayoutManager.VERTICAL)
        (activity as CommunityDetailActivity).loadingOff()
    }

    fun showPopup(option: Int, comment: Comment?) {
        val inflater = (activity as CommunityDetailActivity).getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.select_popup, null)
        val alertDialog = AlertDialog.Builder((activity as CommunityDetailActivity))

        when(option) {
            0 -> {
                alertDialog.setTitle("게시글을 삭제하시겠습니까?")
                    .setPositiveButton("네", DialogInterface.OnClickListener { dialogInterface, i ->
                        (activity as CommunityDetailActivity).loadingOn()
                        communityDetailViewModel.deletePost(id!!, uid!!)
                    })
                    .setNegativeButton("아니오", DialogInterface.OnClickListener { dialogInterface, i ->
                        return@OnClickListener
                    })
                    .create()
            }
            1 -> {
                alertDialog.setTitle("댓글을 삭제하시겠습니까?")
                    .setPositiveButton("네", DialogInterface.OnClickListener { dialogInterface, i ->
                        (activity as CommunityDetailActivity).loadingOn()
                        communityDetailViewModel.deleteComment(id!!, comment!!, uid!!)
                    })
                    .setNegativeButton("아니오", DialogInterface.OnClickListener { dialogInterface, i ->
                        return@OnClickListener
                    })
                    .create()
            }
            2 -> {
                val mList = arrayOf("광고", "도배", "음란물", "욕설", "개인정보침해", "저작권침해", "기타")
                lateinit var reason : String
                alertDialog.setTitle("신고 사유를 선택해주세요.")
                    .setItems(mList, DialogInterface.OnClickListener { dialogInterface, i ->
                        (activity as CommunityDetailActivity).loadingOn()
                        communityDetailViewModel.reportPost(id!!, uid!!, mList[i])
                    })
            }
        }

        alertDialog.setView(view)
        alertDialog.show()
    }
}