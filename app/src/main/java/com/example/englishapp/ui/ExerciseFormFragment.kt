package com.example.englishapp.ui

import android.os.Bundle
import android.view.View
import android.view.ViewParent
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.englishapp.R
import com.example.englishapp.adapter.MatchMeaningAdapter
import com.example.englishapp.model.ExerciseType
import com.example.englishapp.model.MatchMeaning
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class ExerciseFormFragment : Fragment(R.layout.exercise_form) {
    private val vm: AdminViewModel by activityViewModels()

    // sent by ExerciseListFragment
    private var section = ""
    private var sessionId = 0

    private var selectedType: ExerciseType? = null

    // rows of the "Match meaning" form, kept here so we can check them when saving
    private val pairs = arrayListOf(MatchMeaning(), MatchMeaning(), MatchMeaning())
    private lateinit var pairsAdapter: MatchMeaningAdapter

    private lateinit var tvError: TextView

    private companion object {
        const val KEY_TYPE = "selected_type"
        const val BLANK = "___"
        const val AUDIO_TTS = "Generate with text-to-speech"
        const val AUDIO_UPLOAD = "Upload an audio file"
    }

    // ---------------------------------------------------------------- setup

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        section = requireArguments().getString("section").orEmpty()
        sessionId = requireArguments().getInt("sessionId")
        val sessionTitle = requireArguments().getString("sessionTitle").orEmpty()

        tvError = view.findViewById<TextView>(R.id.tvExerciseError)
        view.findViewById<TextView>(R.id.tvFormLocation).text = "$section • $sessionTitle"
        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            findNavController().navigateUp()
        }

        setUpMultipleChoice(view)
        setUpScramble(view)
        setUpMatchMeaning(view)
        setUpRepeatPhrase(view)
        setUpTypeDropdown(view, savedInstanceState)   // last: it may show a form straight away

        view.findViewById<View>(R.id.btnSaveExercise).setOnClickListener { button ->
            save(button)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_TYPE, selectedType?.key)
    }

    // Dropdown: only the types of THIS section
    private fun setUpTypeDropdown(view: View, savedInstanceState: Bundle?) {
        val types = ExerciseType.forSection(section)
        val acType = view.findViewById<MaterialAutoCompleteTextView>(R.id.acExerciseType)

        acType.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, types.map { it.label })
        )
        // We restore the text ourselves below; letting Android do it filters the dropdown list
        acType.isSaveEnabled = false

        acType.setOnItemClickListener { _, _, position, _ ->
            selectType(types[position])
        }

        // Start with a type already chosen when: the phone was rotated, or the section
        // has only one type (Speaking)
        val initial = ExerciseType.fromKey(savedInstanceState?.getString(KEY_TYPE).orEmpty())
            ?.takeIf { it in types }
            ?: types.singleOrNull()
        initial?.let {
            acType.setText(it.label, false)
            selectType(it)
        }
    }

    // Show ONLY the form that belongs to the chosen type
    private fun selectType(type: ExerciseType) {
        selectedType = type
        hideError()

        val root = requireView()
        root.findViewById<View>(R.id.tvPickTypeHint).visibility = View.GONE
        ExerciseType.entries.forEach { t ->
            root.findViewById<View>(formIdFor(t)).visibility =
                if (t == type) View.VISIBLE else View.GONE
        }

        if (type == ExerciseType.MULTIPLE_CHOICE) applyMultipleChoiceLabels()
    }

    private fun formIdFor(type: ExerciseType): Int = when (type) {
        ExerciseType.MULTIPLE_CHOICE -> R.id.formMultipleChoice
        ExerciseType.MATCH_MEANING -> R.id.formMatchMeaning
        ExerciseType.COMPLETE_BLANK -> R.id.formCompleteBlank
        ExerciseType.SCRAMBLE -> R.id.formScramble
        ExerciseType.REPEAT_PHRASE -> R.id.formRepeatPhrase
    }

    // ------------------------------------------------------- multiple choice

    private fun setUpMultipleChoice(view: View) {
        // The 4 radio buttons sit in 4 separate layouts, so Android won't make them
        // exclusive. Do it by hand: checking one unchecks the other three.
        val radios = listOf(R.id.rbOptionA, R.id.rbOptionB, R.id.rbOptionC, R.id.rbOptionD)
            .map { view.findViewById<RadioButton>(it) }
        radios.forEach { radio ->
            radio.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) radios.filter { it !== radio }.forEach { it.isChecked = false }
            }
        }
    }

    // Same form, different meaning:
    //   Vocabulary: English word (spoken)   -> 4 Khmer meanings
    //   Reading:    Khmer sentence          -> 4 English sentences
    private fun applyMultipleChoiceLabels() {
        val root = requireView()
        val isReading = section == "Reading"

        root.findViewById<View>(R.id.etQuestion).parentInputLayout()?.let {
            it.hint = if (isReading) "Khmer sentence" else "English word (the app reads it aloud)"
            it.placeholderText = if (isReading) "ខ្ញុំបានទៅផ្សារ" else "borrow"
        }

        listOf(R.id.etOptionA, R.id.etOptionB, R.id.etOptionC, R.id.etOptionD)
            .forEachIndexed { i, id ->
                val letter = 'A' + i
                root.findViewById<View>(id).parentInputLayout()?.hint =
                    if (isReading) "English sentence $letter" else "Khmer meaning $letter"
            }
    }

    private fun buildMultipleChoice(): Built? {
        val promptName = if (section == "Reading") "Khmer sentence" else "English word"
        val prompt = text(R.id.etQuestion)
        val options = listOf(R.id.etOptionA, R.id.etOptionB, R.id.etOptionC, R.id.etOptionD)
            .map { text(it) }
        val radios = listOf(R.id.rbOptionA, R.id.rbOptionB, R.id.rbOptionC, R.id.rbOptionD)
            .map { requireView().findViewById<RadioButton>(it) }

        if (prompt.isEmpty()) { showError("Enter the $promptName."); return null }
        if (options.any { it.isEmpty() }) { showError("Fill in all 4 options."); return null }
        if (options.map { it.lowercase() }.toSet().size < options.size) {
            showError("Two options are the same. Every option must be different."); return null
        }
        val correctIndex = radios.indexOfFirst { it.isChecked }
        if (correctIndex < 0) { showError("Select the correct option."); return null }

        val payload = JSONObject()
            .put("prompt", prompt)
            .put("options", JSONArray(options))
            .put("correctIndex", correctIndex)
        return Built(prompt, payload)
    }

    // ---------------------------------------------------------------- scramble

    private fun setUpScramble(view: View) {
        val etSentence = view.findViewById<EditText>(R.id.etCorrectSentence)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupTiles)
        val tvHint = view.findViewById<TextView>(R.id.tvTilesHint)

        // live preview: one tile per word as the admin types
        etSentence.doAfterTextChanged { s ->
            val words = splitWords(s?.toString().orEmpty())
            chipGroup.removeAllViews()
            words.forEach { word ->
                val chip = Chip(requireContext())
                chip.text = word
                chip.isClickable = false
                chip.isCheckable = false
                chipGroup.addView(chip)
            }
            tvHint.visibility = if (words.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun buildScramble(): Built? {
        val khmer = text(R.id.etKhmerPrompt)
        val tiles = splitWords(text(R.id.etCorrectSentence))

        if (khmer.isEmpty()) { showError("Enter the Khmer prompt."); return null }
        if (tiles.isEmpty()) { showError("Enter the correct English sentence."); return null }
        if (tiles.size < 2) { showError("The sentence needs at least 2 words to scramble."); return null }

        val payload = JSONObject()
            .put("khmerPrompt", khmer)
            .put("correctSentence", tiles.joinToString(" "))
            .put("tiles", JSONArray(tiles))
        return Built(khmer, payload)
    }

    // ---------------------------------------------------------- match meaning

    private fun setUpMatchMeaning(view: View) {
        pairsAdapter = MatchMeaningAdapter(pairs)
        view.findViewById<RecyclerView>(R.id.rvPairs).adapter = pairsAdapter
        view.findViewById<View>(R.id.btnAddPair).setOnClickListener { pairsAdapter.addPair() }
    }

    private fun buildMatchMeaning(): Built? {
        // a row with only ONE box filled is a mistake
        if (pairs.any { it.inputWord.isBlank() != it.pairMeaning.isBlank() }) {
            showError("Fill in both the word and the meaning in every row, or leave the row empty.")
            return null
        }
        val filled = pairs
            .filter { it.inputWord.isNotBlank() && it.pairMeaning.isNotBlank() }
            .map { it.inputWord.trim() to it.pairMeaning.trim() }
        if (filled.size < 2) { showError("Add at least 2 word and meaning pairs."); return null }

        val array = JSONArray()
        filled.forEach { (word, meaning) ->
            array.put(JSONObject().put("word", word).put("meaning", meaning))
        }
        return Built(filled.joinToString(", ") { it.first }, JSONObject().put("pairs", array))
    }

    // --------------------------------------------------------- complete blank

    private fun buildCompleteBlank(): Built? {
        val sentence = text(R.id.etSentence)
        val correct = text(R.id.etCorrectAnswer)
        val wrong = text(R.id.etWrongOptions)
            .split(",").map { it.trim() }.filter { it.isNotEmpty() }.distinct()

        if (sentence.isEmpty()) { showError("Enter the sentence."); return null }
        if (sentence.split(BLANK).size != 2) {
            showError("Use $BLANK (three underscores) exactly once to mark the blank."); return null
        }
        if (correct.isEmpty()) { showError("Enter the correct answer."); return null }
        if (wrong.isEmpty()) {
            showError("Add at least one wrong option (separate with commas)."); return null
        }
        if (wrong.any { it.equals(correct, ignoreCase = true) }) {
            showError("A wrong option is the same as the correct answer."); return null
        }

        val payload = JSONObject()
            .put("sentence", sentence)
            .put("correctAnswer", correct)
            .put("wrongOptions", JSONArray(wrong))
        return Built(sentence, payload)
    }

    // ---------------------------------------------------------- repeat phrase

    private fun setUpRepeatPhrase(view: View) {
        val acAudio = view.findViewById<MaterialAutoCompleteTextView>(R.id.acAudioSource)
        acAudio.setAdapter(
            ArrayAdapter(
                requireContext(), android.R.layout.simple_list_item_1,
                listOf(AUDIO_TTS, AUDIO_UPLOAD)
            )
        )
        acAudio.isSaveEnabled = false
        acAudio.setText(AUDIO_TTS, false)
    }

    private fun buildRepeatPhrase(): Built? {
        val phrase = text(R.id.etPhrase)
        val audio = requireView().findViewById<MaterialAutoCompleteTextView>(R.id.acAudioSource)
            .text.toString()

        if (phrase.isEmpty()) { showError("Enter the phrase to repeat."); return null }
        if (audio == AUDIO_UPLOAD) {
            // needs file picking + Supabase Storage, so it comes later
            showError("Uploading audio is not ready yet. Choose text-to-speech for now.")
            return null
        }

        val payload = JSONObject()
            .put("phrase", phrase)
            .put("audioSource", "tts")
        return Built(phrase, payload)
    }

    // ------------------------------------------------------------------- save

    private fun save(saveButton: View) {
        hideError()

        val type = selectedType
        if (type == null) { showError("Pick a type of exercise."); return }

        val category = text(R.id.etCategory)
        if (category.isEmpty()) { showError("Enter a category (for example: Past tense)."); return }

        // each builder checks its own fields; on a problem it shows the error and returns null
        val built = when (type) {
            ExerciseType.MULTIPLE_CHOICE -> buildMultipleChoice()
            ExerciseType.MATCH_MEANING -> buildMatchMeaning()
            ExerciseType.COMPLETE_BLANK -> buildCompleteBlank()
            ExerciseType.SCRAMBLE -> buildScramble()
            ExerciseType.REPEAT_PHRASE -> buildRepeatPhrase()
        } ?: return

        saveButton.isEnabled = false          // stop double taps
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                vm.repo.addExercise(sessionId, type.key, category, built.prompt, built.payload.toString())
                findNavController().navigateUp()   // the list reloads itself
            } catch (e: Exception) {
                showError("Could not save: ${e.message}")
                saveButton.isEnabled = true
            }
        }
    }

    // ---------------------------------------------------------------- helpers

    // trimmed text of an EditText inside the form
    private fun text(id: Int): String =
        requireView().findViewById<EditText>(id).text.toString().trim()

    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    private fun hideError() {
        tvError.visibility = View.GONE
    }
}

// What a builder returns: the short text for the admin list + the JSON for the database
private class Built(val prompt: String, val payload: JSONObject)

private fun splitWords(sentence: String): List<String> =
    sentence.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }

// An EditText inside a TextInputLayout: walk up to find the layout (to change its hint)
private fun View.parentInputLayout(): TextInputLayout? {
    var p: ViewParent? = this.parent
    while (p != null && p !is TextInputLayout) p = p.parent
    return p as? TextInputLayout
}