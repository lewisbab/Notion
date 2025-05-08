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

        // Reference to the input field for workspace name
        val workspaceNameEditText: EditText = findViewById(R.id.etWorkspaceName)

        // Reference to the "Create" button
        val createButton: Button = findViewById(R.id.btnCreate)

        // Handle create button click
        createButton.setOnClickListener {
            val name = workspaceNameEditText.text.toString().trim()

            // Only proceed if the input is not empty
            if (name.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val db = AppDatabase.getInstance(this@CreateWorkspaceActivity)

                    // Check if a workspace with the same name already exists
                    val exists = db.workspaceDao().getByName(name) != null
                    if (exists) {
                        // Show a toast on the main thread if workspace already exists
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@CreateWorkspaceActivity, "Workspace '$name' already exists", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Insert the new workspace into the database
                        db.workspaceDao().insert(Workspace(name = name))

                        // Show success toast and close the activity
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@CreateWorkspaceActivity, "Workspace '$name' created!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
            } else {
                // Prompt the user to enter a name if input is empty
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
