package com.example.instagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagram.databinding.FragmentDetailBinding
import com.example.instagram.databinding.ItemDetailBinding
import com.example.instagram.navigation.model.ContentDTO
import com.google.firebase.firestore.FirebaseFirestore

class DetailViewFragment: Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    var firestore: FirebaseFirestore? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()

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
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var holder = (holder as CustomViewHolder)
            holder.detailviewitemProfileTextview.text = contentDTOs!![position].userId
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).into(holder.detailviewitemImageviewContent)
            holder.detailviewitemExplainTextview.text = contentDTOs!![position].explain
            holder.detailviewitemFavoriteTextview.text = "Likes " + contentDTOs!![position].favoriteCount
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).into(holder.detailviewitemProfileImage)
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

    }

}

