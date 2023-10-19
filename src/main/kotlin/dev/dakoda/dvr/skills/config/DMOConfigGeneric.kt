package dev.dakoda.dvr.skills.config

import com.google.gson.annotations.SerializedName

class DMOConfigGeneric(
    @field:SerializedName("enabled") var enabled: Boolean = true,
    @field:SerializedName("default_hidden") var defaultHidden: Boolean = true
)
