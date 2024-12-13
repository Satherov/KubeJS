package dev.latvian.mods.kubejs.recipe.component;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.fluid.FluidWrapper;
import dev.latvian.mods.kubejs.holder.HolderWrapper;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.schema.RecipeComponentFactory;
import dev.latvian.mods.kubejs.registry.RegistryType;
import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.kubejs.util.ID;
import dev.latvian.mods.kubejs.util.RegistryAccessContainer;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.type.TypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public record RegistryComponent<T>(Registry<T> registry, @Nullable RegistryType<T> regType, Codec<Holder<T>> codec) implements RecipeComponent<Holder<T>> {
	@SuppressWarnings({"rawtypes"})
	public static final RecipeComponentFactory FACTORY = (registries, storage, reader) -> {
		reader.skipWhitespace();
		reader.expect('<');
		reader.skipWhitespace();
		var regId = ResourceLocation.read(reader);
		reader.expect('>');
		var key = ResourceKey.createRegistryKey(regId);
		return new RegistryComponent(registries, key);
	};

	@SuppressWarnings({"unchecked", "rawtypes"})
	public RegistryComponent(RegistryAccessContainer registries, ResourceKey key) {
		this((Registry) registries.access().registry(key).orElseThrow(), (RegistryType) RegistryType.ofKey(key), RegistryFixedCodec.create(key));
	}

	@Override
	public Codec<Holder<T>> codec() {
		return codec;
	}

	@Override
	public TypeInfo typeInfo() {
		return regType == null || regType.type() == TypeInfo.STRING ? TypeInfo.STRING : TypeInfo.STRING.or(regType.type());
	}

	@Override
	@SuppressWarnings("unchecked")
	public Holder<T> wrap(Context cx, KubeRecipe recipe, Object from) {
		if (registry == BuiltInRegistries.ITEM) {
			if (from instanceof ItemStack is) {
				return (Holder<T>) is.getItem().builtInRegistryHolder();
			} else if (from instanceof Item item) {
				return (Holder<T>) item.builtInRegistryHolder();
			} else {
				return (Holder<T>) ItemStackJS.wrap(cx, from).getItemHolder();
			}
		} else if (registry == BuiltInRegistries.FLUID) {
			if (from instanceof FluidStack fs) {
				return (Holder<T>) fs.getFluid().builtInRegistryHolder();
			} else if (from instanceof Fluid fluid) {
				return (Holder<T>) fluid.builtInRegistryHolder();
			} else {
				return (Holder<T>) FluidWrapper.wrap(RegistryAccessContainer.of(cx), from).getFluidHolder();
			}
		} else if (regType != null) {
			return (Holder<T>) HolderWrapper.wrap((KubeJSContext) cx, from, regType.type());
		} else if (from instanceof ResourceKey<?> key) {
			return registry.getHolderOrThrow((ResourceKey) key);
		} else if (from instanceof CharSequence || from instanceof ResourceLocation) {
			return registry.getHolderOrThrow(ResourceKey.create(registry.key(), ID.mc(from.toString())));
		} else {
			throw new IllegalStateException("Missing key in " + registry.key() + ": " + from);
		}
	}

	@Override
	public boolean hasPriority(Context cx, KubeRecipe recipe, Object from) {
		return (regType != null && regType.baseClass().isInstance(from)) || (from instanceof CharSequence && ID.mc(from.toString()) != null) || (from instanceof JsonPrimitive json && json.isString() && ID.mc(json.getAsString()) != null);
	}

	@Override
	public void buildUniqueId(UniqueIdBuilder builder, Holder<T> value) {
		var id = value.getKey();

		if (id != null) {
			builder.append(id.location());
		}
	}

	@Override
	public String toString() {
		return "registry_element<" + registry.key().location() + ">";
	}
}
