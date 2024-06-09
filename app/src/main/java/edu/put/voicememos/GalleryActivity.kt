package edu.put.voicememos

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GalleryActivity : AppCompatActivity() {

    private lateinit var records: ArrayList<AudioRecord>
    private lateinit var mAdapter: Adapter
    private lateinit var db: AppDatabase
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        records = ArrayList()
        mAdapter = Adapter(records)

        // Initialize RecyclerView
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
    }

    private fun fetchAll() {
        GlobalScope.launch(Dispatchers.IO) {
            val recordsFromDb = db.audioRecordDao().getAll()
            withContext(Dispatchers.Main) {
                records.clear()
                records.addAll(recordsFromDb)
                mAdapter.notifyDataSetChanged()
            }
        }
    }
}
