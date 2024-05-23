package com.tkpay.kotlinproject

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.tkpay.kotlinproject.databinding.ActivityAddSharedWalletBinding

class AddSharedWallet : AppCompatActivity() {

    private lateinit var binding: ActivityAddSharedWalletBinding
    private lateinit var firestore: FirebaseFirestore
    private var memberFieldCount = 1 // Counter to generate unique IDs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSharedWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        val memberListLayout = findViewById<LinearLayout>(R.id.memberListLayout)
        val addMemberButton = findViewById<Button>(R.id.addMemberButton)
        val inviteFriendButton = findViewById<Button>(R.id.inviteFriendButton)

        addMemberButton.setOnClickListener {
            addMemberField(memberListLayout)
        }

        inviteFriendButton.setOnClickListener {
            saveSharedWallet()
        }
    }

    private fun addMemberField(memberListLayout: LinearLayout) {
        val newMemberField = EditText(this).apply {
            hint = "TK numarası..."
            setBackgroundResource(android.R.drawable.editbox_background)
            setPadding(10,10,10,10)
            id = View.generateViewId() // Generate a unique ID
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 10)
            }
        }
        memberListLayout.addView(newMemberField, memberListLayout.childCount - 3) // Adds the new EditText above the buttons
    }

    private fun saveSharedWallet() {
        val accountName = findViewById<EditText>(R.id.accountName).text.toString()
        val memberListLayout = findViewById<LinearLayout>(R.id.memberListLayout)
        val members = mutableListOf<String>()
        val currentuserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentuserId != null) {
            members.add(currentuserId)
        };
        for (i in 0 until memberListLayout.childCount) {
            val child = memberListLayout.getChildAt(i)
            if (child is EditText && child.hint == "TK numarası...") {
                val memberTKNumber = child.text.toString()
                if (memberTKNumber.isNotEmpty()) {
                    members.add(memberTKNumber)
                }
            }
        }

        if (accountName.isEmpty() || members.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedWallet = hashMapOf(
            "name" to accountName,
            "balance" to 0,
            "users" to members
        )

        firestore.collection("sharedwallets")
            .add(sharedWallet)
            .addOnSuccessListener { documentReference ->
                val sharedWalletId = documentReference.id
                updateUserSharedWallets(members, sharedWalletId)
                Toast.makeText(this, "Shared Wallet created successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error creating shared wallet: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserSharedWallets(members: List<String>, sharedWalletId: String) {
        for (member in members) {
            val userRef = firestore.collection("users").document(member)
            userRef.update("sharedwallets", FieldValue.arrayUnion(sharedWalletId))
                .addOnSuccessListener {
                    // Successfully updated user
                }
                .addOnFailureListener { e ->
                    // Handle the error
                }
        }
    }
}
