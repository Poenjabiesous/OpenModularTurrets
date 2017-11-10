package omtteam.openmodularturrets.items.blocks;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import omtteam.openmodularturrets.handler.ConfigHandler;
import omtteam.openmodularturrets.reference.OMTNames;
import omtteam.openmodularturrets.reference.Reference;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.text.DecimalFormat;
import java.util.List;

import static omtteam.omlib.util.GeneralUtil.safeLocalize;

@SuppressWarnings("deprecation")
public class ItemBlockRocketTurret extends ItemBlockBaseAddon {
    private static final DecimalFormat df = new DecimalFormat("0.0");

    public ItemBlockRocketTurret(Block block) {
        super(block);
        this.setRegistryName(Reference.MOD_ID, OMTNames.Blocks.rocketTurret);
    }

    @Override
    @SuppressWarnings("unchecked")
    @ParametersAreNonnullByDefault
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(safeLocalize(OMTNames.Localizations.GUI.TURRET_HEAD_DESCRIPTION));
        tooltip.add("");
        tooltip.add(TextFormatting.GOLD + "--" + safeLocalize("tooltip.info") + "--");
        tooltip.add(safeLocalize("tooltip.tier") + ": " + TextFormatting.WHITE + "4");
        tooltip.add(safeLocalize("tooltip.range") + ": " + TextFormatting.WHITE +
                ConfigHandler.getRocketTurretSettings().getRange());
        tooltip.add(safeLocalize("tooltip.accuracy") + ": " + TextFormatting.WHITE +
                safeLocalize("turret.accuracy.exact"));
        tooltip.add(safeLocalize("tooltip.ammo") + ": " + TextFormatting.WHITE +
                safeLocalize("turret.ammo.3"));
        tooltip.add(safeLocalize("tooltip.tier_required") + ": " + TextFormatting.WHITE +
                safeLocalize("base.tier.4"));
        tooltip.add("");
        tooltip.add(
                TextFormatting.DARK_PURPLE + "--" + safeLocalize("tooltip.damage.label") + "--");
        tooltip.add(safeLocalize("tooltip.damage.stat") + ": " + TextFormatting.WHITE +
                (ConfigHandler.getRocketTurretSettings().getDamage() / 2F) + " " + safeLocalize(
                "tooltip.health"));
        tooltip.add(safeLocalize("tooltip.aoe") + ": " + TextFormatting.WHITE + "5");
        tooltip.add(safeLocalize("tooltip.fire_rate") + ": " + TextFormatting.WHITE + df.format(
                20.0F / ConfigHandler.getRocketTurretSettings().getFireRate()));
        tooltip.add(safeLocalize("tooltip.energy.stat") + ": " + TextFormatting.WHITE +
                ConfigHandler.getRocketTurretSettings().getPowerUsage() + " RF");
        tooltip.add("");
        tooltip.add(TextFormatting.DARK_GRAY + safeLocalize("flavour.turret.3"));
    }
}
