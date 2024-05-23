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

/*
    private fun fetchDataAndPopulateUI() {
        val db = FirebaseFirestore.getInstance()

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val querySnapshot = db.collection("animals").get().await()
                for (document in querySnapshot.documents) {
                    val animalName = document.getString("name")
                    val animalgender = document.getString("gender")
                    val animalrace = document.getString("race")
                    val animalAge = document.getLong("age")
                    val photoUrl = document.getString("photoUrl")
                    val ad_id = document.id
                    val cardview_id = ad_id.hashCode()
                    val string_cardview_id = cardview_id.toString()

                    // CardView oluştur
                    val cardView = CardView(requireContext())
                    val layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.setMargins(0, 0, 0, 3)
                    cardView.layoutParams = layoutParams
                    cardView.cardElevation = 8f

                    // Horizontal LinearLayout oluştur
                    val horizontalLayout = LinearLayout(requireContext())
                    horizontalLayout.orientation = LinearLayout.HORIZONTAL
                    cardView.addView(horizontalLayout)

                    // ImageView oluştur ve ayarla
                    val imageView = ImageView(requireContext())
                    val imageLayoutParams = LinearLayout.LayoutParams(
                        350,
                        350,
                    )
                    imageLayoutParams.setMargins(124, 144, 144, 144) // Sağdan, soldan, yukarıdan, aşağıdan padding ayarlayın
                    // Yuvarlak kenarlığın oluşturulması
                    val shape = GradientDrawable()
                    shape.shape = GradientDrawable.OVAL
                    shape.setColor(Color.TRANSPARENT) // İçi boşaltılıyor, çünkü resmi yuvarlak kenarların içine koymak istiyoruz
                    shape.setStroke(5, Color.BLACK) // Kenarlığı siyah renkte, isteğe bağlı olarak ayarlanabilir

                    // ImageView'ın arkaplanını ve yuvarlak kenarı ayarlama
                    imageView.background = shape
                    imageView.clipToOutline = true
                    imageView.layoutParams = imageLayoutParams
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    Glide.with(requireContext())
                        .load(photoUrl)
                        .into(imageView)
                    horizontalLayout.addView(imageView)

// Dikey LinearLayout oluştur
                    val textInfoLayout = LinearLayout(requireContext())
                    textInfoLayout.orientation = LinearLayout.VERTICAL
                    textInfoLayout.layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    textInfoLayout.gravity = Gravity.CENTER_VERTICAL
                    horizontalLayout.addView(textInfoLayout)

                    val nameTextView = TextView(requireContext())
                    nameTextView.setPadding(0, 12, 0, 12)
                    nameTextView.text = "${animalName}"
                    nameTextView.textSize = 25f // Yazı boyutunu ayarlayın
                    nameTextView.setTypeface(null, Typeface.BOLD)
                    nameTextView.setTextColor(Color.parseColor("#6B5172"))
                    nameTextView.gravity = Gravity.LEFT
                    textInfoLayout.addView(nameTextView)

                    val ageTextView = TextView(requireContext())
                    ageTextView.text = "· $animalAge years "
                    ageTextView.textSize = 18f // Yazı boyutunu ayarlayın
                    ageTextView.setTextColor(Color.parseColor("#6B5172"))
                    ageTextView.gravity = Gravity.LEFT
                    textInfoLayout.addView(ageTextView)

                    val genderTextView = TextView(requireContext())
                    genderTextView.text = "· $animalgender"
                    genderTextView.textSize = 18f
                    genderTextView.setTextColor(Color.parseColor("#6B5172"))
                    genderTextView.gravity = Gravity.LEFT
                    textInfoLayout.addView(genderTextView)

                    val raceTextView = TextView(requireContext())
                    raceTextView.text = "· $animalrace"
                    raceTextView.textSize = 18f //
                    raceTextView.setTextColor(Color.parseColor("#6B5172"))
                    raceTextView.gravity = Gravity.LEFT
                    textInfoLayout.addView(raceTextView)

                    val detailsButton = TextView(requireContext())


                    detailsButton.text = "Details"
                    detailsButton.textSize = 18f
                    detailsButton.setTextColor(Color.parseColor("#FFFFFF"))
                    detailsButton.setBackgroundResource(R.drawable.green_border)


                    detailsButton.setPadding(65, 5, 65, 5)
                    detailsButton.paddingRight

                    detailsButton.gravity = Gravity.CENTER

                    detailsButton.setOnClickListener {
                        // Details butonuna tıklandığında yapılacak işlemler
                        showAdDetails(ad_id)
                    }
                    val marginLayoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )


                    marginLayoutParams.setMargins(0, 16, 8, 0)


                    detailsButton.layoutParams = marginLayoutParams
                    textInfoLayout.addView(detailsButton)




                    containerLayout.addView(cardView)



                }
            } catch (e: Exception) {

            }
        }
    }
*/

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
