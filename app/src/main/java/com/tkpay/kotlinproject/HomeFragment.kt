package com.tkpay.kotlinproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeFragment : Fragment() {

    private lateinit var containerLayout: LinearLayout

    private lateinit var balanceTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        balanceTextView = view.findViewById(R.id.balanceAmount)

        val button = view.findViewById<Button>(R.id.btnNavigateToSharedWallet)
        button.setOnClickListener {
            val balance = balanceTextView.text.toString()
            val intent = Intent(requireContext(), SharedWallet::class.java).apply {
                putExtra("BALANCE", balance)
            }
            startActivity(intent)
        }

        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Firestore'dan verileri al ve arayüzü güncelle
//        fetchDataAndPopulateUI()
        fetchBalanceAndDisplay()
    }


    private fun fetchBalanceAndDisplay() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val documentSnapshot = userId?.let { db.collection("users").document(it).get().await() }
                val balance = documentSnapshot?.getDouble("balance") ?: 0.0
                balanceTextView.text = "TRY ${String.format("%.2f", balance)}"
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }


}
