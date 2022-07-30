package com.example.instagram.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagram.R
import com.example.instagram.databinding.FragmentDetailBinding
import com.example.instagram.databinding.ItemDetailBinding
import com.example.instagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DetailViewFragment: Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    var firestore: FirebaseFirestore? = null
    var uid: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        _binding!!.detailfragmentRecyclerview.adapter = DetailViewRecyclerViewAdapter()
        _binding!!.detailfragmentRecyclerview.layoutManager = LinearLayoutManager(activity)
        return binding.root
    }

    inner class DetailViewRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()

        init {
            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, e ->
                contentDTOs.clear()
                contentUidList.clear()
                if(querySnapshot == null) return@addSnapshotListener
                for(snapshot in querySnapshot!!.documents){
                    var item = snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            var view = ItemDetailBinding.inflate(LayoutInflater.from(parent.context), parent ,false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(private val bind: ItemDetailBinding) : RecyclerView.ViewHolder(bind.root){
            var detailviewitemProfileTextview = bind.detailviewitemProfileTextview
            var detailviewitemImageviewContent = bind.detailviewitemImageviewContent
            var detailviewitemExplainTextview = bind.detailviewitemExplainTextview
            var detailviewitemFavoriteTextview = bind.detailviewitemFavoriteTextview
            var detailviewitemProfileImage = bind.detailviewitemProfileImage
            var detailviewitemFavoriteImageview = bind.detailviewitemFavoriteImageview
            var detailviewitemCommentImageview = bind.detailviewitemCommentImageview
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var holder = (holder as CustomViewHolder)
            holder.detailviewitemProfileTextview.text = contentDTOs!![position].userId
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).centerCrop().into(holder.detailviewitemImageviewContent)
            holder.detailviewitemExplainTextview.text = contentDTOs!![position].explain
            holder.detailviewitemFavoriteTextview.text = "Likes " + contentDTOs!![position].favoriteCount
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).into(holder.detailviewitemProfileImage)

            //This code is when the button is clicked
            holder.detailviewitemFavoriteImageview.setOnClickListener{
                favoriteEvent(position)
            }

            //This code is when the page is loaded
            if(contentDTOs!![position].favorites.containsKey(uid)){
                //This is like status
                holder.detailviewitemFavoriteImageview.setImageResource(R.drawable.ic_favorite)
            }else{
                //This is unlike status
                holder.detailviewitemFavoriteImageview.setImageResource(R.drawable.ic_favorite_border)
            }

            holder.detailviewitemProfileImage.setOnClickListener{
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid", contentDTOs[position].uid)
                bundle.putString("userId", contentDTOs[position].userId)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, fragment)?.commit()
            }

            holder.detailviewitemCommentImageview.setOnClickListener{ v->
                var intent = Intent(v.context, CommentActivity::class.java)
                intent.putExtra("contentUid", contentUidList[position])
                startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        fun favoriteEvent(position: Int){
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction{ transaction ->
                var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)
                if(contentDTO!!.favorites.containsKey(uid)){
                    //Where the button is clicked
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount - 1
                    contentDTO?.favorites.remove(uid)
                }else{
                    //When the button is not clicked
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount + 1
                    contentDTO?.favorites[uid!!] = true

                }
                transaction.set(tsDoc, contentDTO)
            }
        }
    }
}