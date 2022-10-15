package dev.latvian.mods.kubejs.platform.ingredient;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import org.jetbrains.annotations.Nullable;

public class CreativeTabIngredient extends KubeJSIngredient {
	public static final KubeJSIngredientSerializer<CreativeTabIngredient> SERIALIZER = new KubeJSIngredientSerializer<>(CreativeTabIngredient::new, CreativeTabIngredient::new);

	public final CreativeModeTab tab;

	public CreativeTabIngredient(CreativeModeTab tab) {
		this.tab = tab;
	}

	public CreativeTabIngredient(FriendlyByteBuf buf) {
		this(UtilsJS.findCreativeTab(buf.readUtf()));
	}

	public CreativeTabIngredient(JsonObject json) {
		this(UtilsJS.findCreativeTab(json.get("tab").getAsString()));
	}

	@Override
	public IIngredientSerializer<? extends Ingredient> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public boolean test(@Nullable ItemStack stack) {
		return stack != null && stack.getItem().getItemCategory() == tab;
	}

	@Override
	public void toJson(JsonObject json) {
		json.addProperty("tab", tab.getRecipeFolderName());
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeUtf(tab.getRecipeFolderName());
	}
}
