package com.voicebudget.data.repository

import com.voicebudget.data.database.CustomCategoryEntity
import com.voicebudget.domain.model.CustomCategory
import com.voicebudget.domain.model.CustomCategoryIcon
import com.voicebudget.domain.model.TransactionType

fun CustomCategoryEntity.toDomain(): CustomCategory = CustomCategory(
    id = id,
    name = name,
    type = TransactionType.valueOf(type),
    icon = CustomCategoryIcon.valueOf(icon),
    color = color,
)

fun CustomCategory.toEntity(): CustomCategoryEntity = CustomCategoryEntity(
    id = id,
    name = name,
    type = type.name,
    icon = icon.name,
    color = color,
)
