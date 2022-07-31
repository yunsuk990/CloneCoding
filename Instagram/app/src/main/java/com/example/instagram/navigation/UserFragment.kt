package com.example.instagram.navigation

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.circleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.instagram.LoginActivity
import com.example.instagram.MainActivity
import com.example.instagram.R
import com.example.instagram.databinding.FragmentUserBinding
import com.example.instagram.navigation.model.AlarmDTO
import com.example.instagram.navigation.model.ContentDTO
import com.example.instagram.navigation.model.FollowDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask


class UserFragment: Fragment() {
    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    var firestore: FirebaseFirestore? = null
    var uid: String? = null
    var auth: FirebaseAuth? = null
    var currentUserUid: String? = null
    private lateinit var ActivityResult: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserBinding.inflate(inflater, container, false)


        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        uid = arguments?.getString("destinationUid")
        currentUserUid = auth?.currentUser?.uid


        if( uid == currentUserUid){
            //MyPage
            binding?.accountBtnFollowSignout?.text = getString(R.string.signout)
            binding?.accountBtnFollowSignout?.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity, LoginActivity::class.java))
                auth?.signOut()
            }

        }else{
            //OtherUserPage
            binding?.accountBtnFollowSignout?.text = getString(R.string.follow)
            var mainactivity = (activity as MainActivity)
            mainactivity.binding.toolbarUsername?.text = arguments?.getString("userId")
            mainactivity?.binding.toolbarBtnBack?.setOnClickListener{
                mainactivity.binding.bottomNav.selectedItemId = R.id.action_home
            }
            mainactivity?.binding.toolbarTitleImage?.visibility = View.GONE
            mainactivity?.binding.toolbarUsername?.visibility = View.VISIBLE
            mainactivity?.binding.toolbarBtnBack?.visibility = View.VISIBLE

            binding.accountBtnFollowSignout.setOnClickListener{
                requestFollow()
            }

        }
        binding.accountRecyclerview.adapter = UserFragmentRecyclerViewAdapter()
        binding.accountRecyclerview.layoutManager = GridLayoutManager(requireActivity(), 3)
        binding.accountIvProfile.setOnClickListener{
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            ActivityResult = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()){ result ->
                if(result.resultCode == RESULT_OK){
                    var imageUri = result.data?.data
                    var uid = FirebaseAuth.getInstance().currentUser?.uid
                    var storageRef = FirebaseStorage.getInstance().reference.child("userProfileImages").child(uid!!)
                    storageRef.putFile(imageUri!!).continueWithTask{ task: Task<UploadTask.TaskSnapshot> ->
                        return@continueWithTask storageRef.downloadUrl
                    }.addOnSuccessListener { uri ->
                        var map = HashMap<String, Any>()
                        map["image"] = uri.toString()
                        firestore?.collection("profileImages")?.document(uid!!)?.set(map)
                    }
                }
            }
            ActivityResult.launch(photoPickerIntent)
        }
        getProfileImage()
        getFollowerAndFollowing()
        return binding.root
    }

    fun getFollowerAndFollowing() {
        firestore?.collection("users")?.document(uid!!)?.addSnapshotListener{ documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot == null) return@addSnapshotListener
            var followDTO = documentSnapshot.toObject(FollowDTO::class.java)
            if(followDTO?.followingCount != null){
                binding.accountTvFollowingCount.text = followDTO?.followingCount?.toString()
            }
            if(followDTO?.followerCount != null){
                binding.accountTvFollowerCount.text = followDTO?.followerCount?.toString()
                if(followDTO?.followers?.containsKey(currentUserUid!!)){
                    binding.accountBtnFollowSignout.text = getString(R.string.follow_cancel)
                    binding.accountBtnFollowSignout.background?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorLightGray), PorterDuff.Mode.MULTIPLY)

                }else{
                    if(uid != currentUserUid){
                        binding.accountBtnFollowSignout.text = getString(R.string.follow)
                        binding.accountBtnFollowSignout.background.colorFilter = null
                    }

                }
            }
        }
    }

    fun requestFollow() {
        //Save data to my account
        var tsDocFollowing = firestore?.collection("users")?.document(currentUserUid!!)
        firestore?.runTransaction{ transaction ->
            var followDTO = transaction.get(tsDocFollowing!!).toObject(FollowDTO::class.java)
            if(followDTO == null){
                followDTO = FollowDTO()
                followDTO!!.followingCount = 1
                followDTO!!.followers[uid!!] = true

                transaction.set(tsDocFollowing, followDTO)
                return@runTransaction
            }

            if(followDTO.followings.containsKey(uid)){
                //It remove following third person when a third person follow me
                followDTO?.followingCount = followDTO?.followingCount - 1
                followDTO?.followers?.remove(uid)

            }else{
                //It add following third person when a third person do not follow me
                followDTO?.followingCount = followDTO?.followingCount + 1
                followDTO?.followers[uid!!] = true
            }
            transaction.set(tsDocFollowing, followDTO)
            return@runTransaction
        }
        //Save data to third person
        var tsDocFollower = firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction{ transaction ->
            var followDTO = transaction.get(tsDocFollower!!).toObject(FollowDTO::class.java)
            if(followDTO == null){
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUserUid!!] = true
                transaction.set(tsDocFollower, followDTO!!)
                return@runTransaction
            }
            if(followDTO!!.followers.containsKey(currentUserUid)){
                //It remove following third person when a third person follow me
                followDTO!!.followerCount = followDTO!!.followerCount - 1
                followDTO!!.followers?.remove(currentUserUid!!)

            }else{
                //It add following third person when a third person do not follow me
                followDTO!!.followerCount  = followDTO!!.followerCount + 1
                followDTO!!.followers[currentUserUid!!] = true
                followerAlarm(uid!!)
            }
            transaction.set(tsDocFollower, followDTO!!)
            return@runTransaction
        }



    }

    fun followerAlarm(destinationUid: String){
        var alarmDto = AlarmDTO()
        alarmDto.destinationUid = destinationUid
        alarmDto.userId = auth?.currentUser?.email
        alarmDto.uid = auth?.currentUser?.uid
        alarmDto.kind = 2
        alarmDto.timestamp = System.currentTimeMillis()
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDto)
    }

    fun getProfileImage() {
        firestore?.collection("profileImages")?.document(uid!!)?.addSnapshotListener{ documentSnapshot, firbaseFirestoreException ->
            if(documentSnapshot == null) return@addSnapshotListener
            if(documentSnapshot.data != null){
                var url = documentSnapshot?.data!!["image"]
                Glide.with(requireContext()).load(url).apply(RequestOptions().circleCrop()).into(binding.accountIvProfile)
            }
        }
    }

    inner class UserFragmentRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTos: ArrayList<ContentDTO> = arrayListOf()
        init {
            firestore?.collection("images")?.whereEqualTo("uid", uid)?.addSnapshotListener { value, error ->
                //Sometimes, This code return null of querySnapshot when it signout
                if(value == null) return@addSnapshotListener
                //Get data
                for(snapshot in value.documents){
                    contentDTos.add(snapshot.toObject(ContentDTO::class.java)!!)
                }
                binding.accountTvPostCount.text = contentDTos.size.toString()
                notifyDataSetChanged()  // RecyclerView update
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3
            var imageview = ImageView(parent.context)
            imageview.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            return CustomViewHolder(imageview)

        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageView = (holder as CustomViewHolder).imageview
            Glide.with(holder.itemView.context).load(contentDTos[position].imageUrl).apply(RequestOptions().centerCrop()).into(imageView)
        }

        override fun getItemCount(): Int {
            return contentDTos.size
        }

        inner class CustomViewHolder(var imageview: ImageView): RecyclerView.ViewHolder(imageview){

        }
    }
}
