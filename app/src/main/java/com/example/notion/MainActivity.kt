package com.example.notion

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notion.data.AppDatabase
import com.example.notion.data.Workspace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WorkspaceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enables full screen layout with proper system bar padding
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Automatically adjust padding based on system bar insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up the RecyclerView and adapter for displaying a list of workspaces
        recyclerView = findViewById(R.id.recyclerView)
        adapter = WorkspaceAdapter(emptyList()) { workspace ->
            // On workspace item click, navigate to WorkspaceDetailsActivity with workspace name
            val intent = Intent(this, WorkspaceDetailsActivity::class.java)
            intent.putExtra("workspace_name", workspace.name)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Set up the "Create Workspace" button to navigate to CreateWorkspaceActivity
        val createWorkspaceButton: Button = findViewById(R.id.btnCreateWorkspace)
        createWorkspaceButton.setOnClickListener {
            val intent = Intent(this, CreateWorkspaceActivity::class.java)
            startActivity(intent)
        }
    }

    // Called when the activity becomes visible again; refresh workspace list
    override fun onResume() {
        super.onResume()
        loadWorkspaces()
    }

    // Loads all workspaces from the database asynchronously and updates the UI
    private fun loadWorkspaces() {
        lifecycleScope.launch(Dispatchers.IO) {
            val workspaces = AppDatabase.getInstance(this@MainActivity)
                .workspaceDao()
                .getAll()

            withContext(Dispatchers.Main) {
                adapter.updateData(workspaces)
            }
        }
    }
}