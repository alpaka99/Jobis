package com.ssafy.jobis.presentation.chat

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.storage.ktx.storage
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.ssafy.jobis.R
import com.ssafy.jobis.data.model.study.Study
import com.ssafy.jobis.databinding.ActivityChatBinding
import com.ssafy.jobis.presentation.chat.MyFCMService.Companion.currentStudyId
import com.ssafy.jobis.presentation.chat.MyFCMService.Companion.currentStudyTitle
import com.ssafy.jobis.presentation.chat.adapter.ChatAdapter
import com.ssafy.jobis.presentation.chat.adapter.GridAdapter
import com.ssafy.jobis.presentation.chat.adapter.ViewPagerAdapter
import com.ssafy.jobis.presentation.chat.viewholder.GIFViewHolder
import com.ssafy.jobis.presentation.chat.viewmodel.ChatViewModel
import com.ssafy.jobis.view.DrawingView
import kotlinx.coroutines.*
import java.io.File
import java.util.*


class ChatActivity: AppCompatActivity(), View.OnClickListener, ColorPickerDialogListener,
    ViewPagerAdapter.CanvasListener, GIFViewHolder.OnClickGIFListener,
    ChatAdapter.onAddedChatListener, NavigationView.OnNavigationItemSelectedListener{

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private val viewPagerAdapter: ViewPagerAdapter by lazy {
        ViewPagerAdapter(this@ChatActivity)
    }
    private val girdAdapter: GridAdapter by lazy {
        GridAdapter(this@ChatActivity)
    }

    private lateinit var model: ChatViewModel
    private val map = HashMap<Int, ImgChat?>()
    private var startIdx = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentStudyId = intent.getStringExtra("study_id").toString()

        currentStudyTitle = intent.getStringExtra("study_title").toString()
        binding.tvTbTitle.text = currentStudyTitle

        model = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(ChatViewModel::class.java)
        model.readAllChat()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val mNotificationManager = getSystemService(NotificationManager::class.java)
            mNotificationManager.cancel(currentStudyId.hashCode())
        }

        val isFirstTime = intent.getBooleanExtra("isFirstTime", false)

        if (isFirstTime) {  // 처음 이 방에 입장
            model.setFirstTime()
        }

        if (PreferenceManager.getDefaultSharedPreferences(this).getString("gif", "")?.length!! > 0) {
            binding.txtEmoticonBlank.visibility = View.GONE
        }

        // 파이어 스토어에서 값을 가져오느라고 빈 이미지가 생기고나서 채워지는데 LiveData를 하나더 사용하면 바로 될줄 알았는데
        // 결국 이미지는 하나씩 받아오는 거임. 근데 어차피 채팅도 하나씩 들어오자네? 모르것다 일단 다른것부터 하자.
        model.studyWithChats.observe(this, {
            val chatList = it.chats
            val storageRef = Firebase.storage.reference

            for (i in startIdx until chatList.size) {
                val chat = chatList[i]
                if (chat.file_name.isNotEmpty()) {

                    map[i] = null

                    val path = "images/${chat.file_name}"
                    val pathRef = storageRef.child(path)

                    val localFile = File(
                        applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        chat.file_name
                    )
                    if (localFile.exists()) {
                        val source = ImageDecoder.createSource(localFile)
                        map[i] = ImgChat(ImageDecoder.decodeDrawable(source))
                    } else {
                        pathRef.getFile(localFile).addOnSuccessListener {
                            if (localFile.exists()) {
                                val source = ImageDecoder.createSource(localFile)
                                map[i] = ImgChat(ImageDecoder.decodeDrawable(source))
                                chatAdapter.notifyDataSetChanged()
                            }
                        }
                    }

                }
            }
            chatAdapter = ChatAdapter(model.uid, chatList, map)
            binding.rvChat.adapter = chatAdapter
            goToRecentChat()
            startIdx = chatList.size
        })

        val display = windowManager.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        val density = resources.displayMetrics.density
        val width = (size.x - 300*density)/4

        binding.frameEmoticonChat.layoutParams.width = size.x
        binding.frameEmoticonChat.layoutParams.height = size.x

        binding.gridEmoticonChat.apply {
            adapter = girdAdapter
            layoutManager = GridLayoutManager(context, 3)
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                val spanCount = 3
                val spacing = width.toInt()
                override fun getItemOffsets( outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    val position: Int = parent.getChildAdapterPosition(view)
                    val column: Int = position % spanCount

                    outRect.apply {
                        left = spacing - column * spacing / spanCount
                        right = (column + 1) * spacing / spanCount

                        if (position < spanCount) top = spacing
                        bottom = spacing
                    }
                }
            })
        }

        binding.viewpagerChat.apply {
            isUserInputEnabled = false
            adapter = viewPagerAdapter
            registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                    if (viewPagerAdapter.isAdded(currentItem)) {
                        binding.imgCheck.setImageResource(R.drawable.ic_check_circle_24)
                    } else {
                        binding.imgCheck.setImageResource(R.drawable.ic_check_circle_outline_24)
                    }
                }
            })
        }

        setSupportActionBar(binding.tbChat)  // 액션바 설정
        val actionbar = supportActionBar

        actionbar?.apply {
            setDisplayShowTitleEnabled(false)  // 앱 이름 안보이게
            setDisplayHomeAsUpEnabled(true)  // 뒤로가기 버튼
            setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)  // 뒤로가기 아이콘 설정
        }

        Firebase.messaging.subscribeToTopic(currentStudyId)

        binding.apply {
            imgAddChat.setOnClickListener(this@ChatActivity)
            imgEmoticonChat.setOnClickListener(this@ChatActivity)
            imgSendChat.setOnClickListener(this@ChatActivity)
            editTextChat.requestFocus()
            editTextChat.setOnClickListener(this@ChatActivity)
            editTextChat.addTextChangedListener {
                if (it.toString().trim().isNotEmpty()) {
                    binding.imgSendChat.setColorFilter(Color.parseColor("#448aff"))
                } else {
                    binding.imgSendChat.setColorFilter(Color.parseColor("#7C7C7C"))
                }
            }
            imgSelectColor.setOnClickListener(this@ChatActivity)
            imgSave.setOnClickListener(this@ChatActivity)
            imgLeft.setOnClickListener(this@ChatActivity)
            imgRight.setOnClickListener(this@ChatActivity)
            imgCheck.setOnClickListener(this@ChatActivity)
            imgCloseGif.setOnClickListener(this@ChatActivity)
            gifProgressChat.setOnClickListener(this@ChatActivity)
            chatNavigation.setNavigationItemSelectedListener(this@ChatActivity)
            chatNavigation.menu.getItem(0).setActionView(R.layout.menu_image)
            imgUndo.setOnClickListener(this@ChatActivity)
            imgRedo.setOnClickListener(this@ChatActivity)
        }
    }

    private fun goToRecentChat() {
        CoroutineScope(Dispatchers.Main).launch {
            binding.rvChat.scrollToPosition(chatAdapter.itemCount-1)
        }
    }

    suspend fun showCanvas(view: View, imm: InputMethodManager) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)   // 키보드가 layout에 영향을 주지 않게 함
        binding.frameEmoticonChat.visibility = View.VISIBLE                             // 캔버스를 보임
        imm.hideSoftInputFromWindow(view.windowToken, 0)                           // 키보드를 숨김
        delay(100)                                                              // 키보드가 전부 안보일 떄까지 기다림
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)    // 키보드가 layout에 영향을 주도록 함
        goToRecentChat()
    }

    suspend fun showKeyboard(imm: InputMethodManager) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)   // 키보드가 layout에 영향을 주지 않게 함
        imm.showSoftInput(binding.editTextChat, 0)                                 // 키보드를 보임
        delay(100)                                                              // 키보드가 전부 보일 떄까지 기다림
        binding.frameEmoticonChat.visibility = View.GONE                                // 캔버스를 숨김
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)    // 키보드가 layout에 영향을 주도록 함
        goToRecentChat()
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.img_add_chat -> {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                CoroutineScope(Dispatchers.Main).launch {
                    if (binding.frameEmoticonChat.visibility == View.GONE) {
                        binding.imgAddChat.setColorFilter(Color.parseColor("#448aff"))
                        binding.imgEmoticonChat.setColorFilter(Color.parseColor("#7C7C7C"))
                        binding.gridEmoticonChat.visibility = View.GONE
                        showCanvas(view, imm)
                    } else if(binding.gridEmoticonChat.visibility == View.VISIBLE) {
                        binding.txtEmoticonBlank.visibility = View.GONE
                        binding.imgAddChat.setColorFilter(Color.parseColor("#448aff"))
                        binding.imgEmoticonChat.setColorFilter(Color.parseColor("#7C7C7C"))
                        binding.gridEmoticonChat.visibility = View.GONE
                    } else {
                        binding.imgAddChat.setColorFilter(Color.parseColor("#7C7C7C"))
                        binding.imgEmoticonChat.setColorFilter(Color.parseColor("#448aff"))
                        binding.frameEmoticonChat.visibility = View.GONE
                    }
                }
            }
            R.id.img_emoticon_chat -> {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                CoroutineScope(Dispatchers.Main).launch {
                    if (binding.frameEmoticonChat.visibility == View.GONE) {
                        binding.imgEmoticonChat.setColorFilter(Color.parseColor("#448aff"))
                        binding.imgAddChat.setColorFilter(Color.parseColor("#7C7C7C"))
                        binding.gridEmoticonChat.visibility = View.VISIBLE
                        showCanvas(view, imm)
                    } else if(binding.gridEmoticonChat.visibility == View.GONE) {
                        if(girdAdapter.itemCount == 0 ) binding.txtEmoticonBlank.visibility = View.VISIBLE
                        binding.imgEmoticonChat.setColorFilter(Color.parseColor("#448aff"))
                        binding.imgAddChat.setColorFilter(Color.parseColor("#7C7C7C"))
                        binding.gridEmoticonChat.visibility = View.VISIBLE
                    } else {
                        binding.imgEmoticonChat.setColorFilter(Color.parseColor("#7C7C7C"))
                        binding.imgAddChat.setColorFilter(Color.parseColor("#448aff"))
                        binding.frameEmoticonChat.visibility = View.GONE
                    }
                }
            }
            R.id.img_send_chat -> {
                if (model.chooseFileName.isNotEmpty()) {
                    Log.d("내가 고른 파일", "파일이름 = "+model.chooseFileName)
                    model.sendMessage("이모티콘을 보냈습니다.", model.chooseFileName)
                    clearGIFLayout()
                }
                val text = binding.editTextChat.text?.trim().toString()
                if (text != "") {
                    binding.editTextChat.text = null
                    model.sendMessage(text)
                }
            }
            R.id.edit_text_chat -> {
                binding.imgAddChat.setColorFilter(Color.parseColor("#7C7C7C"))
                binding.imgEmoticonChat.setColorFilter(Color.parseColor("#7C7C7C"))
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                val scope = CoroutineScope(Job() + Dispatchers.Main)
                scope.launch {
                    if (binding.frameEmoticonChat.visibility == View.VISIBLE) {    // 1. Edit Text만 보일 때
                        showKeyboard(imm)                                             // 키보드를 보임
                    }
                    delay(300)
                    goToRecentChat()
                }
            }
            R.id.img_select_color -> {
                ColorPickerDialog
                    .newBuilder()
                    .setDialogTitle(R.string.color_dialog_title)
                    .setSelectedButtonText(R.string.select)
                    .setCustomButtonText(R.string.custom)
                    .setPresetsButtonText(R.string.presets)
                    .show(this)
            }
            R.id.img_save -> {
                CoroutineScope(Dispatchers.Main).launch {
                    binding.gifProgressChat.visibility = View.VISIBLE
                    launch {
                        viewPagerAdapter.saveView()
                    }.join()
                }
            }
            R.id.img_left -> {
                binding.viewpagerChat.apply {
                    if (currentItem > 0) {
                        currentItem--
                    }
                    binding.textViewpagerNum.text = "${currentItem+1}/24"
                }
            }
            R.id.img_right -> {
                binding.viewpagerChat.apply {
                    if (currentItem < 23) {
                        currentItem++
                    }
                    binding.textViewpagerNum.text = "${currentItem+1}/24"
                }
            }
            R.id.img_check -> {
                val current = binding.viewpagerChat.currentItem
                viewPagerAdapter.apply {
                    if (isAdded(current)) {
                        removeView(current)
                        binding.imgCheck.setImageResource(R.drawable.ic_check_circle_outline_24)
                    } else {
                        addView(current)
                        binding.imgCheck.setImageResource(R.drawable.ic_check_circle_24)
                    }
                }

            }
            R.id.img_undo -> {
                viewPagerAdapter.unDo(binding.viewpagerChat.currentItem)
            }
            R.id.img_redo -> {
                viewPagerAdapter.reDo(binding.viewpagerChat.currentItem)
            }
            R.id.img_close_gif -> {
                clearGIFLayout()
            }
            R.id.gif_progress_chat -> return
        }
    }

    fun clearGIFLayout() {
        binding.layoutGif.visibility = View.GONE
        binding.imgSendChat.setColorFilter(Color.parseColor("#7C7C7C"))
        model.chooseFileName = ""
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_chat_toolbar, menu)  // 툴바의 메뉴들 연결
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {  // 툴바의 메뉴를 눌럿을 때
        when (item.itemId) {
            android.R.id.home -> onBackPressed()  // 뒤로가기
            R.id.item_open_nav -> binding.dlChat.openDrawer(GravityCompat.END)  // 네비게이션바 오픈. END가 아니라 START이고 NavigationView의 layout_gravity를 start로 바꾸면 오른쪽에서 생김
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {  // 뒤로가기를 눌렀을 때
        binding.imgEmoticonChat.setColorFilter(Color.parseColor("#7C7C7C"))
        binding.imgAddChat.setColorFilter(Color.parseColor("#7C7C7C"))
        if (binding.dlChat.isDrawerOpen(GravityCompat.END)) {  // 네이게이션 뷰가 열려있다면 닫아 주고 아니라면 뒤로가기
            binding.dlChat.closeDrawer(GravityCompat.END)
        } else if(binding.frameEmoticonChat.visibility == View.VISIBLE) {
            binding.frameEmoticonChat.visibility = View.GONE
        } else {
            super.onBackPressed()
        }
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        binding.imgSelectColor.background.setTint(color)
        DrawingView.color = color
    }

    override fun onDialogDismissed(dialogId: Int) {

    }

    override fun onSuccess(file: File) {
        girdAdapter.getGif()                                       // gif파일 그리드를 새로고침한다


        val gifFile = Uri.fromFile(file)                           // gif파일의 로컬 경로를 가져온다
        val filePath = "images/${gifFile.lastPathSegment}"         // gif파일을 저장할 파이어스토어 경로를 지정한다
        model.fileReference = filePath                             // 도중에 취소될 경우를 대비하여 뷰모델에 저장

        val storageRef = Firebase.storage.reference                // 파이어스토어의 루트 경로를 가져온다
        val riversRef = storageRef.child(filePath)                 // gif파일을 저장할 파이어스토어 경로를 가져온다

        riversRef.putFile(gifFile).addOnFailureListener {
            binding.gifProgressChat.visibility = View.GONE
            Toast.makeText(this, "파일 생성 실패!", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener {
            binding.gifProgressChat.visibility = View.GONE
            Toast.makeText(this, "파일 생성!", Toast.LENGTH_SHORT).show()
            binding.txtEmoticonBlank.visibility = View.GONE
        }

    }

    override fun chooseGIF(source: ImageDecoder.Source?, filePath: String) {
        if (source != null) {
            model.chooseFileName = filePath

            val drawable = ImageDecoder.decodeDrawable(source)
            binding.imgGif.setImageDrawable(drawable)
            binding.layoutGif.visibility = View.VISIBLE
            if (drawable is AnimatedImageDrawable) {
                drawable.repeatCount = 4
                drawable.start()
            }
            binding.imgSendChat.setColorFilter(Color.parseColor("#448aff"))
        }
    }

    override fun onAddedChat() {
        goToRecentChat()
    }

    override fun onPause() {
        super.onPause()
        currentStudyId = ""
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("reference", model.fileReference)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val StringRef = savedInstanceState.getString("reference") ?: return
        val storageRef = Firebase.storage.getReference(StringRef)
        storageRef.activeUploadTasks
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_1 -> {
                val intent = Intent(this, ChatScheduleActivity::class.java)
                intent.putExtra("study_id", currentStudyId)
                startActivity(intent)
            }
            R.id.item_2 -> { // 네비게이션 뷰에 이미지뷰를 넣고 클릭 이벤트를 달아도 클릭이 안됨. 애초에 아이템 말고는 클릭이 안되고 Dialog도 안뜬다...
                var removeStudy = false
                Firebase.database.reference.child("Study").child(currentStudyId).get().addOnSuccessListener {
                    val study = it.getValue<Study>() ?: return@addOnSuccessListener

                    study.current_user--
                    if (study.current_user == 0) removeStudy = true
                    var idx = 0
                    for (i in study.user_list?.indices!!) {
                        if (study.user_list!![i].id == FirebaseAuth.getInstance().currentUser?.uid ?: "") {
                            idx = i
                            break
                        }
                    }
                    study.user_list!!.removeAt(idx)
                    Firebase.database.reference.child("Study").updateChildren(hashMapOf<String, Any>(currentStudyId to study)).addOnSuccessListener {
                        if (removeStudy) {
                            Firebase.database.reference.child("Study").child(currentStudyId).removeValue()
                        }
                    }
                }
                Firebase.messaging.unsubscribeFromTopic(currentStudyId)
                model.studyWithChats.removeObservers(this)
                model.exitStudy(currentStudyId)
                finish()
            }
        }
        return true
    }

}

data class ImgChat(
    val drawable: Drawable? = null,
    var isMoved: Boolean = false
)