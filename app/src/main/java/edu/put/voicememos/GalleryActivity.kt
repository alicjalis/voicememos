package edu.put.voicememos

import android.content.Intent
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Delete
import androidx.room.Room
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GalleryActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var records: ArrayList<AudioRecord>
    private lateinit var mAdapter: Adapter
    private lateinit var db: AppDatabase
    private lateinit var recyclerView: RecyclerView

    private var allChecked = false

    private lateinit var toolbar: MaterialToolbar

    private lateinit var editBar: View
    private lateinit var btnClose: ImageButton
    private lateinit var btnSelectAll: ImageButton

    private lateinit var searchInput : TextInputEditText
    private lateinit var bottomSheet: LinearLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var btnRename : ImageButton
    private lateinit var btnDelete: ImageButton
    private lateinit var tvEdit : TextView
    private lateinit var tvDelete : TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)


        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener{
            onBackPressed()
        }

        btnRename = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDeleteRound)
        tvEdit = findViewById(R.id.tvEdit)
        tvDelete = findViewById(R.id.tvDelete)

        editBar = findViewById(R.id.editBar)
        btnClose = findViewById(R.id.btnClose)
        btnSelectAll = findViewById(R.id.btnSelectAll)

        bottomSheet = findViewById(R.id.bottomSheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        records = ArrayList()
        mAdapter = Adapter(records, this)

        recyclerView = findViewById(R.id.recyclerview)
        recyclerView.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(this@GalleryActivity)
        }

        // Initialize the database
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "audioRecords"
        ).build()

        // Fetch data from the database
        fetchAll()

        searchInput = findViewById(R.id.search_input)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                var query = p0.toString()
                searchDatabase(query)
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        btnClose.setOnClickListener{
            leaveEditMode()
        }

        btnSelectAll.setOnClickListener{
            allChecked = !allChecked
            records.map{it.isChecked = allChecked}
            mAdapter.notifyDataSetChanged()

            if(allChecked){
                disableRename()
                enableDelete()
            }else{
                disableRename()
                disableDelete()
            }
        }

        btnDelete.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete record?")
            val nbRecords = records.count{it.isChecked}
            builder.setMessage("Are you sure you want to delete $nbRecords record(s)?")

            builder.setPositiveButton("Delete"){_,_,->
                val toDelete = records.filter{it.isChecked}.toTypedArray()
                GlobalScope.launch {
                    db.audioRecordDao().delete(toDelete)
                    runOnUiThread{
                        records.removeAll(toDelete)
                        mAdapter.notifyDataSetChanged()
                        leaveEditMode()
                    }
                }
            }

            builder.setNegativeButton("Cancel"){_,_,->
                // robi nic
            }

            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun leaveEditMode(){
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        editBar.visibility = View.GONE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        records.map{it.isChecked = false}
        mAdapter.setEditMode(false)
    }

    private fun disableRename () {
        btnRename.isClickable = false
        btnRename.backgroundTintList = ResourcesCompat.getColorStateList(resources, R.color.grayDarkDisabled, theme)
        tvEdit.setTextColor(ResourcesCompat.getColorStateList(resources, R.color.grayDarkDisabled, theme))
    }

    private fun disableDelete () {
        btnDelete.isClickable = false
        btnDelete.backgroundTintList = ResourcesCompat.getColorStateList(resources, R.color.grayDarkDisabled, theme)
        tvDelete.setTextColor(ResourcesCompat.getColorStateList(resources, R.color.grayDarkDisabled, theme))
    }

    private fun enableRename () {
        btnRename.isClickable = true
        btnRename.backgroundTintList = ResourcesCompat.getColorStateList(resources, R.color.grayDark, theme)
        tvEdit.setTextColor(ResourcesCompat.getColorStateList(resources, R.color.grayDark, theme))
    }

    private fun enableDelete () {
        btnDelete.isClickable = true
        btnDelete.backgroundTintList = ResourcesCompat.getColorStateList(resources, R.color.grayDark, theme)
        tvDelete.setTextColor(ResourcesCompat.getColorStateList(resources, R.color.grayDark, theme))
    }

    private fun searchDatabase(query: String){
        GlobalScope.launch(Dispatchers.IO) {
            records.clear()
            val recordsFromDb = db.audioRecordDao().searchDatabase("%$query%")
            withContext(Dispatchers.Main) {
                records.addAll(recordsFromDb)
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun fetchAll() {
        GlobalScope.launch(Dispatchers.IO) {
            records.clear()
            val recordsFromDb = db.audioRecordDao().getAll()
            withContext(Dispatchers.Main) {
                records.addAll(recordsFromDb)
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onItemClickListener(position: Int) {
        var audioRecord = records[position]

        if(mAdapter.isEditMode()){
            records[position].isChecked = !records[position].isChecked
            mAdapter.notifyItemChanged(position)

            var nbSelected = records.count{it.isChecked}
            when(nbSelected){
                0 ->{
                    disableRename()
                    disableDelete()
                }
                1 ->{
                    enableDelete()
                    enableRename()
                }
                else -> {
                    disableRename()
                    enableDelete()
                }
            }
        }else{
            var intent = Intent(this, AudioPlayerActivity::class.java)

            intent.putExtra("filepath", audioRecord.filePath)
            intent.putExtra("filename", audioRecord.filename)
            startActivity(intent)
        }
    }

    override fun onItemLongClickListener(position: Int) {
        mAdapter.setEditMode(true)
        records[position].isChecked = !records[position].isChecked
        mAdapter.notifyItemChanged(position)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        if(mAdapter.isEditMode() && editBar.visibility == View.GONE){
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            supportActionBar?.setDisplayShowHomeEnabled(false)

            editBar.visibility = View.VISIBLE

            enableDelete()
            enableRename()
        }
    }
}
