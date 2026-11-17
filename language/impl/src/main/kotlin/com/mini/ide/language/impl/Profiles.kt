package com.mini.ide.language.impl

import com.mini.ide.language.api.LanguageId
import com.mini.ide.language.api.LanguageProfile

internal object HtmlProfile : LanguageProfile {
    override val id = LanguageId.HTML
    override val displayName = "HTML"
}
