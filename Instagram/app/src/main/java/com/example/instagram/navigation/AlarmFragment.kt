package com.example.instagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.instagram.R
import com.example.instagram.databinding.FragmentAlarmBinding
import com.example.instagram.databinding.FragmentDetailBinding
import com.example.instagram.databinding.ItemCommentBinding
import com.example.instagram.navigation.model.AlarmDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class AlarmFragment: Fragment() {

    private var _binding: FragmentAlarmBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAlarmBinding.inflate(inflater, container, false)
        binding.alarmfragmentRecyclerview.adapter = AlarmRecyclerviewAdapter()
        binding.alarmfragmentRecyclerview.layoutManager = LinearLayoutManager(activity)

        return binding.root
    }

    inner class AlarmRecyclerviewAdapter: RecyclerView.Adapter<AlarmRecyclerviewAdapter.CustomViewHolder>(){
        var alarmDtoList: ArrayList<AlarmDTO> = arrayListOf()

        init {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destination", uid).addSnapshotListener{ querySnapshot, exception ->
                alarmDtoList.clear()
                if(querySnapshot == null) return@addSnapshotListener
                for(snapshot in querySnapshot.documents){
                    alarmDtoList.add(snapshot.toObject(AlarmDTO::class.java)!!)
                }
            }
        }

        inner class CustomViewHolder(private val bind: ItemCommentBinding): RecyclerView.ViewHolder(bind.root){
            var commentviewitemImageview = bind.commentviewitemImageview
            var commentviewitemTextview = bind.commentviewitemTextview
            var commentviewitemTextviewComment = bind.commentviewitemTextviewComment
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            var view = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            FirebaseFirestore.getInstance().collection("profileImages").document(alarmDtoList[position].uid!!).get().addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    val url = task.result!!["image"]
                    Glide.with(requireContext()).load(url).apply(RequestOptions().circleCrop()).into(holder.commentviewitemImageview)
                }
            }
            when(alarmDtoList[position].kind){
                0 -> {
                    var str_0 = alarmDtoList[position].userId + getString(R.string.alarm_favorite)
                    holder.commentviewitemTextview.text = str_0
                }
                1 -> {
                    var str_1 = alarmDtoList[position].userId + " " + getString(R.string.alarm_comment) + " of " + alarmDtoList[position].message
                    holder.commentviewitemTextview.text = str_1
                }
                2 -> {
                    var str_2 = alarmDtoList[position].userId + " " + getString(R.string.alarm_follow)
                    holder.commentviewitemTextview.text = str_2
                }

            }
            holder.commentviewitemTextviewComment.visibility = View.INVISIBLE
        }

        override fun getItemCount(): Int {
            return alarmDtoList.size
        }
    }

}