package com.dnavarro.sidequest.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.Dp
import com.dnavarro.sidequest.model.CharacterClass
import com.dnavarro.sidequest.model.SkillIcon

/**
 * Placeholder art. To use real pixel art, drop a PNG into res/drawable named [drawableName]
 * (e.g. hero_wizard.png, skill_d20.png) and it's picked up automatically, drawn with
 * nearest-neighbour scaling so pixels stay crisp. Until then the emoji is shown.
 */
data class ArtSpec(val label: String, val emoji: String, val drawableName: String)

val SkillIcon.art: ArtSpec
    get() = when (this) {
        SkillIcon.FOOTSTEPS -> ArtSpec("Footsteps", "👣", "skill_footsteps")
        SkillIcon.SWORD -> ArtSpec("Sword", "🗡️", "skill_sword")
        SkillIcon.SPELLBOOK -> ArtSpec("Spellbook", "📖", "skill_spellbook")
        SkillIcon.SCROLL -> ArtSpec("Scroll", "📜", "skill_scroll")
        SkillIcon.DISHES -> ArtSpec("Dishes", "🍳", "skill_dishes")
        SkillIcon.BROOM -> ArtSpec("Broom", "🧹", "skill_broom")
        SkillIcon.D20 -> ArtSpec("D20", "🎲", "skill_d20")
        SkillIcon.TRAINING_DUMMY -> ArtSpec("Training Dummy", "🎯", "skill_training_dummy")
        SkillIcon.SHIELD -> ArtSpec("Shield", "🛡️", "skill_shield")
        SkillIcon.DRAGON -> ArtSpec("Dragon", "🐉", "skill_dragon")
        SkillIcon.BOW_AND_ARROW -> ArtSpec("Bow and Arrow", "🏹", "skill_bow_and_arrow")
        SkillIcon.QUILL_AND_PARCHMENT -> ArtSpec("Quill and Parchment", "🪶", "skill_quill_and_parchment")
        SkillIcon.POTION -> ArtSpec("Potion", "🧪", "skill_potion")
    }

val CharacterClass.art: ArtSpec
    get() = when (this) {
        CharacterClass.WARRIOR -> ArtSpec("Warrior", "⚔️", "hero_warrior")
        CharacterClass.WIZARD -> ArtSpec("Wizard", "🧙", "hero_wizard")
        CharacterClass.ARCHER -> ArtSpec("Archer", "🧝", "hero_archer")
        CharacterClass.ROGUE -> ArtSpec("Rogue", "🥷", "hero_rogue")
        CharacterClass.BARD -> ArtSpec("Bard", "🪕", "hero_bard")
    }

@SuppressLint("DiscouragedApi") // Looked up by name so art can be added without code changes.
@Composable
fun ArtImage(art: ArtSpec, size: Dp, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val resId = remember(art.drawableName) {
        context.resources.getIdentifier(art.drawableName, "drawable", context.packageName)
    }
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        if (resId != 0) {
            Image(
                bitmap = ImageBitmap.imageResource(resId),
                contentDescription = art.label,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.None,
            )
        } else {
            val fontSize = with(LocalDensity.current) { (size * 0.62f).toSp() }
            Text(art.emoji, fontSize = fontSize)
        }
    }
}
