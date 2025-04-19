package com.example.notion

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.notion.data.AppDatabase
import com.example.notion.data.Workspace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CreateWorkspaceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_workspace)

        val workspaceNameEditText: EditText = findViewById(R.id.etWorkspaceName)
        val createButton: Button = findViewById(R.id.btnCreate)

        createButton.setOnClickListener {
            val name = workspaceNameEditText.text.toString().trim()
            if (name.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val db = AppDatabase.getInstance(this@CreateWorkspaceActivity)
                    val exists = db.workspaceDao().getByName(name) != null
                    if (exists) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@CreateWorkspaceActivity, "Workspace '$name' already exists", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        db.workspaceDao().insert(Workspace(name = name))
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@CreateWorkspaceActivity, "Workspace '$name' created!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
