package com.kazumaproject.markdownnote.ui.opensource

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.kazumaproject.markdownnote.databinding.FragmentOpenSourceBinding
import de.psdev.licensesdialog.LicensesDialog
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20
import de.psdev.licensesdialog.licenses.MITLicense
import de.psdev.licensesdialog.model.Notice

class OpenSourceFragment : Fragment() {

    private var _binding : FragmentOpenSourceBinding? = null
    private val binding get() = _binding!!

    companion object{
        val OPEN_SOURCE_LICENSES = listOf<String>(
            "androidx.core:core-ktx","androidx.appcompat:appcompat","com.google.android.material:material",
            "androidx.constraintlayout:constraintlayout","androidx.preference:preference-ktx","androidx.lifecycle:lifecycle-extensions",
            "androidx.lifecycle:lifecycle-livedata-ktx","androidx.lifecycle:lifecycle-runtime-ktx","androidx.lifecycle:lifecycle-viewmodel-ktx",
            "androidx.fragment:fragment-ktx", "org.jetbrains.kotlinx:kotlinx-coroutines-core", "org.jetbrains.kotlinx:kotlinx-coroutines-android",
            "androidx.activity:activity-ktx:1.6.1", "com.google.dagger:hilt-android", "com.google.dagger:hilt-android-compiler",
            "androidx.hilt:hilt-compiler", "androidx.room:room-runtime", "androidx.room:room-compiler",
            "androidx.room:room-ktx", "com.jakewharton.timber:timber", "androidx.navigation:navigation-fragment-ktx",
            "androidx.navigation:navigation-ui-ktx","androidx.swiperefreshlayout:swiperefreshlayout",
            "io.noties.markwon","io.noties:prism4j", "io.github.florent37:shapeofview", "androidx.emoji:emoji",
            "pl.droidsonroids.gif:android-gif-drawable", "com.caverock:androidsvg-aar", "com.google.code.gson:gson",
            "pub.hanks:smallbang", "com.github.vic797:android_native_code_view", "com.github.Irineu333:Highlight","de.psdev.licensesdialog:licensesdialog"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOpenSourceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, OPEN_SOURCE_LICENSES)
        binding.openSourceLicenseList.apply {
            adapter = arrayAdapter
            setOnItemClickListener { parent, view, position, id ->
                when(position){
                    0,1,2,3,4,5,6,7,8,9,12,13,14,15,16,17,18 ->{
                        val name = OPEN_SOURCE_LICENSES[position]
                        val copyright = "Copyright (c) 2005-2011, The Android Open Source Project"
                        val license = ApacheSoftwareLicense20()
                        val notice = Notice(name,"",copyright,license)
                        LicensesDialog.Builder(requireContext())
                            .setTitle("Apache Software License")
                            .setNotices(notice)
                            .build()
                            .show()
                    }
                    10,11 ->{
                        val name = OPEN_SOURCE_LICENSES[position]
                        val copyright = "Copyright (c) 2018 Wellington Costa"
                        val license = MITLicense()
                        val notice = Notice(name,"",copyright,license)
                        LicensesDialog.Builder(requireContext())
                            .setTitle("MIT Software License")
                            .setNotices(notice)
                            .build()
                            .show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}