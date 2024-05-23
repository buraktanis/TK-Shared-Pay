package com.tkpay.kotlinproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SharedWallet : AppCompatActivity() {

    private lateinit var balanceTextView: TextView
    private lateinit var jointAccountsList: LinearLayout
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shared_wallet)

        balanceTextView = findViewById(R.id.balanceAmount)
        jointAccountsList = findViewById(R.id.jointAccountsList)

        // Get the balance value from the intent
        val balance = intent.getStringExtra("BALANCE")
        balanceTextView.text = balance

        // Load shared wallets
        loadUserWallets()

        val button = findViewById<Button>(R.id.btnNavigateToAddSharedWallet)
        button.setOnClickListener {
            val intent = Intent(this, AddSharedWallet::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserWallets() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            lifecycleScope.launch(Dispatchers.Main) {
                try {
                    val userDoc = db.collection("users").document(userId).get().await()
                    val walletIds = userDoc.get("sharedwallets") as? List<String>

                    if (walletIds != null) {
                        for (walletId in walletIds) {
                            val walletDoc = db.collection("sharedwallets").document(walletId).get().await()
                            addSharedWalletToView(walletDoc)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SharedWallet", "Error fetching user wallets", e)
                }
            }
        }
    }

    private fun addSharedWalletToView(document: DocumentSnapshot) {
        val inflater = LayoutInflater.from(this)
        val walletView = inflater.inflate(R.layout.item_shared_wallet, jointAccountsList, false) as RelativeLayout

        val tripNameTextView = walletView.findViewById<TextView>(R.id.tripNameTextView)
        val balanceTextView = walletView.findViewById<TextView>(R.id.balanceTextView)
        val membersTextView = walletView.findViewById<TextView>(R.id.membersTextView)

        tripNameTextView.text = document.getString("name")
        balanceTextView.text = "Mevcut Bakiye: ${document.getDouble("balance")} TRY"

        val users = document.get("users") as? List<String>
        if (users != null) {
            loadUserNames(users) { names ->
                membersTextView.text = "Üyeler: ${names.joinToString(", ")}"
                walletView.setOnClickListener {
                    val intent = Intent(this, SharedWalletDetail::class.java)
                    intent.putExtra("ACCOUNT_NAME", document.getString("name"))
                    intent.putExtra("ACCOUNT_BALANCE", document.getDouble("balance").toString())
                    intent.putStringArrayListExtra("ACCOUNT_MEMBERS", ArrayList(names))
                    intent.putExtra("ACCOUNT_NUMBER", document.id)
                    startActivity(intent)
                }
            }
        }

        jointAccountsList.addView(walletView)
    }

    private fun loadUserNames(userIds: List<String>, callback: (List<String>) -> Unit) {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val userNames = mutableListOf<String>()
                for (userId in userIds) {
                    val userDoc = db.collection("users").document(userId).get().await()
                    val userName = userDoc.getString("fullName")
                    if (userName != null) {
                        userNames.add(userName)
                    }
                }
                callback(userNames)
            } catch (e: Exception) {
                Log.e("SharedWallet", "Error fetching user names", e)
            }
        }
    }
}
