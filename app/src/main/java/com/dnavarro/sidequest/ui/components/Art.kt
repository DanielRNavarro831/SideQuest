package com.dnavarro.sidequest.ui.components

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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
        SkillIcon.ARMOR -> ArtSpec("Armor", "🥋", "skill_armor")
        SkillIcon.HEART -> ArtSpec("Heart", "❤️", "skill_heart")
        SkillIcon.LUTE -> ArtSpec("Lute", "🪕", "skill_lute")
        SkillIcon.SKULL -> ArtSpec("Skull", "💀", "skill_skull")
        SkillIcon.TREASURE_CHEST -> ArtSpec("Treasure Chest", "🧰", "skill_treasure_chest")
        SkillIcon.TREE -> ArtSpec("Tree", "🌳", "skill_tree")
        SkillIcon.TURKEY_LEG -> ArtSpec("Turkey Leg", "🍗", "skill_turkey_leg")
    }

val CharacterClass.art: ArtSpec
    get() = when (this) {
        CharacterClass.WARRIOR -> ArtSpec("Warrior", "⚔️", "hero_warrior")
        CharacterClass.WIZARD -> ArtSpec("Wizard", "🧙", "hero_wizard")
        CharacterClass.ARCHER -> ArtSpec("Archer", "🧝", "hero_archer")
        CharacterClass.ROGUE -> ArtSpec("Rogue", "🥷", "hero_rogue")
        CharacterClass.BARD -> ArtSpec("Bard", "🪕", "hero_bard")
    }

/**
 * A button icon that uses custom art when res/drawable-nodpi/[artName].png exists (shown
 * untinted at [artSize]), and otherwise the built-in vector [fallback] tinted with [tint].
 */
@SuppressLint("DiscouragedApi")
@Composable
fun ButtonIcon(
    artName: String,
    @DrawableRes fallback: Int,
    contentDescription: String?,
    tint: Color,
    modifier: Modifier = Modifier,
    artSize: Dp = 30.dp,
) {
    val context = LocalContext.current
    val resId = remember(artName) {
        context.resources.getIdentifier(artName, "drawable", context.packageName)
    }
    if (resId != 0) {
        val bitmap = ImageBitmap.imageResource(resId)
        val boxPx = with(LocalDensity.current) { artSize.roundToPx() }
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = modifier.size(artSize),
            contentScale = ContentScale.Fit,
            filterQuality = if (bitmap.width < boxPx) FilterQuality.None else FilterQuality.High,
        )
    } else {
        Icon(painterResource(fallback), contentDescription, modifier.size(24.dp), tint = tint)
    }
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
            val bitmap = ImageBitmap.imageResource(resId)
            val boxPx = with(LocalDensity.current) { size.roundToPx() }
            Image(
                bitmap = bitmap,
                contentDescription = art.label,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                // Small pixel art is enlarged with crisp, blocky pixels; large images are
                // shrunk smoothly, since nearest-neighbour shrinking looks jagged.
                filterQuality = if (bitmap.width < boxPx) FilterQuality.None else FilterQuality.High,
            )
        } else {
            val fontSize = with(LocalDensity.current) { (size * 0.62f).toSp() }
            Text(art.emoji, fontSize = fontSize)
        }
    }
}
