package dev.latvian.mods.kubejs.recipe.match;

import dev.latvian.mods.kubejs.item.ingredient.IngredientJS;
import dev.latvian.mods.rhino.Context;

public interface ReplacementMatch {
	ReplacementMatch NONE = new ReplacementMatch() {
		@Override
		public String toString() {
			return "NONE";
		}
	};

	static ReplacementMatch wrap(Context cx, Object o) {
		if (o == null) {
			return NONE;
		} else if (o instanceof ReplacementMatch m) {
			return m;
		} else {
			var in = IngredientJS.wrap(cx, o);
			return in.isEmpty() ? NONE : in;
		}
	}
}
