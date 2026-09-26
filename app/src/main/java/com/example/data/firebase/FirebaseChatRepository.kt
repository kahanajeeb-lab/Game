package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.model.ChatMessage
import com.example.data.model.FriendRequest
import com.example.data.model.UserProfile
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class FirebaseChatRepository(private val context: Context) {

    private val auth: FirebaseAuth
    private val database: FirebaseDatabase
    private val usersRef: DatabaseReference
    private val chatIdsRef: DatabaseReference
    private val requestsRef: DatabaseReference
    private val friendsRef: DatabaseReference
    private val chatsRef: DatabaseReference

    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    val currentUserProfile: StateFlow<UserProfile?> = _currentUserProfile.asStateFlow()

    init {
        // Initialize Firebase with target project credentials
        val app = if (FirebaseApp.getApps(context).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setApplicationId("1:68083133837:web:42e126e1672d06df121c4")
                .setApiKey("AIzaSyBTQKtAjDWLk-IwfOuSJ6ST4qEyON4dKg")
                .setProjectId("chat-7305d")
                .setDatabaseUrl("https://chat-7305d-default-rtdb.firebaseio.com")
                .setStorageBucket("chat-7305d.appspot.com")
                .setGcmSenderId("68083133837")
                .build()
            FirebaseApp.initializeApp(context, options, "RetroDashMessenger")
        } else {
            FirebaseApp.getInstance()
        }

        auth = FirebaseAuth.getInstance(app)
        database = FirebaseDatabase.getInstance(app, "https://chat-7305d-default-rtdb.firebaseio.com")

        usersRef = database.getReference("users")
        chatIdsRef = database.getReference("chat_ids")
        requestsRef = database.getReference("requests")
        friendsRef = database.getReference("friends")
        chatsRef = database.getReference("chats")

        // Track current auth state and profile
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                listenToUserProfile(user.uid)
                setupPresence(user.uid)
            } else {
                _currentUserProfile.value = null
            }
        }

        auth.currentUser?.let { user ->
            listenToUserProfile(user.uid)
            setupPresence(user.uid)
        }
    }

    val currentUserId: String?
        get() = auth.currentUser?.uid

    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    private fun listenToUserProfile(uid: String) {
        usersRef.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profile = snapshot.getValue(UserProfile::class.java)
                if (profile != null) {
                    _currentUserProfile.value = profile
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepo", "Error listening to profile: ${error.message}")
            }
        })
    }

    private fun setupPresence(uid: String) {
        val userStatusRef = usersRef.child(uid).child("isOnline")
        val lastSeenRef = usersRef.child(uid).child("lastSeen")
        val connectedRef = database.getReference(".info/connected")

        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    userStatusRef.setValue(true)
                    userStatusRef.onDisconnect().setValue(false)
                    lastSeenRef.onDisconnect().setValue(ServerValue.TIMESTAMP)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // ==========================================
    // AUTHENTICATION
    // ==========================================

    suspend fun registerWithEmail(email: String, pass: String, name: String, phone: String = ""): Result<UserProfile> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user ?: throw Exception("Registration failed, empty user")
            val generatedChatId = "SC-" + Random.nextInt(100000, 999999).toString()
            val profile = UserProfile(
                uid = user.uid,
                email = email,
                phone = phone,
                displayName = name.ifBlank { "User" },
                avatarUrl = "avatar_${Random.nextInt(1, 8)}",
                chatId = generatedChatId,
                about = "Hey there! I am using SecureChat.",
                isOnline = true,
                lastSeen = System.currentTimeMillis()
            )
            usersRef.child(user.uid).setValue(profile).await()
            chatIdsRef.child(generatedChatId).setValue(user.uid).await()
            _currentUserProfile.value = profile
            Result.success(profile)
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Register error", e)
            Result.failure(e)
        }
    }

    suspend fun loginWithEmail(email: String, pass: String): Result<UserProfile> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val user = result.user ?: throw Exception("Login failed, empty user")
            val snapshot = usersRef.child(user.uid).get().await()
            var profile = snapshot.getValue(UserProfile::class.java)
            if (profile == null) {
                val chatId = "SC-" + Random.nextInt(100000, 999999).toString()
                profile = UserProfile(
                    uid = user.uid,
                    email = user.email ?: email,
                    displayName = user.displayName ?: "Secure User",
                    avatarUrl = "avatar_1",
                    chatId = chatId,
                    isOnline = true,
                    lastSeen = System.currentTimeMillis()
                )
                usersRef.child(user.uid).setValue(profile).await()
                chatIdsRef.child(chatId).setValue(user.uid).await()
            }
            _currentUserProfile.value = profile
            Result.success(profile)
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Login error", e)
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fast login for testing / demo accounts on single or multi-device emulator.
     */
    suspend fun quickLoginOrRegister(demoEmail: String, demoPass: String, displayName: String, defaultChatId: String, defaultAvatar: String): Result<UserProfile> {
        return try {
            val profile = try {
                loginWithEmail(demoEmail, demoPass).getOrThrow()
            } catch (loginEx: Exception) {
                // Register if not exists
                registerWithEmail(demoEmail, demoPass, displayName).getOrThrow()
            }
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        currentUserId?.let { uid ->
            usersRef.child(uid).child("isOnline").setValue(false)
            usersRef.child(uid).child("lastSeen").setValue(System.currentTimeMillis())
        }
        auth.signOut()
        _currentUserProfile.value = null
    }

    // ==========================================
    // PROFILE MANAGEMENT
    // ==========================================

    suspend fun updateProfile(name: String, about: String, avatarUrl: String): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            val updates = mapOf(
                "displayName" to name,
                "about" to about,
                "avatarUrl" to avatarUrl
            )
            usersRef.child(uid).updateChildren(updates).await()
            _currentUserProfile.value = _currentUserProfile.value?.copy(
                displayName = name,
                about = about,
                avatarUrl = avatarUrl
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // CONNECTIONS & FRIEND REQUESTS
    // ==========================================

    suspend fun findUserByChatId(chatId: String): UserProfile? {
        val cleanId = chatId.trim().uppercase()
        val formatted = if (cleanId.startsWith("SC-")) cleanId else "SC-$cleanId"
        val snap = chatIdsRef.child(formatted).get().await()
        val targetUid = snap.getValue(String::class.java) ?: return null
        val userSnap = usersRef.child(targetUid).get().await()
        return userSnap.getValue(UserProfile::class.java)
    }

    suspend fun sendFriendRequest(targetChatId: String): Result<String> {
        val myProfile = _currentUserProfile.value ?: return Result.failure(Exception("No current user"))
        val targetUser = findUserByChatId(targetChatId) ?: return Result.failure(Exception("No user found with Chat ID: $targetChatId"))

        if (targetUser.uid == myProfile.uid) {
            return Result.failure(Exception("You cannot connect with your own Chat ID"))
        }

        val requestId = requestsRef.child(targetUser.uid).push().key ?: return Result.failure(Exception("Failed to generate request"))
        val request = FriendRequest(
            id = requestId,
            fromUid = myProfile.uid,
            fromName = myProfile.displayName,
            fromAvatar = myProfile.avatarUrl,
            fromChatId = myProfile.chatId,
            toUid = targetUser.uid,
            status = "pending",
            timestamp = System.currentTimeMillis()
        )

        return try {
            requestsRef.child(targetUser.uid).child(requestId).setValue(request).await()
            Result.success("Request sent to ${targetUser.displayName}!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeFriendRequests(): Flow<List<FriendRequest>> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<FriendRequest>()
                for (child in snapshot.children) {
                    val req = child.getValue(FriendRequest::class.java)
                    if (req != null && req.status == "pending") {
                        list.add(req)
                    }
                }
                trySend(list.sortedByDescending { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepo", "Requests error: ${error.message}")
            }
        }

        requestsRef.child(uid).addValueEventListener(listener)
        awaitClose { requestsRef.child(uid).removeEventListener(listener) }
    }

    suspend fun respondToRequest(request: FriendRequest, accept: Boolean): Result<Unit> {
        val myUid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        val myProfile = _currentUserProfile.value ?: return Result.failure(Exception("Profile missing"))

        return try {
            if (accept) {
                requestsRef.child(myUid).child(request.id).child("status").setValue("accepted").await()
                
                // Add to both users' friends lists
                val senderSnap = usersRef.child(request.fromUid).get().await()
                val senderProfile = senderSnap.getValue(UserProfile::class.java) ?: UserProfile(
                    uid = request.fromUid,
                    displayName = request.fromName,
                    avatarUrl = request.fromAvatar,
                    chatId = request.fromChatId
                )

                friendsRef.child(myUid).child(request.fromUid).setValue(senderProfile).await()
                friendsRef.child(request.fromUid).child(myUid).setValue(myProfile).await()
            } else {
                requestsRef.child(myUid).child(request.id).child("status").setValue("rejected").await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFriend(friendUid: String): Result<Unit> {
        val myUid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            friendsRef.child(myUid).child(friendUid).removeValue().await()
            friendsRef.child(friendUid).child(myUid).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeFriends(): Flow<List<UserProfile>> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<UserProfile>()
                for (child in snapshot.children) {
                    val friend = child.getValue(UserProfile::class.java)
                    if (friend != null) {
                        list.add(friend)
                    }
                }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        friendsRef.child(uid).addValueEventListener(listener)
        awaitClose { friendsRef.child(uid).removeEventListener(listener) }
    }

    // ==========================================
    // REALTIME CHAT & DELIVERY TICKS
    // ==========================================

    fun getChatRoomId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    suspend fun sendMessage(friendUid: String, text: String): Result<ChatMessage> {
        val myUid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        val roomId = getChatRoomId(myUid, friendUid)
        val msgKey = chatsRef.child(roomId).child("messages").push().key ?: return Result.failure(Exception("Message key generation failed"))

        val message = ChatMessage(
            id = msgKey,
            senderId = myUid,
            receiverId = friendUid,
            text = text.trim(),
            timestamp = System.currentTimeMillis(),
            status = "sent" // 1 tick: sent
        )

        return try {
            chatsRef.child(roomId).child("messages").child(msgKey).setValue(message).await()
            
            // Check if receiver is online to immediately mark as delivered
            val receiverSnap = usersRef.child(friendUid).child("isOnline").get().await()
            val isReceiverOnline = receiverSnap.getValue(Boolean::class.java) ?: false
            if (isReceiverOnline) {
                chatsRef.child(roomId).child("messages").child(msgKey).child("status").setValue("delivered")
            }

            // Update recent chat timestamp and preview
            chatsRef.child(roomId).child("lastMessage").setValue(message.text)
            chatsRef.child(roomId).child("lastTimestamp").setValue(message.timestamp)

            Result.success(message)
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Error sending message", e)
            Result.failure(e)
        }
    }

    suspend fun sendVoiceMessage(
        friendUid: String,
        durationSec: Int,
        presetName: String,
        audioDataUri: String,
        summaryText: String = "🎤 Voice message ($durationSec s)"
    ): Result<ChatMessage> {
        val myUid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        val roomId = getChatRoomId(myUid, friendUid)
        val msgKey = chatsRef.child(roomId).child("messages").push().key ?: return Result.failure(Exception("Message key generation failed"))

        val message = ChatMessage(
            id = msgKey,
            senderId = myUid,
            receiverId = friendUid,
            text = summaryText,
            timestamp = System.currentTimeMillis(),
            status = "sent",
            isVoiceMessage = true,
            voiceDurationSec = durationSec,
            voicePresetName = presetName,
            audioDataUri = audioDataUri
        )

        return try {
            chatsRef.child(roomId).child("messages").child(msgKey).setValue(message).await()

            val receiverSnap = usersRef.child(friendUid).child("isOnline").get().await()
            val isReceiverOnline = receiverSnap.getValue(Boolean::class.java) ?: false
            if (isReceiverOnline) {
                chatsRef.child(roomId).child("messages").child(msgKey).child("status").setValue("delivered")
            }

            chatsRef.child(roomId).child("lastMessage").setValue(message.text)
            chatsRef.child(roomId).child("lastTimestamp").setValue(message.timestamp)

            Result.success(message)
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Error sending voice message", e)
            Result.failure(e)
        }
    }

    fun observeMessages(friendUid: String): Flow<List<ChatMessage>> = callbackFlow {
        val myUid = currentUserId
        if (myUid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val roomId = getChatRoomId(myUid, friendUid)
        val messagesNode = chatsRef.child(roomId).child("messages")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ChatMessage>()
                for (child in snapshot.children) {
                    val msg = child.getValue(ChatMessage::class.java)
                    if (msg != null) {
                        list.add(msg)
                        // If I received this message and it's not marked read yet, mark it read!
                        if (msg.receiverId == myUid && msg.status != "read") {
                            child.ref.child("status").setValue("read")
                        }
                    }
                }
                trySend(list.sortedBy { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepo", "Error observing messages: ${error.message}")
            }
        }

        messagesNode.addValueEventListener(listener)
        awaitClose { messagesNode.removeEventListener(listener) }
    }

    fun observeUserPresence(targetUid: String): Flow<Pair<Boolean, Long>> = callbackFlow {
        val userNode = usersRef.child(targetUid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isOnline = snapshot.child("isOnline").getValue(Boolean::class.java) ?: false
                val lastSeen = snapshot.child("lastSeen").getValue(Long::class.java) ?: 0L
                trySend(Pair(isOnline, lastSeen))
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        userNode.addValueEventListener(listener)
        awaitClose { userNode.removeEventListener(listener) }
    }

    fun setTyping(friendUid: String, isTyping: Boolean) {
        val myUid = currentUserId ?: return
        val roomId = getChatRoomId(myUid, friendUid)
        chatsRef.child(roomId).child("typing").child(myUid).setValue(isTyping)
        if (isTyping) {
            chatsRef.child(roomId).child("typing").child(myUid).onDisconnect().setValue(false)
        }
    }

    fun observeTyping(friendUid: String): Flow<Boolean> = callbackFlow {
        val myUid = currentUserId
        if (myUid == null) {
            trySend(false)
            close()
            return@callbackFlow
        }

        val roomId = getChatRoomId(myUid, friendUid)
        val typingNode = chatsRef.child(roomId).child("typing").child(friendUid)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isTyping = snapshot.getValue(Boolean::class.java) ?: false
                trySend(isTyping)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        typingNode.addValueEventListener(listener)
        awaitClose { typingNode.removeEventListener(listener) }
    }
}
