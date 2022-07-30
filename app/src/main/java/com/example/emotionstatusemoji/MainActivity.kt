package com.example.emotionstatusemoji

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

//For RecyclerView start
data class UserClass(
    val displayName: String="",
    val emoji: String=""
)
class UserViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView)
//For RecyclerView end

class MainActivity : AppCompatActivity() {
    companion object{
        const val TAG="MainActivity"
        const val emoji="\uD83C\uDFC4"
    }
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title="List Of User With Status"
        //check newUser
        auth=FirebaseAuth.getInstance()
        val db = Firebase.firestore

        // Create a new user with a first and last name
        val user = hashMapOf(
            "displayName" to (auth.currentUser?.displayName ?: ""),
            "emoji" to emoji,
            "time" to FieldValue.serverTimestamp()

        )
        if (newUser){
            auth.currentUser?.let {
                db.collection("users")
                    .document(it.uid)
                    .set(user)
                    .addOnSuccessListener { documentReference ->
                        Log.i(TAG, "DocumentSnapshot added with ID:}")
                    }
                    .addOnFailureListener { e ->
                        Log.i(TAG, "Error adding document", e)
                    }
            }
        }
        //read data from firestore
        val query=db.collection("users")
        val options = FirestoreRecyclerOptions.Builder<UserClass>().setQuery(query,UserClass::class.java)
            .setLifecycleOwner(this).build()
        val adapter=object :FirestoreRecyclerAdapter<UserClass,UserViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
                val view=LayoutInflater.from(this@MainActivity).inflate(R.layout.activity_display_name,parent,false)
                return UserViewHolder(view)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: UserClass) {
                val name=holder.itemView.findViewById<TextView>(R.id.tvName)
                val emoji=holder.itemView.findViewById<TextView>(R.id.tvEmoji)
                name.text=model.displayName
                emoji.text=model.emoji
                Log.i(TAG,"TIME")
            }
        }
        rvUserId.adapter=adapter
        rvUserId.layoutManager=LinearLayoutManager(this)


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId==R.id.logoutId){
            auth.signOut()
            if (auth.currentUser==null){
                startActivity(Intent(this@MainActivity,LoginActivity::class.java))
            }
        }
        if (item.itemId==R.id.editbtn){
            Toast.makeText(this,"Only Emoji Are Allowed !!",Toast.LENGTH_LONG).show()
            val editText=EditText(this)
            //input Only Filter
            val lengthFilter=InputFilter.LengthFilter(9)
            editText.filters= arrayOf(lengthFilter,EmojiFilter())
            val alertDialog=AlertDialog.Builder(this)
                .setTitle("Update Your Emoji")
                .setView(editText)
                .setNegativeButton("NO",null)
                .setPositiveButton("YES",null)
                .show()
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val emoji=editText.text.toString()
                val currentUser=auth.currentUser
                if (emoji.isBlank()){
                    Toast.makeText(this,"Does Not Allow Empty !"+ (currentUser?.uid ?: ""),Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (currentUser==null){
                    Toast.makeText(this,"Your Not Signin !",Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                //update firestore value with new value
                val db = Firebase.firestore
                db.collection("users").document(currentUser.uid)
                    .update("emoji",emoji)
                alertDialog.dismiss()

            }
        }
        return super.onOptionsItemSelected(item)
    }
}