package com.kazumaproject.markdownnote.other

sealed class FragmentType {
    object HomeFragment : FragmentType()
    object CreateEditFragment : FragmentType()
    object DraftFragment : FragmentType()
    object SettingFragment : FragmentType()
}
