package com.example.instagram.navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.instagram.databinding.ActivityCommentBinding
import com.example.instagram.databinding.ItemCommentBinding
import com.example.instagram.navigation.model.AlarmDTO
import com.example.instagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CommentActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityCommentBinding.inflate(layoutInflater)
    }
    var contentUid: String? = null
    var destinationUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        contentUid = intent.getStringExtra("contentUid")
        destinationUid = intent.getStringExtra("destinationUid")
        binding.commentRecyclerview.adapter = CommentRecyclerviewAdapter()
        binding.commentRecyclerview.layoutManager = LinearLayoutManager(this)

        binding.commentBtnSend?.setOnClickListener {
            var comment = ContentDTO.Comment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = binding.commentEditMessage.text.toString()
            comment.timestamp = System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("images").document(contentUid!!)
                .collection("comments").document()
                .set(comment)
            commentAlarm(destinationUid!!, comment.comment!!)
            binding.commentEditMessage.setText("")
        }
    }

    fun commentAlarm(destinationUid: String, message: String){
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
        alarmDTO.kind = 1
        alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
        alarmDTO.timestamp = System.currentTimeMillis()
        alarmDTO.message = message
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

    }
    inner class CommentRecyclerviewAdapter(): RecyclerView.Adapter<CommentRecyclerviewAdapter.CustomViewHolder>(){

        var comments: ArrayList<ContentDTO.Comment> = arrayListOf()
        init {
            FirebaseFirestore.getInstance()
                .collection("images")
                .document(contentUid!!)
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener{ querySnapshot, Exception ->
                    comments.clear()
                    if(querySnapshot == null)return@addSnapshotListener
                    for(snapshot in querySnapshot.documents!!){
                        comments.add(snapshot.toObject(ContentDTO.Comment::class.java)!!)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            var view = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return CustomViewHolder(view)

        }

        inner class CustomViewHolder(private val bind: ItemCommentBinding): RecyclerView.ViewHolder(bind.root){
            var commentviewitemImageview = bind.commentviewitemImageview
            var commentviewitemTextview = bind.commentviewitemTextview
            var commentviewitemTextviewComment = bind.commentviewitemTextviewComment
        }

        override fun onBindViewHolder(holder: CommentRecyclerviewAdapter.CustomViewHolder, position: Int) {
            holder.commentviewitemTextviewComment.text = comments[position].comment
            holder.commentviewitemTextview.text = comments[position].userId

            FirebaseFirestore.getInstance()
                .collection("profileImages")
                .document(comments[position].uid!!)
                .get()
                .addOnCompleteListener{ task->
                    if(task.isSuccessful){
                        var url = task.result!!["image"]
                        Glide.with(holder.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(holder.commentviewitemImageview)
                    }
                }
        }

        override fun getItemCount(): Int {
            return comments.size
        }
    }
}