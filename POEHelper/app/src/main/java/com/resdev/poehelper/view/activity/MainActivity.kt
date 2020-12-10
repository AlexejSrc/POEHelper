package com.resdev.poehelper.view.activity


import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.resdev.poehelper.MyApplication
import com.resdev.poehelper.R
import com.resdev.poehelper.model.Config
import com.resdev.poehelper.model.CurrentValue
import com.resdev.poehelper.utils.getDarkenColor
import com.resdev.poehelper.utils.isColorLight
import com.resdev.poehelper.utils.showSnackbar
import com.resdev.poehelper.view.activity.ActivityUtil.Companion.createColorPicker
import com.resdev.poehelper.view.activity.ActivityUtil.Companion.createCurrencyPicker
import com.resdev.poehelper.view.activity.ActivityUtil.Companion.createLanguagePicker
import com.resdev.poehelper.view.activity.ActivityUtil.Companion.createLeaguePicker
import com.resdev.poehelper.view.activity.ActivityUtil.Companion.setLang
import com.resdev.poehelper.view.fragment.BookmarkFragment
import com.resdev.poehelper.view.fragment.CurrencyFragment
import com.resdev.poehelper.view.fragment.ItemFragment
import com.resdev.poehelper.view.fragment.MainFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.lang.reflect.Field


class MainActivity : AppCompatActivity() {
private lateinit var fragment: MainFragment
val Config = MyApplication.getConfig()
private val APP_PREFERENCES = "mysettings"
private var lastFragmentMenuId: Int = R.id.nav_currency
private var isBookmarkOpened = false
private var mSettings: SharedPreferences? = null
private lateinit var bookmarkItem: MenuItem
private lateinit var searchItem: MenuItem
private var bookmarkIconClosed: Drawable? = null
private var bookmarkIconOpened : Drawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        loadData()
        setLang()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            0, 0
        )
        initializeActivityComponents()
        setListeners()
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        openFragmentWithCheck(lastFragmentMenuId)
        navigation_view.itemIconTintList = null
        navigation_view.setNavigationItemSelectedListener {
            toolbar.collapseActionView()
            isBookmarkOpened = false
            bookmarkItem.icon = bookmarkIconClosed
            openFragmentWithCheck(it.itemId)
            return@setNavigationItemSelectedListener true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        searchItem = menu?.findItem(R.id.app_bar_search)!!
        bookmarkItem = menu?.findItem(R.id.switch_fragments)!!
        paintInterface(Config.getColor())
        var searchView = searchItem!!.actionView as SearchView
        searchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return try{
                    fragment.setFilter(query)
                    true
                } catch (ex : Exception){
                    false
                }
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return try{
                    fragment.setFilter(newText)
                    true
                } catch (ex : Exception){
                    false
                }
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.currency_picker -> {
                try{
                    this.createCurrencyPicker{}
                }
                catch (ex: java.lang.Exception){
                    ex.printStackTrace()
                    showSnackbar(frameLayout, CONNECTION_ERROR)
                }
                true
            }
            R.id.league_picker -> {
                try {
                    this.createLeaguePicker{

                    }
                }
                catch (ex: java.lang.Exception){
                    ex.printStackTrace()
                    showSnackbar(frameLayout, CONNECTION_ERROR)
                }
                true
            }
            R.id.color_picker ->{
                this.createColorPicker{
                    this@MainActivity.paintInterface(Config.getColor())
                }
                return true}
            R.id.language_picker -> {
                this.createLanguagePicker{
                    this@MainActivity.recreate()
                }
                true
            }
            R.id.switch_fragments->{
                try {
                    fragment = BookmarkFragment()
                    isBookmarkOpened = !isBookmarkOpened
                    switchBookmarkIcons()
                    openFragmentWithCheck(lastFragmentMenuId)
                }
                catch (ex: java.lang.Exception){
                    showSnackbar(frameLayout, CONNECTION_ERROR)
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



    override fun onStop() {
        super.onStop()
        saveData()
    }

    fun initializeActivityComponents(){
        title = Config.getLeague()
        bookmarkIconClosed = getDrawable(R.drawable.ic_star_border_white_24dp)
        bookmarkIconOpened = getDrawable(R.drawable.ic_star_white_24dp)
    }

    fun saveData(){
        val editor: SharedPreferences.Editor = mSettings!!.edit()
        editor.putInt(LAST_FRAGMENT, lastFragmentMenuId)
        editor.putBoolean(IS_BOOKMARK_OPENED, isBookmarkOpened)
        editor.apply()
        Config.saveConfigs(mSettings)
    }

    fun loadData(){
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        lastFragmentMenuId = mSettings!!.getInt(LAST_FRAGMENT, R.id.nav_currency)
        isBookmarkOpened = mSettings!!.getBoolean(IS_BOOKMARK_OPENED, false)
        Config.loadConfig(mSettings)
    }

    fun openFragmentWithCheck(navigationItemId: Int){
        lifecycleScope.coroutineContext.cancelChildren()
        drawer_layout.closeDrawer(GravityCompat.START)
        setProgressBar()
        lifecycleScope.launch(Dispatchers.Default) {
            while (!MyApplication.getCurrentValue().isCurrentDataIsReady()){
                delay(500)
            }
            withContext(Dispatchers.Main){
                hideProgressBar()
                openFragment(navigationItemId)
            }
        }
        
    }

    private fun openFragment(navigationItemId: Int){
        if (isBookmarkOpened){
            openBookmarkFragment()
            return
        }
        fragment = if (navigationItemId==R.id.nav_fragment || navigationItemId==R.id.nav_currency){
            CurrencyFragment()
        } else{
            ItemFragment()
        }
        openDefaultFragment(navigationItemId)
    }

    private fun openBookmarkFragment(){
        fragment = BookmarkFragment()
        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, fragment as Fragment).commit()
    }
    private fun openDefaultFragment(navigationItemId: Int){
        var bundle = Bundle()
        bundle.putInt(VALUE_KEY, navigationItemId)
        (fragment as Fragment).arguments = bundle
        lastFragmentMenuId = navigationItemId
        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, fragment as Fragment).commit()

    }

    fun switchBookmarkIcons(){
            if (isBookmarkOpened)
                bookmarkItem.icon = bookmarkIconOpened
            else{
                bookmarkItem.icon = bookmarkIconClosed
            }

    }

    //interface painting

    fun paintInterface(color : Int){
        toolbar.setBackgroundColor(color)
        setTextColor(color)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = getDarkenColor(color)
        progressBar.indeterminateTintList = ColorStateList.valueOf(color)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.colorMode = color
        }
    }

    fun setTextColor(color: Int){
        var color = Integer.toHexString(color)
        if (isColorLight(color))
        {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_menu_black_24dp)
            paintElements(resources.getColor(R.color.black))
        }
        else{
            window.decorView.systemUiVisibility = 0
            toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_menu_white_24dp)
            paintElements(resources.getColor(R.color.white))
        }
        switchBookmarkIcons()
    }

    private fun paintElements(color: Int){
        var editText = searchItem.actionView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        val mCollapseIcon: Field = toolbar.javaClass.getDeclaredField("mCollapseIcon")
        mCollapseIcon.isAccessible = true
        val drw: Drawable = mCollapseIcon.get(toolbar) as Drawable
        val searchIcon = getDrawable(R.drawable.ic_search_white_24dp)
        toolbar.overflowIcon?.setTint(color)
        (searchItem.actionView.findViewById(androidx.appcompat.R.id.search_close_btn) as ImageView).drawable.setTint(color)
        drw.setTint(color)
        toolbar.setTitleTextColor(color)
        editText.setTextColor(color)
        editText.setHintTextColor(color)
        searchIcon?.setTint(color)
        bookmarkIconClosed?.setTint(color)
        bookmarkIconOpened?.setTint(color)
        searchItem.icon = searchIcon
    }

    fun setListeners(){
        Config.getObservableLeague().observe(this, Observer {
            title = it
        })

    }

    fun setProgressBar(){
        progressBar.visibility = View.VISIBLE

    }

    fun hideProgressBar(){
        progressBar.visibility = View.GONE
    }




}
