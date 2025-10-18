package io.github.sanlorng.warmtones.ui.screen.contacts

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.sanlorng.warmtones.data.SettingsRepository
import io.github.sanlorng.warmtones.tts.SystemTtsHelper
import io.github.sanlorng.warmtones.tts.TtsHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination

open class ContactsViewModel(application: Application, private val settingsRepository: SettingsRepository) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(ContactsState())
    val state = _state.asStateFlow()

    private val _sideEffects = MutableSharedFlow<SideEffect>()
    val sideEffects = _sideEffects.asSharedFlow()

    protected val ttsHelper: TtsHelper
    private var dialConfirmationJob: Job? = null

    init {
        ttsHelper = SystemTtsHelper(application) {
            _state.update { it.copy(showInstallTtsDataDialog = true) }
        }

        viewModelScope.launch {
            settingsRepository.isLeftHandedModeEnabled.collect { isEnabled ->
                _state.update { it.copy(isLeftHandedModeEnabled = isEnabled) }
            }
        }

        // Check for permissions
        val permissions = arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE)
        val allGranted = permissions.all { ContextCompat.checkSelfPermission(getApplication(), it) == PackageManager.PERMISSION_GRANTED }
        onEvent(ContactsEvent.PermissionResult(allGranted))
    }

    fun onEvent(event: ContactsEvent) {
        when (event) {
            is ContactsEvent.PermissionResult -> {
                viewModelScope.launch {
                    val currentlyGranted = _state.value.permissionGranted
                    if (event.isGranted != currentlyGranted) {
                        _state.update { it.copy(permissionGranted = event.isGranted) }
                    }
                    if (event.isGranted) {
                        loadContacts()
                    }
                }
            }
            ContactsEvent.LoadContacts -> {
                loadContacts()
            }
            is ContactsEvent.SpeakContact -> {
                viewModelScope.launch { ttsHelper.speak(event.contact.name) }
            }
            ContactsEvent.SelectNextContact -> {
                selectNextContact()
            }
            ContactsEvent.SelectPreviousContact -> {
                selectPreviousContact()
            }
            is ContactsEvent.SelectContact -> {
                selectContact(event.contact)
            }
            ContactsEvent.DialContact -> {
                handleDialContact()
            }
            ContactsEvent.DismissInstallTtsDataDialog -> {
                _state.update { it.copy(showInstallTtsDataDialog = false) }
            }
        }
    }

    private fun handleDialContact() {
        val state = _state.value
        if (state.selectedIndex == -1) return

        val contact = state.contacts[state.selectedIndex]
        handleDialContact(contact)
    }
    
    fun handleDialContact(contact: Contact) {
        viewModelScope.launch {
            val isConfirmationEnabled = settingsRepository.isDialConfirmationEnabled.first()

            if (isConfirmationEnabled && !_state.value.isDialConfirmationPending) {
                _state.update { it.copy(isDialConfirmationPending = true) }
                ttsHelper.speak("即将拨打电话给 ${contact.name}，如需确认，请再次按下拨打按钮")
                dialConfirmationJob = viewModelScope.launch {
                    delay(10000)
                    _state.update { it.copy(isDialConfirmationPending = false) }
                }
            } else {
                dialConfirmationJob?.cancel()
                _state.update { it.copy(isDialConfirmationPending = false) }
                _sideEffects.emit(SideEffect.Dial(contact.phoneNumber))
            }
        }
    }

    private fun selectContact(contact: Contact) {
        dialConfirmationJob?.cancel()
        _state.update { it.copy(isDialConfirmationPending = false) }
        val index = _state.value.contacts.indexOf(contact)
        if (index != -1) {
            _state.update { it.copy(selectedIndex = index) }
            viewModelScope.launch { ttsHelper.speak(contact.name) }
        }
    }

    private fun selectNextContact() {
        dialConfirmationJob?.cancel()
        _state.update { it.copy(isDialConfirmationPending = false) }
        val state = _state.value
        if (state.contacts.isNotEmpty()) {
            val nextIndex = (state.selectedIndex + 1) % state.contacts.size
            _state.update { it.copy(selectedIndex = nextIndex) }
            viewModelScope.launch { ttsHelper.speak(state.contacts[nextIndex].name) }
        }
    }

    private fun selectPreviousContact() {
        dialConfirmationJob?.cancel()
        _state.update { it.copy(isDialConfirmationPending = false) }
        val state = _state.value
        if (state.contacts.isNotEmpty()) {
            val prevIndex = (state.selectedIndex - 1 + state.contacts.size) % state.contacts.size
            _state.update { it.copy(selectedIndex = prevIndex) }
            viewModelScope.launch { ttsHelper.speak(state.contacts[prevIndex].name) }
        }
    }

    private fun loadContacts() {
        viewModelScope.launch {
            val contacts = mutableListOf<Contact>()
            val contentResolver = getApplication<Application>().contentResolver
            val cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone._ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_URI
                ),
                null,
                null,
                null
            )
            cursor?.use { 
                val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoUriIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
                while (it.moveToNext()) {
                    val id = it.getLong(idIndex)
                    val name = it.getString(nameIndex)
                    val phoneNumber = it.getString(numberIndex)
                    val photoUri = it.getString(photoUriIndex)
                    contacts.add(Contact(id, name, phoneNumber, photoUri))
                }
            }

            val sortedContacts = contacts.sortedBy { getSortKey(it.name) }

            _state.update { it.copy(contacts = sortedContacts) }
            
            if (sortedContacts.isNotEmpty()) {
                _state.update { it.copy(selectedIndex = 0) }
                ttsHelper.speak(sortedContacts[0].name)
            }
        }
    }

    private fun getSortKey(name: String): String {
        val format = HanyuPinyinOutputFormat().apply {
            caseType = HanyuPinyinCaseType.UPPERCASE
            toneType = HanyuPinyinToneType.WITHOUT_TONE
        }
        val pinyin = StringBuilder()
        for (char in name) {
            val pinyinArray = try {
                PinyinHelper.toHanyuPinyinStringArray(char, format)
            } catch (e: BadHanyuPinyinOutputFormatCombination) {
                Log.e("Pinyin4j", "Failed to convert char to pinyin", e)
                null
            }
            if (!pinyinArray.isNullOrEmpty()) {
                pinyin.append(pinyinArray[0])
            } else {
                pinyin.append(char.uppercaseChar())
            }
        }
        return pinyin.toString()
    }

    override fun onCleared() {
        ttsHelper.shutdown()
        super.onCleared()
    }

    sealed class SideEffect {
        data class Dial(val phoneNumber: String) : SideEffect()
    }
}