package com.example.notion

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CreateWorkspaceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_workspace)

        val workspaceNameEditText: EditText = findViewById(R.id.etWorkspaceName)
        val createButton: Button = findViewById(R.id.btnCreate)

        createButton.setOnClickListener {
            val name = workspaceNameEditText.text.toString().trim()
            if (name.isNotEmpty()) {
                // TODO: Save workspace to Room DB
                Toast.makeText(this, "Workspace '$name' created!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
            }
        }
    }
}