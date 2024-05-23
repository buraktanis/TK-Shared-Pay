package com.tkpay.kotlinproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tkpay.kotlinproject.databinding.ActivitySharedWalletDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SharedWalletDetail : AppCompatActivity() {

    private lateinit var binding: ActivitySharedWalletDetailBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySharedWalletDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val button = findViewById<Button>(R.id.backHomePage)
        button.setOnClickListener {
            val intent = Intent(this, HomeFragment::class.java)
            startActivity(intent)
        }

        // Get data from intent
        val accountName = intent.getStringExtra("ACCOUNT_NAME")
        val accountBalance = intent.getStringExtra("ACCOUNT_BALANCE")
        val accountMembers = intent.getStringArrayListExtra("ACCOUNT_MEMBERS")
        val accountNumber = intent.getStringExtra("ACCOUNT_NUMBER")

        // Set data to views
        binding.accountName.text = accountName
        binding.accountBalance.text = "Mevcut Bakiye: $accountBalance"
        binding.accountMembers.text = "Üyeler: ${accountMembers?.joinToString(", ")}"
        binding.accountNumber.text = accountNumber

        val sendMoneyButton = findViewById<Button>(R.id.sendMoneyButton)
        val sendMoneyAmount = findViewById<EditText>(R.id.sendMoneyAmount)

        sendMoneyButton.setOnClickListener {
            val amountString = sendMoneyAmount.text.toString()
            if (amountString.isNotEmpty()) {
                val amount = amountString.toDouble()
                sendMoney(accountNumber!!, amount)
            } else {
                Toast.makeText(this, "Lütfen gönderilecek tutarı girin.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendMoney(accountNumber: String, amount: Double) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId != null) {
            lifecycleScope.launch(Dispatchers.Main) {
                try {
                    // Get the current balance of the current user
                    val userDoc = db.collection("users").document(currentUserId).get().await()
                    val userCurrentBalance = userDoc.getDouble("balance") ?: 0.0

                    // Check if user has enough balance
                    if (userCurrentBalance >= amount) {
                        // Deduct amount from user's balance
                        val userNewBalance = userCurrentBalance - amount
                        db.collection("users").document(currentUserId)
                            .update("balance", userNewBalance)
                            .await()

                        // Get the current balance of the shared wallet
                        val walletDoc = db.collection("sharedwallets").document(accountNumber).get().await()
                        val walletCurrentBalance = walletDoc.getDouble("balance") ?: 0.0
                        val walletNewBalance = walletCurrentBalance + amount

                        // Update the shared wallet's balance
                        db.collection("sharedwallets").document(accountNumber)
                            .update("balance", walletNewBalance)
                            .await()

                        // Update the UI
                        binding.accountBalance.text = "Mevcut Bakiye: $walletNewBalance TRY"
                        Toast.makeText(this@SharedWalletDetail, "Para başarıyla gönderildi.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@SharedWalletDetail, "Yetersiz bakiye.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("SharedWalletDetail", "Error sending money", e)
                    Toast.makeText(this@SharedWalletDetail, "Para gönderimi sırasında hata oluştu.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Kullanıcı bilgisi alınamadı.", Toast.LENGTH_SHORT).show()
        }
    }
}
