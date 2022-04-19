package com.progcorp.unitedmessengers.ui.conversations

import androidx.lifecycle.*
import com.progcorp.unitedmessengers.data.Event
import com.progcorp.unitedmessengers.data.db.vk.VKConversations
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.ui.DefaultViewModel
import com.progcorp.unitedmessengers.util.addNewItem
import com.progcorp.unitedmessengers.util.updateItemAt

class ConversationViewModelFactory(private val conversation: Conversation) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ConversationsViewModel() as T
    }
}

class ConversationsViewModel() : DefaultViewModel(),VKConversations.OnConversationsFetched {

    private val _conversations: VKConversations = VKConversations(this)

    private val _updatedConversation = MutableLiveData<Conversation>()
    private val _selectedConversation = MutableLiveData<Event<Conversation>>()

    var selectedConversation: LiveData<Event<Conversation>> = _selectedConversation
    val conversationsList = MediatorLiveData<MutableList<Conversation>>()

    init {
        conversationsList.addSource(_updatedConversation) { newConversation ->
            val conversation = conversationsList.value?.find { it.id == newConversation.id }
            if (conversation == null) {
                conversationsList.addNewItem(newConversation)
            }
            else {
                conversationsList.updateItemAt(newConversation, conversationsList.value!!.indexOf(conversation))
            }
        }
    }

    private fun setupConversations() {
        loadAndObserveConversations()
    }

    private fun loadAndObserveConversations() {
        _conversations.getConversations(0)
    }

    override fun showConversations(chats: ArrayList<Conversation>) {
        for (conversation in chats) {
            _updatedConversation.value = conversation
        }
    }

    fun selectConversationPressed(conversation: Conversation) {
        _selectedConversation.value = Event(conversation)
    }

}